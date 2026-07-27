package ch.admin.bit.jeap.jme.security.oauth.resource;

import ch.admin.bit.jeap.security.resource.semanticAuthentication.SemanticApplicationRole;
import ch.admin.bit.jeap.security.resource.token.JeapAuthenticationToken;
import ch.admin.bit.jeap.security.test.resource.JeapAuthenticationTestTokenBuilder;
import io.restassured.http.ContentType;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.micrometer.metrics.autoconfigure.export.prometheus.PrometheusScrapeEndpoint;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.context.WebApplicationContext;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;


/**
 * The same as PartnerResourceMockMvcIT but using REST Assured on top of Spring Test MockMvc.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK) // MOCK -> no application server started
// Must be a profile that includes a non empty jeap.security.oauth2.resourceserver.authorization-server.issuer property.
// Otherwise the jeap-spring-boot-security-starter autoconfiguration would not be activated and the security not be configured.
@ActiveProfiles("local")
@AutoConfigureMockMvc
class PartnerResourceRestAssuredIT {

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

    @MockitoBean
    private PrometheusScrapeEndpoint prometheusScrapeEndpoint;

    @Autowired
	private WebApplicationContext webApplicationContext;

	@BeforeEach
	public void initializeRestAssuredMockMvcWebApplicationContext() {
		RestAssuredMockMvc.webAppContextSetup(webApplicationContext);
	}

	@Test
    void testListPartners_whenWriteRole_thenSuccess() {
        JeapAuthenticationToken authentication = createAuthenticationForUserRoles(PARTNER_READ_ROLE);
        given().
                auth().with(authentication(authentication)).
        when().
                get(PARTNERS_BASE_URL).
        then().
                statusCode(HttpStatus.OK.value());
    }

    @Test
    void testListPartners_whenMissingWriteRole_thenForbidden() {
        JeapAuthenticationToken authentication = createAuthenticationForUserRoles(UNRELATED_ROLE);
        given().
                auth().with(authentication(authentication)).
        when().
                get(PARTNERS_BASE_URL).
        then().
                statusCode(HttpStatus.FORBIDDEN.value());
    }

    @Test
    void testGetPartnerById_whenReadRole_thenSuccess() {
        JeapAuthenticationToken authentication = createAuthenticationForUserRoles(PARTNER_READ_ROLE);
        given().
                auth().with(authentication(authentication)).
        when().
                get(PARTNERS_GET_ONE_URL_TEMPLATE, PARTNER_ID).
        then().
                statusCode(HttpStatus.OK.value());
    }

    @Test
    void testGetPartnerById_whenMissingReadRole_thenForbidden() {
        JeapAuthenticationToken authentication = createAuthenticationForUserRoles(UNRELATED_ROLE);
        given().
                auth().with(authentication(authentication)).
        when().
                get(PARTNERS_GET_ONE_URL_TEMPLATE, PARTNER_ID).
        then().
                statusCode(HttpStatus.FORBIDDEN.value());
    }

    @Test
    void testGetPartnerByExternalRef_whenReadRole_thenSuccess() {
        JeapAuthenticationToken authentication = createAuthenticationForUserRoles(PARTNER_READ_ROLE);
        given().
                auth().with(authentication(authentication)).
        when().
                get(PARTNERS_GET_ONE_URL_TEMPLATE, PARTNER_EXTERNAL_REF).
        then().
                statusCode(HttpStatus.OK.value());
    }

    @Test
    void testGetPartnerByExternalRef_whenMissingReadRole_thenForbidden() {
        JeapAuthenticationToken authentication = createAuthenticationForUserRoles(UNRELATED_ROLE);
        given().
                auth().with(authentication(authentication)).
        when().
                get(PARTNERS_GET_ONE_URL_TEMPLATE, PARTNER_EXTERNAL_REF).
        then().
                statusCode(HttpStatus.FORBIDDEN.value());
    }

    @Test
    void testGetPartnerNameByExternalRef_whenReadRole_thenSuccess() {
        JeapAuthenticationToken authentication = createAuthenticationForUserRoles(PARTNER_READ_ROLE);
        given().
                auth().with(authentication(authentication)).
        when().
                get(PARTNERS_GET_ONE_NAME_URL_TEMPLATE, PARTNER_EXTERNAL_REF).
        then().
                statusCode(HttpStatus.OK.value());
    }

    @Test
    void testGetPartnerNameByExternalRef_whenMissingReadRole_thenForbidden() {
        JeapAuthenticationToken authentication = createAuthenticationForUserRoles(UNRELATED_ROLE);
        given().
                auth().with(authentication(authentication)).
        when().
                get(PARTNERS_GET_ONE_NAME_URL_TEMPLATE, PARTNER_EXTERNAL_REF).
        then().
                statusCode(HttpStatus.FORBIDDEN.value());
    }

    @Test
    void testCreateOrUpdatePartner_whenWriteRole_thenSuccess() {
        JeapAuthenticationToken authentication = createAuthenticationForUserRoles(PARTNER_WRITE_ROLE);
        Partner partner = new Partner(NEW_PARTNER_ID, NEW_PARTNER_EXTERNAL_REF, NEW_PARTNER_NAME);
        given().
                contentType(ContentType.JSON).
                body(partner).
                auth().with(authentication(authentication)).
        when().
                put(PARTNERS_BASE_URL).
        then().
                statusCode(HttpStatus.OK.value());
    }

    @Test
    void testCreateOrUpdatePartner_whenMissingWriteRole_thenForbidden() {
        JeapAuthenticationToken authentication = createAuthenticationForUserRoles(UNRELATED_ROLE);
        Partner partner = new Partner(NEW_PARTNER_ID, NEW_PARTNER_EXTERNAL_REF, NEW_PARTNER_NAME);
        given().
                contentType(ContentType.JSON).
                body(partner).
                auth().with(authentication(authentication)).
        when().
                put(PARTNERS_BASE_URL).
        then().
                statusCode(HttpStatus.FORBIDDEN.value());
    }

	private JeapAuthenticationToken createAuthenticationForUserRoles(SemanticApplicationRole... userroles)  {
		return JeapAuthenticationTestTokenBuilder.create().withUserRoles(userroles).build();
	}

}
