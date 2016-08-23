package Wechat;

import K.BizLogic.SysConfBL;
import K.Common.BizLogicException;
import K.Common.Helper;
import Wechat.PlanTasks.AutoGetAccessToken;
import com.fasterxml.jackson.databind.JsonNode;
import jodd.http.HttpRequest;
import jodd.http.HttpResponse;
import jodd.io.FileUtil;
import jodd.util.StringUtil;
import org.apache.commons.codec.EncoderException;
import org.apache.commons.codec.net.URLCodec;
import play.Configuration;
import play.Play;
import play.libs.Json;

import java.io.File;

/**
 * Created by kk on 15/6/10.
 */
public class WeChatApi {

    public static String WeChatToken() {
        return Hub.configuration().getString("WeChat.Token", "EBF802EC8463460B8FADBB4C5838F8DA");
    }

    public static String EncodingAESKey() {
        return Hub.configuration().getString("WeChat.EncodingAESKey", "Cp1SFHmMDHtOrkp9DUevjXXOmWBHCbPPYesG87Da0Cx");
    }

    public static String AppID() {
        return Hub.configuration().getString("WeChat.AppID", "wxb31aa0ff54f60d1f");
    }

    public static String AppSecret() {
        return Hub.configuration().getString("WeChat.AppSecret", "ef5c448ea289f3176e9bd62b7f8354a8");
    }

    public static String CachedAccessToken() {
        return SysConfBL.GetConf(AutoGetAccessToken.conf_key, null);
    }

    public static boolean IsSuccessed(JsonNode api_result) {
        if (api_result == null) {
            return false;
        }
        if (api_result.has("errcode")) {
            long errcode = api_result.get("errcode").longValue();
            if (errcode == 0) {
                return true;
            } else {
                return false;
            }
        }
        return true;
    }

    private static String ResponseText(HttpResponse response) {
        try {
            return new String(response.bodyBytes(), "UTF-8");
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    /*
     * 获取access token: http://mp.weixin.qq.com/wiki/11/0e4b294685f817b95cbed85ba5e82b8f.html
     */
    public static JsonNode QueryAccessToken() {
        String url = "https://api.weixin.qq.com/cgi-bin/token";
        HttpResponse response = HttpRequest.get(url)
                .query("grant_type", "client_credential")
                .query("appid", AppID())
                .query("secret", AppSecret())
                .send();

        return Json.parse(ResponseText(response));
    }

    /*
     * 获取微信服务器IP地址: http://mp.weixin.qq.com/wiki/0/2ad4b6bfd29f30f71d39616c2a0fcedc.html
     */
    public static JsonNode QueryCallbackIpList(String access_token) {
        String url = "https://api.weixin.qq.com/cgi-bin/getcallbackip";
        HttpResponse response = HttpRequest.get(url)
                .query("access_token", access_token)
                .send();

        return Json.parse(ResponseText(response));
    }

    /*
     * 检查指定的 access_token 是否有效
     */
    public static boolean CheckAccessToken(String access_token) {
        JsonNode result = QueryCallbackIpList(access_token);
        return IsSuccessed(result);
    }

    /*
     * 通过code换取网页授权access_token
     * http://mp.weixin.qq.com/wiki/17/c0f37d5704f0b64713d5d2c37b468d75.html
     */
    public static JsonNode QueryAccessTokenByCode(String code) {
        String url = "https://api.weixin.qq.com/sns/oauth2/access_token";

        HttpRequest query_request = HttpRequest.get(url)
                .query("appid", AppID())
                .query("secret", AppSecret())
                .query("code", code)
                .query("grant_type", "authorization_code");

        HttpResponse response = query_request.send();

        JsonNode result = Json.parse(ResponseText(response));
        if (result.has("access_token")) {
            return result;
        } else {
            throw new BizLogicException("Get access_token by code failed: %s\n%s",
                    query_request.url(),
                    ResponseText(response));
        }
    }

    /*
     * 自定义菜单查询接口: http://mp.weixin.qq.com/wiki/16/ff9b7b85220e1396ffa16794a9d95adc.html
     */
    public static JsonNode QueryMenu(String access_token) {
        String url = "https://api.weixin.qq.com/cgi-bin/menu/get";
        HttpResponse response = HttpRequest.get(url)
                .query("access_token", access_token)
                .send();

        return Json.parse(ResponseText(response));
    }

    /*
     * 查询本地的微信菜单配置: /conf/wechat/menu.(test|product).conf
     */
    public static JsonNode LocalMenuConfig() {
        try {
            String menu_conf;

            File menu_file = Play.application().getFile("/wechat/menu.conf");
            menu_conf = FileUtil.readUTFString(menu_file);

            return Json.parse(menu_conf);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    /*
     * 自定义菜单创建接口: http://mp.weixin.qq.com/wiki/13/43de8269be54a0a6f64413e4dfa94f39.html
     */
    public static JsonNode CreateMenu(String access_token, JsonNode menu_conf) {
        String url = "https://api.weixin.qq.com/cgi-bin/menu/create";
        HttpResponse response = HttpRequest.post(url)
                .query("access_token", access_token)
                .bodyText(Helper.ToJsonString(menu_conf), "application/json", "utf-8")
                .send();

        return Json.parse(ResponseText(response));
    }

    /*
     * 自定义菜单删除接口: http://mp.weixin.qq.com/wiki/16/8ed41ba931e4845844ad6d1eeb8060c8.html
     */
    public static JsonNode DeleteMenu(String access_token) {
        String url = "https://api.weixin.qq.com/cgi-bin/menu/delete";
        HttpResponse response = HttpRequest.get(url)
                .query("access_token", access_token)
                .send();

        return Json.parse(ResponseText(response));
    }

    // relative_path 跳转的相对路径, 以 '/ '开头, 不包含 http, domain
    public static String BuildRedirectUrl(String relative_path) throws EncoderException {
        URLCodec urlCodec = new URLCodec("UTF-8");
        String smart_redirect_url = urlCodec.encode(String.format("http://%s/api/wechat/SmartRedirect",
                mobileSiteDomain()));
        String redirect_url = urlCodec.encode(relative_path);

        return String.format("https://open.weixin.qq.com/connect/oauth2/authorize?appid=%s&redirect_uri=%s&response_type=code&scope=snsapi_base&state=%s#wechat_redirect",
                WeChatApi.AppID(),
                smart_redirect_url,
                redirect_url);
    }

    public static String mobileSiteDomain() {
        String domain = Play.application().configuration().getString("mobile_site_domain", null);
        if (StringUtil.isBlank(domain)) {
            throw new BizLogicException("请在 application.conf 里设置 mobile_site_domain");
        }

        return domain;
    }
}
