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


import org.junit.jupiter.api.Test;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.stereotype.Component;

import static io.microsphere.collection.Sets.ofSet;
import static io.microsphere.spring.boot.util.TestUtils.application;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * {@link ArtifactsCollisionFailureAnalyzer} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see ArtifactsCollisionFailureAnalyzer
 * @since 1.0.0
 */
@Component
class ArtifactsCollisionFailureAnalyzerTest implements CommandLineRunner {

    @Test
    void test() {
        SpringApplication springApplication = application(getClass());
        assertThrows(Exception.class, springApplication::run);
    }

    @Override
    public void run(String... args) throws Exception {
        throw new ArtifactsCollisionException("For testing", ofSet("test-groupId:test-artifactId"));
    }
}