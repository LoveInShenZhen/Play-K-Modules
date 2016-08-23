package K.Common

import org.apache.commons.lang3.StringUtils

import java.util.UUID

/**
 * Created with IntelliJ IDEA.
 * User: kk
 * Date: 13-11-21
 * Time: 下午4:05
 * To change this template use File | Settings | File Templates.
 */
object Token {

    val tokenTimeouot: Int

    init {
        tokenTimeouot = Hub.configuration().getInt("cg.token_timeout", 1800)!!
    }

    fun NewToken(tokenObject: TokenObject): String {
        val token = UUID.randomUUID().toString()
        Hub.cacheApi().set(getCacheKey(token), Helper.ToJsonString(tokenObject), tokenTimeouot)
        return token
    }

    fun NewToken(tokenObject: TokenObject, timeout: Int): String {
        val token = UUID.randomUUID().toString()
        Hub.cacheApi().set(getCacheKey(token), Helper.ToJsonString(tokenObject), timeout)
        return token
    }

    fun TokenTimeout(token: String): Boolean {
        val token_val = Hub.cacheApi().get<String>(getCacheKey(token))
        if (StringUtils.isBlank(token_val)) {
            return true
        } else {
            KeepToken(token, token_val)
            return false
        }
    }

    fun GetTokenObject(token: String): TokenObject? {
        val token_val = Hub.cacheApi().get<String>(getCacheKey(token)) ?: return null
        KeepToken(token, token_val)
        return Helper.FromJsonString(token_val, TokenObject::class.java)
    }

    fun GetUserId(token: String): UUID {
        val tokenObject = GetTokenObject(token) ?: throw BizLogicException(String.format("Token 已经失效. token=%s", token))
        return tokenObject.user_id!!
    }

    fun KeepToken(token: String, tokenVal: String) {
        Hub.cacheApi().set(getCacheKey(token), tokenVal, tokenTimeouot)
    }

    fun RemoveToken(token: String) {
        Hub.cacheApi().remove(getCacheKey(token))

    }

    private fun getCacheKey(token: String): String {
        return String.format("token.%s", token)
    }


}
