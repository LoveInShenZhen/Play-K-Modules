package K.Aop.annotations

import play.mvc.With
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy

/**
 * Created by kk on 15/1/4.
 */
@With(K.Aop.actions.CheckCaptchaAction::class)
@Target(AnnotationTarget.CLASS, AnnotationTarget.FILE, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER)
@Retention(RetentionPolicy.RUNTIME)
annotation class CheckCaptcha(val captcha_key_arg: String = "captcha_key", val captcha_code_arg:

String = "captcha_code")
