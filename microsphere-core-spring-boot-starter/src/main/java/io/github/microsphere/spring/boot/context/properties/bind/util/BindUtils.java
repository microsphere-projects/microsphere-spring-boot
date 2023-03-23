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
package io.github.microsphere.spring.boot.context.properties.bind.util;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.BindContext;
import org.springframework.boot.context.properties.bind.BindResult;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.ConfigurationPropertySource;
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource;
import org.springframework.core.env.ConfigurableEnvironment;

import java.util.Map;

/**
 * The utilities class for binding
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see Binder
 * @since 1.0.0
 */
public abstract class BindUtils {

    private BindUtils() throws InstantiationException {
        throw new InstantiationException();
    }

    public static boolean isConfigurationPropertiesBean(Bindable<?> target, BindContext context) {
        return target.getAnnotation(ConfigurationProperties.class) != null && context.getDepth() == 0;
    }

    public static boolean isConfigurationPropertiesBean(BindContext context) {
        return context != null && context.getDepth() == 0;
    }

    public static boolean isBoundProperty(BindContext context) {
        return context != null && context.getDepth() > 0;
    }

    public static <T> T bind(ConfigurableEnvironment environment, String propertyNamePrefix, Class<T> targetType) {
        Binder binder = Binder.get(environment);
        return bind(binder, propertyNamePrefix, targetType);
    }

    public static <T> T bind(Map<?, ?> properties, String propertyNamePrefix, Class<T> targetType) {
        ConfigurationPropertySource propertySource = new MapConfigurationPropertySource(properties);
        Binder binder = new Binder(propertySource);
        return bind(binder, propertyNamePrefix, targetType);
    }

    protected static <T> T bind(Binder binder, String name, Class<T> targetType) {
        BindResult<T> result = binder.bind(name, targetType);
        return result.orElse(null);
    }
}