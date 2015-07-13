package K.Aop.annotations;

import K.Aop.actions.FreqencyLimitAction;
import play.mvc.With;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by kk on 14/11/11.
 */
@With(FreqencyLimitAction.class)
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface FreqencyLimit {
    public String[] IncludedParams() default {};

    public int TimeOutInSec() default 5;

    public String ErrMsg() default "调用频繁,超过限制,请稍后重试";
}
