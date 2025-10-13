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
public class SpringApplicationRunListenerAdapter implements SpringApplicationRunListener, Ordered {

    protected final SpringApplication springApplication;

    protected final String[] args;

    private int order;

    public SpringApplicationRunListenerAdapter(SpringApplication springApplication, String[] args) {
        this.springApplication = springApplication;
        this.args = args;
        this.setOrder(0);
    }

    public void starting(ConfigurableBootstrapContext bootstrapContext) {
    }

    public void starting() {
    }

    public void environmentPrepared(ConfigurableEnvironment environment) {
    }

    @Override
    public void contextPrepared(ConfigurableApplicationContext context) {
    }

    @Override
    public void contextLoaded(ConfigurableApplicationContext context) {
    }

    @Override
    public void started(ConfigurableApplicationContext context) {
    }

    @Override
    public void running(ConfigurableApplicationContext context) {
    }

    @Override
    public void failed(ConfigurableApplicationContext context, Throwable exception) {
    }

    public final SpringApplication getSpringApplication() {
        return springApplication;
    }

    public final String[] getArgs() {
        return args;
    }

    public final void setOrder(int order) {
        this.order = order;
    }

    @Override
    public final int getOrder() {
        return order;
    }
}
