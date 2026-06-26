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
import io.microsphere.logging.Logger;
import io.microsphere.reflect.MemberUtils;
import io.microsphere.spring.boot.context.properties.ConfigurationPropertiesBeanInfo;
import io.microsphere.spring.boot.context.properties.bind.util.BindUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.source.ConfigurationProperty;
import org.springframework.boot.context.properties.source.ConfigurationPropertyName;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.ResolvableType;
import org.springframework.core.annotation.AnnotationAttributes;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;

import static io.microsphere.collection.MapUtils.newHashMap;
import static io.microsphere.constants.SeparatorConstants.LINE_SEPARATOR;
import static io.microsphere.constants.SymbolConstants.DOT;
import static io.microsphere.logging.LoggerFactory.getLogger;
import static io.microsphere.reflect.FieldUtils.findAllDeclaredFields;
import static io.microsphere.reflect.MethodUtils.invokeMethod;
import static io.microsphere.spring.boot.context.properties.source.util.ConfigurationPropertyUtils.newConfigurationPropertiesBeanProperty;
import static io.microsphere.spring.boot.context.properties.source.util.ConfigurationPropertyUtils.toDashedForm;
import static io.microsphere.text.FormatUtils.format;
import static io.microsphere.util.Assert.assertNotBlank;
import static io.microsphere.util.Assert.assertNotNull;
import static io.microsphere.util.ClassUtils.isConcreteClass;
import static io.microsphere.util.StringUtils.replace;
import static java.util.Objects.deepEquals;
import static org.springframework.beans.BeanUtils.getPropertyDescriptors;
import static org.springframework.boot.context.properties.bind.Bindable.of;
import static org.springframework.boot.context.properties.source.ConfigurationPropertyName.of;
import static org.springframework.core.ResolvableType.forInstance;
import static org.springframework.util.ClassUtils.isPrimitiveOrWrapper;

/**
 * The context for the bean annotated {@link ConfigurationProperties @ConfigurationProperties}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see ConfigurationProperties
 * @see org.springframework.boot.context.properties.bind.JavaBeanBinder
 * @see org.springframework.boot.context.properties.bind.JavaBeanBinder.BeanProperty
 * @since 1.0.0
 */
class ConfigurationPropertiesBeanContext {

    private static final Logger logger = getLogger(ConfigurationPropertiesBeanContext.class);

    @Nonnull
    private final ResolvableType beanType;

    @Nonnull
    private final AnnotationAttributes annotationAttributes;

    @Nonnull
    private final String prefix;

    @Nonnull
    private final ConfigurableApplicationContext context;

    /**
     * The {@link ConfigurationPropertiesBeanProperty} map which key is the {@link ConfigurationPropertyName} and
     * value is the {@link ConfigurationPropertiesBeanProperty}.
     */
    @Nonnull
    private final Map<ConfigurationPropertyName, ConfigurationPropertiesBeanProperty> beanProperties;

    @Nullable
    private volatile Object initializedBean;

    /**
     * Constructor
     *
     * @param beanType             the bean type
     * @param annotationAttributes the {@link AnnotationAttributes} of {@link ConfigurationProperties}
     * @param prefix               {@link ConfigurationProperties#prefix() the prefix}
     * @param context              {@link ConfigurableApplicationContext}
     * @throws IllegalArgumentException If <code>beanClass</code> or <code>annotation</code> or <code>context</code> argument is null,
     *                                  or the <code>prefix</code> is blank
     */
    ConfigurationPropertiesBeanContext(ResolvableType beanType, AnnotationAttributes annotationAttributes, String prefix,
                                       ConfigurableApplicationContext context) throws IllegalArgumentException {
        assertNotNull(beanType, () -> "The 'beanType' must not be null!");
        assertNotNull(annotationAttributes, () -> "The 'annotationAttributes' must not be null!");
        assertNotBlank(prefix, () -> "The 'prefix' must not be black!");
        assertNotNull(context, () -> "The 'assertNotNull' must not be null!");
        this.beanType = beanType;
        this.annotationAttributes = annotationAttributes;
        this.prefix = prefix;
        this.context = context;
        this.beanProperties = newHashMap();
    }

