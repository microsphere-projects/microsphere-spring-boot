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
     * Determines whether the given application context should be ignored by delegating to
     * {@link #isIgnored(ConfigurableApplicationContext)}. This final implementation ignores
     * the {@link SpringApplication} and args parameters.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   // Typically invoked internally by the framework:
     *   boolean ignored = listener.isIgnored(springApplication, args, context);
     * }</pre>
     *
     * @param springApplication the Spring application instance
     * @param args              the command-line arguments
     * @param context           the configurable application context
     * @return {@code true} if the context should be ignored, {@code false} otherwise
     */
    protected final boolean isIgnored(SpringApplication springApplication, String[] args, ConfigurableApplicationContext context) {
        return isIgnored(context);
    }

    /**
     * Determines whether the given application context should be ignored. A context is ignored
     * if it is a bootstrap context or not the main application context when the
     * {@code BootstrapApplicationListener} is present on the classpath.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   OnceMainApplicationPreparedEventListener listener = ...;
     *   boolean ignored = listener.isIgnored(applicationContext);
     *   if (!ignored) {
     *       // proceed with main-context-only logic
     *   }
     * }</pre>
     *
     * @param context the configurable application context to evaluate
     * @return {@code true} if the context should be ignored, {@code false} otherwise
     */
    protected boolean isIgnored(ConfigurableApplicationContext context) {
        if (isBootstrapApplicationListenerPresent(context)) {
            return isBootstrapContext(context) || !isMainApplicationContext(context);
        }
        return false;
    }

    /**
     * Checks whether the given application context is a Spring Cloud bootstrap context
     * by comparing its {@linkplain ConfigurableApplicationContext#getId() id} with the
     * configured bootstrap context id.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   boolean bootstrap = listener.isBootstrapContext(applicationContext);
     *   if (bootstrap) {
     *       // skip bootstrap-specific context
     *   }
     * }</pre>
     *
     * @param context the configurable application context to check
     * @return {@code true} if the context is a bootstrap context, {@code false} otherwise
     */
    boolean isBootstrapContext(ConfigurableApplicationContext context) {
        return getBootstrapContextId(context).equals(context.getId());
    }

    /**
     * Determines whether the given application context is the main application context.
     * The main context is identified by having a parent that is the bootstrap context.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   boolean isMain = listener.isMainApplicationContext(applicationContext);
     *   if (isMain) {
     *       // perform main-context initialization
     *   }
     * }</pre>
     *
     * @param context the configurable application context to check
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
     * Retrieves the bootstrap context id from the given application context's environment.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   String bootstrapId = listener.getBootstrapContextId(applicationContext);
     *   System.out.println("Bootstrap context id: " + bootstrapId);
     * }</pre>
     *
     * @param context the configurable application context
     * @return the bootstrap context id, defaulting to {@value #DEFAULT_BOOTSTRAP_CONTEXT_ID}
     */
    @Nonnull
    String getBootstrapContextId(ConfigurableApplicationContext context) {
        return getBootstrapContextId(context.getEnvironment());
    }

    /**
     * Retrieves the bootstrap context id from the given environment by reading the
     * {@value #BOOTSTRAP_CONTEXT_ID_PROPERTY_NAME} property.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   ConfigurableEnvironment env = applicationContext.getEnvironment();
     *   String bootstrapId = listener.getBootstrapContextId(env);
     * }</pre>
     *
     * @param environment the configurable environment to read the property from
     * @return the bootstrap context id, defaulting to {@value #DEFAULT_BOOTSTRAP_CONTEXT_ID}
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