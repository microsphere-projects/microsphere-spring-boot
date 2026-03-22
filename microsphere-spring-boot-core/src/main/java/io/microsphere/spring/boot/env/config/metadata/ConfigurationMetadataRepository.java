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
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.configurationprocessor.metadata.ConfigurationMetadata;
import org.springframework.boot.configurationprocessor.metadata.ItemHint;
import org.springframework.boot.configurationprocessor.metadata.ItemMetadata;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.Collection;
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
public class ConfigurationMetadataRepository implements CommandLineRunner {

    private final ConfigurationMetadataReader configurationMetadataReader;

    private Map<String, ItemMetadata> namedGroups;

    private Map<String, ItemMetadata> namedProperties;

    private Map<String, List<ItemHint>> namedHints;

    /**
     * Constructs a new {@link ConfigurationMetadataRepository} backed by the given
     * {@link ConfigurationMetadataReader}.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   ConfigurationMetadataReader reader = new ConfigurationMetadataReader();
     *   ConfigurationMetadataRepository repository = new ConfigurationMetadataRepository(reader);
     * }</pre>
     *
     * @param configurationMetadataReader the reader used to load {@link ConfigurationMetadata}
     */
    public ConfigurationMetadataRepository(ConfigurationMetadataReader configurationMetadataReader) {
        this.configurationMetadataReader = configurationMetadataReader;
    }

    /**
     * Returns the names of all configuration property groups.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   Set<String> groups = repository.getPropertyGroups();
     *   // e.g. ["server", "spring.datasource"]
     * }</pre>
     *
     * @return an unmodifiable {@link Set} of group names
     */
    @NonNull
    public Set<String> getPropertyGroups() {
        return this.namedGroups.keySet();
    }

    /**
     * Returns the names of all configuration properties.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   Set<String> names = repository.getPropertyNames();
     *   // e.g. ["server.port", "spring.datasource.url"]
     * }</pre>
     *
     * @return an unmodifiable {@link Set} of property names
     */
    @NonNull
    public Set<String> getPropertyNames() {
        return this.namedProperties.keySet();
    }

    /**
     * Returns all configuration property group {@link ItemMetadata} entries.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   Collection<ItemMetadata> groups = repository.getGroups();
     *   groups.forEach(g -> System.out.println(g.getName()));
     * }</pre>
     *
     * @return a {@link Collection} of group {@link ItemMetadata}
     */
    @NonNull
    public Collection<ItemMetadata> getGroups() {
        return this.namedGroups.values();
    }

    /**
     * Returns all configuration property {@link ItemMetadata} entries.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   Collection<ItemMetadata> properties = repository.getProperties();
     *   properties.forEach(p -> System.out.println(p.getName() + " = " + p.getType()));
     * }</pre>
     *
     * @return a {@link Collection} of property {@link ItemMetadata}
     */
    @NonNull
    public Collection<ItemMetadata> getProperties() {
        return this.namedProperties.values();
    }

    /**
     * Returns the group {@link ItemMetadata} for the given name, or {@code null} if not found.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   ItemMetadata group = repository.getGroup("server");
     *   if (group != null) {
     *       System.out.println(group.getType());
     *   }
     * }</pre>
     *
     * @param name the group name to look up
     * @return the matching {@link ItemMetadata}, or {@code null}
     */
    @Nullable
    public ItemMetadata getGroup(String name) {
        return this.namedGroups.get(name);
    }

    /**
     * Returns the property {@link ItemMetadata} for the given name, or {@code null} if not found.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   ItemMetadata property = repository.getProperty("server.port");
     *   if (property != null) {
     *       System.out.println(property.getDefaultValue());
     *   }
     * }</pre>
     *
     * @param name the property name to look up
     * @return the matching {@link ItemMetadata}, or {@code null}
     */
    @Nullable
    public ItemMetadata getProperty(String name) {
        return this.namedProperties.get(name);
    }

    /**
     * Returns the {@link ItemHint} list associated with the given name, or an empty list
     * if no hints exist.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   List<ItemHint> hints = repository.getHints("server.port");
     *   hints.forEach(h -> System.out.println(h.getName()));
     * }</pre>
     *
     * @param name the property or group name to look up hints for
     * @return a list of {@link ItemHint} entries, never {@code null}
     */
    @NonNull
    public List<ItemHint> getHints(String name) {
        return this.namedHints.getOrDefault(name, emptyList());
    }

    /**
     * Returns the {@link ConfigurationMetadataReader} used by this repository.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   ConfigurationMetadataReader reader = repository.getConfigurationMetadataReader();
     *   ConfigurationMetadata metadata = reader.read();
     * }</pre>
     *
     * @return the underlying {@link ConfigurationMetadataReader}
     */
    @NonNull
    public ConfigurationMetadataReader getConfigurationMetadataReader() {
        return configurationMetadataReader;
    }

    /**
     * Reads the {@link ConfigurationMetadata} via the underlying reader and initializes
     * the internal group, property, and hint indexes. Invoked automatically by the Spring
     * Boot {@link CommandLineRunner} lifecycle.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   // Typically called by Spring Boot; manual invocation:
     *   repository.run();
     * }</pre>
     *
     * @param args command-line arguments (unused)
     * @throws Exception if reading the metadata fails
     */
    @Override
    public void run(String... args) throws Exception {
        ConfigurationMetadata configurationMetadata = this.configurationMetadataReader.read();
        // ConfigurationMetadata can't return the underlying items as Map
        init(configurationMetadata);
    }

    private void init(ConfigurationMetadata configurationMetadata) {
        List<ItemMetadata> items = configurationMetadata.getItems();
        initNamedGroupItems(items);
        initNamedPropertyItems(items);
        initNamedHints(configurationMetadata.getHints());
    }

    private void initNamedGroupItems(List<ItemMetadata> items) {
        this.namedGroups = createNamedItems(items, GROUP);
    }

    private void initNamedPropertyItems(List<ItemMetadata> items) {
        this.namedProperties = createNamedItems(items, PROPERTY);
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