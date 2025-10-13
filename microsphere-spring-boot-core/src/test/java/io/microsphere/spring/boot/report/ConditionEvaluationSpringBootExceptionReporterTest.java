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

package io.microsphere.spring.boot.report;


import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.GenericApplicationContext;

import static io.microsphere.spring.boot.util.TestUtils.application;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * {@link ConditionEvaluationSpringBootExceptionReporter} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see ConditionEvaluationSpringBootExceptionReporter
 * @since 1.0.0
 */
class ConditionEvaluationSpringBootExceptionReporterTest {

    @Test
    void testReportException() {
        ConfigurableApplicationContext context = new GenericApplicationContext();
        ConditionEvaluationSpringBootExceptionReporter reporter = new ConditionEvaluationSpringBootExceptionReporter(context);
        reporter.reportException(new Throwable("For testing"));
    }

    @Test
    void testReportExceptionWithinSpringApplication() {
        SpringApplication springApplication = application();
        springApplication.addInitializers(context -> {
            throw new RuntimeException("For testing");
        });
        assertThrows(RuntimeException.class, springApplication::run);
    }
}