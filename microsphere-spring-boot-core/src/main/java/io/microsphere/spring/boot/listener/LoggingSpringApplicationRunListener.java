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
 */
public class LoggingSpringApplicationRunListener extends SpringApplicationRunListenerAdapter {

    public LoggingSpringApplicationRunListener(SpringApplication springApplication, String[] args) {
        super(springApplication, args);
    }

    @Override
    public void starting(ConfigurableBootstrapContext bootstrapContext) {
        super.starting(bootstrapContext);
        log(getSpringApplication(), getArgs(), "starting... : {}", bootstrapContext);
    }

    @Override
    public void starting() {
        super.starting();
        log(getSpringApplication(), getArgs(), "starting...");
    }

    @Override
    public void environmentPrepared(ConfigurableEnvironment environment) {
        super.environmentPrepared(environment);
        log(getSpringApplication(), getArgs(), "environmentPrepared : {}", environment);

    }

    @Override
    public void contextPrepared(ConfigurableApplicationContext context) {
        super.contextPrepared(context);
        log(getSpringApplication(), getArgs(), context, "contextPrepared : {}", context);

    }

    @Override
    public void contextLoaded(ConfigurableApplicationContext context) {
        super.contextLoaded(context);
        log(getSpringApplication(), getArgs(), context, "contextLoaded : {}", context);
    }

    @Override
    public void started(ConfigurableApplicationContext context) {
        super.started(context);
        log(getSpringApplication(), getArgs(), context, "started : {}", context);

    }

    @Override
    public void running(ConfigurableApplicationContext context) {
        super.running(context);
        log(getSpringApplication(), getArgs(), context, "running : {}", context);
    }

    @Override
    public void failed(ConfigurableApplicationContext context, Throwable exception) {
        super.failed(context, exception);
        log(getSpringApplication(), getArgs(), context, "failed : {}", exception);
    }
}