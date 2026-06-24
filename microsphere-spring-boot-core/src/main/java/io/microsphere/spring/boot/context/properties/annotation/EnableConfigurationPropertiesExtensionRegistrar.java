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

package io.microsphere.spring.boot.context.properties.annotation;

import io.microsphere.spring.beans.BeanSource;
import io.microsphere.spring.boot.context.properties.ListenableConfigurationPropertiesBindHandlerAdvisor;
import io.microsphere.spring.boot.context.properties.bind.BindListener;
import io.microsphere.spring.boot.context.properties.bind.ConfigurationPropertiesBeanPropertyChangedEvent;
import io.microsphere.spring.boot.context.properties.bind.EventPublishingConfigurationPropertiesBeanPropertyChangedListener;
import io.microsphere.spring.context.annotation.AnnotatedBeanCapableImportBeanDefinitionRegistrar;
import io.microsphere.spring.core.annotation.ResolvablePlaceholderAnnotationAttributes;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.boot.context.properties.ConfigurationPropertiesBindHandlerAdvisor;
import org.springframework.boot.context.properties.bind.BindHandler;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;

import static io.microsphere.spring.beans.BeanSource.registerBeans;
import static io.microsphere.spring.beans.factory.support.BeanRegistrar.registerGenericBean;

/**
 * The {@link ImportBeanDefinitionRegistrar} for {@link EnableConfigurationPropertiesExtension @EnableConfigurationPropertiesExtension}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see EnableConfigurationPropertiesExtension
 * @see AnnotatedBeanCapableImportBeanDefinitionRegistrar
 * @see ListenableConfigurationPropertiesBindHandlerAdvisor
 * @see BindListener
 * @see EventPublishingConfigurationPropertiesBeanPropertyChangedListener
 * @see ConfigurationPropertiesBeanPropertyChangedEvent
 * @see ConfigurationPropertiesBindHandlerAdvisor
 * @see BindHandler
 * @see ImportBeanDefinitionRegistrar
 * @since 1.0.0
 */
class EnableConfigurationPropertiesExtensionRegistrar extends AnnotatedBeanCapableImportBeanDefinitionRegistrar<EnableConfigurationPropertiesExtension> {

    @Override
    protected void registerBeanDefinitions(AnnotationMetadata metadata, BeanDefinitionRegistry registry,
                                           BeanNameGenerator importBeanNameGenerator,
                                           ResolvablePlaceholderAnnotationAttributes<EnableConfigurationPropertiesExtension> annotationAttributes) {

        boolean adviseBindListener = annotationAttributes.getBoolean("adviseBindListener");
        BeanSource[] sources = (BeanSource[]) annotationAttributes.get("sources");

        if (adviseBindListener) {
            // register ListenableConfigurationPropertiesBindHandlerAdvisor
            registerGenericBean(registry, ListenableConfigurationPropertiesBindHandlerAdvisor.class);
            boolean publishEvents = annotationAttributes.getBoolean("publishEvents");
            if (publishEvents) {
                // register EventPublishingConfigurationPropertiesBeanPropertyChangedListener
                registerGenericBean(registry, EventPublishingConfigurationPropertiesBeanPropertyChangedListener.class);
            }
            // register BindListener Beans
            registerBindListenerBeans(registry, sources);
        }
        // register ConfigurationPropertiesBindHandlerAdvisor Beans
        registerConfigurationPropertiesBindHandlerAdvisorBeans(registry, sources);
    }

    private void registerBindListenerBeans(BeanDefinitionRegistry registry, BeanSource[] sources) {
        registerBeans(getBeanFactory(), registry, sources, BindListener.class);
    }

    private void registerConfigurationPropertiesBindHandlerAdvisorBeans(BeanDefinitionRegistry registry, BeanSource[] sources) {
        registerBeans(getBeanFactory(), registry, sources, ConfigurationPropertiesBindHandlerAdvisor.class);
    }
}