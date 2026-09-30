package ai.interviewhq.api.question.repository;

import ai.interviewhq.api.infrastructure.dynamodb.DynamoDbDataMapper;
import ai.interviewhq.api.infrastructure.dynamodb.DynamoDbPage;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class DynamoDbQuestionRepository implements QuestionRepository {
    private static final Logger log = LoggerFactory.getLogger(DynamoDbQuestionRepository.class);
    private static final String QUESTION_ID_LOOKUP_SK = "ENTITY";

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
        if (id == null) return new QuestionResult(Map.of());

        GetItemResponse lookup;
        try {
            lookup = client.getItem(GetItemRequest.builder()
                    .tableName(table)
                    .key(key("QUESTION_ID#" + id, QUESTION_ID_LOOKUP_SK))
                    .build());
        } catch (RuntimeException ex) {
            log.warn("dynamodb_get_failed operation=question_id_lookup");
            throw ex;
        }

        if (!lookup.hasItem()) return new QuestionResult(Map.of());

        QuestionIdLookup ref = mapper.convertValue(
                DynamoDbDataMapper.unwrapMap(lookup.item()), QuestionIdLookup.class);
        if (ref.experienceId() == null
                || ref.dedupeHash() == null
                || ref.dedupeHash().isBlank()) {
            return new QuestionResult(Map.of());
        }

        try {
            GetItemResponse canonical = client.getItem(GetItemRequest.builder()
                    .tableName(table)
                    .key(key(
                            "EXPERIENCE#" + ref.experienceId(),
                            "QUESTION#" + ref.dedupeHash()))
                    .build());
            return canonical.hasItem()
                    ? new QuestionResult(DynamoDbDataMapper.unwrapMap(canonical.item()))
                    : new QuestionResult(Map.of());
        } catch (RuntimeException ex) {
            log.warn("dynamodb_get_failed operation=question_canonical_lookup");
            throw ex;
        }
    }

    @Override
    public DynamoDbPage<QuestionListProjection> list(
            String partitionKey,
            boolean scanForward,
            int limit,
            Map<String, String> startKey,
            String filterExpression,
            Map<String, AttributeValue> filterValues) {
        Map<String, AttributeValue> values = new HashMap<>();
        values.put(":pk", AttributeValue.builder().s(partitionKey).build());
        if (filterExpression != null && !filterExpression.isBlank()) {
            values.putAll(filterValues == null ? Map.of() : filterValues);
        }

        QueryRequest.Builder request = QueryRequest.builder()
                .tableName(table)
                .keyConditionExpression("#pk = :pk")
                .expressionAttributeNames(Map.of("#pk", "pk"))
                .expressionAttributeValues(values)
                .limit(limit)
                .scanIndexForward(scanForward);

        if (startKey != null && !startKey.isEmpty()) {
            request.exclusiveStartKey(key(startKey));
        }
        if (filterExpression != null && !filterExpression.isBlank()) {
            request.filterExpression(filterExpression);
        }

        try {
            QueryResponse response = client.query(request.build());
            // QINDEX partitions are question projections. Do not require entityType
            // to be projected at the top level of the GSI item: some DynamoDB index
            // projections contain only pk/sk/data, which previously caused valid
            // question rows to be discarded after DynamoDB had already returned them.
            List<QuestionListProjection> items = response.items().stream()
                    .map(item -> mapper.convertValue(
                            DynamoDbDataMapper.unwrapMap(item), QuestionListProjection.class))
                    .toList();
            return new DynamoDbPage<>(items, stringKey(response.lastEvaluatedKey()));
        } catch (RuntimeException ex) {
            log.warn("dynamodb_query_failed operation=question_list");
            throw ex;
        }
    }

    @Override
    public DynamoDbPage<QuestionListProjection> listByExperience(
            Integer experienceId,
            int limit,
            Map<String, String> startKey) {
        if (experienceId == null) return new DynamoDbPage<>(List.of(), Map.of());

        QueryRequest.Builder request = QueryRequest.builder()
                .tableName(table)
                .keyConditionExpression("#pk = :pk AND begins_with(#sk, :sk)")
                .expressionAttributeNames(Map.of("#pk", "pk", "#sk", "sk"))
                .expressionAttributeValues(Map.of(
                        ":pk", AttributeValue.builder().s("EXPERIENCE#" + experienceId).build(),
                        ":sk", AttributeValue.builder().s("QUESTION#").build()))
                .limit(limit)
                .scanIndexForward(true);

        if (startKey != null && !startKey.isEmpty()) {
            request.exclusiveStartKey(key(startKey));
        }

        try {
            QueryResponse response = client.query(request.build());
            List<QuestionListProjection> items = response.items().stream()
                    .filter(this::isQuestionItem)
                    .map(item -> mapper.convertValue(
                            DynamoDbDataMapper.unwrapMap(item), QuestionListProjection.class))
                    .toList();
            return new DynamoDbPage<>(items, stringKey(response.lastEvaluatedKey()));
        } catch (RuntimeException ex) {
            log.warn("dynamodb_query_failed operation=experience_questions");
            throw ex;
        }
    }

    private boolean isQuestionItem(Map<String, AttributeValue> item) {
        AttributeValue entityType = item.get("entityType");
        return entityType != null && "InterviewQuestion".equals(entityType.s());
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

    private static Map<String, String> stringKey(Map<String, AttributeValue> key) {
        if (key == null || key.isEmpty()) return Map.of();
        Map<String, String> result = new HashMap<>();
        key.forEach((name, value) -> {
            if (value != null && value.s() != null) result.put(name, value.s());
        });
        return Map.copyOf(result);
    }
}
