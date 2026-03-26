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

    /**
     * Constructs a new {@link MonitoredThreadPoolTaskScheduler} instance, delegating to the
     * parent {@link ThreadPoolTaskScheduler} constructor.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   MonitoredThreadPoolTaskScheduler scheduler = new MonitoredThreadPoolTaskScheduler();
     *   scheduler.setPoolSize(2);
     *   scheduler.initialize();
     * }</pre>
     */
    public MonitoredThreadPoolTaskScheduler() {
        super();
    }

    /**
     * Creates the underlying {@link ScheduledExecutorService} and wraps it in a
     * {@link DelegatingScheduledExecutorService} for later monitoring instrumentation.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   // Invoked internally during initialize(); not called directly.
     *   // The created executor is wrapped with monitoring support.
     * }</pre>
     *
     * @param poolSize                  the core pool size for the executor
     * @param threadFactory             the {@link ThreadFactory} to use
     * @param rejectedExecutionHandler  the {@link RejectedExecutionHandler} to use
     * @return the created {@link ScheduledExecutorService}
     */
    @Override
    protected ScheduledExecutorService createExecutor(int poolSize, ThreadFactory threadFactory, RejectedExecutionHandler rejectedExecutionHandler) {
        ScheduledExecutorService scheduledExecutor = super.createExecutor(poolSize, threadFactory, rejectedExecutionHandler);
        this.delegate = new DelegatingScheduledExecutorService(scheduledExecutor);
        return scheduledExecutor;
    }

    /**
     * Returns the delegating {@link ScheduledExecutorService} that provides monitoring capabilities.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   MonitoredThreadPoolTaskScheduler scheduler = new MonitoredThreadPoolTaskScheduler();
     *   scheduler.initialize();
     *   ScheduledExecutorService executor = scheduler.getScheduledExecutor();
     * }</pre>
     *
     * @return the monitored {@link ScheduledExecutorService} delegate
     * @throws IllegalStateException if the executor has not been initialized
     */
    @Override
    public ScheduledExecutorService getScheduledExecutor() throws IllegalStateException {
        return delegate;
    }

    /**
     * Initializes the {@link ExecutorServiceMetrics} monitoring by obtaining the {@link MeterRegistry}
     * from the {@link ApplicationContext} and wrapping the underlying executor.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   // Called automatically by Spring after all singletons are instantiated.
     *   // Registers the scheduled executor with the MeterRegistry for metrics collection.
     * }</pre>
     */
    @Override
    public void afterSingletonsInstantiated() {
        MeterRegistry registry = context.getBean(MeterRegistry.class);
        ScheduledExecutorService scheduledExecutor = super.getScheduledExecutor();
        this.delegate.setDelegate(monitor(registry, scheduledExecutor, beanName));
    }

    /**
     * Stores the bean name for use as the metrics identifier and delegates to the parent implementation.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   // Called automatically by the Spring container during bean initialization.
     *   scheduler.setBeanName("actuatorTaskScheduler");
     * }</pre>
     *
     * @param name the name of the bean in the Spring container
     */
    @Override
    public void setBeanName(String name) {
        super.setBeanName(name);
        this.beanName = name;
    }

    /**
     * Stores the {@link ApplicationContext} reference for later retrieval of the {@link MeterRegistry}.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   // Called automatically by the Spring container.
     *   scheduler.setApplicationContext(applicationContext);
     * }</pre>
     *
     * @param applicationContext the {@link ApplicationContext} this bean runs in
     * @throws BeansException if thrown by application context methods
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.context = applicationContext;
    }
}