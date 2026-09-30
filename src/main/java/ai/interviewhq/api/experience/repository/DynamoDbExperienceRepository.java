package ai.interviewhq.api.experience.repository;

import ai.interviewhq.api.infrastructure.dynamodb.DynamoDbDataMapper;
import ai.interviewhq.api.infrastructure.dynamodb.DynamoDbPage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.GetItemResponse;
import software.amazon.awssdk.services.dynamodb.model.QueryRequest;
import software.amazon.awssdk.services.dynamodb.model.QueryResponse;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class DynamoDbExperienceRepository implements ExperienceRepository {
    private static final Logger log = LoggerFactory.getLogger(DynamoDbExperienceRepository.class);
    private static final String EXPERIENCE_ENTITY_SK = "ENTITY";
    private static final String EXPERIENCE_RUN_PREFIX = "EXPERIENCE_RUN#";
    private static final String EXPERIENCE_LIST_ENTITY = "ExperienceListIndex";

    private final DynamoDbClient client;
    private final String table;

    public DynamoDbExperienceRepository(
            DynamoDbClient client,
            @Value("${aws.dynamodb.table}") String table) {
        this.client = client;
        this.table = table;
    }

    @Override
    public Optional<Map<String, Object>> findById(Integer id) {
        if (id == null) return Optional.empty();

        try {
            GetItemResponse response = client.getItem(GetItemRequest.builder()
                    .tableName(table)
                    .key(key("EXPERIENCE#" + id, EXPERIENCE_ENTITY_SK))
                    .build());

            return response.hasItem()
                    ? Optional.of(DynamoDbDataMapper.unwrapMap(response.item()))
                    : Optional.empty();
        } catch (RuntimeException ex) {
            log.warn("dynamodb_get_failed operation=experience_detail");
            throw ex;
        }
    }

    @Override
    public DynamoDbPage<Map<String, Object>> list(
            String partitionKey,
            boolean scanForward,
            int limit,
            Map<String, String> startKey) {
        if (partitionKey == null || partitionKey.isBlank()) {
            return new DynamoDbPage<>(List.of(), Map.of());
        }

        QueryRequest.Builder query = QueryRequest.builder()
                .tableName(table)
                .keyConditionExpression("#pk = :pk")
                .filterExpression("#entityType = :entityType")
                .expressionAttributeNames(Map.of("#pk", "pk", "#entityType", "entityType"))
                .expressionAttributeValues(Map.of(
                        ":pk", AttributeValue.builder().s(partitionKey).build(),
                        ":entityType", AttributeValue.builder().s(EXPERIENCE_LIST_ENTITY).build()))
                .limit(limit)
                .scanIndexForward(scanForward);

        if (startKey != null && !startKey.isEmpty()) {
            query.exclusiveStartKey(stringKeyToAttributeKey(startKey));
        }

        try {
            QueryResponse response = client.query(query.build());
            List<Map<String, Object>> items = new ArrayList<>();
            for (Map<String, AttributeValue> item : response.items()) {
                AttributeValue entityType = item.get("entityType");
                if (entityType != null && EXPERIENCE_LIST_ENTITY.equals(entityType.s())) {
                    items.add(DynamoDbDataMapper.unwrapMap(item));
                }
            }
            return new DynamoDbPage<>(List.copyOf(items), stringKey(response.lastEvaluatedKey()));
        } catch (RuntimeException ex) {
            log.warn("dynamodb_query_failed operation=experience_list");
            throw ex;
        }
    }

    @Override
    public DynamoDbPage<Map<String, Object>> listByCrawlRun(
            Integer crawlRunId,
            int limit,
            Map<String, String> startKey) {
        if (crawlRunId == null) {
            return new DynamoDbPage<>(List.of(), Map.of());
        }

        QueryRequest.Builder query = QueryRequest.builder()
                .tableName(table)
                .keyConditionExpression("#pk = :pk AND begins_with(#sk, :sk)")
                .expressionAttributeNames(Map.of("#pk", "pk", "#sk", "sk"))
                .expressionAttributeValues(Map.of(
                        ":pk", AttributeValue.builder().s(EXPERIENCE_RUN_PREFIX + crawlRunId).build(),
                        ":sk", AttributeValue.builder().s("EXPERIENCE#").build()))
                .limit(limit)
                .scanIndexForward(false);

        if (startKey != null && !startKey.isEmpty()) {
            query.exclusiveStartKey(stringKeyToAttributeKey(startKey));
        }

        try {
            QueryResponse response = client.query(query.build());
            List<Map<String, AttributeValue>> indexItems = response.items();
            if (indexItems.isEmpty()) {
                return new DynamoDbPage<>(List.of(), stringKey(response.lastEvaluatedKey()));
            }

            List<Map<String, AttributeValue>> experienceKeys = new ArrayList<>();
            for (Map<String, AttributeValue> item : indexItems) {
                AttributeValue sk = item.get("sk");
                if (sk != null && sk.s() != null && sk.s().startsWith("EXPERIENCE#")) {
                    experienceKeys.add(key(sk.s(), EXPERIENCE_ENTITY_SK));
                }
            }

            if (experienceKeys.isEmpty()) {
                return new DynamoDbPage<>(List.of(), stringKey(response.lastEvaluatedKey()));
            }

            var batchResponse = client.batchGetItem(
                    software.amazon.awssdk.services.dynamodb.model.BatchGetItemRequest.builder()
                            .requestItems(Map.of(table,
                                    software.amazon.awssdk.services.dynamodb.model.KeysAndAttributes.builder()
                                            .keys(experienceKeys).build()))
                            .build());

            Map<String, Map<String, Object>> byPk = new java.util.HashMap<>();
            for (Map<String, AttributeValue> item :
                    batchResponse.responses().getOrDefault(table, List.of())) {
                AttributeValue pk = item.get("pk");
                if (pk != null && pk.s() != null) {
                    byPk.put(pk.s(), DynamoDbDataMapper.unwrapMap(item));
                }
            }

            List<Map<String, Object>> experiences = new ArrayList<>();
            for (Map<String, AttributeValue> key : experienceKeys) {
                AttributeValue pk = key.get("pk");
                if (pk != null && pk.s() != null) {
                    Map<String, Object> experience = byPk.get(pk.s());
                    if (experience != null && !experience.isEmpty()) {
                        experiences.add(experience);
                    }
                }
            }

            return new DynamoDbPage<>(List.copyOf(experiences), stringKey(response.lastEvaluatedKey()));
        } catch (RuntimeException ex) {
            log.warn("dynamodb_read_failed operation=experience_crawl_run");
            throw ex;
        }
    }

    private static Map<String, AttributeValue> key(String pk, String sk) {
        return Map.of(
                "pk", AttributeValue.builder().s(pk).build(),
                "sk", AttributeValue.builder().s(sk).build());
    }

    private static Map<String, AttributeValue> stringKeyToAttributeKey(Map<String, String> key) {
        Map<String, AttributeValue> result = new LinkedHashMap<>();
        key.forEach((name, value) -> result.put(name, AttributeValue.builder().s(value).build()));
        return result;
    }

    private static Map<String, String> stringKey(Map<String, AttributeValue> key) {
        if (key == null || key.isEmpty()) return Map.of();

        Map<String, String> result = new LinkedHashMap<>();
        key.forEach((name, value) -> {
            if (value != null && value.s() != null) {
                result.put(name, value.s());
            }
        });
        return Map.copyOf(result);
    }
}
