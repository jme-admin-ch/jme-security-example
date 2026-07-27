package ch.admin.bit.jeap.jme.security.oauth.resource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

import static org.springframework.security.config.Customizer.withDefaults;

/**
 * When including the jeap-spring-boot-security-starter dependency and providing the matching configuration properties
 * all web endpoints of the application will be automatically protected by OAuth2 as a default. If in addition web endpoints
 * with different protection (i.e. basic auth or no protection at all) must be provided at the same time by the application
 * an additional SecurityFilterChain bean (like the one below) needs to explicitly punch a hole into the
 * jeap-spring-boot-security-starter OAuth2 protection with an appropriate HttpSecurity configuration. The jeap security starter's
 * OAuth2 security filter chain bean has order Ordered.LOWEST_PRECEDENCE to allow overriding its configuration when needed.
 * An additional SecurityFilterChain bean must have a higher order e.g. 100 (which happens to be the default order of the now
 * deprecated Spring WebSecurityConfigurerAdapter).
 * Note: jeap-spring-boot-monitoring-starter already does exactly that for some actuator endpoints (e.g. prometheus) and
 * jeap-spring-boot-swagger-starter for the OpenAPI documentation and the Swagger UI endpoints.
 */
@Configuration
public class WebSecurityConfig {

    @Bean
    @Order(100)
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // Exclude the web endpoints of the 'info' API ('/api/info/**') and the OpenAPI documentation web endpoint ('/api-docs/**')
        // as well as the web Endpoints of the Swagger UI from the OAuth2 protection provided by jeap-spring-boot-security-starter
        // and put them under a simple basic auth protection. Protect the access to the 'info' API by the 'info-role' and the access
        // to the OpenAPI documentation and the Swagger UI by the 'apidocs-role'.
        // The use of requestMatchers() allows to override just the desired endpoints with a different security configuration.
        // Note: This configuration just makes the OpenApi documentation and the Swagger UI available as basic auth protected resources.
        //       Calling the 'partners' endpoint from within Swagger UI would still need Swagger UI to provide a valid access token.
        http.securityMatchers(matchers -> matchers
                        .requestMatchers("/api/info/**", "/api-docs/**", "/swagger-ui.html", "/swagger-ui/**"))
                .authorizeHttpRequests(authorizeHttpRequests -> authorizeHttpRequests
                        .requestMatchers("/api/info/**").hasRole("info-role")
                        .anyRequest().hasRole("apidocs-role"))
                .httpBasic(withDefaults());
        http.authenticationManager(createAuthManager(http.getSharedObject(AuthenticationManagerBuilder.class)));
        return http.build();
    }

    @Bean
    @Order(101)
    SecurityFilterChain securityFilterChainNoAuth(HttpSecurity http) throws Exception {
        // Only for testing purpose: exclude the web endpoints of the 'noauth' API ('/api/noauth/**') from the OAuth2 protection provided by
        // jeap-spring-boot-security-starter and put them without protection in order to test the jeap_rest_endpoint_without_jwt metric.
        http.securityMatchers(matchers -> matchers.requestMatchers("/api/noauth/**"))
                .authorizeHttpRequests(authorizeHttpRequests -> authorizeHttpRequests
                        .requestMatchers("/api/noauth/**").permitAll());
        return http.build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource(@Value("${server.port:'8080'") String serverPort) {
        //Define your own CORS configuration. This e.g. lets a UI deployed under localhost:4200 access
        //POST and GET Endpoint. You might need to set this environment dependant (e.g. not on PROD)
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:4200", "http://localhost:" + serverPort));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowedMethods(List.of("*"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    private AuthenticationManager createAuthManager(AuthenticationManagerBuilder auth) throws Exception {
        auth.inMemoryAuthentication().
                withUser("user").password("{noop}secret").roles("info-role").and().
                withUser("apidocs").password("{noop}secret").roles("apidocs-role");
        return auth.build();
    }

}
