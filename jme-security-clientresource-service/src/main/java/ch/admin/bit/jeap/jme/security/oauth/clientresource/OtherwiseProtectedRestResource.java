package ch.admin.bit.jeap.jme.security.oauth.clientresource;

import ch.admin.bit.jeap.security.restclient.JeapOAuth2RestClientBuilderFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;

/**
 * This class gives a usage example for the WebServiceClient instance kind created by the factory method
 * createForClientRegistryIdPreferringTokenFromIncomingRequest() of the JeapOAuth2RestClientBuilderFactory.
 * The following authentication pattern is shown:
 * <p><ul>
 * <li> the web client authenticates outgoing requests with its own token if the incoming requests are missing tokens
 * </ul><p>
 * To show that pattern this class implements a non-OAuth2-protected REST resource that acts as an OAuth2 client by forwarding
 * the incoming calls to an OAuth2 protected REST resource.
 */
@RestController
@RequestMapping("/api/info")
public class OtherwiseProtectedRestResource {

    private final RestClient restClientTokenFromIncomingRequestElseAuthServer;

    public OtherwiseProtectedRestResource(JeapOAuth2RestClientBuilderFactory jeapOAuth2RestClientBuilderFactory, ClientResourceServiceProperties clientProperties) {
        // Create a RestClient that authenticates its requests with the access token from the incoming request the RestClient is used in or if no token is
        // available in the incoming request with an access token fetched via client credentials flow from the authorization server for the given client id.
        this.restClientTokenFromIncomingRequestElseAuthServer =
                jeapOAuth2RestClientBuilderFactory.createForClientRegistryIdPreferringTokenFromIncomingRequest(clientProperties.getClientRegistrationId()).
                        baseUrl(clientProperties.getResourceUrl()).
                    build();
    }

    // For this example, the info web endpoint does not call the corresponding endpoint of the oauth-resource example. Instead, it calls the
    // partner web endpoint, because we want to call an OAuth2 protected web endpoint from an otherwise protected endpoint.
    @GetMapping
    public String getInfo() {
        // This web endpoint is protected by basic authentication, i.e. when executed the incoming request will *not* contain a valid JWT access token.
        // This RestClient will check for an access token in the current incoming request and because the token will be missing in basic auth, the RestClient will
        // use a token fetched via client credentials flow from the authorization server for the OAuth2 client configured at the creation of the RestClient instance.
        String response = restClientTokenFromIncomingRequestElseAuthServer.
                get().
                uri("/api/partners").
                retrieve().
                body(String.class);
        return String.format("Partners instead of info: %s", response);
    }

}

