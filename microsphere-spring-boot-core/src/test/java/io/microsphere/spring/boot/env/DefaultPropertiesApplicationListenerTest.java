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

package io.microsphere.spring.boot.env;


import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import java.io.IOException;
import java.net.URLClassLoader;

import static io.microsphere.util.ArrayUtils.ofArray;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.boot.WebApplicationType.NONE;

/**
 * {@link DefaultPropertiesApplicationListener} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see DefaultPropertiesApplicationListener
 * @since 1.0.0
 */
class DefaultPropertiesApplicationListenerTest {

    @Test
    void testOnApplicationEvent() {
        SpringApplication springApplication = application();
        ConfigurableApplicationContext context = springApplication.run();
        ConfigurableEnvironment environment = context.getEnvironment();
        assertEquals("test", environment.getProperty("test.name"));
    }

    @Test
    void testOnApplicationEventOnIOException() {
        SpringApplication springApplication = application();
        ResourcePatternResolver delegate = (ResourcePatternResolver) springApplication.getResourceLoader();

        ResourcePatternResolver resourceLoader = new ResourcePatternResolver() {

            @Override
            public Resource getResource(String location) {
                return delegate.getResource(location);
            }

            @Override
            public ClassLoader getClassLoader() {
                return delegate.getClassLoader();
            }

            @Override
            public Resource[] getResources(String locationPattern) throws IOException {
                throw new IOException("For testing");
            }
        };

        springApplication.setResourceLoader(resourceLoader);
        ConfigurableApplicationContext context = springApplication.run();
        ConfigurableEnvironment environment = context.getEnvironment();
        assertNull(environment.getProperty("test.name"));
    }

    private SpringApplication application() {
        SpringApplication springApplication = new SpringApplication(getClass());
        springApplication.setWebApplicationType(NONE);
        springApplication.setMainApplicationClass(getClass());
        ClassLoader classLoader = new URLClassLoader(ofArray(), springApplication.getClassLoader());
        springApplication.setResourceLoader(new PathMatchingResourcePatternResolver(classLoader));
        return springApplication;
    }
}