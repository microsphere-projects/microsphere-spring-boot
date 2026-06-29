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

import io.microsphere.annotation.Nonnull;
import io.microsphere.annotation.Nullable;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.MethodParameter;
import org.springframework.core.ResolvableType;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static io.microsphere.text.FormatUtils.format;
import static java.beans.Introspector.decapitalize;
import static org.springframework.core.ResolvableType.NONE;
import static org.springframework.core.ResolvableType.forField;
import static org.springframework.core.ResolvableType.forMethodParameter;

/**
 * The Property of {@link ConfigurationProperties @ConfigurationProperties} Bean
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see ConfigurationProperties
 * @see org.springframework.boot.context.properties.ConfigurationPropertiesBean
 * @see org.springframework.boot.context.properties.bind.JavaBeanBinder
 * @see org.springframework.boot.context.properties.bind.JavaBeanBinder.BeanProperty
 * @since 1.0.0
 */
public class ConfigurationPropertiesBeanProperty {

    private String name;

    private ResolvableType declaringClassType;

    @Nullable
    private Method getter;

    @Nullable
    private Method setter;

    @Nullable
    private Field field;

    @Nullable
    private Object value;

    public void setName(String name) {
        this.name = name;
    }

    public void setDeclaringClassType(ResolvableType declaringClassType) {
        this.declaringClassType = declaringClassType;
    }

    public void setGetter(@Nullable Method getter) {
        this.getter = getter;
    }

    public void setSetter(@Nullable Method setter) {
        this.setter = setter;
    }

    public void setField(@Nullable Field field) {
        this.field = field;
    }

    public void setValue(@Nullable Object value) {
        this.value = value;
    }

    @Nonnull
    public ResolvableType getDeclaringClassType() {
        return declaringClassType;
    }

    @Nullable
    public Method getGetter() {
        return getter;
    }

    @Nullable
    public Method getSetter() {
        return setter;
    }

    @Nullable
    public Field getField() {
        return field;
    }

    @Nullable
    public Object getValue() {
        return value;
    }

    public String getName() {
        if (this.name == null) {
            Field field = this.field;
            if (field != null) {
                return field.getName();
            }
            Method setterOrGetter = this.setter;
            if (setterOrGetter == null) {
                setterOrGetter = this.getter;
            }
            if (setterOrGetter != null) {
                String methodName = setterOrGetter.getName();
                return decapitalize(methodName.substring(3));
            }
        }
        return this.name;
    }

    public ResolvableType getType() {
        if (this.setter != null) {
            MethodParameter methodParameter = new MethodParameter(this.setter, 0);
            return forMethodParameter(methodParameter, this.declaringClassType);
        }
        if (this.getter != null) {
            MethodParameter methodParameter = new MethodParameter(this.getter, -1);
            return forMethodParameter(methodParameter, this.declaringClassType);
        }
        if (this.field != null) {
            return forField(this.field, this.declaringClassType);
        }
        return NONE;
    }

    @Override
    public String toString() {
        return format("{} {}.{} = {}", getType(), getDeclaringClassType(), getName(), getValue());
    }
}
