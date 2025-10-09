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

import static io.microsphere.spring.boot.context.properties.bind.util.BindHandlerUtils.createBindHandler;
import static java.util.Arrays.asList;
import static org.springframework.boot.context.properties.source.ConfigurationPropertySources.from;

/**
 * {@link Bindable} {@link ConfigurationBeanBinder} based on Spring Boot 2 {@link Binder}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see DefaultConfigurationBeanBinder
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

        Iterable<PropertySource<?>> propertySources = asList(new MapPropertySource("internal", configurationProperties));

        // Converts ConfigurationPropertySources
        Iterable<ConfigurationPropertySource> configurationPropertySources = from(propertySources);

        // Wrap Bindable from configuration bean
        Bindable bindable = Bindable.ofInstance(configurationBean);

        Binder binder = new Binder(configurationPropertySources, new PropertySourcesPlaceholdersResolver(propertySources), conversionService);

        // Get BindHandler
        BindHandler bindHandler = createBindHandler(ignoreUnknownFields, ignoreInvalidFields);

        // Bind
        binder.bind("", bindable, bindHandler);
    }
}
