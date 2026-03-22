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

import io.microsphere.spring.boot.context.properties.ListenableConfigurationPropertiesBindHandlerAdvisor;
import org.springframework.boot.context.properties.bind.AbstractBindHandler;
import org.springframework.boot.context.properties.bind.BindContext;
import org.springframework.boot.context.properties.bind.BindHandler;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.ConfigurationPropertyName;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;

import static io.microsphere.invoke.MethodHandleUtils.LookupMode.TRUSTED;
import static io.microsphere.invoke.MethodHandleUtils.lookup;
import static io.microsphere.invoke.MethodHandlesLookupUtils.NOT_FOUND_METHOD_HANDLE;
import static io.microsphere.lang.function.ThrowableSupplier.execute;
import static java.lang.invoke.MethodType.methodType;

/**
 * Listable {@link BindHandler} Adapter
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see ListenableConfigurationPropertiesBindHandlerAdvisor
 * @see BindHandler
 * @see Binder
 * @see Bindable
 * @since 1.0.0
 */
public class ListenableBindHandlerAdapter extends AbstractBindHandler {

    /**
     * The method {@link BindHandler#onCreate(ConfigurationPropertyName, Bindable, BindContext, Object)} was introduced
     * in Spring Boot 2.2.2
     */
    static final MethodHandle onCreateMethodHandle;

    static {
        Lookup lookup = lookup(AbstractBindHandler.class, TRUSTED);
        MethodType methodType = methodType(Object.class, ConfigurationPropertyName.class, Bindable.class, BindContext.class, Object.class);
        onCreateMethodHandle = execute(
                () -> lookup.findSpecial(AbstractBindHandler.class, "onCreate", methodType, ListenableBindHandlerAdapter.class),
                e -> null
        );
    }

    private final BindListeners bindHandlers;

    /**
     * Constructs a {@link ListenableBindHandlerAdapter} with the default parent handler
     * and the given bind listeners.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   List<BindListener> listeners = Arrays.asList(listener1, listener2);
     *   ListenableBindHandlerAdapter adapter = new ListenableBindHandlerAdapter(listeners);
     * }</pre>
     *
     * @param bindListeners the {@link BindListener} instances to notify during binding
     */
    public ListenableBindHandlerAdapter(Iterable<BindListener> bindListeners) {
        this(DEFAULT, bindListeners);
    }

    /**
     * Constructs a {@link ListenableBindHandlerAdapter} with the specified parent handler
     * and the given bind listeners.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   List<BindListener> listeners = Arrays.asList(listener1, listener2);
     *   ListenableBindHandlerAdapter adapter =
     *       new ListenableBindHandlerAdapter(existingHandler, listeners);
     * }</pre>
     *
     * @param parent        the parent {@link BindHandler} to delegate to
     * @param bindListeners the {@link BindListener} instances to notify during binding
     */
    public ListenableBindHandlerAdapter(BindHandler parent, Iterable<BindListener> bindListeners) {
        super(parent);
        this.bindHandlers = new BindListeners(bindListeners);
    }

    @Override
    public <T> Bindable<T> onStart(ConfigurationPropertyName name, Bindable<T> target, BindContext context) {
        Bindable<T> result = super.onStart(name, target, context);
        bindHandlers.onStart(name, target, context);
        return result;
    }

    @Override
    public Object onSuccess(ConfigurationPropertyName name, Bindable<?> target, BindContext context, Object result) {
        Object returnValue = super.onSuccess(name, target, context, result);
        bindHandlers.onSuccess(name, target, context, result);
        return returnValue;
    }

    /**
     * Handles the creation of a bound object, delegating to the parent handler if the
     * {@code onCreate} method is available (Spring Boot 2.2.2+), and notifying all
     * registered bind listeners.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   // Called internally by the Binder during object creation
     *   ConfigurationPropertyName name = ConfigurationPropertyName.of("server");
     *   Object result = adapter.onCreate(name, target, context, boundObject);
     * }</pre>
     *
     * @param name    the configuration property name being bound
     * @param target  the bindable target
     * @param context the bind context
     * @param result  the created object
     * @return the (possibly modified) result
     */
    public Object onCreate(ConfigurationPropertyName name, Bindable<?> target, BindContext context, Object result) {
        Object returnValue = result;
        if (onCreateMethodHandle != NOT_FOUND_METHOD_HANDLE) {
            returnValue = execute(() -> onCreateMethodHandle.invoke(this, name, target, context, result));
        }
        bindHandlers.onCreate(name, target, context, result);
        return returnValue;
    }

    @Override
    public Object onFailure(ConfigurationPropertyName name, Bindable<?> target, BindContext context, Exception error) throws Exception {
        try {
            return super.onFailure(name, target, context, error);
        } catch (Exception e) {
            bindHandlers.onFailure(name, target, context, error);
            throw e;
        }
    }

    @Override
    public void onFinish(ConfigurationPropertyName name, Bindable<?> target, BindContext context, Object result) throws Exception {
        super.onFinish(name, target, context, result);
        bindHandlers.onFinish(name, target, context, result);
    }
}