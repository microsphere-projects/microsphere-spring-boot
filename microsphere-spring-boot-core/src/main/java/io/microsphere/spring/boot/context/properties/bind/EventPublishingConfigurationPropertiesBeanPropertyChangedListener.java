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

import io.microsphere.logging.Logger;
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

import static io.microsphere.logging.LoggerFactory.getLogger;
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

    private final static Logger logger = getLogger(EventPublishingConfigurationPropertiesBeanPropertyChangedListener.class);

    private static final Class<ConfigurableApplicationContext> CONFIGURABLE_APPLICATION_CONTEXT_CLASS = ConfigurableApplicationContext.class;

    private Map<String, ConfigurationPropertiesBeanContext> beanContexts;

    private ConfigurableApplicationContext context;

    private boolean bound = false;

    /**
     * Handles the start of a binding operation. During initial binding, initializes the
     * {@link ConfigurationPropertiesBeanContext} for the target bean.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   EventPublishingConfigurationPropertiesBeanPropertyChangedListener listener = ...;
     *   listener.onStart(ConfigurationPropertyName.of("app"),
     *       Bindable.of(MyProps.class), context);
     * }</pre>
     *
     * @param name    the configuration property name
     * @param target  the bindable target
     * @param context the bind context
     */
    @Override
    public <T> void onStart(ConfigurationPropertyName name, Bindable<T> target, BindContext context) {
        if (isBound()) {
        } else {
            initConfigurationPropertiesBeanContext(name, target, context);
        }
    }

    /**
     * Handles a successful binding operation. After initial binding is complete, detects property
     * changes and publishes {@link ConfigurationPropertiesBeanPropertyChangedEvent}s.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   EventPublishingConfigurationPropertiesBeanPropertyChangedListener listener = ...;
     *   listener.onSuccess(ConfigurationPropertyName.of("app.name"),
     *       Bindable.of(String.class), context, "newValue");
     * }</pre>
     *
     * @param name    the configuration property name
     * @param target  the bindable target
     * @param context the bind context
     * @param result  the bound result value
     */
    @Override
    public void onSuccess(ConfigurationPropertyName name, Bindable<?> target, BindContext context, Object result) {
        if (isBound()) {
            setConfigurationPropertiesBeanProperty(name, target, context, result);
        } else {
            initConfigurationPropertiesBeanContext(name, target, context);
        }
    }

    /**
     * Initializes a {@link ConfigurationPropertiesBeanContext} for the given
     * {@link ConfigurationProperties @ConfigurationProperties} bean if at root binding depth.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   EventPublishingConfigurationPropertiesBeanPropertyChangedListener listener = ...;
     *   listener.initConfigurationPropertiesBeanContext(
     *       ConfigurationPropertyName.of("app"), Bindable.of(MyProps.class), context);
     * }</pre>
     *
     * @param name    the configuration property name
     * @param target  the bindable target
     * @param context the bind context
     */
    void initConfigurationPropertiesBeanContext(ConfigurationPropertyName name, Bindable<?> target, BindContext context) {
        if (isConfigurationPropertiesBean(context)) {
            ConfigurationPropertiesBeanContext configurationPropertiesBeanContext = getConfigurationPropertiesBeanContext(name, target, context);
            Supplier<?> value = target.getValue();
            Object bean = value.get();
            if (bean != null) {
                logger.trace("The ConfigurationPropertiesBean binding is finished , configuration property name : '{}' , type : '{}' , depth : {} , bean : '{}'", name, target.getType(), context.getDepth(), bean);
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

    /**
     * Sets a property on the {@link ConfigurationPropertiesBeanContext} when a bound property
     * change is detected. Publishes an event if the value has changed.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   EventPublishingConfigurationPropertiesBeanPropertyChangedListener listener = ...;
     *   listener.setConfigurationPropertiesBeanProperty(
     *       ConfigurationPropertyName.of("app.name"),
     *       Bindable.of(String.class), context, "newValue");
     * }</pre>
     *
     * @param name    the configuration property name
     * @param target  the bindable target
     * @param context the bind context
     * @param result  the new property value
     */
    void setConfigurationPropertiesBeanProperty(ConfigurationPropertyName name, Bindable<?> target, BindContext context, Object result) {
        ConfigurationProperty property = context.getConfigurationProperty();
        if (property != null && isBoundProperty(context)) {
            ConfigurationPropertiesBeanContext configurationPropertiesBeanContext = getConfigurationPropertiesBeanContext(name, target, context);
            configurationPropertiesBeanContext.setProperty(property, result);
            logger.trace("binding Bean property is finished , configuration property : '{}' , type : '{}' , depth : {} , result : '{}'", property, target.getType(), context.getDepth(), result);
        }
    }

    /**
     * Post-processes the bean factory to initialize the internal map of
     * {@link ConfigurationPropertiesBeanContext} instances for all
     * {@link ConfigurationProperties @ConfigurationProperties} beans.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   EventPublishingConfigurationPropertiesBeanPropertyChangedListener listener =
     *       new EventPublishingConfigurationPropertiesBeanPropertyChangedListener();
     *   listener.postProcessBeanFactory(beanFactory);
     * }</pre>
     *
     * @param beanFactory the bean factory to post-process
     * @throws BeansException if an error occurs
     */
    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        initConfigurationPropertiesBeanContexts(beanFactory);
    }

    private void initConfigurationPropertiesBeanContexts(ConfigurableListableBeanFactory beanFactory) {
        String[] beanNames = beanFactory.getBeanNamesForAnnotation(CONFIGURATION_PROPERTIES_CLASS);
        int beanCount = beanNames.length;
        this.beanContexts = new HashMap<>(beanCount);
    }

    /**
     * Sets the {@link ApplicationContext}, which must be a {@link ConfigurableApplicationContext},
     * for publishing property change events.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   EventPublishingConfigurationPropertiesBeanPropertyChangedListener listener =
     *       new EventPublishingConfigurationPropertiesBeanPropertyChangedListener();
     *   listener.setApplicationContext(applicationContext);
     * }</pre>
     *
     * @param context the application context, must be a {@link ConfigurableApplicationContext}
     * @throws BeansException if the context is not a {@link ConfigurableApplicationContext}
     */
    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        Class<ConfigurableApplicationContext> expectedType = CONFIGURABLE_APPLICATION_CONTEXT_CLASS;
        isInstanceOf(expectedType, context, "The 'context' argument is not an instance of " + expectedType.getName());
        this.context = expectedType.cast(context);
    }

    /**
     * Called after all singleton beans have been instantiated, marking that initial binding
     * is complete. Subsequent binding operations will detect and publish property changes.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   EventPublishingConfigurationPropertiesBeanPropertyChangedListener listener = ...;
     *   // Called automatically by the Spring container
     *   listener.afterSingletonsInstantiated();
     *   assertTrue(listener.isBound());
     * }</pre>
     */
    @Override
    public void afterSingletonsInstantiated() {
        bound = true;
    }

    /**
     * Returns whether the initial binding of all singleton beans has been completed.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   EventPublishingConfigurationPropertiesBeanPropertyChangedListener listener = ...;
     *   if (listener.isBound()) {
     *       // Property changes will now be published as events
     *   }
     * }</pre>
     *
     * @return {@code true} if initial binding is complete, {@code false} otherwise
     */
    public boolean isBound() {
        return bound;
    }
}