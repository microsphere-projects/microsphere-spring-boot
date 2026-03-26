package io.microsphere.spring.boot.report;

import io.microsphere.logging.Logger;
import org.springframework.boot.SpringBootExceptionReporter;
import org.springframework.boot.autoconfigure.condition.ConditionEvaluationReport;
import org.springframework.context.ConfigurableApplicationContext;

import static io.microsphere.logging.LoggerFactory.getLogger;

/**
 * Spring Boot Conditions {@link SpringBootExceptionReporter}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see ConditionEvaluationReport
 * @since 1.0.0
 */
public class ConditionEvaluationSpringBootExceptionReporter implements SpringBootExceptionReporter {

    private static final Logger logger = getLogger(ConditionEvaluationSpringBootExceptionReporter.class);

    private final ConfigurableApplicationContext context;

    /**
     * Constructs a new {@link ConditionEvaluationSpringBootExceptionReporter} with the given application context.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   ConfigurableApplicationContext context = SpringApplication.run(MyApp.class, args);
     *   ConditionEvaluationSpringBootExceptionReporter reporter =
     *       new ConditionEvaluationSpringBootExceptionReporter(context);
     * }</pre>
     *
     * @param context the {@link ConfigurableApplicationContext} to report conditions for
     */
    public ConditionEvaluationSpringBootExceptionReporter(ConfigurableApplicationContext context) {
        this.context = context;
    }

    /**
     * Reports the exception by logging the failure and outputting condition evaluation details.
     * This override logs the startup failure at error level and delegates to
     * {@link #reportConditions(ConfigurableApplicationContext)} for detailed condition reporting.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   SpringBootExceptionReporter reporter =
     *       new ConditionEvaluationSpringBootExceptionReporter(context);
     *   boolean handled = reporter.reportException(new RuntimeException("Startup failed"));
     * }</pre>
     *
     * @param failure the {@link Throwable} that caused the application startup to fail
     * @return {@code false} always, indicating the exception was not fully handled
     */
    @Override
    public boolean reportException(Throwable failure) {
        logger.error("Spring Boot fails to start!", failure);
        reportConditions(context);
        return false;
    }

    /**
     * Reports the condition evaluation details for the given application context by building
     * and logging condition report messages at error level.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   ConditionEvaluationSpringBootExceptionReporter reporter =
     *       new ConditionEvaluationSpringBootExceptionReporter(context);
     *   reporter.reportConditions(context);
     * }</pre>
     *
     * @param context the {@link ConfigurableApplicationContext} whose conditions to report
     */
    protected void reportConditions(ConfigurableApplicationContext context) {
        ConditionsReportMessageBuilder messageBuilder = new ConditionsReportMessageBuilder(context);
        messageBuilder.build().forEach(logger::error);
    }
}