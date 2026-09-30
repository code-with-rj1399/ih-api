package ai.interviewhq.api.config;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class WebConfigTest {

    @Autowired
    private CorsConfigurationSource corsConfigurationSource;

    @Test
    void allowsConfiguredLocalOrigin() {
        HttpServletRequest request =
                new org.springframework.mock.web.MockHttpServletRequest(
                        "GET",
                        "/api/v1/health");

        CorsConfiguration configuration =
                corsConfigurationSource.getCorsConfiguration(request);

        assertTrue(configuration != null);
        assertEquals(
                "http://localhost:3000",
                configuration.checkOrigin("http://localhost:3000"));
        assertTrue(configuration.getAllowedMethods().contains("GET"));
        assertTrue(configuration.getAllowedMethods().contains("OPTIONS"));
    }
}
