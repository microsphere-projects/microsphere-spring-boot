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
package io.microsphere.spring.boot.env.config;

import io.microsphere.logging.Logger;
import io.microsphere.spring.boot.env.PropertySourceLoaders;
import io.microsphere.spring.context.event.BeanFactoryListenerAdapter;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.env.OriginTrackedMapPropertySource;
import org.springframework.boot.origin.Origin;
import org.springframework.boot.origin.OriginLookup;
import org.springframework.boot.origin.OriginTrackedValue;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.support.ResourcePropertySource;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import static io.microsphere.logging.LoggerFactory.getLogger;
import static io.microsphere.spring.beans.factory.support.BeanRegistrar.registerBean;
import static org.springframework.boot.origin.OriginTrackedValue.of;

/**
 * {@link ApplicationContextInitializer} class supports origin tracked configuration property.
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see ConfigurableEnvironment
 * @since ApplicationContextInitializer
 */
public class OriginTrackedConfigurationPropertyInitializer implements BeanFactoryListenerAdapter, ApplicationContextInitializer<ConfigurableApplicationContext> {

    public static final String BEAN_NAME = "originTrackedConfigurationPropertyInitializer";

    private static final Logger logger = getLogger(OriginTrackedConfigurationPropertyInitializer.class);

    private ConfigurableApplicationContext applicationContext;

    private PropertySourceLoaders propertySourceLoaders;

    /**
     * Initializes the application context by storing references and registering this instance
     * as a bean in the bean factory. This override sets up the {@link PropertySourceLoaders}
     * and registers this initializer for later bean factory lifecycle callbacks.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   OriginTrackedConfigurationPropertyInitializer initializer =
     *       new OriginTrackedConfigurationPropertyInitializer();
     *   initializer.initialize(applicationContext);
     * }</pre>
     *
     * @param applicationContext the {@link ConfigurableApplicationContext} to initialize
     */
    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
        this.propertySourceLoaders = new PropertySourceLoaders(applicationContext.getClassLoader());
        ConfigurableListableBeanFactory beanFactory = applicationContext.getBeanFactory();
        BeanDefinitionRegistry registry = (BeanDefinitionRegistry) beanFactory;
        registerBean(registry, BEAN_NAME, this);
    }

    /**
     * Callback invoked when the bean factory configuration is frozen. This override
     * triggers the replacement of non-origin-tracked property sources with origin-tracked versions.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   // Typically invoked by the Spring framework lifecycle:
     *   OriginTrackedConfigurationPropertyInitializer initializer =
     *       new OriginTrackedConfigurationPropertyInitializer();
     *   initializer.onBeanFactoryConfigurationFrozen(beanFactory);
     * }</pre>
     *
     * @param beanFactory the {@link ConfigurableListableBeanFactory} whose configuration was frozen
     */
    @Override
    public void onBeanFactoryConfigurationFrozen(ConfigurableListableBeanFactory beanFactory) {
        ConfigurableEnvironment environment = applicationContext.getEnvironment();
        MutablePropertySources propertySources = environment.getPropertySources();
        initializePropertySources(propertySources);
    }

    /**
     * Iterates over the given {@link MutablePropertySources} and replaces eligible
     * property sources with origin-tracked versions.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   ConfigurableEnvironment environment = applicationContext.getEnvironment();
     *   MutablePropertySources propertySources = environment.getPropertySources();
     *   initializer.initializePropertySources(propertySources);
     * }</pre>
     *
     * @param propertySources the mutable property sources to process
     */
    void initializePropertySources(MutablePropertySources propertySources) {
        for (PropertySource propertySource : propertySources) {
            if (isPropertySourceCandidate(propertySource)) {
                String name = propertySource.getName();
                try {
                    PropertySource originTrackedPropertySource = createOriginTrackedPropertySource(propertySource);
                    propertySources.replace(name, originTrackedPropertySource);
                } catch (IOException e) {
                    logger.error("Failed to create the origin tracked PropertySource[name : '{}', class : '{}']",
                            name, propertySource.getClass().getName());
                }
            }
        }
    }

    private boolean isPropertySourceCandidate(PropertySource propertySource) {
        return (propertySource instanceof EnumerablePropertySource<?>) &&
                !(propertySource instanceof OriginLookup);
    }

    /**
     * Creates an origin-tracked {@link PropertySource} from the given property source.
     * If the source is a {@link ResourcePropertySource}, it is reloaded with origin tracking;
     * otherwise, its properties are wrapped with {@link OriginTrackedValue}.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   PropertySource<?> original = environment.getPropertySources().get("mySource");
     *   PropertySource<?> tracked = initializer.createOriginTrackedPropertySource(original);
     * }</pre>
     *
     * @param propertySource the {@link PropertySource} to convert
     * @return a new origin-tracked {@link PropertySource}
     * @throws IOException if reloading the resource fails
     */
    PropertySource createOriginTrackedPropertySource(PropertySource propertySource) throws IOException {
        if (propertySource instanceof ResourcePropertySource) {
            return propertySourceLoaders.reloadAsOriginTracked(propertySource);
        }

        EnumerablePropertySource enumerablePropertySource = (EnumerablePropertySource) propertySource;
        String[] propertyNames = enumerablePropertySource.getPropertyNames();
        int size = propertyNames.length;
        Map<String, Object> source = new LinkedHashMap<>(size);
        for (int i = 0; i < size; i++) {
            String propertyName = propertyNames[i];
            Object propertyValue = enumerablePropertySource.getProperty(propertyName);
            if (propertyValue != null && !(propertyValue instanceof OriginTrackedValue)) {
                // Skip if propertyValue is OriginTrackedValue
                Origin origin = resolveOrigin(propertySource);
                // propertyValue with origin
                propertyValue = of(propertyValue, origin);
            }
            source.put(propertyName, propertyValue);
        }
        return new OriginTrackedMapPropertySource(propertySource.getName(), source);
    }

    private Origin resolveOrigin(PropertySource propertySource) {
        // TODO more Origin implementations
        return new NamedOrigin(propertySource.getName());
    }

    static class NamedOrigin implements Origin {

        private final String name;

        NamedOrigin(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }
}