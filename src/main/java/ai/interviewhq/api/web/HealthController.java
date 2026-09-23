package ai.interviewhq.api.web;

import ai.interviewhq.api.IhApiApplication;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class HealthController {

    private final Environment environment;

    public HealthController(Environment environment) {
        this.environment = environment;
    }

    @GetMapping("/meta")
    public ResponseEntity<Map<String, Object>> meta() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("service", "ih-api");
        body.put("version", "1.0.0");
        body.put("status", "up");
        body.put("profiles", environment.getActiveProfiles());
        body.put("timestamp", Instant.now().toString());
        body.put("implementation", IhApiApplication.class.getPackageName());
        return ResponseEntity.ok(body);
    }
}
