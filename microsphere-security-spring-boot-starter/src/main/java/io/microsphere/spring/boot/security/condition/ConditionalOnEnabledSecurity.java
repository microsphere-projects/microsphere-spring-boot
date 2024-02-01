package io.microsphere.spring.boot.security.condition;

import io.microsphere.spring.boot.security.constants.SecurityConstants;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A condition annotation to enable Security
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@ConditionalOnProperty(prefix = SecurityConstants.PROPERTY_NAME_PREFIX, name = SecurityConstants.ENABLED_PROPERTY_NAME, matchIfMissing = true)
public @interface ConditionalOnEnabledSecurity {
}
