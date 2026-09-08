package ch.admin.bit.jeap.jme.security;

import ch.admin.bit.jeap.jme.test.BootServiceSpringIntegrationTestBase;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.ValidatableResponse;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.not;

/**
 * End-to-end test of the security example: starts the OAuth mock server and all example services with their 'local'
 * profiles on free ports and exercises the features of the jEAP security starter the example demonstrates, mostly
 * through the endpoints of the client service (see the 'Local Test URLs' section of the README).
 * <p>
 * Started services and access chains:
 * <ul>
 *     <li>auth-scs: the OAuth mock server issuing the tokens of all example clients</li>
 *     <li>client-service -> resource-service</li>
 *     <li>client-service -> clientresource-service -> resource-service</li>
 *     <li>client-service -> scs (current user endpoint)</li>
 *     <li>a second resource-service instance with a deliberately misconfigured introspection client</li>
 *     <li>a third resource-service instance used exclusively by the token introspection caching test</li>
 * </ul>
 */
class SecurityExampleIT extends BootServiceSpringIntegrationTestBase {

    private static final List<Integer> SERVICE_PORTS = reserveFreePorts(7);
    private static final int AUTH_PORT_INDEX = 0;
    private static final int RESOURCE_PORT_INDEX = 1;
    private static final int CLIENT_RESOURCE_PORT_INDEX = 2;
    private static final int SCS_PORT_INDEX = 3;
    private static final int CLIENT_PORT_INDEX = 4;
    private static final int MISCONFIGURED_RESOURCE_PORT_INDEX = 5;
    private static final int CACHING_RESOURCE_PORT_INDEX = 6;

    private static final String RESOURCE_MODULE = "jme-security-resource-service";
    private static final String AUTH_BASE_URL = baseUrl(AUTH_PORT_INDEX, "jme-security-auth-scs");
    private static final String RESOURCE_BASE_URL = baseUrl(RESOURCE_PORT_INDEX, RESOURCE_MODULE);
    private static final String MISCONFIGURED_RESOURCE_BASE_URL = baseUrl(MISCONFIGURED_RESOURCE_PORT_INDEX, RESOURCE_MODULE);
    private static final String CACHING_RESOURCE_BASE_URL = baseUrl(CACHING_RESOURCE_PORT_INDEX, RESOURCE_MODULE);
    private static final String CLIENT_RESOURCE_BASE_URL = baseUrl(CLIENT_RESOURCE_PORT_INDEX, "jme-security-clientresource-service");
    private static final String SCS_BASE_URL = baseUrl(SCS_PORT_INDEX, "jme-security-scs");
    private static final String CLIENT_BASE_URL = baseUrl(CLIENT_PORT_INDEX, "jme-security-client-service");

    private static final String JWK_SET_URL = AUTH_BASE_URL + "/.well-known/jwks.json";
    private static final String INTROSPECTION_URL = AUTH_BASE_URL + "/oauth2/introspect";

    private static final String SERVER_PORT_PROPERTY = "server.port";
    private static final String ISSUER_PROPERTY = "jeap.security.oauth2.resourceserver.authorization-server.issuer";
    private static final String JWK_SET_URI_PROPERTY = "jeap.security.oauth2.resourceserver.authorization-server.jwk-set-uri";
    private static final String INTROSPECTION_URI_PROPERTY = "jeap.security.oauth2.resourceserver.authorization-server.introspection.uri";
    private static final String INTROSPECTION_CLIENT_ID_PROPERTY = "jeap.security.oauth2.resourceserver.authorization-server.introspection.client-id";
    private static final String RESOURCE_URL_PROPERTY = "jme.security.client.resource-url";

    private static final String RESOURCE_ID = RESOURCE_MODULE;
    private static final String AUDIENCE_CLAIM = "aud";
    private static final String ROLES_PRUNED_CHARS_CLAIM = "roles_pruned_chars";

