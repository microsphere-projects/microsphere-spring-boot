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
package io.microsphere.spring.boot.webflux.constants;

import io.microsphere.annotation.ConfigurationProperty;

import static io.microsphere.annotation.ConfigurationProperty.APPLICATION_SOURCE;
import static io.microsphere.constants.PropertyConstants.ENABLED_PROPERTY_NAME;
import static io.microsphere.spring.boot.constants.PropertyConstants.MICROSPHERE_SPRING_BOOT_PROPERTY_NAME_PREFIX;

/**
 * The Property constants for Microsphere Spring Boot Web MVC
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public interface PropertyConstants {

    /**
     * The property name prefix of Microsphere Spring Boot WebFlux : "microsphere.spring.boot.webflux."
     */
    String MICROSPHERE_SPRING_BOOT_WEBFLUX_PROPERTY_NAME_PREFIX = MICROSPHERE_SPRING_BOOT_PROPERTY_NAME_PREFIX + "webflux.";

    /**
     * The default value of 'enabled' property of Microsphere Spring Boot WebFlux : "true"
     */
    String DEFAULT_MICROSPHERE_SPRING_BOOT_WEBMVC_ENABLED = "true";

    /**
     * The 'enabled' property name of Microsphere Spring Boot WebFlux : "microsphere.spring.boot.webflux.enabled"
     */
    @ConfigurationProperty(
            type = boolean.class,
            defaultValue = DEFAULT_MICROSPHERE_SPRING_BOOT_WEBMVC_ENABLED,
            source = APPLICATION_SOURCE
    )
    String MICROSPHERE_SPRING_BOOT_WEBFLUX_ENALBED_PROPERTY_NAME = MICROSPHERE_SPRING_BOOT_WEBFLUX_PROPERTY_NAME_PREFIX + ENABLED_PROPERTY_NAME;
}