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


import io.microsphere.spring.web.servlet.filter.ContentCachingFilter;
import io.microsphere.spring.webmvc.annotation.WebMvcExtensionConfiguration;
import io.microsphere.spring.webmvc.config.ConfigurableContentNegotiationManagerWebMvcConfigurer;
import io.microsphere.spring.webmvc.context.ExclusiveViewResolverApplicationListener;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * {@link WebMvcAutoConfiguration} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see WebMvcAutoConfiguration
 * @since 1.0.0
 */
@SpringBootTest(classes = {
        WebMvcAutoConfigurationTest.class
})
class WebMvcAutoConfigurationTest extends AbstractWebMvcAutoConfigurationTest {

    @Autowired
    private WebMvcExtensionConfiguration webMvcExtensionConfiguration;

    @Autowired
    private ContentCachingFilter contentCachingFilter;

    @Autowired
    private ConfigurableContentNegotiationManagerWebMvcConfigurer webMvcConfigurer;

    @Autowired
    private WebMvcAutoConfiguration.LoggingConfiguration loggingConfiguration;

    @Autowired(required = false)
    private ExclusiveViewResolverApplicationListener listener;

    @Test
    void test() throws Exception {
        super.testWebEndpoints();
        assertNotNull(this.webMvcExtensionConfiguration);
        assertNotNull(this.contentCachingFilter);
        assertNotNull(this.webMvcConfigurer);
        assertNotNull(this.loggingConfiguration);
        assertNull(this.listener);
    }
}