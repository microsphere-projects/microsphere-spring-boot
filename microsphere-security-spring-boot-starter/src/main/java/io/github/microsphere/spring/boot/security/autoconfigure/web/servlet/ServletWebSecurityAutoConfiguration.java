package io.github.microsphere.spring.boot.security.autoconfigure.web.servlet;

import io.github.microsphere.spring.boot.security.annotation.WebSecurityFilter;
import io.github.microsphere.spring.boot.security.autoconfigure.WebSecurityProperties;
import io.github.microsphere.spring.boot.security.autoconfigure.web.condition.ConditionalOnEnabledWebSecurity;
import io.github.microsphere.spring.boot.security.constants.SecurityConstants;
import io.github.microsphere.spring.boot.security.web.servlet.WebSecurityProcessor;
import io.github.microsphere.spring.boot.security.web.servlet.WebSecurityProcessorFilter;
import io.github.microsphere.spring.boot.security.web.servlet.csp.ContentSecurityPolicyProcessor;
import org.apache.catalina.filters.HttpHeaderSecurityFilter;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CompositeFilter;
import org.springframework.web.filter.CorsFilter;

import javax.servlet.Filter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static javax.servlet.DispatcherType.ERROR;
import static javax.servlet.DispatcherType.FORWARD;
import static javax.servlet.DispatcherType.REQUEST;
import static org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type.SERVLET;
import static org.springframework.boot.autoconfigure.web.servlet.DispatcherServletAutoConfiguration.DEFAULT_DISPATCHER_SERVLET_BEAN_NAME;
import static org.springframework.util.StringUtils.hasText;


