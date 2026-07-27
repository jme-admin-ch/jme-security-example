package ch.admin.bit.jeap.jme.security.oauth.clientresource;

import ch.admin.bit.jeap.security.resource.semanticAuthentication.ServletSemanticAuthorization;
import ch.admin.bit.jeap.security.resource.token.JeapAuthenticationToken;
import ch.admin.bit.jeap.security.restclient.JeapOAuth2RestClientBuilderFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;

/**
 * This class gives examples for usages of different kinds of WebServiceClient instances the JeapOAuth2RestClientBuilderFactory
 * can create. It shows the following authentication patterns:
 * <p><ul>
 * <li> the web client authenticates outgoing requests with its own token
 * <li> the web client authenticates outgoing requests with the tokens from the incoming requests
 * </ul><p>
 * To show those patterns this class implements an OAuth2 protected REST resource that acts as an OAuth2 client by forwarding
 * the incoming calls to another OAuth2 protected REST resource.
 */
@Slf4j
@RestController
@RequestMapping("/api/partners")
public class OAuth2ProtectedRestResource {

    private final ServletSemanticAuthorization jeapSemanticAuthorization;

    private final RestClient restClientTokenFromAuthServer;
    private final RestClient restClientTokenFromIncomingRequest;
    private final RestClient restClientTokenFromIncomingRequestElseAuthServer;

    public OAuth2ProtectedRestResource(JeapOAuth2RestClientBuilderFactory jeapOAuth2RestClientBuilderFactory,
                                       ClientResourceServiceProperties clientProperties,
                                                    ServletSemanticAuthorization jeapSemanticAuthorization) {
        this.jeapSemanticAuthorization = jeapSemanticAuthorization;

        // Create a RestClient that authenticates its requests with an access token fetched via client credentials flow from the authorization server for the given client id.
        this.restClientTokenFromAuthServer = jeapOAuth2RestClientBuilderFactory.createForClientRegistryId(clientProperties.getClientRegistrationId()).
                baseUrl(clientProperties.getResourceUrl()).
                build();

        // Create a RestClient that authenticates its requests with the access token from the incoming request the RestClient is used in.
        // No token is added to the requests if no token is associated with the incoming request the RestClient is used in.
        this.restClientTokenFromIncomingRequest = jeapOAuth2RestClientBuilderFactory.createForTokenFromIncomingRequest().
                baseUrl(clientProperties.getResourceUrl()).
                build();

        // Create a RestClient that authenticates its requests with the access token from the incoming request the RestClient is used in or if no token is
        // available in the incoming request with an access token fetched via client credentials flow from the authorization server for the given client id.
        this.restClientTokenFromIncomingRequestElseAuthServer = jeapOAuth2RestClientBuilderFactory.createForClientRegistryIdPreferringTokenFromIncomingRequest(clientProperties.getClientRegistrationId()).
                baseUrl(clientProperties.getResourceUrl()).
                build();
    }

    @GetMapping
    @PreAuthorize("hasRole('partner','read')")
    public String listPartners() {
        // This web endpoint is protected as an OAuth2 resource, i.e. when executed the incoming request will contain a valid access token.
        // Said token will be ignored by this RestClient, instead this RestClient will authenticate its requests with a token fetched via
        // client credentials flow from the authorization server for the OAuth2 client configured at the creation of the RestClient instance.
        logAuthorizationInfo();
        return restClientTokenFromAuthServer.
                get().
                uri("/api/partners").
                retrieve().
                body(String.class);
    }

    @GetMapping("/{partnerId:[0-9]+}")
    @PreAuthorize("hasRoleForPartner('partner','read', #partnerId)")
    public String getPartner(@PathVariable("partnerId") String partnerId) {
        // This web endpoint is protected as an OAuth2 resource, i.e. when executed the incoming request will contain a valid access token.
        // The same access token will be used by this RestClient to authenticate its requests.
        logAuthorizationInfo();
        return restClientTokenFromIncomingRequest.
                get().
                uri("/api/partners/{partnerId}", partnerId).
                retrieve().
                body(String.class);
    }

    @GetMapping("/{externalRef:[a-z]+}")
    @PreAuthorize("hasRole('partner','read')")
    public String getPartnerByExternalRef(@PathVariable("externalRef") String externalRef) {
        // This web endpoint is protected as an OAuth2 resource, i.e. when executed the incoming request will contain a valid access token.
        // The same access token will be used by this RestClient to authenticate its requests. Thus there is no need for the RestClient to
        // fetch a token as OAuth2 client from the authorization server configured at the creation of the RestClient instance. See OtherwiseProtectedRestResource
        // for an example of an analogous RestClient that will have to fetch a token from the configured authorization server.
        logAuthorizationInfo();
        return restClientTokenFromIncomingRequestElseAuthServer.
                get().
                uri("/api/partners/{externalRef}", externalRef).
                retrieve().
                body(String.class);
    }

    private void logAuthorizationInfo() {
        // Access the Spring Security Authentication as JeapAuthenticationToken
        JeapAuthenticationToken jeapAuthenticationToken = jeapSemanticAuthorization.getAuthenticationToken();
        log.info(jeapAuthenticationToken.toString());
    }
}

