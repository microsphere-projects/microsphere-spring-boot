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
package io.microsphere.spring.boot.context.properties.bind.util;

import io.microsphere.spring.boot.context.properties.bind.BindListener;
import io.microsphere.spring.boot.context.properties.bind.ListenableBindHandlerAdapter;
import io.microsphere.util.Utils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.BindContext;
import org.springframework.boot.context.properties.bind.BindResult;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.ConfigurationPropertySource;
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource;
import org.springframework.core.env.Environment;

import java.util.Map;

import static io.microsphere.collection.Lists.ofList;
import static io.microsphere.spring.boot.context.properties.util.ConfigurationPropertiesUtils.CONFIGURATION_PROPERTIES_CLASS;
import static org.springframework.boot.context.properties.bind.Bindable.of;
import static org.springframework.boot.context.properties.bind.Binder.get;

/**
 * The utilities class for binding
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see Binder
 * @since 1.0.0
 */
public abstract class BindUtils implements Utils {

    /**
     * Checks if the given {@link Bindable} target and {@link BindContext} represent a bean
     * annotated with {@link ConfigurationProperties} at the root binding level.
     *
     * <h3>Example Usage</h3>
     * <h4>Example 1: Should return true</h4>
     * <pre>{@code
     * @ConfigurationProperties(prefix = "app")
     * public class AppProperties {}
     * boolean result = isConfigurationPropertiesBean(
     *     Bindable.of(AppProperties.class).withAnnotation(ConfigurationProperties.class),
     *     mockBindContextWithDepth(0)
     * );
     * }</pre>
     *
     * <h4>Example 2: Should return false (not a ConfigurationProperties bean)</h4>
     * <pre>{@code
     * boolean result2 = isConfigurationPropertiesBean(
     *     Bindable.of(String.class),
     *     mockBindContextWithDepth(0)
     * );
     * }</pre>
     *
     * <h4>Example 3: Should return false (not at root level)</h4>
     * <pre>{@code
     * boolean result3 = isConfigurationPropertiesBean(
     *     Bindable.of(AppProperties.class).withAnnotation(ConfigurationProperties.class),
     *     mockBindContextWithDepth(1)
     * );
     * }</pre>
     *
     * @param target  the bindable target to check, may be {@code null}
     * @param context the bind context to check, may be {@code null}
     * @return {@code true} if the target has {@link ConfigurationProperties} annotation and context is at root level,
     * {@code false} otherwise
     * @see #isConfigurationPropertiesBean(BindContext)
     * @see ConfigurationProperties
     * @see BindContext#getDepth()
     */
    public static boolean isConfigurationPropertiesBean(Bindable<?> target, BindContext context) {
        return target != null && target.getAnnotation(CONFIGURATION_PROPERTIES_CLASS) != null && isConfigurationPropertiesBean(context);
    }

    /**
     * Checks if the given {@link BindContext} represents a bean annotated with {@link ConfigurationProperties}
     * at the root binding level (depth = 0).
     *
     * <h3>Example Usage</h3>
     * <h4>Example 1: Should return true</h4>
     * <pre>{@code
     * boolean result = isConfigurationPropertiesBean(mockBindContextWithDepth(0));
     * }</pre>
     *
     * <h4>Example 2: Should return false (not at root level)</h4>
     * <pre>{@code
     * boolean result = isConfigurationPropertiesBean(mockBindContextWithDepth(1));
     * }</pre>
     *
     * <h4>Example 3: Should return false (null context)</h4>
     * <pre>{@code
     * boolean result = isConfigurationPropertiesBean(null);
     * }</pre>
     *
     * @param context the bind context to check, may be {@code null}
     * @return {@code true} if the context is not null and at root level (depth = 0),
     * {@code false} otherwise
     * @see BindContext#getDepth()
     */
    public static boolean isConfigurationPropertiesBean(BindContext context) {
        return context != null && context.getDepth() == 0;
    }

