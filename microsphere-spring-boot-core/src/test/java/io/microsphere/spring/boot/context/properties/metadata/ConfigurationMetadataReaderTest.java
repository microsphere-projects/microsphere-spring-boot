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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.configurationprocessor.metadata.ConfigurationMetadata;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link ConfigurationMetadataReader} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
class ConfigurationMetadataReaderTest {

    private ConfigurationMetadataReader reader;

    @BeforeEach
    void setUp() {
        this.reader = new ConfigurationMetadataReader();
    }

    @Test
    void test() {
        ConfigurationMetadata metadata = this.reader.read();
        assertTrue(metadata.getItems().size() > 1);
    }

    @Test
    void testOnLoadingResourceFailed() {
        this.reader.setResourceLoader(new ResourcePatternResolver() {

            @Override
            public Resource getResource(String location) {
                throw new RuntimeException("For testing");
            }

            @Override
            public ClassLoader getClassLoader() {
                throw new RuntimeException("For testing");
            }

            @Override
            public Resource[] getResources(String locationPattern) throws IOException {
                throw new IOException("For testing");
            }
        });
        ConfigurationMetadata metadata = this.reader.read();
        assertTrue(metadata.getItems().isEmpty());

    }
}
