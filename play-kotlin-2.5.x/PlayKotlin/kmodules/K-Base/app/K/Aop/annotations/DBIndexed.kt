package K.Aop.annotations

import java.lang.annotation.Retention

import java.lang.annotation.ElementType.FIELD
import java.lang.annotation.ElementType.METHOD
import java.lang.annotation.RetentionPolicy.RUNTIME

/**
 * Created by kk on 14/11/14.
 */

@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER, AnnotationTarget.FIELD)
@Retention(RUNTIME)
annotation class DBIndexed
