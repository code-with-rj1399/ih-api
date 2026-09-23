package ai.interviewhq.api.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;
import java.util.Optional;

public class ApiKeyAuthenticationFilter extends OncePerRequestFilter {

    private final SecurityProperties properties;

    public ApiKeyAuthenticationFilter(SecurityProperties properties) {
        this.properties = properties;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !properties.isEnabled();
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String presented = extractKey(request);
        if (presented == null || presented.isBlank()) {
            chain.doFilter(request, response);
            return;
        }
        Optional<SecurityProperties.ApiKey> match = properties.resolvedKeys().stream()
                .filter(candidate -> constantTimeEquals(candidate.getKey(), presented))
                .findFirst();
        if (match.isEmpty()) {
            unauthorized(response, "Invalid API key");
            return;
        }
        SecurityProperties.ApiKey key = match.get();
        List<String> roles = key.getRoles() == null || key.getRoles().isEmpty()
                ? List.of("READ")
                : key.getRoles();
        SecurityContextHolder.getContext().setAuthentication(new ApiKeyAuthentication(key.getName(), roles));
        chain.doFilter(request, response);
    }

    private String extractKey(HttpServletRequest request) {
        String headerName = properties.getHeader() == null || properties.getHeader().isBlank()
                ? "X-API-Key"
                : properties.getHeader();
        String header = request.getHeader(headerName);
        if (header != null && !header.isBlank()) {
            return header.trim();
        }
        String authorization = request.getHeader("Authorization");
        if (authorization != null && authorization.regionMatches(true, 0, "Bearer ", 0, 7)) {
            return authorization.substring(7).trim();
        }
        return null;
    }

    private static boolean constantTimeEquals(String expected, String presented) {
        if (expected == null || presented == null) {
            return false;
        }
        byte[] a = expected.getBytes(StandardCharsets.UTF_8);
        byte[] b = presented.getBytes(StandardCharsets.UTF_8);
        return MessageDigest.isEqual(a, b);
    }

    private static void unauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write("{\"error\":\"unauthorized\",\"message\":\"" + message + "\"}");
    }
}
