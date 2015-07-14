package K.Common;

import org.apache.commons.lang3.StringUtils;
import play.Logger;
import play.mvc.Controller;
import play.mvc.Http;

/**
 * Created by kk on 15/1/7.
 */
public class ClientType {
    public static final String wechat = "wechat";               // 微信客户端
    public static final String ios_app = "ios_app";             // iOS Application
    public static final String android_app = "android_app";     // Android Application
    public static final String m_browser = "m_browser";         // 移动端浏览器
    public static final String pc_browser = "pc_browser";       // PC 端浏览器
    public static final String wp_app = "wp_app";               // Windows Phone Application
    public static final String unknown = "";                    // 未指定

    public static boolean IsValid(String platform) {
        if (platform.equals(wechat)) {
            return true;
        }
        if (platform.equals(ios_app)) {
            return true;
        }
        if (platform.equals(android_app)) {
            return true;
        }
        if (platform.equals(m_browser)) {
            return true;
        }
        if (platform.equals(pc_browser)) {
            return true;
        }
        if (platform.equals(wp_app)) {
            return true;
        }

        return false;
    }

    private static String getToken() {
        String token;
        token = Controller.request().getQueryString("token");
        if (StringUtils.isBlank(token)) {
            Http.Cookie token_cookie = Controller.request().cookie("token");
            if (token_cookie != null) {
                token = token_cookie.value();
            }
        }
        return token;
    }

    private static String GetRequestBody() {
        Logger.info("==> http method: {}", Controller.request().method());
        if (Controller.request().method().equalsIgnoreCase("POST")) {
            return Helper.ToJsonStringPretty(Controller.request().body().asFormUrlEncoded());
        } else {
            return Helper.ToJsonStringPretty(Controller.request().queryString());
        }
    }

    public static String GetClientTypeFromRequest() {
        Http.Request request = Controller.request();
        Logger.info("url: {} \nbody text: {}", request.uri(), GetRequestBody());
        String token = getToken();
        if (StringUtils.isBlank(token)) {
            // QueryString 和 cookie 里都没有指定 token, 则根据 Agent 来判断
            return GetPlatformByAgent();
        }
        TokenObject tokenObject = Token.GetTokenObject(token);
        if (tokenObject == null) {
            // token 无效了, 则根据 Agent 来判断
            return GetPlatformByAgent();
        }
        // token 有效, 则根据 token 里的 platform 来判断
        return tokenObject.platform;
    }

    public static String GetPlatformByAgent() {
        Http.Request request = Controller.request();
        String UserAgent = request.getHeader("User-Agent");
        String appTypeCookie = "null";
        if (request.cookie("AppType") != null) {
            appTypeCookie = request.cookie("AppType").value();
        }

        if (IsMobile(UserAgent)) {
            if (IsWeChat(UserAgent)) {
                // 微信客户端
                return wechat;
            } else {
                if (IsIOS(UserAgent)) {
                    // ios
                    if (IsIOSApp(UserAgent)) {
                        // ios App
                        return ios_app;
                    } else {
                        return m_browser;
                    }
                } else {
                    // 非 ios 的移动端, 暂时都当做 android 来处理
                    if (IsAndroidApp(UserAgent, appTypeCookie)) {
                        return android_app;
                    } else {
                        return m_browser;
                    }
                }
            }
        } else {
            return pc_browser;
        }
    }

    public static boolean IsMobile(String UserAgent) {
        String lower = UserAgent.toLowerCase();
        return lower.contains("mobile")
                || lower.contains("android")
                || lower.contains("iphone")
                || lower.contains("ipad");
    }

    public static boolean IsIOS(String UserAgent) {
        String lower = UserAgent.toLowerCase();
        return lower.contains("iphone") || lower.contains("ipad");
    }

    public static boolean IsIOSApp(String UserAgent) {
        String lower = UserAgent.toLowerCase();
        return IsIOS(UserAgent) && !lower.contains("safari");
    }

    public static boolean IsIOSBrowser(String UserAgent) {
        String lower = UserAgent.toLowerCase();
        return IsIOS(UserAgent) && lower.contains("safari");
    }

    public static boolean IsAndroid(String UserAgent) {
        String lower = UserAgent.toLowerCase();
        return lower.contains("android");
    }

    public static boolean IsAndroidApp(String UserAgent, String AppTypeCookie) {
        if (StringUtils.isBlank(AppTypeCookie)) {
            return false;
        }
        return IsAndroid(UserAgent) && AppTypeCookie.equals("AndroidApp");
    }

    public static boolean IsAndroidBrowser(String UserAgent, String AppTypeCookie) {
        return IsAndroid(UserAgent) && !IsAndroidApp(UserAgent, AppTypeCookie);
    }

    public static boolean IsWeChat(String UserAgent) {
        String lower = UserAgent.toLowerCase();
        return lower.contains("micromessenger");
    }
}
