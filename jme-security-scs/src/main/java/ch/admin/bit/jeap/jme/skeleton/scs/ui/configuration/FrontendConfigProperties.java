package ch.admin.bit.jeap.jme.skeleton.scs.ui.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.net.URI;
import java.util.List;

/**
 * Configuration properties that will be forwarded to the UI
 */
@Configuration
@ConfigurationProperties(prefix = "frontend")
@Data
public class FrontendConfigProperties {
    /**
     * Authentication server to be used.
     */
    private URI authority;

    /**
     * URL of the application for the redirect URI after a login.
     */
    private URI applicationUrl;

    /**
     * URL to go to after a logout.
     */
    private URI logoutRedirectUri;

    /**
     * List of backends where to a token shall be send.
     */
    private List<String> tokenAwarePatterns;

    /**
     * URL to go to after a login.
     */
    private String redirectUrl;

    /**
     * Identifier for the client
     */
    private String clientId;
    /**
     * Whether to log in automatically
     */
    private boolean useAutoLogin;
}
