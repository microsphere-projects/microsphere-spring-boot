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
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.source.ConfigurationProperty;
import org.springframework.context.support.GenericApplicationContext;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.springframework.boot.context.properties.bind.Bindable.ofInstance;
import static org.springframework.boot.context.properties.source.ConfigurationPropertyName.of;
import static org.springframework.core.ResolvableType.forRawClass;
import static org.springframework.core.ResolvableType.forType;

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
        this.beanContext = new ConfigurationPropertiesBeanContext(forRawClass(ServerProperties.class), beanInfo.getAnnotation(),
                beanInfo.getPrefix(), context);
    }

    @Test
    void testGetter() {
        assertEquals(ServerProperties.class, this.beanContext.getBeanClass());
        assertEquals(ServerProperties.class.getAnnotation(ConfigurationProperties.class), this.beanContext.getAnnotation());
        assertEquals("server", this.beanContext.getPrefix());
        assertNull(this.beanContext.getInitializedBean());
    }

    @Test
    void testInitialize() {
        ServerProperties serverProperties = new ServerProperties();
        this.beanContext.initialize(serverProperties);
        assertNull(serverProperties.getPort());

        ServerProperties bean = (ServerProperties) this.beanContext.getInitializedBean();
        assertNull(bean.getPort());

        assertSame(serverProperties, bean);
    }

    @Test
    void testSetProperty() {
        // String type field
        String propertyName = "test.name";
        Object propertyValue = "Mercy";
        ConfigurationProperty property = newConfigurationProperty(propertyName, propertyValue);

        ServerProperties serverProperties = new ServerProperties();
        this.beanContext.initialize(serverProperties);

        this.beanContext.setProperty(property, propertyValue);

        Bindable<String> bindable = ofInstance("Mercy");
        this.beanContext.initProperty(property.getName(), bindable);
        this.beanContext.setProperty(property, propertyValue);
        this.beanContext.setProperty(property, propertyValue);
    }

//    @Test
//    void testTestConfigurationProperties() {
//        ConfigurationPropertiesBeanInfo beanInfo = new ConfigurationPropertiesBeanInfo(TestConfigurationProperties.class);
//        GenericApplicationContext context = new GenericApplicationContext();
//        ValueHolder<ConfigurationPropertiesBeanPropertyChangedEvent> eventHolder = new ValueHolder<>();
//        context.addApplicationListener((ApplicationListener<ConfigurationPropertiesBeanPropertyChangedEvent>) event -> {
//            eventHolder.setValue(event);
//        });
//        context.refresh();
//        ConfigurationPropertiesBeanContext beanContext = new ConfigurationPropertiesBeanContext(TestConfigurationProperties.class, beanInfo.getAnnotation(), beanInfo.getPrefix(), context);
//        beanContext.initialize(new TestConfigurationProperties());
//
//        // String type field
//        String propertyName = "test.name";
//        Object propertyValue = "Mercy";
//        ConfigurationProperty property = newConfigurationProperty(propertyName, propertyValue);
//        beanContext.setProperty(property, property.getValue());
//
//        assertEvent(eventHolder, property, beanContext);
//
//        // Map type field
//        propertyName = "test.properties.status";
//        propertyValue = "OK";
//        property = newConfigurationProperty(propertyName, propertyValue);
//        beanContext.setProperty(property, property.getValue());
//    }
//
//    void assertEvent(ValueHolder<ConfigurationPropertiesBeanPropertyChangedEvent> eventHolder,
//                     ConfigurationProperty property, ConfigurationPropertiesBeanContext beanContext) {
//        String propertyName = property.getName().toString();
//        Object propertyValue = property.getValue();
//        ConfigurationPropertiesBeanPropertyChangedEvent event = eventHolder.getValue();
//        assertNotNull(event);
//        assertTrue(propertyName.contains(event.getPropertyName()));
//        assertNull(event.getOldValue());
//        assertEquals(propertyValue, event.getNewValue());
//        assertEquals(property, event.getConfigurationProperty());
//    }

    ConfigurationProperty newConfigurationProperty(String propertyName, Object propertyValue) {
        return new ConfigurationProperty(of(propertyName), propertyValue, null);
    }
}
