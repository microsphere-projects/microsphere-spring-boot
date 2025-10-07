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

package io.microsphere.spring.boot.context;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.event.ApplicationPreparedEvent;

import static io.microsphere.spring.boot.constants.PropertyConstants.MICROSPHERE_SPRING_BOOT_LOGGING_LEVEL_PROPERTY_NAME;

/**
 * {@link LoggingOnceApplicationPreparedEventListener} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see LoggingOnceApplicationPreparedEventListener
 * @since 1.0.0
 */
class LoggingOnceApplicationPreparedEventListenerTest extends AbstractApplicationPreparedEventTest {

    private LoggingOnceApplicationPreparedEventListener listener;

    @BeforeEach
    void setUp() {
        this.listener = new LoggingOnceApplicationPreparedEventListener();
    }

    @Test
    void testOnApplicationEventOnIgnoredAndProcessed() {
        ApplicationPreparedEvent event = createEvent();
        this.listener.onApplicationEvent(event);
        this.listener.onApplicationEvent(event);
    }

    @Test
    void testOnApplicationEventOnLoggingTrace() {
        testOnApplicationEventOnLogging("trace");
    }

    @Test
    void testOnApplicationEventOnLoggingDebug() {
        testOnApplicationEventOnLogging("debug");
    }

    @Test
    void testOnApplicationEventOnLoggingInfo() {
        testOnApplicationEventOnLogging("info");
    }

    @Test
    void testOnApplicationEventOnLoggingWarn() {
        testOnApplicationEventOnLogging("warn");
    }

    @Test
    void testOnApplicationEventOnLoggingError() {
        testOnApplicationEventOnLogging("error");
    }

    void testOnApplicationEventOnLogging(String level) {
        String name = MICROSPHERE_SPRING_BOOT_LOGGING_LEVEL_PROPERTY_NAME;
        String arg = "--" + name + "=" + level;
        ApplicationPreparedEvent event = createEvent(arg);
        this.listener.onApplicationEvent(event);
    }
}