    // Clients registered on the OAuth mock server (see application-local.yml of jme-security-auth-scs)
    private static final String CLIENT_SERVICE_CLIENT_ID = "jme-security-client-service";
    private static final String ROLES_PRUNED_CLIENT_ID = "jme-security-client-service-roles-pruned";
    private static final String ALTERNATE_ROLES_CLIENT_ID = "jme-security-client-service-alternate-roles";
    private static final String BP_SCOPED_CLIENT_ID = "jme-security-client-service-bpscoped";
    // The resource service introspects with the client whose id equals its resource id (the mock server requires the
    // introspection client id to be contained in the audience of the introspected token, like Keycloak does).
    private static final String INTROSPECTION_CLIENT_ID = RESOURCE_ID;
    private static final String CLIENT_SECRET = "secret";

    // Credentials of the basic auth protected 'info' endpoints
    private static final String BASIC_AUTH_USER = "user";
    private static final String BASIC_AUTH_PASSWORD = "secret";

    // The bproles scope of a client with 'bproles-scope-enabled' selects the business partners whose roles are
    // included in the token: a single partner ('bproles:11111') or all partners ('bproles:*'), no scope -> no bproles.
    private static final String ALL_PARTNERS_BPROLES_SCOPE = "bproles:*";

    private static final String PARTNERS_PATH = "/api/partners";
    private static final String INFO_PATH = "/api/info";
    private static final String INTROSPECTED_ROLES_PATH = "/api/introspected-roles";
    private static final String HAS_BEEN_INTROSPECTED = "hasBeenIntrospected";
    private static final String ROLES_PRUNED_CHARS = "rolesPrunedChars";
    private static final String USERROLES = "userroles";
    private static final String BPROLES = "bproles";
    private static final String PARTNER_1 = "Partner 1";
    private static final String PARTNER_2 = "Partner 2";
    private static final String THING_1 = "Thing1";
    private static final String THING_2 = "Thing2";
    private static final String THING_3 = "Thing3";
    private static final String THING_9 = "Thing9";
    private static final String PARTNER_READ_ROLE = "jme_@partner_#read";
    private static final String THING_READ_ROLE = "jme_@thing_#read";

    // Prometheus endpoint of the resource service (see jeap.monitor.prometheus in its application.yml) and the names
    // of the token introspection metrics of the security starter as exposed on it
    private static final String PROMETHEUS_PATH = "/actuator/prometheus";
    private static final String PROMETHEUS_USER = "prometheus";
    private static final String PROMETHEUS_PASSWORD = "secret";
    private static final String INTROSPECTION_CACHE_LOOKUPS_METRIC = "jeap_security_token_introspection_cache_lookups_total";
    private static final String INTROSPECTION_ENDPOINT_REQUESTS_METRIC = "jeap_security_token_introspection_endpoint_requests_seconds_count";
    private static final String CACHE_HIT_LABEL = "result=\"hit\"";
    private static final String CACHE_MISS_LABEL = "result=\"miss\"";

