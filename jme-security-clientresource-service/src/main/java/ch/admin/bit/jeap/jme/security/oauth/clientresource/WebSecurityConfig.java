package ch.admin.bit.jeap.jme.security.oauth.clientresource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;


@Configuration
public class WebSecurityConfig {

    @Bean
    @Order(100)
    public SecurityFilterChain apiInfoSecurityFilterChain(HttpSecurity http) throws Exception {
        // Exclude the 'info' API web endpoint ('/api/info/**') from the OAuth2 protection provided by jeap-spring-boot-security-starter
        // and put it under a simple basic auth protection. Protect the access to the 'info' API by the 'info-role'.
        http.securityMatchers(matchers -> matchers
                        .requestMatchers("/api/info/**"))
                .authorizeHttpRequests(authorizeHttpRequests -> authorizeHttpRequests
                        .anyRequest().hasRole("info-role"))
                .httpBasic(withDefaults());
        http.authenticationManager(createAuthManager(http.getSharedObject(AuthenticationManagerBuilder.class)));
        return http.build();
    }

    private AuthenticationManager createAuthManager(AuthenticationManagerBuilder auth) throws Exception {
        auth.inMemoryAuthentication().
                withUser("user").password("{noop}secret").roles("info-role");
        return auth.build();
    }

}
