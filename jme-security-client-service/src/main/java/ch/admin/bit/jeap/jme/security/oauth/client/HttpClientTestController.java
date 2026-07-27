package ch.admin.bit.jeap.jme.security.oauth.client;

import ch.admin.bit.jeap.security.restclient.JeapOAuth2RestClientBuilderFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.EntityDetails;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.ProtocolVersion;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;

/**
 * This controller creates a RestClient instance following the instructions in the jEAP documentation
 * "HTTP Compression with Spring Boot". If the clients in this controller need to be updated, so does the documentation!
 *
 * See the jEAP documentation for HTTP compression configuration details.
 */
@Slf4j
@RestController
@RequestMapping
@RequiredArgsConstructor
public class HttpClientTestController {

    private final JeapOAuth2RestClientBuilderFactory jeapOAuth2RestClientBuilderFactory;
    private final ClientServiceProperties clientProperties;

    @GetMapping("/api/httptest/restclient")
    public String listThingsDefaultRestClient() {
        RestClient restClient = createDefaultRestClient();
        return listThings(restClient);
    }

    @GetMapping("/api/httptest/restclient-custom")
    public String listThingsCustomRestClient() {
        RestClient customRestClient = createCustomRestClient();
        return listThings(customRestClient);
    }

    @GetMapping("/api/httptest/restclient-custom-http2")
    public String listThingsCustomRestClientHttp2() {
        RestClient customRestClient = createCustomRestClientHttp2();
        return listThings(customRestClient);
    }

    private String listThings(RestClient restClient) {
        String response = restClient.
                get().
                uri("/api/things").
                retrieve().
                body(String.class);
        return createThingsResponse(response);
    }

    private String createThingsResponse(String things) {
        return String.format("Got things: %s", things);
    }

    private RestClient createDefaultRestClient() {
        return jeapOAuth2RestClientBuilderFactory.createForClientRegistryId(clientProperties.getClientRegistrationId()).
                baseUrl(clientProperties.getResourceUrl()).
                build();
    }

    private RestClient createCustomRestClient() {
        // Create and customize an Apache HTTP client
        CloseableHttpClient httpClient = HttpClients.custom().
                // disableContentCompression().
                // additional http client customizations
                        build();
        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory(httpClient);
        return jeapOAuth2RestClientBuilderFactory.createForClientRegistryId(clientProperties.getClientRegistrationId()).
                baseUrl(clientProperties.getResourceUrl()).
                requestFactory(requestFactory).
                build();
    }

    private RestClient createCustomRestClientHttp2() {
        CloseableHttpClient httpClient = HttpClients.custom().
                // check that the response is an HTTP/2 response
                        addResponseInterceptorFirst(this::checkHttp2).
                build();
        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory(httpClient);
        return jeapOAuth2RestClientBuilderFactory.createForClientRegistryId(clientProperties.getClientRegistrationId()).
                baseUrl(clientProperties.getResourceUrl()).
                requestFactory(requestFactory).
                build();
    }

    private void checkHttp2(HttpResponse response, EntityDetails details, HttpContext context) throws HttpException {
        final ProtocolVersion responseProtocolVersion = context.getProtocolVersion();
        final ProtocolVersion http2 = new ProtocolVersion("HTTP", 2, 0);
        log.info("Response protocol version is {}", responseProtocolVersion);
        if (!responseProtocolVersion.greaterEquals(http2)) {
            String notHttp2Message = "Expected response protocol version equal or greater than " + http2 + " but got " + responseProtocolVersion;
            log.error(notHttp2Message);
            throw new HttpException(notHttp2Message);
        }
    }
}
