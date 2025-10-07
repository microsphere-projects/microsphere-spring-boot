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

package io.microsphere.spring.boot.context;

import io.microsphere.logging.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;

import static io.microsphere.logging.LoggerFactory.getLogger;
import static io.microsphere.spring.boot.constants.PropertyConstants.DEFAULT_MICROSPHERE_SPRING_BOOT_LOGGING_LEVEL;
import static io.microsphere.spring.boot.constants.PropertyConstants.MICROSPHERE_SPRING_BOOT_LOGGING_LEVEL_PROPERTY_NAME;
import static io.microsphere.util.ArrayUtils.arrayToString;
import static io.microsphere.util.ArrayUtils.ofArray;
import static java.util.Locale.ENGLISH;

/**
 * {@link OnceApplicationPreparedEventListener} class for logging with debug level.
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see OnceApplicationPreparedEventListener
 * @since 1.0.0
 */
public class LoggingOnceApplicationPreparedEventListener extends OnceApplicationPreparedEventListener {

    private static final Logger logger = getLogger(LoggingOnceApplicationPreparedEventListener.class);

    public LoggingOnceApplicationPreparedEventListener() {
        super.setOrder(LOWEST_PRECEDENCE);
    }

    @Override
    protected void onApplicationEvent(SpringApplication springApplication, String[] args, ConfigurableApplicationContext context) {
        log(springApplication, args, context);
    }

    @Override
    protected boolean isIgnored(SpringApplication springApplication, String[] args, ConfigurableApplicationContext context) {
        String level = getLoggingLevel(context);
        return DEFAULT_MICROSPHERE_SPRING_BOOT_LOGGING_LEVEL.equals(level);
    }

    String getLoggingLevel(ConfigurableApplicationContext context) {
        Environment environment = context.getEnvironment();
        String level = environment.getProperty(MICROSPHERE_SPRING_BOOT_LOGGING_LEVEL_PROPERTY_NAME, DEFAULT_MICROSPHERE_SPRING_BOOT_LOGGING_LEVEL);
        return level.toLowerCase(ENGLISH);
    }

    void log(SpringApplication springApplication, String[] args, ConfigurableApplicationContext context) {
        String messagePattern =
                "SpringApplication: " +
                        "    main class : '{}' ," +
                        "    web type : '{}' ," +
                        "    sources : {} ," +
                        "    all sources : {} ," +
                        "    additional profiles : {} ," +
                        "    initializers : {} ," +
                        "    listeners : {}," +
                        "    args : {}," +
                        "    context id : '{}'";

        Object[] arguments = ofArray(springApplication.getMainApplicationClass(),
                springApplication.getWebApplicationType(),
                springApplication.getSources(),
                springApplication.getAllSources(),
                springApplication.getAdditionalProfiles(),
                springApplication.getInitializers(),
                springApplication.getListeners(),
                arrayToString(args),
                context.getId());

        String level = getLoggingLevel(context);

        switch (level) {
            case "trace": {
                logger.trace(messagePattern, arguments);
                break;
            }
            case "debug": {
                logger.debug(messagePattern, arguments);
                break;
            }
            case "info": {
                logger.info(messagePattern, arguments);
                break;
            }
            case "warn": {
                logger.warn(messagePattern, arguments);
                break;
            }
            case "error": {
                logger.error(messagePattern, arguments);
                break;
            }
        }
    }

}
