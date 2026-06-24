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
package io.microsphere.spring.boot.webmvc.constants;

import io.microsphere.annotation.ConfigurationProperty;
import io.microsphere.spring.webmvc.config.ConfigurableContentNegotiationManagerWebMvcConfigurer;

import static io.microsphere.annotation.ConfigurationProperty.APPLICATION_SOURCE;
import static io.microsphere.constants.PropertyConstants.ENABLED_PROPERTY_NAME;
import static io.microsphere.spring.boot.constants.PropertyConstants.MICROSPHERE_SPRING_BOOT_PROPERTY_NAME_PREFIX;
import static io.microsphere.spring.webmvc.constants.PropertyConstants.MICROSPHERE_SPRING_WEBMVC_PROPERTY_NAME_PREFIX;

/**
 * The Property constants for Microsphere Spring Boot Web MVC
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public interface PropertyConstants {

    /**
     * The property name prefix of Microsphere Spring Boot Web MVC : "microsphere.spring.boot.webmvc."
     */
    String MICROSPHERE_SPRING_BOOT_WEBMVC_PROPERTY_NAME_PREFIX = MICROSPHERE_SPRING_BOOT_PROPERTY_NAME_PREFIX + "webmvc.";

    /**
     * The default value of 'enabled' property of Microsphere Spring Boot Web MVC : "true"
     */
    String DEFAULT_MICROSPHERE_SPRING_BOOT_WEBMVC_ENABLED = "true";

    /**
     * The default value of 'enabled' property of Microsphere Spring Boot MVC Filter : "true"
     */
    String DEFAULT_MICROSPHERE_SPRING_BOOT_WEBMVC_FILTER_ENABLED = "true";

    /**
     * The default value of 'enabled' property of Microsphere Spring Boot MVC Logging : "true"
     */
    String DEFAULT_MICROSPHERE_SPRING_BOOT_WEBMVC_LOGGING_ENABLED = "true";

    /**
     * The default value of 'enabled' property of Microsphere Spring Web MVC Content Negotiation : "true"
     */
    String DEFAULT_MICROSPHERE_SPRING_WEBMVC_CONTENT_NEGOTIATION_ENABLED = "true";

    /**
     * The 'enabled' property name of Microsphere Spring Boot Web MVC : "microsphere.spring.boot.webmvc.enabled"
     */
    @ConfigurationProperty(
            type = boolean.class,
            defaultValue = DEFAULT_MICROSPHERE_SPRING_BOOT_WEBMVC_ENABLED,
            source = APPLICATION_SOURCE
    )
    String MICROSPHERE_SPRING_BOOT_WEBMVC_ENALBED_PROPERTY_NAME = MICROSPHERE_SPRING_BOOT_WEBMVC_PROPERTY_NAME_PREFIX + ENABLED_PROPERTY_NAME;

    /**
     * The property name prefix of Microsphere Spring Boot Web MVC Filter : "microsphere.spring.boot.webmvc.filter."
     */
    String FILTER_PROPERTY_NAME_PREFIX = MICROSPHERE_SPRING_BOOT_WEBMVC_PROPERTY_NAME_PREFIX + "filter.";

    /**
     * The 'enabled' property name of Microsphere Spring Boot Web MVC Filter : "microsphere.spring.boot.webmvc.filter.enabled"
     */
    @ConfigurationProperty(
            type = boolean.class,
            defaultValue = DEFAULT_MICROSPHERE_SPRING_BOOT_WEBMVC_FILTER_ENABLED,
            source = APPLICATION_SOURCE
    )
    String MICROSPHERE_SPRING_BOOT_WEBMVC_FILTER_ENALBED_PROPERTY_NAME = FILTER_PROPERTY_NAME_PREFIX + ENABLED_PROPERTY_NAME;

    /**
     * The property name prefix of Microsphere Spring Web MVC Content Negotiation : "microsphere.spring.webmvc.content-negotiation."
     *
     * @see ConfigurableContentNegotiationManagerWebMvcConfigurer#PROPERTY_NAME_PREFIX
     */
    String CONTENT_NEGOTIATION_PROPERTY_NAME_PREFIX = MICROSPHERE_SPRING_WEBMVC_PROPERTY_NAME_PREFIX + "content-negotiation.";

    /**
     * The 'enabled' property name of Microsphere Spring Web MVC Content Negotiation : "microsphere.spring.webmvc.content-negotiation.enabled"
     */
    @ConfigurationProperty(
            type = boolean.class,
            defaultValue = DEFAULT_MICROSPHERE_SPRING_WEBMVC_CONTENT_NEGOTIATION_ENABLED,
            source = APPLICATION_SOURCE
    )
    String MICROSPHERE_SPRING_WEBMVC_CONTENT_NEGOTIATION_ENABLED_PROPERTY_NAME = CONTENT_NEGOTIATION_PROPERTY_NAME_PREFIX + ENABLED_PROPERTY_NAME;

    /**
     * The property name prefix of Microsphere Spring Boot Web MVC Logging : "microsphere.spring.boot.webmvc.logging."
     */
    String LOGGING_PROPERTY_NAME_PREFIX = MICROSPHERE_SPRING_BOOT_WEBMVC_PROPERTY_NAME_PREFIX + "logging.";

    /**
     * The 'enabled' property name of Microsphere Spring Boot Web MVC Logging : "microsphere.spring.boot.webmvc.logging.enabled"
     */
    @ConfigurationProperty(
            type = boolean.class,
            defaultValue = DEFAULT_MICROSPHERE_SPRING_BOOT_WEBMVC_LOGGING_ENABLED,
            source = APPLICATION_SOURCE
    )
    String MICROSPHERE_SPRING_BOOT_WEBMVC_LOGGING_ENALBED_PROPERTY_NAME = LOGGING_PROPERTY_NAME_PREFIX + ENABLED_PROPERTY_NAME;
}