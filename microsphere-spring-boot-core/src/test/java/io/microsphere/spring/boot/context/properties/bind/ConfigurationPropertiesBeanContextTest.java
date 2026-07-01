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
import org.springframework.core.ResolvableType;
import org.springframework.core.annotation.AnnotationAttributes;

import java.beans.PropertyDescriptor;
import java.util.concurrent.TimeUnit;

import static io.microsphere.spring.boot.context.properties.bind.ConfigurationPropertiesBeanContext.getInstance;
import static io.microsphere.spring.boot.context.properties.bind.ConfigurationPropertiesBeanContext.isCandidateClass;
import static io.microsphere.spring.boot.context.properties.bind.ConfigurationPropertiesBeanContext.isCandidateProperty;
import static io.microsphere.spring.core.annotation.AnnotationUtils.getAnnotationAttributes;
import static org.junit.jupiter.api.Assertions.assertEquals;
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
public class ConfigurationPropertiesBeanContextTest {

    private static String beanName = "server-org.springframework.boot.autoconfigure.web.ServerProperties";

    private static ResolvableType beanType = forRawClass(ServerProperties.class);

    private static ConfigurationProperties annotation = new ConfigurationPropertiesBeanInfo(ServerProperties.class).getAnnotation();

    private static AnnotationAttributes annotationAttributes = getAnnotationAttributes(annotation);

    private GenericApplicationContext context;

    private ConfigurationPropertiesBeanContext beanContext;

    @BeforeEach
    void setUp() {
        this.context = new GenericApplicationContext();
        context.refresh();
        this.beanContext = new ConfigurationPropertiesBeanContext(beanName, beanType, annotationAttributes, context);
    }

    @Test
    void testGetter() {
        assertSame(beanName, this.beanContext.getBeanName());
        assertSame(beanType, this.beanContext.getBeanType());
        assertEquals("server", this.beanContext.getPrefix());
        assertSame(ServerProperties.class, this.beanContext.getBeanClass());
    }

    @Test
    void testInitializeBean() {
        ServerProperties serverProperties = new ServerProperties();
        this.beanContext.initializeBean(serverProperties);
        assertNull(serverProperties.getPort());

        this.beanContext.initializeBean(new JacksonProperties());
    }

    @Test
    void testSetProperty() {
        // String type field
        String propertyName = "test.name";
        Object propertyValue = "Mercy";
        ConfigurationProperty property = newConfigurationProperty(propertyName, propertyValue);

        ServerProperties serverProperties = new ServerProperties();
        this.beanContext.initializeBean(serverProperties);

        this.beanContext.setProperty(property, propertyValue);

        Bindable<String> bindable = ofInstance("Mercy");
        // this.beanContext.initProperty(property.getName(), bindable);
        this.beanContext.setProperty(property, propertyValue);
        this.beanContext.setProperty(property, propertyValue);
    }

    @Test
    void testSetAndGetPropertyValue() {
        Integer port = 8080;
        assertFalse(this.beanContext.setPropertyValue("port", port));
        ServerProperties serverProperties = new ServerProperties();
        this.beanContext.initializeBean(serverProperties);
        assertNull(this.beanContext.getPropertyValue("port"));
        assertTrue(this.beanContext.setPropertyValue("port", port));
        assertEquals(port, this.beanContext.getPropertyValue("port"));
    }

    @Test
    void testSetAndGetPropertyValueOnFailed() {
        assertNull(this.beanContext.getPropertyValue("invalid.property.name"));
        this.beanContext.initializeBean(new ServerProperties());
        assertNull(this.beanContext.getPropertyValue("invalid.property.name"));
        assertNull(this.beanContext.getPropertyValue("ssl.enabled"));
        assertNull(this.beanContext.getPropertyValue(null));
        assertFalse(this.beanContext.setPropertyValue("s", null));
        assertFalse(this.beanContext.setPropertyValue(null, null));
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
        assertFalse(isCandidateProperty(null));
        PropertyDescriptor descriptor = getPropertyDescriptor(ConfigurationPropertiesBeanContextTest.class, "name");
        assertFalse(isCandidateProperty(descriptor));

        descriptor = getPropertyDescriptor(ConfigurationPropertiesBeanContextTest.class, "not-found");
        assertFalse(isCandidateProperty(descriptor));

        descriptor = getPropertyDescriptor(ConfigurationPropertiesBeanContextTest.class, "class");
        assertFalse(isCandidateProperty(descriptor));

        descriptor = getPropertyDescriptor(ServerProperties.class, "port");
        assertTrue(isCandidateProperty(descriptor));
    }

    @Test
    void testIsCandidateClass() {
        // null
        assertFalse(isCandidateClass(null));
        // primitive type and wrapper type are not candidate class
        assertFalse(isCandidateClass(int.class));
        assertFalse(isCandidateClass(Integer.class));
        // enumeration type
        assertFalse(isCandidateClass(TimeUnit.class));
        // String is candidate class,but is under java.lang package.
        assertFalse(isCandidateClass(String.class));
        // current class
        assertTrue(isCandidateClass(getClass()));
    }

    public void setName(String name) {
        // Just for testing purpose
    }

    public static ConfigurationProperty newConfigurationProperty(String propertyName, Object propertyValue) {
        return new ConfigurationProperty(of(propertyName), propertyValue, null);
    }
}
