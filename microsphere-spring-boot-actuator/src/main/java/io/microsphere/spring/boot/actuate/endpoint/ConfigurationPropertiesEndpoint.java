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

import io.microsphere.spring.boot.env.config.metadata.ConfigurationMetadataRepository;
import io.microsphere.spring.config.ConfigurationProperty;
import org.springframework.boot.actuate.endpoint.OperationResponseBody;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.configurationprocessor.metadata.ConfigurationMetadata;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.EventListener;

import java.util.List;

/**
 * {@link Endpoint @Endpoint} to expose the configuration properties.
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see ConfigurationMetadata
 * @see ConfigurationProperties
 * @since 1.0.0
 */
@Endpoint(id = "configProperties")
public class ConfigurationPropertiesEndpoint {

    private final ConfigurationMetadataRepository configurationMetadataRepository;

    public ConfigurationPropertiesEndpoint(ConfigurationMetadataRepository configurationMetadataRepository) {
        this.configurationMetadataRepository = configurationMetadataRepository;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReadyEvent(ApplicationReadyEvent event) {
        ConfigurableApplicationContext context = event.getApplicationContext();
    }

    @ReadOperation
    public ConfigurationPropertiesDescriptor getConfigurationProperties() {
        ConfigurationPropertiesDescriptor configurationProperties = new ConfigurationPropertiesDescriptor();
        return configurationProperties;
    }


    public static class ConfigurationPropertiesDescriptor implements OperationResponseBody {

        private List<ConfigurationProperty> configurationProperties;

        public List<ConfigurationProperty> getConfigurationProperties() {
            return configurationProperties;
        }

        public void setConfigurationProperties(List<ConfigurationProperty> configurationProperties) {
            this.configurationProperties = configurationProperties;
        }
    }


}
