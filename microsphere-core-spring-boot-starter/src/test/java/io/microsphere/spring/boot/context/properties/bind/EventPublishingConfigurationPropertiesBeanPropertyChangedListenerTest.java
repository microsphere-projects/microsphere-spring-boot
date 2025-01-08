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
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jackson.JacksonProperties;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.context.properties.source.ConfigurationProperty;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.mock.env.MockPropertySource;
import org.springframework.test.context.TestPropertySource;

import javax.annotation.PostConstruct;

import java.util.Locale;

import static java.lang.Integer.valueOf;
import static java.util.Locale.SIMPLIFIED_CHINESE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * {@link EventPublishingConfigurationPropertiesBeanPropertyChangedListener} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
@SpringBootTest(classes =
        {ListenableConfigurationPropertiesBindHandlerAdvisor.class,
                EventPublishingConfigurationPropertiesBeanPropertyChangedListener.class,
                EventPublishingConfigurationPropertiesBeanPropertyChangedListenerTest.class},
        webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@TestPropertySource(properties = {"server.error.path=/error.jsp"})
@EnableAutoConfiguration
@EnableConfigurationProperties
public class EventPublishingConfigurationPropertiesBeanPropertyChangedListenerTest {

    @Autowired
    private ConfigurableListableBeanFactory beanFactory;

    @Autowired
    private ConfigurableApplicationContext context;

    @Autowired
    private JacksonProperties jacksonProperties;

    @Autowired
    private ServerProperties serverProperties;

    private MockPropertySource mockPropertySource;

    @PostConstruct
    public void init() {
        MutablePropertySources propertySources = context.getEnvironment().getPropertySources();
        mockPropertySource = new MockPropertySource();
        propertySources.addFirst(mockPropertySource);
    }

    @Test
    public void testJacksonProperties() {
        assertNull(jacksonProperties.getLocale());

        context.addApplicationListener((ApplicationListener<ConfigurationPropertiesBeanPropertyChangedEvent>) event -> {
            ConfigurationProperty configurationProperty = event.getConfigurationProperty();
            String propertyName = event.getPropertyName();
            if ("locale".equals(propertyName)) {
                assertEquals(jacksonProperties, event.getSource());
                assertNull(event.getOldValue());
                assertEquals(SIMPLIFIED_CHINESE, event.getNewValue());
                assertEquals("spring.jackson.locale", configurationProperty.getName().toString());
                assertEquals(event.getNewValue().toString(), configurationProperty.getValue());
            }
        });

        mockPropertySource.setProperty("spring.jackson.locale", "zh_CN");
        beanFactory.destroyBean(jacksonProperties);
        beanFactory.initializeBean(jacksonProperties, getBeanName(jacksonProperties));
        assertEquals(SIMPLIFIED_CHINESE, jacksonProperties.getLocale());
    }

    @Test
    public void testServerProperties() {
        assertNull(serverProperties.getPort());

        String newPortPropertyValue = "9527";

        context.addApplicationListener((ApplicationListener<ConfigurationPropertiesBeanPropertyChangedEvent>) event -> {
            ConfigurationProperty configurationProperty = event.getConfigurationProperty();
            String propertyName = event.getPropertyName();
            if ("port".equals(propertyName)) {
                assertEquals(serverProperties, event.getSource());
                assertNull(event.getOldValue());
                assertEquals(valueOf(newPortPropertyValue), event.getNewValue());
                assertEquals(valueOf((String) configurationProperty.getValue()), event.getNewValue());
            }
        });

        mockPropertySource.setProperty("server.port", newPortPropertyValue);
        beanFactory.destroyBean(serverProperties);
        beanFactory.initializeBean(serverProperties, getBeanName(serverProperties));

    }

    private String getBeanName(Object configurationPropertiesBean) {
        Class<?> beanClass = configurationPropertiesBean.getClass();
        String[] beanNames = this.context.getBeanNamesForType(beanClass);
        return beanNames[0];
    }
}