    @BeforeAll
    static void startServices() throws Exception {
        // The OAuth2 clients resolve the issuer of the mock server at startup -> start the mock server first.
        startService("jme-security-auth-scs", AUTH_BASE_URL, Map.of(
                SERVER_PORT_PROPERTY, port(AUTH_PORT_INDEX),
                "mockserver.base-url", AUTH_BASE_URL));
        startService(RESOURCE_MODULE, RESOURCE_BASE_URL, Map.of(
                SERVER_PORT_PROPERTY, port(RESOURCE_PORT_INDEX),
                ISSUER_PROPERTY, AUTH_BASE_URL,
                JWK_SET_URI_PROPERTY, JWK_SET_URL,
                INTROSPECTION_URI_PROPERTY, INTROSPECTION_URL));
        // A second, deliberately misconfigured resource service instance that introspects with a client whose id is
        // not contained in the audience of the tokens issued for the resource service (see
        // introspectionRejectsTokenIfIntrospectionClientIsNotContainedInItsAudience).
        startService(RESOURCE_MODULE, MISCONFIGURED_RESOURCE_BASE_URL, Map.of(
                SERVER_PORT_PROPERTY, port(MISCONFIGURED_RESOURCE_PORT_INDEX),
                ISSUER_PROPERTY, AUTH_BASE_URL,
                JWK_SET_URI_PROPERTY, JWK_SET_URL,
                INTROSPECTION_URI_PROPERTY, INTROSPECTION_URL,
                INTROSPECTION_CLIENT_ID_PROPERTY, CLIENT_SERVICE_CLIENT_ID));
        // A third resource service instance (configured like the first one) that is used by the token introspection
        // caching test only: the test asserts exact increments of the introspection metrics of the instance, which
        // no other test must interfere with (see resourceServiceIntrospectsRepeatedlyPresentedTokenOnlyOnceThanksToIntrospectionCache).
        startService(RESOURCE_MODULE, CACHING_RESOURCE_BASE_URL, Map.of(
                SERVER_PORT_PROPERTY, port(CACHING_RESOURCE_PORT_INDEX),
                ISSUER_PROPERTY, AUTH_BASE_URL,
                JWK_SET_URI_PROPERTY, JWK_SET_URL,
                INTROSPECTION_URI_PROPERTY, INTROSPECTION_URL));
        startService("jme-security-clientresource-service", CLIENT_RESOURCE_BASE_URL, Map.of(
                SERVER_PORT_PROPERTY, port(CLIENT_RESOURCE_PORT_INDEX),
                ISSUER_PROPERTY, AUTH_BASE_URL,
                JWK_SET_URI_PROPERTY, JWK_SET_URL,
                RESOURCE_URL_PROPERTY, RESOURCE_BASE_URL));
        // The SCS depends on the UI module: starting both in the same reactor resolves the UI from the reactor instead
        // of requiring an installed UI artifact (spring-boot:run is skipped on the UI module, see its pom).
        startService("jme-security-ui,jme-security-scs", SCS_BASE_URL, Map.of(
                SERVER_PORT_PROPERTY, port(SCS_PORT_INDEX),
                ISSUER_PROPERTY, AUTH_BASE_URL,
                JWK_SET_URI_PROPERTY, JWK_SET_URL));
        startService("jme-security-client-service", CLIENT_BASE_URL, Map.of(
                SERVER_PORT_PROPERTY, port(CLIENT_PORT_INDEX),
                "spring.security.oauth2.client.provider.local-mock.issuer-uri", AUTH_BASE_URL,
                RESOURCE_URL_PROPERTY, RESOURCE_BASE_URL,
                "jme.security.client.client-resource-url", CLIENT_RESOURCE_BASE_URL,
                "jme.security.client.scs-url", SCS_BASE_URL));
    }

    // --- client-service -> resource-service: declarative and programmatic semantic authorization ---
    // The client service authenticates with the client 'jme-security-client-service' whose user roles grant
    // 'read' on the resources 'partner' and 'thing' for all business partners.

    @Test
    void clientServiceListsAllPartnersFromResourceService() {
        // @PreAuthorize("hasRole('partner','read')") and hasRoleForAllPartners() -> all partners are listed
        getFromClientService(PARTNERS_PATH)
                .body(containsString("Partner list:"))
                .body(containsString(PARTNER_1))
                .body(containsString(PARTNER_2))
                .body(containsString("Partner 8"))
                .body(containsString("Partner 9"));
    }

    @Test
    void clientServiceGetsPartnerByIdFromResourceService() {
        // @PreAuthorize("hasRoleForPartner('partner','read', #partnerId)")
        getFromClientService(PARTNERS_PATH + "/11111")
                .body(containsString("Partner '11111' data:"))
                .body(containsString("\"externalRef\":\"eins\""))
                .body(containsString(PARTNER_1));
    }

    @Test
    void clientServiceGetsPartnerByExternalRefFromResourceService() {
        // @PostAuthorize("hasRoleForPartner('partner','read', returnObject.getId())")
        getFromClientService(PARTNERS_PATH + "/eins")
                .body(containsString("\"id\":\"11111\""))
                .body(containsString(PARTNER_1));
    }

    @Test
    void clientServiceGetsPartnerNameByExternalRefFromResourceService() {
        // programmatic check with jeapSemanticAuthorization.hasRoleForPartner()
        getFromClientService(PARTNERS_PATH + "/eins/name")
                .body(equalTo("Partner 'eins' name: " + PARTNER_1));
    }

