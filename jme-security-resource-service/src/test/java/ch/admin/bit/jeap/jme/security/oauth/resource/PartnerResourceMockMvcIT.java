package ch.admin.bit.jeap.jme.security.oauth.resource;

import ch.admin.bit.jeap.security.resource.semanticAuthentication.SemanticApplicationRole;
import ch.admin.bit.jeap.security.resource.token.JeapAuthenticationToken;
import ch.admin.bit.jeap.security.test.resource.JeapAuthenticationTestTokenBuilder;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.micrometer.metrics.autoconfigure.export.prometheus.PrometheusScrapeEndpoint;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.json.JsonMapper;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * This test class gives an example for integration tests checking REST controllers and user authorization.
 * The tests make use of the Spring Test MockMvc functionality which allows to run tests on MVC controllers
 * without starting up a servlet container. We use the MockMvc functionality to directly set the authentication
 * associated with a test request with a JeapAuthenticationToken custom-built to the tests needs.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK) // MOCK -> no application server started
// Must be a profile that includes a non empty jeap.security.oauth2.resourceserver.authorization-server.issuer property.
// Otherwise, the jeap-spring-boot-security-starter autoconfiguration would not be activated and the security not be configured.
@ActiveProfiles("local")
@AutoConfigureMockMvc
class PartnerResourceMockMvcIT {

	private static final String PARTNERS_BASE_URL = "/api/partners";
	private static final String PARTNERS_GET_ONE_URL_TEMPLATE = PARTNERS_BASE_URL + "/{id}";
    private static final String PARTNERS_GET_ONE_NAME_URL_TEMPLATE = PARTNERS_GET_ONE_URL_TEMPLATE + "/name";
	private static final String PARTNER_ID = "11111";
	private static final String PARTNER_EXTERNAL_REF = "eins";
    private static final String NEW_PARTNER_ID = "12345";
    private static final String NEW_PARTNER_EXTERNAL_REF = "external-ref-new-partner";
    private static final String NEW_PARTNER_NAME = "new partner";

	private static final SemanticApplicationRole PARTNER_READ_ROLE = SemanticApplicationRole.builder()
            .system("jme")
            .resource("partner")
            .operation("read")
            .build();

	private static final SemanticApplicationRole PARTNER_WRITE_ROLE = SemanticApplicationRole.builder()
            .system("jme")
            .resource("partner")
            .operation("write")
            .build();

    private static final SemanticApplicationRole UNRELATED_ROLE = SemanticApplicationRole.builder()
            .system("jme")
            .resource("unrelated")
            .operation("read")
            .build();

    @Autowired
	private MockMvc mvc;

    @Autowired
    private JsonMapper jsonMapper;

    @MockitoBean
    private PrometheusScrapeEndpoint prometheusScrapeEndpoint;

	@Test
    void testListPartners_whenReadRole_thenSuccess() throws Exception {
        JeapAuthenticationToken authentication = createAuthenticationForUserRoles(PARTNER_READ_ROLE);
        mvc.perform(
                get(PARTNERS_BASE_URL).
                        with(authentication(authentication))).
                andExpect(status().isOk());
    }

    @Test
    void testListPartners_whenMissingReadRole_thenForbidden() throws Exception {
        JeapAuthenticationToken authentication = createAuthenticationForUserRoles(UNRELATED_ROLE);
        mvc.perform(
                get(PARTNERS_BASE_URL).
                        with(authentication(authentication))).
                andExpect(status().isForbidden());
    }

    @Test
    void testGetPartnerById_whenReadRole_thenSuccess() throws Exception {
        JeapAuthenticationToken authentication = createAuthenticationForUserRoles(PARTNER_READ_ROLE);
        mvc.perform(
                get(PARTNERS_GET_ONE_URL_TEMPLATE, PARTNER_ID).
                        with(authentication(authentication))).
                andExpect(status().isOk());
    }

