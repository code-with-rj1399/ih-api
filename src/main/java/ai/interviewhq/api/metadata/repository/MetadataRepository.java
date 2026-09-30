package ai.interviewhq.api.metadata.repository;

import ai.interviewhq.api.infrastructure.dynamodb.DynamoDbPage;
import java.util.Map;

public interface MetadataRepository {
    DynamoDbPage<Map<String, Object>> list(
            String partitionKey,
            int limit,
            Map<String, String> startKey);
}
