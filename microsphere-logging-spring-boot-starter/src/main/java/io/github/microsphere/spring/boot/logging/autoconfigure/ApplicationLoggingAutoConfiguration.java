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
package io.github.microsphere.spring.boot.logging.autoconfigure;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.event.ApplicationFailedEvent;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.EventListener;

import static org.springframework.util.StringUtils.arrayToDelimitedString;

/**
 * {@link SpringApplication} Logging Auto-Configuration
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public class ApplicationLoggingAutoConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(ApplicationLoggingAutoConfiguration.class);

    @EventListener(ApplicationStartedEvent.class)
    public void onApplicationStartedEvent(ApplicationStartedEvent event) {
        loggingApplicationEvent(event.getSpringApplication(), event.getArgs(), event.getApplicationContext(), null);
        registerLoggingUncaughtExceptionHandlerAsDefault();
    }

    @EventListener(ApplicationFailedEvent.class)
    public void onApplicationFailedEvent(ApplicationFailedEvent event) {
        loggingApplicationEvent(event.getSpringApplication(), event.getArgs(), event.getApplicationContext(), event.getException());
    }

    private void loggingApplicationEvent(SpringApplication springApplication, String[] args,
                                         ConfigurableApplicationContext context, Throwable exception) {
        String status = exception == null ? "Success" : "Failure";
        Class<?> mainClass = springApplication.getMainApplicationClass();
        String options = arrayToDelimitedString(args, " ");
        logger.debug("Spring Boot startup's status : {} , bootstrap: {} , args : {} , context : {}", status, mainClass, options, context, exception);
    }

    private void registerLoggingUncaughtExceptionHandlerAsDefault() {
        Thread.UncaughtExceptionHandler parent = Thread.getDefaultUncaughtExceptionHandler();
        LoggingUncaughtExceptionHandler loggingUncaughtExceptionHandler = new LoggingUncaughtExceptionHandler(parent);
        Thread.setDefaultUncaughtExceptionHandler(loggingUncaughtExceptionHandler);
    }

    static class LoggingUncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {

        private final Thread.UncaughtExceptionHandler parent;

        LoggingUncaughtExceptionHandler(Thread.UncaughtExceptionHandler parent) {
            this.parent = parent;
        }

        @Override
        public void uncaughtException(Thread t, Throwable e) {
            if (parent != null) {
                parent.uncaughtException(t, e);
            }
            logger.error("Thread[name : {}] uncaught exception : {}", t.getName(), e.getLocalizedMessage(), e);
        }
    }

}
