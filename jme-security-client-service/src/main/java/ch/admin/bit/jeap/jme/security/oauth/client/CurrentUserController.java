package ch.admin.bit.jeap.jme.security.oauth.client;

import ch.admin.bit.jeap.security.restclient.JeapOAuth2RestClientBuilderFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;

/**
 * This particular endpoint is designed to emulate access to the current-user endpoint with the use of a token, subsequently returning a JSON response.
 * It should be noted that this endpoint is exclusively utilized for testing purposes. Typically, the current-user endpoint is exclusively used by the frontend.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/current-user")
public class CurrentUserController {

    private final JeapOAuth2RestClientBuilderFactory jeapOAuth2RestClientBuilderFactory;
    private final ClientServiceProperties clientProperties;

    @GetMapping(produces = "application/json")
    public String currentUser(@RequestParam(name = "alternateRoles", required = false, defaultValue = "false") boolean alternateRoles) {
        return createRestClient(alternateRoles).
                get().
                uri("/api/current-user").
                retrieve().
                body(String.class);
    }

    private RestClient createRestClient(boolean alternateRoles) {
        String clientRegistrationId = clientProperties.getClientRegistrationId();
        if (alternateRoles) {
            clientRegistrationId = clientRegistrationId + "-alternate-roles";
        }
        log.debug("Creating a RestClient for the Spring client registration id '{}'.", clientRegistrationId);
        return jeapOAuth2RestClientBuilderFactory.createForClientRegistryId(clientRegistrationId).
                baseUrl(clientProperties.getScsUrl()).
                build();
    }
}
