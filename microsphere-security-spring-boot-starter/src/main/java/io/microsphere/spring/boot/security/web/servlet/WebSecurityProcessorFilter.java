package io.microsphere.spring.boot.security.web.servlet;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;

/**
 * {@link WebSecurityProcessor} {@link Filter}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public class WebSecurityProcessorFilter implements Filter {

    private final WebSecurityProcessors processors;

    public WebSecurityProcessorFilter(Collection<WebSecurityProcessor> processors) {
        this.processors = new WebSecurityProcessors(processors);
    }

    @Override
    public void init(FilterConfig filterConfig) {
    }

    @Override
    public final void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        // Processes the response
        processors.process((HttpServletRequest) request, (HttpServletResponse) response);
        chain.doFilter(request, response);
    }
}
