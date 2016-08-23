package controllers;


import K.Aop.annotations.Comment;
import K.Aop.annotations.JsonApi;
import K.Common.Helper;
import K.Controllers.JsonpController;
import K.Reply.JsonNodeReply;
import K.Reply.StringReply;
import Wechat.WeChatApi;
import com.fasterxml.jackson.databind.JsonNode;
import jodd.util.URLDecoder;
import org.apache.commons.codec.EncoderException;
import play.Logger;
import play.mvc.Result;


/**
 * Created by kk on 15/6/15.
 */

@Comment("微信接入")
public class WeChatMethods extends JsonpController {

    @Comment("构造微信端跳转的 url")
    @JsonApi(ReplyClass = StringReply.class)
    public static Result BuildRedirectUrl(String relative_path) throws EncoderException {
        StringReply reply = new StringReply();
        reply.result = WeChatApi.BuildRedirectUrl(relative_path);

        return ok(reply);
    }

    public static Result RedirectTo(String relative_path) throws EncoderException {
        String url = WeChatApi.BuildRedirectUrl(relative_path);

        return redirect(url);

    }

    @Comment("设置微信菜单")
    @JsonApi(ReplyClass = JsonNodeReply.class)
    public static Result SetupMenu() {
        JsonNodeReply reply = new JsonNodeReply();
        String access_token = WeChatApi.CachedAccessToken();
        JsonNode current_menu_config = WeChatApi.QueryMenu(access_token);
        JsonNode local_menu_config = WeChatApi.LocalMenuConfig();
        if (!current_menu_config.equals(local_menu_config)) {
            WeChatApi.DeleteMenu(access_token);
            WeChatApi.CreateMenu(access_token, local_menu_config);
            Logger.info("==> 微信菜单更新");
        } else {
            Logger.info("==> 微信菜单无需更新");
        }

        reply.data = WeChatApi.QueryMenu(access_token);

        return ok(reply);
    }

    @Comment("查询当前微信菜单配置")
    @JsonApi(ReplyClass = JsonNodeReply.class)
    public static Result QueryMenu() {
        JsonNodeReply reply = new JsonNodeReply();
        String access_token = WeChatApi.CachedAccessToken();
        reply.data = WeChatApi.QueryMenu(access_token);

        return ok(reply);
    }

    public static Result TestSmartRedirect(String redirect_url) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("url: %s\n", request().uri()));
        sb.append(String.format("redirect_url: %s\n", URLDecoder.decode(redirect_url)));
        sb.append(String.format("headers:\n%s\n", Helper.ToJsonStringPretty(request().headers())));

        return ok(sb.toString());
    }

}
