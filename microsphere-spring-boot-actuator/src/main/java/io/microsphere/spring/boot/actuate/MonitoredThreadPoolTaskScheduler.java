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
package io.microsphere.spring.boot.actuate;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.jvm.ExecutorServiceMetrics;
import io.microsphere.concurrent.DelegatingScheduledExecutorService;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;

import static io.micrometer.core.instrument.binder.jvm.ExecutorServiceMetrics.monitor;

/**
 * {@link ThreadPoolTaskScheduler} with {@link ExecutorServiceMetrics} features
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see ThreadPoolTaskScheduler
 * @see ExecutorServiceMetrics
 * @see io.micrometer.core.instrument.internal.TimedScheduledExecutorService
 * @since 1.0.0
 */
public class MonitoredThreadPoolTaskScheduler extends ThreadPoolTaskScheduler implements ApplicationContextAware, SmartInitializingSingleton {

    private String beanName;

    private ApplicationContext context;

    private DelegatingScheduledExecutorService delegate;

    public MonitoredThreadPoolTaskScheduler() {
        super();
    }

    @Override
    protected ScheduledExecutorService createExecutor(int poolSize, ThreadFactory threadFactory, RejectedExecutionHandler rejectedExecutionHandler) {
        ScheduledExecutorService scheduledExecutor = super.createExecutor(poolSize, threadFactory, rejectedExecutionHandler);
        this.delegate = new DelegatingScheduledExecutorService(scheduledExecutor);
        return scheduledExecutor;
    }

    @Override
    public ScheduledExecutorService getScheduledExecutor() throws IllegalStateException {
        return delegate;
    }

    @Override
    public void afterSingletonsInstantiated() {
        MeterRegistry registry = context.getBean(MeterRegistry.class);
        ScheduledExecutorService scheduledExecutor = super.getScheduledExecutor();
        this.delegate.setDelegate(monitor(registry, scheduledExecutor, beanName));
    }

    @Override
    public void setBeanName(String name) {
        super.setBeanName(name);
        this.beanName = name;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.context = applicationContext;
    }
}
