package controllers;


import K.Aop.annotations.Comment;
import K.Aop.annotations.JsonApi;
import K.Common.*;
import K.Controllers.JsonpController;
import K.DataDict.RoleType;
import K.Reply.StringReply;
import Wechat.AES.AesException;
import Wechat.AES.WXBizMsgCrypt;
import Wechat.AES.WeChatSHA1;

import Wechat.ReceiveMessage.Message.WXMessage;
import Wechat.ReceiveMessage.ReplyMessage.ReplyMsgBase;
import Wechat.ReceiveMessage.WeChatMsgBase;
import Wechat.WeChatApi;
import com.fasterxml.jackson.databind.JsonNode;
import controllers.Reply.LoginReply;
import jodd.util.StringUtil;
import jodd.util.URLCoder;
import models.K.User;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.EncoderException;
import org.apache.commons.codec.net.URLCodec;
import play.Logger;
import play.cache.Cache;
import play.mvc.BodyParser;
import play.mvc.Result;

import java.util.UUID;


/**
 * Created by kk on 15/6/10.
 */

@Comment("微信接入")
public class WeChatNotify extends JsonpController {

    // 针对 自定义菜单的动作 view：跳转URL 进行处理
    public static Result SmartRedirect(String code, String state) {

        // 根据 code 获取用户的 open_id
        JsonNode access_token_json = WeChatApi.QueryAccessTokenByCode(code);
        String open_id = access_token_json.get("openid").textValue();
        Logger.debug("==> SmartRedirect->Get OpenId: {}", open_id);

        User user = User.find.where()
                .eq("weixin_id", open_id)
                .findUnique();

        String token;
        if (user != null) {
            // 该 open_id 已经绑定到一个投资用户的账户上
            // 接下来判断, 是否需要创建一个 token

            // 首先试图从 cookie 里找到 token
            String token_cookie = request().cookie("token").value();

            boolean create_token = false;

            if (StringUtil.isNotBlank(token_cookie)) {
                // 已经有存在的 token 在 cookie 里

                // 先判断 token 是否有效
                TokenObject token_obj_cookie = Token.GetTokenObject(token_cookie);
                if (token_obj_cookie == null) {
                    // token 已经失效, 那就需要创建一个新的 token
                    create_token = true;
                } else {
                    // token 是有效的, 但是还是需要判断, 该 token 对应的用户是否和
                    // 当前的 open_id 相匹配
                    User user_by_cookie = User.find.where()
                            .eq("user_id", token_obj_cookie.user_id)
                            .findUnique();

                    if (user_by_cookie == null || !user_by_cookie.weixin_id.equals(open_id)) {
                        // 此 token 是过时的,无效的
                        create_token = true;
                    } else {
                        // 此 token 是有效的, 继续保持(增长其存在时间)
                        Token.KeepToken("token", Helper.ToJsonString(token_obj_cookie));
                    }
                }

            } else {
                // cookie 里没有已经存在的 token, 那就需要创建一个新的 token
                create_token = true;
            }

            if (create_token) {
                // 需要创建一个新的 token
                TokenObject tokenObject = new TokenObject();
                tokenObject.user_id = user.user_id;
                tokenObject.roles.add(RoleType.PersonInvestor.code);
                tokenObject.platform = ClientType.wechat;
                token = Token.NewToken(tokenObject);

            } else {
                token = token_cookie;
            }

        } else {
            // 该 open_id 没有绑定到用户, 无 token
            token = "NOTBIND";
        }

        String redirect_url;
        String prefix_part = "mob";

        // 根据 state 来判断是什么菜单(约定:不同的菜单项, 具有不同的state),
        if (state.equals("login")) {
            String open_id_auth_key = UUID.randomUUID().toString();
            String open_id_cache_key = String.format("WeChat.open_id.%s", open_id_auth_key);
            Cache.set(open_id_cache_key, open_id, 5 * 60);
            redirect_url = String.format("http://%s/%s/login.html?open_id_auth_key=%s",
                    WeChatApi.mobileSiteDomain(),
                    prefix_part,
                    open_id_auth_key);

        } else if (state.equals("logout")) {
            String open_id_auth_key = UUID.randomUUID().toString();
            String open_id_cache_key = String.format("WeChat.open_id.%s", open_id_auth_key);
            Cache.set(open_id_cache_key, open_id, 5 * 60);
            redirect_url = String.format("http://%s/%s/login_for_wechat.html?open_id_auth_key=%s&token=%s",
                    WeChatApi.mobileSiteDomain(),
                    prefix_part,
                    open_id_auth_key,
                    token);
        } else {
            redirect_url = String.format("http://%s/%s/%s.html?token=%s",
                    WeChatApi.mobileSiteDomain(),
                    prefix_part,
                    state,
                    token);
            if (StringUtil.isBlank(token)) {
                String back_url = URLCoder.encodeUri(String.format("http://%s/%s/%s.html",
                        prefix_part,
                        WeChatApi.mobileSiteDomain(),
                        state)
                );

                String open_id_auth_key = UUID.randomUUID().toString();
                String open_id_cache_key = String.format("WeChat.open_id.%s", open_id_auth_key);
                Cache.set(open_id_cache_key, open_id, 5 * 60);

                redirect_url = String.format("http://%s/%s/login_for_wechat.html?open_id_auth_key=%s&back_url=%s",
                        WeChatApi.mobileSiteDomain(),
                        prefix_part,
                        open_id_auth_key,
                        back_url
                );
            }
        }

        response().setCookie("client_type", ClientType.wechat);
        response().setCookie("token", token);

        return redirect(redirect_url);
    }

