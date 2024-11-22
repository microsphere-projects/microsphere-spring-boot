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
package io.microsphere.spring.boot.configuration.metadata;

import io.microsphere.spring.config.metadata.ConfigurationPropertyDescriptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertyResolver;
import org.springframework.core.env.PropertySource;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.EnumSet;
import java.util.Set;

/**
 * The context descriptor class of the Spring Configuration Property
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see ConfigurationPropertyDescriptor
 * @since 1.0.0
 */
public class ContextConfigurationPropertyDescriptor extends ConfigurationPropertyDescriptor {

    private Set<Usage> usages;

    public Set<Usage> getUsages() {
        return usages;
    }

    public void setUsages(Set<Usage> usages) {
        this.usages = usages;
    }

    /**
     * The configuration property usage
     *
     * @see ConfigurationProperties
     * @see Value
     * @see PropertyResolver#resolvePlaceholders(String)
     * @see PropertyResolver#getProperty(String)
     */
    public enum Usage {

        /**
         * Indicates the configuration property was used for {@link ConfigurationProperties @ConfigurationProperties}
         * Bean.
         */
        CONFIGURATION_PROPERTIES_BEAN,

        /**
         * Indicates the configuration property was used to inject the Beans' {@link Member member}({@link Field field},
         * {@link Method method} and {@link Constructor constructor}) that was annotated {@link Value @Value}.
         */
        VALUE_MEMBER,

        /**
         * Indicates the configuration property was used to {@link PropertyResolver#resolvePlaceholders(String) resolve the placeholders}
         */
        PLACEHOLDER,

        /**
         * Indicates the configuration property was used to {@link PropertyResolver#getProperty(String, Class) get the property}
         */
        DEFAULT,
    }
}
