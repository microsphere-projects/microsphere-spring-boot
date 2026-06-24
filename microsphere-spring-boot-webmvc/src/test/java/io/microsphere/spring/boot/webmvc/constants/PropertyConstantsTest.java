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

import org.junit.jupiter.api.Test;

import static io.microsphere.spring.boot.webmvc.constants.PropertyConstants.DEFAULT_MICROSPHERE_SPRING_BOOT_WEBMVC_ENABLED;
import static io.microsphere.spring.boot.webmvc.constants.PropertyConstants.DEFAULT_MICROSPHERE_SPRING_BOOT_WEBMVC_FILTER_ENABLED;
import static io.microsphere.spring.boot.webmvc.constants.PropertyConstants.DEFAULT_MICROSPHERE_SPRING_BOOT_WEBMVC_LOGGING_ENABLED;
import static io.microsphere.spring.boot.webmvc.constants.PropertyConstants.DEFAULT_MICROSPHERE_SPRING_WEBMVC_CONTENT_NEGOTIATION_ENABLED;
import static io.microsphere.spring.boot.webmvc.constants.PropertyConstants.FILTER_PROPERTY_NAME_PREFIX;
import static io.microsphere.spring.boot.webmvc.constants.PropertyConstants.LOGGING_PROPERTY_NAME_PREFIX;
import static io.microsphere.spring.boot.webmvc.constants.PropertyConstants.MICROSPHERE_SPRING_BOOT_WEBMVC_ENALBED_PROPERTY_NAME;
import static io.microsphere.spring.boot.webmvc.constants.PropertyConstants.MICROSPHERE_SPRING_BOOT_WEBMVC_FILTER_ENALBED_PROPERTY_NAME;
import static io.microsphere.spring.boot.webmvc.constants.PropertyConstants.MICROSPHERE_SPRING_BOOT_WEBMVC_LOGGING_ENALBED_PROPERTY_NAME;
import static io.microsphere.spring.boot.webmvc.constants.PropertyConstants.MICROSPHERE_SPRING_BOOT_WEBMVC_PROPERTY_NAME_PREFIX;
import static io.microsphere.spring.boot.webmvc.constants.PropertyConstants.MICROSPHERE_SPRING_WEBMVC_CONTENT_NEGOTIATION_ENABLED_PROPERTY_NAME;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * {@link PropertyConstants} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see PropertyConstants
 * @since 1.0.0
 */
class PropertyConstantsTest {

    @Test
    void testConstants() {
        assertEquals("microsphere.spring.boot.webmvc.", MICROSPHERE_SPRING_BOOT_WEBMVC_PROPERTY_NAME_PREFIX);
        assertEquals("true", DEFAULT_MICROSPHERE_SPRING_BOOT_WEBMVC_ENABLED);
        assertEquals("true", DEFAULT_MICROSPHERE_SPRING_BOOT_WEBMVC_FILTER_ENABLED);
        assertEquals("true", DEFAULT_MICROSPHERE_SPRING_BOOT_WEBMVC_LOGGING_ENABLED);
        assertEquals("true", DEFAULT_MICROSPHERE_SPRING_WEBMVC_CONTENT_NEGOTIATION_ENABLED);
        assertEquals("microsphere.spring.boot.webmvc.enabled", MICROSPHERE_SPRING_BOOT_WEBMVC_ENALBED_PROPERTY_NAME);
        assertEquals("microsphere.spring.boot.webmvc.filter.", FILTER_PROPERTY_NAME_PREFIX);
        assertEquals("microsphere.spring.boot.webmvc.filter.enabled", MICROSPHERE_SPRING_BOOT_WEBMVC_FILTER_ENALBED_PROPERTY_NAME);
        assertEquals("microsphere.spring.webmvc.content-negotiation.enabled", MICROSPHERE_SPRING_WEBMVC_CONTENT_NEGOTIATION_ENABLED_PROPERTY_NAME);
        assertEquals("microsphere.spring.boot.webmvc.logging.", LOGGING_PROPERTY_NAME_PREFIX);
        assertEquals("microsphere.spring.boot.webmvc.logging.enabled", MICROSPHERE_SPRING_BOOT_WEBMVC_LOGGING_ENALBED_PROPERTY_NAME);
    }
}
