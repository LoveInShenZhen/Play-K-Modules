package K.Aop.annotations;

import play.mvc.With;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by kk on 14-6-10.
 */
@With(K.Aop.actions.JsonApiAction.class)
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface JsonApi {

    boolean Transactional() default true;

    boolean UseEtag() default false;

    Class ReplyClass();

    String ApiMethodType() default "GET";

    Class PostDataClass() default Object.class;
}
