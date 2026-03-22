package io.microsphere.spring.boot.report;

import io.microsphere.annotation.Nonnull;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionEvaluationReport;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.Collections.unmodifiableMap;
import static org.springframework.util.ObjectUtils.identityToString;

/**
 * Spring Boot Condition evaluation report builder
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
abstract class ConditionEvaluationReportBuilder {

    private static final Map<ConfigurableListableBeanFactory, ConditionEvaluationReport> reports = new ConcurrentHashMap<>();

    /**
     * Build a {@link ConditionEvaluationReport} for the given {@link ConfigurableListableBeanFactory}.
     * If a report has already been built for the given bean factory, the cached report is returned.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   ConfigurableListableBeanFactory beanFactory = context.getBeanFactory();
     *   ConditionEvaluationReport report = ConditionEvaluationReportBuilder.build(beanFactory);
     * }</pre>
     *
     * @param beanFactory the {@link ConfigurableListableBeanFactory} to build the report for
     * @return the {@link ConditionEvaluationReport} for the given bean factory
     */
    static ConditionEvaluationReport build(ConfigurableListableBeanFactory beanFactory) {
        return reports.computeIfAbsent(beanFactory, ConditionEvaluationReport::get);
    }

    /**
     * Return an unmodifiable map of all built {@link ConditionEvaluationReport} instances,
     * keyed by the bean factory identifier.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   Map<String, ConditionEvaluationReport> reports = ConditionEvaluationReportBuilder.getReportsMap();
     *   reports.forEach((id, report) -> {
     *       System.out.println("Context: " + id + ", Exclusions: " + report.getExclusions());
     *   });
     * }</pre>
     *
     * @return an unmodifiable map of bean factory identifiers to their {@link ConditionEvaluationReport}
     */
    static Map<String, ConditionEvaluationReport> getReportsMap() {
        Map<String, ConditionEvaluationReport> reportsMap = new LinkedHashMap<>(reports.size());
        reports.forEach((beanFactory, report) -> {
            String id = getBeanFactoryId(beanFactory);
            reportsMap.put(id, report);
        });
        return unmodifiableMap(reportsMap);
    }

    /**
     * Get the identifier for the given {@link ConfigurableListableBeanFactory}.
     * If the bean factory is a {@link DefaultListableBeanFactory}, its serialization ID is returned;
     * otherwise, an identity string representation is used.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   ConfigurableListableBeanFactory beanFactory = context.getBeanFactory();
     *   String id = ConditionEvaluationReportBuilder.getBeanFactoryId(beanFactory);
     * }</pre>
     *
     * @param beanFactory the {@link ConfigurableListableBeanFactory} to get the identifier for
     * @return the identifier of the bean factory, never {@code null}
     */
    @Nonnull
    static String getBeanFactoryId(ConfigurableListableBeanFactory beanFactory) {
        if (beanFactory instanceof DefaultListableBeanFactory) {
            return ((DefaultListableBeanFactory) beanFactory).getSerializationId();
        }
        return identityToString(beanFactory);
    }

    private ConditionEvaluationReportBuilder() {
    }
}