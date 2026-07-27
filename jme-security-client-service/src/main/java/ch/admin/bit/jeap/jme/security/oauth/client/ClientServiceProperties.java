package ch.admin.bit.jeap.jme.security.oauth.client;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties("jme.security.client")
public class ClientServiceProperties {

    /**
     * Base URL of the OAuth2 resource service
     */
    private String resourceUrl;

    /**
     * Base URL of the client-resource service (a service that acts as both client and resource)
     */
    private String clientResourceUrl;

    /**
     * Base URL of the SCS example
     */
    private String scsUrl;

    /**
     * OAuth2 client registration ID from Spring's client registry to use for accessing OAuth2 resources
     */
    private String clientRegistrationId;
}
