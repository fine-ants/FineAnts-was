package codesquad.fineants.spring.auth;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Order(Ordered.HIGHEST_PRECEDENCE)
public @interface HasNotificationAuthorization {

}
