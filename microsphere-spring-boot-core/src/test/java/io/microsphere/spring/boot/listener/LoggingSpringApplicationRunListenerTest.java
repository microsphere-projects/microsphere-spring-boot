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

package io.microsphere.spring.boot.listener;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.ConfigurableBootstrapContext;
import org.springframework.boot.DefaultBootstrapContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.GenericApplicationContext;

import static io.microsphere.spring.boot.util.TestUtils.application;
import static io.microsphere.util.StringUtils.EMPTY_STRING_ARRAY;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * {@link LoggingSpringApplicationRunListener} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see LoggingSpringApplicationRunListener
 * @since 1.0.0
 */
class LoggingSpringApplicationRunListenerTest {

    private LoggingSpringApplicationRunListener listener;

    private ConfigurableBootstrapContext bootstrapContext;

    private ConfigurableApplicationContext context;

    @BeforeEach
    void setUp() {
        this.listener = new LoggingSpringApplicationRunListener(application(getClass()), EMPTY_STRING_ARRAY);
        this.bootstrapContext = new DefaultBootstrapContext();
        this.context = new GenericApplicationContext();
    }

    @Test
    void testStartingWithBootstrapContext() {
        this.listener.starting(this.bootstrapContext);
    }

    @Test
    void testStarting() {
        this.listener.starting();
    }

    @Test
    void testEnvironmentPrepared() {
        this.listener.environmentPrepared(this.context.getEnvironment());
    }

    @Test
    void testContextPrepared() {
        this.listener.contextPrepared(this.context);
    }

    @Test
    void testContextLoaded() {
        this.listener.contextLoaded(this.context);
    }

    @Test
    void testStarted() {
        this.listener.started(this.context);
    }

    @Test
    void testRunning() {
        this.listener.running(this.context);
    }

    @Test
    void testFailed() {
        this.listener.failed(context, new Throwable("For testing"));
    }

    @Test
    void testGetOrder() {
        assertEquals(0, this.listener.getOrder());
        this.listener.setOrder(1);
        assertEquals(1, this.listener.getOrder());
    }
}