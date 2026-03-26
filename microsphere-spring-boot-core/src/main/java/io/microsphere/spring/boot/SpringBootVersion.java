/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License,
 Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 software
 * distributed under the License is distributed on an "AS IS" BASIS,

 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.microsphere.spring.boot;

import io.microsphere.util.Version;

import static io.microsphere.constants.SymbolConstants.DOT_CHAR;
import static io.microsphere.constants.SymbolConstants.UNDER_SCORE_CHAR;
import static io.microsphere.util.Version.of;

/**
 * The enumeration for the released Spring Boot versions since 3.0
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public enum SpringBootVersion {

    SPRING_BOOT_3_0,

    SPRING_BOOT_3_0_0,

    SPRING_BOOT_3_0_1,

    SPRING_BOOT_3_0_2,

    SPRING_BOOT_3_0_3,

    SPRING_BOOT_3_0_4,

    SPRING_BOOT_3_0_5,

    SPRING_BOOT_3_0_6,

    SPRING_BOOT_3_0_7,

    SPRING_BOOT_3_0_8,

    SPRING_BOOT_3_0_9,

    SPRING_BOOT_3_0_10,

    SPRING_BOOT_3_0_11,

    SPRING_BOOT_3_0_12,

    SPRING_BOOT_3_0_13,

    SPRING_BOOT_3_1,

    SPRING_BOOT_3_1_0,

    SPRING_BOOT_3_1_1,

    SPRING_BOOT_3_1_2,

    SPRING_BOOT_3_1_3,

    SPRING_BOOT_3_1_4,

    SPRING_BOOT_3_1_5,

    SPRING_BOOT_3_1_6,

    SPRING_BOOT_3_1_7,

    SPRING_BOOT_3_1_8,

    SPRING_BOOT_3_1_9,

    SPRING_BOOT_3_1_10,

    SPRING_BOOT_3_1_11,

    SPRING_BOOT_3_1_12,

    SPRING_BOOT_3_2,

    SPRING_BOOT_3_2_0,

    SPRING_BOOT_3_2_1,

    SPRING_BOOT_3_2_2,

    SPRING_BOOT_3_2_3,

    SPRING_BOOT_3_2_4,

    SPRING_BOOT_3_2_5,

    SPRING_BOOT_3_2_6,

    SPRING_BOOT_3_2_7,

    SPRING_BOOT_3_2_8,

    SPRING_BOOT_3_2_9,

    SPRING_BOOT_3_2_10,

    SPRING_BOOT_3_2_11,

    SPRING_BOOT_3_2_12,

    SPRING_BOOT_3_3,

    SPRING_BOOT_3_3_0,

    SPRING_BOOT_3_3_1,

    SPRING_BOOT_3_3_2,

    SPRING_BOOT_3_3_3,

    SPRING_BOOT_3_3_4,

    SPRING_BOOT_3_3_5,

    SPRING_BOOT_3_3_6,

    SPRING_BOOT_3_3_7,

    SPRING_BOOT_3_3_8,

    SPRING_BOOT_3_3_9,

    SPRING_BOOT_3_3_10,

    SPRING_BOOT_3_3_11,

    SPRING_BOOT_3_3_12,

    SPRING_BOOT_3_3_13,

    SPRING_BOOT_3_4,

    SPRING_BOOT_3_4_0,

    SPRING_BOOT_3_4_1,

    SPRING_BOOT_3_4_2,

    SPRING_BOOT_3_4_3,

    SPRING_BOOT_3_4_4,

    SPRING_BOOT_3_4_5,

    SPRING_BOOT_3_4_6,

    SPRING_BOOT_3_4_7,

    SPRING_BOOT_3_4_8,

    SPRING_BOOT_3_4_9,

    SPRING_BOOT_3_4_10,

    SPRING_BOOT_3_4_11,

    SPRING_BOOT_3_4_12,

    SPRING_BOOT_3_4_13,

    SPRING_BOOT_3_5,

    SPRING_BOOT_3_5_0,

    SPRING_BOOT_3_5_1,

    SPRING_BOOT_3_5_2,

    SPRING_BOOT_3_5_3,

    SPRING_BOOT_3_5_4,

    SPRING_BOOT_3_5_5,

    SPRING_BOOT_3_5_6,

    SPRING_BOOT_3_5_7,

    SPRING_BOOT_3_5_8,

    SPRING_BOOT_3_5_9,

    SPRING_BOOT_3_5_10,

    SPRING_BOOT_3_5_11,

    SPRING_BOOT_3_5_12,

    SPRING_BOOT_4_0,

    SPRING_BOOT_4_0_0,

    SPRING_BOOT_4_0_1,

    SPRING_BOOT_4_0_2,

    SPRING_BOOT_4_0_3,

    SPRING_BOOT_4_0_4,

    CURRENT(of(org.springframework.boot.SpringBootVersion.getVersion()));


    private final Version version;

    SpringBootVersion() {
        this.version = resolveVersion(name());
    }

    SpringBootVersion(Version version) {
        this.version = version;
    }

    static Version resolveVersion(String name) {
        String version = name.substring(12).replace(UNDER_SCORE_CHAR, DOT_CHAR);
        return of(version);
    }

    public Version getVersion() {
        return version;
    }

    public int getMajor() {
        return version.getMajor();
    }

    public int getMinor() {
        return version.getMinor();
    }

    public int getPatch() {
        return version.getPatch();
    }

    public boolean gt(SpringBootVersion that) {
        return version.gt(that.version);
    }

    public boolean isGreaterThan(SpringBootVersion that) {
        return version.isGreaterThan(that.version);
    }

    public boolean ge(SpringBootVersion that) {
        return version.ge(that.version);
    }

    public boolean isGreaterOrEqual(SpringBootVersion that) {
        return version.isGreaterOrEqual(that.version);
    }

    public boolean lt(SpringBootVersion that) {
        return version.lt(that.version);
    }

    public boolean isLessThan(SpringBootVersion that) {
        return version.isLessThan(that.version);
    }

    public boolean le(SpringBootVersion that) {
        return version.le(that.version);
    }

    public boolean isLessOrEqual(SpringBootVersion that) {
        return version.isLessOrEqual(that.version);
    }

    public boolean eq(SpringBootVersion that) {
        return version.eq(that.version);
    }

    public boolean equals(SpringBootVersion that) {
        return version.equals(that.version);
    }
}
