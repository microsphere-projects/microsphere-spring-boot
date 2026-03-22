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
     *       public MyRunListener(SpringApplication application, String[] args) {
     *           super(application, args);
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
     * Called when the application is starting, with the {@link ConfigurableBootstrapContext} available.
     * Subclasses may override this method to perform custom logic during the starting phase.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   @Override
     *   public void starting(ConfigurableBootstrapContext bootstrapContext) {
     *       // perform custom initialization during application starting
     *   }
     * }</pre>
     *
     * @param bootstrapContext the bootstrap context
     */
    public void starting(ConfigurableBootstrapContext bootstrapContext) {
    }

    /**
     * Called when the application is starting, without a bootstrap context.
     * Subclasses may override this method to perform custom logic during the starting phase.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   @Override
     *   public void starting() {
     *       // perform custom initialization during application starting
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
     *       // customize environment properties before context creation
     *   }
     * }</pre>
     *
     * @param environment the prepared {@link ConfigurableEnvironment}
     */
    public void environmentPrepared(ConfigurableEnvironment environment) {
    }

    /**
     * {@inheritDoc}
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   @Override
     *   public void contextPrepared(ConfigurableApplicationContext context) {
     *       // perform logic after the application context has been prepared
     *   }
     * }</pre>
     */
    @Override
    public void contextPrepared(ConfigurableApplicationContext context) {
    }

    /**
     * {@inheritDoc}
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   @Override
     *   public void contextLoaded(ConfigurableApplicationContext context) {
     *       // perform logic after the application context has been loaded
     *   }
     * }</pre>
     */
    @Override
    public void contextLoaded(ConfigurableApplicationContext context) {
    }

    /**
     * {@inheritDoc}
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   @Override
     *   public void started(ConfigurableApplicationContext context) {
     *       // perform logic after the application context has been started
     *   }
     * }</pre>
     */
    @Override
    public void started(ConfigurableApplicationContext context) {
    }

    /**
     * {@inheritDoc}
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   @Override
     *   public void running(ConfigurableApplicationContext context) {
     *       // perform logic when the application is running
     *   }
     * }</pre>
     */
    @Override
    public void running(ConfigurableApplicationContext context) {
    }

    /**
     * {@inheritDoc}
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   @Override
     *   public void failed(ConfigurableApplicationContext context, Throwable exception) {
     *       // handle application startup failure
     *       exception.printStackTrace();
     *   }
     * }</pre>
     */
    @Override
    public void failed(ConfigurableApplicationContext context, Throwable exception) {
    }

    /**
     * Return the {@link SpringApplication} instance associated with this listener.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   SpringApplication app = listener.getSpringApplication();
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

    @Override
    public final int getOrder() {
        return order;
    }
}