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

import io.microsphere.annotation.Nullable;
import io.microsphere.logging.Logger;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConfigurationPropertiesBindingPostProcessor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.context.properties.bind.BindContext;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.source.ConfigurationProperty;
import org.springframework.boot.context.properties.source.ConfigurationPropertyName;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.Map;
import java.util.function.Supplier;

import static io.microsphere.logging.LoggerFactory.getLogger;
import static io.microsphere.spring.boot.context.properties.bind.ConfigurationPropertiesBeanContext.buildConfigurationPropertiesBeanContexts;
import static io.microsphere.spring.boot.context.properties.bind.util.BindUtils.isBoundProperty;
import static io.microsphere.spring.boot.context.properties.bind.util.BindUtils.isConfigurationPropertiesBean;
import static io.microsphere.spring.boot.context.properties.source.util.ConfigurationPropertyUtils.getPrefix;
import static io.microsphere.spring.context.ApplicationContextUtils.asConfigurableApplicationContext;

/**
 * A {@link BindListener} implementation of {@link ConfigurationProperties @ConfigurationProperties} Bean to publish
 * the {@link ConfigurationPropertiesBeanPropertyChangedEvent}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see ConfigurationProperties
 * @see EnableConfigurationProperties
 * @see ConfigurationPropertiesBindingPostProcessor
 * @see ConfigurationPropertiesBeanContext
 * @see ConfigurationPropertiesBeanPropertyChangedEvent
 * @since 1.0.0
 */
public class EventPublishingConfigurationPropertiesBeanPropertyChangedListener implements BindListener, ApplicationContextAware, InitializingBean, SmartInitializingSingleton {

    private static final Logger logger = getLogger(EventPublishingConfigurationPropertiesBeanPropertyChangedListener.class);

    private Map<String, ConfigurationPropertiesBeanContext> beanContexts;

    private ConfigurableApplicationContext context;

    private boolean bound = false;

    /**
     * Handles the start of a binding operation. During initial binding, initializes the
     * {@link ConfigurationPropertiesBeanContext} for the target bean.
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

    @Override
    public void onFinish(ConfigurationPropertyName name, Bindable<?> target, BindContext context, Object result) {
        if (isBound()) {
            setConfigurationPropertiesBeanProperty(name, target, context, result);
        }
    }

    void initConfigurationPropertiesBeanContext(ConfigurationPropertyName name, Bindable<?> target, BindContext context) {
        ConfigurationPropertiesBeanContext configurationPropertiesBeanContext = getConfigurationPropertiesBeanContext(name, target, context);
        if (configurationPropertiesBeanContext == null) {
            return;
        }
        if (isConfigurationPropertiesBean(context)) {
            Supplier<?> value = target.getValue();
            Object bean = value == null ? null : value.get();
            if (bean == null) {
                if (logger.isWarnEnabled()) {
                    logger.warn("The ConfigurationPropertiesBeanContext is not initialized caused by the bean being null[name : '{}' , target : {} , depth : {}]",
                            name, target, context.getDepth());
                }
            } else {
                configurationPropertiesBeanContext.setBean(bean);
                if (logger.isTraceEnabled()) {
                    logger.trace("The ConfigurationPropertiesBean binding is finished[name : '{}' , target : {} , depth : {} , bean : '{}']",
                            name, target, context.getDepth(), bean);
                }
            }
        } else {
            configurationPropertiesBeanContext.initProperty(name, target);
        }
    }

    @Nullable
    private ConfigurationPropertiesBeanContext getConfigurationPropertiesBeanContext(ConfigurationPropertyName name,
                                                                                     Bindable<?> target, BindContext context) {
        String prefix = getPrefix(name, context);
        ConfigurationPropertiesBeanContext configurationPropertiesBeanContext = this.beanContexts.get(prefix);
        if (configurationPropertiesBeanContext == null) {
            if (logger.isWarnEnabled()) {
                logger.warn("No ConfigurationPropertiesBeanContext was found[name : '{}' , target : {} , depth : {}]",
                        name, target, context.getDepth());
            }
        }
        return configurationPropertiesBeanContext;
    }

    /**
     * Sets a property on the {@link ConfigurationPropertiesBeanContext} when a bound property
     * change is detected. Publishes an event if the value has changed.
     *
     * @param name    the configuration property name
     * @param target  the bindable target
     * @param context the bind context
     * @param result  the new property value
     */
    void setConfigurationPropertiesBeanProperty(ConfigurationPropertyName name, Bindable<?> target, BindContext context, Object result) {
        if (isBoundProperty(context)) {
            ConfigurationProperty property = context.getConfigurationProperty();
            ConfigurationPropertiesBeanContext configurationPropertiesBeanContext = getConfigurationPropertiesBeanContext(name, target, context);
            if (configurationPropertiesBeanContext == null) {
                return;
            }
            configurationPropertiesBeanContext.setPropertyValue(property, result);
            if (logger.isTraceEnabled()) {
                logger.trace("binding Bean property is finished , configuration property : '{}' , type : '{}' , depth : {} , result : '{}'",
                        property, target.getType(), context.getDepth(), result);
            }
        }
    }

    /**
     * Sets the {@link ApplicationContext}, which must be a {@link ConfigurableApplicationContext},
     * for publishing property change events.
     *
     * @param context the application context, must be a {@link ConfigurableApplicationContext}
     * @throws BeansException if the context is not a {@link ConfigurableApplicationContext}
     */
    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        this.context = asConfigurableApplicationContext(context);
    }

    @Override
    public void afterPropertiesSet() {
        this.beanContexts = buildConfigurationPropertiesBeanContexts(this.context);
    }

    /**
     * Called after all singleton beans have been instantiated, marking that initial binding
     * is complete. Subsequent binding operations will detect and publish property changes.
     */
    @Override
    public void afterSingletonsInstantiated() {
        initializePropertyValues();
        bound = true;
    }

    /**
     * Returns whether the initial binding of all singleton beans has been completed.
     *
     * @return {@code true} if initial binding is complete, {@code false} otherwise
     */
    public boolean isBound() {
        return bound;
    }

    private void initializePropertyValues() {
        for (ConfigurationPropertiesBeanContext beanContext : beanContexts.values()) {
            beanContext.initializePropertyValues();
        }
    }
}
