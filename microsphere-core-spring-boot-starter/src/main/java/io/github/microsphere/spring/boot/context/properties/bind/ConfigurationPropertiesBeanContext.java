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
package io.github.microsphere.spring.boot.context.properties.bind;

import io.github.microsphere.spring.boot.context.properties.ConfigurationPropertiesBeanInfo;
import io.github.microsphere.spring.context.event.JavaBeansPropertyChangeListenerAdapter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.source.ConfigurationPropertyName;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.util.ReflectionUtils;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyDescriptor;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static io.github.microsphere.spring.boot.context.properties.source.util.ConfigurationPropertyUtils.toDashedForm;
import static org.springframework.aop.support.AopUtils.getTargetClass;
import static org.springframework.beans.BeanUtils.copyProperties;
import static org.springframework.beans.BeanUtils.instantiateClass;

/**
 * The context for the bean annotated {@link ConfigurationProperties @ConfigurationProperties}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
class ConfigurationPropertiesBeanContext {

    private final ConfigurationPropertiesBeanInfo beanInfo;

    private Object initializedBean;

    private Map<String, Object> properties;

    private Map<String, String> bindingPropertyNames;

    private PropertyChangeSupport propertyChangeSupport;

    public ConfigurationPropertiesBeanContext(Class<?> beanClass, ConfigurationProperties annotation, String prefix) {
        this.beanInfo = new ConfigurationPropertiesBeanInfo(beanClass, annotation, prefix);
    }

    public void initialize(Object bean, ConfigurableApplicationContext context) {
        this.initializedBean = cloneBean(bean);
        initBinding(bean);
        this.propertyChangeSupport = buildPropertyChangeSupport(bean, context);
    }

    private Object cloneBean(Object bean) {
        Class<?> beanClass = beanInfo.getBeanClass();
        // TODO support @ConstructorBinding creating beans
        Object initializedBean = instantiateClass(beanClass);
        copyProperties(bean, initializedBean);
        return initializedBean;
    }

    private void initBinding(Object bean) {
        List<PropertyDescriptor> descriptors = beanInfo.getPropertyDescriptors();
        int descriptorSize = descriptors.size();
        Map<String, Object> properties = new HashMap<>(descriptorSize);
        Map<String, String> bindingPropertyNames = new HashMap<>(descriptorSize);
        String prefix = getPrefix();
        for (int i = 0; i < descriptorSize; i++) {
            PropertyDescriptor descriptor = descriptors.get(i);
            String propertyName = descriptor.getName();
            Object propertyValue = getPropertyValue(bean, descriptor);
            String configurationPropertyName = prefix + "." + toDashedForm(propertyName);
            properties.put(propertyName, propertyValue);
            bindingPropertyNames.put(configurationPropertyName, propertyName);
        }
        this.properties = properties;
        this.bindingPropertyNames = bindingPropertyNames;
    }

    private PropertyChangeSupport buildPropertyChangeSupport(Object bean, ConfigurableApplicationContext context) {
        PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(bean);
        propertyChangeSupport.addPropertyChangeListener(new JavaBeansPropertyChangeListenerAdapter(context));
        return propertyChangeSupport;
    }

    public void update(Object updatedBean) {
        Class<?> beanType = getTargetClass(updatedBean);

//        Collection<BeanProperty> beanProperties = bindingProperties.values();
//
//        for (BeanProperty beanProperty : beanProperties) {
//            PropertyDescriptor descriptor = beanProperty.getDescriptor();
//            String propertyName = beanProperty.getName();
//            Object newPropertyValue = getPropertyValue(updatedBean, descriptor);
//            setProperty(beanProperty, propertyName, newPropertyValue);
//        }

        // TODO
    }

    public void reset() {
        update(this.initializedBean);
    }

    public void setProperty(ConfigurationPropertyName name, Object newValue) {
        String propertyName = getPropertyName(name);
        Object oldValue = getPropertyValue(propertyName);
        if (!Objects.deepEquals(oldValue, newValue)) {
            propertyChangeSupport.firePropertyChange(propertyName, oldValue, newValue);
            properties.put(propertyName, newValue);
        }
    }

    private String getPropertyName(ConfigurationPropertyName name) {
        return bindingPropertyNames.get(name.toString());
    }

    private Object getPropertyValue(Object bean, PropertyDescriptor descriptor) {
        return ReflectionUtils.invokeMethod(descriptor.getReadMethod(), bean);
    }

    public String getPrefix() {
        return beanInfo.getPrefix();
    }

    public Class<?> getBeanClass() {
        return beanInfo.getBeanClass();
    }

    public Object getPropertyValue(String name) {
        return properties.get(name);
    }

    public Object getInitializedBean() {
        return initializedBean;
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(listener);
    }
}
