package ai.interviewhq.api.web;

import ai.interviewhq.api.IhApiApplication;
import ai.interviewhq.api.config.DynamoDbProperties;
import ai.interviewhq.api.security.SecurityProperties;
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
    private final DynamoDbProperties dynamoDbProperties;
    private final SecurityProperties securityProperties;

    public HealthController(Environment environment,
                            DynamoDbProperties dynamoDbProperties,
                            SecurityProperties securityProperties) {
        this.environment = environment;
        this.dynamoDbProperties = dynamoDbProperties;
        this.securityProperties = securityProperties;
    }

    @GetMapping("/meta")
    public ResponseEntity<Map<String, Object>> meta() {
        Map<String, Object> dynamodb = new LinkedHashMap<>();
        dynamodb.put("table", dynamoDbProperties.getTable());
        dynamodb.put("region", dynamoDbProperties.getRegion());
        dynamodb.put("endpointOverride", dynamoDbProperties.hasEndpointOverride()
                ? dynamoDbProperties.getEndpoint()
                : "aws");
        dynamodb.put("autoCreateTable", dynamoDbProperties.isAutoCreateTable());
        dynamodb.put("mode", dynamoDbProperties.hasEndpointOverride() ? "local" : "aws");

        Map<String, Object> security = new LinkedHashMap<>();
        security.put("enabled", securityProperties.isEnabled());
        security.put("header", securityProperties.getHeader());
        security.put("configuredKeys", securityProperties.resolvedKeys().size());

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("service", "ih-api");
        body.put("version", "1.0.0");
        body.put("status", "up");
        body.put("profiles", environment.getActiveProfiles());
        body.put("timestamp", Instant.now().toString());
        body.put("implementation", IhApiApplication.class.getPackageName());
        body.put("dynamodb", dynamodb);
        body.put("security", security);
        return ResponseEntity.ok(body);
    }
}
