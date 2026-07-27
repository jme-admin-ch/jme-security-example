package ch.admin.bit.jeap.jme.skeleton.scs;

import ch.admin.bit.jeap.security.test.mock.OidcAuthorizationMockServer;
import ch.admin.bit.jeap.security.test.resource.configuration.DisableJeapPermitAllSecurityConfiguration;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.Response;
import com.microsoft.playwright.assertions.PlaywrightAssertions;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.boot.micrometer.metrics.autoconfigure.export.prometheus.PrometheusScrapeEndpoint;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;
import java.util.Map;
import java.util.stream.StreamSupport;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.DEFINED_PORT;

@SpringBootTest(
        classes = Application.class,
        webEnvironment = DEFINED_PORT
)
@ActiveProfiles("frontend-e2e")
@ContextConfiguration(initializers = SecurityUiBrowserTestBase.OauthMockServerInitializer.class)
@Import(DisableJeapPermitAllSecurityConfiguration.class)
abstract class SecurityUiBrowserTestBase {

    protected static final int APP_PORT = 8889;

    protected static final String APPLICATION_NAME =
            "jme-security-scs";

    protected static final String APP_ORIGIN =
            "http://localhost:" + APP_PORT;

    protected static final String APP_CONTEXT_PATH =
            "/" + APPLICATION_NAME;

    protected static final String APP_URL =
            APP_ORIGIN + APP_CONTEXT_PATH + "/";

    protected static final String USER_OVERVIEW_URL =
            APP_URL;

    protected static final String CLIENT_ID =
            APPLICATION_NAME;

    protected static final String SYSTEM_NAME =
            "jme";

    protected static final String SUBJECT =
            "69368608-D736-43C8-5F76-55B7BF168299";

    protected static final String GIVEN_NAME =
            "E2E";

    protected static final String FAMILY_NAME =
            "Testuser";

    protected static final String FULL_NAME =
            GIVEN_NAME + " " + FAMILY_NAME;

    /*
     * Titles displayed by app-user-overview.
     */
    protected static final String FRONTEND_CLAIMS_TITLE =
            "Claims";

    protected static final String BACKEND_ROLES_TITLE =
            "Roles assigned to user (rest-call to backend)";

    protected static final String TOKEN_CLAIMS_TITLE =
            "Claims in Token (rest-call to backend)";

    protected static final String CURRENT_USER_TITLE =
            "Current-User Information from Token (rest-call to backend)";

    /*
     * Roles from the production token example.
     */
    protected static final String EXAMPLE_READ_ROLE =
            "JEAP_EXAMPLE_READ";

    protected static final String EXAMPLE_WRITE_ROLE =
            "JEAP_EXAMPLE_WRITE";

    protected static final String PARTNER_READ_ROLE =
            "jme_@partner_#read";

    protected static final String THING_READ_ROLE =
            "jme_@thing_#read";

    protected static final String SYSTEM_ROLE =
            "jme";

    /*
     * Role unrelated to this UI. The mock server requires at least one role.
     */
    protected static final String UNRELATED_ROLE =
            "jme_@unrelated_#none";

    protected static final List<String> ALL_ROLES =
            List.of(
                    EXAMPLE_READ_ROLE,
                    EXAMPLE_WRITE_ROLE,
                    PARTNER_READ_ROLE,
                    THING_READ_ROLE,
                    SYSTEM_ROLE
            );

    /*
     * All known read roles, but no write role.
     */
    protected static final List<String> READ_ONLY_ROLES =
            List.of(
                    EXAMPLE_READ_ROLE,
                    PARTNER_READ_ROLE,
                    THING_READ_ROLE,
                    SYSTEM_ROLE
            );

    protected static final List<String> UNRELATED_ROLES =
            List.of(UNRELATED_ROLE);

    private static final int OAUTH_MOCK_PORT = 8890;

    private static final String OAUTH_MOCK_BASE_PATH =
            "/security-ui-oidc-mock";

    protected static final String ISSUER =
            "http://localhost:"
                    + OAUTH_MOCK_PORT
                    + OAUTH_MOCK_BASE_PATH;

    private static final String DEFAULT_PROFILE =
            "default";

    private static final String READ_ONLY_PROFILE =
            "read-only";

    private static final String UNRELATED_PROFILE =
            "unrelated";

    private static final ObjectMapper OBJECT_MAPPER =
            new ObjectMapper();

    private static OidcAuthorizationMockServer oauthMockServer;
    private static Playwright playwright;
    private static Browser browser;


    @MockitoBean
    private PrometheusScrapeEndpoint prometheusScrapeEndpoint;

    protected BrowserContext context;
    protected Page page;

    static final class OauthMockServerInitializer implements
            ApplicationContextInitializer<ConfigurableApplicationContext> {

        @Override
        public void initialize(ConfigurableApplicationContext context) {
            ensureMockServerStarted();
        }
    }

