package K.Aop.actions;


import K.Reply.ReplyBase;
import K.Aop.annotations.CheckCaptcha;
import K.Controllers.JsonpController;
import org.apache.commons.lang3.StringUtils;
import play.cache.Cache;
import play.libs.F;
import play.libs.Json;

import play.mvc.Action;
import play.mvc.Http;
import play.mvc.Result;


/**
 * Created by kk on 15/1/4.
 */
public class CheckCaptchaAction extends Action<CheckCaptcha> {

    @Override
    public F.Promise<Result> call(Http.Context ctx) throws Throwable {
        if (!checkCaptchaCode(ctx)) {
            ReplyBase reply = new ReplyBase();
            reply.ret = -2;
            reply.errmsg = "图形验证码错误";
            Result result = JsonpController.ok(Json.toJson(reply));
            return F.Promise.pure(result);
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
        return (String) Cache.get(cache_key);
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
