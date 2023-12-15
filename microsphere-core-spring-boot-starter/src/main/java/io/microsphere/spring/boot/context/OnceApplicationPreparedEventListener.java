package io.microsphere.spring.boot.context;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.event.ApplicationPreparedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.Ordered;

/**
 * Once execution {@link ApplicationPreparedEvent} {@link ApplicationListener}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see io.microsphere.spring.context.event.OnceApplicationContextEventListener
 * @since 1.0.0
 **/
public abstract class OnceApplicationPreparedEventListener implements ApplicationListener<ApplicationPreparedEvent>, Ordered {

    private static Map<Class<? extends ApplicationListener>, Set<String>> listenerProcessedContextIds = new ConcurrentHashMap<>();

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private final Set<String> processedContextIds;

    private int order = LOWEST_PRECEDENCE;

    public OnceApplicationPreparedEventListener() {
        this.processedContextIds = getProcessedContextIds(getClass());
    }

    @Override
    public final void onApplicationEvent(ApplicationPreparedEvent event) {

        ConfigurableApplicationContext context = event.getApplicationContext();

        String contextId = context.getId();

        if (isProcessed(contextId)) {
            logger.debug("Current ApplicationContext[id : {}] has been processed!", contextId);
            return;
        }

        SpringApplication springApplication = event.getSpringApplication();
        String[] args = event.getArgs();

        if (isIgnored(springApplication, args, context)) {
            markProcessed(contextId);
            logger.debug("Current ApplicationContext[id : {}] is ignored!", contextId);
            return;
        }

        markProcessed(contextId);

        onApplicationEvent(springApplication, args, context);

    }

    protected boolean isProcessed(String contextId) {
        return processedContextIds.contains(contextId);
    }

    protected void markProcessed(String contextId) {
        processedContextIds.add(contextId);
        logger.debug("Current ApplicationContext[id : {}] was mark to be 'processed'", contextId);
    }

    protected boolean isIgnored(SpringApplication springApplication, String[] args, ConfigurableApplicationContext context) {
        // Sub-Class implements current method
        return false;
    }

    protected abstract void onApplicationEvent(SpringApplication springApplication, String[] args, ConfigurableApplicationContext context);

    public final void setOrder(int order) {
        this.order = order;
    }

    @Override
    public final int getOrder() {
        return order;
    }

    private static Set<String> getProcessedContextIds(Class<? extends ApplicationListener> listenerClass) {
        return listenerProcessedContextIds.computeIfAbsent(listenerClass, type -> new ConcurrentSkipListSet<>());
    }
}
