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
     * Returns the set of all configuration property group names.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   ConfigurationMetadataRepository repository = // obtain instance
     *   Set<String> groupNames = repository.getPropertyGroups();
     *   groupNames.forEach(name -> System.out.println("Group: " + name));
     * }</pre>
     *
     * @return a non-null set of property group names
     */
    @NonNull
    public Set<String> getPropertyGroups() {
        return this.namedGroups.keySet();
    }

    /**
     * Returns the set of all configuration property names.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   ConfigurationMetadataRepository repository = // obtain instance
     *   Set<String> propertyNames = repository.getPropertyNames();
     *   propertyNames.forEach(name -> System.out.println("Property: " + name));
     * }</pre>
     *
     * @return a non-null set of property names
     */
    @NonNull
    public Set<String> getPropertyNames() {
        return this.namedProperties.keySet();
    }

    /**
     * Returns all configuration metadata group items.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   ConfigurationMetadataRepository repository = // obtain instance
     *   Collection<ItemMetadata> groups = repository.getGroups();
     *   groups.forEach(group -> System.out.println("Group type: " + group.getType()));
     * }</pre>
     *
     * @return a non-null collection of group {@link ItemMetadata}
     */
    @NonNull
    public Collection<ItemMetadata> getGroups() {
        return this.namedGroups.values();
    }

    /**
     * Returns all configuration metadata property items.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   ConfigurationMetadataRepository repository = // obtain instance
     *   Collection<ItemMetadata> properties = repository.getProperties();
     *   properties.forEach(prop -> System.out.println(prop.getName() + " = " + prop.getDefaultValue()));
     * }</pre>
     *
     * @return a non-null collection of property {@link ItemMetadata}
     */
    @NonNull
    public Collection<ItemMetadata> getProperties() {
        return this.namedProperties.values();
    }

    /**
     * Returns the configuration metadata group item for the given name.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   ConfigurationMetadataRepository repository = // obtain instance
     *   ItemMetadata group = repository.getGroup("spring.datasource");
     *   if (group != null) {
     *       System.out.println("Source type: " + group.getSourceType());
     *   }
     * }</pre>
     *
     * @param name the name of the configuration group
     * @return the {@link ItemMetadata} for the group, or {@code null} if not found
     */
    @Nullable
    public ItemMetadata getGroup(String name) {
        return this.namedGroups.get(name);
    }

    /**
     * Returns the configuration metadata property item for the given name.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   ConfigurationMetadataRepository repository = // obtain instance
     *   ItemMetadata property = repository.getProperty("server.port");
     *   if (property != null) {
     *       System.out.println("Default: " + property.getDefaultValue());
     *   }
     * }</pre>
     *
     * @param name the name of the configuration property
     * @return the {@link ItemMetadata} for the property, or {@code null} if not found
     */
    @Nullable
    public ItemMetadata getProperty(String name) {
        return this.namedProperties.get(name);
    }

    /**
     * Returns the list of {@link ItemHint} instances associated with the given property name.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   ConfigurationMetadataRepository repository = // obtain instance
     *   List<ItemHint> hints = repository.getHints("spring.profiles.active");
     *   hints.forEach(hint -> System.out.println("Hint: " + hint.getName()));
     * }</pre>
     *
     * @param name the name of the configuration property to get hints for
     * @return a non-null list of {@link ItemHint} instances; empty if none exist
     */
    @NonNull
    public List<ItemHint> getHints(String name) {
        return this.namedHints.getOrDefault(name, emptyList());
    }

    /**
     * Returns the underlying {@link ConfigurationMetadataReader} used to read configuration metadata.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   ConfigurationMetadataRepository repository = // obtain instance
     *   ConfigurationMetadataReader reader = repository.getConfigurationMetadataReader();
     *   ConfigurationMetadata metadata = reader.read();
     * }</pre>
     *
     * @return the non-null {@link ConfigurationMetadataReader}
     */
    @NonNull
    public ConfigurationMetadataReader getConfigurationMetadataReader() {
        return configurationMetadataReader;
    }

    /**
     * Reads the {@link ConfigurationMetadata} and initializes the internal indexed maps
     * for groups, properties, and hints. This override is invoked automatically as a
     * {@link CommandLineRunner} callback after application startup.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   ConfigurationMetadataRepository repository =
     *       new ConfigurationMetadataRepository(metadataReader);
     *   repository.run(); // initializes the repository
     *   Set<String> names = repository.getPropertyNames();
     * }</pre>
     *
     * @param args the command-line arguments (not used)
     * @throws Exception if reading the configuration metadata fails
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