    @Test
    void clientServiceListsAllThingsFromResourceService() {
        // @PreAuthorize("hasRole('thing', 'read')") and hasRoleForAllPartners() -> things of all partners are listed
        getFromClientService("/api/things")
                .body(containsString("Got things:"))
                .body(containsString(THING_1))
                .body(containsString(THING_3))
                .body(containsString(THING_9));
    }

    @Test
    void clientServiceListsThingsOfPartnerFromResourceService() {
        // @PreAuthorize("hasRoleForPartner('thing', 'read', #partnerId)")
        getFromClientService(PARTNERS_PATH + "/11111/things")
                .body(containsString(THING_1))
                .body(containsString(THING_2))
                .body(not(containsString(THING_3)));
    }

    @Test
    void clientServiceGetsThingByIdFromResourceService() {
        // @PostAuthorize("hasRoleForPartner('thing', 'read', returnObject.getPartnerId())")
        getFromClientService("/api/things/1")
                .body(containsString("Got thing with id '1':"))
                .body(containsString("\"partnerId\":\"11111\""))
                .body(containsString(THING_1));
    }

    // --- client-service -> resource-service: authorization by operation only ---

    @Test
    void clientServiceListsThingsAuthorizedByOperationOnly() {
        // @PreAuthorize("hasOperation('read')") and hasOperationForAllPartners()
        getFromClientService("/api/operation-things")
                .body(containsString("operation-only auth"))
                .body(containsString(THING_1))
                .body(containsString(THING_3))
                .body(containsString(THING_9));
    }

    @Test
    void clientServiceListsThingsOfPartnerAuthorizedByOperationOnly() {
        // @PreAuthorize("hasOperationForPartner('read', #partnerId)")
        getFromClientService("/api/operation-things/partners/11111")
                .body(containsString(THING_1))
                .body(containsString(THING_2))
                .body(not(containsString(THING_3)));
    }

    @Test
    void clientServiceGetsThingByIdAuthorizedByOperationOnly() {
        // programmatic check with jeapSemanticAuthorization.hasOperationForPartner()
        getFromClientService("/api/operation-things/1")
                .body(containsString("Got thing with id '1' (operation-only auth):"))
                .body(containsString(THING_1));
    }

    // --- client-service -> clientresource-service -> resource-service: RestClient token strategies ---

    @Test
    void clientResourceServiceCallsResourceServiceWithItsOwnToken() {
        // createForClientRegistryId(): the clientresource service fetches its own token for the resource service
        getFromClientService(PARTNERS_PATH + "?target=clientresource")
                .body(containsString("Partner list:"))
                .body(containsString(PARTNER_1))
                .body(containsString(PARTNER_2));
    }

    @Test
    void clientResourceServiceForwardsIncomingTokenToResourceService() {
        // createForTokenFromIncomingRequest(): the token of the client service is forwarded to the resource service
        getFromClientService(PARTNERS_PATH + "/11111?target=clientresource")
                .body(containsString("Partner '11111' data:"))
                .body(containsString(PARTNER_1));
    }

    @Test
    void clientResourceServicePrefersIncomingTokenOverItsOwnToken() {
        // createForClientRegistryIdPreferringTokenFromIncomingRequest() with a token in the incoming request
        getFromClientService(PARTNERS_PATH + "/eins?target=clientresource")
                .body(containsString("\"id\":\"11111\""))
                .body(containsString(PARTNER_1));
    }

    @Test
    void clientResourceServiceFallsBackToItsOwnTokenOnBasicAuthProtectedEndpoint() {
        // createForClientRegistryIdPreferringTokenFromIncomingRequest() without a token in the incoming request:
        // the basic auth protected 'info' endpoint calls the OAuth2 protected partner resource with the service's own token
        given()
                .auth().preemptive().basic(BASIC_AUTH_USER, BASIC_AUTH_PASSWORD)
                .get(CLIENT_RESOURCE_BASE_URL + INFO_PATH)
                .then()
                .statusCode(200)
                .body(containsString("Partners instead of info:"))
                .body(containsString(PARTNER_1));
    }

    // --- endpoints excluded from the OAuth2 protection of the security starter ---

