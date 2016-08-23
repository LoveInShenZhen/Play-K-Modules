package K.Common

import org.apache.commons.lang3.StringUtils
import play.Logger
import play.mvc.Controller

/**
 * Created by kk on 15/1/7.
 */
object ClientType {
    val wechat = "wechat"               // 微信客户端
    val ios_app = "ios_app"             // iOS Application
    val android_app = "android_app"     // Android Application
    val m_browser = "m_browser"         // 移动端浏览器
    val pc_browser = "pc_browser"       // PC 端浏览器
    val wp_app = "wp_app"               // Windows Phone Application
    val unknown = ""                    // 未指定

    fun IsValid(platform: String): Boolean {
        if (platform == wechat) {
            return true
        }
        if (platform == ios_app) {
            return true
        }
        if (platform == android_app) {
            return true
        }
        if (platform == m_browser) {
            return true
        }
        if (platform == pc_browser) {
            return true
        }
        if (platform == wp_app) {
            return true
        }

        return false
    }

    private val token: String
        get() {
            var token: String
            token = Controller.request().getQueryString("token")
            if (StringUtils.isBlank(token)) {
                val token_cookie = Controller.request().cookie("token")
                if (token_cookie != null) {
                    token = token_cookie.value()
                }
            }
            return token
        }

    private fun GetRequestBody(): String {
        Logger.info("==> http method: {}", Controller.request().method())
        if (Controller.request().method().equals("POST", ignoreCase = true)) {
            return Helper.ToJsonStringPretty(Controller.request().body().asFormUrlEncoded())
        } else {
            return Helper.ToJsonStringPretty(Controller.request().queryString())
        }
    }

    fun GetClientTypeFromRequest(): String {
        val request = Controller.request()
        Logger.info("url: {} \nbody text: {}", request.uri(), GetRequestBody())
        val token = token
        if (StringUtils.isBlank(token)) {
            // QueryString 和 cookie 里都没有指定 token, 则根据 Agent 来判断
            return GetPlatformByAgent()
        }
        val tokenObject = Token.GetTokenObject(token) ?: return GetPlatformByAgent() // token 无效了, 则根据 Agent 来判断

        // token 有效, 则根据 token 里的 platform 来判断
        return tokenObject.platform
    }

    fun GetPlatformByAgent(): String {
        val request = Controller.request()
        val UserAgent = request.getHeader("User-Agent")
        var appTypeCookie = "null"
        if (request.cookie("AppType") != null) {
            appTypeCookie = request.cookie("AppType").value()
        }

        if (IsMobile(UserAgent)) {
            if (IsWeChat(UserAgent)) {
                // 微信客户端
                return wechat
            } else {
                if (IsIOS(UserAgent)) {
                    // ios
                    if (IsIOSApp(UserAgent)) {
                        // ios App
                        return ios_app
                    } else {
                        return m_browser
                    }
                } else {
                    // 非 ios 的移动端, 暂时都当做 android 来处理
                    if (IsAndroidApp(UserAgent, appTypeCookie)) {
                        return android_app
                    } else {
                        return m_browser
                    }
                }
            }
        } else {
            return pc_browser
        }
    }

    fun IsMobile(UserAgent: String): Boolean {
        val lower = UserAgent.toLowerCase()
        return lower.contains("mobile")
                || lower.contains("android")
                || lower.contains("iphone")
                || lower.contains("ipad")
    }

    fun IsIOS(UserAgent: String): Boolean {
        val lower = UserAgent.toLowerCase()
        return lower.contains("iphone") || lower.contains("ipad")
    }

    fun IsIOSApp(UserAgent: String): Boolean {
        val lower = UserAgent.toLowerCase()
        return IsIOS(UserAgent) && !lower.contains("safari")
    }

    fun IsIOSBrowser(UserAgent: String): Boolean {
        val lower = UserAgent.toLowerCase()
        return IsIOS(UserAgent) && lower.contains("safari")
    }

    fun IsAndroid(UserAgent: String): Boolean {
        val lower = UserAgent.toLowerCase()
        return lower.contains("android")
    }

    fun IsAndroidApp(UserAgent: String, AppTypeCookie: String): Boolean {
        if (StringUtils.isBlank(AppTypeCookie)) {
            return false
        }
        return IsAndroid(UserAgent) && AppTypeCookie == "AndroidApp"
    }

    fun IsAndroidBrowser(UserAgent: String, AppTypeCookie: String): Boolean {
        return IsAndroid(UserAgent) && !IsAndroidApp(UserAgent, AppTypeCookie)
    }

    fun IsWeChat(UserAgent: String): Boolean {
        val lower = UserAgent.toLowerCase()
        return lower.contains("micromessenger")
    }
}
