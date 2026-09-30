package ai.interviewhq.api.config;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import static org.junit.jupiter.api.Assertions.*;

class WebConfigTest {
    @Test
    void configuredOriginIsAllowedForPublicApi() {
        CorsConfigurationSource source = new WebConfig().corsConfigurationSource(
                "http://localhost:3000,https://app.interviewhq.ai");

        MockHttpServletRequest request =
                new MockHttpServletRequest("OPTIONS", "/api/v1/questions");
        request.addHeader("Origin", "https://app.interviewhq.ai");

        CorsConfiguration configuration = source.getCorsConfiguration(request);

        assertNotNull(configuration);
        assertNotNull(configuration.checkOrigin("https://app.interviewhq.ai"));
        assertTrue(configuration.getAllowedMethods().contains("GET"));
        assertTrue(configuration.getAllowedMethods().contains("OPTIONS"));
        assertFalse(configuration.getAllowCredentials());
    }

    @Test
    void wildcardOriginIsNotCredentialed() {
        CorsConfigurationSource source = new WebConfig().corsConfigurationSource("*");

        MockHttpServletRequest request =
                new MockHttpServletRequest("OPTIONS", "/api/v1/questions");
        request.addHeader("Origin", "https://example.com");

        CorsConfiguration configuration = source.getCorsConfiguration(request);

        assertNotNull(configuration);
        assertFalse(configuration.getAllowCredentials());
    }
}
