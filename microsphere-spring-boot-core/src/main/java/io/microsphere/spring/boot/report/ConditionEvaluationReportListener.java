package io.microsphere.spring.boot.report;

import io.microsphere.logging.Logger;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;

import static io.microsphere.logging.LoggerFactory.getLogger;

/**
 * Spring Boot Conditions Evaluation report listener
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public class ConditionEvaluationReportListener implements ApplicationListener<ApplicationReadyEvent> {

    private static final Logger logger = getLogger(ConditionEvaluationReportListener.class);

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        reportConditions(event.getApplicationContext());
    }

    protected void reportConditions(ConfigurableApplicationContext context) {
        ConditionsReportMessageBuilder messageBuilder = new ConditionsReportMessageBuilder(context);
        messageBuilder.build().forEach(logger::info);
    }
}