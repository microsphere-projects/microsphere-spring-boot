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

    /**
     * Constructs a {@link ConfigurationPropertiesBeanInfo} by introspecting the given bean class
     * for its {@link ConfigurationProperties @ConfigurationProperties} annotation.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   @ConfigurationProperties(prefix = "app")
     *   public class AppProperties { }
     *
     *   ConfigurationPropertiesBeanInfo info = new ConfigurationPropertiesBeanInfo(AppProperties.class);
     * }</pre>
     *
     * @param beanClass the bean class annotated with {@link ConfigurationProperties}
     */
    public ConfigurationPropertiesBeanInfo(Class<?> beanClass) {
        this(beanClass, beanClass.getAnnotation(CONFIGURATION_PROPERTIES_CLASS));
    }

    /**
     * Constructs a {@link ConfigurationPropertiesBeanInfo} with the given bean class and annotation,
     * deriving the prefix from the annotation's {@code prefix} or {@code value} attribute.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   ConfigurationProperties annotation = AppProperties.class.getAnnotation(ConfigurationProperties.class);
     *   ConfigurationPropertiesBeanInfo info =
     *       new ConfigurationPropertiesBeanInfo(AppProperties.class, annotation);
     * }</pre>
     *
     * @param beanClass  the bean class
     * @param annotation the {@link ConfigurationProperties} annotation
     */
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

    /**
     * Returns the bean class associated with this info.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   ConfigurationPropertiesBeanInfo info = new ConfigurationPropertiesBeanInfo(AppProperties.class);
     *   Class<?> beanClass = info.getBeanClass(); // AppProperties.class
     * }</pre>
     *
     * @return the bean class, never {@code null}
     */
    public Class<?> getBeanClass() {
        return beanClass;
    }

    /**
     * Returns the {@link ConfigurationProperties} annotation of the bean.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   ConfigurationPropertiesBeanInfo info = new ConfigurationPropertiesBeanInfo(AppProperties.class);
     *   ConfigurationProperties annotation = info.getAnnotation();
     *   String prefix = annotation.prefix();
     * }</pre>
     *
     * @return the {@link ConfigurationProperties} annotation, never {@code null}
     */
    public ConfigurationProperties getAnnotation() {
        return annotation;
    }

    /**
     * Returns the property name prefix for this configuration properties bean.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   ConfigurationPropertiesBeanInfo info = new ConfigurationPropertiesBeanInfo(AppProperties.class);
     *   String prefix = info.getPrefix(); // e.g. "app"
     * }</pre>
     *
     * @return the prefix, never {@code null}
     */
    public String getPrefix() {
        return prefix;
    }

    /**
     * Returns all {@link PropertyDescriptor}s of the bean class.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   ConfigurationPropertiesBeanInfo info = new ConfigurationPropertiesBeanInfo(AppProperties.class);
     *   List<PropertyDescriptor> descriptors = info.getPropertyDescriptors();
     *   descriptors.forEach(d -> System.out.println(d.getName()));
     * }</pre>
     *
     * @return an unmodifiable list of property descriptors
     */
    public List<PropertyDescriptor> getPropertyDescriptors() {
        return ofList(propertyDescriptors);
    }

    /**
     * Returns the {@link PropertyDescriptor} for the specified property name.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   ConfigurationPropertiesBeanInfo info = new ConfigurationPropertiesBeanInfo(AppProperties.class);
     *   PropertyDescriptor descriptor = info.getPropertyDescriptor("name");
     * }</pre>
     *
     * @param name the property name
     * @return the {@link PropertyDescriptor}, or {@code null} if not found
     */
    public PropertyDescriptor getPropertyDescriptor(String name) {
        return BeanUtils.getPropertyDescriptor(beanClass, name);
    }

    /**
     * Compares this instance with the specified object for equality based on bean class and prefix.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   ConfigurationPropertiesBeanInfo info1 = new ConfigurationPropertiesBeanInfo(AppProperties.class);
     *   ConfigurationPropertiesBeanInfo info2 = new ConfigurationPropertiesBeanInfo(AppProperties.class);
     *   boolean equal = info1.equals(info2); // true
     * }</pre>
     *
     * @param o the object to compare with
     * @return {@code true} if equal, {@code false} otherwise
     */
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

    /**
     * Returns a hash code based on the bean class and prefix.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   ConfigurationPropertiesBeanInfo info = new ConfigurationPropertiesBeanInfo(AppProperties.class);
     *   int hash = info.hashCode();
     * }</pre>
     *
     * @return the hash code value
     */
    @Override
    public int hashCode() {
        return hash(beanClass, prefix);
    }

    /**
     * Returns a string representation including bean class, annotation, prefix, and property descriptors.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   ConfigurationPropertiesBeanInfo info = new ConfigurationPropertiesBeanInfo(AppProperties.class);
     *   System.out.println(info.toString());
     * }</pre>
     *
     * @return a string representation of this object
     */
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