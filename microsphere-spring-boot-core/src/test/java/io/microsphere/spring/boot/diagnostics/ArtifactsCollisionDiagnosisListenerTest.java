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

package io.microsphere.spring.boot.diagnostics;


import io.microsphere.classloading.Artifact;
import io.microsphere.classloading.MavenArtifact;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.event.ApplicationContextInitializedEvent;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.mock.env.MockEnvironment;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static io.microsphere.classloading.Artifact.create;
import static io.microsphere.spring.boot.diagnostics.ArtifactsCollisionDiagnosisListener.ENABLED_PROPERTY_NAME;
import static io.microsphere.spring.boot.diagnostics.ArtifactsCollisionResourceResolver.disable;
import static io.microsphere.spring.boot.diagnostics.ArtifactsCollisionResourceResolver.enable;
import static io.microsphere.spring.boot.util.TestUtils.application;
import static io.microsphere.util.StringUtils.EMPTY_STRING_ARRAY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link ArtifactsCollisionDiagnosisListener} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see ArtifactsCollisionDiagnosisListener
 * @since 1.0.0
 */
class ArtifactsCollisionDiagnosisListenerTest {

    private MockEnvironment environment;

    private ArtifactsCollisionDiagnosisListener listener;

    @BeforeEach
    void setUp() {
        this.environment = new MockEnvironment();
        this.listener = new ArtifactsCollisionDiagnosisListener();
    }

    @Test
    void testOnApplicationEventOnDefault() {
        SpringApplication springApplication = application();
        ConfigurableApplicationContext context = new GenericApplicationContext();
        context.setEnvironment(this.environment);
        ApplicationContextInitializedEvent event = new ApplicationContextInitializedEvent(springApplication, EMPTY_STRING_ARRAY, context);
        this.listener.onApplicationEvent(event);
    }

    @Test
    void testOnApplicationEvent() {
        this.environment.setProperty(ENABLED_PROPERTY_NAME, "true");
        this.testOnApplicationEventOnDefault();
    }

    @Test
    void testOnApplicationEventOnException() {
        try {
            enable();
            assertThrows(ArtifactsCollisionException.class, this::testOnApplicationEvent);
        } finally {
            disable();
        }
    }

    @Test
    void testDiagnose() {
        Set<String> artifactsCollisionSet = this.listener.diagnose(getClass().getClassLoader());
        assertTrue(artifactsCollisionSet.isEmpty());
    }

    @Test
    void testGetArtifactsCollisionMap() {
        List<Artifact> artifacts = createArtifacts();
        Map<String, Artifact> artifactsCollisionMap = this.listener.getArtifactsCollisionMap(artifacts);
        assertEquals(1, artifactsCollisionMap.size());
        assertTrue(artifactsCollisionMap.containsKey("test-artifact"));
    }

    private List<Artifact> createArtifacts() {
        List<Artifact> artifacts = new ArrayList<>(3);
        artifacts.add(create("test-artifact"));
        artifacts.add(create("test-artifact"));
        artifacts.add(MavenArtifact.create("test-group", "test-artifact"));
        return artifacts;
    }
}