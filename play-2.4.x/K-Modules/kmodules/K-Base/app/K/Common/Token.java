package K.Common;

import org.apache.commons.lang3.StringUtils;
import play.Configuration;
import play.cache.Cache;

import java.util.UUID;

/**
 * Created with IntelliJ IDEA.
 * User: kk
 * Date: 13-11-21
 * Time: 下午4:05
 * To change this template use File | Settings | File Templates.
 */
public class Token {

    private static final int TOKEN_TIMEOUOT;

    static {
        TOKEN_TIMEOUOT = Configuration.root().getInt("cg.token_timeout", 1800);
    }

    public static String NewToken(TokenObject tokenObject) {
        String token = UUID.randomUUID().toString();
        Cache.set(getCacheKey(token), Helper.ToJsonString(tokenObject), TOKEN_TIMEOUOT);
        return token;
    }

    public static String NewToken(TokenObject tokenObject, int timeout) {
        String token = UUID.randomUUID().toString();
        Cache.set(getCacheKey(token), Helper.ToJsonString(tokenObject), timeout);
        return token;
    }

    public static boolean TokenTimeout(String token) {
        String token_val = (String) Cache.get(getCacheKey(token));
        if (StringUtils.isBlank(token_val)) {
            return true;
        } else {
            KeepToken(token, token_val);
            return false;
        }
    }

    public static TokenObject GetTokenObject(String token) {
        String token_val = (String) Cache.get(getCacheKey(token));
        if (token_val == null) {
            return null;
        }
        KeepToken(token, token_val);
        return Helper.FromJsonString(token_val, TokenObject.class);
    }

    public static UUID GetUserId(String token) {
        TokenObject tokenObject = GetTokenObject(token);
        if (tokenObject == null) {
            throw new RuntimeException(String.format("Token 已经失效. token=%s", token));
        }
        return tokenObject.user_id;
    }

    public static void KeepToken(String token, String tokenVal) {
        Cache.set(getCacheKey(token), tokenVal, TOKEN_TIMEOUOT);
    }

    public static void RemoveToken(String token) {
        Cache.remove(getCacheKey(token));

    }

    private static String getCacheKey(String token) {
        return String.format("token.%s", token);
    }

    public static int getTokenTimeouot() {
        return TOKEN_TIMEOUOT;
    }


}
