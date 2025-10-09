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

package io.microsphere.spring.boot.context.properties.bind;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.context.properties.bind.BindContext;
import org.springframework.boot.context.properties.bind.BindHandler;
import org.springframework.boot.context.properties.bind.BindResult;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.ConfigurationPropertyName;
import org.springframework.mock.env.MockEnvironment;

import java.lang.reflect.Method;

import static io.microsphere.collection.Lists.ofList;
import static io.microsphere.reflect.MethodUtils.findMethod;
import static io.microsphere.reflect.MethodUtils.invokeMethod;
import static io.microsphere.spring.boot.context.properties.bind.util.BindHandlerUtils.createBindHandler;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.boot.context.properties.bind.Bindable.of;
import static org.springframework.boot.context.properties.source.ConfigurationPropertySources.get;

/**
 * {@link ListenableBindHandlerAdapter} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see ListenableBindHandlerAdapter
 * @since 1.0.0
 */
class ListenableBindHandlerAdapterTest {

    private Bindable<ServerProperties> bindable;

    private MockEnvironment environment;

    private Binder binder;

    @BeforeEach
    void setUp() {
        this.bindable = of(ServerProperties.class);
        this.environment = new MockEnvironment();
        this.environment.setProperty("server.port", "12345");
        this.binder = new Binder(get(this.environment));
    }

    @Test
    void test() {
        BindResult<ServerProperties> serverProperties = this.binder.bind("server", this.bindable, createBinder());
        assertTrue(serverProperties.isBound());
        assertServerProperties(serverProperties.get());
    }

    @Test
    void testOnCreate() {
        ListenableBindHandlerAdapter adapter = createBinder(new BindHandler() {
            @Override
            public <T> Bindable<T> onStart(ConfigurationPropertyName name, Bindable<T> target, BindContext context) {
                return null;
            }
        });

        Method method = findMethod(Binder.class, "bindOrCreate", String.class, Bindable.class, BindHandler.class);
        if (method == null) { // before Spring Boot 2.2.0
            assertNull(adapter.onCreate(ConfigurationPropertyName.EMPTY, this.bindable, null, null));
        } else {
            ServerProperties serverProperties = invokeMethod(this.binder, method, "server", this.bindable, adapter);
            assertNull(serverProperties.getPort());
        }
    }

    @Test
    void testOnFailure() {
        assertThrows(Exception.class, () -> this.binder.bind("", this.bindable, createBinder(createBindHandler(false, false))));
    }

    ListenableBindHandlerAdapter createBinder(BindListener... bindListeners) {
        return new ListenableBindHandlerAdapter(ofList(bindListeners));
    }

    ListenableBindHandlerAdapter createBinder(BindHandler parent, BindListener... bindListeners) {
        return new ListenableBindHandlerAdapter(parent, ofList(bindListeners));
    }

    void assertServerProperties(ServerProperties serverProperties) {
        assertEquals(12345, serverProperties.getPort());
    }
}