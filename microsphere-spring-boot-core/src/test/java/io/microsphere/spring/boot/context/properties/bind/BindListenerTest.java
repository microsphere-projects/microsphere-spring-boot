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
import org.springframework.boot.context.properties.bind.BindContext;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.source.ConfigurationPropertyName;

/**
 * {@link BindListener} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see BindListener
 * @since 1.0.0
 */
class BindListenerTest {

    private BindListener listener;

    @BeforeEach
    void setUp() {
        listener = new BindListener() {
            @Override
            public <T> void onStart(ConfigurationPropertyName name, Bindable<T> target, BindContext context) {
                BindListener.super.onStart(name, target, context);
            }

            @Override
            public void onSuccess(ConfigurationPropertyName name, Bindable<?> target, BindContext context, Object result) {
                BindListener.super.onSuccess(name, target, context, result);
            }

            @Override
            public void onCreate(ConfigurationPropertyName name, Bindable<?> target, BindContext context, Object result) {
                BindListener.super.onCreate(name, target, context, result);
            }

            @Override
            public void onFailure(ConfigurationPropertyName name, Bindable<?> target, BindContext context, Exception error) {
                BindListener.super.onFailure(name, target, context, error);
            }

            @Override
            public void onFinish(ConfigurationPropertyName name, Bindable<?> target, BindContext context, Object result) {
                BindListener.super.onFinish(name, target, context, result);
            }
        };
    }

    @Test
    void testOnStart() {
        this.listener.onStart(null, null, null);
    }

    @Test
    void testOnSuccess() {
        this.listener.onSuccess(null, null, null, null);
    }

    @Test
    void testOnCreate() {
        this.listener.onCreate(null, null, null, null);
    }

    @Test
    void testOnFailure() {
        this.listener.onFailure(null, null, null, null);
    }

    @Test
    void testOnFinish() {
        this.listener.onFinish(null, null, null, null);
    }
}