    @Test
    void testGetPartnerById_whenMissingReadRole_thenForbidden() throws Exception {
        JeapAuthenticationToken authentication = createAuthenticationForUserRoles(UNRELATED_ROLE);
        mvc.perform(
                get(PARTNERS_GET_ONE_URL_TEMPLATE, PARTNER_ID).
                        with(authentication(authentication))).
                andExpect(status().isForbidden());
    }

    @Test
    void testGetPartnerByExternalRef_whenReadRole_thenSuccess() throws Exception {
        JeapAuthenticationToken authentication = createAuthenticationForUserRoles(PARTNER_READ_ROLE);
        mvc.perform(
                get(PARTNERS_GET_ONE_URL_TEMPLATE, PARTNER_EXTERNAL_REF).
                        with(authentication(authentication))).
                andExpect(status().isOk());
    }

    @Test
    void testGetPartnerByExternalRef_whenMissingRole_thenForbidden() throws Exception {
        JeapAuthenticationToken authentication = createAuthenticationForUserRoles(UNRELATED_ROLE);
        mvc.perform(
                get(PARTNERS_GET_ONE_URL_TEMPLATE, PARTNER_EXTERNAL_REF).
                        with(authentication(authentication))).
                andExpect(status().isForbidden());
    }

    @Test
    void testGetPartnerNameByExternalRef_whenReadRole_thenSuccess() throws Exception {
        JeapAuthenticationToken authentication = createAuthenticationForUserRoles(PARTNER_READ_ROLE);
        mvc.perform(
                get(PARTNERS_GET_ONE_NAME_URL_TEMPLATE, PARTNER_EXTERNAL_REF).
                        with(authentication(authentication))).
                andExpect(status().isOk());
    }

    @Test
    void testGetPartnerNameByExternalRef_whenMissingRole_thenForbidden() throws Exception {
        JeapAuthenticationToken authentication = createAuthenticationForUserRoles(UNRELATED_ROLE);
        mvc.perform(
                get(PARTNERS_GET_ONE_NAME_URL_TEMPLATE, PARTNER_EXTERNAL_REF).
                        with(authentication(authentication))).
                andExpect(status().isForbidden());
    }

    @Test
    void testCreateOrUpdatePartner_whenWriteRole_thenSuccess() throws Exception {
        JeapAuthenticationToken authentication = createAuthenticationForUserRoles(PARTNER_WRITE_ROLE);
	    String newPartnerJsonStr = createPartnerJsonString(NEW_PARTNER_ID, NEW_PARTNER_EXTERNAL_REF, NEW_PARTNER_NAME);
        mvc.perform(
                put(PARTNERS_BASE_URL).
                        content(newPartnerJsonStr).
                        contentType(MediaType.APPLICATION_JSON).
                        with(authentication(authentication))).
                andExpect(status().is2xxSuccessful());
    }

    @Test
    void testCreateOrUpdatePartner_whenMissingRole_thenForbidden() throws Exception {
        JeapAuthenticationToken authentication = createAuthenticationForUserRoles(UNRELATED_ROLE);
        String newPartnerJsonStr = createPartnerJsonString(NEW_PARTNER_ID, NEW_PARTNER_EXTERNAL_REF, NEW_PARTNER_NAME);
        mvc.perform(
                put(PARTNERS_BASE_URL).
                        content(newPartnerJsonStr).
                        contentType(MediaType.APPLICATION_JSON).
                        with(authentication(authentication))).
                andExpect(status().isForbidden());
    }

	private JeapAuthenticationToken createAuthenticationForUserRoles(SemanticApplicationRole... userroles)  {
		return JeapAuthenticationTestTokenBuilder.create().withUserRoles(userroles).build();
	}

    private String createPartnerJsonString(String id, String externalRef, String name) throws JsonProcessingException {
        return jsonMapper.writeValueAsString(new Partner(id, externalRef, name));
    }

}
