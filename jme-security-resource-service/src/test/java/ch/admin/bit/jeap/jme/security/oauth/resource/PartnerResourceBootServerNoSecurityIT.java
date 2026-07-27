package ch.admin.bit.jeap.jme.security.oauth.resource;

import ch.admin.bit.jeap.security.resource.semanticAuthentication.ServletSemanticAuthorization;
import ch.admin.bit.jeap.security.test.configuration.DisableJeapSecurityStarterAutoConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.micrometer.metrics.autoconfigure.export.prometheus.PrometheusScrapeEndpoint;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;


/**
 * This test class gives an example for an integration test that wants to test a Spring Boot application without security
 * i.e. without activating jeap spring boot security starter. To prohibit jeap spring boot security starter from autoconfiguring
 * itself in a Spring Boot application, the provided configuration class DisableJeapSecurityStarterAutoConfiguration can be
 * used. If the application makes use of the jeapSemanticAuthorization bean provided by the jeap spring boot security starter
 * i.e. in programmatic authorization checks or by accessing user information from JeapAuthenticationToken then such a bean
 * has to be provided by the test.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(DisableJeapSecurityStarterAutoConfiguration.class) // Prohibit jeap security starter from autoconfiguring itself
@ActiveProfiles("local")
class PartnerResourceBootServerNoSecurityIT {

    private static final String PARTNERS_BASE_URL = "/jme-security-resource-service/api/partners";
    private static final String PARTNER_NAME_GET_URL_TEMPLATE = PARTNERS_BASE_URL + "/{id}/name";
    private static final String PARTNER_EXTERNAL_REF = "eins";
    private static final String PARTNER_ID = "11111";
    private static final String PARTNER_NAME = "Partner 1";

    @LocalServerPort
    private int randomServerPort;

    // We have to provide a ServletSemanticAuthorization bean and mock calls to it as needed by the test.
    // The ServletSemanticAuthorization bean could also be mocked using the ServletSemanticAuthorizationMock test support
    // class instead of using Mockito.
    @MockitoBean
    ServletSemanticAuthorization jeapSemanticAuthorization;

    @MockitoBean
    private PrometheusScrapeEndpoint prometheusScrapeEndpoint;

    private final RestTemplate restTemplate = new RestTemplate();

    @Test
    void testGetPartnerNameByExternalRef_whenReadRole_thenSuccess() {
        // Mocking of calls to jeapSemanticAuthorization is only needed for tests on methods that make programmatic
        // authorization checks and/or access the SemanticRoleRepository and its JeapAuthenticationToken
        when(jeapSemanticAuthorization.hasRoleForPartner("partner", "read", PARTNER_ID)).thenReturn(true);

        String result = restTemplate.getForObject("http://localhost:" + randomServerPort + PARTNER_NAME_GET_URL_TEMPLATE, String.class, PARTNER_EXTERNAL_REF);

        assertThat(result).isEqualTo(PARTNER_NAME);
    }

	// additional tests...

}