    @Test
    void clientServiceForwardsBasicAuthRequestToResourceService() {
        // the 'info' endpoint of the resource service is protected by basic auth instead of OAuth2
        getFromClientService(INFO_PATH)
                .body(equalTo("Info : Some info"));
    }

    @Test
    void resourceServiceInfoEndpointRequiresBasicAuth() {
        given()
                .get(RESOURCE_BASE_URL + INFO_PATH)
                .then()
                .statusCode(401);
        given()
                .auth().preemptive().basic(BASIC_AUTH_USER, BASIC_AUTH_PASSWORD)
                .get(RESOURCE_BASE_URL + INFO_PATH)
                .then()
                .statusCode(200)
                .body(equalTo("Some info"));
    }

    // --- token introspection (introspection mode 'lightweight' on the resource service) ---

    @Test
    void resourceServiceDoesNotIntrospectTokenWithEmbeddedRoles() {
        // the token of 'jme-security-client-service' carries its roles -> no introspection needed
        JsonPath introspectedRoles = getFromClientService(INTROSPECTED_ROLES_PATH)
                .extract().jsonPath(); // the client service forwards the JSON of the resource service as text/plain

        assertThat(introspectedRoles.getBoolean(HAS_BEEN_INTROSPECTED)).isFalse();
        assertThat(introspectedRoles.getInt(ROLES_PRUNED_CHARS)).isZero();
        assertThat(introspectedRoles.getList(USERROLES)).isEmpty();
        assertThat(introspectedRoles.getMap(BPROLES)).isEmpty();
    }

    @Test
    void resourceServiceRecoversPrunedRolesByTokenIntrospection() {
        // the roles of 'jme-security-client-service-roles-pruned' exceed the pruning limit of the mock server and are
        // therefore not embedded in the token -> the resource service fetches them from the introspection endpoint
        JsonPath introspectedRoles = getFromClientService(INTROSPECTED_ROLES_PATH + "?pruned=true")
                .extract().jsonPath();

        assertPrunedRolesRecoveredByIntrospection(introspectedRoles);
    }

    @Test
    void introspectionRejectsTokenIfIntrospectionClientIsNotContainedInItsAudience() {
        // A token with pruned roles must be introspected by the resource to see the roles associated with the token.
        String prunedRolesToken = fetchAccessToken(AUTH_BASE_URL, ROLES_PRUNED_CLIENT_ID, CLIENT_SECRET);
        assertThat(tokenClaims(prunedRolesToken)).containsKey(ROLES_PRUNED_CHARS_CLAIM);
        assertThat(tokenAudience(prunedRolesToken)).containsExactly(RESOURCE_ID);

        // The mock server (like Keycloak) only reports a token as active on introspection if the introspecting client
        // is contained in the token's audience. The correctly configured resource service introspects with its own id
        // -> the pruned roles are restored
        given()
                .auth().oauth2(prunedRolesToken)
                .get(RESOURCE_BASE_URL + PARTNERS_PATH)
                .then()
                .statusCode(200)
                .body(containsString(PARTNER_1));

        // The misconfigured resource service introspects with the id of the client service: the token passes the
        // audience validation of the resource, but its introspection reports it as inactive -> token rejected
        given()
                .auth().oauth2(prunedRolesToken)
                .get(MISCONFIGURED_RESOURCE_BASE_URL + PARTNERS_PATH)
                .then()
                .statusCode(401);

        // Tokens with embedded roles are not introspected in the 'lightweight' mode -> the misconfiguration goes
        // unnoticed for them
        String tokenWithEmbeddedRoles = fetchAccessToken(AUTH_BASE_URL, CLIENT_SERVICE_CLIENT_ID, CLIENT_SECRET);
        given()
                .auth().oauth2(tokenWithEmbeddedRoles)
                .get(MISCONFIGURED_RESOURCE_BASE_URL + PARTNERS_PATH)
                .then()
                .statusCode(200)
                .body(containsString(PARTNER_1));
    }

