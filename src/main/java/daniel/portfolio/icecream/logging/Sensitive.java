package daniel.portfolio.icecream.logging;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// On a parameter: value is never logged (a raw password, a raw token).
// On a method: return value is never logged.
// ServiceLogger checks prints "***" instead.
@Target({ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Sensitive {
}