    public static Result SmartRedirectV2(String code, String state) throws DecoderException {

        // 根据 code 获取用户的 open_id
        JsonNode access_token_json = WeChatApi.QueryAccessTokenByCode(code);
        String open_id = access_token_json.get("openid").textValue();
        Logger.info("==> SmartRedirect->QueryAccessTokenByCode: {}", Helper.ToJsonStringPretty(access_token_json));

        if (StringUtil.isBlank(open_id)) {
            // todo: 无法获得 open_id, 则重定向到移动端的登录页面

            return redirect("/login.html");
        }

        User user = User.find.where()
                .eq("weixin_id", open_id)
                .findUnique();

        String token;
        if (user != null) {
            // 该 open_id 已经绑定到一个投资用户的账户上
            // 接下来判断, 是否需要创建一个 token

            // 首先试图从 cookie 里找到 token

            String token_cookie = "";

            if (request().cookie("token") != null) {
                token_cookie = request().cookie("token").value();
            }

            boolean create_token = false;

            if (StringUtil.isNotBlank(token_cookie)) {
                // 已经有存在的 token 在 cookie 里

                // 先判断 token 是否有效
                TokenObject token_obj_cookie = Token.GetTokenObject(token_cookie);
                if (token_obj_cookie == null) {
                    // token 已经失效, 那就需要创建一个新的 token
                    create_token = true;
                } else {
                    // token 是有效的, 但是还是需要判断, 该 token 对应的用户是否和
                    // 当前的 open_id 相匹配
                    User user_by_cookie = User.find.where()
                            .eq("user_id", token_obj_cookie.user_id)
                            .findUnique();

                    if (user_by_cookie == null || !user_by_cookie.weixin_id.equals(open_id)) {
                        // 此 token 是过时的,无效的
                        create_token = true;
                    } else {
                        // 此 token 是有效的, 继续保持(增长其存在时间)
                        Token.KeepToken("token", Helper.ToJsonString(token_obj_cookie));
                    }
                }

            } else {
                // cookie 里没有已经存在的 token, 那就需要创建一个新的 token
                create_token = true;
            }

            if (create_token) {
                // 需要创建一个新的 token
                TokenObject tokenObject = new TokenObject();
                tokenObject.user_id = user.user_id;
                tokenObject.roles.add(RoleType.PersonInvestor.code);
                tokenObject.platform = ClientType.wechat;
                token = Token.NewToken(tokenObject);

                // TODO: 16/4/25 生成用户微信登录的 event (通过 event bus)
                // 在用户登录事件的响应 handler 里, 同步第三方资金账户信息

            } else {
                token = token_cookie;
            }

        } else {
            // 该 open_id 没有绑定到用户, 无 token
            token = "NOTBIND";
        }

        response().setCookie("client_type", ClientType.wechat);
        response().setCookie("token", token);
        SaveOpenIdAuthKeyCookie(open_id);

        URLCodec urlCodec = new URLCodec("UTF-8");
        String decode_state = urlCodec.decode(state);

        if (decode_state.equals(RefreshToken)) {
            LoginReply reply = new LoginReply();
            reply.token = token;

            return ok(reply);

        } else {
            String redirect_url = String.format("http://%s%s",
                    WeChatApi.mobileSiteDomain(),
                    decode_state);

            return redirect(redirect_url);
        }
    }

    public static void SaveOpenIdAuthKeyCookie(String open_id) {
        if (StringUtil.isBlank(open_id)) {
            response().setCookie("open_id_auth_key", "");
            return;
        }

        String open_id_auth_key = Helper.Md5OfString(open_id);
        String open_id_cache_key = String.format("WeChat.open_id.%s", open_id_auth_key);
        Cache.set(open_id_cache_key, open_id, 5 * 60);

        response().setCookie("open_id_auth_key", open_id_auth_key);

        Logger.info("==> SaveOpenIdAuthKeyCookie: key : {} open_id : {}", open_id_auth_key, open_id);
    }

