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

import io.microsphere.annotation.Nonnull;
import org.springframework.beans.BeanUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.beans.PropertyDescriptor;
import java.util.List;
import java.util.Objects;

import static io.microsphere.collection.Lists.ofList;
import static io.microsphere.spring.boot.context.properties.util.ConfigurationPropertiesUtils.CONFIGURATION_PROPERTIES_CLASS;
import static io.microsphere.util.ArrayUtils.arrayToString;
import static io.microsphere.util.Assert.assertNotNull;
import static java.util.Objects.hash;
import static org.springframework.util.StringUtils.hasText;

/**
 * The information class for introspecting the bean annotated {@link ConfigurationProperties @ConfigurationProperties}.
 *
 * <h3>Example Usage</h3>
 * <pre>{@code
 * // Create instance with bean class only
 * ConfigurationPropertiesBeanInfo info = new ConfigurationPropertiesBeanInfo(MyConfiguration.class);
 *
 * // Create instance with bean class and annotation
 * ConfigurationProperties annotation = MyConfiguration.class.getAnnotation(ConfigurationProperties.class);
 * ConfigurationPropertiesBeanInfo info = new ConfigurationPropertiesBeanInfo(MyConfiguration.class, annotation);
 *
 * // Create instance with all parameters
 * ConfigurationPropertiesBeanInfo info = new ConfigurationPropertiesBeanInfo(
 *     MyConfiguration.class,
 *     annotation,
 *     "my.config.prefix"
 * );
 *
 * // Access properties
 * Class<?> beanClass = info.getBeanClass();
 * String prefix = info.getPrefix();
 * List<PropertyDescriptor> descriptors = info.getPropertyDescriptors();
 * PropertyDescriptor descriptor = info.getPropertyDescriptor("propertyName");
 * }</pre>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see ConfigurationProperties
 * @since 1.0.0
 */
public class ConfigurationPropertiesBeanInfo {

    @Nonnull
    private final Class<?> beanClass;

    @Nonnull
    private final ConfigurationProperties annotation;

    @Nonnull
    private final String prefix;

    @Nonnull
    private final PropertyDescriptor[] propertyDescriptors;

    public ConfigurationPropertiesBeanInfo(Class<?> beanClass) {
        this(beanClass, beanClass.getAnnotation(CONFIGURATION_PROPERTIES_CLASS));
    }

    public ConfigurationPropertiesBeanInfo(Class<?> beanClass, ConfigurationProperties annotation) {
        this(beanClass, annotation, hasText(annotation.prefix()) ? annotation.prefix() : annotation.value());
    }

    /**
     * @param beanClass  the bean class
     * @param annotation the annotation
     * @param prefix     the prefix of property name
     * @throws IllegalArgumentException if any argument is <code>null</code>
     */
    public ConfigurationPropertiesBeanInfo(Class<?> beanClass, ConfigurationProperties annotation, String prefix) throws IllegalArgumentException {
        assertNotNull(beanClass, () -> "The 'beanClass' must not be null !");
        assertNotNull(annotation, () -> "The 'annotation' must not be null !");
        assertNotNull(prefix, () -> "The 'prefix' must not be null !");
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
        return ofList(propertyDescriptors);
    }

    public PropertyDescriptor getPropertyDescriptor(String name) {
        return BeanUtils.getPropertyDescriptor(beanClass, name);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ConfigurationPropertiesBeanInfo)) {
            return false;
        }

        ConfigurationPropertiesBeanInfo that = (ConfigurationPropertiesBeanInfo) o;

        if (!Objects.equals(beanClass, that.beanClass)) {
            return false;
        }
        return Objects.equals(prefix, that.prefix);
    }

    @Override
    public int hashCode() {
        return hash(beanClass, prefix);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ConfigurationPropertiesBeanInfo{");
        sb.append("beanClass=").append(beanClass);
        sb.append(", annotation=").append(annotation);
        sb.append(", prefix='").append(prefix).append('\'');
        sb.append(", propertyDescriptors=").append(arrayToString(propertyDescriptors));
        sb.append('}');
        return sb.toString();
    }
}