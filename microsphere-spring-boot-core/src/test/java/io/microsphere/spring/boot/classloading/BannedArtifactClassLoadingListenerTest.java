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

package io.microsphere.spring.boot.classloading;


import io.microsphere.annotation.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;

import java.net.URLClassLoader;
import java.util.Properties;

import static io.microsphere.spring.boot.classloading.BannedArtifactClassLoadingListener.BANNED_ARTIFACTS_ENABLED_PROPERTY_NAME;
import static io.microsphere.util.ArrayUtils.ofArray;
import static java.lang.System.getProperties;
import static java.lang.Thread.currentThread;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.boot.WebApplicationType.NONE;

/**
 * {@link BannedArtifactClassLoadingListener} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see BannedArtifactClassLoadingListener
 * @since 1.0.0
 */
class BannedArtifactClassLoadingListenerTest {

    @BeforeEach
    void setUp() {
        getProperties().remove(BANNED_ARTIFACTS_ENABLED_PROPERTY_NAME);
    }

    @AfterEach
    void tearDown() {
        getProperties().remove(BANNED_ARTIFACTS_ENABLED_PROPERTY_NAME);
    }

    @Test
    void testStartingOnDefaults() {
        assertListener(application());
    }

    @Test
    void testStarting() {
        doInEnabled(() -> assertListener(application()));
    }

    @Test
    void testStartingOnDifferentClassLoader() {
        doInEnabled(() -> {
            SpringApplication springApplication = application();
            currentThread().setContextClassLoader(new URLClassLoader(ofArray(), null));
            assertListener(springApplication);
        });
    }

    void assertListener(SpringApplication application) {
        BannedArtifactClassLoadingListener listener = new BannedArtifactClassLoadingListener(application);
        assertFalse(listener.isProcessed());
        listener.starting();
        assertTrue(listener.isProcessed());
        listener.starting();
        assertTrue(listener.isProcessed());
    }

    void doInEnabled(Runnable runnable) {
        ClassLoader classLoader = currentThread().getContextClassLoader();
        Properties properties = getProperties();
        try {
            properties.setProperty(BANNED_ARTIFACTS_ENABLED_PROPERTY_NAME, "true");
            runnable.run();
        } finally {
            properties.remove(BANNED_ARTIFACTS_ENABLED_PROPERTY_NAME);
            currentThread().setContextClassLoader(classLoader);
        }
    }

    SpringApplication application() {
        return application(currentThread().getContextClassLoader());
    }

    SpringApplication application(@Nullable ClassLoader classLoader) {
        SpringApplication springApplication = new SpringApplication();
        springApplication.setWebApplicationType(NONE);
        springApplication.setMainApplicationClass(getClass());
        if (classLoader != null) {
            ResourceLoader resourceLoader = new DefaultResourceLoader(classLoader);
            springApplication.setResourceLoader(resourceLoader);
        }
        return springApplication;
    }
}