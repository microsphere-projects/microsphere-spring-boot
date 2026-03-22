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
package io.microsphere.spring.boot.env;

import io.microsphere.logging.Logger;
import org.springframework.boot.env.PropertySourceLoader;
import org.springframework.boot.origin.OriginLookup;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static io.microsphere.logging.LoggerFactory.getLogger;
import static io.microsphere.util.ClassLoaderUtils.getDefaultClassLoader;
import static io.microsphere.util.StringUtils.substringBetween;
import static org.springframework.core.io.support.SpringFactoriesLoader.loadFactories;
import static org.springframework.util.StringUtils.hasText;


/**
 * The composite class of {@link PropertySourceLoader} with utilities features
 * <p>
 * This class loads property sources from various file formats by delegating to individual
 * {@link PropertySourceLoader} implementations. It supports all file extensions that are
 * supported by the loaded factories.
 *
 * <h3>Example Usage</h3>
 * <pre>{@code
 * // Create a new PropertySourceLoaders instance
 * PropertySourceLoaders loaders = new PropertySourceLoaders();
 *
 * // Get supported file extensions
 * String[] extensions = loaders.getFileExtensions();
 *
 * // Load property sources from a resource
 * Resource resource = new ClassPathResource("application.properties");
 * List<PropertySource<?>> propertySources = loaders.load("myProperties", resource);
 *
 * // Reload a property source with origin tracking
 * PropertySource<?> trackedSource = loaders.reloadAsOriginTracked(propertySources.get(0));
 * }</pre>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see PropertySourceLoader
 * @since 1.0.0
 */
public class PropertySourceLoaders implements PropertySourceLoader {

    private static final Logger logger = getLogger(PropertySourceLoaders.class);

    private final ResourceLoader resourceLoader;

    private final List<PropertySourceLoader> loaders;

    /**
     * Constructs a new {@link PropertySourceLoaders} using the
     * {@linkplain io.microsphere.util.ClassLoaderUtils#getDefaultClassLoader() default ClassLoader}.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   PropertySourceLoaders loaders = new PropertySourceLoaders();
     *   List<PropertySource<?>> sources = loaders.load("test",
     *       new ClassPathResource("test.properties"));
     * }</pre>
     */
    public PropertySourceLoaders() {
        this(getDefaultClassLoader());
    }

    /**
     * Constructs a new {@link PropertySourceLoaders} using the given {@link ClassLoader}
     * to discover {@link PropertySourceLoader} factories.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
     *   PropertySourceLoaders loaders = new PropertySourceLoaders(classLoader);
     * }</pre>
     *
     * @param classLoader the {@link ClassLoader} used to load {@link PropertySourceLoader} factories
     */
    public PropertySourceLoaders(ClassLoader classLoader) {
        this(new DefaultResourceLoader(classLoader));
    }

    /**
     * Constructs a new {@link PropertySourceLoaders} using the given {@link ResourceLoader}
     * and its associated {@link ClassLoader} to discover {@link PropertySourceLoader} factories.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   ResourceLoader resourceLoader = new DefaultResourceLoader();
     *   PropertySourceLoaders loaders = new PropertySourceLoaders(resourceLoader);
     * }</pre>
     *
     * @param resourceLoader the {@link ResourceLoader} used for resource resolution and factory loading
     */
    public PropertySourceLoaders(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
        this.loaders = loadFactories(PropertySourceLoader.class, resourceLoader.getClassLoader());
    }

    /**
     * Returns the aggregated file extensions supported by all underlying
     * {@link PropertySourceLoader} instances (e.g. {@code "properties"}, {@code "yml"}).
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   PropertySourceLoaders loaders = new PropertySourceLoaders();
     *   String[] extensions = loaders.getFileExtensions();
     *   // e.g. ["properties", "xml", "yml", "yaml"]
     * }</pre>
     *
     * @return an array of supported file extensions
     */
    @Override
    public String[] getFileExtensions() {
        String[] fileExtensions = loaders.stream()
                .map(PropertySourceLoader::getFileExtensions)
                .map(Arrays::asList)
                .flatMap(List::stream)
                .toArray(String[]::new);
        return fileExtensions;
    }

    /**
     * Loads {@link PropertySource property sources} from the given {@link Resource} by
     * delegating to every underlying {@link PropertySourceLoader} whose supported file
     * extensions match the resource URL.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   PropertySourceLoaders loaders = new PropertySourceLoaders();
     *   Resource resource = new ClassPathResource("test.properties");
     *   List<PropertySource<?>> sources = loaders.load("test", resource);
     * }</pre>
     *
     * @param name     the root name of the {@link PropertySource}
     * @param resource the {@link Resource} to load properties from
     * @return a list of loaded {@link PropertySource} instances (may be empty)
     * @throws IOException if an I/O error occurs while reading the resource
     */
    @Override
    public List<PropertySource<?>> load(String name, Resource resource) throws IOException {
        List<PropertySource<?>> propertySources = new LinkedList<>();
        URL url = resource.getURL();
        for (PropertySourceLoader loader : loaders) {
            if (supports(loader, url)) {
                propertySources.addAll(loader.load(name, resource));
            }
        }
        return propertySources;
    }

    /**
     * Reload the {@link PropertySource} as an instance of {@link PropertySource} with {@link OriginLookup}
     *
     * @param propertySource {@link PropertySource}
     * @return an instance of {@link PropertySource} with {@link OriginLookup}
     * @throws IOException
     */
    public PropertySource<?> reloadAsOriginTracked(PropertySource<?> propertySource) throws IOException {
        if (propertySource instanceof OriginLookup) {
            logger.trace("The PropertySource[name : '{}', class : '{}'] is already an instance of OriginLookup",
                    propertySource.getName(), propertySource.getClass().getName());
            return propertySource;
        }
        // the name is source from Resource#getDescription()
        String name = propertySource.getName();
        String location = substringBetween(name, "[", "]");
        // the location or uri can be resolved from FileSystemResource, ClassPathResource  and UrlResource
        if (hasText(location)) {
            return loadAsOriginTracked(name, location);
        }
        return propertySource;
    }

    /**
     * Load the {@link PropertySource} as an instance of {@link PropertySource} with {@link OriginLookup}
     *
     * @param name     the name of {@link PropertySource}
     * @param location the location of {@link Resource} for {@link PropertySource}
     * @return an instance of {@link PropertySource} with {@link OriginLookup}
     * @throws IOException
     */
    public PropertySource<?> loadAsOriginTracked(String name, String location) throws IOException {
        Resource resource = resourceLoader.getResource(location);
        List<PropertySource<?>> propertySources = load(name, resource);
        for (PropertySource<?> propertySource : propertySources) {
            if (propertySource instanceof OriginLookup) {
                return propertySource;
            }
        }
        return null;
    }

    private boolean supports(PropertySourceLoader loader, URL resourceURL) {
        String[] fileExtensions = loader.getFileExtensions();
        String path = resourceURL.getPath();
        boolean supported = false;
        for (String fileExtension : fileExtensions) {
            if (path.endsWith(fileExtension)) {
                supported = true;
                break;
            }
        }
        return supported;
    }
}