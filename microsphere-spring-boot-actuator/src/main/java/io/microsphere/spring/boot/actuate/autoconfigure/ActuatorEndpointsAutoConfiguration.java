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
package io.microsphere.spring.boot.actuate.autoconfigure;

import io.microsphere.spring.boot.actuate.condition.ConditionalOnConfigurationProcessorPresent;
import io.microsphere.spring.boot.actuate.endpoint.ArtifactsEndpoint;
import io.microsphere.spring.boot.actuate.endpoint.ConfigurationMetadataEndpoint;
import io.microsphere.spring.boot.actuate.endpoint.ConfigurationPropertiesEndpoint;
import io.microsphere.spring.boot.actuate.endpoint.WebEndpoints;
import io.microsphere.spring.boot.context.properties.metadata.ConfigurationMetadataReader;
import io.microsphere.spring.boot.env.config.metadata.ConfigurationMetadataRepository;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.boot.actuate.autoconfigure.endpoint.condition.ConditionalOnAvailableEndpoint;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.web.WebEndpointsSupplier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

/**
 * Actuator {@link Endpoint @Endpoint} Auto-Configuration class
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see Endpoint
 * @since 1.0.0
 */
@ConditionalOnClass(name = {
        "org.springframework.boot.actuate.endpoint.annotation.Endpoint"
})
@Import(value = {ActuatorEndpointsAutoConfiguration.ConfigurationProcessorConfiguration.class})
public class ActuatorEndpointsAutoConfiguration implements BeanClassLoaderAware {

    private ClassLoader classLoader;

    /**
     * Creates an {@link ArtifactsEndpoint} bean using the current class loader.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   // Auto-configured as a Spring bean; access via actuator endpoint "/actuator/artifacts".
     * }</pre>
     *
     * @return a new {@link ArtifactsEndpoint} instance
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnAvailableEndpoint
    public ArtifactsEndpoint artifactsEndpoint() {
        return new ArtifactsEndpoint(classLoader);
    }

    /**
     * Creates a {@link WebEndpoints} bean that aggregates all web endpoint read operations.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   // Auto-configured as a Spring bean; access via actuator endpoint "/actuator/webEndpoints".
     * }</pre>
     *
     * @param webEndpointsSupplier the supplier of {@link org.springframework.boot.actuate.endpoint.web.ExposableWebEndpoint} instances
     * @return a new {@link WebEndpoints} instance
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnWebApplication
    @ConditionalOnAvailableEndpoint
    public WebEndpoints webEndpoints(WebEndpointsSupplier webEndpointsSupplier) {
        return new WebEndpoints(webEndpointsSupplier);
    }

    /**
     * Configuration class that registers configuration-processor-related beans when the
     * Spring Boot Configuration Processor is present on the classpath.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   // Automatically imported by ActuatorEndpointsAutoConfiguration when
     *   // spring-boot-configuration-processor is on the classpath.
     * }</pre>
     */
    @ConditionalOnConfigurationProcessorPresent
    static class ConfigurationProcessorConfiguration {

        /**
         * Creates a {@link ConfigurationMetadataReader} bean for reading configuration metadata.
         *
         * <h3>Example Usage</h3>
         * <pre>{@code
         *   // Auto-configured; typically injected into ConfigurationMetadataRepository.
         *   ConfigurationMetadataReader reader = configurationMetadataReader();
         * }</pre>
         *
         * @return a new {@link ConfigurationMetadataReader} instance
         */
        @Bean
        @ConditionalOnMissingBean
        public ConfigurationMetadataReader configurationMetadataReader() {
            return new ConfigurationMetadataReader();
        }

        /**
         * Creates a {@link ConfigurationMetadataRepository} bean backed by the given reader.
         *
         * <h3>Example Usage</h3>
         * <pre>{@code
         *   // Auto-configured; provides metadata for configuration endpoints.
         *   ConfigurationMetadataRepository repo = configurationMetadataRepository(reader);
         * }</pre>
         *
         * @param configurationMetadataReader the {@link ConfigurationMetadataReader} to use
         * @return a new {@link ConfigurationMetadataRepository} instance
         */
        @Bean
        @ConditionalOnMissingBean
        public ConfigurationMetadataRepository configurationMetadataRepository(ConfigurationMetadataReader configurationMetadataReader) {
            return new ConfigurationMetadataRepository(configurationMetadataReader);
        }

        /**
         * Creates a {@link ConfigurationMetadataEndpoint} bean exposing metadata at an actuator endpoint.
         *
         * <h3>Example Usage</h3>
         * <pre>{@code
         *   // Auto-configured; access via actuator endpoint "/actuator/configMetadata".
         * }</pre>
         *
         * @param configurationMetadataRepository the {@link ConfigurationMetadataRepository} to use
         * @return a new {@link ConfigurationMetadataEndpoint} instance
         */
        @Bean
        @ConditionalOnMissingBean
        @ConditionalOnAvailableEndpoint
        public ConfigurationMetadataEndpoint configurationMetadataEndpoint(ConfigurationMetadataRepository configurationMetadataRepository) {
            return new ConfigurationMetadataEndpoint(configurationMetadataRepository);
        }

        /**
         * Creates a {@link ConfigurationPropertiesEndpoint} bean exposing configuration properties
         * at an actuator endpoint.
         *
         * <h3>Example Usage</h3>
         * <pre>{@code
         *   // Auto-configured; access via actuator endpoint "/actuator/configProperties".
         * }</pre>
         *
         * @param configurationMetadataRepository the {@link ConfigurationMetadataRepository} to use
         * @return a new {@link ConfigurationPropertiesEndpoint} instance
         */
        @Bean
        @ConditionalOnMissingBean
        @ConditionalOnAvailableEndpoint
        public ConfigurationPropertiesEndpoint configurationPropertiesEndpoint(ConfigurationMetadataRepository configurationMetadataRepository) {
            return new ConfigurationPropertiesEndpoint(configurationMetadataRepository);
        }
    }

    /**
     * Stores the bean {@link ClassLoader} for use when creating class-loader-aware endpoint beans.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   // Called automatically by the Spring container.
     *   config.setBeanClassLoader(Thread.currentThread().getContextClassLoader());
     * }</pre>
     *
     * @param classLoader the {@link ClassLoader} to set
     */
    @Override
    public void setBeanClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }
}