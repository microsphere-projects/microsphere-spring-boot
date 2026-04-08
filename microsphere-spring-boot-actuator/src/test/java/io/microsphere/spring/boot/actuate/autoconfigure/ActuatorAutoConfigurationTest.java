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

import org.junit.jupiter.api.ClassOrderer.OrderAnnotation;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestClassOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import static io.microsphere.spring.boot.actuate.autoconfigure.ActuatorAutoConfiguration.ACTUATOR_TASK_SCHEDULER_SERVICE_BEAN_NAME;
import static io.microsphere.spring.boot.actuate.autoconfigure.ActuatorAutoConfiguration.DEFAULT_TASK_SCHEDULER_POOL_SIZE;
import static io.microsphere.spring.boot.actuate.autoconfigure.ActuatorAutoConfiguration.TASK_SCHEDULER_POOL_SIZE_PROPERTY_NAME;
import static io.microsphere.spring.boot.actuate.autoconfigure.ActuatorAutoConfiguration.TASK_SCHEDULER_THREAD_NAME_PREFIX_PROPERTY_NAME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.NONE;

/**
 * {@link ActuatorAutoConfiguration} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see ActuatorAutoConfiguration
 * @since 1.0.0
 */
@TestClassOrder(OrderAnnotation.class)
class ActuatorAutoConfigurationTest {

    @Test
    void testConstants() {
        assertEquals("1", DEFAULT_TASK_SCHEDULER_POOL_SIZE);
        assertEquals("microsphere.spring.boot.actuator.task-scheduler.pool-size", TASK_SCHEDULER_POOL_SIZE_PROPERTY_NAME);
        assertEquals("microsphere.spring.boot.actuator.task-scheduler.thread-name-prefix", TASK_SCHEDULER_THREAD_NAME_PREFIX_PROPERTY_NAME);
    }

    @Order(1)
    @Nested
    @DisplayName("test when the 'MeterRegistry' Bean is present")
    @SpringBootTest(
            webEnvironment = NONE,
            classes = {
                    MeterRegistryPresent.class
            }, properties = {
            "microsphere.spring.boot.actuator.task-scheduler.pool-size=2",
            "microsphere.spring.boot.actuator.task-scheduler.thread-name-prefix=my-prefix",

    })
    @EnableAutoConfiguration
    class MeterRegistryPresent {

        @Autowired
        @Qualifier(ACTUATOR_TASK_SCHEDULER_SERVICE_BEAN_NAME)
        private ThreadPoolTaskScheduler actuatorTaskScheduler;

        @Value("${microsphere.spring.boot.actuator.task-scheduler.pool-size}")
        private int poolSize;

        @Value("${microsphere.spring.boot.actuator.task-scheduler.thread-name-prefix}")
        private String prefix;

        @Test
        void testActuatorTaskScheduler() {
            assertEquals(2, poolSize);
            assertEquals("my-prefix", prefix);
            assertNotNull(actuatorTaskScheduler, "Actuator task scheduler should not be null");
            assertEquals(prefix, actuatorTaskScheduler.getThreadNamePrefix(), "Thread name prefix should match");
            assertEquals(0, actuatorTaskScheduler.getPoolSize(), "Pool size should match");
            actuatorTaskScheduler.execute(() -> {
            });
            assertEquals(1, actuatorTaskScheduler.getPoolSize(), "Pool size should match");
            for (int i = 0; i < 9; i++) {
                actuatorTaskScheduler.execute(() -> {
                });
            }
            assertEquals(poolSize, actuatorTaskScheduler.getPoolSize(), "Pool size should match");
        }
    }

    @Order(2)
    @Nested
    @DisplayName("test when the 'MeterRegistry' Bean is absent")
    @SpringBootTest(
            webEnvironment = NONE,
            classes = {
                    MeterRegistryAbsent.class
            }, properties = {
            // Spring Boot [2.x,3.x]
            "microsphere.autoconfigure.exclude[0]=org.springframework.boot.actuate.autoconfigure.metrics.MetricsAutoConfiguration",
            "microsphere.autoconfigure.exclude[1]=org.springframework.boot.actuate.autoconfigure.metrics.CompositeMeterRegistryAutoConfiguration",
            // Spring Boot 4.x+
            "microsphere.autoconfigure.exclude[2]=org.springframework.boot.micrometer.metrics.autoconfigure.MetricsAutoConfiguration",
            "microsphere.autoconfigure.exclude[3]=org.springframework.boot.micrometer.metrics.autoconfigure.CompositeMeterRegistryAutoConfiguration",
    })
    @EnableAutoConfiguration
    class MeterRegistryAbsent {

        @Autowired(required = false)
        @Qualifier(ACTUATOR_TASK_SCHEDULER_SERVICE_BEAN_NAME)
        private ThreadPoolTaskScheduler actuatorTaskScheduler;

        @Test
        void testActuatorTaskScheduler() {
            assertNull(actuatorTaskScheduler, "Actuator task scheduler should be null");
        }
    }
}