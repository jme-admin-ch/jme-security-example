package ch.admin.bit.jeap.jme.security.oauth.resource;

import ch.admin.bit.jeap.security.resource.semanticAuthentication.SemanticApplicationRole;
import ch.admin.bit.jeap.security.resource.semanticAuthentication.ServletSemanticAuthorization;
import ch.admin.bit.jeap.security.test.resource.ServletSemanticAuthorizationMock;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

/**
 * This test class shows an example of unit testing a class that relies on a jEAP semantic authorization instance.
 * Such cases occur when programmatic authorization tests or access to the user/system data of the authentication are needed.
 * To check the functionality of such classes the jEAP semantic authorization instance has to be mocked. This can be done
 * using e.g. Mockito or using the convenient ServletSemanticAuthorizationMock class supplied by the
 * jeap-spring-boot-security-test-starter.
 */
class PartnerResourceTest {

	@Test
    void testGetPartnerNameByExternalId_whenPartnerReadRole_thenSuccess() {
        SemanticApplicationRole partnerReadRole = SemanticApplicationRole.builder()
                .system("jme")
                .resource("partner")
                .operation("read")
                .build();
        ServletSemanticAuthorization jeapSemanticAuthorization = ServletSemanticAuthorizationMock.builder()
                .systemName("jme")
                .userRole(partnerReadRole)
                .build();
        PartnerResource partnerResource = new PartnerResource(jeapSemanticAuthorization);

        String name = partnerResource.getPartnerNameByExternalRef("eins");

        Assertions.assertEquals("Partner 1", name);
    }

    @Test
    void testGetPartnerNameByExternalId_whenMissingRole_thenForbidden() {
        ServletSemanticAuthorization jeapSemanticAuthorization = ServletSemanticAuthorizationMock.builder().systemName("jme").build();
        PartnerResource partnerResource = new PartnerResource(jeapSemanticAuthorization);

        Assertions.assertThrows(AccessDeniedException.class, () -> partnerResource.getPartnerNameByExternalRef("eins"));
    }

	// Additional tests....

}
