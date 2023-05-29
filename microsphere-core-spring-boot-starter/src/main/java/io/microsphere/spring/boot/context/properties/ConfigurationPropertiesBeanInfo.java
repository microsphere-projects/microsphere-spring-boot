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

import org.springframework.beans.BeanUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.beans.PropertyDescriptor;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import static io.microsphere.spring.boot.context.properties.util.ConfigurationPropertiesUtils.CONFIGURATION_PROPERTIES_CLASS;

/**
 * The information class for introspecting the bean annotated {@link ConfigurationProperties @ConfigurationProperties}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see ConfigurationProperties
 * @since 1.0.0
 */
public class ConfigurationPropertiesBeanInfo {

    private final Class<?> beanClass;

    private final ConfigurationProperties annotation;

    private final String prefix;

    private final PropertyDescriptor[] propertyDescriptors;

    public ConfigurationPropertiesBeanInfo(Class<?> beanClass) {
        this(beanClass, beanClass.getAnnotation(CONFIGURATION_PROPERTIES_CLASS));
    }

    public ConfigurationPropertiesBeanInfo(Class<?> beanClass, ConfigurationProperties annotation) {
        this(beanClass, annotation, annotation.prefix());
    }

    public ConfigurationPropertiesBeanInfo(Class<?> beanClass, ConfigurationProperties annotation, String prefix) {
        this.beanClass = beanClass;
        this.annotation = annotation;
        this.prefix = prefix;
        this.propertyDescriptors = BeanUtils.getPropertyDescriptors(beanClass);
    }

    public Class<?> getBeanClass() {
        return beanClass;
    }

    public ConfigurationProperties getAnnotation() {
        return annotation;
    }

    public String getPrefix() {
        return prefix;
    }

    public List<PropertyDescriptor> getPropertyDescriptors() {
        return Arrays.asList(propertyDescriptors);
    }

    public PropertyDescriptor getPropertyDescriptor(String name) {
        return BeanUtils.getPropertyDescriptor(beanClass, name);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ConfigurationPropertiesBeanInfo)) return false;

        ConfigurationPropertiesBeanInfo that = (ConfigurationPropertiesBeanInfo) o;

        if (!Objects.equals(beanClass, that.beanClass)) return false;
        return Objects.equals(prefix, that.prefix);
    }

    @Override
    public int hashCode() {
        int result = beanClass != null ? beanClass.hashCode() : 0;
        result = 31 * result + (prefix != null ? prefix.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ConfigurationPropertiesBeanInfo{");
        sb.append("beanClass=").append(beanClass);
        sb.append(", annotation=").append(annotation);
        sb.append(", prefix='").append(prefix).append('\'');
        sb.append(", propertyDescriptors=").append(propertyDescriptors);
        sb.append('}');
        return sb.toString();
    }

}
