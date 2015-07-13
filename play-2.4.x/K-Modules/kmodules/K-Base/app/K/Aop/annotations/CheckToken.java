package K.Aop.annotations;

import play.mvc.With;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by kk on 13-12-16.
 */

@With(K.Aop.actions.CheckTokenAction.class)
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface CheckToken {
    String token_arg() default "token";

    int[] allowed_roles() default {};
}
