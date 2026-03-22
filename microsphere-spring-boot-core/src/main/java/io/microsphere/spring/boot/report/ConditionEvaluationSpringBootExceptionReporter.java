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
     * Construct a new {@link ConditionEvaluationSpringBootExceptionReporter} with the given application context.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   // Typically registered in META-INF/spring.factories:
     *   // org.springframework.boot.SpringBootExceptionReporter=\
     *   //   io.microsphere.spring.boot.report.ConditionEvaluationSpringBootExceptionReporter
     * }</pre>
     *
     * @param context the {@link ConfigurableApplicationContext} to report conditions for
     */
    public ConditionEvaluationSpringBootExceptionReporter(ConfigurableApplicationContext context) {
        this.context = context;
    }

    @Override
    public boolean reportException(Throwable failure) {
        logger.error("Spring Boot fails to start!", failure);
        reportConditions(context);
        return false;
    }

    /**
     * Report condition evaluation results for the given application context.
     * Builds the condition evaluation report messages and logs each one at error level.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   ConditionEvaluationSpringBootExceptionReporter reporter =
     *       new ConditionEvaluationSpringBootExceptionReporter(context);
     *   reporter.reportConditions(context);
     * }</pre>
     *
     * @param context the {@link ConfigurableApplicationContext} to report conditions for
     */
    protected void reportConditions(ConfigurableApplicationContext context) {
        ConditionsReportMessageBuilder messageBuilder = new ConditionsReportMessageBuilder(context);
        messageBuilder.build().forEach(logger::error);
    }
}