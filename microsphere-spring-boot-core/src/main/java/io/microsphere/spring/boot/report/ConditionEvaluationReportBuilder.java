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

    static ConditionEvaluationReport build(ConfigurableListableBeanFactory beanFactory) {
        return reports.computeIfAbsent(beanFactory, ConditionEvaluationReport::get);
    }

    static Map<String, ConditionEvaluationReport> getReportsMap() {
        Map<String, ConditionEvaluationReport> reportsMap = new LinkedHashMap<>(reports.size());
        reports.forEach((beanFactory, report) -> {
            String id = getBeanFactoryId(beanFactory);
            reportsMap.put(id, report);
        });
        return unmodifiableMap(reportsMap);
    }

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