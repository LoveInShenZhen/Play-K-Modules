package K.Aop.annotations

import java.lang.annotation.Retention

import java.lang.annotation.ElementType.TYPE
import java.lang.annotation.RetentionPolicy.RUNTIME


/**
 * Created by kk on 14/11/28.
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.FILE)
@Retention(RUNTIME)
annotation class WithPersistLog
