package ch.admin.bit.jeap.jme.security.oauth.resource;

import ch.admin.bit.jeap.security.resource.semanticAuthentication.ServletSemanticAuthorization;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static java.util.Collections.singleton;

/**
 * This class gives an example for an OAuth2 protected resource 'thing' that requires certain roles for access.
 * The thing resource manages data of things that belong to business partners. To access a thing's data
 * a user is required to have the function 'read' on the resource 'thing' for the system 'jme' for the specific
 * business partner to which the thing to be accessed belongs.
 */
@RestController
@Slf4j
@RequiredArgsConstructor
public class ThingResource {

    private final ServletSemanticAuthorization jeapSemanticAuthorization;

    private Set<Thing> things = Set.of(
            new Thing("1", "11111", "Thing1"),
            new Thing("2", "11111", "Thing2"),
            new Thing("3", "22222", "Thing3"),
            new Thing("8", "88888", "Thing8"),
            new Thing("9", "99999", "Thing9"));

    @GetMapping("/api/things")
    @PreAuthorize("hasRole('thing', 'read')")
    public Collection<Thing> listThings() {
        // Does the token grant 'read' access on the things of all partners?
        if (jeapSemanticAuthorization.hasRoleForAllPartners("thing", "read")) {
            // Fetch all things.
            return listAll();
        } else {
            // Determine the partners the token grants read access on things...
            Collection<String> partners = jeapSemanticAuthorization.getPartnersForRole("thing", "read");
            // ...then only provide the things belonging to those partners.
            return listForPartners(partners);
        }
    }

    @GetMapping("/api/partners/{partnerId}/things")
    @PreAuthorize("hasRoleForPartner('thing', 'read', #partnerId)")
    public Collection<Thing> listThingsForBusinessPartner(@PathVariable("partnerId") String partnerId) {
        return listForPartners(singleton(partnerId));
    }

    /**
     * We can't do a detailed authorization check on the thing entering the method because we do not yet know the
     * partner to which the thing belongs. However, we can do this check when leaving the method, because the return
     * object contains the partner id. This web endpoint will not return the thing if the token does not contain a role
     * that grants the caller 'read' access on the partner to which the thing belongs. If the return object would not
     * contain the partner id, the detailed authorization check would have to be done programmatically using the
     * appropriate ServletSemanticAuthorization bean method. See {@link #getThingById2(String)} for such an example.
     */
    @GetMapping("/api/things/{id:[0-4][0-9]*}")
    @PreAuthorize("hasRole('thing', 'read')")
    @PostAuthorize("hasRoleForPartner('thing', 'read', returnObject.getPartnerId())")
    public Thing getThingById1(@PathVariable("id") String id) {
        return findThingById(id).orElseThrow(supplyThingNotFoundStatusException(id));
    }

    /**
     * Same as {@link #getThingById1(String)} but replacing the declarative @PostAuthorize() check with a programmatic check.
     * See {@link #getThingById1(String)} for explanation.
     */
    @GetMapping("/api/things/{id:[5-9][0-9]*}")
    @PreAuthorize("hasRole('thing', 'read')")
    public Thing getThingById2(@PathVariable("id") String id) {
        Thing thing = findThingById(id).orElseThrow(supplyThingNotFoundStatusException(id));
        if (jeapSemanticAuthorization.hasRoleForPartner("thing", "read", thing.getPartnerId())) {
            return thing;
        } else {
            throw new AccessDeniedException("Access to thing with id '" + id + "' denied.");
        }
    }

    // --- Operation-only authorization examples ---
    // NOTE: Authorization only by operation is an edge case and should usually be avoided. Prefer more specific
    // authorization checks that also take the resource into account.
    // The following endpoints demonstrate the operation-only methods on SemanticRoleRepository / ServletSemanticAuthorization.
    // These methods check authorization based solely on the operation part of a semantic role (without specifying a resource),
    // matching any role that grants the given operation regardless of which resource it applies to.
    // Since jeap-spring-boot-security-starter 21.0.0, these methods have distinct names containing "Operation" to clearly
    // differentiate them from the role-based overloads that take both resource and operation.

    /**
     * Lists things accessible via any role granting the 'read' operation, regardless of resource.
     * Uses {@code hasOperationForAllPartners} (annotation) and {@code getPartnersForOperation} (programmatic).
     */
    @GetMapping("/api/operation-things")
    @PreAuthorize("hasOperation('read')")
    public Collection<Thing> listThingsByOperation() {
        if (jeapSemanticAuthorization.hasOperationForAllPartners("read")) {
            return listAll();
        } else {
            Collection<String> partners = jeapSemanticAuthorization.getPartnersForOperation("read");
            return listForPartners(partners);
        }
    }

    /**
     * Gets things for a specific partner, authorized by operation only.
     * Uses {@code hasOperationForPartner} in a @PreAuthorize annotation.
     */
    @GetMapping("/api/operation-things/partners/{partnerId}")
    @PreAuthorize("hasOperationForPartner('read', #partnerId)")
    public Collection<Thing> listThingsByOperationForPartner(@PathVariable("partnerId") String partnerId) {
        return listForPartners(singleton(partnerId));
    }

    /**
     * Gets a single thing, using programmatic operation-only authorization check.
     * Uses {@code hasOperationForPartner} programmatically.
     */
    @GetMapping("/api/operation-things/{id}")
    @PreAuthorize("hasOperation('read')")
    public Thing getThingByOperationAndId(@PathVariable("id") String id) {
        Thing thing = findThingById(id).orElseThrow(supplyThingNotFoundStatusException(id));
        if (jeapSemanticAuthorization.hasOperationForPartner("read", thing.getPartnerId())) {
            return thing;
        } else {
            throw new AccessDeniedException("Access to thing with id '" + id + "' denied.");
        }
    }

    private Supplier<ResponseStatusException> supplyThingNotFoundStatusException(final String thingId) {
        return () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Thing with id '" + thingId + "' not found");
    }

    private Collection<Thing> listAll() {
        return things;
    }

    private Collection<Thing> listForPartners(Collection<String> partners) {
        return things.stream().filter(thing -> partners.contains(thing.getPartnerId())).collect(Collectors.toSet());
    }

    private Optional<Thing> findThingById(String id) {
        return things.stream().filter(thing -> thing.getId().equals(id)).findFirst();
    }
}
