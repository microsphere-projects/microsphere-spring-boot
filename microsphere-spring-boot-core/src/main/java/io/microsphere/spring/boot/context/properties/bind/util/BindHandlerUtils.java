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

package io.microsphere.spring.boot.context.properties.bind.util;

import io.microsphere.annotation.Nonnull;
import org.springframework.boot.context.properties.bind.BindHandler;
import org.springframework.boot.context.properties.bind.handler.IgnoreErrorsBindHandler;
import org.springframework.boot.context.properties.bind.handler.NoUnboundElementsBindHandler;
import org.springframework.boot.context.properties.source.UnboundElementsSourceFilter;

import static org.springframework.boot.context.properties.bind.BindHandler.DEFAULT;

/**
 * The utilities class of {@link BindHandler}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see BindHandler
 * @see IgnoreErrorsBindHandler
 * @see NoUnboundElementsBindHandler
 * @since 1.0.0
 */
public abstract class BindHandlerUtils {

    /**
     * Create a {@link BindHandler} instance based on the given flags.
     * <h3>Example Usage</h3>
     * <pre>{@code
     * // Create a handler that ignores both unknown and invalid fields
     * BindHandler handler = BindHandlerUtils.createBindHandler(true, true);
     *
     * // Create a handler that does not ignore any fields
     * BindHandler handler = BindHandlerUtils.createBindHandler(false, false);
     *
     * // Create a handler that ignores only invalid fields
     * BindHandler handler = BindHandlerUtils.createBindHandler(true, false);
     *
     * // Create a handler that ignores only unknown fields
     * BindHandler handler = BindHandlerUtils.createBindHandler(false, true);
     * }</pre>
     *
     * @param ignoreUnknownFields whether to ignore unknown fields
     * @param ignoreInvalidFields whether to ignore invalid fields
     * @return a {@link BindHandler} instance
     */
    @Nonnull
    public static BindHandler createBindHandler(boolean ignoreUnknownFields, boolean ignoreInvalidFields) {
        BindHandler handler = DEFAULT;
        if (ignoreInvalidFields) {
            handler = new IgnoreErrorsBindHandler(handler);
        }
        if (!ignoreUnknownFields) {
            UnboundElementsSourceFilter filter = new UnboundElementsSourceFilter();
            handler = new NoUnboundElementsBindHandler(handler, filter);
        }
        return handler;
    }


    private BindHandlerUtils() {
    }
}