    void initialize(Object bean) {
        if (!this.beanType.isInstance(bean)) {
            if (logger.isWarnEnabled()) {
                Class<?> beanClass = bean.getClass();
                ConfigurationPropertiesBeanInfo beanInfo = new ConfigurationPropertiesBeanInfo(beanClass);
                logger.warn("The bean[info : {}] is not an instance of {}, they have same prefix : '{}' , expected annotation : {}",
                        beanInfo, this.beanType, this.prefix, this.annotationAttributes);
            }
            return;
        }
        this.initializedBean = bean;
        initBeanProperties(bean);

        if (logger.isTraceEnabled()) {
            StringJoiner beanInfo = new StringJoiner(LINE_SEPARATOR);
            for (Map.Entry<ConfigurationPropertyName, ConfigurationPropertiesBeanProperty> entry : this.beanProperties.entrySet()) {
                ConfigurationPropertyName propertyName = entry.getKey();
                ConfigurationPropertiesBeanProperty property = entry.getValue();
                beanInfo.add(format("Configuration Property Name : '{}' => Java Bean Property : {}", propertyName, property));
            }
            logger.trace("The ConfigurationPropertiesBeanContext is initialized, bean type : {} , prefix : '{}' , properties : {}",
                    this.beanType, this.prefix, beanInfo);
        }
    }

    private void initBeanProperties(Object bean) {
        String prefix = this.prefix;
        ConfigurationPropertyName prefixName = of(prefix);
        initBeanProperties(forInstance(bean), prefixName, null);
    }

    private void initBeanProperties(ResolvableType beanType, ConfigurationPropertyName prefixName, String nestedPath) {
        Class<?> beanClass = beanType.getRawClass();
        if (isCandidateClass(beanClass)) {
            Constructor<?> bindConstructor = getBindConstructor(beanType);
            if (bindConstructor == null) {
                PropertyDescriptor[] descriptors = getPropertyDescriptors(beanClass);
                initBeanProperties(beanType, descriptors, prefixName, nestedPath);
            } else {
                Set<Field> fields = findAllDeclaredFields(beanClass, MemberUtils::isNonStatic);
                initBeanProperties(beanType, fields, prefixName, nestedPath);
            }
        }
    }

    private Constructor<?> getBindConstructor(ResolvableType beanType) {
        return BindUtils.getBindConstructor(of(beanType), true);
    }

    private void initBeanProperties(ResolvableType beanType, PropertyDescriptor[] descriptors, ConfigurationPropertyName prefixName, String nestedPath) {
        for (PropertyDescriptor descriptor : descriptors) {
            initBeanProperty(beanType, descriptor, prefixName, nestedPath);
        }
    }

    private void initBeanProperty(ResolvableType beanType, PropertyDescriptor descriptor, ConfigurationPropertyName prefixName, String nestedPath) {
        if (isCandidateProperty(descriptor)) {
            String propertyName = descriptor.getName();
            String propertyPath = buildPropertyPath(propertyName, nestedPath);
            ConfigurationPropertyName configurationPropertyName = prefixName.append(toDashedForm(propertyName));
            ConfigurationPropertiesBeanProperty property = this.beanProperties.computeIfAbsent(configurationPropertyName, name -> {
                ConfigurationPropertiesBeanProperty newProperty = new ConfigurationPropertiesBeanProperty();
                newProperty.setDeclaringClassType(beanType);
                newProperty.setName(propertyPath);
                newProperty.setGetter(descriptor.getReadMethod());
                newProperty.setSetter(descriptor.getWriteMethod());
                return newProperty;
            });
            initBeanProperties(property.getType(), configurationPropertyName, propertyPath);
        }
    }

    private void initBeanProperties(ResolvableType beanType, Set<Field> fields, ConfigurationPropertyName prefixName, String nestedPath) {
        for (Field field : fields) {
            initBeanProperty(beanType, field, prefixName, nestedPath);
        }
    }

