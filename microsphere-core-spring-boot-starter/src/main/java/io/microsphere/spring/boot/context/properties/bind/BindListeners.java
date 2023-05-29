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

import org.springframework.boot.context.properties.bind.BindContext;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.source.ConfigurationPropertyName;

import java.util.function.Consumer;

/**
 * Composite {@link BindListener}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
class BindListeners implements BindListener {

    private final Iterable<BindListener> listeners;

    BindListeners(Iterable<BindListener> listeners) {
        this.listeners = listeners;
    }

    @Override
    public <T> void onStart(ConfigurationPropertyName name, Bindable<T> target, BindContext context) {
        iterate(listener -> listener.onStart(name, target, context));
    }

    @Override
    public void onSuccess(ConfigurationPropertyName name, Bindable<?> target, BindContext context, Object result) {
        iterate(listener -> listener.onSuccess(name, target, context, result));
    }

    @Override
    public void onCreate(ConfigurationPropertyName name, Bindable<?> target, BindContext context, Object result) {
        iterate(listener -> listener.onCreate(name, target, context, result));
    }

    @Override
    public void onFailure(ConfigurationPropertyName name, Bindable<?> target, BindContext context, Exception error) {
        iterate(listener -> listener.onFailure(name, target, context, error));
    }

    @Override
    public void onFinish(ConfigurationPropertyName name, Bindable<?> target, BindContext context, Object result) {
        iterate(listener -> listener.onFinish(name, target, context, result));
    }

    private void iterate(Consumer<BindListener> listenerConsumer) {
        listeners.forEach(listenerConsumer);
    }
}
