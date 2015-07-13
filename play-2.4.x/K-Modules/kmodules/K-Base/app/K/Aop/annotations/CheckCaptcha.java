package K.Aop.annotations;

import play.mvc.With;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by kk on 15/1/4.
 */
@With(K.Aop.actions.CheckCaptchaAction.class)
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface CheckCaptcha {
    String captcha_key_arg() default "captcha_key";

    String captcha_code_arg() default "captcha_code";
}
