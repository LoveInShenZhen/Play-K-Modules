package K.Aop.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Created by kk on 14/11/14.
 */

@Target({METHOD, FIELD})
@Retention(RUNTIME)
public @interface DBIndexed {

}
