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
import io.microsphere.spring.context.annotation.OverrideAnnotationAttributes;
import org.springframework.boot.context.properties.ConfigurationPropertiesBindHandlerAdvisor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.context.properties.bind.BindHandler;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.annotation.Import;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static io.microsphere.spring.beans.BeanSource.BEAN_FACTORY;
import static io.microsphere.spring.beans.BeanSource.JAVA_SERVICE_PROVIDER;
import static io.microsphere.spring.beans.BeanSource.SPRING_FACTORIES;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;


/**
 * An extension annotation of {@link EnableConfigurationProperties @EnableConfigurationProperties} that provides
 * advanced configuration properties binding features.
 * <p>
 * This annotation enables:
 * <ul>
 *     <li>Automatic advising of {@link BindListener} implementations during {@link Binder#bind(String, Class) binding}</li>
 *     <li>Event publishing for configuration properties bean property changes (e.g., {@link ConfigurationPropertiesBeanPropertyChangedEvent})</li>
 *     <li>Flexible bean source configuration for discovering extension components</li>
 * </ul>
 *
 * <h3>Example Usage</h3>
 * <pre>{@code
 * @Configuration
 * @EnableConfigurationPropertiesExtension(
 *     adviseBindListener = true,
 *     publishEvents = true,
 *     sources = {BeanSource.BEAN_FACTORY, BeanSource.SPRING_FACTORIES}
 * )
 * public class MyConfiguration {
 *
 *     @Bean
 *     public BindListener myBindListener() {
 *         return event -> {
 *             // Handle bind events
 *         };
 *     }
 * }
 * }</pre>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see EnableConfigurationProperties
 * @see OverrideAnnotationAttributes
 * @see ListenableConfigurationPropertiesBindHandlerAdvisor
 * @see BindListener
 * @see ConfigurationPropertiesBindHandlerAdvisor
 * @see BindHandler
 * @since 1.0.0
 */
@Target(TYPE)
@Retention(RUNTIME)
@Documented
@OverrideAnnotationAttributes
@Import(EnableConfigurationPropertiesExtensionRegistrar.class)
public @interface EnableConfigurationPropertiesExtension {

    /**
     * Indicate whether advise {@link BindListener} adapting on the lifecycle of {@link BindHandler} when
     * {@link EnableConfigurationProperties @EnableConfigurationProperties} {@link Binder#bind(String, Class) binds}.
     *
     * @return {@code true} as default
     * @see BindListener
     * @see ListenableConfigurationPropertiesBindHandlerAdvisor
     * @see ConfigurationPropertiesBindHandlerAdvisor
     * @see BindHandler
     */
    boolean adviseBindListener() default true;

    /**
     * Indicate whether publish events when {@link EnableConfigurationProperties @EnableConfigurationProperties}
     * {@link Binder#bind(String, Class) binds}, such as:
     * <ul>
     *  <li>{@link ConfigurationPropertiesBeanPropertyChangedEvent}</li>
     * </ul>
     * <p>
     * If {@link #adviseBindListener()} is {@code false}, the events will not be published.
     *
     * @return {@code true} as default
     * @see #adviseBindListener()
     * @see ConfigurationPropertiesBeanPropertyChangedEvent
     * @see EventPublishingConfigurationPropertiesBeanPropertyChangedListener
     * @see ListenableConfigurationPropertiesBindHandlerAdvisor
     */
    boolean publishEvents() default true;

    /**
     * Indicate the sources of beans from which the {@link EnableConfigurationProperties @EnableConfigurationProperties}
     * extension components are collected, such as:
     * <ul>
     *  <li>{@link ConfigurationPropertiesBindHandlerAdvisor}</li>
     *  <li>{@link BindListener}</li>
     * </ul>
     *
     * @return the default value is the array of
     * {@link BeanSource#BEAN_FACTORY}, {@link BeanSource#SPRING_FACTORIES} and {@link BeanSource#JAVA_SERVICE_PROVIDER}
     * @see BeanSource#BEAN_FACTORY
     * @see BeanSource#SPRING_FACTORIES
     * @see BeanSource#JAVA_SERVICE_PROVIDER
     */
    BeanSource[] sources() default {BEAN_FACTORY, SPRING_FACTORIES, JAVA_SERVICE_PROVIDER};
}