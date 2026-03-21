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

import static io.microsphere.spring.boot.util.SpringApplicationUtils.log;

/**
 * {@link OnceMainApplicationPreparedEventListener} for Logging with specified level
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see OnceMainApplicationPreparedEventListener
 * @since 1.0.0
 */
public class LoggingOnceMainApplicationPreparedEventListener extends OnceMainApplicationPreparedEventListener {

    /**
     * Constructs a new {@code LoggingOnceMainApplicationPreparedEventListener} with the
     * lowest precedence order.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   LoggingOnceMainApplicationPreparedEventListener listener =
     *       new LoggingOnceMainApplicationPreparedEventListener();
     * }</pre>
     */
    public LoggingOnceMainApplicationPreparedEventListener() {
        super.setOrder(LOWEST_PRECEDENCE);
    }

    /**
     * Logs the application-prepared event details for the main application context using
     * {@code SpringApplicationUtils#log}.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   // This method is invoked by the framework when the ApplicationPreparedEvent fires
     *   // for the main application context:
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
}