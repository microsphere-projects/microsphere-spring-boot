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

import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.configurationprocessor.metadata.ConfigurationMetadata;
import org.springframework.boot.configurationprocessor.metadata.ItemHint;
import org.springframework.boot.configurationprocessor.metadata.ItemMetadata;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.Collections.emptyList;
import static org.springframework.boot.configurationprocessor.metadata.ItemMetadata.ItemType.GROUP;
import static org.springframework.boot.configurationprocessor.metadata.ItemMetadata.ItemType.PROPERTY;

/**
 * The Repository for {@link ConfigurationMetadata}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see ConfigurationMetadata
 * @see ConfigurationMetadataReader
 * @since 1.0.0
 */
public class ConfigurationMetadataRepository implements BeanClassLoaderAware, CommandLineRunner {

    private ConfigurationMetadataReader configurationMetadataReader;

    private Map<String, ItemMetadata> namedGroupItems;

    private Map<String, ItemMetadata> namedPropertyItems;

    private Map<String, List<ItemHint>> namedHints;

    public Set<String> getPropertyGroups() {
        return this.namedGroupItems.keySet();
    }

    public Set<String> getPropertyNames() {
        return this.namedPropertyItems.keySet();
    }

    public Collection<ItemMetadata> getGroups() {
        return this.namedGroupItems.values();
    }

    public Collection<ItemMetadata> getProperties() {
        return this.namedPropertyItems.values();
    }

    public ItemMetadata getGroup(String name) {
        return this.namedGroupItems.get(name);
    }

    public ItemMetadata getProperty(String name) {
        return this.namedPropertyItems.get(name);
    }

    public List<ItemHint> getHints(String name) {
        return this.namedHints.getOrDefault(name, emptyList());
    }

    @Override
    public void run(String... args) throws Exception {
        ConfigurationMetadata configurationMetadata = this.configurationMetadataReader.read();
        // ConfigurationMetadata can't return the underlying items as Map
        init(configurationMetadata);
    }

    @Override
    public void setBeanClassLoader(ClassLoader classLoader) {
        this.configurationMetadataReader = new ConfigurationMetadataReader(classLoader);
    }

    private void init(ConfigurationMetadata configurationMetadata) {
        List<ItemMetadata> items = configurationMetadata.getItems();
        initNamedGroupItems(items);
        initNamedPropertyItems(items);
        initNamedHints(configurationMetadata.getHints());
    }

    private void initNamedGroupItems(List<ItemMetadata> items) {
        this.namedGroupItems = createNamedItems(items, GROUP);
    }

    private void initNamedPropertyItems(List<ItemMetadata> items) {
        this.namedPropertyItems = createNamedItems(items, PROPERTY);
    }

    private void initNamedHints(List<ItemHint> items) {
        Map<String, List<ItemHint>> namedHints = new LinkedHashMap<>(items.size());
        items.stream().forEach(itemHint -> {
            List<ItemHint> itemHints = namedHints.computeIfAbsent(itemHint.getName(), i -> new LinkedList<>());
            itemHints.add(itemHint);
        });
        this.namedHints = namedHints;
    }

    private Map<String, ItemMetadata> createNamedItems(List<ItemMetadata> items, ItemMetadata.ItemType itemType) {
        Map<String, ItemMetadata> namedItems = new LinkedHashMap<>(items.size());
        items.stream().filter(item -> item.isOfItemType(itemType)).forEach(item -> {
            namedItems.put(item.getName(), item);
        });
        return namedItems;
    }
}
