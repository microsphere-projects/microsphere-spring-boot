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

import io.microsphere.spring.boot.context.properties.bind.BindListener;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.context.properties.bind.BindContext;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.source.ConfigurationPropertyName;

import java.util.Map;

import static io.microsphere.collection.Maps.ofMap;
import static io.microsphere.spring.boot.context.properties.bind.util.BindUtils.bind;
import static io.microsphere.spring.boot.context.properties.source.util.ConfigurationPropertyUtils.getPrefix;
import static io.microsphere.spring.boot.context.properties.source.util.ConfigurationPropertyUtils.toDashedForm;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * {@link ConfigurationPropertyUtils} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public class ConfigurationPropertyUtilsTest {

    @Test
    void testGetPrefix() {
        String prefix = "server";
        Map<String, String> properties = ofMap(prefix + ".port", "12345");
        ServerProperties serverProperties = bind(properties, prefix, ServerProperties.class, new BindListener() {
            @Override
            public void onSuccess(ConfigurationPropertyName name, Bindable<?> target, BindContext context, Object result) {
                assertEquals(prefix, getPrefix(name, context));
            }
        });
        assertEquals(12345, serverProperties.getPort());
    }

    @Test
    void testToDashedForm() {
        assertEquals("my-name", toDashedForm("my-name"));
        assertEquals("my-name", toDashedForm("myName"));
        assertEquals("my-name", toDashedForm("MyName"));
        assertEquals("my-name", toDashedForm("my_name"));
    }

    @Test
    void testToDashedFormWithStart() {
        assertToDashedForm("my-name");
        assertToDashedForm("myName");
        assertToDashedForm("MyName");
        assertToDashedForm("my_name");
    }

    void assertToDashedForm(String name) {
        assertEquals(toDashedForm(name), toDashedForm(name, 0));
    }
}
