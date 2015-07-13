package K.Aop.actions;


import K.Reply.ReplyBase;
import K.Aop.annotations.FreqencyLimit;
import K.Controllers.JsonpController;
import org.apache.commons.lang3.StringUtils;
import play.Logger;
import play.cache.Cache;
import play.libs.F;
import play.libs.Json;

import play.mvc.Action;
import play.mvc.Http;
import play.mvc.Result;


import java.util.HashMap;
import java.util.Map;

/**
 * Created by kk on 14/11/11.
 */
public class FreqencyLimitAction extends Action<FreqencyLimit> {

    private Http.Context context;

    @Override
    public F.Promise<Result> call(Http.Context ctx) throws Throwable {
        context = ctx;

        String key = getKey();
        F.Promise<Result> result;
        if (!Lock(key)) {
            ReplyBase reply = new ReplyBase();
            reply.ret = -1;
            reply.errmsg = this.configuration.ErrMsg();
            Logger.warn("TimeOutLockAction: {}", JsonpController.request().uri());
            Result err_result = JsonpController.ok(Json.toJson(reply));
            return F.Promise.pure(err_result);
        } else {
            result = delegate.call(ctx);
        }
//        UnLock(key);
        return result;
    }

    private String getKey() {
        StringBuilder sb = new StringBuilder();
        sb.append("token.JsonApiTimeOutLock$");
        sb.append(context.request().uri());
        sb.append(String.format("#%s#", JsonpController.ClientIp()));

        if (context.request().method().equals("GET")) {
            for (String paramName : this.configuration.IncludedParams()) {
                String param = context.request().getQueryString(paramName);
                sb.append(param).append("#");
            }

        }

        if (context.request().method().equals("POST")) {
            Map<String, String[]> formData = context.request().body().asFormUrlEncoded();
            for (String paramName : this.configuration.IncludedParams()) {
                sb.append(StringUtils.join(formData.get(paramName), "")).append("#");
            }
        }

        return sb.toString();
    }

    private boolean Lock(String key) {
        Object lock = getCacheLock();
        synchronized (lock) {
            if (isLocked(key)) {
                // 已经被锁定
                return false;
            } else {
                // 锁定
                Cache.set(key, context.request().uri(), this.configuration.TimeOutInSec());
                return true;
            }
        }
    }

    private void UnLock(String key) {
        Object lock = getCacheLock();
        synchronized (lock) {
            Cache.remove(key);
        }
    }

    private boolean isLocked(String key) {

        return Cache.get(key) != null;
    }

    private Object getCacheLock() {
        String keyName = context.request().uri();
        synchronized (cache_lock) {
            if (!cache_lock.containsKey(keyName)) {
                cache_lock.put(keyName, new Object());
            }
            return cache_lock.get(keyName);
        }
    }

    private static HashMap<String, Object> cache_lock;

    static {
        cache_lock = new HashMap<>();
    }
}
