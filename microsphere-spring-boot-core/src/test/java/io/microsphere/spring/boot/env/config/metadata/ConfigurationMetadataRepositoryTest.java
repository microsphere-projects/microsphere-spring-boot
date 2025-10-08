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

package io.microsphere.spring.boot.env.config.metadata;


import io.microsphere.spring.boot.context.properties.metadata.ConfigurationMetadataReader;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.configurationprocessor.metadata.ItemHint;
import org.springframework.boot.configurationprocessor.metadata.ItemMetadata;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link ConfigurationMetadataRepository} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see ConfigurationMetadataRepository
 * @since 1.0.0
 */
@SpringBootTest(classes = {
        ConfigurationMetadataReader.class,
        ConfigurationMetadataRepository.class,
        ConfigurationMetadataRepositoryTest.class,
})
class ConfigurationMetadataRepositoryTest {

    @Autowired
    private ConfigurationMetadataRepository repository;

    @Test
    void test() {
        Collection<ItemMetadata> groups = repository.getGroups();
        assertFalse(groups.isEmpty());

        Set<String> propertyGroups = repository.getPropertyGroups();
        assertFalse(propertyGroups.isEmpty());

        for (String propertyGroup : propertyGroups) {
            ItemMetadata group = repository.getGroup(propertyGroup);
            assertTrue(groups.contains(group));
        }

        Collection<ItemMetadata> items = repository.getProperties();
        assertFalse(items.isEmpty());

        Set<String> propertyNames = repository.getPropertyNames();
        assertFalse(propertyNames.isEmpty());

        for (String propertyName : propertyNames) {
            ItemMetadata item = repository.getProperty(propertyName);
            List<ItemHint> hints = repository.getHints(propertyName);
            assertTrue(items.contains(item));
            assertNotNull(hints);
        }

        ConfigurationMetadataReader configurationMetadataReader = repository.getConfigurationMetadataReader();
        assertNotNull(configurationMetadataReader);

    }
}