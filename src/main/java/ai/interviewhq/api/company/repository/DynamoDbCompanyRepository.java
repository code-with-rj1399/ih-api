package ai.interviewhq.api.company.repository;

import ai.interviewhq.api.infrastructure.dynamodb.DynamoDbDataMapper;
import ai.interviewhq.api.infrastructure.dynamodb.DynamoDbPage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.util.*;

@Repository
public class DynamoDbCompanyRepository implements CompanyRepository {
    private static final String PK = "META#COMPANIES";
    private static final String SK_PREFIX = "COMPANY#";
    private static final String ENTITY_TYPE = "CompanyMetadata";

    private final DynamoDbClient client;
    private final String table;

    public DynamoDbCompanyRepository(DynamoDbClient client, @Value("$" + "{aws.dynamodb.table}") String table) {
        this.client = client;
        this.table = table;
    }

    @Override
    public DynamoDbPage<Map<String, Object>> list(int limit, Map<String, String> startKey) {
        QueryRequest.Builder query = QueryRequest.builder()
                .tableName(table)
                .keyConditionExpression("#pk = :pk AND begins_with(#sk, :prefix)")
                .filterExpression("#entityType = :entityType")
                .expressionAttributeNames(Map.of("#pk", "pk", "#sk", "sk", "#entityType", "entityType"))
                .expressionAttributeValues(Map.of(
                        ":pk", AttributeValue.builder().s(PK).build(),
                        ":prefix", AttributeValue.builder().s(SK_PREFIX).build(),
                        ":entityType", AttributeValue.builder().s(ENTITY_TYPE).build()))
                .limit(limit)
                .scanIndexForward(true);
        if (startKey != null && !startKey.isEmpty()) query.exclusiveStartKey(toAttributeKey(startKey));
        QueryResponse response = client.query(query.build());
        List<Map<String, Object>> items = response.items().stream()
                .filter(item -> item.get("entityType") != null && ENTITY_TYPE.equals(item.get("entityType").s()))
                .map(DynamoDbDataMapper::unwrapMap)
                .toList();
        return new DynamoDbPage<>(items, stringKey(response.lastEvaluatedKey()));
    }

    @Override
    public Optional<Map<String, Object>> find(String companyName) {
        String normalized = normalize(companyName);
        if (normalized == null) return Optional.empty();
        GetItemResponse response = client.getItem(GetItemRequest.builder().tableName(table)
                .key(key(SK_PREFIX + normalized)).build());
        return response.hasItem() ? Optional.of(DynamoDbDataMapper.unwrapMap(response.item())) : Optional.empty();
    }

    @Override
    public void save(Map<String, Object> company) {
        String companyName = normalize(String.valueOf(company.get("companyName")));
        if (companyName == null) throw new IllegalArgumentException("companyName is required");
        client.putItem(PutItemRequest.builder().tableName(table).item(Map.of(
                "pk", AttributeValue.builder().s(PK).build(),
                "sk", AttributeValue.builder().s(SK_PREFIX + companyName).build(),
                "entityType", AttributeValue.builder().s(ENTITY_TYPE).build(),
                "data", AttributeValue.builder().m(wrapMap(company)).build())).build());
    }

    @Override
    public void delete(String companyName) {
        String normalized = normalize(companyName);
        if (normalized == null) return;
        client.deleteItem(DeleteItemRequest.builder().tableName(table).key(key(SK_PREFIX + normalized)).build());
    }

    private static Map<String, AttributeValue> key(String sk) {
        return Map.of("pk", AttributeValue.builder().s(PK).build(), "sk", AttributeValue.builder().s(sk).build());
    }
    private static Map<String, AttributeValue> wrapMap(Map<String, Object> source) {
        Map<String, AttributeValue> result = new LinkedHashMap<>();
        source.forEach((name, value) -> result.put(name, wrap(value)));
        return result;
    }
    private static AttributeValue wrap(Object value) {
        if (value == null) return AttributeValue.builder().nul(true).build();
        if (value instanceof String s) return AttributeValue.builder().s(s).build();
        if (value instanceof Number n) return AttributeValue.builder().n(n.toString()).build();
        if (value instanceof Boolean b) return AttributeValue.builder().bool(b).build();
        if (value instanceof Map<?, ?> map) {
            Map<String, Object> typed = new LinkedHashMap<>();
            map.forEach((k, v) -> typed.put(String.valueOf(k), v));
            return AttributeValue.builder().m(wrapMap(typed)).build();
        }
        if (value instanceof Collection<?> collection) return AttributeValue.builder().l(collection.stream().map(DynamoDbCompanyRepository::wrap).toList()).build();
        return AttributeValue.builder().s(String.valueOf(value)).build();
    }
    private static Map<String, AttributeValue> toAttributeKey(Map<String, String> key) {
        Map<String, AttributeValue> result = new LinkedHashMap<>();
        key.forEach((name, value) -> result.put(name, AttributeValue.builder().s(value).build()));
        return result;
    }
    private static Map<String, String> stringKey(Map<String, AttributeValue> key) {
        if (key == null || key.isEmpty()) return Map.of();
        Map<String, String> result = new LinkedHashMap<>();
        key.forEach((name, value) -> { if (value != null && value.s() != null) result.put(name, value.s()); });
        return Map.copyOf(result);
    }
    static String normalize(String value) {
        if (value == null || value.isBlank()) return null;
        String normalized = value.trim().toLowerCase(Locale.ROOT).replaceAll("\\s+", " ");
        return normalized.isBlank() ? null : normalized;
    }
}