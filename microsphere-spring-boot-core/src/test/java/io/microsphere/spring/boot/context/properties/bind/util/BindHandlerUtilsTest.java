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

package io.microsphere.spring.boot.context.properties.bind.util;


import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.bind.handler.IgnoreErrorsBindHandler;
import org.springframework.boot.context.properties.bind.handler.NoUnboundElementsBindHandler;

import static io.microsphere.spring.boot.context.properties.bind.util.BindHandlerUtils.createBindHandler;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.boot.context.properties.bind.BindHandler.DEFAULT;

/**
 * {@link BindHandlerUtils} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see BindHandlerUtils
 * @since 1.0.0
 */
class BindHandlerUtilsTest {

    @Test
    void testCreateBindHandler() {
        assertTrue(createBindHandler(false, false) instanceof NoUnboundElementsBindHandler);
        assertSame(DEFAULT, createBindHandler(true, false));
        assertTrue(createBindHandler(true, true) instanceof IgnoreErrorsBindHandler);
        assertTrue(createBindHandler(false, true) instanceof NoUnboundElementsBindHandler);
    }
}