package io.microsphere.spring.boot.listener;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringApplicationRunListener;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.PrintStream;

/**
 * {@link SpringApplicationRunListener} Failure Report
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public class FailureReportSpringApplicationRunListener extends SpringApplicationRunListenerAdapter {

    /**
     * Construct a new {@link FailureReportSpringApplicationRunListener} for failure reporting.
     * When the application fails to start, the failure details are printed to {@link System#err}.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   // Typically registered in META-INF/spring.factories:
     *   // org.springframework.boot.SpringApplicationRunListener=\
     *   //   io.microsphere.spring.boot.listener.FailureReportSpringApplicationRunListener
     * }</pre>
     *
     * @param springApplication the {@link SpringApplication} instance
     * @param args the command line arguments
     */
    public FailureReportSpringApplicationRunListener(SpringApplication springApplication, String[] args) {
        super(springApplication, args);
    }

    @Override
    public void failed(ConfigurableApplicationContext context, Throwable exception) {
        PrintStream err = System.err;
        err.print("The Spring Boot application fails to start. The causes are as follows:");
        exception.printStackTrace(err);
    }
}