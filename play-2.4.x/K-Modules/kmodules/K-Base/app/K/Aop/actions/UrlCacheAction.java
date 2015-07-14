package K.Aop.actions;


import K.Common.InProcessCache;
import K.Aop.annotations.UrlCache;
import org.apache.commons.lang3.StringUtils;
import play.libs.F;
import play.mvc.Action;
import play.mvc.Http;
import play.mvc.Result;

import java.util.TreeMap;


/**
 * Created by kk on 14-7-14.
 */
public class UrlCacheAction extends Action<UrlCache> {
    @Override
    public F.Promise<Result> call(Http.Context ctx) throws Throwable {
        try {
            final String key = getKeyByUrl(ctx);
            final Integer expireTime = this.configuration.ExpireTimeInSec();
            Result result = (Result) InProcessCache.Get(key);
            F.Promise<Result> promise;
            if (result == null) {
                promise = delegate.call(ctx);
                promise.onRedeem(new F.Callback<Result>() {
                    @Override
                    public void invoke(Result Result) throws Throwable {
                        InProcessCache.Put(key, Result, expireTime);
                    }
                });
            } else {
                promise = F.Promise.pure(result);
            }
            return promise;

        } catch (RuntimeException e) {
            throw e;
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    private String getKeyByUrl(final Http.Context ctx) {
        TreeMap<String, String[]> paramsMap;
        if (ctx.request().method().equals("POST")) {
            paramsMap = new TreeMap<>(ctx.request().body().asFormUrlEncoded());
        } else if (ctx.request().method().equals("GET")) {
            paramsMap = new TreeMap<>(ctx.request().queryString());
        } else {
            throw new RuntimeException("UrlCache annotation only support POST and GET");
        }

        for (String excludeParam : this.configuration.ExcludedParams()) {
            if (paramsMap.containsKey(excludeParam)) {
                paramsMap.remove(excludeParam);
            }
        }

        StringBuilder sb = new StringBuilder();
        int idx = ctx.request().uri().indexOf("?");
        if (idx > 0) {
            sb.append(ctx.request().uri().substring(0, idx));
        } else {
            sb.append(ctx.request().uri());
        }
        sb.append("#");

        for (String paramKey : paramsMap.keySet()) {
            sb.append("$").append(paramKey);
            String[] value = paramsMap.get(paramKey);
            sb.append("@").append(StringUtils.join(value, ""));
        }

        return sb.toString();
    }
}