    @BeforeAll
    static void startBrowser() {
        playwright = Playwright.create(
                new Playwright.CreateOptions()
                        .setEnv(Map.of(
                                "PLAYWRIGHT_SKIP_BROWSER_DOWNLOAD", "1"
                        ))
        );

        browser = playwright.chromium().launch(
                new BrowserType.LaunchOptions()
                        .setChannel("chrome")
                        .setHeadless(true)
        );
    }
    @AfterAll
    static void stopBrowser() {

        if (oauthMockServer != null) {
            oauthMockServer.stop();
            oauthMockServer = null;
        }

        if (browser != null) {
            browser.close();
            browser = null;
        }

        if (playwright != null) {
            playwright.close();
            playwright = null;
        }
    }

    @AfterEach
    void closeBrowserContext() {
        closeCurrentContext();
    }

    /**
     * Creates a fresh browser session configured for the selected OAuth
     * profile. The next navigation performs a new login.
     */
    protected void openBrowserAs(UserProfile profile) {
        ensureMockServerStarted();

        oauthMockServer.reset();
        oauthMockServer.setActiveProfile(profile.mockProfile());

        closeCurrentContext();

        context = browser.newContext(
                new Browser.NewContextOptions()
                        .setLocale("de-CH")
                        .setBypassCSP(true)
        );

        page = context.newPage();
        page.setDefaultTimeout(20_000);

        PlaywrightAssertions.setDefaultAssertionTimeout(15_000);
    }

    /**
     * Opens the protected user-overview route and waits until Angular has
     * rendered its root component.
     */
    protected Response openUserOverview() {
        Response response = page.navigate(USER_OVERVIEW_URL);

        PlaywrightAssertions.assertThat(
                page.locator("app-user-overview")
        ).isVisible();

        return response;
    }

    protected void openUserOverviewAs(UserProfile profile) {
        openBrowserAs(profile);
        openUserOverview();
    }

    /**
     * Returns the JSON pre-element belonging to the card with the exact title.
     *
     * This works with the existing Angular DOM and does not require test IDs.
     */
    protected Locator jsonCard() {
        return page.getByText(
                        SecurityUiBrowserTestBase.FRONTEND_CLAIMS_TITLE,
                        new Page.GetByTextOptions()
                                .setExact(true)
                )
                .first()
                .locator("xpath=ancestor::mat-card[1]")
                .locator("pre");
    }

    protected JsonNode readJsonCard() {
        String json = jsonCard().innerText();

        try {
            return OBJECT_MAPPER.readTree(json);
        } catch (JsonProcessingException exception) {
            throw new AssertionError(
                    "Card '" + SecurityUiBrowserTestBase.FRONTEND_CLAIMS_TITLE + "' does not contain valid JSON:\n"
                            + json,
                    exception
            );
        }
    }

    protected List<String> stringArray(
            JsonNode object
    ) {
        JsonNode array = object.path("userroles");

        if (!array.isArray()) {
            throw new AssertionError(
                    "JSON property '" + "userroles" + "' is not an array: "
                            + object
            );
        }

        return StreamSupport.stream(
                        array.spliterator(),
                        false
                )
                .map(JsonNode::asText)
                .toList();
    }

    private static synchronized void ensureMockServerStarted() {
        if (oauthMockServer != null) {
            return;
        }

        OidcAuthorizationMockServer mockServer =
                OidcAuthorizationMockServer.builder(
                                OAUTH_MOCK_PORT,
                                OAUTH_MOCK_BASE_PATH,
                                APP_ORIGIN
                        )
                        .withDefaultClientId(CLIENT_ID)
                        .withSubject(SUBJECT)
                        .withGivenName(GIVEN_NAME)
                        .withFamilyName(FAMILY_NAME)
                        .withName(FULL_NAME)
                        .withUserRoles(ALL_ROLES)
                        .withRoleProfile(
                                READ_ONLY_PROFILE,
                                READ_ONLY_ROLES
                        )
                        .withRoleProfile(
                                UNRELATED_PROFILE,
                                UNRELATED_ROLES
                        )
                        .build();

        mockServer.start();
        oauthMockServer = mockServer;
    }

    private void closeCurrentContext() {
        if (context != null) {
            context.close();
            context = null;
        }

        page = null;
    }

    protected enum UserProfile {
        FULL_ACCESS(DEFAULT_PROFILE),
        READ_ONLY(READ_ONLY_PROFILE),
        UNRELATED(UNRELATED_PROFILE);

        private final String mockProfile;

        UserProfile(String mockProfile) {
            this.mockProfile = mockProfile;
        }

        String mockProfile() {
            return mockProfile;
        }
    }
}
