package io.github.microsphere.spring.boot.logging.autoconfigure;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.web.context.WebServerApplicationContext;
import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.boot.web.server.WebServer;
import org.springframework.context.event.EventListener;

/**
 * {@link WebServer} Auto-Configuration
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
@ConditionalOnWebApplication
public class WebServerLoggingAutoConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(WebServerLoggingAutoConfiguration.class);

    @EventListener(WebServerInitializedEvent.class)
    public void onWebServerInitializedEvent(WebServerInitializedEvent event) {
        WebServer webServer = event.getWebServer();
        WebServerApplicationContext context = event.getApplicationContext();
        logger.debug("WebServer[class : '{}' , context : '{}'] port : {}", webServer.getClass().getName(),
                context.getId(), webServer.getPort());
    }
}
