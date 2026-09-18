package ch.admin.bit.jeap.jme.skeleton.scs;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Route;
import com.microsoft.playwright.options.AriaRole;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Browser tests for authentication and role-based route authorization.
 */
class UiAuthorizationBrowserIT
        extends SecurityUiBrowserTestBase {

    @Test
    void userOverview_withAuthorizedRoles_completesOAuthFlow() {
        openUserOverviewAs(UserProfile.FULL_ACCESS);

        assertThat(page.locator("ob-master-layout-header"))
                .containsText("JME Security Example");

        /*
         * The protected route was loaded after the browser completed the
         * authorization-code flow against the OAuth mock server.
         */
        assertThat(page.url())
                .startsWith(APP_URL)
                .doesNotContain("security-ui-oidc-mock");

        JsonNode claims = readJsonCard();

        assertThat(claims.path("sub").asText())
                .isEqualTo(SUBJECT);
    }

    @Test
    void userOverview_withoutRelevantRoles_redirectsToStartPage() {
        openBrowserAs(UserProfile.UNRELATED);

        page.navigate(USER_OVERVIEW_URL);

        /*
         * The frontend route guard redirects unauthorized users back
         * to the application start page rather than to /forbidden.
         */
        page.waitForURL(APP_URL);

        assertThat(page.url())
                .isEqualTo(APP_URL);

        assertThat(
                page.locator("app-user-overview")
        ).not().isVisible();
    }

    @Test
    void userOverview_withReadOnlyRoles_isAccessible() {
        openUserOverviewAs(UserProfile.READ_ONLY);

        JsonNode claims = readJsonCard();

        assertThat(stringArray(claims))
                .contains(
                        EXAMPLE_READ_ROLE,
                        PARTNER_READ_ROLE,
                        THING_READ_ROLE,
                        SYSTEM_ROLE
                )
                .doesNotContain(EXAMPLE_WRITE_ROLE);
    }

    @Test
    void userOverview_doesNotLoadEportalServiceNavigation() {
        openBrowserAs(UserProfile.FULL_ACCESS);
        List<String> requestedUrls = new CopyOnWriteArrayList<>();
        page.onRequest(request -> requestedUrls.add(request.url()));

        openUserOverview();

        assertThat(requestedUrls)
                .noneMatch(url -> url.contains("pams-api.eportal"))
                .noneMatch(url -> url.contains("service-navigation-web-component.js"));
    }

    @Test
    void header_logsOutThroughTheAuthorizationServer() {
        openBrowserAs(UserProfile.FULL_ACCESS);
        // The shared authorization-code mock has no logout endpoint. Add its discovery entry for this scenario.
        page.route(ISSUER + "/.well-known/openid-configuration", route -> {
            var discovery = route.fetch();
            ObjectNode body = parseObject(discovery.text());
            body.put("end_session_endpoint", ISSUER + "/logout");
            route.fulfill(new Route.FulfillOptions().setResponse(discovery).setBody(body.toString()));
        });
        page.route(ISSUER + "/logout**", route -> route.fulfill(new Route.FulfillOptions()
                .setContentType("text/html").setBody("<h1>Logged out</h1>")));
        openUserOverview();

        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Abmelden")).click();

        page.waitForURL(ISSUER + "/logout**");
        assertThat(page.url()).contains("id_token_hint=", "post_logout_redirect_uri=");
        assertThat(page.getByRole(AriaRole.HEADING)).hasText("Logged out");
    }

    @Test
    void header_canChangeLanguageWithoutEportal() {
        openUserOverviewAs(UserProfile.FULL_ACCESS);

        page.locator("#ob-language-dropdown").click();
        page.locator("#ob-language-en-option").click();

        assertThat(page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Log out"))).isVisible();
        assertThat(jsonCard()).containsText(SUBJECT);
    }

    @Test
    void userOverview_deepLinkCanBeReloaded() {
        openBrowserAs(UserProfile.FULL_ACCESS);
        page.navigate(APP_URL + "user");
        assertThat(jsonCard()).containsText(SUBJECT);

        page.reload();

        assertThat(jsonCard()).containsText(SUBJECT);
        assertThat(jsonCard(CURRENT_USER_TITLE)).containsText(GIVEN_NAME);
    }

    @Test
    void silentRenewCallback_isPackagedUnderTheScsContextPath() {
        openUserOverviewAs(UserProfile.FULL_ACCESS);

        var response = context.request().get(APP_URL + "assets/auth/silent-renew.html");

        assertThat(response.status()).isEqualTo(200);
        assertThat(response.text()).contains("oidc-silent-renew-message");
    }

    private static ObjectNode parseObject(String json) {
        try {
            return (ObjectNode) new ObjectMapper().readTree(json);
        } catch (java.io.IOException exception) {
            throw new AssertionError("Expected a JSON configuration object", exception);
        }
    }
}
