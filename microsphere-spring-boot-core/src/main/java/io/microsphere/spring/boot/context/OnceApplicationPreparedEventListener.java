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

    /**
     * Constructs a new {@code OnceApplicationPreparedEventListener} and initializes
     * the set of processed context ids for the concrete listener class.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   public class MyListener extends OnceApplicationPreparedEventListener {
     *       public MyListener() {
     *           // The superclass constructor tracks processed context ids automatically
     *           super();
     *       }
     *   }
     * }</pre>
     */
    public OnceApplicationPreparedEventListener() {
        this.processedContextIds = getProcessedContextIds(getClass());
    }

    /**
     * Handles the {@link ApplicationPreparedEvent} by ensuring the event is processed only once
     * per application context. If the context has already been processed or is ignored, the
     * event is skipped. Otherwise, it delegates to
     * {@link #onApplicationEvent(SpringApplication, String[], ConfigurableApplicationContext)}.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   // This method is invoked automatically by the Spring event mechanism:
     *   ApplicationPreparedEvent event = ...;
     *   listener.onApplicationEvent(event);
     * }</pre>
     *
     * @param event the application prepared event
     */
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

    /**
     * Checks whether the application context with the given id has already been processed
     * by this listener.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   if (isProcessed(context.getId())) {
     *       // skip duplicate processing
     *   }
     * }</pre>
     *
     * @param contextId the application context id
     * @return {@code true} if the context has been processed, {@code false} otherwise
     */
    protected boolean isProcessed(String contextId) {
        return processedContextIds.contains(contextId);
    }

    /**
     * Marks the application context with the given id as processed so that subsequent
     * events for the same context are ignored.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   markProcessed(context.getId());
     *   // Future calls to isProcessed(context.getId()) will return true
     * }</pre>
     *
     * @param contextId the application context id to mark as processed
     */
    protected void markProcessed(String contextId) {
        processedContextIds.add(contextId);
        logger.trace("Current ApplicationContext[id : {}] was mark to be 'processed'", contextId);
    }

    /**
     * Determines whether the given application context should be ignored and not processed
     * by this listener. Subclasses implement this to provide custom filtering logic.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   @Override
     *   protected boolean isIgnored(SpringApplication springApplication, String[] args,
     *                               ConfigurableApplicationContext context) {
     *       return "test".equals(context.getId());
     *   }
     * }</pre>
     *
     * @param springApplication the Spring application instance
     * @param args              the command-line arguments
     * @param context           the configurable application context
     * @return {@code true} if the context should be ignored, {@code false} otherwise
     */
    protected abstract boolean isIgnored(SpringApplication springApplication, String[] args, ConfigurableApplicationContext context);

    /**
     * Called when an {@link ApplicationPreparedEvent} is received for a context that has not
     * yet been processed and is not ignored. Subclasses implement this to define
     * their one-time initialization logic.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   @Override
     *   protected void onApplicationEvent(SpringApplication springApplication, String[] args,
     *                                     ConfigurableApplicationContext context) {
     *       System.out.println("Context prepared: " + context.getId());
     *   }
     * }</pre>
     *
     * @param springApplication the Spring application instance
     * @param args              the command-line arguments
     * @param context           the configurable application context
     */
    protected abstract void onApplicationEvent(SpringApplication springApplication, String[] args, ConfigurableApplicationContext context);

    /**
     * Sets the order value for this listener, controlling its execution priority among
     * other {@link Ordered} listeners.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   OnceApplicationPreparedEventListener listener = ...;
     *   listener.setOrder(Ordered.HIGHEST_PRECEDENCE);
     * }</pre>
     *
     * @param order the order value (lower values have higher priority)
     */
    public final void setOrder(int order) {
        this.order = order;
    }

    /**
     * Returns the order value of this listener, as set by {@link #setOrder(int)}.
     * Defaults to {@link Ordered#LOWEST_PRECEDENCE}.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   int order = listener.getOrder();
     *   System.out.println("Listener order: " + order);
     * }</pre>
     *
     * @return the order value
     */
    @Override
    public final int getOrder() {
        return order;
    }

    private static Set<String> getProcessedContextIds(Class<? extends ApplicationListener> listenerClass) {
        return listenerProcessedContextIds.computeIfAbsent(listenerClass, type -> new ConcurrentSkipListSet<>());
    }
}