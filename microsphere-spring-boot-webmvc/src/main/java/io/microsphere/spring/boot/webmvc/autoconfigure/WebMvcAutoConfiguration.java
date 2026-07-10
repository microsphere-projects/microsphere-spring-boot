/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.microsphere.spring.boot.webmvc.autoconfigure;

import io.microsphere.spring.boot.webmvc.autoconfigure.condition.ConditionalOnWebMvcAvailable;
import io.microsphere.spring.web.servlet.filter.ContentCachingFilter;
import io.microsphere.spring.webmvc.annotation.EnableWebMvcExtension;
import io.microsphere.spring.webmvc.config.ConfigurableContentNegotiationManagerWebMvcConfigurer;
import io.microsphere.spring.webmvc.context.ExclusiveViewResolverApplicationListener;
import io.microsphere.spring.webmvc.interceptor.LoggingMethodHandlerInterceptor;
import io.microsphere.spring.webmvc.interceptor.LoggingPageRenderContextHandlerInterceptor;
import io.microsphere.spring.webmvc.method.support.LoggingHandlerMethodArgumentResolverAdvice;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import static io.microsphere.spring.boot.webmvc.constants.PropertyConstants.MICROSPHERE_SPRING_BOOT_WEBMVC_FILTER_ENALBED_PROPERTY_NAME;
import static io.microsphere.spring.boot.webmvc.constants.PropertyConstants.MICROSPHERE_SPRING_BOOT_WEBMVC_LOGGING_ENALBED_PROPERTY_NAME;
import static io.microsphere.spring.boot.webmvc.constants.PropertyConstants.MICROSPHERE_SPRING_WEBMVC_CONTENT_NEGOTIATION_ENABLED_PROPERTY_NAME;
import static io.microsphere.spring.webmvc.context.ExclusiveViewResolverApplicationListener.EXCLUSIVE_VIEW_RESOLVER_BEAN_NAME_PROPERTY_NAME;

/**
 * MicroSphere Spring Boot WebMVC Auto-Configuration
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration
 * @see org.springframework.boot.webmvc.autoconfigure.WebMvcAutoConfiguration
 * @since 1.0.0
 */
@ConditionalOnWebMvcAvailable
@AutoConfigureAfter(name = {
        "org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration", // Spring Boot API [2.0, 4.0)
        "org.springframework.boot.webmvc.autoconfigure.WebMvcAutoConfiguration"       // Spring Boot API [4.0,)
})
@EnableWebMvcExtension(
        registerHandlerInterceptors = true,
        reversedProxyHandlerMapping = true
)
@Import(value = {
        WebMvcAutoConfiguration.LoggingConfiguration.class
})
public class WebMvcAutoConfiguration {

    @ConditionalOnProperty(name = MICROSPHERE_SPRING_BOOT_WEBMVC_FILTER_ENALBED_PROPERTY_NAME, matchIfMissing = true)
    @Bean
    public ContentCachingFilter contentCachingFilter() {
        return new ContentCachingFilter();
    }

    @ConditionalOnProperty(name = MICROSPHERE_SPRING_WEBMVC_CONTENT_NEGOTIATION_ENABLED_PROPERTY_NAME, matchIfMissing = true)
    @Bean
    public ConfigurableContentNegotiationManagerWebMvcConfigurer contentNegotiationManagerWebMvcConfigurer() {
        return new ConfigurableContentNegotiationManagerWebMvcConfigurer();
    }

    @ConditionalOnProperty(name = EXCLUSIVE_VIEW_RESOLVER_BEAN_NAME_PROPERTY_NAME)
    @Bean
    public ExclusiveViewResolverApplicationListener exclusiveViewResolverApplicationListener() {
        return new ExclusiveViewResolverApplicationListener();
    }

    @ConditionalOnProperty(name = MICROSPHERE_SPRING_BOOT_WEBMVC_LOGGING_ENALBED_PROPERTY_NAME, matchIfMissing = true)
    @Import(value = {
            LoggingMethodHandlerInterceptor.class,
            LoggingPageRenderContextHandlerInterceptor.class,
            LoggingHandlerMethodArgumentResolverAdvice.class
    })
    static class LoggingConfiguration {
    }
}