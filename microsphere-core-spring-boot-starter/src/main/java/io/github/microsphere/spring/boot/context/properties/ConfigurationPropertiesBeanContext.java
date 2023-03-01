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
package io.github.microsphere.spring.boot.context.properties;

import io.github.microsphere.beans.BeanProperty;
import io.github.microsphere.spring.context.event.JavaBeansPropertyChangeListenerAdapter;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.util.ReflectionUtils;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyDescriptor;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static io.github.microsphere.spring.boot.util.ConfigurationPropertyUtils.toDashedForm;
import static java.beans.Introspector.decapitalize;

/**
 * The context of {@link ConfigurationProperties @ConfigurationProperties} Bean
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public class ConfigurationPropertiesBeanContext {

    private final String prefix;

    private final Object initializedBean;

    private final Class<?> beanType;

    private final Map<String, BeanProperty> bindingProperties;

    private final PropertyChangeSupport propertyChangeSupport;

    public ConfigurationPropertiesBeanContext(String prefix, Object initializedBean, ConfigurableApplicationContext context) {
        this.prefix = prefix;
        this.initializedBean = initializedBean;
        this.beanType = AopUtils.getTargetClass(initializedBean);
        this.bindingProperties = initBindingProperties(initializedBean);
        this.propertyChangeSupport = initPropertyChangeSupport(initializedBean, context);
    }

    private Map<String, BeanProperty> initBindingProperties(Object bean) {
        BeanWrapper beanWrapper = new BeanWrapperImpl(bean);
        PropertyDescriptor[] descriptors = beanWrapper.getPropertyDescriptors();
        int descriptorCount = descriptors.length;
        Map<String, BeanProperty> bindingProperties = new HashMap<>(descriptorCount << 1);
        for (int i = 0; i < descriptorCount; i++) {
            PropertyDescriptor descriptor = descriptors[i];
            String propertyName = decapitalize(descriptor.getName());
            Object propertyValue = getPropertyValue(bean, descriptor);
            BeanProperty beanProperty = new BeanProperty();
            beanProperty.setName(propertyName);
            beanProperty.setValue(propertyValue);
            beanProperty.setDescriptor(descriptor);
            beanProperty.setDeclaringClass(beanType);
            String configurationPropertyName = prefix + "." + toDashedForm(propertyName);
            bindingProperties.put(propertyName, beanProperty);
            bindingProperties.put(configurationPropertyName, beanProperty);
        }
        return bindingProperties;
    }

    private PropertyChangeSupport initPropertyChangeSupport(Object bean, ConfigurableApplicationContext context) {
        PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(bean);
        propertyChangeSupport.addPropertyChangeListener(new JavaBeansPropertyChangeListenerAdapter(context));
        return propertyChangeSupport;
    }

    public void update(Object updatedBean) {
        Class<?> beanType = AopUtils.getTargetClass(initializedBean);
        if (!this.beanType.equals(beanType)) {
            throw new IllegalArgumentException("The type of updated bean does not match , expect : " + this.beanType.getName() + " , actual : " + beanType.getName());
        }

        Collection<BeanProperty> beanProperties = bindingProperties.values();

        for (BeanProperty beanProperty : beanProperties) {
            PropertyDescriptor descriptor = beanProperty.getDescriptor();
            String propertyName = beanProperty.getName();
            Object newPropertyValue = getPropertyValue(updatedBean, descriptor);
            setProperty(beanProperty, propertyName, newPropertyValue);
        }
    }

    public void reset() {
        update(this.initializedBean);
    }

    public void setProperty(String propertyName, Object newValue) {
        BeanProperty beanProperty = getBeanProperty(propertyName);
        setProperty(beanProperty, propertyName, newValue);
    }

    private Object getPropertyValue(Object bean, PropertyDescriptor descriptor) {
        return ReflectionUtils.invokeMethod(descriptor.getReadMethod(), bean);
    }

    private void setProperty(BeanProperty beanProperty, String propertyName, Object newValue) {
        Object oldValue = beanProperty.getValue();
        if (!Objects.deepEquals(oldValue, newValue)) {
            propertyChangeSupport.firePropertyChange(propertyName, oldValue, newValue);
            beanProperty.setValue(newValue);
        }
    }

    public String getPrefix() {
        return prefix;
    }

    public BeanProperty getBeanProperty(String name) {
        return bindingProperties.get(name);
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
