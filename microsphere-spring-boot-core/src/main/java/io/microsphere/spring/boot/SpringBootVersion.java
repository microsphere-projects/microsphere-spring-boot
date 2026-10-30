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
import static io.microsphere.util.Version.ofVersion;

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

    SPRING_BOOT_3_5_13,

    SPRING_BOOT_3_5_14,

    SPRING_BOOT_4_0,

    SPRING_BOOT_4_0_0,

    SPRING_BOOT_4_0_1,

    SPRING_BOOT_4_0_2,

    SPRING_BOOT_4_0_3,

    SPRING_BOOT_4_0_4,

    SPRING_BOOT_4_0_5,

    SPRING_BOOT_4_0_6,

    SPRING_BOOT_4_0_7,

    SPRING_BOOT_4_1,

    SPRING_BOOT_4_1_0,

    CURRENT(ofVersion(org.springframework.boot.SpringBootVersion.class));

    private final Version version;

    /**
     * Creates a new instance of {@link SpringBootVersion} with the resolved version from enumeration name.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   SpringBootVersion current = SpringBootVersion.CURRENT;
     *   System.out.println("Current version: " + current.getVersion());
     * }</pre>
     */
    SpringBootVersion() {
        this.version = resolveVersion(name());
    }

    /**
     * Creates a new instance of {@link SpringBootVersion} with the specified version.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   Version v = of("3.0.0");
     *   SpringBootVersion springBootVersion = new SpringBootVersion(v);
     *   System.out.println("Version: " + springBootVersion.getVersion());
     * }</pre>
     *
     * @param version the version to set, not null
     */
    SpringBootVersion(Version version) {
        this.version = version;
    }

    /**
     * Resolves a version string from the enumeration name by extracting the version part
     * after removing the "SPRING_BOOT_" or "CURRENT" prefix and converting underscores to dots.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   Version v1 = SpringBootVersion.resolveVersion("SPRING_BOOT_3_0_0"); // 3.0.0
     *   Version v2 = SpringBootVersion.resolveVersion("SPRING_BOOT_3_1_2"); // 3.1.2
     * }</pre>
     *
     * @param name the enumeration name (e.g., "SPRING_BOOT_3_0_0" or "CURRENT")
     * @return the resolved {@link Version} object
     */
    static Version resolveVersion(String name) {
        String version = name.substring(12).replace(UNDER_SCORE_CHAR, DOT_CHAR);
        return of(version);
    }

    /**
     * Returns the underlying {@link Version} object representing the Spring Boot version.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   SpringBootVersion version = SpringBootVersion.SPRING_BOOT_3_0_0;
     *   Version v = version.getVersion();
     *   System.out.println("Major: " + v.getMajor()); // 3
     *   System.out.println("Minor: " + v.getMinor()); // 0
     *   System.out.println("Patch: " + v.getPatch()); // 0
     * }</pre>
     *
     * @return the version object, never null
     */
    public Version getVersion() {
        return version;
    }

    /**
     * Returns the major version number (e.g., 3 for version 3.0.0).
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   SpringBootVersion v = SpringBootVersion.SPRING_BOOT_3_2_1;
     *   int major = v.getMajor(); // returns 3
     *   if (major >= 3) {
     *       // Use Spring Boot 3+ features
     *   }
     * }</pre>
     *
     * @return the major version number
     */
    public int getMajor() {
        return version.getMajor();
    }

    /**
     * Returns the minor version number (e.g., 2 for version 3.2.1).
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   SpringBootVersion v = SpringBootVersion.SPRING_BOOT_3_2_1;
     *   int minor = v.getMinor(); // returns 2
     * }</pre>
     *
     * @return the minor version number
     */
    public int getMinor() {
        return version.getMinor();
    }

    /**
     * Returns the patch/fix version number (e.g., 1 for version 3.2.1).
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   SpringBootVersion v = SpringBootVersion.SPRING_BOOT_3_2_1;
     *   int patch = v.getPatch(); // returns 1
     * }</pre>
     *
     * @return the patch version number
     */
    public int getPatch() {
        return version.getPatch();
    }

    /**
     * Checks if this version is greater than the given version using strict comparison (> operator).
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   SpringBootVersion v1 = SpringBootVersion.SPRING_BOOT_3_1_0;
     *   SpringBootVersion v2 = SpringBootVersion.SPRING_BOOT_3_0_0;
     *   boolean result = v1.gt(v2); // true, 3.1.0 > 3.0.0
     * }</pre>
     *
     * @param that the version to compare against, not null
     * @return true if this version is strictly greater than the given version
     */
    public boolean gt(SpringBootVersion that) {
        return version.gt(that.version);
    }

    /**
     * Checks if this version is greater than the given version using strict comparison (> operator).
     * This is an alias for {@link #gt(SpringBootVersion)}.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   SpringBootVersion v1 = SpringBootVersion.SPRING_BOOT_3_2_0;
     *   SpringBootVersion v2 = SpringBootVersion.SPRING_BOOT_3_2_0;
     *   boolean isGreaterThan = v1.isGreaterThan(v2); // false, 3.2.0 is not greater than 3.2.0
     * }</pre>
     *
     * @param that the version to compare against, not null
     * @return true if this version is strictly greater than the given version
     */
    public boolean isGreaterThan(SpringBootVersion that) {
        return version.isGreaterThan(that.version);
    }

    /**
     * Checks if this version is greater than or equal to the given version using non-strict comparison (>= operator).
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   SpringBootVersion v1 = SpringBootVersion.SPRING_BOOT_3_1_0;
     *   SpringBootVersion v2 = SpringBootVersion.SPRING_BOOT_3_0_0;
     *   boolean result = v1.ge(v2); // true, 3.1.0 >= 3.0.0
     *   boolean sameResult = v1.ge(v1); // true, 3.1.0 >= 3.1.0
     * }</pre>
     *
     * @param that the version to compare against, not null
     * @return true if this version is greater than or equal to the given version
     */
    public boolean ge(SpringBootVersion that) {
        return version.ge(that.version);
    }

    /**
     * Checks if this version is greater than or equal to the given version using non-strict comparison (>= operator).
     * This is an alias for {@link #ge(SpringBootVersion)}.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   SpringBootVersion current = SpringBootVersion.CURRENT;
     *   boolean supportsNewFeatures = current.isGreaterOrEqual(SpringBootVersion.SPRING_BOOT_3_0_0);
     * }</pre>
     *
     * @param that the version to compare against, not null
     * @return true if this version is greater than or equal to the given version
     */
    public boolean isGreaterOrEqual(SpringBootVersion that) {
        return version.isGreaterOrEqual(that.version);
    }

    /**
     * Checks if this version is less than the given version using strict comparison (< operator).
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   SpringBootVersion v1 = SpringBootVersion.SPRING_BOOT_3_0_0;
     *   SpringBootVersion v2 = SpringBootVersion.SPRING_BOOT_3_1_0;
     *   boolean result = v1.lt(v2); // true, 3.0.0 < 3.1.0
     * }</pre>
     *
     * @param that the version to compare against, not null
     * @return true if this version is strictly less than the given version
     */
    public boolean lt(SpringBootVersion that) {
        return version.lt(that.version);
    }

    /**
     * Checks if this version is less than the given version using strict comparison (< operator).
     * This is an alias for {@link #lt(SpringBootVersion)}.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   SpringBootVersion oldVersion = SpringBootVersion.SPRING_BOOT_3_0_0;
     *   boolean isOld = oldVersion.isLessThan(SpringBootVersion.SPRING_BOOT_3_1_0); // true
     * }</pre>
     *
     * @param that the version to compare against, not null
     * @return true if this version is strictly less than the given version
     */
    public boolean isLessThan(SpringBootVersion that) {
        return version.isLessThan(that.version);
    }

    /**
     * Checks if this version is less than or equal to the given version using non-strict comparison (<= operator).
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   SpringBootVersion v1 = SpringBootVersion.SPRING_BOOT_3_0_0;
     *   SpringBootVersion v2 = SpringBootVersion.SPRING_BOOT_3_1_0;
     *   boolean result1 = v1.le(v2); // true, 3.0.0 <= 3.1.0
     *   boolean result2 = v1.le(v1); // true, 3.0.0 <= 3.0.0
     * }</pre>
     *
     * @param that the version to compare against, not null
     * @return true if this version is less than or equal to the given version
     */
    public boolean le(SpringBootVersion that) {
        return version.le(that.version);
    }

    /**
     * Checks if this version is less than or equal to the given version using non-strict comparison (<= operator).
     * This is an alias for {@link #le(SpringBootVersion)}.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   SpringBootVersion requiredMin = SpringBootVersion.SPRING_BOOT_3_0_0;
     *   boolean meetsMinimum = SpringBootVersion.CURRENT.le(requiredMin);
     * }</pre>
     *
     * @param that the version to compare against, not null
     * @return true if this version is less than or equal to the given version
     */
    public boolean isLessOrEqual(SpringBootVersion that) {
        return version.isLessOrEqual(that.version);
    }

    /**
     * Checks if this version is equal to the given version using exact comparison (== operator).
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   SpringBootVersion v1 = SpringBootVersion.SPRING_BOOT_3_0_0;
     *   SpringBootVersion v2 = SpringBootVersion.SPRING_BOOT_3_0_0;
     *   boolean result = v1.eq(v2); // true, both are 3.0.0
     * }</pre>
     *
     * @param that the version to compare against, not null
     * @return true if this version is equal to the given version
     */
    public boolean eq(SpringBootVersion that) {
        return version.eq(that.version);
    }

    /**
     * Checks if this version is equal to the given version. This is an alias for {@link #eq(SpringBootVersion)}.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   SpringBootVersion target = SpringBootVersion.SPRING_BOOT_3_2_0;
     *   boolean matches = SpringBootVersion.SPRING_BOOT_3_2_0.equals(target); // true
     * }</pre>
     *
     * @param that the version to compare against, not null
     * @return true if this version is equal to the given version
     */
    public boolean equals(SpringBootVersion that) {
        return version.equals(that.version);
    }
}
