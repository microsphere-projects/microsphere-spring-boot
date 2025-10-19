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
package io.microsphere.spring.boot.context.properties.source.util;

import org.springframework.boot.context.properties.bind.BindContext;
import org.springframework.boot.context.properties.bind.DataObjectPropertyName;
import org.springframework.boot.context.properties.source.ConfigurationProperty;
import org.springframework.boot.context.properties.source.ConfigurationPropertyName;

import static io.microsphere.constants.SymbolConstants.DOT;
import static io.microsphere.util.StringUtils.substringBeforeLast;

/**
 * The utilities class of {@link ConfigurationProperty}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see ConfigurationProperty
 * @since 1.0.0
 */
public abstract class ConfigurationPropertyUtils {

    /**
     * Get the prefix of the specified {@link ConfigurationPropertyName}
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * ConfigurationPropertyName name = ConfigurationPropertyName.of("server.port");
     * BindContext context = ...; // assume depth = 0
     * String prefix = getPrefix(name, context); // returns "server.port"
     *
     * ConfigurationPropertyName name2 = ConfigurationPropertyName.of("spring.datasource.url");
     * BindContext context2 = ...; // assume depth = 1
     * String prefix2 = getPrefix(name2, context2); // returns "spring.datasource"
     * }</pre>
     *
     * @param name    the {@link ConfigurationPropertyName}
     * @param context the {@link BindContext}
     * @return the prefix of the specified {@link ConfigurationPropertyName}
     * @throws IllegalArgumentException if name or context is null
     */
    public static final String getPrefix(ConfigurationPropertyName name, BindContext context) {
        int depth = context.getDepth();
        String propertyName = name.toString();
        String prefix = propertyName;
        for (int i = 0; i < depth; i++) {
            prefix = substringBeforeLast(prefix, DOT);
        }
        return prefix;
    }

    /**
     * Return the specified Java Bean property name in dashed form.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * String result1 = toDashedForm("userName");     // returns "user-name"
     * String result2 = toDashedForm("firstName");    // returns "first-name"
     * String result3 = toDashedForm("URL");          // returns "url"
     * String result4 = toDashedForm("myURL");        // returns "my-url"
     * String result5 = toDashedForm("user_name");    // returns "user-name"
     * }</pre>
     *
     * @param name the source name
     * @return the dashed from
     * @see org.springframework.boot.context.properties.bind.DataObjectPropertyName#toDashedForm(String)
     */
    public static String toDashedForm(String name) {
        return DataObjectPropertyName.toDashedForm(name);
    }

    private ConfigurationPropertyUtils() {
    }
}