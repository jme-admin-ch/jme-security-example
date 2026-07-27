package ch.admin.bit.jeap.jme.security.oauth.client;

import ch.admin.bit.jeap.security.restclient.JeapOAuth2RestClientBuilderFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;

/**
 * This endpoint makes the basic auth protected endpoint of the jme-security-resource's 'bproles' resource publicly
 * available by forwarding corresponding requests to the resource's endpoints acting as an OAuth2 client, i.e. equipping
 * the requests with an appropriate authorization token.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/bproles")
public class BprolesController {

    private final JeapOAuth2RestClientBuilderFactory jeapOAuth2RestClientBuilderFactory;
    private final ClientServiceProperties clientServiceProperties;

    @GetMapping
    public String listBproles(@RequestParam(name = "scoped", required = false, defaultValue = "false") boolean scoped) {
        return createRestClient(scoped).
                get().
                uri("/api/bproles").
                retrieve().
                body(String.class);
    }

    private RestClient createRestClient(boolean scoped) {
        String registrationId = scoped ? clientServiceProperties.getClientRegistrationId() + "-bpscoped" :
                clientServiceProperties.getClientRegistrationId();
        // By creating the RestClient builder with the JeapOAuth2RestClientBuilderFactory all RestClient instances built with
        // this builder will add an OAuth2 access token to the exchanges based on the provided Spring client registration as
        // defined in the application*.yml files.
        return jeapOAuth2RestClientBuilderFactory.createForClientRegistryId(registrationId).
                baseUrl(clientServiceProperties.getResourceUrl()).
                build();
    }
}
