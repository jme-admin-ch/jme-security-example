package ch.admin.bit.jeap.jme.skeleton.scs;

import com.fasterxml.jackson.databind.JsonNode;
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

        assertThat(page.locator("[qd-shell-header] .title"))
                .hasText("JME Security Example");

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
}
