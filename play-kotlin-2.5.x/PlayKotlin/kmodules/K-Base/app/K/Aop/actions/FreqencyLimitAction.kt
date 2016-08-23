package K.Aop.actions


import K.Aop.annotations.FreqencyLimit
import K.Common.Hub
import K.Controllers.JsonpController
import K.Reply.ReplyBase
import org.apache.commons.lang3.StringUtils
import play.Logger
import play.mvc.Action
import play.mvc.Http
import play.mvc.Result

import java.util.HashMap
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletionStage

/**
 * Created by kk on 14/11/11.
 */
class FreqencyLimitAction : Action<FreqencyLimit>() {

    private var context: Http.Context? = null

    override fun call(ctx: Http.Context): CompletionStage<Result> {
        context = ctx

        val key = key
        val result: CompletionStage<Result>
        if (!Lock(key)) {
            val reply = ReplyBase()
            reply.ret = -1
            reply.errmsg = this.configuration.ErrMsg
            Logger.warn("TimeOutLockAction: {}", JsonpController.request().uri())
            val err_result = JsonpController.ok(reply)
            return CompletableFuture.completedFuture(err_result)
        } else {
            result = delegate.call(ctx)
        }
        return result
    }

    private val key: String
        get() {
            val sb = StringBuilder()
            sb.append("token.JsonApiTimeOutLock$")
            sb.append(context!!.request().uri())
            sb.append(String.format("#%s#", JsonpController.ClientIp()))

            if (context!!.request().method() == "GET") {
                for (paramName in this.configuration.IncludedParams) {
                    val param = context!!.request().getQueryString(paramName)
                    sb.append(param).append("#")
                }

            }

            if (context!!.request().method() == "POST") {
                val formData = context!!.request().body().asFormUrlEncoded()
                for (paramName in this.configuration.IncludedParams) {
                    sb.append(StringUtils.join(formData[paramName], "")).append("#")
                }
            }

            return sb.toString()
        }

    private fun Lock(key: String): Boolean {
        val lock = cache_lock
        synchronized (lock) {
            if (isLocked(key)) {
                // 已经被锁定
                return false
            } else {
                // 锁定
                Hub.cacheApi().set(key, context!!.request().uri(), this.configuration.TimeOutInSec)
                return true
            }
        }
    }

    private fun isLocked(key: String): Boolean {

        return Hub.cacheApi().get<Any>(key) != null
    }

    private val cacheLock: Any?
        get() {
            val keyName = context!!.request().uri()
            val lock = cache_lock
            synchronized (lock){

                if (!cache_lock.containsKey(keyName)) {
                    cache_lock.put(keyName, Object())
                }
                return cache_lock.get(keyName)
            }
        }

    companion object {

        private val cache_lock: HashMap<String, Any> = HashMap()
    }
}
