package ch.admin.bit.jeap.jme.security.oauth.resource;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * The endpoints of this resource are not protected (see {@link WebSecurityConfig}).
 */
@RestController
@RequestMapping("/api/noauth")
public class NoAuthResource {

    @GetMapping
    public String getInfo() {
        return "Some unprotected info";
    }

    @GetMapping(path = "/internal-error")
    public ResponseEntity<Void> getInternalError() {
        return ResponseEntity.internalServerError().build();
    }

    @GetMapping(path = "/bad-request")
    public ResponseEntity<Void> getBadRequest() {
        return ResponseEntity.badRequest().build();
    }

    @GetMapping(path = "/forbidden")
    public ResponseEntity<Void> getForbidden() {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    @GetMapping(path = "/unauthorized")
    public ResponseEntity<Void> getUnauthorized() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

}
