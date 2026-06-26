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
import io.microsphere.spring.boot.context.properties.TestConfigurationProperties;
import io.microsphere.spring.test.junit.jupiter.SpringLoggingTest;
import io.microsphere.util.ValueHolder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.context.properties.bind.BindContext;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.source.ConfigurationPropertyName;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.mock.env.MockPropertySource;
import org.springframework.test.context.TestPropertySource;

import java.util.Locale;

import static io.microsphere.spring.beans.BeanUtils.generateBeanName;
import static io.microsphere.spring.core.annotation.AnnotationUtils.tryGetMergedAnnotationAttributes;
import static io.microsphere.util.ArrayUtils.ofArray;
import static java.util.Locale.SIMPLIFIED_CHINESE;
import static java.util.Locale.US;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.boot.autoconfigure.web.WebProperties.LocaleResolver.FIXED;
import static org.springframework.boot.context.properties.source.ConfigurationPropertyName.of;

/**
 * {@link EventPublishingConfigurationPropertiesBeanPropertyChangedListener} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
@SpringLoggingTest
@SpringBootTest(classes = {
        ListenableConfigurationPropertiesBindHandlerAdvisor.class,
        EventPublishingConfigurationPropertiesBeanPropertyChangedListener.class,
        EventPublishingConfigurationPropertiesBeanPropertyChangedListenerTest.class
})
@TestPropertySource(properties = {
        // WebProperties
        "spring.web.locale=en_US",
        "spring.web.locale-resolver=fixed",
        "spring.web.resources.static-locations[0]=/static",
        "spring.web.resources.static-locations[1]=/public",
        "spring.web.resources.static-locations[2]=/resources",

        // TestConfigurationProperties
        "test.name=test-name",
        "test.properties.key-1=value-1",
        "test.properties.key-2=value-2",
        "test.properties[key-3]=value-3",
        "test.aliases[0]=a",
        "test.aliases[1]=b",
        "test.aliases[2]=c",
        "test.ports=7070,8080,9090"
})
@EnableAutoConfiguration
@EnableConfigurationProperties(
        value = {
                WebProperties.class,
                TestConfigurationProperties.class
        }
)
class EventPublishingConfigurationPropertiesBeanPropertyChangedListenerTest {

    @Autowired
    private ConfigurableListableBeanFactory beanFactory;

    @Autowired
    private ConfigurableApplicationContext context;

    private ConfigurableEnvironment environment;

    @Autowired
    private WebProperties webProperties;

    @Autowired
    private EventPublishingConfigurationPropertiesBeanPropertyChangedListener listener;

    private MockPropertySource mockPropertySource;

    private ValueHolder<ConfigurationPropertiesBeanPropertyChangedEvent> eventHolder;

    @BeforeEach
    void setUp() {
        this.environment = this.context.getEnvironment();
        MutablePropertySources propertySources = this.environment.getPropertySources();
        this.mockPropertySource = new MockPropertySource();
        this.eventHolder = new ValueHolder<>();
        propertySources.addFirst(this.mockPropertySource);

        this.context.addApplicationListener((ApplicationListener<ConfigurationPropertiesBeanPropertyChangedEvent>) event -> {
            this.eventHolder.setValue(event);
        });
    }

    @Test
    void testWebProperties(int index) {
        if (index > 0) {
            return;
        }

        // assert the configured values
        assertEquals(US, webProperties.getLocale());
        assertEquals(FIXED, webProperties.getLocaleResolver());

        WebProperties.Resources resources = webProperties.getResources();
        String[] staticLocations = resources.getStaticLocations();
        assertArrayEquals(ofArray("/static/", "/public/", "/resources/"), staticLocations);

        this.eventHolder.reset();

        setProperty("spring.web.locale", "zh_CN", webProperties);

        ConfigurationPropertiesBeanPropertyChangedEvent event = this.eventHolder.getValue();
        assertSame(this.webProperties, event.getSource());
        assertNotNull(event.getConfigurationProperty());
        assertEquals(Locale.class, event.getPropertyType().resolve());
        assertEquals(US, event.getOldValue());
        assertEquals(SIMPLIFIED_CHINESE, event.getNewValue());

    }

    void setProperty(String configurationPropertyName, String propertyValue, Object configurationPropertiesBean) {
        this.mockPropertySource.setProperty(configurationPropertyName, propertyValue);
        this.beanFactory.destroyBean(configurationPropertiesBean);

        Class<?> configurationPropertiesBeanClass = configurationPropertiesBean.getClass();
        AnnotationAttributes annotationAttributes = tryGetMergedAnnotationAttributes(configurationPropertiesBeanClass, ConfigurationProperties.class, this.environment, false);
        String prefix = annotationAttributes.getString("prefix");
        String suffix = generateBeanName(configurationPropertiesBeanClass);
        String beanName = prefix + "-" + suffix;
        this.beanFactory.initializeBean(configurationPropertiesBean, beanName);
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
