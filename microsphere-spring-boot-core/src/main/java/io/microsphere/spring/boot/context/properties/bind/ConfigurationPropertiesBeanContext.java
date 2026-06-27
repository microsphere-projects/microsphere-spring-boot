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
import io.microsphere.reflect.FieldUtils;
import io.microsphere.reflect.MemberUtils;
import io.microsphere.spring.boot.context.properties.bind.util.BindUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
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
import static io.microsphere.constants.SymbolConstants.DOT_CHAR;
import static io.microsphere.lang.function.ThrowableSupplier.execute;
import static io.microsphere.logging.LoggerFactory.getLogger;
import static io.microsphere.reflect.FieldUtils.findAllDeclaredFields;
import static io.microsphere.reflect.MethodUtils.invokeMethod;
import static io.microsphere.spring.boot.context.properties.source.util.ConfigurationPropertyUtils.getParent;
import static io.microsphere.spring.boot.context.properties.source.util.ConfigurationPropertyUtils.newConfigurationPropertiesBeanProperty;
import static io.microsphere.spring.boot.context.properties.source.util.ConfigurationPropertyUtils.toDashedForm;
import static io.microsphere.text.FormatUtils.format;
import static io.microsphere.util.Assert.assertNotBlank;
import static io.microsphere.util.Assert.assertNotNull;
import static io.microsphere.util.ClassUtils.isConcreteClass;
import static io.microsphere.util.StringUtils.isBlank;
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

    private volatile BeanWrapper beanWrapper;

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
                logger.warn("The bean[{}] is not an instance of {}, they have same prefix : '{}' , expected annotation : {}",
                        beanClass, this.beanType, this.prefix, this.annotationAttributes);
            }
            return;
        }
        this.initializedBean = bean;
        this.beanWrapper = new BeanWrapperImpl(bean);
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
                Method getter = descriptor.getReadMethod();
                Method setter = descriptor.getWriteMethod();
                Object value = getPropertyValue(nestedPath, propertyPath);
                newProperty.setDeclaringClassType(beanType);
                newProperty.setName(propertyPath);
                newProperty.setGetter(getter);
                newProperty.setSetter(setter);
                newProperty.setValue(value);
                return newProperty;
            });
            initBeanProperties(property.getType(), configurationPropertyName, propertyPath);
        }
    }

    private Object getPropertyValue(@Nullable String nestedPath, String propertyPath) {
        if (!isBlank(nestedPath)) {
            Object parent = getPropertyValue(nestedPath);
            if (parent == null) {
                if (logger.isTraceEnabled()) {
                    logger.trace("The instance is null in nested path : '{}' , property path : {}", nestedPath, propertyPath);
                }
                return null;
            }
        }
        return getPropertyValue(propertyPath);
    }

    Object getPropertyValue(String propertyPath) {
        return execute(() -> this.beanWrapper.getPropertyValue(propertyPath), e -> {
            if (logger.isTraceEnabled()) {
                logger.trace("Failed to get property value for property path : {}", propertyPath, e);
            }
            return null;
        });
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
        Object value = readFieldValue(field, nestedPath);
        property.setName(propertyPath);
        property.setField(field);
        property.setValue(value);
        initBeanProperties(property.getType(), configurationPropertyName, propertyPath);
    }

    Object readFieldValue(Field field, @Nullable String nestedPath) {
        Object instance = getInstance(this.initializedBean, nestedPath);
        if (instance == null) {
            return null;
        }
        return FieldUtils.getFieldValue(instance, field);
    }

    static Object getInstance(Object bean, @Nullable String nestedPath) {
        if (isBlank(nestedPath)) {
            return bean;
        }
        int index = nestedPath.indexOf(DOT_CHAR);
        if (index == -1) {
            return getFieldValue(bean, nestedPath);
        }
        String fieldName = nestedPath.substring(0, index);
        Object instance = getFieldValue(bean, fieldName);
        if (instance == null) {
            return null;
        }
        String subNestedPath = nestedPath.substring(index + 1);
        return getFieldValue(instance, subNestedPath);
    }

    static Object getFieldValue(Object instance, String fieldName) {
        Object fieldValue = FieldUtils.getFieldValue(instance, fieldName);
        if (fieldValue == null) {
            if (logger.isTraceEnabled()) {
                logger.trace("The field[name : '{}'] value can't be found in the instance : '{}'", fieldName, instance);
            }
        }
        return fieldValue;
    }

    static String buildPropertyPath(String propertyName, @Nullable String nestedPath) {
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
            propertyName = getParent(propertyName);
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
            name = getParent(name);
        }

        configurationPropertiesBeanProperty = getProperty(name);
        // name is not indexed or Map-typed
        if (configurationPropertiesBeanProperty == null) { // name is Map-typed
            name = getParent(name);
            configurationPropertiesBeanProperty = getProperty(name);
        }

        if (configurationPropertiesBeanProperty == null) {
            return;
        }

        ResolvableType propertyType = configurationPropertiesBeanProperty.getType();
        if (propertyType.isInstance(newValue)) {
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