/**
 * Servlet Web Auto-Configuration
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
@ConditionalOnEnabledWebSecurity
@ConditionalOnWebApplication(type = SERVLET)
@ConditionalOnClass(name = "org.apache.catalina.startup.Tomcat")
@AutoConfigureBefore(name = {
        "org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration",
        "org.springframework.boot.autoconfigure.web.servlet.DispatcherServletAutoConfiguration"
})
@Import(value = {ServletWebSecurityAutoConfiguration.TomcatConfiguration.class})
@EnableConfigurationProperties(WebSecurityProperties.class)
public class ServletWebSecurityAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = SecurityConstants.WEB_SECURITY_CSP_PROPERTY_NAME_PREFIX, name = SecurityConstants.ENABLED_PROPERTY_NAME)
    public ContentSecurityPolicyProcessor contentSecurityPolicyHeaderProcessor(WebSecurityProperties webSecurityProperties) {
        WebSecurityProperties.Csp csp = webSecurityProperties.getCsp();
        ContentSecurityPolicyProcessor headerProcessor = new ContentSecurityPolicyProcessor();
        headerProcessor.setPolicyDirectives(csp.getPolicyDirectives());
        headerProcessor.setReportOnly(csp.isReportOnly());
        return headerProcessor;
    }

    @Bean
    @ConditionalOnProperty(prefix = SecurityConstants.WEB_SECURITY_FILTER_PROPERTY_NAME_PREFIX, name = SecurityConstants.ENABLED_PROPERTY_NAME, matchIfMissing = true)
    public FilterRegistrationBean<Filter> servletWebSecurityFilter(@Autowired @WebSecurityFilter Collection<Filter> filters) {
        FilterRegistrationBean<Filter> filterRegistrationBean = new FilterRegistrationBean<>();
        CompositeFilter filter = new CompositeFilter();
        List<Filter> orderedFilters = new ArrayList<>(filters);
        AnnotationAwareOrderComparator.sort(orderedFilters);
        filter.setFilters(orderedFilters);
        filterRegistrationBean.setFilter(filter);
        filterRegistrationBean.setAsyncSupported(false);
        filterRegistrationBean.setDispatcherTypes(EnumSet.of(REQUEST, FORWARD, ERROR));
        filterRegistrationBean.setServletNames(asList(DEFAULT_DISPATCHER_SERVLET_BEAN_NAME));
        return filterRegistrationBean;
    }

    @Bean
    @WebSecurityFilter
    @ConditionalOnBean(WebSecurityProcessor.class)
    @ConditionalOnMissingBean
    public WebSecurityProcessorFilter webSecurityProcessorFilter(Collection<WebSecurityProcessor> processors) {
        return new WebSecurityProcessorFilter(processors);
    }

    @Bean
    @WebSecurityFilter
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = SecurityConstants.WEB_SECURITY_CORS_PROPERTY_NAME_PREFIX, name = SecurityConstants.ENABLED_PROPERTY_NAME)
    public CorsFilter corsFilter(WebSecurityProperties properties) {
        CorsConfigurationSource corsConfigurationSource = buildCorsConfigurationSource(properties);
        CorsFilter corsFilter = new CorsFilter(corsConfigurationSource);
        return corsFilter;
    }

    private CorsConfigurationSource buildCorsConfigurationSource(WebSecurityProperties properties) {
        WebSecurityProperties.Cors cors = properties.getCors();
        UrlBasedCorsConfigurationSource corsConfigurationSource = new UrlBasedCorsConfigurationSource();
        Map<String, WebSecurityProperties.Cors.Config> configs = cors.getConfigs();
        Map<String, CorsConfiguration> corsConfigurations = new HashMap<>(configs.size());
        for (Map.Entry<String, WebSecurityProperties.Cors.Config> entry : configs.entrySet()) {
            String urlPattern = entry.getKey();
            CorsConfiguration corsConfiguration = buildCorsConfiguration(entry.getValue());
            corsConfigurations.put(urlPattern, corsConfiguration);
        }
        corsConfigurationSource.setCorsConfigurations(corsConfigurations);
        return corsConfigurationSource;
    }

    private CorsConfiguration buildCorsConfiguration(WebSecurityProperties.Cors.Config config) {
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        BeanUtils.copyProperties(config, corsConfiguration);
        return corsConfiguration;
    }

    static class TomcatConfiguration {

        @Bean
        @WebSecurityFilter
        @ConditionalOnMissingBean
        public HttpHeaderSecurityFilter httpHeaderSecurityFilter(WebSecurityProperties properties) {
            HttpHeaderSecurityFilter filter = new HttpHeaderSecurityFilter();
            initHsts(filter, properties.getHsts());
            initXssProtection(filter, properties.getXss());
            initXFrameOptions(filter, properties.getXFrame());
            initXContentTypeOptions(filter, properties.getXContentType());
            return filter;
        }

        private void initHsts(HttpHeaderSecurityFilter filter, WebSecurityProperties.Hsts hsts) {
            filter.setHstsEnabled(hsts.isEnabled());
            filter.setHstsMaxAgeSeconds(hsts.getMaxAgeInSeconds());
            filter.setHstsIncludeSubDomains(hsts.isIncludeSubDomains());
            filter.setHstsPreload(hsts.isPreload());
        }

        private void initXssProtection(HttpHeaderSecurityFilter filter, WebSecurityProperties.Xss xss) {
            filter.setXssProtectionEnabled(xss.isEnabled());
        }

        private void initXFrameOptions(HttpHeaderSecurityFilter filter, WebSecurityProperties.XFrame xFrame) {
            filter.setAntiClickJackingEnabled(xFrame.isEnabled());
            filter.setAntiClickJackingOption(xFrame.getOption());
            String uri = xFrame.getUri();
            if (hasText(uri)) {
                filter.setAntiClickJackingUri(xFrame.getUri());
            }
        }

        private void initXContentTypeOptions(HttpHeaderSecurityFilter filter, WebSecurityProperties.XContentType xContentType) {
            filter.setBlockContentTypeSniffingEnabled(xContentType.isEnabled());
        }
    }


}
