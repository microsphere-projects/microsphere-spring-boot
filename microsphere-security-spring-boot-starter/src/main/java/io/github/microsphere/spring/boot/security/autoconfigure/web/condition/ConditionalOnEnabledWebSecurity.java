package io.github.microsphere.spring.boot.security.autoconfigure.web.condition;

import io.github.microsphere.spring.boot.security.autoconfigure.condition.ConditionalOnEnabledSecurity;
import io.github.microsphere.spring.boot.security.constants.SecurityConstants;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A condition annotation to enable Web Security
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@ConditionalOnEnabledSecurity
@ConditionalOnProperty(prefix = SecurityConstants.WEB_SECURITY_PROPERTY_NAME_PREFIX, name = SecurityConstants.ENABLED_PROPERTY_NAME)
public @interface ConditionalOnEnabledWebSecurity {
}
