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

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * {@link ActuatorAutoConfiguration} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see ActuatorAutoConfiguration
 * @since 1.0.0
 */
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.NONE,
        classes = {
                ActuatorAutoConfigurationTest.class
        }, properties = {
        "microsphere.spring.boot.actuator.task-scheduler.pool-size=2",
        "microsphere.spring.boot.actuator.task-scheduler.thread-name-prefix=my-prefix",

})
@EnableAutoConfiguration
class ActuatorAutoConfigurationTest {

    @Autowired
    @SuppressWarnings("unchecked")
    private ThreadPoolTaskScheduler actuatorTaskScheduler;

    @Value("${microsphere.spring.boot.actuator.task-scheduler.pool-size}")
    private int poolSize;

    @Value("${microsphere.spring.boot.actuator.task-scheduler.thread-name-prefix}")
    private String prefix;

    @Test
    void testActuatorTaskScheduler() {
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
