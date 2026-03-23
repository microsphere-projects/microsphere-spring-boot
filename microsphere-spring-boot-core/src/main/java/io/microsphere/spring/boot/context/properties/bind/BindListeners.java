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

    /**
     * Delegates the {@code onStart} event to all registered {@link BindListener}s.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   BindListeners listeners = new BindListeners(Arrays.asList(listener1, listener2));
     *   listeners.onStart(ConfigurationPropertyName.of("app"), Bindable.of(MyBean.class), context);
     * }</pre>
     *
     * @param name    the configuration property name
     * @param target  the bindable target
     * @param context the bind context
     */
    @Override
    public <T> void onStart(ConfigurationPropertyName name, Bindable<T> target, BindContext context) {
        iterate(listener -> listener.onStart(name, target, context));
    }

    /**
     * Delegates the {@code onSuccess} event to all registered {@link BindListener}s.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   BindListeners listeners = new BindListeners(Arrays.asList(listener1, listener2));
     *   listeners.onSuccess(ConfigurationPropertyName.of("app.name"),
     *       Bindable.of(String.class), context, "value");
     * }</pre>
     *
     * @param name    the configuration property name
     * @param target  the bindable target
     * @param context the bind context
     * @param result  the bound result value
     */
    @Override
    public void onSuccess(ConfigurationPropertyName name, Bindable<?> target, BindContext context, Object result) {
        iterate(listener -> listener.onSuccess(name, target, context, result));
    }

    /**
     * Delegates the {@code onCreate} event to all registered {@link BindListener}s.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   BindListeners listeners = new BindListeners(Arrays.asList(listener1, listener2));
     *   listeners.onCreate(ConfigurationPropertyName.of("app"),
     *       Bindable.of(MyBean.class), context, new MyBean());
     * }</pre>
     *
     * @param name    the configuration property name
     * @param target  the bindable target
     * @param context the bind context
     * @param result  the created object
     */
    @Override
    public void onCreate(ConfigurationPropertyName name, Bindable<?> target, BindContext context, Object result) {
        iterate(listener -> listener.onCreate(name, target, context, result));
    }

    /**
     * Delegates the {@code onFailure} event to all registered {@link BindListener}s.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   BindListeners listeners = new BindListeners(Arrays.asList(listener1, listener2));
     *   listeners.onFailure(ConfigurationPropertyName.of("app.name"),
     *       Bindable.of(String.class), context, new RuntimeException("bind error"));
     * }</pre>
     *
     * @param name    the configuration property name
     * @param target  the bindable target
     * @param context the bind context
     * @param error   the exception that occurred during binding
     */
    @Override
    public void onFailure(ConfigurationPropertyName name, Bindable<?> target, BindContext context, Exception error) {
        iterate(listener -> listener.onFailure(name, target, context, error));
    }

    /**
     * Delegates the {@code onFinish} event to all registered {@link BindListener}s.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   BindListeners listeners = new BindListeners(Arrays.asList(listener1, listener2));
     *   listeners.onFinish(ConfigurationPropertyName.of("app"),
     *       Bindable.of(MyBean.class), context, myBean);
     * }</pre>
     *
     * @param name    the configuration property name
     * @param target  the bindable target
     * @param context the bind context
     * @param result  the bound result value
     */
    @Override
    public void onFinish(ConfigurationPropertyName name, Bindable<?> target, BindContext context, Object result) {
        iterate(listener -> listener.onFinish(name, target, context, result));
    }

    private void iterate(Consumer<BindListener> listenerConsumer) {
        listeners.forEach(listenerConsumer);
    }
}