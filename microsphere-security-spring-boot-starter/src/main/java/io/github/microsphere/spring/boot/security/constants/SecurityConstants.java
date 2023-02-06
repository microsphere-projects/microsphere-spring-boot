package io.github.microsphere.spring.boot.security.constants;

/**
 * Security Constants
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public interface SecurityConstants {

    /**
     * The property name prefix of Security
     */
    String PROPERTY_NAME_PREFIX = "microsphere.security";

    /**
     * The property name of "Enabled"
     */
    String ENABLED_PROPERTY_NAME = "enabled";

    /**
     * The property name prefix of Web Security
     */
    String WEB_SECURITY_PROPERTY_NAME_PREFIX = PROPERTY_NAME_PREFIX + "." + "web";

    /**
     * The property name prefix of Web Security Filter
     */
    String WEB_SECURITY_FILTER_PROPERTY_NAME_PREFIX = WEB_SECURITY_PROPERTY_NAME_PREFIX + "." + "filter";

    /**
     * The property name prefix of Content Security Policy(CSP)
     */
    String WEB_SECURITY_CSP_PROPERTY_NAME_PREFIX = WEB_SECURITY_PROPERTY_NAME_PREFIX + "." + "csp";

    /**
     * The property name prefix of Cross-Origin Resource Sharing(CORS)
     */
    String WEB_SECURITY_CORS_PROPERTY_NAME_PREFIX = WEB_SECURITY_PROPERTY_NAME_PREFIX + "." + "cors";
}
