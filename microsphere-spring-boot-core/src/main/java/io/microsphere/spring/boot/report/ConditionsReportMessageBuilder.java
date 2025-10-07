package io.microsphere.spring.boot.report;

import io.microsphere.annotation.ConfigurationProperty;
import org.springframework.boot.autoconfigure.condition.ConditionEvaluationReport;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static io.microsphere.annotation.ConfigurationProperty.APPLICATION_SOURCE;
import static io.microsphere.collection.ListUtils.newArrayList;
import static io.microsphere.spring.boot.constants.PropertyConstants.MICROSPHERE_SPRING_BOOT_PROPERTY_NAME_PREFIX;
import static io.microsphere.spring.boot.report.ConditionEvaluationReportBuilder.getReportsMap;
import static io.microsphere.text.FormatUtils.format;
import static java.lang.System.lineSeparator;
import static java.util.Collections.singleton;

/**
 * Spring Boot Conditions Report builder
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public class ConditionsReportMessageBuilder {

    private static final String DEFAULT_BASE_PACKAGE = "io.microsphere";

    /**
     * The base packages for Spring Boot Conditions Report : "microsphere.spring.boot.conditions.report.base-packages"
     */
    @ConfigurationProperty(
            type = String[].class,
            defaultValue = DEFAULT_BASE_PACKAGE,
            source = APPLICATION_SOURCE
    )
    public static final String BASE_PACKAGES_PROPERTY_NAME = MICROSPHERE_SPRING_BOOT_PROPERTY_NAME_PREFIX + "conditions.report.base-packages";

    private static final Set<String> DEFAULT_BASE_PACKAGES = singleton(DEFAULT_BASE_PACKAGE);

    private final ConfigurableApplicationContext context;

    public ConditionsReportMessageBuilder(ConfigurableApplicationContext context) {
        this.context = context;
    }

    List<String> build() {
        Map<String, ConditionEvaluationReport> reportsMap = getReportsMap();
        List<String> reportMessages = newArrayList(reportsMap.size());
        reportsMap.forEach((id, report) -> reportMessages.add(buildSingle(id, report)));
        return reportMessages;
    }

    String buildSingle(String id, ConditionEvaluationReport report) {
        StringBuilder reportMessage = new StringBuilder(lineSeparator());
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
        stringBuilder.append(format(text, args)).append(lineSeparator());
    }
}