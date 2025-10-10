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

import io.microsphere.annotation.Nonnull;
import io.microsphere.beans.ConfigurationProperty;
import io.microsphere.beans.ConfigurationProperty.Metadata;
import io.microsphere.spring.boot.env.config.metadata.ConfigurationMetadataRepository;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.configurationprocessor.metadata.ConfigurationMetadata;
import org.springframework.boot.configurationprocessor.metadata.ItemMetadata;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import static io.microsphere.annotation.ConfigurationProperty.APPLICATION_SOURCE;
import static io.microsphere.collection.CollectionUtils.isNotEmpty;
import static io.microsphere.collection.ListUtils.newArrayList;
import static io.microsphere.metadata.ConfigurationPropertyLoader.loadAll;

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

    @ReadOperation
    public ConfigurationPropertiesDescriptor getConfigurationProperties() {
        ConfigurationPropertiesDescriptor descriptor = new ConfigurationPropertiesDescriptor();
        List<ConfigurationProperty> configurationProperties = loadFromServiceLoaders();
        List<ConfigurationProperty> adaptedConfigurationProperties = adaptFromConfigurationMetadataRepository();
        descriptor.addConfigurationProperties(configurationProperties)
                .addConfigurationProperties(adaptedConfigurationProperties);
        return descriptor;
    }

    private List<ConfigurationProperty> loadFromServiceLoaders() {
        return loadAll();
    }

    private List<ConfigurationProperty> adaptFromConfigurationMetadataRepository() {
        Collection<ItemMetadata> properties = configurationMetadataRepository.getProperties();
        List<ConfigurationProperty> configurationProperties = newArrayList(properties.size());
        for (ItemMetadata property : properties) {
            ConfigurationProperty configurationProperty = adaptConfigurationProperty(property);
            configurationProperties.add(configurationProperty);
        }
        return configurationProperties;
    }

    private ConfigurationProperty adaptConfigurationProperty(ItemMetadata property) {
        String name = property.getName();
        String type = property.getType();
        String description = property.getDescription();
        String sourceType = property.getSourceType();
        String sourceMethod = property.getSourceMethod();
        Object defaultValue = property.getDefaultValue();

        ConfigurationProperty configurationProperty = new ConfigurationProperty(name);
        configurationProperty.setType(type == null ? String.class.getName() : type);
        configurationProperty.setDescription(description);
        configurationProperty.setDefaultValue(defaultValue);

        Metadata metadata = configurationProperty.getMetadata();
        metadata.setDeclaredClass(sourceType);
        metadata.setDeclaredField(sourceMethod);
        metadata.getSources().add(APPLICATION_SOURCE);

        return configurationProperty;
    }


    public static class ConfigurationPropertiesDescriptor {

        private final List<ConfigurationProperty> configurationProperties = new LinkedList<>();

        @Nonnull
        public List<ConfigurationProperty> getConfigurationProperties() {
            return configurationProperties;
        }

        public ConfigurationPropertiesDescriptor addConfigurationProperties(List<ConfigurationProperty> configurationProperties) {
            if (isNotEmpty(configurationProperties)) {
                this.configurationProperties.addAll(configurationProperties);
            }
            return this;
        }
    }
}