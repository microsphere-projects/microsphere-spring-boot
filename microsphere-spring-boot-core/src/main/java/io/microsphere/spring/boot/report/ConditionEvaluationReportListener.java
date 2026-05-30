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

    /**
     * Handles the {@link ApplicationReadyEvent} by reporting the condition evaluation results
     * for the application context that triggered the event.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   ConditionEvaluationReportListener listener = new ConditionEvaluationReportListener();
     *   // Typically invoked by the Spring event system:
     *   listener.onApplicationEvent(applicationReadyEvent);
     * }</pre>
     *
     * @param event the {@link ApplicationReadyEvent} indicating the application is ready
     */
    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        reportConditions(event.getApplicationContext());
    }

    /**
     * Reports the condition evaluation details for the given application context by building
     * and logging condition report messages at info level.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   ConditionEvaluationReportListener listener = new ConditionEvaluationReportListener();
     *   listener.reportConditions(applicationContext);
     * }</pre>
     *
     * @param context the {@link ConfigurableApplicationContext} whose conditions to report
     */
    protected void reportConditions(ConfigurableApplicationContext context) {
        ConditionsReportMessageBuilder messageBuilder = new ConditionsReportMessageBuilder(context);
        messageBuilder.build().forEach(logger::info);
    }
}