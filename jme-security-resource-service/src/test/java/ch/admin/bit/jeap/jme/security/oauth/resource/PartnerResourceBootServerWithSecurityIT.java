package ch.admin.bit.jeap.jme.security.oauth.resource;

import ch.admin.bit.jeap.security.resource.semanticAuthentication.SemanticApplicationRole;
import ch.admin.bit.jeap.security.resource.token.JeapAuthenticationContext;
import ch.admin.bit.jeap.security.test.jws.JwsBuilder;
import ch.admin.bit.jeap.security.test.jws.JwsBuilderFactory;
import ch.admin.bit.jeap.security.test.resource.configuration.JeapOAuth2IntegrationTestResourceConfiguration;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.micrometer.metrics.autoconfigure.export.prometheus.PrometheusScrapeEndpoint;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static io.restassured.RestAssured.given;

/**
 * This test class gives an example for an integration test that checks the REST endpoint of a Spring Boot application
 * that is protected with the jeap spring boot security starter. REST-assured is used to call and check the responses of
 * the REST endpoint.
 * <p>
 * To call an OAuth2 protected REST endpoint a suitable bearer token needs to be provided. Such a token can be easily created
 * with a JwsBuilder created by the jwsBuilderFactory. The key to sign the token is automatically fetched from the TestKeyProvider.
 * </p>
 * The OAuth2 resource called, i.e. the REST endpoint, must be able to verify the signature of the bearer token with the public
 * key that matches the private key used to sign the token. The OAuth2 resource fetches this public key from the URI specified
 * in the property jeap.security.oauth2.resourceserver.authorization-server.jwk-set-uri. The configuration class
 * OAuth2IntegrationTestConfiguration instantiates a REST endpoint "/.well-known/jwks.json" that provides a Java web key set
 * populated with the public key from the TestKeyProvider, which allows the OAuth2 resource to verify the test bearer token successfully.
 * </p>
 * As the resource is configured with strict audience validation (jeap.security.oauth2.resourceserver.strict-audience-validation: on),
 * the bearer token must address the resource explicitly, i.e. its audience ('aud' claim) must contain the resource id of the resource.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT,
        properties = {  "server.port=8889",
                        // Override the external authorization server JWKS configuration, use the test support JWKS endpoint instead.
                        "jeap.security.oauth2.resourceserver.authorization-server.issuer=" + JwsBuilder.DEFAULT_ISSUER,
                        // For your own implementation, don't forget to adapt the service context (jme-security-resource-service) to your own service context.
                        "jeap.security.oauth2.resourceserver.authorization-server.jwk-set-uri=http://localhost:${server.port}/jme-security-resource-service/.well-known/jwks.json"})
@Import(JeapOAuth2IntegrationTestResourceConfiguration.class)  // Enable test support for OAuth2 integration testing
@ActiveProfiles({"local"})
class PartnerResourceBootServerWithSecurityIT {

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

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
	@Autowired
	private JwsBuilderFactory jwsBuilderFactory;

    @MockitoBean
    private PrometheusScrapeEndpoint prometheusScrapeEndpoint;

	private static final String PARTNER_ID_PARAM_NAME = "id";
	private static final String PARTNERS_BASE_URL = "/jme-security-resource-service/api/partners";
	private static final String PARTNERS_GET_ONE_URL_TEMPLATE = PARTNERS_BASE_URL + "/{" + PARTNER_ID_PARAM_NAME + "}";
    private static final String PARTNERS_GET_ONE_NAME_URL_TEMPLATE = PARTNERS_GET_ONE_URL_TEMPLATE + "/name";

	private static final String PARTNER_ID = "11111";
	private static final String PARTNER_EXTERNAL_REF = "eins";
    private static final String NEW_PARTNER_ID = "12345";
    private static final String NEW_PARTNER_EXTERNAL_REF = "external-ref-new-partner";
    private static final String NEW_PARTNER_NAME = "new partner";

	private static final String SUBJECT = "69368608-D736-43C8-5F76-55B7BF168299";
	private static final JeapAuthenticationContext CONTEXT = JeapAuthenticationContext.SYS;
    // The resource id of the resource under test
    private static final String RESOURCE_ID = "jme-security-resource-service";
    // The resource id of another unrelated resource
    private static final String OTHER_RESOURCE_ID = "jme-security-other-resource";

	private final RequestSpecification partnersBaseUrlSpec;
	private final RequestSpecification partnersGetOneUrlTemplateSec;
    private final RequestSpecification partnersGetOneNameUrlTemplateSec;

	@Autowired
    PartnerResourceBootServerWithSecurityIT(@Value("${server.port}") int serverPort) {
		partnersBaseUrlSpec = new RequestSpecBuilder().setBasePath(PARTNERS_BASE_URL).setPort(serverPort).build();
		partnersGetOneUrlTemplateSec = new RequestSpecBuilder().setBasePath(PARTNERS_GET_ONE_URL_TEMPLATE).setPort(serverPort).build();
        partnersGetOneNameUrlTemplateSec = new RequestSpecBuilder().setBasePath(PARTNERS_GET_ONE_NAME_URL_TEMPLATE).setPort(serverPort).build();
	}

	@Test
    void testListPartners_whenReadRole_thenSuccess() {
        String authToken = createAuthTokenForUserRoles(PARTNER_READ_ROLE);
        given().
                spec(partnersBaseUrlSpec).
                auth().oauth2(authToken).
        when().
                get().
        then().
                statusCode(HttpStatus.OK.value());
    }

    @Test
    void testListPartners_whenMissingRole_thenForbidden() {
        String authToken = createAuthTokenForUserRoles(UNRELATED_ROLE);
        given().
                spec(partnersBaseUrlSpec).
                auth().oauth2(authToken).
        when().
                get().
        then().
                statusCode(HttpStatus.FORBIDDEN.value());
    }

    @Test
    void testListPartners_whenTokenForOtherAudience_thenUnauthorized() {
        // A token addressing another resource is rejected, even though it carries the required role.
        String authToken = createAuthTokenBuilderForUserRoles(PARTNER_READ_ROLE).
                withAudiences(OTHER_RESOURCE_ID).
                build().serialize();
        given().
                spec(partnersBaseUrlSpec).
                auth().oauth2(authToken).
        when().
                get().
        then().
                statusCode(HttpStatus.UNAUTHORIZED.value());
    }

    @Test
    void testListPartners_whenTokenWithoutAudience_thenUnauthorized() {
        // With strict audience validation switched on, a token without an audience is rejected,
        // even though it carries the required role.
        String authToken = createAuthTokenBuilderForUserRoles(PARTNER_READ_ROLE).
                build().serialize();
        given().
                spec(partnersBaseUrlSpec).
                auth().oauth2(authToken).
        when().
                get().
        then().
                statusCode(HttpStatus.UNAUTHORIZED.value());
    }

    @Test
    void testGetPartnerById_whenReadRole_thenSuccess() {
        String authToken = createAuthTokenForUserRoles(PARTNER_READ_ROLE);
        given().
                spec(partnersGetOneUrlTemplateSec).
                pathParam(PARTNER_ID_PARAM_NAME, PARTNER_ID).
                auth().oauth2(authToken).
        when().
                get().
        then().
                statusCode(HttpStatus.OK.value());
    }

    @Test
    void testGetPartnerById_whenMissingReadRole_thenForbidden() {
        String authToken = createAuthTokenForUserRoles(UNRELATED_ROLE);
        given().
                spec(partnersGetOneUrlTemplateSec).
                pathParam(PARTNER_ID_PARAM_NAME, PARTNER_ID).
                auth().oauth2(authToken).
        when().
                get().
        then().
                statusCode(HttpStatus.FORBIDDEN.value());
    }

    @Test
    void testGetPartnerByExternalRef_whenReadRole_thenSuccess() {
        String authToken = createAuthTokenForUserRoles(PARTNER_READ_ROLE);
        given().
                spec(partnersGetOneUrlTemplateSec).
                pathParam(PARTNER_ID_PARAM_NAME, PARTNER_EXTERNAL_REF).
                auth().oauth2(authToken).
            when().
                get().
        then().
                statusCode(HttpStatus.OK.value());
    }

    @Test
    void testGetPartnerByExternalRef_whenMissingReadRole_thenForbidden() {
        String authToken = createAuthTokenForUserRoles(UNRELATED_ROLE);
        given().
                spec(partnersGetOneUrlTemplateSec).
                pathParam(PARTNER_ID_PARAM_NAME, PARTNER_EXTERNAL_REF).
                auth().oauth2(authToken).
        when().
                get().
        then().
                statusCode(HttpStatus.FORBIDDEN.value());
    }

    @Test
    void testGetPartnerNameByExternalRef_whenReadRole_thenSuccess() {
        String authToken = createAuthTokenForUserRoles(PARTNER_READ_ROLE);
        given().
                spec(partnersGetOneNameUrlTemplateSec).
                pathParam(PARTNER_ID_PARAM_NAME, PARTNER_EXTERNAL_REF).
                auth().oauth2(authToken).
        when().
                get().
        then().
                statusCode(HttpStatus.OK.value());
    }

    @Test
    void testGetPartnerNameByExternalRef_whenMissingReadRole_thenForbidden() {
        String authToken = createAuthTokenForUserRoles(UNRELATED_ROLE);
        given().
                spec(partnersGetOneNameUrlTemplateSec).
                pathParam(PARTNER_ID_PARAM_NAME, PARTNER_EXTERNAL_REF).
                auth().oauth2(authToken).
        when().
                get().
        then().
                statusCode(HttpStatus.FORBIDDEN.value());
    }

    @Test
    void testCreateOrUpdatePartner_whenWriteRole_thenSuccess() {
        String authToken = createAuthTokenForUserRoles(PARTNER_WRITE_ROLE);
        Partner partner = new Partner(NEW_PARTNER_ID, NEW_PARTNER_EXTERNAL_REF, NEW_PARTNER_NAME);
        given().
                spec(partnersBaseUrlSpec).
                body(partner).
                contentType(ContentType.JSON).
                auth().oauth2(authToken).
        when().
                put().
        then().
                statusCode(HttpStatus.OK.value());
    }

    @Test
    void testCreateOrUpdatePartner_whenMissingWriteRole_thenForbidden() {
        String authToken = createAuthTokenForUserRoles(UNRELATED_ROLE);
        Partner partner = new Partner(NEW_PARTNER_ID, NEW_PARTNER_EXTERNAL_REF, NEW_PARTNER_NAME);
        given().
                spec(partnersBaseUrlSpec).
                body(partner).
                contentType(ContentType.JSON).
                auth().oauth2(authToken).
        when().
                put().
        then().
                statusCode(HttpStatus.FORBIDDEN.value());
    }

	private String createAuthTokenForUserRoles(SemanticApplicationRole... userroles) {
		return createAuthTokenBuilderForUserRoles(userroles).
				withAudiences(RESOURCE_ID). // the token must address the resource under test
				build().serialize();
	}

	private JwsBuilder createAuthTokenBuilderForUserRoles(SemanticApplicationRole... userroles) {
		return jwsBuilderFactory.createValidForFixedLongPeriodBuilder(SUBJECT, CONTEXT).
				withUserRoles(userroles);
	}

}
