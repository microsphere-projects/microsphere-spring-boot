package io.microsphere.spring.boot.logging.autoconfigure;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.event.EventListener;
import org.springframework.web.context.support.ServletRequestHandledEvent;

/**
 * Web MVC Auto-Configuration
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class WebMvcLoggingAutoConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(WebMvcLoggingAutoConfiguration.class);

    @EventListener(ServletRequestHandledEvent.class)
    public void onServletRequestHandledEvent(ServletRequestHandledEvent event) {
        logger.debug("{}", event);
    }

}
