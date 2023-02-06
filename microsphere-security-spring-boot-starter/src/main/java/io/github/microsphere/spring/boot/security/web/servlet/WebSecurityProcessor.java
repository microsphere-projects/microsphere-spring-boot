package io.github.microsphere.spring.boot.security.web.servlet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Web Security Processor
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public interface WebSecurityProcessor {

    /**
     * Process
     *
     * @param request  {@link HttpServletRequest}
     * @param response {@link HttpServletResponse}
     * @throws IOException
     * @throws ServletException
     */
    void process(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException;
}
