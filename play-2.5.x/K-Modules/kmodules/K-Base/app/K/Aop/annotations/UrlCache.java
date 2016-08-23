package K.Aop.annotations;

import play.mvc.With;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by kk on 14-7-14.
 */
@With(K.Aop.actions.UrlCacheAction.class)
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface UrlCache {
    int ExpireTimeInSec() default 5;

    String[] ExcludedParams() default {"token", "_"};
}
