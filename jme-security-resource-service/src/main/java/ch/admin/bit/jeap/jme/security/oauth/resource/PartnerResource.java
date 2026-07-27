package ch.admin.bit.jeap.jme.security.oauth.resource;

import ch.admin.bit.jeap.security.resource.semanticAuthentication.ServletSemanticAuthorization;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * This class gives an example for an OAuth2 protected resource 'partner' that requires certain roles for access.
 * The partner resource manages business partner data. To access a partner's data a user is required to have
 * the function 'read' on the resource 'partner' for the system 'jme' for the specific business partner
 * who's data is to be accessed.
 */
@RestController
@RequestMapping("/api/partners")
@Slf4j
@RequiredArgsConstructor
public class PartnerResource {

    private final ServletSemanticAuthorization jeapSemanticAuthorization;

    private Map<String, Partner> partnersById = new HashMap<>(Map.of(
            "11111", new Partner("11111", "eins", "Partner 1"),
            "22222", new Partner("22222", "zwei", "Partner 2"),
            "88888", new Partner("88888", "acht", "Partner 8"),
            "99999", new Partner("99999", "neun", "Partner 9")));

    /**
     * This web endpoint can only be accessed if the request includes a valid OAuth2 access token and the token contains
     * a role that grants the caller 'read' access on the 'partner' resource for at least one partner.
     */
    @GetMapping
    @PreAuthorize("hasRole('partner','read')")
    public Collection<Partner> listPartners() {
        // Does the token grant 'read' access on all partners?
        if (jeapSemanticAuthorization.hasRoleForAllPartners("partner", "read")) {
            // Fetch all partners.
            return listAll();
        }
        else {
            // Determine the partner ids the token grants read access on...
            Collection<String> partners = jeapSemanticAuthorization.getPartnersForRole("partner", "read");
            // ...then only provide those partners.
            return listForPartnerIds(partners);
        }
    }

    /**
     * This web endpoint can only be accessed if the request includes a valid OAuth2 access token and the token contains
     * a role that grants the caller 'read' access on the partner with the requested partner id.
     */
    @GetMapping("/{partnerId:[0-9]+}")
    @PreAuthorize("hasRoleForPartner('partner','read', #partnerId)")
    public Partner getPartnerById(@PathVariable("partnerId") String partnerId) {
        return findPartnerById(partnerId).orElseThrow(supplyPartnerByIdNotFoundStatusException(partnerId));
    }

    /**
     * This web endpoint can only be accessed if the request includes a valid OAuth2 access token and the token contains
     * a role that grants the caller 'read' access on the 'partner' resource for at least one partner. We can't do a detailed
     * authorization check on the partner associated with the given external reference when entering the method because
     * we do not yet know the referenced partner's partner id. However, we can do this check when leaving the method,
     * because the return object contains the partner id. This web endpoint will not return the partner if the token does
     * not contain a role that grants the caller 'read' access on the partner identified by the external reference.
     * If the return object would not contain the partner id, the detailed authorization check would have to be done
     * programmatically using the appropriate ServletSemanticAuthorization bean method.
     * See {@link #getPartnerNameByExternalRef(String)} for such an example.
     */
    @GetMapping("/{externalRef:[a-z]+}")
    @PreAuthorize("hasRole('partner','read')")
    @PostAuthorize("hasRoleForPartner('partner','read', returnObject.getId())")
    public Partner getPartnerByExternalRef(@PathVariable("externalRef") String externalRef) {
        return findPartnerByExternalRef(externalRef).orElseThrow(supplyPartnerByExternalRefNotFoundStatusException(externalRef));
    }

    /**
     * This web endpoint can only be accessed if the request includes a valid OAuth2 access token and the token contains
     * a role that grants the caller 'read' access on the 'partner' resource for at least one partner. We can't do a detailed
     * authorization check on the partner associated with the given external reference when entering the method because
     * we do not yet know the referenced partner's partner id. The return object does not contain the partner id either.
     * Therefore, the detailed authorization check has to be done programmatically using the appropriate
     * ServletSemanticAuthorization bean method..
     */
    @GetMapping("/{externalRef:[a-z]+}/name")
    @PreAuthorize("hasRole('partner','read')")
    public String getPartnerNameByExternalRef(@PathVariable("externalRef") String externalRef) {
        Partner partner = findPartnerByExternalRef(externalRef).orElseThrow(supplyPartnerByExternalRefNotFoundStatusException(externalRef));
        if (jeapSemanticAuthorization.hasRoleForPartner("partner", "read", partner.getId())) {
            return partner.getName();
        }
        else {
            throw new AccessDeniedException("Access to partner with external ref '" + externalRef + "' denied.");
        }
    }

    /**
     *  This web endpoint can only be accessed if the request includes a valid OAuth2 access token and the token contains
     *  a role that grants the caller 'write' access on the partner to create or update.
     */
    @PutMapping
    @PreAuthorize("hasRoleForPartner('partner','write', #partner.id)")
    public void createOrUpdatePartner(@RequestBody Partner partner) {
        partnersById.put(partner.getId(), partner);
    }

    private Supplier<ResponseStatusException> supplyPartnerByIdNotFoundStatusException(final String partnerId) {
        return () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Partner with id '" + partnerId + "' not found");
    }

    private Supplier<ResponseStatusException> supplyPartnerByExternalRefNotFoundStatusException(final String externalRef) {
        return () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Partner with external ref '" + externalRef + "' not found");
    }

    private Collection<Partner> listAll() {
        return partnersById.values();
    }

    private Collection<Partner> listForPartnerIds(Collection<String> partnerIds) {
        return partnerIds.stream().map(partnersById::get).filter(Objects::nonNull).collect(Collectors.toSet());
    }
    private Optional<Partner> findPartnerById(String id) {
        return Optional.ofNullable(partnersById.get(id));
    }

    private Optional<Partner> findPartnerByExternalRef(String externalRef) {
        return listAll().stream().filter(partner -> partner.getExternalRef().equals(externalRef)).findFirst();
    }

}

