package K.Aop.annotations

import play.mvc.With
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy

/**
 * Created by kk on 13-12-16.
 */

@With(K.Aop.actions.CheckTokenAction::class)
@Target(AnnotationTarget.CLASS, AnnotationTarget.FILE, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER)
@Retention(RetentionPolicy.RUNTIME)
annotation class CheckToken(val token_arg: String = "token", val allowed_roles: IntArray = intArrayOf())
