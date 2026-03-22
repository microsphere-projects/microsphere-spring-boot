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
     * Construct a new {@link FailureReportSpringApplicationRunListener}.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   // Typically instantiated by Spring Boot via spring.factories
     *   // io.microsphere.spring.boot.listener.FailureReportSpringApplicationRunListener
     *   SpringApplication app = new SpringApplication(MyApplication.class);
     *   app.run(args);
     * }</pre>
     *
     * @param springApplication the {@link SpringApplication} instance
     * @param args the command line arguments
     */
    public FailureReportSpringApplicationRunListener(SpringApplication springApplication, String[] args) {
        super(springApplication, args);
    }

    /**
     * Handles application startup failure by printing the exception details to {@link System#err}.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   // This method is called automatically by Spring Boot when the application fails to start.
     *   // The failure report will be printed to System.err:
     *   // "The Spring Boot application fails to start. The causes are as follows:"
     *   // followed by the stack trace.
     * }</pre>
     *
     * @param context   the {@link ConfigurableApplicationContext}, may be {@code null}
     * @param exception the exception that caused the failure
     */
    @Override
    public void failed(ConfigurableApplicationContext context, Throwable exception) {
        PrintStream err = System.err;
        err.print("The Spring Boot application fails to start. The causes are as follows:");
        exception.printStackTrace(err);
    }
}