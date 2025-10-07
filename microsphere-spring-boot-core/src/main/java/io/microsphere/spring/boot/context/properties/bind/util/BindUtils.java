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

    public static boolean isConfigurationPropertiesBean(Bindable<?> target, BindContext context) {
        return target != null && target.getAnnotation(ConfigurationProperties.class) != null && isConfigurationPropertiesBean(context);
    }

    public static boolean isConfigurationPropertiesBean(BindContext context) {
        return context != null && context.getDepth() == 0;
    }

    public static boolean isBoundProperty(BindContext context) {
        return context != null && context.getDepth() > 0;
    }

    public static <T> T bind(Environment environment, String propertyNamePrefix, Class<T> targetType, BindListener... bindListeners) {
        Binder binder = get(environment);
        return bind(binder, propertyNamePrefix, targetType, bindListeners);
    }

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