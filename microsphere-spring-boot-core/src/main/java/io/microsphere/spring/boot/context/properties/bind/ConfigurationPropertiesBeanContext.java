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
import io.microsphere.spring.core.convert.support.ConversionServiceResolver;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.source.ConfigurationProperty;
import org.springframework.boot.context.properties.source.ConfigurationPropertyName;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.convert.ConversionService;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static io.microsphere.spring.boot.context.properties.source.util.ConfigurationPropertyUtils.toDashedForm;
import static io.microsphere.util.Assert.assertNotBlank;
import static io.microsphere.util.Assert.assertNotNull;
import static io.microsphere.util.ClassUtils.isConcreteClass;
import static org.springframework.beans.BeanUtils.copyProperties;
import static org.springframework.beans.BeanUtils.getPropertyDescriptors;
import static org.springframework.util.ClassUtils.isPrimitiveOrWrapper;

/**
 * The context for the bean annotated {@link ConfigurationProperties @ConfigurationProperties}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
class ConfigurationPropertiesBeanContext {

    private final ConfigurationProperties annotation;

    private final String prefix;

    private final ConfigurableApplicationContext context;

    private final BeanWrapperImpl initializedBeanWrapper;

    private Map<String, String> bindingPropertyNames;

    private volatile Object bean;

    /**
     * Constructor
     *
     * @param beanClass  the bean class
     * @param annotation the annotation of {@link ConfigurationProperties}
     * @param prefix     {@link ConfigurationProperties#prefix() the prefix}
     * @param context    {@link ConfigurableApplicationContext}
     * @throws IllegalArgumentException If <code>beanClass</code> or <code>annotation</code> or <code>context</code> argument is null,
     *                                  or the <code>prefix</code> is blank
     */
    public ConfigurationPropertiesBeanContext(Class<?> beanClass, ConfigurationProperties annotation, String prefix,
                                              ConfigurableApplicationContext context) throws IllegalArgumentException {
        assertNotNull(beanClass, () -> "The 'beanClass' must not be null!");
        assertNotNull(annotation, () -> "The 'annotation' must not be null!");
        assertNotBlank(prefix, () -> "The 'prefix' must not be black!");
        assertNotNull(context, () -> "The 'assertNotNull' must not be null!");
        // TODO support @ConstructorBinding creating beans
        this.annotation = annotation;
        this.prefix = prefix;
        this.context = context;
        this.initializedBeanWrapper = createInitializedBeanWrapper(beanClass);
    }

    /**
     * Initializes the context by copying the bean's current property values into
     * the internal bean wrapper and setting up property name bindings.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   ConfigurationPropertiesBeanContext ctx = new ConfigurationPropertiesBeanContext(
     *       MyProps.class, annotation, "app", applicationContext);
     *   MyProps bean = applicationContext.getBean(MyProps.class);
     *   ctx.initialize(bean);
     * }</pre>
     *
     * @param bean the bean instance to initialize from
     */
    protected void initialize(Object bean) {
        this.bean = bean;
        setProperties(bean);
        initBinding(bean);
    }

    /**
     * Sets a property value on the initialized bean. If the new value differs from the old value,
     * a {@link ConfigurationPropertiesBeanPropertyChangedEvent} is published.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   ConfigurationPropertiesBeanContext ctx = ...;
     *   ConfigurationProperty property = context.getConfigurationProperty();
     *   ctx.setProperty(property, "newValue");
     * }</pre>
     *
     * @param property the {@link ConfigurationProperty} being set
     * @param newValue the new value to set
     */
    public void setProperty(ConfigurationProperty property, Object newValue) {
        ConfigurationPropertyName name = property.getName();
        String propertyName = getPropertyName(name);
        Object convertedNewValue = convertForProperty(propertyName, newValue);
        Object oldValue = getPropertyValue(propertyName);
        if (!Objects.deepEquals(oldValue, convertedNewValue)) {
            initializedBeanWrapper.setPropertyValue(propertyName, convertedNewValue);
            publishEvent(property, propertyName, oldValue, newValue);
        }
    }

    /**
     * Returns the property name prefix for the {@link ConfigurationProperties @ConfigurationProperties} bean.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   ConfigurationPropertiesBeanContext ctx = ...;
     *   String prefix = ctx.getPrefix(); // e.g. "app.datasource"
     * }</pre>
     *
     * @return the configuration properties prefix, never {@code null}
     */
    @Nonnull
    public String getPrefix() {
        return prefix;
    }

    /**
     * Returns the class of the {@link ConfigurationProperties @ConfigurationProperties} bean.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   ConfigurationPropertiesBeanContext ctx = ...;
     *   Class<?> beanClass = ctx.getBeanClass(); // e.g. MyProps.class
     * }</pre>
     *
     * @return the bean class, never {@code null}
     */
    @Nonnull
    public Class<?> getBeanClass() {
        return initializedBeanWrapper.getWrappedClass();
    }

    /**
     * Returns the current value of the specified property from the initialized bean.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   ConfigurationPropertiesBeanContext ctx = ...;
     *   Object value = ctx.getPropertyValue("name"); // e.g. "myApp"
     * }</pre>
     *
     * @param name the property name
     * @return the current property value, or {@code null} if not set
     */
    @Nullable
    public Object getPropertyValue(String name) {
        return initializedBeanWrapper.getPropertyValue(name);
    }

    /**
     * Returns the initialized bean instance that mirrors the original bean's property values.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   ConfigurationPropertiesBeanContext ctx = ...;
     *   Object initializedBean = ctx.getInitializedBean();
     * }</pre>
     *
     * @return the initialized bean instance, never {@code null}
     */
    @Nonnull
    public Object getInitializedBean() {
        return this.initializedBeanWrapper.getWrappedInstance();
    }

    private BeanWrapperImpl createInitializedBeanWrapper(Class<?> beanClass) {
        BeanWrapperImpl beanWrapper = new BeanWrapperImpl(beanClass);
        ConversionService conversionService = getConversionService(context);
        beanWrapper.setAutoGrowNestedPaths(true);
        beanWrapper.setConversionService(conversionService);
        return beanWrapper;
    }

    private ConversionService getConversionService(ConfigurableApplicationContext context) {
        return new ConversionServiceResolver(context.getBeanFactory()).resolve();
    }

    private void setProperties(Object bean) {
        Object initializedBean = this.initializedBeanWrapper.getWrappedInstance();
        copyProperties(bean, initializedBean);
    }

    private void initBinding(Object bean) {
        Map<String, String> bindingPropertyNames = new HashMap<>();
        String prefix = getPrefix();
        initBinding(bean.getClass(), prefix, bindingPropertyNames, null);
        this.bindingPropertyNames = bindingPropertyNames;
    }

    private void initBinding(Class<?> beanClass, String prefix, Map<String, String> bindingPropertyNames, String nestedPath) {
        if (isCandidateClass(beanClass)) {
            PropertyDescriptor[] descriptors = getPropertyDescriptors(beanClass);
            int descriptorSize = descriptors.length;
            for (int i = 0; i < descriptorSize; i++) {
                PropertyDescriptor descriptor = descriptors[i];
                if (isCandidateProperty(descriptor)) {
                    String propertyName = descriptor.getName();
                    Class<?> propertyType = descriptor.getPropertyType();
                    String configurationPropertyName = prefix + "." + toDashedForm(propertyName);
                    String propertyPath = nestedPath == null ? propertyName : nestedPath + "." + propertyName;
                    bindingPropertyNames.put(configurationPropertyName, propertyPath);
                    initBinding(propertyType, configurationPropertyName, bindingPropertyNames, propertyPath);
                }
            }
        }
    }

    /**
     * Converts the given value to the type of the specified property using the configured
     * {@link ConversionService}.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   Object converted = beanContext.convertForProperty("port", "8080");
     *   // converted is Integer 8080 if the property type is int
     * }</pre>
     *
     * @param propertyName the name of the target property
     * @param value        the value to convert
     * @return the converted value, or the original value if conversion is not supported
     */
    Object convertForProperty(String propertyName, Object value) {
        Class<?> propertyType = this.initializedBeanWrapper.getPropertyType(propertyName);
        ConversionService conversionService = this.initializedBeanWrapper.getConversionService();
        if (conversionService.canConvert(value.getClass(), propertyType)) {
            return conversionService.convert(value, propertyType);
        }
        return value;
    }

    /**
     * Determines whether the given {@link PropertyDescriptor} is a candidate for binding.
     * A property is a candidate unless its read method is declared directly on {@link Object}.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   PropertyDescriptor descriptor = BeanUtils.getPropertyDescriptor(MyConfig.class, "port");
     *   boolean candidate = ConfigurationPropertiesBeanContext.isCandidateProperty(descriptor); // true
     * }</pre>
     *
     * @param descriptor the {@link PropertyDescriptor} to evaluate
     * @return {@code true} if the property is a binding candidate, {@code false} otherwise
     */
    static boolean isCandidateProperty(PropertyDescriptor descriptor) {
        Method readMethod = descriptor.getReadMethod();
        return readMethod == null ? true : !Object.class.equals(readMethod.getDeclaringClass());
    }

    /**
     * Determines whether the given class is a candidate for configuration property binding.
     * Primitives, wrappers, classes from {@code java.*} packages, and non-concrete classes are excluded.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   boolean candidate1 = ConfigurationPropertiesBeanContext.isCandidateClass(MyConfig.class); // true
     *   boolean candidate2 = ConfigurationPropertiesBeanContext.isCandidateClass(String.class);   // false
     *   boolean candidate3 = ConfigurationPropertiesBeanContext.isCandidateClass(int.class);      // false
     * }</pre>
     *
     * @param beanClass the class to evaluate
     * @return {@code true} if the class is a binding candidate, {@code false} otherwise
     */
    static boolean isCandidateClass(Class<?> beanClass) {
        if (isPrimitiveOrWrapper(beanClass)) {
            return false;
        }
        String className = beanClass.getName();
        if (className.startsWith("java.")) {
            return false;
        }
        return isConcreteClass(beanClass);
    }

    private void publishEvent(ConfigurationProperty property, String propertyName, Object oldValue, Object newValue) {
        context.publishEvent(new ConfigurationPropertiesBeanPropertyChangedEvent(bean, propertyName, oldValue, newValue, property));
    }

    private String getPropertyName(ConfigurationPropertyName name) {
        return bindingPropertyNames.get(name.toString());
    }
}