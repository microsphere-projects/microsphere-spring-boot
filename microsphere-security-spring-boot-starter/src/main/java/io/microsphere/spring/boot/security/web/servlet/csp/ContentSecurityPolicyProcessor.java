package io.microsphere.spring.boot.security.web.servlet.csp;

import io.microsphere.spring.boot.security.web.servlet.WebSecurityProcessor;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * The {@link WebSecurityProcessor} for Content Security Policy
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public class ContentSecurityPolicyProcessor implements WebSecurityProcessor {

    public static final String CONTENT_SECURITY_POLICY_HEADER = "Content-Security-Policy";

    public static final String CONTENT_SECURITY_POLICY_REPORT_ONLY_HEADER = "Content-Security-Policy-Report-Only";

    private String policyDirectives;

    private boolean reportOnly;

    @Override
    public void process(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        response.setHeader((!reportOnly ? CONTENT_SECURITY_POLICY_HEADER : CONTENT_SECURITY_POLICY_REPORT_ONLY_HEADER), policyDirectives);
    }

    public void setPolicyDirectives(String policyDirectives) {
        this.policyDirectives = policyDirectives;
    }

    public void setReportOnly(boolean reportOnly) {
        this.reportOnly = reportOnly;
    }
}
