package K.Aop.actions


import K.Aop.annotations.CheckCaptcha
import K.Controllers.JsonpController
import K.Reply.ReplyBase
import org.apache.commons.lang3.StringUtils
import play.cache.CacheApi
import play.mvc.Action
import play.mvc.Http
import play.mvc.Result

import javax.inject.Inject
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletionStage


/**
 * Created by kk on 15/1/4.
 */
class CheckCaptchaAction
@Inject
constructor(private val cache: CacheApi) : Action<CheckCaptcha>() {

    override fun call(ctx: Http.Context): CompletionStage<Result> {
        if (!checkCaptchaCode(ctx)) {
            val reply = ReplyBase()
            reply.ret = -2
            reply.errmsg = "图形验证码错误"
            val result = JsonpController.ok(reply)
            return CompletableFuture.completedFuture(result)
        }

        return delegate.call(ctx)
    }

    private fun getCaptchaKey(ctx: Http.Context): String {
        return ctx.request().getQueryString(this.configuration.captcha_key_arg)
    }

    private fun getCaptchaCode(ctx: Http.Context): String {
        return ctx.request().getQueryString(this.configuration.captcha_code_arg)
    }

    private fun getAnswer(captcha_key: String): String {
        val cache_key = String.format("captcha.%s", captcha_key)
        return cache.get<String>(cache_key)
    }

    private fun checkCaptchaCode(ctx: Http.Context): Boolean {
        val CaptchaKey = getCaptchaKey(ctx)
        val CaptchaCode = getCaptchaCode(ctx)
        if (StringUtils.isBlank(CaptchaKey)) {
            return false
        }
        if (StringUtils.isBlank(CaptchaCode)) {
            return false
        }

        val answer = getAnswer(CaptchaKey)
        if (StringUtils.isBlank(answer)) {
            return false
        }

        if (answer.equals(CaptchaCode, ignoreCase = true)) {
            return true
        } else {
            return false
        }

    }
}
