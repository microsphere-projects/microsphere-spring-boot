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

import io.github.microsphere.spring.boot.context.properties.ConfigurationPropertiesBeanContext;
import io.github.microsphere.spring.context.event.BeanPropertyChangedEvent;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.BindContext;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.source.ConfigurationPropertyName;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.util.Assert;

import java.util.HashMap;
import java.util.Map;

/**
 * A {@link BindListener} implementation of {@link ConfigurationProperties @ConfigurationProperties} Bean to publish
 * the {@link BeanPropertyChangedEvent}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see ConfigurationProperties
 * @see BeanPropertyChangedEvent
 * @since 1.0.0
 */
public class PublishingConfigurationPropertiesBeanPropertyChangedEventListener implements BindListener, BeanFactoryPostProcessor, ApplicationContextAware {

    private static final Class<ConfigurableApplicationContext> CONFIGURABLE_APPLICATION_CONTEXT_CLASS = ConfigurableApplicationContext.class;

    private static final Class<ConfigurationProperties> CONFIGURATION_PROPERTIES_CLASS = ConfigurationProperties.class;

    private Map<String, ConfigurationPropertiesBeanContext> contexts;

    private ConfigurableApplicationContext context;

    @Override
    public <T> void onStart(ConfigurationPropertyName name, Bindable<T> target, BindContext context) {
        BindListener.super.onStart(name, target, context);
    }

    @Override
    public void onSuccess(ConfigurationPropertyName name, Bindable<?> target, BindContext context, Object result) {
        if (isConfigurableListableBean(context)) {
            Object bean = result;
            String prefix = name.toString();
            ConfigurationPropertiesBeanContext beanContext =
                    contexts.computeIfAbsent(prefix, p -> new ConfigurationPropertiesBeanContext(p, bean, this.context));
            beanContext.update(bean);
        }
    }

    @Override
    public void onFinish(ConfigurationPropertyName name, Bindable<?> target, BindContext context, Object result) {
        BindListener.super.onFinish(name, target, context, result);
    }

    private boolean isConfigurableListableBean(BindContext context) {
        return context.getDepth() == 0;
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        String[] configurationPropertiesBeanNames = beanFactory.getBeanNamesForAnnotation(CONFIGURATION_PROPERTIES_CLASS);
        int configurationPropertiesBeanCount = configurationPropertiesBeanNames.length;
        this.contexts = new HashMap<>(configurationPropertiesBeanCount);
    }

    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        Class<ConfigurableApplicationContext> expectedType = CONFIGURABLE_APPLICATION_CONTEXT_CLASS;
        Assert.isInstanceOf(expectedType, context, "The 'context' argument is not an instance of " + expectedType.getName());
        this.context = expectedType.cast(context);
    }
}