    @Test
    void resourceServiceIntrospectsRepeatedlyPresentedTokenOnlyOnceThanksToIntrospectionCache() {
        // The 'local' profile of the resource service caches the introspection responses of the mock server. A fresh
        // token with pruned roles is not yet known to the cache -> the resource service must introspect it on the
        // introspection endpoint once, further requests with the same token are served from the cache. The cache
        // lookups and the requests to the introspection endpoint are observed through the metrics of the security
        // starter on the Prometheus endpoint of the resource service.
        //
        // CAUTION: The metrics are counters global to the resource service instance and the assertions expect exact
        // increments. The test therefore runs against a resource service instance of its own. On an instance shared
        // with other tests (or with any other traffic introspecting tokens at the same time), their introspections
        // would show up in the counters as well and the test would become flaky.
        String prunedRolesToken = fetchAccessToken(AUTH_BASE_URL, ROLES_PRUNED_CLIENT_ID, CLIENT_SECRET);
        IntrospectionMetrics beforeFirstRequest = readIntrospectionMetrics(CACHING_RESOURCE_BASE_URL);

        // first request: cache miss -> introspection endpoint queried, response cached
        assertPrunedRolesRecoveredByIntrospection(getIntrospectedRolesFromResourceService(CACHING_RESOURCE_BASE_URL, prunedRolesToken));
        IntrospectionMetrics afterFirstRequest = readIntrospectionMetrics(CACHING_RESOURCE_BASE_URL);
        assertThat(afterFirstRequest.cacheMisses()).isEqualTo(beforeFirstRequest.cacheMisses() + 1);
        assertThat(afterFirstRequest.cacheHits()).isEqualTo(beforeFirstRequest.cacheHits());
        assertThat(afterFirstRequest.endpointRequests()).isEqualTo(beforeFirstRequest.endpointRequests() + 1);

        // second request with the same token: cache hit -> the cached response enriches the token exactly like the
        // fresh introspection did, without a request to the introspection endpoint
        assertPrunedRolesRecoveredByIntrospection(getIntrospectedRolesFromResourceService(CACHING_RESOURCE_BASE_URL, prunedRolesToken));
        IntrospectionMetrics afterSecondRequest = readIntrospectionMetrics(CACHING_RESOURCE_BASE_URL);
        assertThat(afterSecondRequest.cacheHits()).isEqualTo(afterFirstRequest.cacheHits() + 1);
        assertThat(afterSecondRequest.cacheMisses()).isEqualTo(afterFirstRequest.cacheMisses());
        assertThat(afterSecondRequest.endpointRequests()).isEqualTo(afterFirstRequest.endpointRequests());
    }

    // --- client-service -> scs: current user endpoint of the security starter ---

    @Test
    void currentUserEndpointReturnsRolesAsInTokenInStandardSyntax() {
        getFromClientService("/api/current-user")
                .body("userRoles", hasItems(PARTNER_READ_ROLE, THING_READ_ROLE))
                .body("myCustomValue", equalTo("fooBar")); // added by the JeapCurrentUserCustomizer of the SCS
    }

    @Test
    void currentUserEndpointReturnsRolesAsInTokenInAlternateSyntax() {
        // 'jme-security-client-service-alternate-roles' has its roles in the eIAM syntax system_@resource_!operation.
        // The current user endpoint returns the roles as they appear in the token, see
        // semanticAuthorizationAcceptsRolesInAlternateSyntax for the authorization with such roles.
        getFromClientService("/api/current-user?alternateRoles=true")
                .body("userRoles", hasItems("jme_@partner_!read", "jme_@thing_!read"))
                .body("myCustomValue", equalTo("fooBar"));
    }

    // --- dynamic scope 'bproles:<partner>' restricting the business partner roles in the token ---

    @Test
    void clientServiceRequestsTokenRestrictedToOneBusinessPartnerWithBprolesScope() {
        // 'jme-security-client-service-bpscoped' has roles for the partners 11111 and 22222, the client service
        // requests its token with the scope 'bproles:11111'
        Map<String, Object> bproles = getFromClientService("/api/bproles?scoped=true")
                .extract().jsonPath().getMap("$");

        assertThat(bproles).containsOnlyKeys("11111");
    }

    // --- direct calls to the resource service with tokens from the mock server ---

