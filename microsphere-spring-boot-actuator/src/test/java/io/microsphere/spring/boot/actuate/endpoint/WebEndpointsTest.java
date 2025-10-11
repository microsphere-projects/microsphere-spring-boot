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
import org.springframework.boot.actuate.endpoint.EndpointId;
import org.springframework.boot.actuate.endpoint.InvocationContext;
import org.springframework.boot.actuate.endpoint.OperationType;
import org.springframework.boot.actuate.endpoint.web.ExposableWebEndpoint;
import org.springframework.boot.actuate.endpoint.web.WebOperation;
import org.springframework.boot.actuate.endpoint.web.WebOperationRequestPredicate;

import java.util.Collection;

import static io.microsphere.spring.boot.actuate.endpoint.WebEndpoints.isExposableWebEndpoint;
import static io.microsphere.spring.boot.actuate.endpoint.WebEndpoints.isReadWebOperationCandidate;
import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.boot.actuate.endpoint.OperationType.DELETE;
import static org.springframework.boot.actuate.endpoint.OperationType.READ;
import static org.springframework.boot.actuate.endpoint.OperationType.WRITE;

/**
 * {@link WebEndpoints} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see WebEndpoints
 * @since 1.0.0
 */
class WebEndpointsTest {

    @Test
    void testIsExposableWebEndpoint() {
        assertFalse(isExposableWebEndpoint(new ExposableWebEndpointImpl()));
    }

    @Test
    void testIsReadWebOperationCandidate() {
        assertFalse(isReadWebOperationCandidate(new WebOperationImpl(READ)));
        assertFalse(isReadWebOperationCandidate(new WebOperationImpl(WRITE)));
        assertFalse(isReadWebOperationCandidate(new WebOperationImpl(DELETE)));
    }


    static class ExposableWebEndpointImpl implements ExposableWebEndpoint {

        @Override
        public EndpointId getEndpointId() {
            return null;
        }

        @Override
        public boolean isEnableByDefault() {
            return false;
        }

        @Override
        public Collection<WebOperation> getOperations() {
            return emptyList();
        }

        @Override
        public String getRootPath() {
            return "";
        }
    }

    static class WebOperationImpl implements WebOperation {

        private final OperationType type;

        WebOperationImpl(OperationType type) {
            this.type = type;
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

        @Override
        public OperationType getType() {
            return type;
        }

        @Override
        public Object invoke(InvocationContext context) {
            return null;
        }
    }
}