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
package io.microsphere.spring.boot.context.properties.source.util;

import io.microsphere.spring.boot.context.properties.bind.BindListener;
import io.microsphere.spring.boot.context.properties.bind.ConfigurationPropertiesBeanProperty;
import io.microsphere.spring.test.junit.jupiter.SpringLoggingTest;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.context.properties.bind.BindContext;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.source.ConfigurationPropertyName;

import java.util.Map;

import static io.microsphere.collection.Maps.ofMap;
import static io.microsphere.reflect.FieldUtils.findField;
import static io.microsphere.reflect.MethodUtils.findMethod;
import static io.microsphere.spring.boot.context.properties.bind.util.BindUtils.bind;
import static io.microsphere.spring.boot.context.properties.source.util.ConfigurationPropertyUtils.getParent;
import static io.microsphere.spring.boot.context.properties.source.util.ConfigurationPropertyUtils.getPrefix;
import static io.microsphere.spring.boot.context.properties.source.util.ConfigurationPropertyUtils.getSource;
import static io.microsphere.spring.boot.context.properties.source.util.ConfigurationPropertyUtils.newConfigurationPropertiesBeanProperty;
import static io.microsphere.spring.boot.context.properties.source.util.ConfigurationPropertyUtils.toDashedForm;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.boot.context.properties.bind.Bindable.ofInstance;
import static org.springframework.boot.context.properties.source.ConfigurationPropertyName.EMPTY;
import static org.springframework.boot.context.properties.source.ConfigurationPropertyName.of;

/**
 * {@link ConfigurationPropertyUtils} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
@SpringLoggingTest
class ConfigurationPropertyUtilsTest {

    @Test
    void testGetPrefix() {
        String prefix = "server";
        Map<String, String> properties = ofMap(prefix + ".port", "12345");
        ServerProperties serverProperties = bind(properties, prefix, ServerProperties.class, new BindListener() {
            @Override
            public void onSuccess(ConfigurationPropertyName name, Bindable<?> target, BindContext context, Object result) {
                assertEquals(prefix, getPrefix(name, context));
                assertEquals(prefix, getSource(name));
            }
        });
        assertEquals(12345, serverProperties.getPort());

        BindContext context = mock(BindContext.class);
        when(context.getDepth()).thenReturn(1);

        ConfigurationPropertyName name = of(prefix);
        assertEquals(getSource(name), getPrefix(name, context));
    }

    @Test
    void testToDashedForm() {
        assertEquals("my-name", toDashedForm("my-name"));
        assertEquals("my-name", toDashedForm("myName"));
        assertEquals("my-name", toDashedForm("MyName"));
        assertEquals("my-name", toDashedForm("my_name"));
    }

    @Test
    void testGetParent() {
        assertSame(EMPTY, getParent(EMPTY));
        assertEquals(of("test"), getParent(of("test.name")));
        assertEquals(of("test.name"), getParent(of("test.name.1")));
    }

    @Test
    void testNewConfigurationPropertiesBeanProperty() {
        String prefix = "server";
        Map<String, String> properties = ofMap(prefix + ".port", "12345");
        ServerProperties serverProperties = bind(properties, prefix, ServerProperties.class, new BindListener() {
            @Override
            public void onSuccess(ConfigurationPropertyName name, Bindable<?> target, BindContext context, Object result) {
                ConfigurationPropertiesBeanProperty property = newConfigurationPropertiesBeanProperty(target);
                if (prefix.equals(name.toString())) {
                    assertNull(property);
                } else {
                    assertEquals(ServerProperties.class, property.getDeclaringClassType().resolve());
                    assertEquals("port", property.getName());
                    assertEquals(findMethod(ServerProperties.class, "getPort"), property.getGetter());
                    assertEquals(findMethod(ServerProperties.class, "setPort", Integer.class), property.getSetter());
                    assertEquals(findField(ServerProperties.class, "port"), property.getField());
                    assertNull(property.getValue());
                    assertEquals(Integer.class, property.getType().resolve());
                }
            }
        });
        assertNotNull(serverProperties);
    }

    @Test
    void testNewConfigurationPropertiesBeanPropertyOnNull() {
        Bindable<String> bindable = ofInstance("test");
        assertNull(newConfigurationPropertiesBeanProperty(bindable));
    }
}
