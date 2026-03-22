package io.microsphere.spring.boot.context;

import io.microsphere.annotation.Nonnull;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.event.ApplicationPreparedEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;

import static org.springframework.util.ClassUtils.isPresent;


/**
 * Once execution {@link ApplicationPreparedEvent} {@link ApplicationListener} for Main {@link ApplicationContext}.
 * <p>
 * This listener is designed to execute only once for the main application context, ignoring any bootstrap contexts
 * that might be created by Spring Cloud. It extends {@link OnceApplicationPreparedEventListener} to provide
 * specialized behavior for main application contexts.
 *
 * <h3>Example Usage</h3>
 * <pre>{@code
 * public class MyMainApplicationListener extends OnceMainApplicationPreparedEventListener {
 *
 *     @Override
 *     protected void onMainApplicationPrepared(SpringApplication springApplication, String[] args,
 *                                              ConfigurableApplicationContext context) {
 *         // Your custom logic here - this will only execute for the main application context
 *         System.out.println("Main application context is prepared: " + context.getApplicationName());
 *     }
 * }
 * }</pre>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see OnceApplicationPreparedEventListener
 * @since 1.0.0
 */
public abstract class OnceMainApplicationPreparedEventListener extends OnceApplicationPreparedEventListener {

    /**
     * The class name for {@link org.springframework.cloud.bootstrap.BootstrapApplicationListener}
     */
    static final String BOOTSTRAP_APPLICATION_LISTENER_CLASS_NAME = "org.springframework.cloud.bootstrap.BootstrapApplicationListener";

    /**
     * The property name for bootstrap {@link ApplicationContext context} {@link ConfigurableApplicationContext#getId() id}
     */
    static final String BOOTSTRAP_CONTEXT_ID_PROPERTY_NAME = "spring.cloud.bootstrap.name";

    /**
     * The default bootstrap {@link ApplicationContext context} {@link ConfigurableApplicationContext#getId() id} : "bootstrap"
     */
    static final String DEFAULT_BOOTSTRAP_CONTEXT_ID = "bootstrap";

    /**
     * Determines whether the given {@link ApplicationPreparedEvent} should be ignored by
     * delegating to {@link #isIgnored(ConfigurableApplicationContext)}.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   // This method is called internally by OnceApplicationPreparedEventListener.
     *   // Override isIgnored(ConfigurableApplicationContext) instead for custom logic:
     *   public class MyMainListener extends OnceMainApplicationPreparedEventListener {
     *       protected void onApplicationEvent(SpringApplication app, String[] args,
     *               ConfigurableApplicationContext context) {
     *           // handle event for main application context only
     *       }
     *   }
     * }</pre>
     *
     * @param springApplication the current {@link SpringApplication}
     * @param args              the application arguments
     * @param context           the application context being prepared
     * @return {@code true} if the event should be ignored
     */
    protected final boolean isIgnored(SpringApplication springApplication, String[] args, ConfigurableApplicationContext context) {
        return isIgnored(context);
    }

    /**
     * Determines whether the given application context should be ignored by this listener.
     * A context is ignored if the Spring Cloud Bootstrap listener is present and the context
     * is either a bootstrap context or not the main application context.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   ConfigurableApplicationContext context = new GenericApplicationContext();
     *   ConfigurableApplicationContext parentContext = new GenericApplicationContext();
     *   parentContext.setId("bootstrap");
     *   context.setParent(parentContext);
     *   boolean ignored = listener.isIgnored(context); // false — this is the main context
     * }</pre>
     *
     * @param context the {@link ConfigurableApplicationContext} to evaluate
     * @return {@code true} if the context should be ignored, {@code false} otherwise
     */
    protected boolean isIgnored(ConfigurableApplicationContext context) {
        if (isBootstrapApplicationListenerPresent(context)) {
            return isBootstrapContext(context) || !isMainApplicationContext(context);
        }
        return false;
    }

    /**
     * Determines whether the given application context is a Spring Cloud bootstrap context
     * by comparing its ID with the configured bootstrap context ID.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   ConfigurableApplicationContext context = new GenericApplicationContext();
     *   context.setId("bootstrap");
     *   boolean result = listener.isBootstrapContext(context); // true
     * }</pre>
     *
     * @param context the {@link ConfigurableApplicationContext} to check
     * @return {@code true} if the context is a bootstrap context, {@code false} otherwise
     */
    boolean isBootstrapContext(ConfigurableApplicationContext context) {
        return getBootstrapContextId(context).equals(context.getId());
    }

    /**
     * Determines whether the given application context is the main application context.
     * A context is considered "main" if its parent is the bootstrap context.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   ConfigurableApplicationContext parentContext = new GenericApplicationContext();
     *   parentContext.setId("bootstrap");
     *   ConfigurableApplicationContext context = new GenericApplicationContext();
     *   context.setParent(parentContext);
     *   boolean result = listener.isMainApplicationContext(context); // true
     * }</pre>
     *
     * @param context the {@link ConfigurableApplicationContext} to check
     * @return {@code true} if the context is the main application context, {@code false} otherwise
     */
    boolean isMainApplicationContext(ConfigurableApplicationContext context) {
        boolean main = true;
        String parentId = null;
        ApplicationContext parentContext = context.getParent();
        if (parentContext instanceof ConfigurableApplicationContext) {
            parentId = parentContext.getId();
            main = isBootstrapContext((ConfigurableApplicationContext) parentContext);
        }

        logger.trace("Current ApplicationContext[id : '{}' , parentId : '{}'] is {}main ApplicationContext",
                context.getId(), parentId, main ? "" : "not ");

        return main;
    }

    /**
     * Retrieves the bootstrap context ID from the given application context's environment.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   ConfigurableApplicationContext context = new GenericApplicationContext();
     *   String bootstrapId = listener.getBootstrapContextId(context); // "bootstrap" by default
     * }</pre>
     *
     * @param context the {@link ConfigurableApplicationContext} whose environment provides the ID
     * @return the bootstrap context ID, defaults to {@value #DEFAULT_BOOTSTRAP_CONTEXT_ID}
     */
    @Nonnull
    String getBootstrapContextId(ConfigurableApplicationContext context) {
        return getBootstrapContextId(context.getEnvironment());
    }

    /**
     * Retrieves the bootstrap context ID from the given environment by reading the
     * {@value #BOOTSTRAP_CONTEXT_ID_PROPERTY_NAME} property.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   ConfigurableEnvironment environment = new StandardEnvironment();
     *   String bootstrapId = listener.getBootstrapContextId(environment); // "bootstrap" by default
     * }</pre>
     *
     * @param environment the {@link ConfigurableEnvironment} to read the property from
     * @return the bootstrap context ID, defaults to {@value #DEFAULT_BOOTSTRAP_CONTEXT_ID}
     */
    @Nonnull
    String getBootstrapContextId(ConfigurableEnvironment environment) {
        return environment.getProperty(BOOTSTRAP_CONTEXT_ID_PROPERTY_NAME, DEFAULT_BOOTSTRAP_CONTEXT_ID);
    }

    private boolean isBootstrapApplicationListenerPresent(ConfigurableApplicationContext context) {
        ClassLoader classLoader = context.getClassLoader();
        return isPresent(BOOTSTRAP_APPLICATION_LISTENER_CLASS_NAME, classLoader);
    }
}