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

package io.microsphere.spring.boot.context.properties.util;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.Bindable;

import static io.microsphere.spring.boot.context.properties.util.ConfigurationPropertiesUtils.CONFIGURATION_PROPERTIES_CLASS;
import static io.microsphere.spring.boot.context.properties.util.ConfigurationPropertiesUtils.findConfigurationProperties;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.springframework.boot.context.properties.bind.Bindable.of;

/**
 * {@link ConfigurationPropertiesUtils} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see ConfigurationPropertiesUtils
 * @since 1.0.0
 */
class ConfigurationPropertiesUtilsTest {

    private Bindable<ServerProperties> bindable;

    @BeforeEach
    void setUp() {
        this.bindable = of(ServerProperties.class);
    }

    @Test
    void testFindConfigurationProperties() {
        ConfigurationProperties configurationProperties = ServerProperties.class.getAnnotation(CONFIGURATION_PROPERTIES_CLASS);
        this.bindable = this.bindable.withAnnotations(configurationProperties);
        assertSame(configurationProperties, findConfigurationProperties(this.bindable));
    }

    @Test
    void testFindConfigurationPropertiesWithoutAnnotations() {
        ConfigurationProperties configurationProperties = ServerProperties.class.getAnnotation(CONFIGURATION_PROPERTIES_CLASS);
        assertSame(configurationProperties, findConfigurationProperties(this.bindable));
    }
}