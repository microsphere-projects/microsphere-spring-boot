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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jackson.JacksonProperties;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.context.properties.bind.BindContext;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.source.ConfigurationProperty;
import org.springframework.boot.context.properties.source.ConfigurationPropertyName;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.mock.env.MockPropertySource;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.boot.context.properties.bind.Bindable.ofInstance;
import static org.springframework.boot.context.properties.source.ConfigurationPropertyName.of;

/**
 * {@link EventPublishingConfigurationPropertiesBeanPropertyChangedListener} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
@SpringBootTest(classes = {
        ListenableConfigurationPropertiesBindHandlerAdvisor.class,
        EventPublishingConfigurationPropertiesBeanPropertyChangedListener.class,
        EventPublishingConfigurationPropertiesBeanPropertyChangedListenerTest.class
})
@TestPropertySource(properties = {"server.error.path=/error.jsp"})
@EnableAutoConfiguration
@EnableConfigurationProperties
class EventPublishingConfigurationPropertiesBeanPropertyChangedListenerTest {

    @Autowired
    private ConfigurableListableBeanFactory beanFactory;

    @Autowired
    private ConfigurableApplicationContext context;

    @Autowired
    private JacksonProperties jacksonProperties;

    @Autowired
    private ServerProperties serverProperties;

    @Autowired
    private EventPublishingConfigurationPropertiesBeanPropertyChangedListener listener;

    private MockPropertySource mockPropertySource;

    @BeforeEach
    void setUp() {
        MutablePropertySources propertySources = context.getEnvironment().getPropertySources();
        mockPropertySource = new MockPropertySource();
        propertySources.addFirst(mockPropertySource);
    }

    @Test
    void testJacksonProperties() {
        assertNull(jacksonProperties.getDateFormat());

        context.addApplicationListener((ApplicationListener<ConfigurationPropertiesBeanPropertyChangedEvent>) event -> {
            ConfigurationProperty configurationProperty = event.getConfigurationProperty();
            String propertyName = event.getPropertyName();
            if ("dateFormat".equals(propertyName)) {
                assertEquals(jacksonProperties, event.getSource());
                assertNull(event.getOldValue());
                assertEquals("yyyy-MM-dd HH:mm:ss", event.getNewValue());
                assertEquals("spring.jackson.date-format", configurationProperty.getName().toString());
                assertEquals(event.getNewValue(), configurationProperty.getValue());
            }
        });

        mockPropertySource.setProperty("spring.jackson.dateFormat", "yyyy-MM-dd HH:mm:ss");
        beanFactory.destroyBean(jacksonProperties);
        beanFactory.initializeBean(jacksonProperties, "spring.jackson-jacksonProperties");
        assertEquals("yyyy-MM-dd HH:mm:ss", jacksonProperties.getDateFormat());
    }

    @Test
    void testServerProperties() {
        assertNull(serverProperties.getPort());

        context.addApplicationListener((ApplicationListener<ConfigurationPropertiesBeanPropertyChangedEvent>) event -> {
            ConfigurationProperty configurationProperty = event.getConfigurationProperty();
            String propertyName = event.getPropertyName();
            if ("error.path".equals(propertyName)) {
                assertEquals(serverProperties, event.getSource());
                assertEquals("/error", event.getOldValue());
                assertEquals("/error-page", event.getNewValue());
                assertEquals("server.error.path", configurationProperty.getName().toString());
                assertEquals(event.getNewValue(), configurationProperty.getValue());
            } else if ("compression.enabled".equals(propertyName)) {
                assertEquals(serverProperties, event.getSource());
                assertEquals(false, event.getOldValue());
                assertEquals(true, event.getNewValue());
                assertEquals("server.compression.enabled", configurationProperty.getName().toString());
                assertEquals("true", configurationProperty.getValue());
            }
        });

        mockPropertySource.setProperty("server.error.path", "/error-page");
        mockPropertySource.setProperty("server.compression.enabled", "true");
        beanFactory.destroyBean(serverProperties);
        beanFactory.initializeBean(serverProperties, "server-serverProperties");

    }

    @Test
    void testSetConfigurationPropertiesBeanPropertyOnFailed() {
        ConfigurationPropertyName name = of("test-name");
        Bindable<?> target = ofInstance(this.serverProperties);
        BindContext context = mock(BindContext.class);
        Object result = null;
        when(context.getConfigurationProperty()).thenReturn(null);
        this.listener.setConfigurationPropertiesBeanProperty(name, target, context, result);

        ConfigurationProperty configurationProperty = new ConfigurationProperty(name, "test-value", null);
        when(context.getConfigurationProperty()).thenReturn(configurationProperty);
        when(context.getDepth()).thenReturn(0);
        this.listener.setConfigurationPropertiesBeanProperty(name, target, context, result);
    }

    @Test
    void testInitConfigurationPropertiesBeanContextOnNullValue() {
        ConfigurationPropertyName name = of("test-name");
        Bindable<?> target = Bindable.of(ServerProperties.class);
        target = target.withSuppliedValue(() -> null);

        BindContext context = mock(BindContext.class);
        when(context.getDepth()).thenReturn(0);

        this.listener.initConfigurationPropertiesBeanContext(name, target, context);
    }
}
