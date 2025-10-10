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
package io.microsphere.spring.boot.context.properties;

import io.microsphere.logging.Logger;
import io.microsphere.spring.boot.context.properties.bind.BindListener;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.context.properties.bind.BindContext;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.source.ConfigurationPropertyName;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import static io.microsphere.logging.LoggerFactory.getLogger;

/**
 * {@link ListenableConfigurationPropertiesBindHandlerAdvisor} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
@SpringBootTest(classes = {
        ListenableConfigurationPropertiesBindHandlerAdvisor.class,
        ListenableConfigurationPropertiesBindHandlerAdvisorTest.class,
        ListenableConfigurationPropertiesBindHandlerAdvisorTest.MyBindListener.class,
        ListenableConfigurationPropertiesBindHandlerAdvisorTest.MyBindListener1.class,
        ListenableConfigurationPropertiesBindHandlerAdvisorTest.MyBindListener2.class
},
        properties = {
                "spring.jackson.dateFormat=yyyy-MM-dd HH:mm:ss",
        }
)
@EnableAutoConfiguration
@EnableConfigurationProperties
class ListenableConfigurationPropertiesBindHandlerAdvisorTest {

    private static final Logger logger = getLogger(ListenableConfigurationPropertiesBindHandlerAdvisorTest.class);

    @Test
    void test() {

    }

    static class MyBindListener implements BindListener {

        @Override
        public <T> void onStart(ConfigurationPropertyName name, Bindable<T> target, BindContext context) {
            logger.info("MyBindListener onStart - name : {} , target : {} , context : {}", name, target, context);
        }

        @Override
        public void onSuccess(ConfigurationPropertyName name, Bindable<?> target, BindContext context, Object result) {
            logger.info("MyBindListener onSuccess - name : {} , target : {} , context : {} , result : {}", name, target, context, result);
        }

        @Override
        public void onCreate(ConfigurationPropertyName name, Bindable<?> target, BindContext context, Object result) {
            logger.info("MyBindListener onCreate - name : {} , target : {} , context : {} , result : {}", name, target, context, result);
        }

        @Override
        public void onFailure(ConfigurationPropertyName name, Bindable<?> target, BindContext context, Exception error) {
            logger.info("MyBindListener onFailure - name : {} , target : {} , context : {} , error", name, target, context, error);
        }

        @Override
        public void onFinish(ConfigurationPropertyName name, Bindable<?> target, BindContext context, Object result) {
            logger.info("MyBindListener onFinish - name : {} , target : {} , context : {} , result : {}", name, target, context, result);
        }
    }

    @Order(1)
    static class MyBindListener1 implements BindListener {

        @Override
        public <T> void onStart(ConfigurationPropertyName name, Bindable<T> target, BindContext context) {
            logger.info("MyBindListener1 onStart - name : {} , target : {} , context : {}", name, target, context);
        }

        @Override
        public void onSuccess(ConfigurationPropertyName name, Bindable<?> target, BindContext context, Object result) {
            logger.info("MyBindListener1 onSuccess - name : {} , target : {} , context : {} , result : {}", name, target, context, result);
        }

        @Override
        public void onCreate(ConfigurationPropertyName name, Bindable<?> target, BindContext context, Object result) {
            logger.info("MyBindListener1 onCreate - name : {} , target : {} , context : {} , result : {}", name, target, context, result);
        }

        @Override
        public void onFailure(ConfigurationPropertyName name, Bindable<?> target, BindContext context, Exception error) {
            logger.info("MyBindListener1 onFailure - name : {} , target : {} , context : {} , error", name, target, context, error);
        }

        @Override
        public void onFinish(ConfigurationPropertyName name, Bindable<?> target, BindContext context, Object result) {
            logger.info("MyBindListener1 onFinish - name : {} , target : {} , context : {} , result : {}", name, target, context, result);
        }
    }

    static class MyBindListener2 implements BindListener, Ordered {

        @Override
        public <T> void onStart(ConfigurationPropertyName name, Bindable<T> target, BindContext context) {
            logger.info("MyBindListener2 onStart - name : {} , target : {} , context : {}", name, target, context);
        }

        @Override
        public void onSuccess(ConfigurationPropertyName name, Bindable<?> target, BindContext context, Object result) {
            logger.info("MyBindListener2 onSuccess - name : {} , target : {} , context : {} , result : {}", name, target, context, result);
        }

        @Override
        public void onCreate(ConfigurationPropertyName name, Bindable<?> target, BindContext context, Object result) {
            logger.info("MyBindListener2 onCreate - name : {} , target : {} , context : {} , result : {}", name, target, context, result);
        }

        @Override
        public void onFailure(ConfigurationPropertyName name, Bindable<?> target, BindContext context, Exception error) {
            logger.info("MyBindListener2 onFailure - name : {} , target : {} , context : {} , error", name, target, context, error);
        }

        @Override
        public void onFinish(ConfigurationPropertyName name, Bindable<?> target, BindContext context, Object result) {
            logger.info("MyBindListener2 onFinish - name : {} , target : {} , context : {} , result : {}", name, target, context, result);
        }

        @Override
        public int getOrder() {
            return 2;
        }
    }
}
