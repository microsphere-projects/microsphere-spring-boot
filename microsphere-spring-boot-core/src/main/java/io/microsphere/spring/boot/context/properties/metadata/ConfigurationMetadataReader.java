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
package io.microsphere.spring.boot.context.properties.metadata;

import io.microsphere.logging.Logger;
import org.springframework.boot.configurationprocessor.metadata.ConfigurationMetadata;
import org.springframework.boot.configurationprocessor.metadata.JsonMarshaller;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import java.io.InputStream;

import static io.microsphere.logging.LoggerFactory.getLogger;
import static org.springframework.core.io.support.ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX;

/**
 * {@link ConfigurationMetadata} Reader
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public class ConfigurationMetadataReader implements ResourceLoaderAware {

    private final static Logger logger = getLogger(ConfigurationMetadataReader.class);

    public static final String METADATA_PATH = CLASSPATH_ALL_URL_PREFIX + "/META-INF/spring-configuration-metadata.json";

    public static final String ADDITIONAL_METADATA_PATH = CLASSPATH_ALL_URL_PREFIX + "/META-INF/additional-spring-configuration-metadata.json";

    private ResourcePatternResolver resourcePatternResolver;

    public ConfigurationMetadata read() {
        ConfigurationMetadata metadata = new ConfigurationMetadata();
        readMetadata(metadata, METADATA_PATH);
        readMetadata(metadata, ADDITIONAL_METADATA_PATH);
        return metadata;
    }

    private void readMetadata(ConfigurationMetadata metadata, String locationPattern) {
        ResourcePatternResolver resourcePatternResolver = getResourcePatternResolver();
        try {
            Resource[] resources = resourcePatternResolver.getResources(locationPattern);
            for (Resource resource : resources) {
                readMetadata(metadata, resource);
            }
        } catch (Exception e) {
            logger.error("The configuration metadata resource pattern['{}'] can't be read", locationPattern, e);
        }
    }

    private void readMetadata(ConfigurationMetadata metadata, Resource resource) throws Exception {
        JsonMarshaller jsonMarshaller = new JsonMarshaller();
        try (InputStream inputStream = resource.getInputStream()) {
            ConfigurationMetadata resourceMetadata = jsonMarshaller.read(inputStream);
            metadata.merge(resourceMetadata);
        }
    }

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        if (resourceLoader instanceof ResourcePatternResolver) {
            this.resourcePatternResolver = (ResourcePatternResolver) resourceLoader;
        } else {
            this.resourcePatternResolver = new PathMatchingResourcePatternResolver(resourceLoader);
        }
    }

    public ResourcePatternResolver getResourcePatternResolver() {
        ResourcePatternResolver resourcePatternResolver = this.resourcePatternResolver;
        if (resourcePatternResolver == null) {
            resourcePatternResolver = new PathMatchingResourcePatternResolver();
        }
        return resourcePatternResolver;
    }
}