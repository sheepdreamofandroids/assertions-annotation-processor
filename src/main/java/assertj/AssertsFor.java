/** */
package assertj;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.SOURCE;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Documented
@Retention(SOURCE)
@Target({TYPE, METHOD})
/** @author gbloemsma */
public @interface AssertsFor {
  /** List of classes that assertions will be generated for */
  Class<?>[] value();
}
