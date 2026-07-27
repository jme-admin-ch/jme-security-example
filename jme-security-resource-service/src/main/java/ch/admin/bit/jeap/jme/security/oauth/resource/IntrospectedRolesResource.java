package ch.admin.bit.jeap.jme.security.oauth.resource;

import ch.admin.bit.jeap.security.resource.properties.AuthorizationServerConfigProperties;
import ch.admin.bit.jeap.security.resource.properties.ResourceServerProperties;
import ch.admin.bit.jeap.security.resource.semanticAuthentication.ServletSemanticAuthorization;
import ch.admin.bit.jeap.security.resource.token.JeapAuthenticationConverter;
import ch.admin.bit.jeap.security.resource.token.JeapAuthenticationToken;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.stream.Collectors;

/**
 * This class provides an endpoint to determine the roles that have been added to the authentication by introspection.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class IntrospectedRolesResource {

    private static final String ROLES_PRUNED_CHARS_CLAIM_NAME = "roles_pruned_chars";

    private final ServletSemanticAuthorization jeapSemanticAuthorization;
    private final ResourceServerProperties resourceServerProperties;

    @GetMapping(value = "/api/introspected-roles", produces = MediaType.APPLICATION_JSON_VALUE)
    public IntrospectedRoles listIntrospectedRoles() {
        // Get the current Spring security authentication as created by jEAP security based on the token
        JeapAuthenticationToken jeapAuthentication = jeapSemanticAuthorization.getAuthenticationToken();

        // Determine if the token has been introspected to create the authentication
        boolean hasBeenIntrospected = hasBeenIntrospected(jeapAuthentication);

        // Get the number of characters used by the roles claims that were pruned from the token
        long rolesPrunedChars = getRolesPrunedChars(jeapAuthentication);

        // Get all roles in the current authentication
        Map<String, Set<String>> bprolesFromAuthentication = jeapAuthentication.getBusinessPartnerRoles();
        Set<String> userrolesFromAuthentication = jeapAuthentication.getUserRoles();

        // Determine the roles that were embedded in the token
        String originalToken = jeapAuthentication.getToken().getTokenValue();
        String issuer = jeapAuthentication.getToken().getIssuer().toString();
        JeapAuthenticationToken jeapAuthenticationFromOriginalToken = createJeapAuthentication(originalToken, issuer);
        Map<String, Set<String>> bprolesFromToken = jeapAuthenticationFromOriginalToken.getBusinessPartnerRoles();
        Set<String> userrolesFromToken = jeapAuthenticationFromOriginalToken.getUserRoles();

        // Determine the roles that have been added to the authentication by introspection,
        // i.e. the roles that are in the authentication but not in the token.
        Set<String> introspectedUserroles = getRoleDifference(userrolesFromAuthentication, userrolesFromToken);
        Map<String, Set<String>> introspectedBproles = getRoleDifference(bprolesFromAuthentication, bprolesFromToken);

        return new IntrospectedRoles(introspectedBproles, introspectedUserroles, rolesPrunedChars, hasBeenIntrospected);
    }

    public record IntrospectedRoles(
            Map<String, Set<String>> bproles, // bproles added by introspection
            Set<String> userroles, // userroles added by introspection
            long rolesPrunedChars,  // number of characters used by the roles claims that were pruned from the token
            boolean hasBeenIntrospected) // true if the token has been introspected
    {}

    private JeapAuthenticationToken createJeapAuthentication(String token, String issuer) {
        String issuerJwkSetUri = getJwkSetUri(issuer);
        NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder.withJwkSetUri(issuerJwkSetUri).build();
        jwtDecoder.setJwtValidator( t -> OAuth2TokenValidatorResult.success()); // no validation needed for this example
        JeapAuthenticationConverter jeapAuthenticationConverter = new JeapAuthenticationConverter();
        return (JeapAuthenticationToken) jeapAuthenticationConverter.convert(jwtDecoder.decode(token));
    }

    private String getJwkSetUri(String issuer) {
        return resourceServerProperties.getAllAuthServerConfigurations().stream()
                .filter(authConfig -> authConfig.getIssuer().equals(issuer))
                .findFirst()
                .map(AuthorizationServerConfigProperties::getJwkSetUri)
                .orElseThrow(() -> new IllegalArgumentException("Issuer " + issuer + " not configured."));
    }

    private boolean hasBeenIntrospected(JeapAuthenticationToken jeapAuthenticationToken) {
        Boolean isActiveClaimPresent = jeapAuthenticationToken.getToken().getClaimAsBoolean("active");
        return isActiveClaimPresent != null;
    }

    private long getRolesPrunedChars(JeapAuthenticationToken jeapAuthenticationToken) {
        Long rolesPrunedChars = jeapAuthenticationToken.getToken().getClaim(ROLES_PRUNED_CHARS_CLAIM_NAME);
        return rolesPrunedChars != null ? rolesPrunedChars : 0;
    }

    private Set<String> getRoleDifference(Set<String> minuend, Set<String> subtrahend) {
        final var minuendNonNull = Optional.ofNullable(minuend).orElseGet(Collections::emptySet);
        final var subtrahendNonNull = Optional.ofNullable(subtrahend).orElseGet(Collections::emptySet);
        return minuendNonNull.stream()
                .filter(role -> !subtrahendNonNull.contains(role))
                .collect(Collectors.toSet());
    }

    private Map<String, Set<String>> getRoleDifference(Map<String, Set<String>> minuend, Map<String, Set<String>> subtrahend) {
        final var minuendNonNull = Optional.ofNullable(minuend).orElseGet(Collections::emptyMap);
        final var subtrahendNonNull = Optional.ofNullable(subtrahend).orElseGet(Collections::emptyMap);
        return minuendNonNull.entrySet().stream()
                .map(entry ->
                        Map.entry(entry.getKey(), getRoleDifference(entry.getValue(), subtrahendNonNull.get(entry.getKey()))))
                .filter(entry -> !entry.getValue().isEmpty())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

}
