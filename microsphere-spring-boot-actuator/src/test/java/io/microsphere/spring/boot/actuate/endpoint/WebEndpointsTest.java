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
import org.springframework.boot.actuate.endpoint.annotation.AbstractDiscoveredOperation;
import org.springframework.boot.actuate.endpoint.annotation.DiscoveredEndpoint;
import org.springframework.boot.actuate.endpoint.annotation.DiscoveredOperationMethod;
import org.springframework.boot.actuate.endpoint.invoke.OperationInvoker;
import org.springframework.boot.actuate.endpoint.web.ExposableWebEndpoint;
import org.springframework.boot.actuate.endpoint.web.WebOperation;
import org.springframework.boot.actuate.endpoint.web.WebOperationRequestPredicate;
import org.springframework.core.annotation.AnnotationAttributes;

import java.lang.reflect.Method;
import java.util.Map;

import static io.microsphere.collection.Lists.ofList;
import static io.microsphere.reflect.MethodUtils.findMethod;
import static io.microsphere.spring.boot.actuate.endpoint.WebEndpoints.isExposableWebEndpoint;
import static io.microsphere.spring.boot.actuate.endpoint.WebEndpoints.isReadWebOperationCandidate;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.boot.actuate.endpoint.OperationType.WRITE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * {@link WebEndpoints} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see WebEndpoints
 * @since 1.0.0
 */
class WebEndpointsTest {

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
    void testIsReadWebOperationCandidateOnWriteOperation() {
        Method method = findMethod(getClass(), "testIsReadWebOperationCandidateOnWriteOperation");
        AnnotationAttributes annotationAttributes = new AnnotationAttributes();
        annotationAttributes.put("produces", APPLICATION_JSON_VALUE);
        DiscoveredOperationMethod operationMethod = new DiscoveredOperationMethod(method, WRITE, annotationAttributes);
        DiscoveredOperationImpl discoveredOperation = new DiscoveredOperationImpl(operationMethod, null);
        assertFalse(isReadWebOperationCandidate(discoveredOperation));
    }

    @Test
    void testIsReadWebOperationCandidateOnNull() {
        assertFalse(isReadWebOperationCandidate(null));
    }


    interface WebEndpoint extends ExposableWebEndpoint, DiscoveredEndpoint<WebOperation> {
    }

    class DiscoveredOperationImpl extends AbstractDiscoveredOperation implements WebOperation {

        public DiscoveredOperationImpl(DiscoveredOperationMethod operationMethod, OperationInvoker invoker) {
            super(operationMethod, invoker);
        }

        @Override
        public String getId() {
            return "";
        }

        @Override
        public boolean isBlocking() {
            return false;
        }

        @Override
        public WebOperationRequestPredicate getRequestPredicate() {
            return null;
        }
    }
}