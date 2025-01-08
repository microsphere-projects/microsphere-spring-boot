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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.BindContext;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.source.ConfigurationProperty;
import org.springframework.boot.context.properties.source.ConfigurationPropertyName;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import static io.microsphere.spring.boot.context.properties.bind.util.BindUtils.isBoundProperty;
import static io.microsphere.spring.boot.context.properties.bind.util.BindUtils.isConfigurationPropertiesBean;
import static io.microsphere.spring.boot.context.properties.source.util.ConfigurationPropertyUtils.getPrefix;
import static io.microsphere.spring.boot.context.properties.util.ConfigurationPropertiesUtils.CONFIGURATION_PROPERTIES_CLASS;
import static io.microsphere.spring.boot.context.properties.util.ConfigurationPropertiesUtils.findConfigurationProperties;
import static org.springframework.util.Assert.isInstanceOf;

/**
 * A {@link BindListener} implementation of {@link ConfigurationProperties @ConfigurationProperties} Bean to publish
 * the {@link ConfigurationPropertiesBeanPropertyChangedEvent}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see ConfigurationProperties
 * @see ConfigurationPropertiesBeanContext
 * @see ConfigurationPropertiesBeanPropertyChangedEvent
 * @since 1.0.0
 */
public class EventPublishingConfigurationPropertiesBeanPropertyChangedListener implements BindListener, BeanFactoryPostProcessor, ApplicationContextAware, SmartInitializingSingleton {

    private final static Logger logger = LoggerFactory.getLogger(EventPublishingConfigurationPropertiesBeanPropertyChangedListener.class);

    private static final Class<ConfigurableApplicationContext> CONFIGURABLE_APPLICATION_CONTEXT_CLASS = ConfigurableApplicationContext.class;

    private Map<String, ConfigurationPropertiesBeanContext> beanContexts;

    private ConfigurableApplicationContext context;

    private boolean bound = false;


    @Override
    public <T> void onStart(ConfigurationPropertyName name, Bindable<T> target, BindContext context) {
        if (isBound()) {
        } else {
            initConfigurationPropertiesBeanContext(name, target, context);
        }
    }

    @Override
    public void onSuccess(ConfigurationPropertyName name, Bindable<?> target, BindContext context, Object result) {
        if (isBound()) {
            setConfigurationPropertiesBeanProperty(name, target, context, result);
        } else {
            initConfigurationPropertiesBeanContext(name, target, context);
        }
    }

    private void initConfigurationPropertiesBeanContext(ConfigurationPropertyName name, Bindable<?> target, BindContext context) {
        if (isConfigurationPropertiesBean(context)) {
            ConfigurationPropertiesBeanContext configurationPropertiesBeanContext = getConfigurationPropertiesBeanContext(name, target, context);
            Supplier<?> value = target.getValue();
            Object bean = value.get();
            if (bean != null) {
                logger.debug("The ConfigurationPropertiesBean binding is finished , configuration property name : '{}' , type : '{}' , depth : {} , bean : '{}'", name, target.getType(), context.getDepth(), bean);
                configurationPropertiesBeanContext.initialize(bean);
            }
        }
    }

    private ConfigurationPropertiesBeanContext getConfigurationPropertiesBeanContext(ConfigurationPropertyName name, Bindable<?> target, BindContext context) {
        String prefix = getPrefix(name, context);
        return beanContexts.computeIfAbsent(prefix, p -> {
            Class<?> beanClass = target.getType().getRawClass();
            ConfigurationProperties annotation = findConfigurationProperties(target);
            return new ConfigurationPropertiesBeanContext(beanClass, annotation, p, this.context);
        });
    }

    private void setConfigurationPropertiesBeanProperty(ConfigurationPropertyName name, Bindable<?> target, BindContext context, Object result) {
        ConfigurationProperty property = context.getConfigurationProperty();
        if (property != null && isBoundProperty(context)) {
            ConfigurationPropertiesBeanContext configurationPropertiesBeanContext = getConfigurationPropertiesBeanContext(name, target, context);
            configurationPropertiesBeanContext.setProperty(property, result);
            logger.debug("binding Bean property is finished , configuration property : '{}' , type : '{}' , depth : {} , result : '{}'", property, target.getType(), context.getDepth(), result);
        }
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        initConfigurationPropertiesBeanContexts(beanFactory);
    }

    private void initConfigurationPropertiesBeanContexts(ConfigurableListableBeanFactory beanFactory) {
        String[] beanNames = beanFactory.getBeanNamesForAnnotation(CONFIGURATION_PROPERTIES_CLASS);
        int beanCount = beanNames.length;
        this.beanContexts = new HashMap<>(beanCount);
    }

    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        Class<ConfigurableApplicationContext> expectedType = CONFIGURABLE_APPLICATION_CONTEXT_CLASS;
        isInstanceOf(expectedType, context, "The 'context' argument is not an instance of " + expectedType.getName());
        this.context = expectedType.cast(context);
    }

    @Override
    public void afterSingletonsInstantiated() {
        bound = true;
    }

    public boolean isBound() {
        return bound;
    }
}
