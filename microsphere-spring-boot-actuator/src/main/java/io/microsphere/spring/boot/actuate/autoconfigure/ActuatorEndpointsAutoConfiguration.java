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
     * Creates an {@link ArtifactsEndpoint} bean using the auto-detected bean class loader.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   // Auto-configured; access the endpoint via actuator:
     *   // GET /actuator/artifacts
     *
     *   @Autowired
     *   private ArtifactsEndpoint artifactsEndpoint;
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
     * Creates a {@link WebEndpoints} bean that aggregates all available web endpoints.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   // Auto-configured in web applications; access via actuator:
     *   // GET /actuator/webEndpoints
     *
     *   @Autowired
     *   private WebEndpoints webEndpoints;
     * }</pre>
     *
     * @param webEndpointsSupplier the supplier of web endpoint instances
     * @return a new {@link WebEndpoints} instance
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnWebApplication
    @ConditionalOnAvailableEndpoint
    public WebEndpoints webEndpoints(WebEndpointsSupplier webEndpointsSupplier) {
        return new WebEndpoints(webEndpointsSupplier);
    }

    @ConditionalOnConfigurationProcessorPresent
    static class ConfigurationProcessorConfiguration {

        /**
         * Creates a {@link ConfigurationMetadataReader} bean for reading Spring Boot
         * configuration metadata JSON files.
         *
         * <h3>Example Usage</h3>
         * <pre>{@code
         *   @Autowired
         *   private ConfigurationMetadataReader configurationMetadataReader;
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
         * Creates a {@link ConfigurationMetadataRepository} bean backed by the given
         * {@link ConfigurationMetadataReader}.
         *
         * <h3>Example Usage</h3>
         * <pre>{@code
         *   @Autowired
         *   private ConfigurationMetadataRepository configurationMetadataRepository;
         * }</pre>
         *
         * @param configurationMetadataReader the reader used to load configuration metadata
         * @return a new {@link ConfigurationMetadataRepository} instance
         */
        @Bean
        @ConditionalOnMissingBean
        public ConfigurationMetadataRepository configurationMetadataRepository(ConfigurationMetadataReader configurationMetadataReader) {
            return new ConfigurationMetadataRepository(configurationMetadataReader);
        }

        /**
         * Creates a {@link ConfigurationMetadataEndpoint} bean exposing configuration metadata
         * via the actuator.
         *
         * <h3>Example Usage</h3>
         * <pre>{@code
         *   // Access the endpoint via actuator:
         *   // GET /actuator/configMetadata
         * }</pre>
         *
         * @param configurationMetadataRepository the repository providing configuration metadata
         * @return a new {@link ConfigurationMetadataEndpoint} instance
         */
        @Bean
        @ConditionalOnMissingBean
        @ConditionalOnAvailableEndpoint
        public ConfigurationMetadataEndpoint configurationMetadataEndpoint(ConfigurationMetadataRepository configurationMetadataRepository) {
            return new ConfigurationMetadataEndpoint(configurationMetadataRepository);
        }

        /**
         * Creates a {@link ConfigurationPropertiesEndpoint} bean exposing configuration
         * properties via the actuator.
         *
         * <h3>Example Usage</h3>
         * <pre>{@code
         *   // Access the endpoint via actuator:
         *   // GET /actuator/configProperties
         * }</pre>
         *
         * @param configurationMetadataRepository the repository providing configuration metadata
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
     * Sets the bean {@link ClassLoader} used for artifact detection in
     * the {@link ArtifactsEndpoint}.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   // Automatically called by the Spring container for BeanClassLoaderAware beans.
     *   // No manual invocation is needed when using Spring's lifecycle management.
     * }</pre>
     *
     * @param classLoader the class loader provided by the Spring container
     */
    @Override
    public void setBeanClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }
}