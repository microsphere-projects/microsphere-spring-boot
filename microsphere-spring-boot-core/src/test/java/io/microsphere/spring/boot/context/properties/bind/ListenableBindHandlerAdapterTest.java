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

import static io.microsphere.collection.Lists.ofList;
import static io.microsphere.spring.boot.context.properties.bind.util.BindHandlerUtils.createBindHandler;
import static io.microsphere.spring.boot.util.TestUtils.assertServerPropertiesPort;
import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
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
        assertServerPropertiesPort(this.environment, serverProperties.get());
    }

    @Test
    void testOnCreate() {
        ServerProperties serverProperties = this.binder.bindOrCreate("server", this.bindable, createBinder(new BindHandler() {
            @Override
            public <T> Bindable<T> onStart(ConfigurationPropertyName name, Bindable<T> target, BindContext context) {
                return null;
            }
        }));
        assertNull(serverProperties.getPort());
    }

    @Test
    void testOnFailureWithoutThrowingException() throws Exception {
        ListenableBindHandlerAdapter adapter = new ListenableBindHandlerAdapter(new BindHandler() {
            @Override
            public Object onFailure(ConfigurationPropertyName name, Bindable<?> target, BindContext context, Exception error) throws Exception {
                return error;
            }
        }, emptyList());
        ConfigurationPropertyName name = ConfigurationPropertyName.of("server");
        BindContext context = null;
        Exception error = new Exception("For testing...");
        assertSame(error, adapter.onFailure(name, bindable, context, error));
    }

    @Test
    void testOnFailure() {
        assertThrows(Exception.class, () -> this.binder.bind("", this.bindable, createBinder(createBindHandler(false, false))));
    }

    BindHandler createBinder(BindListener... bindListeners) {
        return new ListenableBindHandlerAdapter(ofList(bindListeners));
    }

    BindHandler createBinder(BindHandler parent, BindListener... bindListeners) {
        return new ListenableBindHandlerAdapter(parent, ofList(bindListeners));
    }
}