package K.Aop.actions


import K.Aop.annotations.CheckToken
import K.Common.Token
import K.Controllers.JsonpController
import K.Reply.ReplyBase
import org.apache.commons.lang3.StringUtils
import play.mvc.Action
import play.mvc.Http
import play.mvc.Result
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletionStage


/**
 * Created by kk on 13-12-16.
 */
class CheckTokenAction : Action<CheckToken>() {

    override fun call(ctx: Http.Context): CompletionStage<Result> {
        val token = ctx.request().getQueryString(this.configuration.token_arg)
        val reply = ReplyBase()
        if (StringUtils.isBlank(token)) {
            reply.ret = -3
            reply.errmsg = "请登录"
            val result = JsonpController.ok(reply)
            return CompletableFuture.completedFuture(result)
        }

        if (Token.TokenTimeout(token)) {
            reply.ret = -3
            reply.errmsg = "请重新登录"
            val result = JsonpController.ok(reply)
            return CompletableFuture.completedFuture(result)
        }

        val tokenObject = Token.GetTokenObject(token)

        if (this.configuration.allowed_roles.size > 0) {
            var has_role = false
            for (roleType in this.configuration.allowed_roles) {
                if (tokenObject!!.HasRole(roleType)) {
                    has_role = true
                    break
                }
            }
            if (!has_role) {
                // 没有权限调用此 api
                reply.ret = -4
                reply.errmsg = "没有权限调用此 api"
                val result = JsonpController.ok(reply)
                return CompletableFuture.completedFuture(result)
            }
        }

        return delegate.call(ctx)
    }


    private fun getApiUrl(ctx: Http.Context): String {
        val url = ctx.request().uri()
        val idx = url.indexOf("?")
        if (idx == -1) {
            return url
        } else {
            return url.substring(0, idx)
        }
    }
}