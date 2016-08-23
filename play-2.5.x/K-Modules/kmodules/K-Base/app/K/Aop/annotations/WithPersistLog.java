package K.Aop.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;


/**
 * Created by kk on 14/11/28.
 */
@Target(TYPE)
@Retention(RUNTIME)
public @interface WithPersistLog {
}
