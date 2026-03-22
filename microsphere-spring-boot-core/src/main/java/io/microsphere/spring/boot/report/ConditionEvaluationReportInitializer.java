package io.microsphere.spring.boot.report;

import org.springframework.boot.autoconfigure.condition.ConditionEvaluationReport;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * {@link ConditionEvaluationReport} initializer
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public class ConditionEvaluationReportInitializer implements ApplicationContextInitializer {

    /**
     * Initializes the {@link ConditionEvaluationReport} by registering a
     * {@link org.springframework.beans.factory.config.BeanFactoryPostProcessor} that builds
     * the report for the application context's bean factory.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   ConditionEvaluationReportInitializer initializer = new ConditionEvaluationReportInitializer();
     *   initializer.initialize(applicationContext);
     * }</pre>
     *
     * @param applicationContext the {@link ConfigurableApplicationContext} to initialize
     */
    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        applicationContext.addBeanFactoryPostProcessor(ConditionEvaluationReportBuilder::build);
    }
}