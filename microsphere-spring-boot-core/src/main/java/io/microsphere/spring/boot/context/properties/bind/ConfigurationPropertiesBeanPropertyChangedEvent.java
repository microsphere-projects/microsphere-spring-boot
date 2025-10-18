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

import io.microsphere.spring.context.event.BeanPropertyChangedEvent;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.source.ConfigurationProperty;

/**
 * Event raised when the property of bean annotated {@link ConfigurationProperties @ConfigurationProperties} was changed.
 *
 * @param <T> the type of the bean annotated with {@link ConfigurationProperties}
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see ConfigurationProperties
 * @see BeanPropertyChangedEvent
 * @see EventPublishingConfigurationPropertiesBeanPropertyChangedListener
 * @since 1.0.0
 */
public class ConfigurationPropertiesBeanPropertyChangedEvent<T> extends BeanPropertyChangedEvent {

    private final ConfigurationProperty configurationProperty;

    public ConfigurationPropertiesBeanPropertyChangedEvent(Object bean, String propertyName, Object oldValue, Object newValue, ConfigurationProperty configurationProperty) {
        super(bean, propertyName, oldValue, newValue);
        this.configurationProperty = configurationProperty;
    }

    public ConfigurationProperty getConfigurationProperty() {
        return configurationProperty;
    }
}
