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

    protected final boolean isIgnored(SpringApplication springApplication, String[] args, ConfigurableApplicationContext context) {
        return isIgnored(context);
    }

    protected boolean isIgnored(ConfigurableApplicationContext context) {
        if (isBootstrapApplicationListenerPresent(context)) {
            return isBootstrapContext(context) || !isMainApplicationContext(context);
        }
        return false;
    }

    boolean isBootstrapContext(ConfigurableApplicationContext context) {
        return getBootstrapContextId(context).equals(context.getId());
    }

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

    @Nonnull
    String getBootstrapContextId(ConfigurableApplicationContext context) {
        return getBootstrapContextId(context.getEnvironment());
    }

    @Nonnull
    String getBootstrapContextId(ConfigurableEnvironment environment) {
        return environment.getProperty(BOOTSTRAP_CONTEXT_ID_PROPERTY_NAME, DEFAULT_BOOTSTRAP_CONTEXT_ID);
    }

    private boolean isBootstrapApplicationListenerPresent(ConfigurableApplicationContext context) {
        ClassLoader classLoader = context.getClassLoader();
        return isPresent(BOOTSTRAP_APPLICATION_LISTENER_CLASS_NAME, classLoader);
    }
}