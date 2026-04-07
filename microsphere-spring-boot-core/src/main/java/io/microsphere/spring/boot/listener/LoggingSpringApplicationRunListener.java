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

package io.microsphere.spring.boot.listener;

import io.microsphere.spring.boot.util.SpringApplicationUtils;
import org.springframework.boot.ConfigurableBootstrapContext;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringApplicationRunListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;

import static io.microsphere.spring.boot.util.SpringApplicationUtils.log;

/**
 * {@link SpringApplicationRunListener} class for logging based on {@link SpringApplicationRunListenerAdapter}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see SpringApplicationRunListenerAdapter
 * @see SpringApplicationRunListener
 * @see SpringApplicationUtils
 * @since 1.0.0
 * @deprecated
 */
@Deprecated(since = "Spring Boot 4.0", forRemoval = true)
public class LoggingSpringApplicationRunListener extends SpringApplicationRunListenerAdapter {

    /**
     * Construct a new {@link LoggingSpringApplicationRunListener}.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   // Typically instantiated by Spring Boot via spring.factories
     *   // io.microsphere.spring.boot.listener.LoggingSpringApplicationRunListener
     *   SpringApplication app = new SpringApplication(MyApplication.class);
     *   app.run(args);
     * }</pre>
     *
     * @param springApplication the {@link SpringApplication} instance
     * @param args              the command line arguments
     */
    public LoggingSpringApplicationRunListener(SpringApplication springApplication, String[] args) {
        super(springApplication, args);
    }

    /**
     * Logs the starting phase of the application with the given bootstrap context.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   // This method is called automatically by Spring Boot during the starting phase.
     *   // It logs: "starting... : {bootstrapContext}"
     * }</pre>
     *
     * @param bootstrapContext the {@link ConfigurableBootstrapContext}
     */
    @Override
    public void starting(ConfigurableBootstrapContext bootstrapContext) {
        super.starting(bootstrapContext);
        log(getSpringApplication(), getArgs(), "starting... : {}", bootstrapContext);
    }

    /**
     * Logs the starting phase of the application (legacy callback without bootstrap context).
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   // This method is called automatically by Spring Boot during the starting phase.
     *   // It logs: "starting..."
     * }</pre>
     */
    @Override
    public void starting() {
        super.starting();
        log(getSpringApplication(), getArgs(), "starting...");
    }

    /**
     * Logs that the {@link ConfigurableEnvironment} has been prepared.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   // This method is called automatically by Spring Boot after environment preparation.
     *   // It logs: "environmentPrepared : {environment}"
     * }</pre>
     *
     * @param environment the prepared {@link ConfigurableEnvironment}
     */
    @Override
    public void environmentPrepared(ConfigurableEnvironment environment) {
        super.environmentPrepared(environment);
        log(getSpringApplication(), getArgs(), "environmentPrepared : {}", environment);

    }

    /**
     * Logs that the {@link ConfigurableApplicationContext} has been prepared.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   // This method is called automatically by Spring Boot after context preparation.
     *   // It logs: "contextPrepared : {context}"
     * }</pre>
     *
     * @param context the prepared {@link ConfigurableApplicationContext}
     */
    @Override
    public void contextPrepared(ConfigurableApplicationContext context) {
        super.contextPrepared(context);
        log(getSpringApplication(), getArgs(), context, "contextPrepared : {}", context);

    }

    /**
     * Logs that the {@link ConfigurableApplicationContext} has been loaded with bean definitions.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   // This method is called automatically by Spring Boot after context loading.
     *   // It logs: "contextLoaded : {context}"
     * }</pre>
     *
     * @param context the loaded {@link ConfigurableApplicationContext}
     */
    @Override
    public void contextLoaded(ConfigurableApplicationContext context) {
        super.contextLoaded(context);
        log(getSpringApplication(), getArgs(), context, "contextLoaded : {}", context);
    }

    /**
     * Logs that the application context has been started.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   // This method is called automatically by Spring Boot after the context is started.
     *   // It logs: "started : {context}"
     * }</pre>
     *
     * @param context the started {@link ConfigurableApplicationContext}
     */
    @Override
    public void started(ConfigurableApplicationContext context) {
        super.started(context);
        log(getSpringApplication(), getArgs(), context, "started : {}", context);

    }

    /**
     * Logs that the application is fully running.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   // This method is called automatically by Spring Boot when the application is running.
     *   // It logs: "running : {context}"
     * }</pre>
     *
     * @param context the running {@link ConfigurableApplicationContext}
     */
    @Override
    public void running(ConfigurableApplicationContext context) {
        super.running(context);
        log(getSpringApplication(), getArgs(), context, "running : {}", context);
    }

    /**
     * Logs that the application has failed to start with the given exception.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   // This method is called automatically by Spring Boot when the application fails.
     *   // It logs: "failed : {exception}"
     * }</pre>
     *
     * @param context   the {@link ConfigurableApplicationContext}, may be {@code null}
     * @param exception the exception that caused the failure
     */
    @Override
    public void failed(ConfigurableApplicationContext context, Throwable exception) {
        super.failed(context, exception);
        log(getSpringApplication(), getArgs(), context, "failed : {}", exception);
    }
}