package daniel.portfolio.icecream.logging;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;

@Slf4j
@Component
@Aspect
public class ServiceLogger {

    private static final String REDACTED = "***";

    @Around("@within(org.springframework.stereotype.Service)")
    public Object logAround(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {

        Signature signature = proceedingJoinPoint.getSignature();
        Method method = ((MethodSignature) signature).getMethod();
        Object[] arguments = maskSensitiveArgs(method, proceedingJoinPoint.getArgs());
        Class<?> declaringClass = signature.getDeclaringType();

        log.info(
                "[{}] [{}] Arguments: {}",
                declaringClass.getSimpleName(),
                method.getName(),
                Arrays.toString(arguments)
        );

        Object returnValue = proceedingJoinPoint.proceed();

        Object loggedReturnValue = method.isAnnotationPresent(Sensitive.class)
                ? REDACTED : returnValue;
        log.info(
                "[{}] [{}] Returning: {}",
                declaringClass.getSimpleName(),
                method.getName(),
                loggedReturnValue
        );

        return returnValue;
    }

    private Object[] maskSensitiveArgs(Method method, Object[] args) {
        Annotation[][] parameterAnnotations = method.getParameterAnnotations();
        Object[] masked = args.clone();

        for (int i = 0; i < parameterAnnotations.length && i < masked.length; i++) {
            for (Annotation annotation : parameterAnnotations[i]) {
                if (annotation.annotationType().equals(Sensitive.class)) {
                    masked[i] = REDACTED;
                }
            }
        }

        return masked;
    }
}
