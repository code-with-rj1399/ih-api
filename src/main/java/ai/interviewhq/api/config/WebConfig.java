package ai.interviewhq.api.config;
import org.springframework.beans.factory.annotation.Value; import org.springframework.context.annotation.Bean; import org.springframework.context.annotation.Configuration; import org.springframework.web.cors.CorsConfiguration; import org.springframework.web.cors.CorsConfigurationSource; import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.Arrays;
@Configuration public class WebConfig {
 @Bean CorsConfigurationSource corsConfigurationSource(@Value("${api.cors.allowed-origins:http://localhost:3000}")String origins){
  CorsConfiguration c=new CorsConfiguration(); c.setAllowedOrigins(Arrays.stream(origins.split(",")).map(String::trim).filter(s->!s.isBlank()).toList()); c.setAllowedMethods(java.util.List.of("GET","OPTIONS")); c.setAllowedHeaders(java.util.List.of("Content-Type","Accept")); c.setAllowCredentials(false); c.setMaxAge(3600L);
  UrlBasedCorsConfigurationSource s=new UrlBasedCorsConfigurationSource();s.registerCorsConfiguration("/api/v1/**",c);return s;
 }
}
