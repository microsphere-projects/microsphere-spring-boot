package io.microsphere.spring.boot.context;

import io.microsphere.logging.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.event.ApplicationPreparedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.Ordered;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

import static io.microsphere.logging.LoggerFactory.getLogger;

/**
 * This abstract class provides a base implementation for application listeners that should only
 * execute once per application context. It handles the logic of tracking which contexts have
 * already been processed to prevent duplicate execution.
 *
 * <h3>Example Usage</h3>
 * <pre>{@code
 * public class MyOnceApplicationPreparedEventListener extends OnceApplicationPreparedEventListener {
 *
 *     @Override
 *     protected boolean isIgnored(SpringApplication springApplication, String[] args,
 *                               ConfigurableApplicationContext context) {
 *         // Return true to skip processing for this context
 *         return false; // Process all contexts by default
 *     }
 *
 *     @Override
 *     protected void onApplicationEvent(SpringApplication springApplication, String[] args,
 *                                     ConfigurableApplicationContext context) {
 *         // Your one-time initialization logic here
 *         System.out.println("Application prepared event handled once for context: " + context.getId());
 *     }
 * }
 * }</pre>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see io.microsphere.spring.context.event.OnceApplicationContextEventListener
 * @since 1.0.0
 **/
public abstract class OnceApplicationPreparedEventListener implements ApplicationListener<ApplicationPreparedEvent>, Ordered {

    private static Map<Class<? extends ApplicationListener>, Set<String>> listenerProcessedContextIds = new ConcurrentHashMap<>();

    protected final Logger logger = getLogger(getClass());

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
            logger.trace("Current ApplicationContext[id : {}] has been processed!", contextId);
            return;
        }

        SpringApplication springApplication = event.getSpringApplication();
        String[] args = event.getArgs();

        if (isIgnored(springApplication, args, context)) {
            markProcessed(contextId);
            logger.trace("Current ApplicationContext[id : {}] is ignored!", contextId);
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
        logger.trace("Current ApplicationContext[id : {}] was mark to be 'processed'", contextId);
    }

    protected abstract boolean isIgnored(SpringApplication springApplication, String[] args, ConfigurableApplicationContext context);

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