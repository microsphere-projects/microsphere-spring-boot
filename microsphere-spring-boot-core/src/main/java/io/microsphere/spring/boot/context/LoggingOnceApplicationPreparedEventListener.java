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

import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

import static io.microsphere.spring.boot.constants.PropertyConstants.DEFAULT_MICROSPHERE_SPRING_BOOT_LOGGING_LEVEL;
import static io.microsphere.spring.boot.util.SpringApplicationUtils.getLoggingLevel;
import static io.microsphere.spring.boot.util.SpringApplicationUtils.log;

/**
 * {@link OnceApplicationPreparedEventListener} class for logging with specified level.
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see OnceApplicationPreparedEventListener
 * @since 1.0.0
 */
public class LoggingOnceApplicationPreparedEventListener extends OnceApplicationPreparedEventListener {

    /**
     * Constructs a new {@code LoggingOnceApplicationPreparedEventListener} with the
     * lowest precedence order.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   LoggingOnceApplicationPreparedEventListener listener =
     *       new LoggingOnceApplicationPreparedEventListener();
     * }</pre>
     */
    public LoggingOnceApplicationPreparedEventListener() {
        super.setOrder(LOWEST_PRECEDENCE);
    }

    /**
     * Logs the application-prepared event details for the given context using
     * {@code SpringApplicationUtils#log}.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   // This method is invoked by the framework when the ApplicationPreparedEvent fires:
     *   listener.onApplicationEvent(springApplication, args, context);
     * }</pre>
     *
     * @param springApplication the Spring application instance
     * @param args              the command-line arguments
     * @param context           the configurable application context
     */
    @Override
    protected void onApplicationEvent(SpringApplication springApplication, String[] args, ConfigurableApplicationContext context) {
        log(springApplication, args, context, "onApplicationPreparedEvent");
    }

    /**
     * Determines whether this listener should be ignored based on the configured
     * logging level. Returns {@code true} when the logging level equals the default
     * (effectively disabling logging output).
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   boolean ignored = listener.isIgnored(springApplication, args, context);
     *   // returns true when the logging level is at the default level
     * }</pre>
     *
     * @param springApplication the Spring application instance
     * @param args              the command-line arguments
     * @param context           the configurable application context
     * @return {@code true} if the event should be ignored, {@code false} otherwise
     */
    @Override
    protected boolean isIgnored(SpringApplication springApplication, String[] args, ConfigurableApplicationContext context) {
        String level = getLoggingLevel(context);
        return DEFAULT_MICROSPHERE_SPRING_BOOT_LOGGING_LEVEL.equals(level);
    }
}