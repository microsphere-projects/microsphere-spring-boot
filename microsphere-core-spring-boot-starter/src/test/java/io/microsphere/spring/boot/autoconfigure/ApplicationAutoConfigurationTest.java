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
package io.microsphere.spring.boot.autoconfigure;

import io.microsphere.spring.context.event.BeanListener;
import io.microsphere.spring.context.event.BeanTimeStatistics;
import io.microsphere.spring.context.event.LoggingBeanListener;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.List;

/**
 * Application AutoConfiguration Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
@SpringBootTest(classes = {
        ApplicationAutoConfigurationTest.class,
        ApplicationAutoConfigurationTest.TestConfig.class
},
        properties = {
                "server.port=12345",
                "spring.mvc.dispatchTraceRequest=true",
                "spring.mvc.format.date=dd/MM/yyyy",
                "logging.level.io.microsphere.spring=DEBUG"
        },
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnableAutoConfiguration
public class ApplicationAutoConfigurationTest {

    @Test
    public void test() {
    }

    static class TestConfig {

        private final ObjectProvider<BeanListener[]> beanListeners;

        private final ObjectProvider<List<BeanListener>> beanListenersList;

        @Autowired
        private ObjectProvider<BeanTimeStatistics> beanTimeStatisticsObjectProvider;

        @Autowired
        private ObjectProvider<LoggingBeanListener> loggingBeanListenerObjectProvider;

        @Resource(name = "io.microsphere.spring.context.event.BeanTimeStatistics#0")
        private BeanTimeStatistics beanTimeStatistics;

        @Resource(type = LoggingBeanListener.class)
        private LoggingBeanListener loggingBeanListener;

        @Resource
        public void setLoggingBeanListener(LoggingBeanListener loggingBeanListener) {

        }

        public TestConfig(ObjectProvider<BeanListener[]> beanListeners,
                          ObjectProvider<List<BeanListener>> beanListenersList,
                          @Qualifier("io.microsphere.spring.context.event.LoggingBeanListener#0") LoggingBeanListener loggingBeanListener) {
            this.beanListeners = beanListeners;
            this.beanListenersList = beanListenersList;
        }

    }
}
