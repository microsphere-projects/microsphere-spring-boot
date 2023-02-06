package io.github.microsphere.spring.boot.security.web.servlet;

import org.springframework.core.annotation.AnnotationAwareOrderComparator;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Composite {@link WebSecurityProcessor}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
class WebSecurityProcessors implements WebSecurityProcessor {

    private final List<WebSecurityProcessor> processors;

    public WebSecurityProcessors(Collection<WebSecurityProcessor> processors) {
        this.processors = new ArrayList<>(processors);
        AnnotationAwareOrderComparator.sort(this.processors);
    }

    @Override
    public void process(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        for (WebSecurityProcessor processor : processors) {
            processor.process(request, response);
        }
    }
}
