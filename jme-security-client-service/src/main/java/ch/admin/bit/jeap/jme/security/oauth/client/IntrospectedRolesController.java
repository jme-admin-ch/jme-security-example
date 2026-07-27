package ch.admin.bit.jeap.jme.security.oauth.client;

import ch.admin.bit.jeap.security.restclient.JeapOAuth2RestClientBuilderFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;


@RestController
@RequiredArgsConstructor
public class IntrospectedRolesController {

    private final JeapOAuth2RestClientBuilderFactory jeapOAuth2RestClientBuilderFactory;
    private final ClientServiceProperties clientProperties;

    @GetMapping("/api/introspected-roles")
    public String listIntrospectedRoles(@RequestParam(name = "pruned", required = false, defaultValue = "false") boolean prunedRoles) {
        // This RestClient exchange will include a bearer OAuth2 access token (see creation of the client).
        return createRestClient(prunedRoles).
                get().
                uri("/api/introspected-roles").
                retrieve().
                body(String.class);
    }

    private RestClient createRestClient(boolean prunedRoles) {
        // By creating the RestClient builder with the JeapOAuth2RestClientBuilderFactory all RestClient instances built with
        // this builder will add an OAuth2 access token to the exchanges based on the provided Spring client registration as
        // defined in the application*.yml files.
        String clientRegistrationId = clientProperties.getClientRegistrationId();
        if (prunedRoles) {
            clientRegistrationId = clientRegistrationId + "-roles-pruned";
        }
        return jeapOAuth2RestClientBuilderFactory.createForClientRegistryId(clientRegistrationId).
                baseUrl(clientProperties.getResourceUrl()).
                build();
    }

}
