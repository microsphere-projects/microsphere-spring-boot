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
package io.microsphere.spring.boot.actuate.endpoint;

import io.microsphere.spring.boot.configuration.metadata.ConfigurationMetadataReader;
import org.springframework.boot.actuate.endpoint.OperationResponseBody;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.configurationprocessor.metadata.ConfigurationMetadata;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * {@link Endpoint @Endpoint} to expose the configuration properties
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see ConfigurationMetadata
 * @see ConfigurationProperties
 * @since 1.0.0
 */
@Endpoint(id = "configProperties")
public class ConfigurationPropertiesEndpoint {

    private final ConfigurationMetadataReader configurationMetadataReader;

    public ConfigurationPropertiesEndpoint(ClassLoader classLoader) {
        this.configurationMetadataReader = new ConfigurationMetadataReader(classLoader);
    }

    @ReadOperation
    public ConfigurationPropertiesDescriptor getConfigurationProperties() {
        ConfigurationPropertiesDescriptor configurationProperties = new ConfigurationPropertiesDescriptor();
        return configurationProperties;
    }

    public static class ConfigurationPropertiesDescriptor implements OperationResponseBody {

        private List<ConfigurationPropertyDescriptor> configurationProperties;

        public List<ConfigurationPropertyDescriptor> getConfigurationProperties() {
            return configurationProperties;
        }

        public void setConfigurationProperties(List<ConfigurationPropertyDescriptor> configurationProperties) {
            this.configurationProperties = configurationProperties;
        }
    }

    public static class ConfigurationPropertyDescriptor {

        private String name;

        private String type;

        private Object value;

        private Object defaultValue;


    }
}
