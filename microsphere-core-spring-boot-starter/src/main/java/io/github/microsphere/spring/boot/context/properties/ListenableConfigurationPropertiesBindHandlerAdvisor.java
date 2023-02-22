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
import io.github.microsphere.spring.boot.context.properties.bind.ListenableBindHandlerAdapter;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.context.properties.ConfigurationPropertiesBindHandlerAdvisor;
import org.springframework.boot.context.properties.bind.BindHandler;

/**
 * {@link ConfigurationPropertiesBindHandlerAdvisor} supports the chaining of the {@link BindListener BindListeners' beans}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see BindHandler
 * @see ConfigurationPropertiesBindHandlerAdvisor
 * @since 1.0.0
 */
public class ListenableConfigurationPropertiesBindHandlerAdvisor implements ConfigurationPropertiesBindHandlerAdvisor, BeanFactoryAware {

    private ObjectProvider<BindListener> bindListeners;

    @Override
    public BindHandler apply(BindHandler bindHandler) {
        return new ListenableBindHandlerAdapter(bindHandler, bindListeners);
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.bindListeners = beanFactory.getBeanProvider(BindListener.class);
    }
}
