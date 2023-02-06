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
package io.github.microsphere.spring.boot.context.properties.bind;

import org.springframework.boot.context.properties.bind.AbstractBindHandler;
import org.springframework.boot.context.properties.bind.BindContext;
import org.springframework.boot.context.properties.bind.BindHandler;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.source.ConfigurationPropertyName;

/**
 * Listable {@link BindHandler} Adapter
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public class ListenableBindHandlerAdapter extends AbstractBindHandler {

    private final BindListeners bindHandlers;

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

    @Override
    public Object onCreate(ConfigurationPropertyName name, Bindable<?> target, BindContext context, Object result) {
        Object returnValue = super.onCreate(name, target, context, result);
        bindHandlers.onCreate(name, target, context, result);
        return returnValue;
    }

    @Override
    public Object onFailure(ConfigurationPropertyName name, Bindable<?> target, BindContext context, Exception error) throws Exception {
        Object result = super.onFailure(name, target, context, error);
        bindHandlers.onFailure(name, target, context, error);
        return result;
    }

    @Override
    public void onFinish(ConfigurationPropertyName name, Bindable<?> target, BindContext context, Object result) throws Exception {
        super.onFinish(name, target, context, result);
        bindHandlers.onFinish(name, target, context, result);
    }
}
