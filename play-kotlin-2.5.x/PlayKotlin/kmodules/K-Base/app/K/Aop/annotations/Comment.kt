package K.Aop.annotations

import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy

/**
 * Created by kk on 14/11/3.
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.FILE, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER, AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.FIELD)
@Retention(RetentionPolicy.RUNTIME)
annotation class Comment(val value: String = "")
