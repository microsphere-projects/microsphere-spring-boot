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
package io.microsphere.spring.boot.condition;

import org.springframework.context.annotation.Conditional;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * {@link Conditional} that checks if the prefix of properties are found in environment..
 *
 * <h3>Example Usage</h3>
 * <h4>Single prefix</h4>
 * <pre>{@code
 * @ConditionalOnPropertyPrefix("myapp.config")
 * public class MyConfig {
 *     // This bean will only be loaded if any property with prefix "myapp.config." exists
 * }
 * }</pre>
 *
 * <h4>Multiple prefixes</h4>
 * <pre>{@code
 * @ConditionalOnPropertyPrefix( {"feature.alpha", "feature.beta"} )
 * public class FeatureConfig {
 *     // Loaded if any property with prefix "feature.alpha." or "feature.beta." exists
 * }
 * }</pre>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see OnPropertyPrefixCondition
 * @since 1.0.0
 */
@Retention(RUNTIME)
@Target({TYPE, METHOD})
@Conditional(OnPropertyPrefixCondition.class)
public @interface ConditionalOnPropertyPrefix {

    /**
     * The prefix values of properties.
     * <p>
     * The prefix automatically ends
     * with a dot if not specified.
     *
     * @return prefix values of properties.
     */
    String[] value();

}