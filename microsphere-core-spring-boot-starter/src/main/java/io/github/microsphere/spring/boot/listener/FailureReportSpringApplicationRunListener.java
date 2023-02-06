package io.github.microsphere.spring.boot.listener;

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
