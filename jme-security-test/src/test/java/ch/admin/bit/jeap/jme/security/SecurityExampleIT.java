package ch.admin.bit.jeap.jme.security;

import ch.admin.bit.jeap.jme.test.BootServiceSpringIntegrationTestBase;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;

class SecurityExampleIT extends BootServiceSpringIntegrationTestBase {

    private static final List<Integer> SERVICE_PORTS = reserveFreePorts(2);
    private static final int AUTH_PORT = SERVICE_PORTS.getFirst();
    private static final int RESOURCE_PORT = SERVICE_PORTS.get(1);
    private static final String AUTH_BASE_URL = "http://localhost:" + AUTH_PORT + "/jme-security-auth-scs";
    private static final String RESOURCE_BASE_URL = "http://localhost:" + RESOURCE_PORT + "/jme-security-resource-service";

    @BeforeAll
    static void startServices() throws Exception {
        startService("jme-security-auth-scs", AUTH_BASE_URL, Map.of(
                "server.port", String.valueOf(AUTH_PORT),
                "mockserver.base-url", AUTH_BASE_URL));
        startService("jme-security-resource-service", RESOURCE_BASE_URL, Map.of(
                "server.port", String.valueOf(RESOURCE_PORT),
                "jeap.security.oauth2.resourceserver.authorization-server.issuer", AUTH_BASE_URL,
                "jeap.security.oauth2.resourceserver.authorization-server.jwk-set-uri",
                AUTH_BASE_URL + "/.well-known/jwks.json"));
    }

    @Test
    void clientCredentialsTokenCanAccessProtectedBusinessResource() {
        String accessToken = fetchAccessToken(AUTH_BASE_URL, "jme-security-client-service", "secret");

        given()
                .auth().oauth2(accessToken)
                .get(RESOURCE_BASE_URL + "/api/partners")
                .then()
                .statusCode(200)
                .body(containsString("Partner 1"));
    }

    @Test
    void protectedResourceRejectsAnonymousRequests() {
        given()
                .get(RESOURCE_BASE_URL + "/api/partners")
                .then()
                .statusCode(401);
    }
}
