package ai.interviewhq.api.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI interviewHqOpenApi() {
        SecurityScheme apiKey = new SecurityScheme()
                .type(SecurityScheme.Type.APIKEY)
                .in(SecurityScheme.In.HEADER)
                .name("X-API-Key")
                .description("Required when ih.security.enabled=true. Use IH_API_KEY_UI / CRAWLER / ADMIN.");
        return new OpenAPI()
                .info(new Info()
                        .title("InterviewHQ API")
                        .version("1.0.0")
                        .description("Canonical persistence boundary for the interview-hq DynamoDB table. "
                                + "Crawlers ingest through POST /api/v1/questions/ingest; the UI reads questions "
                                + "through the query APIs. local/dev talk to DynamoDB Local; stg/prod use AWS."))
                .servers(List.of(
                        new Server().url("/").description("Current host")))
                .components(new Components().addSecuritySchemes("ApiKey", apiKey))
                .addSecurityItem(new SecurityRequirement().addList("ApiKey"));
    }
}
