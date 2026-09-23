package ai.interviewhq.api.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import java.net.URI;

/**
 * Builds a single {@link DynamoDbClient} used by every repository.
 *
 * <p>Local/dev set {@code ih.dynamodb.endpoint} (DynamoDB Local). stg/prod leave
 * the endpoint blank so the SDK talks to AWS DynamoDB in {@code ih.dynamodb.region}
 * using the default credential chain (task/instance role in AWS).
 */
@Configuration
@EnableConfigurationProperties(DynamoDbProperties.class)
public class DynamoDbConfig {

    private static final Logger log = LoggerFactory.getLogger(DynamoDbConfig.class);

    @Bean(destroyMethod = "close")
    DynamoDbClient dynamoDbClient(DynamoDbProperties properties) {
        var builder = DynamoDbClient.builder().region(Region.of(properties.getRegion()));

        if (properties.hasEndpointOverride()) {
            builder.endpointOverride(URI.create(properties.getEndpoint()));
            log.info("DynamoDB client using endpoint override {} in region {}",
                    properties.getEndpoint(), properties.getRegion());
        } else {
            log.info("DynamoDB client using AWS regional endpoint in {}", properties.getRegion());
        }

        if (properties.hasStaticCredentials()) {
            builder.credentialsProvider(StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(properties.getAccessKey(), properties.getSecretKey())));
        } else if (properties.hasEndpointOverride()) {
            // DynamoDB Local requires some credentials object but does not validate them.
            builder.credentialsProvider(StaticCredentialsProvider.create(
                    AwsBasicCredentials.create("dummy", "dummy")));
        } else {
            builder.credentialsProvider(DefaultCredentialsProvider.create());
        }

        return builder.build();
    }
}
