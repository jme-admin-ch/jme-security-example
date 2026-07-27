package ch.admin.bit.jeap.jme.security.oauth.clientresource;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties("jme.security.client")
public class ClientResourceServiceProperties {

    /**
     * Base URL of the OAuth2 resource service
     */
    private String resourceUrl;

    /**
     * OAuth2 client registration ID from Spring's client registry to use for accessing OAuth2 resources
     */
    private String clientRegistrationId;

}
