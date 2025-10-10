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

package io.microsphere.spring.boot.context.properties;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.beans.PropertyDescriptor;
import java.util.List;

import static io.microsphere.spring.boot.context.properties.util.ConfigurationPropertiesUtils.CONFIGURATION_PROPERTIES_CLASS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.springframework.util.StringUtils.hasText;

/**
 * {@link ConfigurationPropertiesBeanInfo} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see ConfigurationPropertiesBeanInfo
 * @since 1.0.0
 */
@ConfigurationProperties(prefix = "server")
class ConfigurationPropertiesBeanInfoTest {

    private ConfigurationPropertiesBeanInfo info;

    @BeforeEach
    void setUp() {
        this.info = new ConfigurationPropertiesBeanInfo(ServerProperties.class);
    }

    @Test
    void testGetBeanClass() {
        assertSame(ServerProperties.class, this.info.getBeanClass());
    }

    @Test
    void testGetAnnotation() {
        ConfigurationProperties configurationProperties = this.info.getAnnotation();
        assertEquals("server", hasText(configurationProperties.prefix()) ?
                configurationProperties.prefix() : configurationProperties.value());
    }

    @Test
    void testGetPrefix() {
        assertEquals("server", this.info.getPrefix());
    }

    @Test
    void testGetPropertyDescriptors() {
        List<PropertyDescriptor> descriptors = this.info.getPropertyDescriptors();
        assertFalse(descriptors.isEmpty());
    }

    @Test
    void testGetPropertyDescriptor() {
        String name = "port";
        PropertyDescriptor portPropertyDescriptor = this.info.getPropertyDescriptor(name);
        assertEquals(name, portPropertyDescriptor.getName());
    }

    @Test
    void testEquals() {
        assertEquals(this.info, this.info);
        assertEquals(this.info, new ConfigurationPropertiesBeanInfo(ServerProperties.class));
        assertNotEquals(this.info, this);
        assertNotEquals(this.info, new ConfigurationPropertiesBeanInfo(ConfigurationPropertiesBeanInfoTest.class));
        assertNotEquals(this.info, new ConfigurationPropertiesBeanInfo(ServerProperties.class,
                ServerProperties.class.getAnnotation(CONFIGURATION_PROPERTIES_CLASS), "server-1"));
    }

    @Test
    void testHashCode() {
        assertEquals(this.info.hashCode(), new ConfigurationPropertiesBeanInfo(ServerProperties.class).hashCode());
    }

    @Test
    void testToString() {
        assertEquals(this.info.toString(), new ConfigurationPropertiesBeanInfo(ServerProperties.class).toString());
    }
}