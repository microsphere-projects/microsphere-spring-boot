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
     * Initializes this instance with the given {@link ConfigurableApplicationContext},
     * creating a {@link PropertySourceLoaders} and registering this bean into the
     * application context's {@link BeanDefinitionRegistry}.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   OriginTrackedConfigurationPropertyInitializer initializer =
     *       new OriginTrackedConfigurationPropertyInitializer();
     *   initializer.initialize(applicationContext);
     * }</pre>
     *
     * @param applicationContext the application context to initialize with
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
     * Called when the bean factory configuration is frozen. Replaces non-origin-tracked
     * {@link PropertySource property sources} in the environment with origin-tracked equivalents.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   // Typically invoked by the Spring container as a SmartInitializingSingleton callback:
     *   initializer.onBeanFactoryConfigurationFrozen(beanFactory);
     * }</pre>
     *
     * @param beanFactory the bean factory whose configuration has been frozen
     */
    @Override
    public void onBeanFactoryConfigurationFrozen(ConfigurableListableBeanFactory beanFactory) {
        ConfigurableEnvironment environment = applicationContext.getEnvironment();
        MutablePropertySources propertySources = environment.getPropertySources();
        initializePropertySources(propertySources);
    }

    /**
     * Iterates over the given {@link MutablePropertySources} and replaces each candidate
     * {@link PropertySource} (enumerable, non-origin-tracked) with an origin-tracked version.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
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
     * Creates an origin-tracked {@link PropertySource} from the given source. If the source is a
     * {@link ResourcePropertySource}, it is reloaded with origin tracking; otherwise, each property
     * value is wrapped with an {@link OriginTrackedValue}.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   PropertySource<?> original = new MapPropertySource("test", map);
     *   PropertySource<?> tracked = initializer.createOriginTrackedPropertySource(original);
     * }</pre>
     *
     * @param propertySource the original {@link PropertySource} to convert
     * @return an origin-tracked {@link PropertySource}
     * @throws IOException if an I/O error occurs during reload
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