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

package io.microsphere.spring.boot.actuate.endpoint;


import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.endpoint.annotation.DiscoveredEndpoint;
import org.springframework.boot.actuate.endpoint.web.ExposableWebEndpoint;
import org.springframework.boot.actuate.endpoint.web.WebOperation;

import java.util.Map;

import static io.microsphere.collection.Lists.ofList;
import static io.microsphere.spring.boot.actuate.endpoint.WebEndpoints.isExposableWebEndpoint;
import static io.microsphere.spring.boot.actuate.endpoint.WebEndpoints.isReadWebOperationCandidate;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.boot.actuate.endpoint.OperationType.WRITE;

/**
 * {@link WebEndpoints} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see WebEndpoints
 * @since 1.0.0
 */
class WebEndpointsTest {

    interface WebEndpoint extends ExposableWebEndpoint, DiscoveredEndpoint<WebOperation> {
    }

    @Test
    void testWebEndpointsOnWriteOperations() {
        WebEndpoint webEndpoint = mock(WebEndpoint.class);
        WebEndpoints webEndpoints = new WebEndpoints(() -> ofList(webEndpoint));
        WebOperation webOperation = mock(WebOperation.class);
        when(webOperation.getType()).thenReturn(WRITE);
        when(webEndpoint.getOperations()).thenReturn(ofList(webOperation));
        Map<String, Object> results = webEndpoints.invokeReadOperations();
        assertTrue(results.isEmpty());
    }

    @Test
    void testWebEndpointsOnNotDiscoveredEndpoints() {
        ExposableWebEndpoint webEndpoint = mock(ExposableWebEndpoint.class);
        WebEndpoints webEndpoints = new WebEndpoints(() -> ofList(webEndpoint));
        Map<String, Object> results = webEndpoints.invokeReadOperations();
        assertTrue(results.isEmpty());
    }

    @Test
    void testIsExposableWebEndpoint() {
        assertFalse(isExposableWebEndpoint(null));
    }

    @Test
    void testIsReadWebOperationCandidate() {
        assertFalse(isReadWebOperationCandidate(null));
    }
}