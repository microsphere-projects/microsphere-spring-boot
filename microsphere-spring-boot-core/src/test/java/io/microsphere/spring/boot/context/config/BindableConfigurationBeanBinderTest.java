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
package io.microsphere.spring.boot.context.config;

import io.microsphere.spring.boot.domain.User;
import io.microsphere.spring.context.config.ConfigurationBeanBinder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.bind.BindException;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Map;

import static io.microsphere.spring.core.env.PropertySourcesUtils.getSubProperties;
import static java.lang.Integer.valueOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * {@link BindableConfigurationBeanBinder} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
@ExtendWith(SpringExtension.class)
@TestPropertySource(properties = {
        "user.name=mercyblitz",
        "user.age=37"
})
@ContextConfiguration(
        classes = BindableConfigurationBeanBinder.class
)
class BindableConfigurationBeanBinderTest {

    @Autowired
    private ConfigurationBeanBinder beanBinder;

    @Autowired
    private ConfigurableEnvironment environment;

    @Test
    void testBind() {
        User user = new User();
        Map<String, Object> properties = getSubProperties(environment.getPropertySources(), "user");

        beanBinder.bind(properties, true, true, user);
        assertUser(user);

        beanBinder.bind(properties, true, false, user);
        assertUser(user);

        assertThrows(BindException.class, () -> beanBinder.bind(properties, false, true, user));

        assertThrows(BindException.class, () -> beanBinder.bind(properties, false, false, user));
    }

    void assertUser(User user) {
        assertEquals("mercyblitz", user.getName());
        assertEquals(valueOf(37), user.getAge());
    }
}