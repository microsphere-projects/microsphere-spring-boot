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
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.source.ConfigurationProperty;
import org.springframework.boot.context.properties.source.ConfigurationPropertyName;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.ResolvableType;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;

import static io.microsphere.collection.MapUtils.newHashMap;
import static io.microsphere.constants.SymbolConstants.DOT;
import static io.microsphere.logging.LoggerFactory.getLogger;
import static io.microsphere.reflect.FieldUtils.findAllDeclaredFields;
import static io.microsphere.reflect.FieldUtils.getFieldValue;
import static io.microsphere.spring.boot.context.properties.source.util.ConfigurationPropertyUtils.toDashedForm;
import static io.microsphere.util.Assert.assertNotBlank;
import static io.microsphere.util.Assert.assertNotNull;
import static io.microsphere.util.ClassLoaderUtils.loadClass;
import static io.microsphere.util.ClassUtils.isConcreteClass;
import static io.microsphere.util.StringUtils.replace;
import static org.springframework.beans.BeanUtils.copyProperties;
import static org.springframework.beans.BeanUtils.getPropertyDescriptors;
import static org.springframework.boot.context.properties.source.ConfigurationPropertyName.Form.ORIGINAL;
import static org.springframework.boot.context.properties.source.ConfigurationPropertyName.of;
import static org.springframework.core.ResolvableType.forInstance;
import static org.springframework.util.ClassUtils.isPrimitiveOrWrapper;
import static org.springframework.util.ReflectionUtils.doWithFields;

