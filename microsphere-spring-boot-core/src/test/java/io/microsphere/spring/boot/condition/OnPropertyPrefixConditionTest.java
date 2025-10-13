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

package io.microsphere.spring.boot.condition;


import io.microsphere.spring.test.context.annotation.TestConditionContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.core.type.StandardMethodMetadata;
import org.springframework.mock.env.MockEnvironment;

import static io.microsphere.reflect.MethodUtils.findMethod;
import static io.microsphere.spring.boot.constants.PropertyConstants.MICROSPHERE_SPRING_BOOT_PROPERTY_NAME_PREFIX;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * {@link OnPropertyPrefixCondition} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see OnPropertyPrefixCondition
 * @since 1.0.0
 */
class OnPropertyPrefixConditionTest {

    private static final String propertyNamePrefix = MICROSPHERE_SPRING_BOOT_PROPERTY_NAME_PREFIX;

    private MockEnvironment environment;

    private TestConditionContext conditionContext;

    private AnnotatedTypeMetadata metadata;

    private OnPropertyPrefixCondition condition;

    @BeforeEach
    @ConditionalOnPropertyPrefix(value = propertyNamePrefix)
    void setUp() {
        this.environment = new MockEnvironment();
        ConfigurableApplicationContext context = new GenericApplicationContext();
        context.setEnvironment(this.environment);
        this.conditionContext = new TestConditionContext();
        this.conditionContext.setApplicationContext(context);
        this.metadata = new StandardMethodMetadata(findMethod(getClass(), "setUp"));
        this.condition = new OnPropertyPrefixCondition();
    }

    @Test
    void testGetMatchOutcomeOnMatch() {
        String propertyName = propertyNamePrefix + "key";
        this.environment.setProperty(propertyName, "value");
        testGetMatchOutcome(true);
    }

    @Test
    void testGetMatchOutcomeOnNoMatch() {
        String propertyName = "key";
        this.environment.setProperty(propertyName, "value");
        testGetMatchOutcome(false);
    }

    @Test
    void testGetMatchOutcomeWithoutProperty() {
        testGetMatchOutcome(false);
    }

    void testGetMatchOutcome(boolean matched) {
        ConditionOutcome outcome = this.condition.getMatchOutcome(conditionContext, metadata);
        assertEquals(matched, outcome.isMatch());
    }
}