    @Test
    void protectedResourceRejectsAnonymousRequests() {
        given()
                .get(RESOURCE_BASE_URL + PARTNERS_PATH)
                .then()
                .statusCode(401);
    }

    @Test
    void strictAudienceValidationAcceptsOnlyTokensAddressingTheResource() {
        // The token of the client service addresses the resource service in its 'aud' claim -> accepted
        String tokenWithAudience = fetchAccessToken(AUTH_BASE_URL, CLIENT_SERVICE_CLIENT_ID, CLIENT_SECRET);
        assertThat(tokenAudience(tokenWithAudience)).contains(RESOURCE_ID);
        given()
                .auth().oauth2(tokenWithAudience)
                .get(RESOURCE_BASE_URL + PARTNERS_PATH)
                .then()
                .statusCode(200)
                .body(containsString(PARTNER_1));

        // The introspection client of the resource service is not meant to call any resource, its tokens carry no
        // audience -> rejected as the resource service only accepts tokens that address it in their 'aud' claim.
        String tokenWithoutAudience = fetchAccessToken(AUTH_BASE_URL, INTROSPECTION_CLIENT_ID, CLIENT_SECRET);
        assertThat(tokenAudience(tokenWithoutAudience)).isEmpty();
        given()
                .auth().oauth2(tokenWithoutAudience)
                .get(RESOURCE_BASE_URL + PARTNERS_PATH)
                .then()
                .statusCode(401);
    }

    @Test
    void semanticAuthorizationAcceptsRolesInAlternateSyntax() {
        // The roles of 'jme-security-client-service-alternate-roles' grant 'partner read' in the eIAM syntax
        String accessToken = fetchAccessToken(AUTH_BASE_URL, ALTERNATE_ROLES_CLIENT_ID, CLIENT_SECRET);

        given()
                .auth().oauth2(accessToken)
                .get(RESOURCE_BASE_URL + PARTNERS_PATH)
                .then()
                .statusCode(200)
                .body(containsString(PARTNER_1));
    }

    @Test
    void businessPartnerRolesRestrictPartnerListToGrantedPartners() {
        // 'jme-security-client-service-bpscoped' has 'partner read' only for the partner 11111 -> only Partner 1 listed
        String accessToken = fetchAccessToken(BP_SCOPED_CLIENT_ID, ALL_PARTNERS_BPROLES_SCOPE);

        given()
                .auth().oauth2(accessToken)
                .get(RESOURCE_BASE_URL + PARTNERS_PATH)
                .then()
                .statusCode(200)
                .body("id", contains("11111"));
    }

    @Test
    void accessToPartnerWithoutMatchingBusinessPartnerRoleIsDenied() {
        // 'jme-security-client-service-bpscoped' has 'partner read' for the partner 11111 but only 'foo bar' for 22222
        String accessToken = fetchAccessToken(BP_SCOPED_CLIENT_ID, ALL_PARTNERS_BPROLES_SCOPE);

        given()
                .auth().oauth2(accessToken)
                .get(RESOURCE_BASE_URL + PARTNERS_PATH + "/11111")
                .then()
                .statusCode(200)
                .body("name", equalTo(PARTNER_1));
        given()
                .auth().oauth2(accessToken)
                .get(RESOURCE_BASE_URL + PARTNERS_PATH + "/22222")
                .then()
                .statusCode(403);
        given()
                .auth().oauth2(accessToken)
                .get(RESOURCE_BASE_URL + "/api/things/3") // Thing3 belongs to the partner 22222 -> denied by @PostAuthorize
                .then()
                .statusCode(403);
    }

    @SuppressWarnings("SameParameterValue")
    private static JsonPath getIntrospectedRolesFromResourceService(String resourceBaseUrl, String token) {
        return given()
                .auth().oauth2(token)
                .get(resourceBaseUrl + INTROSPECTED_ROLES_PATH)
                .then()
                .statusCode(200)
                .extract().jsonPath();
    }

