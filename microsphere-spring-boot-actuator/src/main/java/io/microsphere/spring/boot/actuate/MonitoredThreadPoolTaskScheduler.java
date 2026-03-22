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
     * Constructs a new {@link MonitoredThreadPoolTaskScheduler} with default settings.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   MonitoredThreadPoolTaskScheduler scheduler = new MonitoredThreadPoolTaskScheduler();
     *   scheduler.setPoolSize(2);
     *   scheduler.setThreadNamePrefix("my-task-");
     *   scheduler.initialize();
     * }</pre>
     */
    public MonitoredThreadPoolTaskScheduler() {
        super();
    }

    /**
     * Creates the underlying {@link ScheduledExecutorService} and wraps it with a
     * {@link DelegatingScheduledExecutorService} for monitoring support.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   // Typically invoked internally during initialize():
     *   MonitoredThreadPoolTaskScheduler scheduler = new MonitoredThreadPoolTaskScheduler();
     *   scheduler.setPoolSize(4);
     *   scheduler.initialize(); // triggers createExecutor internally
     * }</pre>
     *
     * @param poolSize the configured pool size
     * @param threadFactory the thread factory to use
     * @param rejectedExecutionHandler the handler for rejected tasks
     * @return the created {@link ScheduledExecutorService}
     */
    @Override
    protected ScheduledExecutorService createExecutor(int poolSize, ThreadFactory threadFactory, RejectedExecutionHandler rejectedExecutionHandler) {
        ScheduledExecutorService scheduledExecutor = super.createExecutor(poolSize, threadFactory, rejectedExecutionHandler);
        this.delegate = new DelegatingScheduledExecutorService(scheduledExecutor);
        return scheduledExecutor;
    }

    /**
     * Returns the monitored {@link ScheduledExecutorService} delegate that provides
     * {@link ExecutorServiceMetrics} instrumentation.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   MonitoredThreadPoolTaskScheduler scheduler = // obtain from Spring context
     *   ScheduledExecutorService executor = scheduler.getScheduledExecutor();
     *   executor.schedule(() -> System.out.println("task"), 1, TimeUnit.SECONDS);
     * }</pre>
     *
     * @return the monitored {@link ScheduledExecutorService}
     * @throws IllegalStateException if the executor has not been initialized
     */
    @Override
    public ScheduledExecutorService getScheduledExecutor() throws IllegalStateException {
        return delegate;
    }

    /**
     * Callback invoked after all singleton beans have been instantiated. Registers the
     * underlying {@link ScheduledExecutorService} with the {@link MeterRegistry} for monitoring.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   // Automatically called by the Spring container after all singletons are created.
     *   // No manual invocation is needed when using Spring's lifecycle management.
     * }</pre>
     */
    @Override
    public void afterSingletonsInstantiated() {
        MeterRegistry registry = context.getBean(MeterRegistry.class);
        ScheduledExecutorService scheduledExecutor = super.getScheduledExecutor();
        this.delegate.setDelegate(monitor(registry, scheduledExecutor, beanName));
    }

    /**
     * Sets the bean name for this scheduler, used as the metric name when registering
     * with the {@link MeterRegistry}.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   // Automatically called by the Spring container during bean initialization.
     *   // The bean name is typically set via @Bean(name = "actuatorTaskScheduler").
     * }</pre>
     *
     * @param name the bean name assigned by the Spring container
     */
    @Override
    public void setBeanName(String name) {
        super.setBeanName(name);
        this.beanName = name;
    }

    /**
     * Sets the {@link ApplicationContext} used to look up the {@link MeterRegistry}
     * for executor monitoring.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   // Automatically called by the Spring container for ApplicationContextAware beans.
     *   // No manual invocation is needed when using Spring's lifecycle management.
     * }</pre>
     *
     * @param applicationContext the application context
     * @throws BeansException if context injection fails
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.context = applicationContext;
    }
}