package ai.interviewhq.api.metadata.repository;

import ai.interviewhq.api.infrastructure.dynamodb.DynamoDbDataMapper;
import ai.interviewhq.api.infrastructure.dynamodb.DynamoDbPage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.QueryRequest;
import software.amazon.awssdk.services.dynamodb.model.QueryResponse;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Repository
public class DynamoDbMetadataRepository implements MetadataRepository {
    private static final Logger log = LoggerFactory.getLogger(DynamoDbMetadataRepository.class);
    private final DynamoDbClient client;
    private final String table;

    public DynamoDbMetadataRepository(
            DynamoDbClient client,
            @Value("${aws.dynamodb.table}") String table) {
        this.client = client;
        this.table = table;
    }

    @Override
    public DynamoDbPage<Map<String, Object>> list(
            String partitionKey,
            int limit,
            Map<String, String> startKey) {
        QueryRequest.Builder request = QueryRequest.builder()
                .tableName(table)
                .keyConditionExpression("#pk = :pk")
                .expressionAttributeNames(Map.of("#pk", "pk"))
                .expressionAttributeValues(Map.of(
                        ":pk", AttributeValue.builder().s(partitionKey).build()))
                .limit(limit)
                .scanIndexForward(true);

        if (startKey != null && !startKey.isEmpty()) {
            Map<String, AttributeValue> exclusiveStartKey = new LinkedHashMap<>();
            startKey.forEach((name, value) ->
                    exclusiveStartKey.put(name, AttributeValue.builder().s(value).build()));
            request.exclusiveStartKey(exclusiveStartKey);
        }

        try {
            QueryResponse response = client.query(request.build());
            List<Map<String, Object>> items = new ArrayList<>();
            for (Map<String, AttributeValue> item : response.items()) {
                AttributeValue entityType = item.get("entityType");
                if (entityType == null) continue;
                if ("CompanyMetadata".equals(entityType.s())
                        || "QuestionTypeMetadata".equals(entityType.s())) {
                    items.add(DynamoDbDataMapper.unwrapMap(item));
                }
            }
            return new DynamoDbPage<>(List.copyOf(items), stringKey(response.lastEvaluatedKey()));
        } catch (RuntimeException ex) {
            log.warn("dynamodb_query_failed operation=metadata_list");
            throw ex;
        }
    }

    private static Map<String, String> stringKey(Map<String, AttributeValue> key) {
        if (key == null || key.isEmpty()) return Map.of();
        Map<String, String> result = new LinkedHashMap<>();
        key.forEach((name, value) -> {
            if (value != null && value.s() != null) result.put(name, value.s());
        });
        return Map.copyOf(result);
    }
}
