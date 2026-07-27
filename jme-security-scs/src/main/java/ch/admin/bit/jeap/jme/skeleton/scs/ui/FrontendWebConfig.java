package ch.admin.bit.jeap.jme.skeleton.scs.ui;

import ch.admin.bit.jeap.starter.application.web.FrontendRouteRedirectExceptionHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
class FrontendWebConfig implements WebMvcConfigurer {

    @Value("${frontend.origin}")
    private String frontendOrigin;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedHeaders("*")
                .allowedMethods("*")
                .allowedOrigins(frontendOrigin)
                .allowCredentials(false);
    }

    @Bean
    public FrontendRouteRedirectExceptionHandler frontendRouteRedirectExceptionHandler() {
        return new FrontendRouteRedirectExceptionHandler();
    }
}