    public static String GetOpenIdFromCookie() {
        if (request().cookie("open_id_auth_key") == null) {
            return null;
        }

        String open_id_auth_key = request().cookie("open_id_auth_key").value();
        if (StringUtil.isBlank(open_id_auth_key)) {
            return null;
        }

        String open_id_cache_key = String.format("WeChat.open_id.%s", open_id_auth_key);
        String open_id = (String) Cache.get(open_id_cache_key);
        Logger.info("==> cookie: open_id_auth_key : {} , open_id from cache : {}", open_id_auth_key, open_id);
        return open_id;
    }

    private static final String RefreshToken = "RefreshToken";

    @Comment("获取给微信端用于刷新 token 用的url")
    @JsonApi(ReplyClass = StringReply.class)
    public static Result RefreshTokenUrl() throws EncoderException {
        StringReply reply = new StringReply();
        reply.result = WeChatApi.BuildRedirectUrl(RefreshToken);
        return ok(reply);
    }

    @BodyParser.Of(BodyParser.TolerantText.class)
    public static Result CallBack(String signature,
                                  String timestamp,
                                  String nonce,
                                  String encrypt_type,
                                  String msg_signature) throws Exception {

        Logger.debug("method: {} url: {}",
                request().method(),
                request().uri()
        );

        Logger.debug("Content-Type:{} post data: \n{}\n{}",
                request().getHeader("Content-Type"),
                Helper.ToJsonStringPretty(request().headers()),
                request().body().asText()
        );

        String plain_text = GetPostPlaintextData(signature, timestamp, nonce, encrypt_type, msg_signature);

        WXMessage message = new WXMessage();
        message.ParseFromStr(plain_text);

        WeChatMsgBase typed_msg = message.CreateByMsgType();

        Logger.debug("==> WeChat Msg Callback: {}\n{}", typed_msg.getClass().getSimpleName(), typed_msg.ToXmlStr());

        ReplyMsgBase reply_msg = typed_msg.ProcessMessage();

        String response_plain = reply_msg.ToXmlStr();
        Logger.debug("==> response_plain:\n{}", response_plain);

        String response_warpped = WrapperReplyText(timestamp, nonce, encrypt_type, reply_msg);
//        Logger.debug("response_warpped:\n{}", response_warpped);

        return ok(response_warpped).as("text/xml");
    }

    public static Result Verify(String signature,
                                String timestamp,
                                String nonce,
                                String echostr,
                                String encrypt_type,
                                String msg_signature) throws AesException {

        Logger.debug("method: {} url: {}",
                request().method(),
                request().uri()
        );

        if (!VerifySignature(signature, timestamp, nonce)) {
            return ok("签名校验失败");
        }

        return ok(echostr);
    }

    private static String GetPostPlaintextData(String signature,
                                               String timestamp,
                                               String nonce,
                                               String encrypt_type,
                                               String msg_signature) throws AesException {

        if (StringUtil.isNotBlank(signature)) {
            if (!VerifySignature(signature, timestamp, nonce)) {
                throw new BizLogicException("签名验证错误");
            }
        }

        if (StringUtil.isNotBlank(encrypt_type) && encrypt_type.equalsIgnoreCase("aes")) {
            // 安全模式
            WXBizMsgCrypt crypt = new WXBizMsgCrypt(WeChatApi.WeChatToken(),
                    WeChatApi.EncodingAESKey(),
                    WeChatApi.AppID()
            );

            String plain_text = crypt.decryptMsg(msg_signature, timestamp, nonce, request().body().asText());

            Logger.debug("==> 安全模式, 解密后明文:\n{}", plain_text);
            return plain_text;
        } else {
            // 明文模式
            String plain_text = request().body().asText();
            Logger.debug("==> 明文模式, 明文:\n{}", plain_text);
            return plain_text;
        }
    }

    private static String WrapperReplyText(String timestamp,
                                           String nonce,
                                           String encrypt_type,
                                           ReplyMsgBase reply_msg) throws Exception {

        if (StringUtil.isNotBlank(encrypt_type) && encrypt_type.equalsIgnoreCase("aes")) {
            // 安全模式
            WXBizMsgCrypt crypt = new WXBizMsgCrypt(WeChatApi.WeChatToken(),
                    WeChatApi.EncodingAESKey(),
                    WeChatApi.AppID()
            );

            return crypt.encryptMsg(reply_msg.ToXmlStr(),
                    timestamp,
                    nonce);

        } else {
            return reply_msg.ToXmlStr();
        }
    }

    private static boolean VerifySignature(String signature, String timestamp, String nonce) throws AesException {

        String chk_signature = WeChatSHA1.getSHA1(WeChatApi.WeChatToken(), timestamp, nonce, "");
        if (chk_signature.equalsIgnoreCase(signature)) {
            return true;
        } else {
            return false;
        }
    }


}
