package ch.admin.bit.jeap.jme.security.oauth.resource;

import ch.admin.bit.jeap.security.resource.semanticAuthentication.SemanticApplicationRole;
import ch.admin.bit.jeap.security.resource.semanticAuthentication.ServletSemanticAuthorization;
import ch.admin.bit.jeap.security.resource.token.JeapAuthenticationToken;
import ch.admin.bit.jeap.security.test.resource.JeapAuthenticationTestTokenBuilder;
import ch.admin.bit.jeap.security.test.resource.configuration.ServletJeapAuthorizationConfig;
import ch.admin.bit.jeap.security.test.resource.extension.WithAuthentication;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

/**
 * This test class gives an example for testing declarative and programmatic authorization with an integration test that
 * only instantiates a narrow Spring application context which is explicitly defined by an inner configuration class.
 * We need an application context because the declarative authorization checks (@Preauthorize, etc.) are part of the
 * magic Spring adds to its beans via proxies. This class can be a starting point for testing authorization on
 * service methods without having e.g. to start the testing with a request to a controller.
 */
@ExtendWith(SpringExtension.class)
class PartnerResourceIT {

	private static final String PARTNER_EXTERNAL_REF = "eins";
	private static final String PARTNER_NAME = "Partner 1";

	@Autowired
	private PartnerResource partnerResource;

	/**
	 * Configuration of a narrow Spring application context needed to execute the test.
	 * Because we do not start a Spring Boot test, autoconfiguration is not enabled and therefore the jeap-spring-boot-security-starter
	 * does not get activated. We have to compensate for that by enabling global method security and by providing a ServletJeapAuthorization
	 * bean for the application's authorization checks to work. The jeap-spring-boot-security-starter-test library facilitates this by
	 * providing the ServletJeapAuthorizationConfig configuration base class from which you can extend your own test configuration class.
	 */
	@Configuration
	static class TestConfiguration extends ServletJeapAuthorizationConfig {

		// You have to provide the system name and the application context to the test support base class.
		TestConfiguration(ApplicationContext applicationContext) {
			super("jme", applicationContext);
		}

		// Instantiate the bean(s) to test
		@Bean
		PartnerResource partnerResource(ServletSemanticAuthorization semanticAuthorization) {
			return new PartnerResource(semanticAuthorization);
		}
	}


	@Test
	@WithAuthentication("readRoleToken") // run the test with the JeapAuthenticationToken created by readRoleToken()
	void testGetPartnerNameByExternalRef_whenReadRole_thenSuccess() {
		String partnerName = partnerResource.getPartnerNameByExternalRef(PARTNER_EXTERNAL_REF);
		assertThat(partnerName).isEqualTo(PARTNER_NAME);
	}

	@Test
	@WithAuthentication("unrelatedRoleToken") // run the test with the JeapAuthenticationToken created by unrelatedRoleToken()
	void testGetPartnerNameByExternalRef_whenMissingReadRole_thenAccessDeniedException() {
		assertThatExceptionOfType(AccessDeniedException.class).isThrownBy(
				() -> partnerResource.getPartnerNameByExternalRef(PARTNER_EXTERNAL_REF));
	}

	// Additional tests of the remaining methods would look about the same...


	private JeapAuthenticationToken readRoleToken() {
		final SemanticApplicationRole readRole = SemanticApplicationRole.builder()
				.system("jme")
				.resource("partner")
				.operation("read")
				.build();
		return createAuthenticationForUserRoles(readRole);
	}

	private JeapAuthenticationToken unrelatedRoleToken() {
		final SemanticApplicationRole unrelatedRole = SemanticApplicationRole.builder()
				.system("jme")
				.resource("unrelated")
				.operation("read")
				.build();
		return createAuthenticationForUserRoles(unrelatedRole);
	}

	private JeapAuthenticationToken createAuthenticationForUserRoles(SemanticApplicationRole... userroles) {
		return JeapAuthenticationTestTokenBuilder.create().withUserRoles(userroles).build();
	}

}
