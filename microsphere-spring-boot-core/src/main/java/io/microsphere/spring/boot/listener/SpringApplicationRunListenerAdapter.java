package io.microsphere.spring.boot.listener;

import org.springframework.boot.ConfigurableBootstrapContext;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringApplicationRunListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;

/**
 * {@link SpringApplicationRunListener} Adapter
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public abstract class SpringApplicationRunListenerAdapter implements SpringApplicationRunListener, Ordered {

    protected final SpringApplication springApplication;

    protected final String[] args;

    private int order;

    /**
     * Construct a new {@link SpringApplicationRunListenerAdapter} with the given application and arguments.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   public class MyRunListener extends SpringApplicationRunListenerAdapter {
     *       public MyRunListener(SpringApplication app, String[] args) {
     *           super(app, args);
     *       }
     *   }
     * }</pre>
     *
     * @param springApplication the {@link SpringApplication} instance
     * @param args the command line arguments
     */
    public SpringApplicationRunListenerAdapter(SpringApplication springApplication, String[] args) {
        this.springApplication = springApplication;
        this.args = args;
        this.setOrder(0);
    }

    /**
     * Called when the application is starting up with a {@link ConfigurableBootstrapContext}.
     * Subclasses may override this method to perform custom logic during the starting phase.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   @Override
     *   public void starting(ConfigurableBootstrapContext bootstrapContext) {
     *       System.out.println("Application is starting with bootstrap context");
     *   }
     * }</pre>
     *
     * @param bootstrapContext the {@link ConfigurableBootstrapContext}
     */
    public void starting(ConfigurableBootstrapContext bootstrapContext) {
    }

    /**
     * Called when the application is starting up (legacy callback without bootstrap context).
     * Subclasses may override this method to perform custom logic during the starting phase.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   @Override
     *   public void starting() {
     *       System.out.println("Application is starting");
     *   }
     * }</pre>
     */
    public void starting() {
    }

    /**
     * Called when the {@link ConfigurableEnvironment} has been prepared.
     * Subclasses may override this method to customize the environment before the context is created.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   @Override
     *   public void environmentPrepared(ConfigurableEnvironment environment) {
     *       environment.getPropertySources().addLast(myPropertySource);
     *   }
     * }</pre>
     *
     * @param environment the prepared {@link ConfigurableEnvironment}
     */
    public void environmentPrepared(ConfigurableEnvironment environment) {
    }

    /**
     * Called when the {@link ConfigurableApplicationContext} has been prepared but before bean definitions are loaded.
     * Subclasses may override this method to perform custom logic after context preparation.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   @Override
     *   public void contextPrepared(ConfigurableApplicationContext context) {
     *       System.out.println("Context prepared: " + context.getId());
     *   }
     * }</pre>
     *
     * @param context the prepared {@link ConfigurableApplicationContext}
     */
    @Override
    public void contextPrepared(ConfigurableApplicationContext context) {
    }

    /**
     * Called when the {@link ConfigurableApplicationContext} has been loaded with bean definitions
     * but not yet refreshed.
     * Subclasses may override this method to perform custom logic after context loading.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   @Override
     *   public void contextLoaded(ConfigurableApplicationContext context) {
     *       System.out.println("Context loaded: " + context.getId());
     *   }
     * }</pre>
     *
     * @param context the loaded {@link ConfigurableApplicationContext}
     */
    @Override
    public void contextLoaded(ConfigurableApplicationContext context) {
    }

    /**
     * Called when the application context has been refreshed and started.
     * Subclasses may override this method to perform custom logic after the context is started.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   @Override
     *   public void started(ConfigurableApplicationContext context) {
     *       System.out.println("Application started: " + context.getId());
     *   }
     * }</pre>
     *
     * @param context the started {@link ConfigurableApplicationContext}
     */
    @Override
    public void started(ConfigurableApplicationContext context) {
    }

    /**
     * Called when the application is fully running.
     * Subclasses may override this method to perform custom logic when the application is ready.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   @Override
     *   public void running(ConfigurableApplicationContext context) {
     *       System.out.println("Application running: " + context.getId());
     *   }
     * }</pre>
     *
     * @param context the running {@link ConfigurableApplicationContext}
     */
    @Override
    public void running(ConfigurableApplicationContext context) {
    }

    /**
     * Called when the application has failed to start.
     * Subclasses may override this method to handle startup failures.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   @Override
     *   public void failed(ConfigurableApplicationContext context, Throwable exception) {
     *       System.err.println("Application failed: " + exception.getMessage());
     *   }
     * }</pre>
     *
     * @param context   the {@link ConfigurableApplicationContext}, may be {@code null}
     * @param exception the exception that caused the failure
     */
    @Override
    public void failed(ConfigurableApplicationContext context, Throwable exception) {
    }

    /**
     * Return the underlying {@link SpringApplication} instance.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   SpringApplication app = listener.getSpringApplication();
     *   Class<?> mainClass = app.getMainApplicationClass();
     * }</pre>
     *
     * @return the {@link SpringApplication} instance
     */
    public final SpringApplication getSpringApplication() {
        return springApplication;
    }

    /**
     * Return the command line arguments passed to the application.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   String[] args = listener.getArgs();
     *   for (String arg : args) {
     *       System.out.println("Argument: " + arg);
     *   }
     * }</pre>
     *
     * @return the command line arguments
     */
    public final String[] getArgs() {
        return args;
    }

    /**
     * Set the order of this listener. Lower values have higher priority.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   listener.setOrder(Ordered.HIGHEST_PRECEDENCE);
     * }</pre>
     *
     * @param order the order value
     */
    public final void setOrder(int order) {
        this.order = order;
    }

    /**
     * Return the order of this listener. Lower values have higher priority.
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
}