    /**
     * Asserts the introspected roles response for a token of 'jme-security-client-service-roles-pruned': the roles of
     * the client exceed the pruning limit of the mock server and are therefore not embedded in the token, the resource
     * service has to fetch them from the introspection endpoint (or its introspection cache).
     */
    private static void assertPrunedRolesRecoveredByIntrospection(JsonPath introspectedRoles) {
        assertThat(introspectedRoles.getBoolean(HAS_BEEN_INTROSPECTED)).isTrue();
        assertThat(introspectedRoles.getInt(ROLES_PRUNED_CHARS)).isPositive();
        assertThat(introspectedRoles.getList(USERROLES, String.class))
                .contains(PARTNER_READ_ROLE, THING_READ_ROLE,
                        "jme_@some-resource-1_#some-operation-1", "jme_@some-resource-2_#some-operation-2");
        assertThat(introspectedRoles.getMap(BPROLES)).isEmpty();
    }

    @SuppressWarnings("SameParameterValue")
    private static IntrospectionMetrics readIntrospectionMetrics(String resourceBaseUrl) {
        String prometheusMetrics = given()
                .auth().preemptive().basic(PROMETHEUS_USER, PROMETHEUS_PASSWORD)
                .get(resourceBaseUrl + PROMETHEUS_PATH)
                .then()
                .statusCode(200)
                .extract().asString();
        return new IntrospectionMetrics(
                sumMetricSamples(prometheusMetrics, INTROSPECTION_CACHE_LOOKUPS_METRIC, CACHE_HIT_LABEL),
                sumMetricSamples(prometheusMetrics, INTROSPECTION_CACHE_LOOKUPS_METRIC, CACHE_MISS_LABEL),
                sumMetricSamples(prometheusMetrics, INTROSPECTION_ENDPOINT_REQUESTS_METRIC));
    }

    /**
     * Sums the samples of a metric in the Prometheus text format (one sample per line: name, labels in curly braces
     * and the value, e.g. {@code name{issuer="...",result="hit"} 3.0}) over all label sets containing the given labels.
     * A metric that has not been registered yet (no samples) sums up to zero.
     */
    private static long sumMetricSamples(String prometheusMetrics, String metricName, String... labels) {
        return prometheusMetrics.lines()
                .filter(line -> line.startsWith(metricName + "{") || line.startsWith(metricName + " "))
                .filter(line -> Arrays.stream(labels).allMatch(line::contains))
                .mapToLong(line -> (long) Double.parseDouble(line.substring(line.lastIndexOf(' ') + 1)))
                .sum();
    }

    private record IntrospectionMetrics(long cacheHits, long cacheMisses, long endpointRequests) {
    }

    private static String baseUrl(int portIndex, String contextPath) {
        return "http://localhost:" + SERVICE_PORTS.get(portIndex) + "/" + contextPath;
    }

    private static String port(int portIndex) {
        return String.valueOf(SERVICE_PORTS.get(portIndex));
    }

    private static ValidatableResponse getFromClientService(String path) {
        return given()
                .get(CLIENT_BASE_URL + path)
                .then()
                .statusCode(200);
    }

    @SuppressWarnings("SameParameterValue")
    private static String fetchAccessToken(String clientId, String scope) {
        return given()
                .baseUri(AUTH_BASE_URL)
                .contentType(ContentType.URLENC)
                .formParam("grant_type", "client_credentials")
                .formParam("client_id", clientId)
                .formParam("client_secret", CLIENT_SECRET)
                .formParam("scope", scope)
                .when()
                .post("/oauth2/token")
                .then()
                .statusCode(200)
                .extract()
                .path("access_token");
    }

    private static Map<String, Object> tokenClaims(String jwt) {
        String payload = new String(Base64.getUrlDecoder().decode(jwt.split("\\.")[1]), StandardCharsets.UTF_8);
        return JsonPath.from(payload).getMap("$");
    }

    private static List<String> tokenAudience(String jwt) {
        // A single audience value may be serialized as a plain string instead of an array (RFC 7519, section 4.1.3)
        Object audience = tokenClaims(jwt).get(AUDIENCE_CLAIM);
        return switch (audience) {
            case null -> List.of();
            case String single -> List.of(single);
            case List<?> multiple -> multiple.stream().map(String::valueOf).toList();
            default -> throw new IllegalStateException("Unexpected audience claim value: " + audience);
        };
    }
}
