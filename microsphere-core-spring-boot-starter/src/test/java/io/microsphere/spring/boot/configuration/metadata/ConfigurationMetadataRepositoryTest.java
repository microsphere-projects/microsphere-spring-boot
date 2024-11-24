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

import io.microsphere.spring.boot.env.config.metadata.ConfigurationMetadataRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.configurationprocessor.metadata.ItemMetadata;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.boot.configurationprocessor.metadata.ItemMetadata.ItemType.GROUP;
import static org.springframework.boot.configurationprocessor.metadata.ItemMetadata.ItemType.PROPERTY;

/**
 * {@link ConfigurationMetadataRepository} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see ConfigurationMetadataRepository
 * @since 1.0.0
 */
@SpringBootTest(
        classes = {
                ConfigurationMetadataRepository.class,
                ConfigurationMetadataRepositoryTest.class,
        },
        webEnvironment = SpringBootTest.WebEnvironment.NONE
)
public class ConfigurationMetadataRepositoryTest {

    @Autowired
    private ConfigurationMetadataRepository repository;

    @Test
    public void testGetter() {
        assertGroups();
    }

    private void assertGroups() {
        this.repository.getGroups().forEach(this::assertGroupItem);
        this.repository.getPropertyGroups().stream()
                .map(this.repository::getGroup).forEach(this::assertGroupItem);
    }

    private void assertProperties() {
        this.repository.getProperties().forEach(this::assertProperty);
        this.repository.getPropertyNames().stream()
                .map(this.repository::getProperty).forEach(this::assertProperty);
    }

    private void assertGroupItem(ItemMetadata groupItem) {
        assertTrue(groupItem.isOfItemType(GROUP));
        assertNotNull(groupItem.getName());
        assertNull(groupItem.getDefaultValue());
        assertNull(groupItem.getDescription());
    }

    private void assertProperty(ItemMetadata propertyItem) {
        assertTrue(propertyItem.isOfItemType(PROPERTY));
        assertNotNull(propertyItem.getName());
        assertNotNull(propertyItem.getSourceType());
        assertNotNull(propertyItem.getType());
        assertNotNull(propertyItem.getDefaultValue());
        assertNotNull(propertyItem.getDescription());
    }
}
