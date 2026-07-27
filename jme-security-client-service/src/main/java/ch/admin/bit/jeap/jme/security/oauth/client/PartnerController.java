package ch.admin.bit.jeap.jme.security.oauth.client;

import ch.admin.bit.jeap.security.restclient.JeapOAuth2RestClientBuilderFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;

/**
 * This endpoint makes the OAuth2 protected endpoints of the jme-security-client-resource's 'partner' resource publicly
 * available by forwarding corresponding requests to the resource's endpoints acting as an OAuth2 client, i.e. equipping
 * the requests with an appropriate authorization token.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/partners")
public class PartnerController {

    private final JeapOAuth2RestClientBuilderFactory jeapOAuth2RestClientBuilderFactory;
    private final ClientServiceProperties clientProperties;

    @GetMapping(produces = "text/plain")
    public String listPartners(@RequestParam(name = "target", required = false) String targetName) {
        // This RestClient exchange will include a bearer OAuth2 access token (see creation of the client).
        String response = createRestClient(targetName).
                get().
                uri("/api/partners").
                retrieve().
                body(String.class);

        return String.format("Partner list: %s", response);
    }

    @GetMapping(value = "/{partner}", produces = "text/plain")
    public String getPartner(@PathVariable("partner") String partner,
                             @RequestParam(name = "target", required = false) String targetName) {
        // This RestClient exchange will include a bearer OAuth2 access token (see creation of the client)
        String response = createRestClient(targetName).
                get().
                uri("/api/partners/{partner}", partner).
                retrieve().
                body(String.class);
        return String.format("Partner '%s' data: %s", partner, response);
    }

    @GetMapping(value = "/{partner}/name", produces = "text/plain")
    public String getPartnerName(@PathVariable("partner") String partner,
                                 @RequestParam(name = "target", required = false) String targetName) {
        // This RestClient exchange will include a bearer OAuth2 access token (see creation of the client)
        String response = createRestClient(targetName).
                get().
                uri("/api/partners/{partner}/name", partner).
                retrieve().
                body(String.class);
        return String.format("Partner '%s' name: %s", partner, response);
    }

    private RestClient createRestClient(String targetName) {
        // By creating the RestClient builder with the JeapOAuth2RestClientBuilderFactory all RestClient instances built with
        // this builder will add an OAuth2 access token to the exchanges based on the provided Spring client registration as
        // defined in the application*.yml files.
        String clientRegistrationId = clientProperties.getClientRegistrationId();
        log.debug("Creating a RestClient for the Spring client registration id '{}'.", clientRegistrationId);
        if (targetName != null && targetName.equals("clientresource")) {
            return jeapOAuth2RestClientBuilderFactory.createForClientRegistryId(clientRegistrationId).
                    baseUrl(clientProperties.getClientResourceUrl()).
                    build();
        }
        return jeapOAuth2RestClientBuilderFactory.createForClientRegistryId(clientRegistrationId).
                baseUrl(clientProperties.getResourceUrl()).
                build();
    }
}
