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
package io.microsphere.spring.boot.constants;

import io.microsphere.annotation.ConfigurationProperty;

import static io.microsphere.annotation.ConfigurationProperty.APPLICATION_SOURCE;
import static io.microsphere.spring.constants.PropertyConstants.MICROSPHERE_SPRING_PROPERTY_NAME_PREFIX;

/**
 * The Property constants for Microsphere Spring Boot
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public interface PropertyConstants {

    /**
     * The property name prefix of Microsphere Spring Boot : "microsphere.spring.boot."
     */
    String MICROSPHERE_SPRING_BOOT_PROPERTY_NAME_PREFIX = MICROSPHERE_SPRING_PROPERTY_NAME_PREFIX + "boot.";

    /**
     * The default value of Microsphere Spring Boot logging level : "TRACE"
     */
    String DEFAULT_MICROSPHERE_SPRING_BOOT_LOGGING_LEVEL = "TRACE";

    /**
     * The property name of Microsphere Spring Boot logging level
     */
    @ConfigurationProperty(
            defaultValue = DEFAULT_MICROSPHERE_SPRING_BOOT_LOGGING_LEVEL,
            source = APPLICATION_SOURCE
    )
    String MICROSPHERE_SPRING_BOOT_LOGGING_LEVEL_PROPERTY_NAME = MICROSPHERE_SPRING_BOOT_PROPERTY_NAME_PREFIX + "logging.level";
}
