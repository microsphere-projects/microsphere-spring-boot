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
package io.github.microsphere.spring.boot.context.properties;

import io.github.microsphere.spring.boot.context.properties.bind.BindListener;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.context.properties.bind.BindContext;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.source.ConfigurationPropertyName;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * {@link ListenableConfigurationPropertiesBindHandlerAdvisor} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
@SpringBootTest(
        classes = {
                ListenableConfigurationPropertiesBindHandlerAdvisor.class,
                ListenableConfigurationPropertiesBindHandlerAdvisorTest.class,
                ListenableConfigurationPropertiesBindHandlerAdvisorTest.MyBindListener.class
        }
)
@EnableAutoConfiguration
@EnableConfigurationProperties
public class ListenableConfigurationPropertiesBindHandlerAdvisorTest {

    private static final Logger logger = LoggerFactory.getLogger(ListenableConfigurationPropertiesBindHandlerAdvisorTest.class);

    @Test
    public void test() {

    }

    static class MyBindListener implements BindListener {

        @Override
        public <T> void onStart(ConfigurationPropertyName name, Bindable<T> target, BindContext context) {
            logger.info("onStart - name : {} , target : {} , context : {}", name, target, context);
        }

        @Override
        public void onSuccess(ConfigurationPropertyName name, Bindable<?> target, BindContext context, Object result) {
            logger.info("onSuccess - name : {} , target : {} , context : {} , result : {}", name, target, context, result);
        }

        @Override
        public void onCreate(ConfigurationPropertyName name, Bindable<?> target, BindContext context, Object result) {
            logger.info("onCreate - name : {} , target : {} , context : {} , result : {}", name, target, context, result);
        }

        @Override
        public void onFailure(ConfigurationPropertyName name, Bindable<?> target, BindContext context, Exception error) {
            logger.info("onFailure - name : {} , target : {} , context : {} , error : {}", name, target, context, error);
        }

        @Override
        public void onFinish(ConfigurationPropertyName name, Bindable<?> target, BindContext context, Object result) {
            logger.info("onFinish - name : {} , target : {} , context : {} , result : {}", name, target, context, result);
        }
    }
}
