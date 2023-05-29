package io.microsphere.spring.boot.report;

import io.microsphere.text.FormatUtils;
import org.springframework.boot.autoconfigure.condition.ConditionEvaluationReport;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Spring Boot Conditions Report builder
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public class ConditionsReportMessageBuilder {

    public static final String BASE_PACKAGES_PROPERTY_NAME = "microsphere.conditions.report.base-packages";

    private static final Set<String> DEFAULT_BASE_PACKAGES = Collections.singleton("io.github.microsphere");

    private final ConfigurableApplicationContext context;

    public ConditionsReportMessageBuilder(ConfigurableApplicationContext context) {
        this.context = context;
    }

    List<String> build() {
        Map<String, ConditionEvaluationReport> reportsMap = ConditionEvaluationReportBuilder.getReportsMap();
        List<String> reportMessages = new LinkedList<>();
        reportsMap.forEach((id, report) -> {
            reportMessages.add(buildSingle(id, report));
        });
        return reportMessages;
    }

    String buildSingle(String id, ConditionEvaluationReport report) {
        StringBuilder reportMessage = new StringBuilder(System.lineSeparator());
        appendTitle(id, reportMessage);
        appendExclusions(report, reportMessage);
        appendUnconditionalClasses(report, reportMessage);
        appendConditionAndOutcomes(report, reportMessage);
        return reportMessage.toString();
    }

    private void appendTitle(String id, StringBuilder reportMessage) {
        appendLine(reportMessage, "Spring Boot condition information[context: {}]：", id);
    }

    private void appendExclusions(ConditionEvaluationReport report, StringBuilder reportMessage) {
        appendLine(reportMessage, "Conditional exclusion list:{}", report.getExclusions());
    }

    private void appendUnconditionalClasses(ConditionEvaluationReport report, StringBuilder reportMessage) {
        appendLine(reportMessage, "List of non-conditional classes:{}", report.getUnconditionalClasses());
    }

    private void appendConditionAndOutcomes(ConditionEvaluationReport report, StringBuilder reportMessage) {
        Set<String> basePackages = getBasePackages(context);
        Map<String, ConditionEvaluationReport.ConditionAndOutcomes> conditionAndOutcomesMap = report.getConditionAndOutcomesBySource();
        for (Map.Entry<String, ConditionEvaluationReport.ConditionAndOutcomes> entry : conditionAndOutcomesMap.entrySet()) {
            appendConditionAndOutcomes(entry.getKey(), entry.getValue(), basePackages, reportMessage);
        }
    }

    private void appendConditionAndOutcomes(String source, ConditionEvaluationReport.ConditionAndOutcomes conditionAndOutcomes,
                                            Set<String> basePackages, StringBuilder reportMessage) {
        boolean matched = false;

        for (String backPackage : basePackages) {
            if (source.startsWith(backPackage)) {
                matched = true;
                break;
            }
        }

        if (matched) {
            appendLine(reportMessage, "Bean definition source:{} , {}", source, combineOutcomesMessage(conditionAndOutcomes));
        }
    }


    private String combineOutcomesMessage(ConditionEvaluationReport.ConditionAndOutcomes conditionAndOutcomes) {
        StringBuilder outcomesMessageBuilder = new StringBuilder();
        boolean matched = conditionAndOutcomes.isFullMatch();
        if (matched) {
            outcomesMessageBuilder.append("Matching conditions:");
        } else {
            outcomesMessageBuilder.append("No matching conditions:");
        }

        conditionAndOutcomes.forEach(conditionAndOutcome -> {
            outcomesMessageBuilder.append(conditionAndOutcome.getOutcome().getMessage()).append(",");
        });
        return outcomesMessageBuilder.toString();
    }

    private Set<String> getBasePackages(ConfigurableApplicationContext context) {
        ConfigurableEnvironment environment = context.getEnvironment();
        Set<String> basePackages = environment.getProperty(BASE_PACKAGES_PROPERTY_NAME, Set.class, DEFAULT_BASE_PACKAGES);
        return basePackages;
    }

    private void appendLine(StringBuilder stringBuilder, String text, Object... args) {
        stringBuilder.append(FormatUtils.format(text, args)).append(System.lineSeparator());
    }
}
