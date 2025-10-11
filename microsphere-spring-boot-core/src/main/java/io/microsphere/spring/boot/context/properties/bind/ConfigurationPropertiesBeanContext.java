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

    public ConfigurationPropertiesBeanContext(Class<?> beanClass, ConfigurationProperties annotation, String prefix, ConfigurableApplicationContext context) {
        // TODO support @ConstructorBinding creating beans
        this.annotation = annotation;
        this.prefix = prefix;
        this.context = context;
        this.initializedBeanWrapper = createInitializedBeanWrapper(beanClass);
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

    protected void initialize(Object bean) {
        this.bean = bean;
        setProperties(bean);
        initBinding(bean);
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

    boolean isCandidateProperty(PropertyDescriptor descriptor) {
        Method readMethod = descriptor.getReadMethod();
        return readMethod == null ? true : !Object.class.equals(readMethod.getDeclaringClass());
    }

    boolean isCandidateClass(Class<?> beanClass) {
        if (isPrimitiveOrWrapper(beanClass)) {
            return false;
        }
        String className = beanClass.getName();
        if (className.startsWith("java.") || className.startsWith("javax.")) {
            return false;
        }
        return isConcreteClass(beanClass);
    }

    public void setProperty(ConfigurationProperty property, Object newValue) {
        ConfigurationPropertyName name = property.getName();
        String propertyName = getPropertyName(name);
        Object oldValue = getPropertyValue(propertyName);
        if (!Objects.deepEquals(oldValue, newValue)) {
            initializedBeanWrapper.setPropertyValue(propertyName, newValue);
            publishEvent(property, propertyName, oldValue, newValue);
        }
    }

    private void publishEvent(ConfigurationProperty property, String propertyName, Object oldValue, Object newValue) {
        context.publishEvent(new ConfigurationPropertiesBeanPropertyChangedEvent(bean, propertyName, oldValue, newValue, property));
    }

    private String getPropertyName(ConfigurationPropertyName name) {
        return bindingPropertyNames.get(name.toString());
    }

    public String getPrefix() {
        return prefix;
    }

    public Class<?> getBeanClass() {
        return initializedBeanWrapper.getWrappedClass();
    }

    public Object getPropertyValue(String name) {
        return initializedBeanWrapper.getPropertyValue(name);
    }

    public Object getInitializedBean() {
        return this.initializedBeanWrapper.getWrappedInstance();
    }
}
