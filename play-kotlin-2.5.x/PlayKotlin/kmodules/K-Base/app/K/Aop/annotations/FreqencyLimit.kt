package K.Aop.annotations

import K.Aop.actions.FreqencyLimitAction
import play.mvc.With
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy

/**
 * Created by kk on 14/11/11.
 */
@With(FreqencyLimitAction::class)
@Target(AnnotationTarget.CLASS, AnnotationTarget.FILE, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER)
@Retention(RetentionPolicy.RUNTIME)
annotation class FreqencyLimit(val IncludedParams: Array<String> = arrayOf(), val TimeOutInSec: Int = 5, val ErrMsg:

String = "调用频繁,超过限制,请稍后重试")
