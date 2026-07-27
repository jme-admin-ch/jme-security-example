package ch.admin.bit.jeap.jme.security.oauth.client;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;

@Configuration
public class WebSecurityConfiguration {

    @Bean
    @Order(100)
    public SecurityFilterChain apiSecurityFilterChain(HttpSecurity http) throws Exception {
        // Because we did not configure OAuth2 security for this example the 'deny-all' configuration was activated and
        // we have to define our own web security configuration. For the ease of use, we simply make the API endpoints
        // accessible to everybody in this example application.
        http.securityMatcher("/api/**")
                .authorizeHttpRequests(authorizeHttpRequests -> authorizeHttpRequests
                        .anyRequest().permitAll())
                .exceptionHandling(exceptionHandling -> exceptionHandling
                        .authenticationEntryPoint((new HttpStatusEntryPoint(HttpStatus.FORBIDDEN))));
        return http.build();
    }

}
