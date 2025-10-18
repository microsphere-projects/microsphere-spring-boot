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

import io.microsphere.spring.boot.context.properties.bind.BindListener;
import io.microsphere.spring.boot.context.properties.bind.ListenableBindHandlerAdapter;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.boot.context.properties.ConfigurationPropertiesBindHandlerAdvisor;
import org.springframework.boot.context.properties.bind.BindHandler;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;

import java.util.List;

import static io.microsphere.spring.beans.BeanUtils.getSortedBeans;

/**
 * {@link ConfigurationPropertiesBindHandlerAdvisor} supports the chaining of the {@link BindListener BindListeners' beans}
 *
 * <h3>Example Usage</h3>
 * <pre>{@code
 * @Component
 * public class MyBindListener implements BindListener {
 *
 *     @Override
 *     public void onStart(ConfigurationPropertyName name, Bindable<?> target, BindContext context) {
 *         System.out.println("Binding started for: " + name);
 *     }
 *
 *     @Override
 *     public Object onSuccess(ConfigurationPropertyName name, Bindable<?> target, BindContext context, Object result) {
 *         System.out.println("Binding succeeded for: " + name);
 *         return result;
 *     }
 *
 *     @Override
 *     public Object onFailure(ConfigurationPropertyName name, Bindable<?> target, BindContext context, Exception error) {
 *         System.out.println("Binding failed for: " + name + ", error: " + error.getMessage());
 *         return null;
 *     }
 * }
 * }</pre>
 *
 * <p>
 * Multiple listeners can be registered as Spring beans and will be automatically discovered and applied
 * in order according to their {@link org.springframework.core.annotation.Order} annotation or
 * {@link org.springframework.core.Ordered} interface implementation.
 * </p>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see ConfigurationPropertiesBindHandlerAdvisor
 * @see BindHandler
 * @see Binder
 * @see Bindable
 * @since 1.0.0
 */
public class ListenableConfigurationPropertiesBindHandlerAdvisor implements ConfigurationPropertiesBindHandlerAdvisor, BeanFactoryAware {

    private BeanFactory beanFactory;

    @Override
    public BindHandler apply(BindHandler bindHandler) {
        List<BindListener> bindListeners = getSortedBeans(this.beanFactory, BindListener.class);
        return new ListenableBindHandlerAdapter(bindHandler, bindListeners);
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }
}