    /**
     * Checks if the given {@link BindContext} represents a bound property.
     *
     * <h3>Example Usage</h3>
     * <h4>Example 1: Should return true</h4>
     * <pre>{@code
     * boolean result = isBoundProperty(mockBindContextWithDepth(1));
     * }</pre>
     *
     * <h4>Example 2: Should return false (at root level)</h4>
     * <pre>{@code
     * boolean result = isBoundProperty(mockBindContextWithDepth(0));
     * }</pre>
     *
     * <h4>Example 3: Should return false (null context)</h4>
     * <pre>{@code
     * boolean result = isBoundProperty(null);
     * }</pre>
     *
     * @param context the bind context to check, may be {@code null}
     * @return {@code true} if the context is not null and depth is greater than 0,
     * {@code false} otherwise
     * @see BindContext#getDepth()
     */
    public static boolean isBoundProperty(BindContext context) {
        return context != null && context.getDepth() > 0;
    }

    /**
     * Bind configuration properties to a target type using the provided environment and property name prefix.
     *
     * <h3>Example Usage</h3>
     * <h4>Example 1: Basic usage with Environment</h4>
     * <pre>{@code
     * @ConfigurationProperties(prefix = "app")
     * public class AppProperties {
     *     private String name;
     *     // getters and setters
     * }
     *
     * AppProperties properties = BindUtils.bind(environment, "app", AppProperties.class);
     * }</pre>
     *
     * <h4>Example 2: With bind listeners</h4>
     * <pre>{@code
     * AppProperties properties = BindUtils.bind(environment, "app", AppProperties.class,
     *     (bindable, context, value) -> {
     *         // Handle bind event
     *     });
     * }</pre>
     *
     * @param environment        the environment to bind from, must not be {@code null}
     * @param propertyNamePrefix the property name prefix to use, may be {@code null}
     * @param targetType         the target type to bind to, must not be {@code null}
     * @param bindListeners      optional bind listeners to register for bind events
     * @param <T>                the target type
     * @return the bound instance or {@code null} if binding failed
     * @see Environment
     * @see BindListener
     * @see #bind(Binder, String, Class, BindListener...)
     */
    public static <T> T bind(Environment environment, String propertyNamePrefix, Class<T> targetType, BindListener... bindListeners) {
        Binder binder = get(environment);
        return bind(binder, propertyNamePrefix, targetType, bindListeners);
    }

    /**
     * Bind configuration properties to a target type using the provided properties map and property name prefix.
     *
     * <h3>Example Usage</h3>
     * <h4>Example 1: Basic usage with Map</h4>
     * <pre>{@code
     * @ConfigurationProperties(prefix = "app")
     * public class AppProperties {
     *     private String name;
     *     // getters and setters
     * }
     *
     * Map<String, Object> properties = new HashMap<>();
     * properties.put("app.name", "MyApp");
     * AppProperties appProps = BindUtils.bind(properties, "app", AppProperties.class);
     * }</pre>
     *
     * <h4>Example 2: With bind listeners</h4>
     * <pre>{@code
     * Map<String, Object> properties = new HashMap<>();
     * properties.put("app.name", "MyApp");
     * AppProperties appProps = BindUtils.bind(properties, "app", AppProperties.class,
     *     (bindable, context, value) -> {
     *         // Handle bind event
     *     });
     * }</pre>
     *
     * @param properties         the properties map to bind from, must not be {@code null}
     * @param propertyNamePrefix the property name prefix to use, may be {@code null}
     * @param targetType         the target type to bind to, must not be {@code null}
     * @param bindListeners      optional bind listeners to register for bind events
     * @param <T>                the target type
     * @return the bound instance or {@code null} if binding failed
     * @see Map
     * @see BindListener
     * @see #bind(Binder, String, Class, BindListener...)
     */
    public static <T> T bind(Map<?, ?> properties, String propertyNamePrefix, Class<T> targetType, BindListener... bindListeners) {
        ConfigurationPropertySource propertySource = new MapConfigurationPropertySource(properties);
        Binder binder = new Binder(propertySource);
        return bind(binder, propertyNamePrefix, targetType, bindListeners);
    }

    protected static <T> T bind(Binder binder, String name, Class<T> targetType, BindListener... bindListeners) {
        ListenableBindHandlerAdapter bindHandlerAdapter = new ListenableBindHandlerAdapter(ofList(bindListeners));
        Bindable<T> bindable = of(targetType);
        BindResult<T> result = binder.bind(name, bindable, bindHandlerAdapter);
        return result.orElse(null);
    }

    private BindUtils() {
    }
}