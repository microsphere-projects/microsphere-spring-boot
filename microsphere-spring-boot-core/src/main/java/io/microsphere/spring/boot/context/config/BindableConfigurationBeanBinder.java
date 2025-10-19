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
package io.microsphere.spring.boot.context.config;

import io.microsphere.spring.context.config.ConfigurationBeanBinder;
import io.microsphere.spring.context.config.DefaultConfigurationBeanBinder;
import org.springframework.boot.context.properties.bind.BindHandler;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.bind.PropertySourcesPlaceholdersResolver;
import org.springframework.boot.context.properties.source.ConfigurationPropertySource;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.PropertySource;

import java.util.Map;

import static io.microsphere.collection.Lists.ofList;
import static io.microsphere.spring.boot.context.properties.bind.util.BindHandlerUtils.createBindHandler;
import static java.util.Arrays.asList;
import static org.springframework.boot.context.properties.bind.Bindable.ofInstance;
import static org.springframework.boot.context.properties.source.ConfigurationPropertySources.from;

/**
 * A {@link ConfigurationBeanBinder} implementation based on Spring Boot 2's {@link Binder},
 * which binds configuration properties to a given bean using {@link Bindable}.
 *
 * <h3>Example Usage</h3>
 * <pre>{@code
 *     Map<String, Object> properties = new HashMap<>();
 *     properties.put("app.name", "demo");
 *     MyConfigBean bean = new MyConfigBean();
 *     BindableConfigurationBeanBinder binder = new BindableConfigurationBeanBinder();
 *     binder.bind(properties, true, true, bean);
 *     // bean.getAppName() == "demo"
 * }</pre>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see DefaultConfigurationBeanBinder
 * @see ConfigurationBeanBinder
 * @since 1.0.0
 */
public class BindableConfigurationBeanBinder implements ConfigurationBeanBinder {

    private ConversionService conversionService;

    @Override
    public void setConversionService(ConversionService conversionService) {
        this.conversionService = conversionService;
    }

    @Override
    public void bind(Map<String, Object> configurationProperties, boolean ignoreUnknownFields,
                     boolean ignoreInvalidFields, Object configurationBean) {

        Iterable<PropertySource<?>> propertySources = ofList(new MapPropertySource("internal", configurationProperties));

        // Converts ConfigurationPropertySources
        Iterable<ConfigurationPropertySource> configurationPropertySources = from(propertySources);

        // Wrap Bindable from configuration bean
        Bindable bindable = ofInstance(configurationBean);

        Binder binder = new Binder(configurationPropertySources, new PropertySourcesPlaceholdersResolver(propertySources), conversionService);

        // Get BindHandler
        BindHandler bindHandler = createBindHandler(ignoreUnknownFields, ignoreInvalidFields);

        // Bind
        binder.bind("", bindable, bindHandler);
    }
}