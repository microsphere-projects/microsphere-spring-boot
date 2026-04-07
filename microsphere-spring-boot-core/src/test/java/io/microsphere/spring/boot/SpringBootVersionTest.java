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
package io.microsphere.spring.boot;

import io.microsphere.util.Version;
import org.junit.jupiter.api.Test;

import static io.microsphere.spring.boot.SpringBootVersion.CURRENT;
import static io.microsphere.spring.boot.SpringBootVersion.SPRING_BOOT_3_0;
import static io.microsphere.spring.boot.SpringBootVersion.SPRING_BOOT_3_1;
import static io.microsphere.spring.boot.SpringBootVersion.SPRING_BOOT_3_2;
import static io.microsphere.spring.boot.SpringBootVersion.SPRING_BOOT_3_3;
import static io.microsphere.spring.boot.SpringBootVersion.SPRING_BOOT_3_4;
import static io.microsphere.spring.boot.SpringBootVersion.SPRING_BOOT_3_5;
import static io.microsphere.spring.boot.SpringBootVersion.SPRING_BOOT_4_0;
import static io.microsphere.spring.boot.SpringBootVersion.SPRING_BOOT_4_0_3;
import static io.microsphere.spring.boot.SpringBootVersion.SPRING_BOOT_4_0_4;
import static io.microsphere.spring.boot.SpringBootVersion.resolveVersion;
import static io.microsphere.spring.boot.SpringBootVersion.valueOf;
import static io.microsphere.spring.boot.SpringBootVersion.values;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link SpringBootVersion} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
class SpringBootVersionTest {

    @Test
    void testVersionRange() {
        // Spring Boot Framework 3.0 -> [3.0.0, 3.0.13]
        testVersionRange(SPRING_BOOT_3_0, 0, 13);
        // Spring Boot Framework 3.1 -> [3.1.0, 3.1.12]
        testVersionRange(SPRING_BOOT_3_1, 0, 12);
        // Spring Boot Framework 3.2 -> [3.2.0, 3.2.12]
        testVersionRange(SPRING_BOOT_3_2, 0, 12);
        // Spring Boot Framework 3.3 -> [3.3.0, 3.3.13]
        testVersionRange(SPRING_BOOT_3_3, 0, 13);
        // Spring Boot Framework 3.4 -> [3.4.0, 3.4.13]
        testVersionRange(SPRING_BOOT_3_4, 0, 13);
        // Spring Boot Framework 3.5 -> [3.5.0, 3.5.13]
        testVersionRange(SPRING_BOOT_3_5, 0, 13);
        // Spring Boot Framework 4.0 -> [4.0.0, 4.0.5]
        testVersionRange(SPRING_BOOT_4_0, 0, 5);
    }

    private void testVersionRange(SpringBootVersion baseVersion, int start, int end) {
        for (int i = start; i <= end; i++) {
            SpringBootVersion springVersion = valueOf(baseVersion.name() + "_" + i);
            Version version = springVersion.getVersion();
            assertNotNull(version);
            assertEquals(baseVersion.getMajor(), version.getMajor());
            assertEquals(baseVersion.getMinor(), version.getMinor());
            assertEquals(i, version.getPatch());
        }
    }

    @Test
    void testGetVersion() {
        for (SpringBootVersion springVersion : values()) {
            if (CURRENT.equals(springVersion)) {
                continue;
            }
            Version version = resolveVersion(springVersion.name());
            assertEquals(springVersion.getVersion(), version);
            assertTrue(springVersion.getVersion().eq(version));
        }
    }

    @Test
    void testOperators() {
        assertEquals(4, SPRING_BOOT_4_0_4.getMajor());
        assertEquals(0, SPRING_BOOT_4_0_4.getMinor());
        assertEquals(4, SPRING_BOOT_4_0_4.getPatch());

        assertTrue(SPRING_BOOT_4_0_4.eq(SPRING_BOOT_4_0_4));
        assertTrue(SPRING_BOOT_4_0_4.equals(SPRING_BOOT_4_0_4));
        assertTrue(SPRING_BOOT_4_0_4.gt(SPRING_BOOT_4_0_3));
        assertFalse(SPRING_BOOT_4_0_3.isGreaterThan(SPRING_BOOT_4_0_4));

        assertTrue(SPRING_BOOT_4_0_4.ge(SPRING_BOOT_4_0_4));
        assertTrue(SPRING_BOOT_4_0_4.ge(SPRING_BOOT_4_0_4));
        assertTrue(SPRING_BOOT_4_0_4.isGreaterOrEqual(SPRING_BOOT_4_0_4));
        assertFalse(SPRING_BOOT_4_0_3.isGreaterOrEqual(SPRING_BOOT_4_0_4));

        assertFalse(SPRING_BOOT_4_0_4.lt(SPRING_BOOT_4_0_3));
        assertTrue(SPRING_BOOT_4_0_3.lt(SPRING_BOOT_4_0_4));
        assertFalse(SPRING_BOOT_4_0_4.isLessThan(SPRING_BOOT_4_0_3));
        assertTrue(SPRING_BOOT_4_0_3.isLessThan(SPRING_BOOT_4_0_4));

        assertFalse(SPRING_BOOT_4_0_4.le(SPRING_BOOT_4_0_3));
        assertTrue(SPRING_BOOT_4_0_4.le(SPRING_BOOT_4_0_4));
        assertTrue(SPRING_BOOT_4_0_4.isLessOrEqual(SPRING_BOOT_4_0_4));
        assertTrue(SPRING_BOOT_4_0_4.isLessOrEqual(SPRING_BOOT_4_0_4));
    }
}