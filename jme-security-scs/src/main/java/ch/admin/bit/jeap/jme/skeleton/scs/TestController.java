package ch.admin.bit.jeap.jme.skeleton.scs;

import ch.admin.bit.jeap.security.resource.semanticAuthentication.SemanticApplicationRole;
import ch.admin.bit.jeap.security.resource.semanticAuthentication.ServletSemanticAuthorization;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/v1/")
@Slf4j
@RequiredArgsConstructor
public class TestController {

    private final ServletSemanticAuthorization jeapSemanticAuthorization;

    @GetMapping(path = "roles")
    @PreAuthorize("hasRole('partner','read')")
    public String listRoles() {
        List<String> roles = jeapSemanticAuthorization.getBusinessPartnerRoles().values()
                .stream().flatMap(Set::stream).map(SemanticApplicationRole::toString).toList();
        log.info("listRoles() called, roles: {}", roles);
        return "{\"roles\":" + JSONArray.toJSONString(roles) + "}";
    }

    @GetMapping(path = "claims")
    public String listClaims() {
        return JSONObject.toJSONString(jeapSemanticAuthorization.getAuthenticationToken().getToken().getClaims());
    }

}
