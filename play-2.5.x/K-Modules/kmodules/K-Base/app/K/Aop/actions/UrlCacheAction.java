package K.Aop.actions;


import K.Aop.annotations.UrlCache;
import K.Common.InProcessCache;
import org.apache.commons.lang3.StringUtils;
import play.mvc.Action;
import play.mvc.Http;
import play.mvc.Result;

import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;


/**
 * Created by kk on 14-7-14.
 */
public class UrlCacheAction extends Action<UrlCache> {
    @Override
    public CompletionStage<Result> call(Http.Context ctx) {
        try {
            final String key = getKeyByUrl(ctx);
            final Integer expireTime = this.configuration.ExpireTimeInSec();
            Result result = (Result) InProcessCache.Get(key);
            CompletionStage<Result> middleResult;
            if (result == null) {
                middleResult = delegate.call(ctx);
                middleResult.whenComplete((result1, throwable) -> InProcessCache.Put(key, result1, expireTime));
            } else {
                middleResult = CompletableFuture.completedFuture(result);
            }
            return middleResult;

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
