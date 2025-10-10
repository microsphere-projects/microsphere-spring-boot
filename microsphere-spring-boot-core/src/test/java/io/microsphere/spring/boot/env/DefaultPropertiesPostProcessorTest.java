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

package io.microsphere.spring.boot.env;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;

import static io.microsphere.collection.Maps.ofMap;
import static io.microsphere.collection.Sets.ofSet;
import static io.microsphere.util.ArrayUtils.ofArray;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.core.Ordered.LOWEST_PRECEDENCE;

/**
 * {@link DefaultPropertiesPostProcessor} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see DefaultPropertiesPostProcessor
 * @since 1.0.0
 */
class DefaultPropertiesPostProcessorTest {

    private DefaultPropertiesPostProcessor processor;

    @BeforeEach
    void setUp() {
        this.processor = new DefaultPropertiesPostProcessor() {
            @Override
            public void initializeResources(Set<String> defaultPropertiesResources) {
            }

            @Override
            public void postProcess(Map<String, Object> defaultProperties) {
                DefaultPropertiesPostProcessor.super.postProcess(defaultProperties);
            }

            @Override
            public int getOrder() {
                return DefaultPropertiesPostProcessor.super.getOrder();
            }
        };
    }

    @Test
    void testInitializeResources() {
        String[] resources = ofArray("a", "b", "c");
        Set<String> defaultPropertiesResources = ofSet(resources);
        this.processor.initializeResources(defaultPropertiesResources);
        assertEquals(ofSet(resources), defaultPropertiesResources);
    }

    @Test
    void testPostProcess() {
        Map<String, Object> defaultProperties = ofMap("a", "1", "b", "2", "c", "3");
        this.processor.postProcess(defaultProperties);
        assertEquals(ofMap("a", "1", "b", "2", "c", "3"), defaultProperties);
    }

    @Test
    void testGetOrder() {
        assertEquals(LOWEST_PRECEDENCE, this.processor.getOrder());
    }
}