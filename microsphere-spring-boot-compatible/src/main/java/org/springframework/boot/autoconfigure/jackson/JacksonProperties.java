/*
 * Copyright 2012-present the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.boot.autoconfigure.jackson;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Locale;
import java.util.TimeZone;

/**
 * Configuration properties to configure Jackson.
 *
 * @author Andy Wilkinson
 * @author Marcel Overdijk
 * @author Johannes Edmeier
 * @author Eddú Meléndez
 * @since 1.2.0
 */
@ConfigurationProperties("spring.jackson")
public class JacksonProperties {

    /**
     * Date format string or a fully-qualified date format class name. For instance,
     * 'yyyy-MM-dd HH:mm:ss'.
     */
    private String dateFormat;

    /**
     * One of the constants on Jackson's PropertyNamingStrategies. Can also be a
     * fully-qualified class name of a PropertyNamingStrategy implementation.
     */
    private String propertyNamingStrategy;

    /**
     * Global default setting (if any) for leniency.
     */
    private Boolean defaultLeniency;

    /**
     * Strategy to use to auto-detect constructor, and in particular behavior with
     * single-argument constructors.
     */
    private ConstructorDetectorStrategy constructorDetector;

    /**
     * Time zone used when formatting dates. For instance, "America/Los_Angeles" or
     * "GMT+10".
     */
    private TimeZone timeZone;

    /**
     * Locale used for formatting.
     */
    private Locale locale;

    public String getDateFormat() {
        return this.dateFormat;
    }

    public void setDateFormat(String dateFormat) {
        this.dateFormat = dateFormat;
    }

    public String getPropertyNamingStrategy() {
        return this.propertyNamingStrategy;
    }

    public void setPropertyNamingStrategy(String propertyNamingStrategy) {
        this.propertyNamingStrategy = propertyNamingStrategy;
    }

    public Boolean getDefaultLeniency() {
        return this.defaultLeniency;
    }

    public void setDefaultLeniency(Boolean defaultLeniency) {
        this.defaultLeniency = defaultLeniency;
    }

    public ConstructorDetectorStrategy getConstructorDetector() {
        return this.constructorDetector;
    }

    public void setConstructorDetector(ConstructorDetectorStrategy constructorDetector) {
        this.constructorDetector = constructorDetector;
    }

    public TimeZone getTimeZone() {
        return this.timeZone;
    }

    public void setTimeZone(TimeZone timeZone) {
        this.timeZone = timeZone;
    }

    public Locale getLocale() {
        return this.locale;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    public enum ConstructorDetectorStrategy {

        /**
         * Use heuristics to see if "properties" mode is to be used.
         */
        DEFAULT,

        /**
         * Assume "properties" mode if not explicitly annotated otherwise.
         */
        USE_PROPERTIES_BASED,

        /**
         * Assume "delegating" mode if not explicitly annotated otherwise.
         */
        USE_DELEGATING,

        /**
         * Refuse to decide implicit mode and instead throw an InvalidDefinitionException
         * for ambiguous cases.
         */
        EXPLICIT_ONLY

    }
}