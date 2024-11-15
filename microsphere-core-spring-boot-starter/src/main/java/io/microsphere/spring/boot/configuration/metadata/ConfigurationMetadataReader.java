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

import io.microsphere.logging.Logger;
import io.microsphere.logging.LoggerFactory;
import org.springframework.boot.configurationprocessor.metadata.ConfigurationMetadata;
import org.springframework.boot.configurationprocessor.metadata.JsonMarshaller;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.lang.NonNull;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import static io.microsphere.text.FormatUtils.format;
import static io.microsphere.util.ClassLoaderUtils.getDefaultClassLoader;
import static io.microsphere.util.StringUtils.replace;
import static org.springframework.core.io.support.ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX;

/**
 * The Reader of {@link ConfigurationMetadata}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see ConfigurationMetadata
 * @since 1.0.0
 */
public class ConfigurationMetadataReader {

    private static final Logger logger = LoggerFactory.getLogger(ConfigurationMetadataReader.class);

    private static final String METADATA_RESOURCE_NAME = "spring-configuration-metadata.json";

    private static final String ADDITIONAL_METADATA_RESOURCE_NAME = "additional-spring-configuration-metadata.json";

    private static final String META_INFO_PATH = "/META-INF/";

    private static final String METADATA_RESOURCE_PATH = META_INFO_PATH + METADATA_RESOURCE_NAME;

    private static final String ADDITIONAL_METADATA_RESOURCE_PATH = META_INFO_PATH + ADDITIONAL_METADATA_RESOURCE_NAME;

    public static final String METADATA_RESOURCE_PATTERN_PATH = CLASSPATH_ALL_URL_PREFIX + METADATA_RESOURCE_PATH;

    public static final String ADDITIONAL_METADATA_RESOURCE_PATTERN_PATH = CLASSPATH_ALL_URL_PREFIX + ADDITIONAL_METADATA_RESOURCE_PATH;

    private final ResourcePatternResolver resourcePatternResolver;

    public ConfigurationMetadataReader() {
        this(getDefaultClassLoader());
    }

    public ConfigurationMetadataReader(ClassLoader classLoader) {
        this.resourcePatternResolver = new PathMatchingResourcePatternResolver(classLoader);
    }

    public ConfigurationMetadata read() {
        ConfigurationMetadata configurationMetadata = new ConfigurationMetadata();
        Resource[] metadataResources = getMetadataResources();
        int metadataResourcesSize = metadataResources.length;
        Set<Resource> processedAdditionalMetadataResources = new HashSet<>(metadataResourcesSize);
        for (int i = 0; i < metadataResourcesSize; i++) {
            Resource resource = metadataResources[i];
            processConfigurationMetadata(configurationMetadata, resource, processedAdditionalMetadataResources);
        }
        Resource[] additionalMetadataResources = getAdditionalMetadataResources();
        for (Resource additionalMetadataResource : additionalMetadataResources) {
            if (processedAdditionalMetadataResources.remove(additionalMetadataResource)) {
                continue;
            }
            processConfigurationMetadata(configurationMetadata, additionalMetadataResource);
        }
        return configurationMetadata;
    }

    private void processConfigurationMetadata(ConfigurationMetadata configurationMetadata, Resource metadataResource,
                                              Set<Resource> processedAdditionalMetadataResources) {
        processConfigurationMetadata(configurationMetadata, metadataResource);
        tryProcessAdditionalMetadataResource(configurationMetadata, metadataResource, processedAdditionalMetadataResources);
    }

    private void tryProcessAdditionalMetadataResource(ConfigurationMetadata configurationMetadata, Resource metadataResource,
                                                      Set<Resource> processedAdditionalMetadataResources) {
        try {
            URI uri = metadataResource.getURI();
            String metadataResourcePath = uri.toString();
            String additionalMetadataResourcePath = replace(metadataResourcePath, METADATA_RESOURCE_NAME, ADDITIONAL_METADATA_RESOURCE_NAME);
            Resource additionalMetadataResource = this.resourcePatternResolver.getResource(additionalMetadataResourcePath);
            if (additionalMetadataResource.exists()) {
                processConfigurationMetadata(configurationMetadata, additionalMetadataResource);
                processedAdditionalMetadataResources.add(additionalMetadataResource);
            } else {
                if (logger.isDebugEnabled()) {
                    logger.debug("The Additional Configuration Metadata resource[{}] can't be found", additionalMetadataResource);
                }
            }
        } catch (IOException e) {
            String message = format("The Additional Configuration Metadata resource[{}] can't be open", metadataResource);
            throw new RuntimeException(message, e);
        }
    }

    private void processConfigurationMetadata(ConfigurationMetadata configurationMetadata, Resource metadataResource) {
        ConfigurationMetadata metadata = loadConfigurationMetadata(metadataResource);
        configurationMetadata.merge(metadata);
    }

    private ConfigurationMetadata loadConfigurationMetadata(Resource metadataResource) {
        JsonMarshaller jsonMarshaller = new JsonMarshaller();
        final ConfigurationMetadata configurationMetadata;
        try (InputStream inputStream = metadataResource.getInputStream()) {
            configurationMetadata = jsonMarshaller.read(inputStream);
            if (logger.isDebugEnabled()) {
                logger.debug("Loaded the Configuration Metadata resource[{}] : {}", metadataResource, configurationMetadata);
            }
        } catch (Throwable e) {
            String message = format("The Configuration Metadata resource[{}] can't be loaded", metadataResource);
            throw new RuntimeException(message, e);
        }
        return configurationMetadata;
    }

    @NonNull
    private Resource[] getMetadataResources() {
        return getResources(METADATA_RESOURCE_PATTERN_PATH);
    }

    @NonNull
    private Resource[] getAdditionalMetadataResources() {
        return getResources(ADDITIONAL_METADATA_RESOURCE_PATTERN_PATH);
    }

    @NonNull
    private Resource[] getResources(String metadataResourcePatternPath) {
        Resource[] metadataResources = null;
        try {
            metadataResources = this.resourcePatternResolver.getResources(metadataResourcePatternPath);
        } catch (IOException e) {
            String message = format("The Configuration Metadata resources[pattern : {}] can't be read", metadataResourcePatternPath);
            throw new RuntimeException(message, e);
        }
        return metadataResources;
    }
}
