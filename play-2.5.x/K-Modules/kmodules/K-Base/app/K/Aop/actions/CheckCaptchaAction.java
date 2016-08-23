package K.Aop.actions;


import K.Aop.annotations.CheckCaptcha;
import K.Controllers.JsonpController;
import K.Reply.ReplyBase;
import org.apache.commons.lang3.StringUtils;
import play.cache.CacheApi;
import play.mvc.Action;
import play.mvc.Http;
import play.mvc.Result;

import javax.inject.Inject;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;


/**
 * Created by kk on 15/1/4.
 */
public class CheckCaptchaAction extends Action<CheckCaptcha> {

    private final CacheApi cache;

    @Inject
    public CheckCaptchaAction(CacheApi Cache) {
        this.cache = Cache;
    }

    @Override
    public CompletionStage<Result> call(Http.Context ctx) {
        if (!checkCaptchaCode(ctx)) {
            ReplyBase reply = new ReplyBase();
            reply.ret = -2;
            reply.errmsg = "图形验证码错误";
            Result result = JsonpController.ok(reply);
            return CompletableFuture.completedFuture(result);
        }

        return delegate.call(ctx);
    }

    private String getCaptchaKey(Http.Context ctx) {
        return ctx.request().getQueryString(this.configuration.captcha_key_arg());
    }

    private String getCaptchaCode(Http.Context ctx) {
        return ctx.request().getQueryString(this.configuration.captcha_code_arg());
    }

    private String getAnswer(String captcha_key) {
        String cache_key = String.format("captcha.%s", captcha_key);
        return (String) cache.get(cache_key);
    }

    private boolean checkCaptchaCode(Http.Context ctx) {
        String CaptchaKey = getCaptchaKey(ctx);
        String CaptchaCode = getCaptchaCode(ctx);
        if (StringUtils.isBlank(CaptchaKey)) {
            return false;
        }
        if (StringUtils.isBlank(CaptchaCode)) {
            return false;
        }

        String answer = getAnswer(CaptchaKey);
        if (StringUtils.isBlank(answer)) {
            return false;
        }

        if (answer.equalsIgnoreCase(CaptchaCode)) {
            return true;
        } else {
            return false;
        }

    }
}
