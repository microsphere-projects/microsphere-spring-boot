package io.microsphere.spring.boot.security.autoconfigure;

import io.microsphere.spring.boot.security.constants.SecurityConstants;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Web Security Properties
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
@ConfigurationProperties(prefix = SecurityConstants.WEB_SECURITY_PROPERTY_NAME_PREFIX)
public class WebSecurityProperties {

    private Hsts hsts = new Hsts();

    private Xss xss = new Xss();

    private XContentType xContentType = new XContentType();

    private XFrame xFrame = new XFrame();

    private Csp csp = new Csp();

    private Cors cors = new Cors();

    public Xss getXss() {
        return xss;
    }

    public void setXss(Xss xss) {
        this.xss = xss;
    }

    public Hsts getHsts() {
        return hsts;
    }

    public void setHsts(Hsts hsts) {
        this.hsts = hsts;
    }

    public XContentType getXContentType() {
        return xContentType;
    }

    public void setXContentType(XContentType xContentType) {
        this.xContentType = xContentType;
    }

    public XFrame getXFrame() {
        return xFrame;
    }

    public void setXFrame(XFrame xFrame) {
        this.xFrame = xFrame;
    }

    public Csp getCsp() {
        return csp;
    }

    public void setCsp(Csp csp) {
        this.csp = csp;
    }

    public Cors getCors() {
        return cors;
    }

    public void setCors(Cors cors) {
        this.cors = cors;
    }

    static class BaseConfig {

        private boolean enabled = false;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        @Override
        public String toString() {
            return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
        }
    }

    public static class Hsts extends BaseConfig {

        private int maxAgeInSeconds = 0;

        private boolean includeSubDomains = false;

        private boolean preload = false;

        public int getMaxAgeInSeconds() {
            return maxAgeInSeconds;
        }

        public void setMaxAgeInSeconds(int maxAgeInSeconds) {
            this.maxAgeInSeconds = maxAgeInSeconds;
        }

        public boolean isIncludeSubDomains() {
            return includeSubDomains;
        }

        public void setIncludeSubDomains(boolean includeSubDomains) {
            this.includeSubDomains = includeSubDomains;
        }

        public boolean isPreload() {
            return preload;
        }

        public void setPreload(boolean preload) {
            this.preload = preload;
        }
    }

    public static class Xss extends BaseConfig {

    }


    public static class XContentType extends BaseConfig {

    }

    public static class XFrame extends BaseConfig {

        private String option = "DENY";

        private String uri;

        public String getOption() {
            return option;
        }

        public void setOption(String option) {
            this.option = option;
        }

        public String getUri() {
            return uri;
        }

        public void setUri(String uri) {
            this.uri = uri;
        }
    }

    public static class Csp extends BaseConfig {

        private String policyDirectives;

        private boolean reportOnly;

        public String getPolicyDirectives() {
            return policyDirectives;
        }

        public void setPolicyDirectives(String policyDirectives) {
            this.policyDirectives = policyDirectives;
        }

        public boolean isReportOnly() {
            return reportOnly;
        }

        public void setReportOnly(boolean reportOnly) {
            this.reportOnly = reportOnly;
        }
    }

    public static class Cors extends BaseConfig {

        private Map<String, Config> configs = new LinkedHashMap<>();

        public Map<String, Config> getConfigs() {
            return configs;
        }

        public void setConfigs(Map<String, Config> configs) {
            this.configs = configs;
        }

        public static class Config {

            private List<String> allowedOrigins;

            private List<String> allowedMethods;

            private List<String> allowedHeaders;

            private List<String> exposedHeaders;

            private Boolean allowCredentials;

            private Long maxAge;

            public List<String> getAllowedOrigins() {
                return allowedOrigins;
            }

            public void setAllowedOrigins(List<String> allowedOrigins) {
                this.allowedOrigins = allowedOrigins;
            }

            public List<String> getAllowedMethods() {
                return allowedMethods;
            }

            public void setAllowedMethods(List<String> allowedMethods) {
                this.allowedMethods = allowedMethods;
            }

            public List<String> getAllowedHeaders() {
                return allowedHeaders;
            }

            public void setAllowedHeaders(List<String> allowedHeaders) {
                this.allowedHeaders = allowedHeaders;
            }

            public List<String> getExposedHeaders() {
                return exposedHeaders;
            }

            public void setExposedHeaders(List<String> exposedHeaders) {
                this.exposedHeaders = exposedHeaders;
            }

            public Boolean getAllowCredentials() {
                return allowCredentials;
            }

            public void setAllowCredentials(Boolean allowCredentials) {
                this.allowCredentials = allowCredentials;
            }

            public Long getMaxAge() {
                return maxAge;
            }

            public void setMaxAge(Long maxAge) {
                this.maxAge = maxAge;
            }
        }
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