    private void initBeanProperty(ResolvableType beanType, Field field, ConfigurationPropertyName prefixName, String nestedPath) {
        String propertyName = field.getName();
        propertyName = replace(propertyName, "_", "");
        ConfigurationPropertyName configurationPropertyName = prefixName.append(toDashedForm(propertyName));
        String propertyPath = buildPropertyPath(field.getName(), nestedPath);
        ConfigurationPropertiesBeanProperty property = this.beanProperties.computeIfAbsent(configurationPropertyName, name -> {
            ConfigurationPropertiesBeanProperty newProperty = new ConfigurationPropertiesBeanProperty();
            newProperty.setDeclaringClassType(beanType);
            return newProperty;
        });
        property.setName(propertyPath);
        property.setField(field);
        initBeanProperties(property.getType(), configurationPropertyName, propertyPath);
    }

    private String buildPropertyPath(String propertyName, String nestedPath) {
        return nestedPath == null ? propertyName : nestedPath + DOT + propertyName;
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
        if (isPrimitiveOrWrapper(beanClass) || beanClass.isEnum()) {
            return false;
        }
        String className = beanClass.getName();
        if (className.startsWith("java.")) {
            return false;
        }
        return isConcreteClass(beanClass);
    }

    ConfigurationPropertiesBeanProperty initProperty(ConfigurationPropertyName propertyName, Bindable<?> bindable) {
        if (propertyName.isLastElementIndexed()) {
            propertyName = propertyName.getParent();
        }
        return this.beanProperties.computeIfAbsent(propertyName, n -> newConfigurationPropertiesBeanProperty(bindable));
    }

    ConfigurationPropertiesBeanProperty getProperty(ConfigurationPropertyName propertyName) {
        ConfigurationPropertiesBeanProperty property = this.beanProperties.get(propertyName);
        if (property == null) {
            if (logger.isWarnEnabled()) {
                logger.warn("No ConfigurationPropertiesBeanProperty was found by the configuration property name : '{}'", propertyName);
            }
        }
        return property;
    }

    void setProperty(ConfigurationProperty property, Object newValue, boolean publishedEvent) {
        ConfigurationPropertyName name = property.getName();

        ConfigurationPropertiesBeanProperty configurationPropertiesBeanProperty = null;

        if (name.isLastElementIndexed()) { // name is numerically indexed or non-numerically indexed
            name = name.getParent();
        }

        configurationPropertiesBeanProperty = getProperty(name);
        // name is not indexed or Map-typed
        if (configurationPropertiesBeanProperty == null) { // name is Map-typed
            name = name.getParent();
            configurationPropertiesBeanProperty = getProperty(name);
        }

        if (configurationPropertiesBeanProperty == null) {
            return;
        }

        ResolvableType propertyType = configurationPropertiesBeanProperty.getType();
        Class<?> propertyClass = propertyType.resolve();
        if (propertyClass.isInstance(newValue)) {
            Object oldValue = configurationPropertiesBeanProperty.getValue();
            if (!deepEquals(oldValue, newValue)) {
                setProperty(property, configurationPropertiesBeanProperty, name, oldValue, newValue, publishedEvent);
            }
        }
    }

    void setProperty(ConfigurationProperty property, ConfigurationPropertiesBeanProperty configurationPropertiesBeanProperty,
                     ConfigurationPropertyName name, Object oldValue, Object newValue, boolean publishedEvent) {
        if (newValue instanceof Cloneable) {
            // Use clone object to avoid the newValue is changed by other code, which will cause the oldValue and newValue are same
            newValue = invokeMethod(newValue, "clone");
        }
        configurationPropertiesBeanProperty.setValue(newValue);
        if (logger.isInfoEnabled()) {
            logger.info("Set property [name : '{}'] from '{}' to '{}' , ConfigurationPropertiesBeanProperty : {} , Source : {}",
                    name, oldValue, newValue, configurationPropertiesBeanProperty, property);
        }
        if (publishedEvent) {
            publishEvent(property, configurationPropertiesBeanProperty, oldValue, newValue);
        }
    }

    void publishEvent(ConfigurationProperty property, ConfigurationPropertiesBeanProperty configurationPropertiesBeanProperty,
                      Object oldValue, Object newValue) {
        String propertyName = configurationPropertiesBeanProperty.getName();
        ResolvableType propertyType = configurationPropertiesBeanProperty.getType();
        this.context.publishEvent(new ConfigurationPropertiesBeanPropertyChangedEvent(initializedBean, propertyName,
                propertyType, oldValue, newValue, property));
    }
}
