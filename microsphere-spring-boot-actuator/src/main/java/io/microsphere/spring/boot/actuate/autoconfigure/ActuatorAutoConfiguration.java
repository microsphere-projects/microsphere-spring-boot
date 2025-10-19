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

import io.microsphere.annotation.ConfigurationProperty;
import io.microsphere.spring.boot.actuate.MonitoredThreadPoolTaskScheduler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import static io.microsphere.annotation.ConfigurationProperty.APPLICATION_SOURCE;
import static io.microsphere.spring.boot.actuate.constants.PropertyConstants.TASK_SCHEDULER_PROPERTY_NAME_PREFIX;
import static org.springframework.core.Ordered.LOWEST_PRECEDENCE;

/**
 * Actuator Spring Boot Auto-Configuration
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see EnableAutoConfiguration
 * @since 1.0.0
 */
@AutoConfigureOrder(LOWEST_PRECEDENCE)
public class ActuatorAutoConfiguration {

    /**
     * The default value of {@link ThreadPoolTaskScheduler#setPoolSize(int)} for Actuator : "1"
     */
    static final String DEFAULT_TASK_SCHEDULER_POOL_SIZE = "1";

    /**
     * The property name of {@link ThreadPoolTaskScheduler#setPoolSize(int)} for Actuator :
     * "microsphere.spring.boot.actuator.task-scheduler.pool-size"
     */
    @ConfigurationProperty(
            defaultValue = DEFAULT_TASK_SCHEDULER_POOL_SIZE,
            source = APPLICATION_SOURCE
    )
    static final String TASK_SCHEDULER_POOL_SIZE_PROPERTY_NAME = TASK_SCHEDULER_PROPERTY_NAME_PREFIX + "pool-size";

    /**
     * The {@link Value @Value} expression of {@link ThreadPoolTaskScheduler#setPoolSize(int)} for Actuator :
     * "${microsphere.spring.boot.actuator.task-scheduler.pool-size:1}"
     */
    static final String TASK_SCHEDULER_POOL_SIZE_VALUE_EXPRESSION = "${" + TASK_SCHEDULER_POOL_SIZE_PROPERTY_NAME + ":" + DEFAULT_TASK_SCHEDULER_POOL_SIZE + "}";

    /**
     * The default thread name prefix of {@link ThreadPoolTaskScheduler} for Actuator : "microsphere-spring-boot-actuator-task-"
     */
    static final String DEFAULT_TASK_SCHEDULER_THREAD_NAME_PREFIX = "microsphere-spring-boot-actuator-task-";

    /**
     * The property name of {@link ThreadPoolTaskScheduler#setThreadNamePrefix(String)} for Actuator :
     * "microsphere.spring.boot.actuator.task-scheduler.thread-name-prefix"
     */
    @ConfigurationProperty(
            defaultValue = DEFAULT_TASK_SCHEDULER_THREAD_NAME_PREFIX,
            source = APPLICATION_SOURCE
    )
    static final String TASK_SCHEDULER_THREAD_NAME_PREFIX_PROPERTY_NAME = TASK_SCHEDULER_PROPERTY_NAME_PREFIX + "thread-name-prefix";

    /**
     * The {@link Value @Value} expression of {@link ThreadPoolTaskScheduler#setThreadNamePrefix(String)} for Actuator :
     * "${microsphere.spring.boot.actuator.task-scheduler.thread-name-prefix:microsphere-spring-boot-actuator-task-}"
     */
    static final String TASK_SCHEDULER_THREAD_NAME_PREFIX_VALUE_EXPRESSION = "${" + TASK_SCHEDULER_THREAD_NAME_PREFIX_PROPERTY_NAME + ":" + DEFAULT_TASK_SCHEDULER_THREAD_NAME_PREFIX + "}";

    /**
     * The bean name of {@link ThreadPoolTaskScheduler} for Actuator : "actuatorTaskScheduler"
     */
    public static final String ACTUATOR_TASK_SCHEDULER_SERVICE_BEAN_NAME = "actuatorTaskScheduler";

    @Bean(name = ACTUATOR_TASK_SCHEDULER_SERVICE_BEAN_NAME, destroyMethod = "shutdown")
    public ThreadPoolTaskScheduler actuatorTaskScheduler(
            @Value(TASK_SCHEDULER_POOL_SIZE_VALUE_EXPRESSION) int poolSize,
            @Value(TASK_SCHEDULER_THREAD_NAME_PREFIX_VALUE_EXPRESSION) String threadNamePrefix) {
        MonitoredThreadPoolTaskScheduler threadPoolTaskScheduler = new MonitoredThreadPoolTaskScheduler();
        threadPoolTaskScheduler.setPoolSize(poolSize);
        threadPoolTaskScheduler.setDaemon(true);
        threadPoolTaskScheduler.setThreadNamePrefix(threadNamePrefix);
        return threadPoolTaskScheduler;
    }
}