package ch.admin.bit.jeap.jme.security.oauth.resource;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * This class gives an example for a REST resource that is excluded from the OAuth2 protection automatically applied to
 * REST resources by the jeap-spring-boot-security-starter. The endpoints of this resource are protected with basic auth
 * (see {@link WebSecurityConfig}).
 */
@RestController
@RequestMapping("/api/info")
public class OtherwiseProtectedResource {

    /**
     * This endpoint has been excluded from protection by OAuth2 (see {@link WebSecurityConfig}).
     */
    @GetMapping
    public String getInfo() {
        return "Some info";
    }

}

