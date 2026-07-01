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
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
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
import static io.microsphere.reflect.FieldUtils.findField;
import static io.microsphere.reflect.MethodUtils.invokeMethod;
import static io.microsphere.spring.boot.context.properties.bind.util.BindUtils.getBindConstructor;
import static io.microsphere.spring.boot.context.properties.source.util.ConfigurationPropertyUtils.getParent;
import static io.microsphere.spring.boot.context.properties.source.util.ConfigurationPropertyUtils.toDashedForm;
import static io.microsphere.spring.boot.context.properties.util.ConfigurationPropertiesUtils.CONFIGURATION_PROPERTIES_CLASS;
import static io.microsphere.spring.core.annotation.AnnotationUtils.getAnnotationAttributes;
import static io.microsphere.text.FormatUtils.format;
import static io.microsphere.util.Assert.assertNotBlank;
import static io.microsphere.util.Assert.assertNotNull;
import static io.microsphere.util.ClassUtils.isConcreteClass;
import static io.microsphere.util.StringUtils.isBlank;
import static io.microsphere.util.StringUtils.replace;
import static java.util.Objects.deepEquals;
import static org.springframework.beans.BeanUtils.copyProperties;
import static org.springframework.beans.BeanUtils.getPropertyDescriptors;
import static org.springframework.beans.BeanUtils.instantiateClass;
import static org.springframework.boot.context.properties.source.ConfigurationPropertyName.of;
import static org.springframework.util.ClassUtils.isAssignableValue;
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
    private final String beanName;

    @Nonnull
    private final ResolvableType beanType;

    private final Constructor<?> bindConstructor;

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

    @Nullable
    private volatile BeanWrapper beanWrapper;

    /**
     * Constructor
     *
     * @param beanName             the bean name
     * @param beanType             the bean type
     * @param annotationAttributes the {@link AnnotationAttributes} of {@link ConfigurationProperties}
     * @param context              {@link ConfigurableApplicationContext}
     * @throws IllegalArgumentException If <code>beanClass</code> or <code>annotation</code> or <code>context</code> argument is null,
     *                                  or the <code>prefix</code> is blank
     */
    ConfigurationPropertiesBeanContext(String beanName, ResolvableType beanType, AnnotationAttributes annotationAttributes,
                                       ConfigurableApplicationContext context) throws IllegalArgumentException {
        assertNotBlank(beanName, () -> "The 'beanName' must not be null!");
        assertNotNull(beanType, () -> "The 'beanType' must not be null!");
        assertNotNull(annotationAttributes, () -> "The 'annotationAttributes' must not be null!");
        assertNotNull(context, () -> "The 'assertNotNull' must not be null!");
        this.beanName = beanName;
        this.beanType = beanType;
        this.bindConstructor = getBindConstructor(beanType);
        this.annotationAttributes = annotationAttributes;
        this.prefix = annotationAttributes.getString("prefix");
        this.context = context;
        this.beanProperties = newHashMap();
    }

    /**
     * Get the bean name
     *
     * @return the bean name
     */
    @Nonnull
    String getBeanName() {
        return this.beanName;
    }

    /**
     * Get the bean type
     *
     * @return the bean type
     */
    @Nonnull
    ResolvableType getBeanType() {
        return this.beanType;
    }

    /**
     * Get the prefix
     *
     * @return the prefix
     */
    @Nonnull
    String getPrefix() {
        return this.prefix;
    }

    /**
     * Get the bean class
     *
     * @return the bean class
     */
    @Nonnull
    Class<?> getBeanClass() {
        return getBeanType().resolve();
    }

    void initializeBean(Object bean) {
        if (!this.beanType.isInstance(bean)) {
            if (logger.isWarnEnabled()) {
                Class<?> beanClass = bean.getClass();
                logger.warn("The bean[{}] is not an instance of {}, they have same prefix : '{}' , expected annotation : {}",
                        beanClass, this.beanType, this.prefix, this.annotationAttributes);
            }
            return;
        }
        this.initializedBean = bean;
        initBeanProperties();
        if (logger.isTraceEnabled()) {
            StringJoiner beanInfo = new StringJoiner(LINE_SEPARATOR);
            for (Map.Entry<ConfigurationPropertyName, ConfigurationPropertiesBeanProperty> entry : this.beanProperties.entrySet()) {
                ConfigurationPropertyName propertyName = entry.getKey();
                ConfigurationPropertiesBeanProperty property = entry.getValue();
                beanInfo.add(format("Configuration Property Name : '{}' => Bean Property : {}", propertyName, property));
            }
            logger.trace("The ConfigurationPropertiesBeanContext is initialized, bean type : {} , prefix : '{}' , properties : {}",
                    this.beanType, this.prefix, beanInfo);
        }
    }

    private void initBeanProperties() {
        Object bean = getBean();
        this.beanWrapper = new BeanWrapperImpl(bean);
        initBeanProperties(this.beanType, of(this.prefix), null);
    }

    private void initBeanProperties(ResolvableType beanType, ConfigurationPropertyName prefixName, String nestedPath) {
        Class<?> beanClass = beanType.resolve();
        if (isCandidateClass(beanClass)) {
            Constructor<?> bindConstructor = this.bindConstructor;
            if (bindConstructor == null) {
                PropertyDescriptor[] descriptors = getPropertyDescriptors(beanClass);
                initBeanProperties(beanType, descriptors, prefixName, nestedPath);
            } else {
                Set<Field> fields = findAllDeclaredFields(beanClass, MemberUtils::isNonStatic);
                initBeanProperties(beanType, fields, prefixName, nestedPath);
            }
        }
    }

    /**
     * Binds the property values of the bean after {@link Binder#bind(String, Bindable) binding}.
     *
     * @see Binder#bind(String, Bindable)
     */
    void bindPropertyValues() {
        if (this.bindConstructor == null) {
            this.beanProperties.values().forEach(this::bindPropertyValue);
        } else {
            initBeanProperties();
        }
    }

    boolean bindPropertyValue(ConfigurationPropertiesBeanProperty beanProperty) {
        String propertyPath = beanProperty.getName();
        Object newValue = getPropertyValue(propertyPath);
        return setProperty(beanProperty, beanProperty.getValue(), newValue, false);
    }

    Object getBean() {
        Object bean = this.initializedBean;
        if (bean == null) {
            // Get the bean from the Spring context by its name
            String beanName = getBeanName();
            bean = this.context.getBean(beanName, getBeanClass());
            this.initializedBean = bean;
        }
        return bean;
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
                ConfigurationPropertiesBeanProperty newBeanProperty = new ConfigurationPropertiesBeanProperty();
                Method getter = descriptor.getReadMethod();
                Method setter = descriptor.getWriteMethod();
                Object value = getPropertyValue(nestedPath, propertyPath);
                newBeanProperty.setDeclaringClassType(beanType);
                newBeanProperty.setName(propertyPath);
                newBeanProperty.setGetter(getter);
                newBeanProperty.setSetter(setter);
                newBeanProperty.setValue(value);
                return newBeanProperty;
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
        ConfigurationPropertiesBeanProperty beanProperty = this.beanProperties.computeIfAbsent(configurationPropertyName, name -> {
            ConfigurationPropertiesBeanProperty newProperty = new ConfigurationPropertiesBeanProperty();
            newProperty.setDeclaringClassType(beanType);
            return newProperty;
        });
        Object value = readFieldValue(field, nestedPath);
        beanProperty.setName(propertyPath);
        beanProperty.setField(field);
        beanProperty.setValue(value);
        initBeanProperties(beanProperty.getType(), configurationPropertyName, propertyPath);
    }

    void setProperty(ConfigurationProperty property, Object newValue) {
        ConfigurationPropertyName name = property.getName();
        ConfigurationPropertiesBeanProperty beanProperty = null;

        if (name.isLastElementIndexed()) { // name is numerically indexed or non-numerically indexed
            name = getParent(name);
        }

        beanProperty = getProperty(name);
        // name is not indexed or Map-typed
        if (beanProperty == null) { // name is Map-typed
            name = getParent(name);
            beanProperty = getProperty(name);
        }

        if (beanProperty == null) {
            return;
        }

        ResolvableType propertyType = beanProperty.getType();
        Class<?> propertyClass = propertyType.resolve();
        if (isAssignableValue(propertyClass, newValue)) {
            Object oldValue = beanProperty.getValue();
            if (setProperty(beanProperty, oldValue, newValue, true)) {
                publishEvent(property, beanProperty, oldValue, newValue);
            }
        }
    }

    boolean setProperty(ConfigurationPropertiesBeanProperty beanProperty, Object oldValue, Object newValue,
                        boolean resolved) {
        boolean changed = false;
        String propertyPath = beanProperty.getName();
        Object actualNewValue = resolveNewPropertyValue(propertyPath, newValue, resolved);
        if (!deepEquals(oldValue, actualNewValue)) {
            // Set the new value if it is different from the old value
            beanProperty.setValue(actualNewValue);
            changed = true;
            if (logger.isTraceEnabled()) {
                logger.trace("Set property [path : '{}'] from '{}' to '{}'(actual : '{}') , Bean Property : {}",
                        beanProperty.getName(), oldValue, newValue, actualNewValue, beanProperty);
            }
        }
        return changed;
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
        return getPropertyValue(this.beanWrapper, propertyPath);
    }

    Object readFieldValue(Field field, @Nullable String nestedPath) {
        Object instance = getInstance(getBean(), nestedPath);
        if (instance == null) {
            return null;
        }
        return getFieldValue(instance, field);
    }

    ConfigurationPropertiesBeanProperty getProperty(ConfigurationPropertyName propertyName) {
        ConfigurationPropertiesBeanProperty property = this.beanProperties.get(propertyName);
        if (property == null) {
            if (logger.isTraceEnabled()) {
                logger.trace("No ConfigurationPropertiesBeanProperty was found by the configuration property name : '{}'", propertyName);
            }
        }
        return property;
    }

    void publishEvent(ConfigurationProperty property, ConfigurationPropertiesBeanProperty beanProperty,
                      Object oldValue, Object newValue) {
        String propertyName = beanProperty.getName();
        ResolvableType propertyType = beanProperty.getType();
        this.context.publishEvent(new ConfigurationPropertiesBeanPropertyChangedEvent(getBean(), propertyName,
                propertyType, oldValue, newValue, property));
    }

    Object cloneBean() {
        Object bean = getBean();
        Object clnoedBean = null;
        Class<?> beanClass = getBeanClass();
        Constructor<?> bindConstructor = this.bindConstructor;
        if (bindConstructor == null) {
            clnoedBean = instantiateClass(beanClass);
            copyProperties(bean, clnoedBean);
        } else {
            clnoedBean = bean;
        }
        return clnoedBean;
    }

    static Map<String, ConfigurationPropertiesBeanContext> buildConfigurationPropertiesBeanContexts(ConfigurableApplicationContext context) {
        ConfigurableListableBeanFactory beanFactory = context.getBeanFactory();
        String[] beanDefinitionNames = beanFactory.getBeanDefinitionNames();
        Map<String, ConfigurationPropertiesBeanContext> beanContexts = newHashMap();
        for (String beanName : beanDefinitionNames) {
            BeanDefinition beanDefinition = beanFactory.getMergedBeanDefinition(beanName);
            ResolvableType beanType = beanDefinition.getResolvableType();
            Class<?> beanClass = beanType.resolve();
            ConfigurationProperties annotation = beanClass == null ? null : beanClass.getAnnotation(CONFIGURATION_PROPERTIES_CLASS);
            if (annotation != null) {
                AnnotationAttributes annotationAttributes = getAnnotationAttributes(annotation);
                ConfigurationPropertiesBeanContext beanContext = new ConfigurationPropertiesBeanContext(beanName, beanType, annotationAttributes, context);
                beanContexts.put(beanContext.getPrefix(), beanContext);
            }
        }
        return beanContexts;
    }

    /**
     * Clone object to avoid the newValue is changed by other code, which will cause the oldValue and newValue are same.
     *
     * @param value the value to be cloned
     * @return the cloned value
     */
    static Object clone(Object value) {
        if (value instanceof Cloneable) {
            value = invokeMethod(value, "clone");
        }
        return value;
    }

    static Object getPropertyValue(BeanWrapper beanWrapper, String propertyPath) {
        if (beanWrapper == null) {
            return null;
        }
        Object propertyValue = execute(() -> {
            if (beanWrapper.isReadableProperty(propertyPath)) {
                return beanWrapper.getPropertyValue(propertyPath);
            }
            if (logger.isTraceEnabled()) {
                logger.trace("The property[path: {}] can't be readable for in the Bean[{}]", propertyPath,
                        beanWrapper.getWrappedInstance());
            }
            return null;
        }, e -> {
            if (logger.isWarnEnabled()) {
                logger.warn("Can't get property value for property path : '{}' in the Bean[{}]", propertyPath,
                        beanWrapper.getWrappedInstance(), e);
            }
            return null;
        });

        return clone(propertyValue);
    }

    Object resolveNewPropertyValue(String propertyPath, Object newValue, boolean resolved) {
        if (resolved) {
            Object clonedBean = cloneBean();
            BeanWrapper clonedBeanWrapper = new BeanWrapperImpl(clonedBean);
            if (setPropertyValue(clonedBeanWrapper, propertyPath, newValue)) {
                return getPropertyValue(clonedBeanWrapper, propertyPath);
            }
        }
        return newValue;
    }

    boolean setPropertyValue(String propertyPath, Object newValue) {
        return setPropertyValue(this.beanWrapper, propertyPath, newValue);
    }

    static boolean setPropertyValue(BeanWrapper beanWrapper, String propertyPath, Object newValue) {
        if (beanWrapper == null) {
            return false;
        }
        return execute(() -> {
            if (beanWrapper.isWritableProperty(propertyPath)) {
                beanWrapper.setPropertyValue(propertyPath, newValue);
                return true;
            }
            return false;
        }, e -> {
            if (logger.isWarnEnabled()) {
                logger.warn("Can't set property value for property path : '{}' in the Bean[{}]", propertyPath,
                        beanWrapper.getWrappedInstance(), e);
            }
            return false;
        });
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
        Field field = findField(instance, fieldName);
        return getFieldValue(instance, field);
    }

    static Object getFieldValue(Object instance, Field field) {
        Object fieldValue = FieldUtils.getFieldValue(instance, field);
        if (fieldValue == null) {
            if (logger.isTraceEnabled()) {
                logger.trace("The field['{}'] value can't be found in the instance : '{}'", field, instance);
            }
        }
        return clone(fieldValue);
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
        return readMethod == null || !Object.class.equals(readMethod.getDeclaringClass());
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
}
