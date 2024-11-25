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

import io.microsphere.util.ClassLoaderUtils;
import org.springframework.boot.env.PropertySourceLoader;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static io.microsphere.util.ClassLoaderUtils.getDefaultClassLoader;
import static org.springframework.core.io.support.SpringFactoriesLoader.loadFactories;


/**
 * The composite {@link PropertySourceLoader}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see PropertySourceLoader
 * @since 1.0.0
 */
public class PropertySourceLoaders implements PropertySourceLoader {

    private final List<PropertySourceLoader> loaders;

    public PropertySourceLoaders() {
        this(getDefaultClassLoader());
    }

    public PropertySourceLoaders(ClassLoader classLoader) {
        this(loadFactories(PropertySourceLoader.class, classLoader));
    }

    public PropertySourceLoaders(List<PropertySourceLoader> loader) {
        this.loaders = new ArrayList<>(loader.size());
        this.loaders.addAll(loader);
    }

    @Override
    public String[] getFileExtensions() {
        String[] fileExtensions = loaders.stream()
                .map(PropertySourceLoader::getFileExtensions)
                .map(Arrays::asList)
                .flatMap(List::stream)
                .toArray(String[]::new);
        return fileExtensions;
    }

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
