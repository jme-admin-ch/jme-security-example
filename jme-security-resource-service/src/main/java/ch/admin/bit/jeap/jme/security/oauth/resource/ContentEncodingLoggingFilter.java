package ch.admin.bit.jeap.jme.security.oauth.resource;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Just for testing purposes, a filter that logs if the request and the response are gzip-encoded.
 */
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ContentEncodingLoggingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // Log the content encoding of the request
        String acceptEncoding = request.getHeader(HttpHeaders.ACCEPT_ENCODING);
        boolean acceptsGzip = (acceptEncoding != null) && acceptEncoding.toLowerCase().contains("gzip");
        log.info("Request Accept-Encoding ({}) includes gzip: {}", acceptEncoding, acceptsGzip);

        // Pass the request and response through the filter chain
        filterChain.doFilter(request, response);

        // Log the content encoding of the response
        String contentEncoding = response.getHeader(HttpHeaders.CONTENT_ENCODING);
        boolean isGzipEncoded = (contentEncoding != null) && contentEncoding.equalsIgnoreCase("gzip");
        log.info("Response Content-Encoding ({}) is gzip: {}", contentEncoding, isGzipEncoded);

    }

}
