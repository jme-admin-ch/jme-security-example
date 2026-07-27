package ch.admin.bit.jeap.jme.security.oauth.resource;

import ch.admin.bit.jeap.security.resource.semanticAuthentication.SemanticApplicationRole;
import ch.admin.bit.jeap.security.resource.semanticAuthentication.ServletSemanticAuthorization;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Set;

/**
 * A simple controller providing an OAuth2 protected endpoint that extracts and returns the business partner roles
 * that a user has been granted based on the access token provided as bearer token in the request. This endpoint is
 * used to show and test the effect of the dynamic scope "bproles:*" that can be specified when authenticating with
 * the auth server.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/bproles")
public class BprolesController {

    private final ServletSemanticAuthorization jeapSemanticAuthorization;

    @GetMapping
    public Map<String, Set<SemanticApplicationRole>> listBproles() {
        return jeapSemanticAuthorization.getBusinessPartnerRoles();
    }

}
