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

package io.microsphere.spring.boot.webflux.autoconfigure;


import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.util.Set;

import static io.microsphere.collection.Sets.ofSet;
import static java.util.Collections.emptySet;

/**
 * {@link WebFluxAutoConfiguration} Test with all disalbed components
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see WebFluxAutoConfiguration
 * @since 1.0.0
 */
@SpringBootTest(classes = {
        WebFluxAutoConfigurationAllDisabledTest.class
})
@TestPropertySource(
        properties = {
                "microsphere.spring.boot.webflux.enabled=false"
        }
)
class WebFluxAutoConfigurationAllDisabledTest extends AbstractWebFluxAutoConfigurationTest {

    @Override
    Set<Class<?>> getRegisteredComponentClasses() {
        return ofSet();
    }
}