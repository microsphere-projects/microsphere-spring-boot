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


import io.microsphere.spring.boot.webmvc.autoconfigure.WebMvcAutoConfiguration.LoggingConfiguration;
import io.microsphere.spring.test.webmvc.AbstractWebMvcTest;
import io.microsphere.spring.web.servlet.filter.ContentCachingFilter;
import io.microsphere.spring.webmvc.annotation.WebMvcExtensionConfiguration;
import io.microsphere.spring.webmvc.config.ConfigurableContentNegotiationManagerWebMvcConfigurer;
import io.microsphere.spring.webmvc.context.ExclusiveViewResolverApplicationListener;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.accept.ContentNegotiationManager;
import org.springframework.web.accept.ContentNegotiationStrategy;
import org.springframework.web.accept.ParameterContentNegotiationStrategy;
import org.springframework.web.accept.PathExtensionContentNegotiationStrategy;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link WebMvcAutoConfiguration} Test with all enabled components
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see WebMvcAutoConfiguration
 * @since 1.0.0
 */
@SpringBootTest(classes = {
        WebMvcAutoConfigurationAllEnabledTest.class
})
@TestPropertySource(
        properties = {
                "microsphere.spring.webmvc.view-resolver.exclusive-bean-name=mvcViewResolver",
                "microsphere.spring.webmvc.content-negotiation.enabled=true",
                "microsphere.spring.webmvc.content-negotiation.favorParameter=true",
                "microsphere.spring.webmvc.content-negotiation.parameterName=p",
                "microsphere.spring.webmvc.content-negotiation.favorPathExtension=true",
                "microsphere.spring.webmvc.content-negotiation.ignoreUnknownPathExtensions=false",
                "microsphere.spring.webmvc.content-negotiation.useRegisteredExtensionsOnly=true",
                "microsphere.spring.webmvc.content-negotiation.ignoreAcceptHeader=true",
                "microsphere.spring.webmvc.filter.enabled=true",
                "microsphere.spring.webmvc.logging.enabled=true"
        }
)
@EnableAutoConfiguration
class WebMvcAutoConfigurationAllEnabledTest extends AbstractWebMvcTest {

    @Autowired
    private WebMvcExtensionConfiguration webMvcExtensionConfiguration;

    @Autowired
    private ContentCachingFilter contentCachingFilter;

    @Autowired
    private ConfigurableContentNegotiationManagerWebMvcConfigurer webMvcConfigurer;

    @Autowired
    private ContentNegotiationManager contentNegotiationManager;

    @Autowired
    private LoggingConfiguration loggingConfiguration;

    @Autowired
    private ExclusiveViewResolverApplicationListener listener;

    @Override
    @BeforeEach
    public void setUp() {
        super.setUp();
    }

    @Test
    void test() throws Exception {

        super.testHelloWorld();
        super.testGreeting();
        super.testUser();
        super.testResponseEntity();

        assertContentNegotiationManager(this.contentNegotiationManager);
    }

    void assertContentNegotiationManager(ContentNegotiationManager contentNegotiationManager) {
        List<ContentNegotiationStrategy> strategies = contentNegotiationManager.getStrategies();
        assertEquals(2, strategies.size());

        ContentNegotiationStrategy strategy1 = strategies.get(0);
        ContentNegotiationStrategy strategy2 = strategies.get(1);

        assertTrue(strategy1 instanceof PathExtensionContentNegotiationStrategy);
        assertTrue(strategy2 instanceof ParameterContentNegotiationStrategy);

        PathExtensionContentNegotiationStrategy pathExtensionContentNegotiationStrategy = (PathExtensionContentNegotiationStrategy) strategy1;
        Map<String, MediaType> mediaTypes = pathExtensionContentNegotiationStrategy.getMediaTypes();
        assertEquals(2, mediaTypes.size());

        ParameterContentNegotiationStrategy parameterContentNegotiationStrategy = (ParameterContentNegotiationStrategy) strategy2;

        assertEquals("p", parameterContentNegotiationStrategy.getParameterName());
        assertEquals(false, pathExtensionContentNegotiationStrategy.isIgnoreUnknownExtensions());
        assertEquals(true, pathExtensionContentNegotiationStrategy.isUseRegisteredExtensionsOnly());
    }
}