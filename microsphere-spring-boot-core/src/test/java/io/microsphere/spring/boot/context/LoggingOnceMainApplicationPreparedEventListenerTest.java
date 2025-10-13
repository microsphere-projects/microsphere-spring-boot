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
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.GenericApplicationContext;

import java.net.URLClassLoader;

import static io.microsphere.spring.boot.context.OnceMainApplicationPreparedEventListener.BOOTSTRAP_CONTEXT_ID_PROPERTY_NAME;
import static io.microsphere.spring.boot.context.OnceMainApplicationPreparedEventListener.DEFAULT_BOOTSTRAP_CONTEXT_ID;
import static io.microsphere.util.ArrayUtils.ofArray;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link LoggingOnceMainApplicationPreparedEventListener} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see LoggingOnceMainApplicationPreparedEventListener
 * @since 1.0.0
 */
class LoggingOnceMainApplicationPreparedEventListenerTest extends AbstractApplicationPreparedEventTest {

    private LoggingOnceMainApplicationPreparedEventListener listener;

    @BeforeEach
    void setUp() {
        this.listener = new LoggingOnceMainApplicationPreparedEventListener();
    }

    @Test
    void testOnApplicationEvent() {
        String arg = "--" + BOOTSTRAP_CONTEXT_ID_PROPERTY_NAME + "=test-bootstrap";
        ApplicationPreparedEvent event = createEvent(arg);
        this.listener.onApplicationEvent(event);
    }

    @Test
    void testOnApplicationEventWithBootstrapContextAsPresent() {
        testOnApplicationEventWithBootstrapContextAsPresent("bootstrap-context");
        testOnApplicationEventWithBootstrapContextAsPresent(DEFAULT_BOOTSTRAP_CONTEXT_ID);
    }

    @Test
    void testIsIgnoredOnBootstrapApplicationListenerClassNotFound() {
        ConfigurableApplicationContext context = new GenericApplicationContext();
        context.setClassLoader(new URLClassLoader(ofArray(), null));
        assertFalse(listener.isIgnored(context));
    }

    @Test
    void testIsIgnoredOnContextIdMismatched() {
        ConfigurableApplicationContext context = new GenericApplicationContext();
        assertFalse(listener.isIgnored(context));
    }

    @Test
    void testIsIgnoredOnBootstrapContextAsPresent() {
        ConfigurableApplicationContext context = new GenericApplicationContext();
        ConfigurableApplicationContext parentContext = new GenericApplicationContext();
        parentContext.setId(DEFAULT_BOOTSTRAP_CONTEXT_ID);
        context.setParent(parentContext);
        assertFalse(listener.isIgnored(context));
    }

    @Test
    void testIsIgnoredOnBootstrapContextMatched() {
        ConfigurableApplicationContext context = new GenericApplicationContext();
        context.setId(DEFAULT_BOOTSTRAP_CONTEXT_ID);
        assertTrue(listener.isIgnored(context));
    }

    @Test
    void testIsMainApplicationContextOnBootstrapApplicationListenerClassNotFound() {
        ConfigurableApplicationContext context = new GenericApplicationContext();
        context.setClassLoader(new URLClassLoader(ofArray(), null));
        assertTrue(listener.isMainApplicationContext(context));
    }

    void testOnApplicationEventWithBootstrapContextAsPresent(String contextId, String... args) {
        ApplicationPreparedEvent event = createEvent(args);
        ConfigurableApplicationContext context = event.getApplicationContext();
        ConfigurableApplicationContext parentContext = new GenericApplicationContext();
        parentContext.setId(contextId);
        context.setParent(parentContext);
        this.listener.onApplicationEvent(event);
    }
}