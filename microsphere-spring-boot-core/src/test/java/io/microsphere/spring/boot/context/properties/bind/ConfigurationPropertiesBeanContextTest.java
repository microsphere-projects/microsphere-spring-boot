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

package io.microsphere.spring.boot.context.properties.bind;


import io.microsphere.spring.boot.context.properties.ConfigurationPropertiesBeanInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.context.properties.source.ConfigurationProperty;
import org.springframework.context.support.GenericApplicationContext;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;

import static io.microsphere.spring.boot.context.properties.bind.ConfigurationPropertiesBeanContext.isCandidateClass;
import static io.microsphere.spring.boot.context.properties.bind.ConfigurationPropertiesBeanContext.isCandidateProperty;
import static java.lang.Integer.valueOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.boot.context.properties.source.ConfigurationPropertyName.of;

/**
 * {@link ConfigurationPropertiesBeanContext} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see ConfigurationPropertiesBeanContext
 * @since 1.0.0
 */
class ConfigurationPropertiesBeanContextTest {

    private ConfigurationPropertiesBeanContext beanContext;

    @BeforeEach
    void setUp() {
        ConfigurationPropertiesBeanInfo beanInfo = new ConfigurationPropertiesBeanInfo(ServerProperties.class);
        GenericApplicationContext context = new GenericApplicationContext();
        context.refresh();
        this.beanContext = new ConfigurationPropertiesBeanContext(beanInfo.getBeanClass(), beanInfo.getAnnotation(),
                beanInfo.getPrefix(), context);
    }

    @Test
    void testInitialize() {
        ServerProperties serverProperties = new ServerProperties();
        this.beanContext.initialize(serverProperties);
        assertNull(serverProperties.getPort());

        ServerProperties bean = (ServerProperties) this.beanContext.getInitializedBean();
        assertNull(bean.getPort());

        // setProperty
        String propertyName = "server.port";
        String propertyValue = "8080";
        ConfigurationProperty configurationProperty = new ConfigurationProperty(of(propertyName), propertyValue, null);
        this.beanContext.setProperty(configurationProperty, "9090");
        // setProperty again
        this.beanContext.setProperty(configurationProperty, "9090");

        assertEquals(valueOf(9090), this.beanContext.getPropertyValue("port"));

        bean = (ServerProperties) this.beanContext.getInitializedBean();
        assertEquals(valueOf(9090), bean.getPort());

    }

    @Test
    void testGetPrefix() {
        assertEquals("server", this.beanContext.getPrefix());
    }

    @Test
    void testGetBeanClass() {
        assertEquals(ServerProperties.class, this.beanContext.getBeanClass());
    }

    @Test
    void testConvertForProperty() {
        ServerProperties serverProperties = new ServerProperties();
        this.beanContext.initialize(serverProperties);
        assertSame(serverProperties, this.beanContext.convertForProperty("port", serverProperties));
    }

    @Test
    void testIsCandidateProperty() throws Exception {
        Method readMethod = null;
        Method writeMethod = BeanNameAware.class.getMethod("setBeanName", String.class);
        PropertyDescriptor propertyDescriptor = new PropertyDescriptor("beanName", readMethod, writeMethod);
        assertTrue(isCandidateProperty(propertyDescriptor));
    }

    @Test
    void testIsCandidateClass() {
        assertTrue(isCandidateClass(ServerProperties.class));
        assertFalse(isCandidateClass(String.class));
    }
}