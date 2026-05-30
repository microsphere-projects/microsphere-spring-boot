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
import org.springframework.boot.context.properties.bind.BindHandler;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.source.ConfigurationPropertyName;

/**
 * Bind Listener
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see BindHandler
 * @since 1.0.0
 */
public interface BindListener {

    /**
     * Called when binding of an element starts but before any result has been determined.
     *
     * @param name    the name of the element being bound
     * @param target  the item being bound
     * @param context the bind context
     */
    default <T> void onStart(ConfigurationPropertyName name, Bindable<T> target, BindContext context) {
    }

    /**
     * Called when binding of an element ends with a successful result. Implementations
     * may change the ultimately returned result or perform addition validation.
     *
     * @param name    the name of the element being bound
     * @param target  the item being bound
     * @param context the bind context
     * @param result  the bound result (never {@code null})
     */
    default void onSuccess(ConfigurationPropertyName name, Bindable<?> target, BindContext context, Object result) {
    }

    /**
     * Called when binding of an element ends with an unbound result and a newly created
     * instance is about to be returned. Implementations may change the ultimately
     * returned result or perform addition validation.
     *
     * @param name    the name of the element being bound
     * @param target  the item being bound
     * @param context the bind context
     * @param result  the newly created instance (never {@code null})
     */
    default void onCreate(ConfigurationPropertyName name, Bindable<?> target, BindContext context, Object result) {
    }

    /**
     * Called when binding fails for any reason (including failures from
     * {@link #onSuccess} Implementations may choose to
     * swallow exceptions and return an alternative result.
     *
     * @param name    the name of the element being bound
     * @param target  the item being bound
     * @param context the bind context
     * @param error   the cause of the error (if the exception stands it may be re-thrown)
     */
    default void onFailure(ConfigurationPropertyName name, Bindable<?> target, BindContext context, Exception error) {
    }

    /**
     * Called when binding finishes with either bound or unbound result. This method will
     * not be called when binding failed, even if a handler returns a result from
     * {@link #onFailure}.
     *
     * @param name    the name of the element being bound
     * @param target  the item being bound
     * @param context the bind context
     * @param result  the bound result (may be {@code null})
     */
    default void onFinish(ConfigurationPropertyName name, Bindable<?> target, BindContext context, Object result) {
    }

}