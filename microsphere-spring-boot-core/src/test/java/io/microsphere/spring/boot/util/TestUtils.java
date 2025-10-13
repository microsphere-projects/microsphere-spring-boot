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

package io.microsphere.spring.boot.util;

import io.microsphere.annotation.Nullable;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.core.env.Environment;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import static io.microsphere.util.ArrayUtils.combine;
import static java.lang.String.valueOf;
import static java.lang.Thread.currentThread;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.boot.WebApplicationType.NONE;

/**
 * The utilities class for testing
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see SpringApplication
 * @see ServerProperties
 * @since 1.0.0
 */
public abstract class TestUtils {

    public static SpringApplication application(Class<?>... primarySources) {
        return application(currentThread().getContextClassLoader(), primarySources);
    }

    public static SpringApplication application(@Nullable ClassLoader classLoader, Class<?>... primarySources) {
        SpringApplication springApplication = new SpringApplication(combine(Object.class, primarySources));
        springApplication.setWebApplicationType(NONE);
        if (classLoader != null) {
            ResourcePatternResolver resourceLoader = new PathMatchingResourcePatternResolver(classLoader);
            springApplication.setResourceLoader(resourceLoader);
        }
        return springApplication;
    }

    public static void assertServerPropertiesPort(Environment environment, ServerProperties serverProperties) {
        assertEquals(environment.getProperty("server.port"), valueOf(serverProperties.getPort()));
    }
}