package ai.interviewhq.api.question.repository;

import ai.interviewhq.api.infrastructure.dynamodb.DynamoDbDataMapper;
import ai.interviewhq.api.infrastructure.dynamodb.DynamoDbPage;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.GetItemResponse;
import software.amazon.awssdk.services.dynamodb.model.QueryRequest;
import software.amazon.awssdk.services.dynamodb.model.QueryResponse;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class DynamoDbQuestionRepository implements QuestionRepository {

    private final DynamoDbClient client;
    private final ObjectMapper mapper;
    private final String table;

    public DynamoDbQuestionRepository(
            DynamoDbClient client,
            ObjectMapper mapper,
            @Value("${aws.dynamodb.table}") String table) {
        this.client = client;
        this.mapper = mapper;
        this.table = table;
    }

    @Override
    public QuestionResult findById(Integer id) {
        if (id == null) {
            return new QuestionResult(Map.of());
        }

        GetItemResponse lookup = client.getItem(
                GetItemRequest.builder()
                        .tableName(table)
                        .key(key("QUESTION_ID#" + id, "LOOKUP"))
                        .build());

        if (!lookup.hasItem()) {
            return new QuestionResult(Map.of());
        }

        QuestionIdLookup ref = mapper.convertValue(
                DynamoDbDataMapper.unwrapMap(lookup.item()),
                QuestionIdLookup.class);

        if (ref.experienceId() == null
                || ref.dedupeHash() == null
                || ref.dedupeHash().isBlank()) {
            return new QuestionResult(Map.of());
        }

        GetItemResponse canonical = client.getItem(
                GetItemRequest.builder()
                        .tableName(table)
                        .key(key(
                                "EXPERIENCE#" + ref.experienceId(),
                                "QUESTION#" + ref.dedupeHash()))
                        .build());

        return canonical.hasItem()
                ? new QuestionResult(DynamoDbDataMapper.unwrapMap(canonical.item()))
                : new QuestionResult(Map.of());
    }

    @Override
    public DynamoDbPage<QuestionListProjection> list(
            String partitionKey,
            boolean scanForward,
            int limit,
            Map<String, String> startKey,
            String filterExpression,
            Map<String, AttributeValue> filterValues) {

        Map<String, AttributeValue> expressionValues = new HashMap<>();
        expressionValues.put(
                ":pk",
                AttributeValue.builder().s(partitionKey).build());

        if (filterExpression != null && !filterExpression.isBlank()) {
            expressionValues.putAll(filterValues == null ? Map.of() : filterValues);
        }

        QueryRequest.Builder request = QueryRequest.builder()
                .tableName(table)
                .keyConditionExpression("#pk = :pk")
                .expressionAttributeNames(Map.of("#pk", "pk"))
                .expressionAttributeValues(expressionValues)
                .limit(limit)
                .scanIndexForward(scanForward);

        if (startKey != null && !startKey.isEmpty()) {
            request.exclusiveStartKey(key(startKey));
        }

        if (filterExpression != null && !filterExpression.isBlank()) {
            request.filterExpression(filterExpression);
        }

        QueryResponse response = client.query(request.build());

        List<QuestionListProjection> items = response.items().stream()
                .map(item -> mapper.convertValue(
                        DynamoDbDataMapper.unwrapMap(item),
                        QuestionListProjection.class))
                .toList();

        return new DynamoDbPage<>(items, stringKey(response.lastEvaluatedKey()));
    }

    @Override
    public DynamoDbPage<QuestionListProjection> listByExperience(
            Integer experienceId,
            int limit,
            Map<String, String> startKey) {

        QueryRequest.Builder request = QueryRequest.builder()
                .tableName(table)
                .keyConditionExpression("#pk = :pk AND begins_with(#sk, :sk)")
                .expressionAttributeNames(Map.of(
                        "#pk", "pk",
                        "#sk", "sk"))
                .expressionAttributeValues(Map.of(
                        ":pk",
                        AttributeValue.builder()
                                .s("EXPERIENCE#" + experienceId)
                                .build(),
                        ":sk",
                        AttributeValue.builder()
                                .s("QUESTION#")
                                .build()))
                .limit(limit)
                .scanIndexForward(true);

        if (startKey != null && !startKey.isEmpty()) {
            request.exclusiveStartKey(key(startKey));
        }

        QueryResponse response = client.query(request.build());

        List<QuestionListProjection> items = response.items().stream()
                .filter(item -> {
                    AttributeValue entityType = item.get("entityType");
                    return entityType != null
                            && "InterviewQuestion".equals(entityType.s());
                })
                .map(item -> mapper.convertValue(
                        DynamoDbDataMapper.unwrapMap(item),
                        QuestionListProjection.class))
                .toList();

        return new DynamoDbPage<>(items, stringKey(response.lastEvaluatedKey()));
    }

    private static Map<String, AttributeValue> key(String pk, String sk) {
        return Map.of(
                "pk", AttributeValue.builder().s(pk).build(),
                "sk", AttributeValue.builder().s(sk).build());
    }

    private static Map<String, AttributeValue> key(Map<String, String> key) {
        Map<String, AttributeValue> result = new HashMap<>();
        key.forEach((name, value) ->
                result.put(name, AttributeValue.builder().s(value).build()));
        return result;
    }

    private static Map<String, String> stringKey(
            Map<String, AttributeValue> key) {

        if (key == null || key.isEmpty()) {
            return Map.of();
        }

        Map<String, String> result = new HashMap<>();
        key.forEach((name, value) -> {
            if (value != null && value.s() != null) {
                result.put(name, value.s());
            }
        });

        return Map.copyOf(result);
    }
}