/**
 * The context for the bean annotated {@link ConfigurationProperties @ConfigurationProperties}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
class ConfigurationPropertiesBeanContext {

    private static final Logger logger = getLogger(ConfigurationPropertiesBeanContext.class);

    /**
     * The {@link Class#getName() class name} of {@link org.springframework.boot.context.properties.bind.JavaBeanBinder.BeanProperty}
     */
    private static final String BEAN_PROPERTY_CLASS_NAME = "org.springframework.boot.context.properties.bind.JavaBeanBinder$BeanProperty";

    private static final ClassLoader classLoader = ConfigurationProperties.class.getClassLoader();

    /**
     * The {@link Class} of {@link org.springframework.boot.context.properties.bind.JavaBeanBinder.BeanProperty}
     */
    private static final Class<?> BEAN_PROPERTY_CLASS = loadClass(classLoader, BEAN_PROPERTY_CLASS_NAME);

    private final ResolvableType beanType;

    private final ConfigurationProperties annotation;

    private final String prefix;

    private final ConfigurableApplicationContext context;

    /**
     * The {@link ConfigurationPropertiesBeanProperty} map which key is the {@link ConfigurationPropertyName} and
     * value is the {@link ConfigurationPropertiesBeanProperty}.
     */
    private final Map<ConfigurationPropertyName, ConfigurationPropertiesBeanProperty> beanProperties;

    private volatile Object initializedBean;

    /**
     * Constructor
     *
     * @param beanType   the bean type
     * @param annotation the annotation of {@link ConfigurationProperties}
     * @param prefix     {@link ConfigurationProperties#prefix() the prefix}
     * @param context    {@link ConfigurableApplicationContext}
     * @throws IllegalArgumentException If <code>beanClass</code> or <code>annotation</code> or <code>context</code> argument is null,
     *                                  or the <code>prefix</code> is blank
     */
    public ConfigurationPropertiesBeanContext(ResolvableType beanType, ConfigurationProperties annotation, String prefix,
                                              ConfigurableApplicationContext context) throws IllegalArgumentException {
        assertNotNull(beanType, () -> "The 'beanType' must not be null!");
        assertNotNull(annotation, () -> "The 'annotation' must not be null!");
        assertNotBlank(prefix, () -> "The 'prefix' must not be black!");
        assertNotNull(context, () -> "The 'assertNotNull' must not be null!");
        // TODO support @ConstructorBinding creating beans
        this.beanType = beanType;
        this.annotation = annotation;
        this.prefix = prefix;
        this.context = context;
        this.beanProperties = newHashMap();
    }

    @Nonnull
    public ResolvableType getBeanType() {
        return beanType;
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
        return this.beanType.getRawClass();
    }

    /**
     * Returns the {@link ConfigurationProperties @ConfigurationProperties} annotation.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   ConfigurationPropertiesBeanContext ctx = ...;
     *   ConfigurationProperties annotation = ctx.getAnnotation();
     * }</pre>
     *
     * @return the annotation, never {@code null}
     */
    @Nonnull
    public ConfigurationProperties getAnnotation() {
        return annotation;
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
    @Nullable
    public Object getInitializedBean() {
        return this.initializedBean;
    }

    void initialize(Object bean) {
        this.initializedBean = bean;
        initBeanProperties(bean);
    }

    private void initBeanProperties(Object bean) {
        String prefix = getPrefix();
        ConfigurationPropertyName prefixName = of(prefix);
        initBeanProperties(forInstance(bean), prefixName, null);
    }

    private void initBeanProperties(ResolvableType beanType, ConfigurationPropertyName prefixName, String nestedPath) {
        Class<?> beanClass = beanType.getRawClass();
        if (isCandidateClass(beanClass)) {
            PropertyDescriptor[] descriptors = getPropertyDescriptors(beanClass);
            initBeanProperties(beanType, descriptors, prefixName, nestedPath);
            Set<Field> fields = findAllDeclaredFields(beanClass, MemberUtils::isNonStatic);
            initBeanProperties(beanType, fields, prefixName, nestedPath);
        }
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
        if (isPrimitiveOrWrapper(beanClass)) {
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
        return this.beanProperties.computeIfAbsent(propertyName, n -> newProperty(n, bindable));
    }

    @Nullable
    ConfigurationPropertiesBeanProperty newProperty(ConfigurationPropertyName name, Bindable<?> bindable) {
        Supplier<?> value = bindable.getValue();
        if (value == null) {
            if (logger.isWarnEnabled()) {
                logger.warn("The value from Bindable[{}] is null for the property name : '{}'", bindable, name);
            }
            return null;
        }
        Class<?> valueClass = value.getClass();
        ConfigurationPropertiesBeanProperty property = new ConfigurationPropertiesBeanProperty();

        doWithFields(valueClass, valueField -> {
            Object beanProperty = getFieldValue(value, valueField);
            if (beanProperty != null) {
                copyProperties(beanProperty, property);
            }
        }, f -> BEAN_PROPERTY_CLASS.equals(f.getType()));

        return property;
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

    void setProperty(ConfigurationProperty property, Object newValue) {
        ConfigurationPropertyName name = property.getName();
        ConfigurationPropertiesBeanProperty configurationPropertiesBeanProperty = getProperty(name);
        if (configurationPropertiesBeanProperty == null) {
            name = name.getParent();
            configurationPropertiesBeanProperty = getProperty(name);
        }
        if (configurationPropertiesBeanProperty != null) {
            ResolvableType propertyType = configurationPropertiesBeanProperty.getType();
            Object oldValue = configurationPropertiesBeanProperty.getValue();
            if (propertyType.isAssignableFrom(Map.class)) {
                Map<String, Object> map = (Map<String, Object>) oldValue;
                String propertyName = property.getName().getLastElement(ORIGINAL);
                oldValue = map.get(propertyName);
            }

            if (!Objects.deepEquals(oldValue, newValue)) {
                configurationPropertiesBeanProperty.setValue(newValue);
                publishEvent(property, configurationPropertiesBeanProperty, oldValue, newValue);
                if (logger.isInfoEnabled()) {
                    logger.info("Set property ['{}'] from [{}] to [{}]", name, oldValue, newValue);
                }
            }
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
