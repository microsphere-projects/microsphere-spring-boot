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
import io.microsphere.spring.boot.actuate.endpoint.WebEndpoints;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.boot.actuate.autoconfigure.endpoint.condition.ConditionalOnAvailableEndpoint;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.web.PathMappedEndpoints;
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

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnAvailableEndpoint
    public ArtifactsEndpoint artifactsEndpoint() {
        return new ArtifactsEndpoint(classLoader);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnWebApplication
    @ConditionalOnAvailableEndpoint
    public WebEndpoints webEndpoints(WebEndpointsSupplier webEndpointsSupplier) {
        return new WebEndpoints(webEndpointsSupplier);
    }

    @ConditionalOnConfigurationProcessorPresent
    static class ConfigurationProcessorConfiguration implements BeanClassLoaderAware {

        private ClassLoader classLoader;

        @Bean
        @ConditionalOnMissingBean
        @ConditionalOnAvailableEndpoint
        public ConfigurationMetadataEndpoint configurationMetadataEndpoint() {
            return new ConfigurationMetadataEndpoint(classLoader);
        }

        @Override
        public void setBeanClassLoader(ClassLoader classLoader) {
            this.classLoader = classLoader;
        }
    }

    @Override
    public void setBeanClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }
}
