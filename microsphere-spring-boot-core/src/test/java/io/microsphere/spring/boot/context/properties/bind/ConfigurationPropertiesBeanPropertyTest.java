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


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static io.microsphere.reflect.FieldUtils.findField;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.springframework.beans.BeanUtils.getPropertyDescriptor;
import static org.springframework.core.ResolvableType.NONE;
import static org.springframework.core.ResolvableType.forClass;

/**
 * {@link ConfigurationPropertiesBeanProperty} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see ConfigurationPropertiesBeanProperty
 * @since 1.0.0
 */
class ConfigurationPropertiesBeanPropertyTest {

    private ConfigurationPropertiesBeanProperty property;

    private String propertyName;

    private PropertyDescriptor propertyDescriptor;

    @BeforeEach
    void setUp() {
        this.property = new ConfigurationPropertiesBeanProperty();
        this.propertyName = "name";
        this.propertyDescriptor = getPropertyDescriptor(ConfigurationPropertiesBeanProperty.class, propertyName);
    }

    @Test
    void testGetters() {
        assertNull(this.property.getName());
        assertNull(this.property.getDeclaringClassType());
        assertNull(this.property.getGetter());
        assertNull(this.property.getSetter());
        assertNull(this.property.getField());
        assertNull(this.property.getValue());
        assertSame(NONE, this.property.getType());
    }

    @Test
    void testName() {
        this.property.setName(propertyName);
        assertSame(propertyName, this.property.getName());
    }

    @Test
    void testDeclaringClassType() {
        this.property.setDeclaringClassType(forClass(ConfigurationPropertiesBeanProperty.class));
        assertEquals(ConfigurationPropertiesBeanProperty.class, this.property.getDeclaringClassType().resolve());
    }

    @Test
    void testGetter() {
        Method readMethod = propertyDescriptor.getReadMethod();
        this.property.setGetter(readMethod);
        assertEquals(propertyName, this.property.getName());
        assertSame(readMethod, this.property.getGetter());
        assertEquals(String.class, this.property.getType().resolve());
    }

    @Test
    void testSetter() {
        Method writeMethod = propertyDescriptor.getWriteMethod();
        this.property.setSetter(writeMethod);
        assertEquals(propertyName, this.property.getName());
        assertSame(writeMethod, this.property.getSetter());
        assertEquals(String.class, this.property.getType().resolve());
    }

    @Test
    void testField() {
        Field field = findField(ConfigurationPropertiesBeanProperty.class, propertyName);
        this.property.setField(field);
        assertEquals(propertyName, this.property.getName());
        assertSame(field, this.property.getField());
        assertEquals(String.class, this.property.getType().resolve());
    }

    @Test
    void testValue() {
        this.property.setValue("value");
        assertEquals("value", this.property.getValue());
    }

    @Test
    void testToString() {
        assertNotNull(this.property.toString());
    }
}