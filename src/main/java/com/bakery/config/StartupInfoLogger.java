package com.bakery.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.UnknownHostException;

@Component
public class StartupInfoLogger {

    private static final Logger logger = LoggerFactory.getLogger(StartupInfoLogger.class);
    private final Environment environment;

    public StartupInfoLogger(Environment environment) {
        this.environment = environment;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void logApplicationStartup() {
        try {
            String protocol = environment.getProperty("server.ssl.key-store") != null ? "https" : "http";
            String serverPort = environment.getProperty("server.port", "8080");
            String contextPath = environment.getProperty("server.servlet.context-path", "");
            String hostAddress = getHostAddress();

            String localUrl = String.format("%s://localhost:%s%s", protocol, serverPort, contextPath);
            String externalUrl = String.format("%s://%s:%s%s", protocol, hostAddress, serverPort, contextPath);
            String h2ConsoleUrl = String.format("%s://localhost:%s%s/h2-console", protocol, serverPort, contextPath);

            logger.info("\n" +
                    "========================================\n" +
                    "🍰 BAKERY SALES MANAGER STARTED SUCCESSFULLY!\n" +
                    "========================================\n" +
                    "🚀 Application: Mimi's Bakery\n" +
                    "📅 Profile: {}\n" +
                    "🌐 Local URL: {}\n" +
                    "🌐 External URL: {}\n" +
                    "🗄️ H2 Console: {}\n" +
                    "========================================",
                    environment.getActiveProfiles().length > 0 ? String.join(",", environment.getActiveProfiles())
                            : "default",
                    localUrl,
                    externalUrl,
                    h2ConsoleUrl);

        } catch (UnknownHostException e) {
            logger.error("Failed to determine host address", e);
        }
    }

    protected String getHostAddress() throws UnknownHostException {
        return InetAddress.getLocalHost().getHostAddress();
    }
}