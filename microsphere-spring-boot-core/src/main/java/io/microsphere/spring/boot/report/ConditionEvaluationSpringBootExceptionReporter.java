package io.microsphere.spring.boot.report;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.boot.SpringBootExceptionReporter;
import org.springframework.boot.autoconfigure.condition.ConditionEvaluationReport;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * Spring Boot Conditions {@link SpringBootExceptionReporter}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see ConditionEvaluationReport
 * @since 1.0.0
 */
public class ConditionEvaluationSpringBootExceptionReporter implements SpringBootExceptionReporter {

    private static final Log logger = LogFactory.getLog(ConditionEvaluationSpringBootExceptionReporter.class);

    private final ConfigurableApplicationContext context;

    public ConditionEvaluationSpringBootExceptionReporter(ConfigurableApplicationContext context) {
        this.context = context;
    }

    @Override
    public boolean reportException(Throwable failure) {
        logger.error("Spring Boot fails to start!", failure);
        reportConditions(context);
        return true;
    }

    protected void reportConditions(ConfigurableApplicationContext context) {
        ConditionsReportMessageBuilder messageBuilder = new ConditionsReportMessageBuilder(context);
        messageBuilder.build().forEach(logger::error);
    }
}
