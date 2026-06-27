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
import io.microsphere.spring.test.junit.jupiter.SpringLoggingTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.jackson.JacksonProperties;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.source.ConfigurationProperty;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.annotation.AnnotationAttributes;

import java.beans.PropertyDescriptor;

import static io.microsphere.spring.boot.context.properties.bind.ConfigurationPropertiesBeanContext.getInstance;
import static io.microsphere.spring.boot.context.properties.bind.ConfigurationPropertiesBeanContext.isCandidateProperty;
import static io.microsphere.spring.core.annotation.AnnotationUtils.getAnnotationAttributes;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.beans.BeanUtils.getPropertyDescriptor;
import static org.springframework.boot.context.properties.bind.Bindable.ofInstance;
import static org.springframework.boot.context.properties.source.ConfigurationPropertyName.of;
import static org.springframework.core.ResolvableType.forRawClass;

/**
 * {@link ConfigurationPropertiesBeanContext} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see ConfigurationPropertiesBeanContext
 * @since 1.0.0
 */
@SpringLoggingTest
class ConfigurationPropertiesBeanContextTest {

    private ConfigurationPropertiesBeanContext beanContext;

    @BeforeEach
    void setUp() {
        ConfigurationPropertiesBeanInfo beanInfo = new ConfigurationPropertiesBeanInfo(ServerProperties.class);
        GenericApplicationContext context = new GenericApplicationContext();
        context.refresh();
        ConfigurationProperties annotation = beanInfo.getAnnotation();
        AnnotationAttributes annotationAttributes = getAnnotationAttributes(annotation);
        this.beanContext = new ConfigurationPropertiesBeanContext(forRawClass(ServerProperties.class), annotationAttributes,
                beanInfo.getPrefix(), context);
    }

    @Test
    void testInitialize() {
        ServerProperties serverProperties = new ServerProperties();
        this.beanContext.initialize(serverProperties);
        assertNull(serverProperties.getPort());

        this.beanContext.initialize(new JacksonProperties());
    }

    @Test
    void testSetProperty() {
        // String type field
        String propertyName = "test.name";
        Object propertyValue = "Mercy";
        ConfigurationProperty property = newConfigurationProperty(propertyName, propertyValue);

        ServerProperties serverProperties = new ServerProperties();
        this.beanContext.initialize(serverProperties);

        this.beanContext.setProperty(property, propertyValue, false);

        Bindable<String> bindable = ofInstance("Mercy");
        this.beanContext.initProperty(property.getName(), bindable);
        this.beanContext.setProperty(property, propertyValue, true);
        this.beanContext.setProperty(property, propertyValue, true);
    }

    @Test
    void testGetPropertyValueOnFailed() {
        assertNull(this.beanContext.getPropertyValue("invalid.property.name"));
        this.beanContext.initialize(new ServerProperties());
        assertNull(this.beanContext.getPropertyValue("invalid.property.name"));
    }

    @Test
    void testGetInstance() {
        assertSame(this.beanContext, getInstance(this.beanContext, null));
        assertSame(this.beanContext, getInstance(this.beanContext, ""));
        assertSame(this.beanContext, getInstance(this.beanContext, " "));
        assertNull(getInstance(this.beanContext, "initializedBean"));
        assertNull(getInstance(this.beanContext, "beanWrapper"));
        assertNull(getInstance(this.beanContext, "beanWrapper."));
        assertSame(ServerProperties.class, getInstance(this.beanContext, "beanType.type"));
    }

    @Test
    void testIsCandidateProperty() {
        PropertyDescriptor descriptor = getPropertyDescriptor(ConfigurationPropertiesBeanContextTest.class, "name");
        assertTrue(isCandidateProperty(descriptor));

        descriptor = getPropertyDescriptor(ConfigurationPropertiesBeanContextTest.class, "class");
        assertFalse(isCandidateProperty(descriptor));
    }

    public void setName(String name) {
        // Just for testing purpose
    }

    ConfigurationProperty newConfigurationProperty(String propertyName, Object propertyValue) {
        return new ConfigurationProperty(of(propertyName), propertyValue, null);
    }
}
