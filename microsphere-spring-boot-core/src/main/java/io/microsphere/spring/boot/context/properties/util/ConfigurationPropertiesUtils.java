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
package io.microsphere.spring.boot.context.properties.util;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.core.ResolvableType;

import static io.microsphere.util.AnnotationUtils.findAnnotation;

/**
 * The utilities class of {@link ConfigurationProperties}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see ConfigurationProperties
 * @since 1.0.0
 */
public abstract class ConfigurationPropertiesUtils {

    /**
     * The {@link Class} of {@link ConfigurationProperties}
     */
    public static final Class<ConfigurationProperties> CONFIGURATION_PROPERTIES_CLASS = ConfigurationProperties.class;

    /**
     * Find an annotation of {@link ConfigurationProperties} from the specified {@link Bindable}.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * @ConfigurationProperties(prefix = "app.config")
     * public class AppConfig {
     *     private String name;
     *     // getters and setters
     * }
     *
     * Bindable<AppConfig> bindable = Bindable.of(AppConfig.class);
     * ConfigurationProperties props = ConfigurationPropertiesUtils.findConfigurationProperties(bindable);
     * // props will contain the annotation with prefix "app.config"
     * }</pre>
     *
     * <h4>Another example with nested properties</h4>
     * <pre>{@code
     * public class ParentConfig {
     *     @ConfigurationProperties(prefix = "parent.child")
     *     private ChildConfig child;
     *     // getters and setters
     * }
     *
     * Bindable<ChildConfig> bindable = Bindable.of(ChildConfig.class);
     * ConfigurationProperties props = ConfigurationPropertiesUtils.findConfigurationProperties(bindable);
     * // props will contain the annotation with prefix "parent.child"
     * }</pre>
     *
     * @param bindable {@link Bindable}
     * @return an annotation of {@link ConfigurationProperties} if present
     * @throws NullPointerException if bindable is null
     * @since 1.0.0
     */
    public static ConfigurationProperties findConfigurationProperties(Bindable bindable) {
        // Find an annotation of @ConfigurationProperties from annotations
        ConfigurationProperties configurationProperties = (ConfigurationProperties) bindable.getAnnotation(CONFIGURATION_PROPERTIES_CLASS);
        if (configurationProperties == null) {
            // If not found, try to find an annotation of @ConfigurationProperties from the bindable's type
            ResolvableType type = bindable.getType();
            Class<?> bindableType = type.resolve();
            configurationProperties = findAnnotation(bindableType, CONFIGURATION_PROPERTIES_CLASS);
        }
        return configurationProperties;
    }

    private ConfigurationPropertiesUtils() {
    }
}