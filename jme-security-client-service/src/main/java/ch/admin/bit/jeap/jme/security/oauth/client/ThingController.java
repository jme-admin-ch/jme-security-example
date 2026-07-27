package ch.admin.bit.jeap.jme.security.oauth.client;

import ch.admin.bit.jeap.security.restclient.JeapOAuth2RestClientBuilderFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;


/**
 * This endpoint makes the OAuth2 protected endpoints of the jme-security-client-resource's 'thing' resource publicly
 * available by forwarding corresponding requests to the resource's endpoints acting as an OAuth2 client, i.e. equipping
 * the requests with an appropriate authorization token.
 */
@Slf4j
@RestController
@RequestMapping
@RequiredArgsConstructor
public class ThingController {

    private final JeapOAuth2RestClientBuilderFactory jeapOAuth2RestClientBuilderFactory;
    private final ClientServiceProperties clientProperties;

    @GetMapping(value = "/api/things", produces = "text/plain")
    public String listThings() {
        // This RestClient exchange will include a bearer OAuth2 access token (see creation of the client).
        String response = createRestClient().
                get().
                uri("/api/things").
                retrieve().
                body(String.class);
        return String.format("Got things: %s", response);
    }

    @GetMapping(value = "/api/partners/{partnerId}/things", produces = "text/plain")
    public String listThingsForPartner(@PathVariable("partnerId") String partnerId) {
        // This RestClient exchange will include a bearer OAuth2 access token (see creation of the client).
        String response = createRestClient().
                get().
                uri("/api/partners/{partnerId}/things", partnerId).
                retrieve().
                body(String.class);
        return String.format("Got things for partner with id %s: %s", partnerId, response);
    }

    @GetMapping(value = "/api/things/{id}", produces = "text/plain")
    public String getThingById(@PathVariable("id") String id) {
        // This RestClient exchange will include a bearer OAuth2 access token (see creation of the client)
        String response = createRestClient().
                get().
                uri("/api/things/{id}", id).
                retrieve().
                body(String.class);
        return String.format("Got thing with id '%s': %s", id, response);
    }

    @GetMapping(value = "/api/operation-things", produces = "text/plain")
    public String listThingsByOperation() {
        // This RestClient exchange will include a bearer OAuth2 access token (see creation of the client)
        String response = createRestClient().
                get().
                uri("/api/operation-things").
                retrieve().
                body(String.class);
        return String.format("Got things (operation-only auth): %s", response);
    }

    @GetMapping(value = "/api/operation-things/partners/{partnerId}", produces = "text/plain")
    public String listThingsByOperationForPartner(@PathVariable("partnerId") String partnerId) {
        // This RestClient exchange will include a bearer OAuth2 access token (see creation of the client)
        String response = createRestClient().
                get().
                uri("/api/operation-things/partners/{partnerId}", partnerId).
                retrieve().
                body(String.class);
        return String.format("Got things for partner %s (operation-only auth): %s", partnerId, response);
    }

    @GetMapping(value = "/api/operation-things/{id}", produces = "text/plain")
    public String getThingByOperationAndId(@PathVariable("id") String id) {
        // This RestClient exchange will include a bearer OAuth2 access token (see creation of the client)
        String response = createRestClient().
                get().
                uri("/api/operation-things/{id}", id).
                retrieve().
                body(String.class);
        return String.format("Got thing with id '%s' (operation-only auth): %s", id, response);
    }

    private RestClient createRestClient() {
        // By creating the RestClient builder with the JeapOAuth2RestClientBuilderFactory all RestClient instances built with
        // this builder will add an OAuth2 access token to the exchanges based on the provided Spring client registration as
        // defined in the application*.yml files.
        String clientRegistrationId = clientProperties.getClientRegistrationId();
        log.debug("Creating a RestClient for the Spring client registration id '{}'.", clientRegistrationId);
        return jeapOAuth2RestClientBuilderFactory.createForClientRegistryId(clientRegistrationId).
                baseUrl(clientProperties.getResourceUrl()).
                build();
    }

}
