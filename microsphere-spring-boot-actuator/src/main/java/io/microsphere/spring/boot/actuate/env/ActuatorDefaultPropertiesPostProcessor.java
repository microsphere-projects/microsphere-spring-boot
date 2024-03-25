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
package io.microsphere.spring.boot.actuate.env;

import io.microsphere.spring.boot.env.DefaultPropertiesApplicationListener;
import io.microsphere.spring.boot.env.DefaultPropertiesPostProcessor;

import java.util.Set;

import static org.springframework.core.io.support.ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX;

/**
 * Spring Boot Actuator's {@link DefaultPropertiesPostProcessor} to load the properties resources being located in the
 * {@link #DEFAULT_PROPERTIES_RESOURCES_PATTERN "classpath*:/META-INF/actuator/*-default.properties"}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see DefaultPropertiesApplicationListener
 * @since 1.0.0
 */
public class ActuatorDefaultPropertiesPostProcessor implements DefaultPropertiesPostProcessor {

    /**
     * The resource pattern to load the properties resources.
     */
    public static final String DEFAULT_PROPERTIES_RESOURCES_PATTERN = CLASSPATH_ALL_URL_PREFIX + "/META-INF/config/default/*.properties";

    @Override
    public void initializeResources(Set<String> defaultPropertiesResources) {
        defaultPropertiesResources.add(DEFAULT_PROPERTIES_RESOURCES_PATTERN);
    }
}
