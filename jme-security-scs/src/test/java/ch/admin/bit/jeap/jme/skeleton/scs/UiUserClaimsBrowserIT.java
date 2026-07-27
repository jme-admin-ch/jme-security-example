package ch.admin.bit.jeap.jme.skeleton.scs;

import com.fasterxml.jackson.databind.JsonNode;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Response;
import com.microsoft.playwright.Route;
import com.microsoft.playwright.assertions.PlaywrightAssertions;
import com.microsoft.playwright.options.AriaRole;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Browser tests for claims and current-user sections shown by
 * app-user-overview.
 */
class UiUserClaimsBrowserIT extends SecurityUiBrowserTestBase {

    @Test
    void allClaimSections_areDisplayed() {
        openUserOverviewAs(UserProfile.FULL_ACCESS);

        assertSectionIsVisible(FRONTEND_CLAIMS_TITLE);
        assertSectionIsVisible(BACKEND_ROLES_TITLE);
        assertSectionIsVisible(TOKEN_CLAIMS_TITLE);
        assertSectionIsVisible(CURRENT_USER_TITLE);
    }

    @Test
    void frontendClaims_displayMockUserAndRoles() {
        openUserOverviewAs(UserProfile.FULL_ACCESS);

        JsonNode claims = readJsonCard();

        assertThat(claims.path("sub").asText())
                .isEqualTo(SUBJECT);

        assertThat(claims.path("given_name").asText())
                .isEqualTo(GIVEN_NAME);

        assertThat(claims.path("family_name").asText())
                .isEqualTo(FAMILY_NAME);

        assertThat(claims.path("name").asText())
                .isEqualTo(FULL_NAME);

        assertThat(stringArray(claims))
                .containsExactlyInAnyOrderElementsOf(ALL_ROLES);
    }

    @Test
    void jokeApi_isAllowedByCspAndDisplaysResponse() {
        openBrowserAs(UserProfile.FULL_ACCESS);
        page.route("https://icanhazdadjoke.com/", route -> route.fulfill(
                new Route.FulfillOptions()
                        .setStatus(200)
                        .setContentType("application/json")
                        .setBody("{\"joke\":\"A securely delivered joke.\"}")
        ));

        Response response = openUserOverview();

        assertThat(response.headers().get("content-security-policy"))
                .contains("connect-src 'self' https://icanhazdadjoke.com");

        page.getByRole(
                AriaRole.BUTTON,
                new Page.GetByRoleOptions().setName("Get new Joke")
        ).click();

        PlaywrightAssertions.assertThat(
                page.getByText("A securely delivered joke.")
        ).isVisible();
    }

    private void assertSectionIsVisible(String title) {
        PlaywrightAssertions.assertThat(
                page.getByText(
                        title,
                        new Page.GetByTextOptions()
                                .setExact(true)
                ).first()
        ).isVisible();
    }
}
