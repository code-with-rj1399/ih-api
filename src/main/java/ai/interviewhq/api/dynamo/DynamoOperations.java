package ai.interviewhq.api.dynamo;

import ai.interviewhq.api.config.DynamoDbProperties;
import ai.interviewhq.api.domain.Keys;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.ConditionalCheckFailedException;
import software.amazon.awssdk.services.dynamodb.model.DeleteItemRequest;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.QueryRequest;
import software.amazon.awssdk.services.dynamodb.model.QueryResponse;
import software.amazon.awssdk.services.dynamodb.model.ReturnValue;
import software.amazon.awssdk.services.dynamodb.model.UpdateItemRequest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

@Component
public class DynamoOperations {

    private final DynamoDbClient client;
    private final String tableName;

    public DynamoOperations(DynamoDbClient client, DynamoDbProperties properties) {
        this.client = client;
        this.tableName = properties.getTable();
    }

    public String tableName() {
        return tableName;
    }

    public DynamoDbClient client() {
        return client;
    }

    public void put(Map<String, AttributeValue> item) {
        client.putItem(PutItemRequest.builder().tableName(tableName).item(item).build());
    }

    /**
     * Idempotent create. Returns {@code true} when the item was written, {@code false}
     * when it already existed.
     */
    public boolean putIfAbsent(Map<String, AttributeValue> item) {
        try {
            client.putItem(PutItemRequest.builder()
                    .tableName(tableName)
                    .item(item)
                    .conditionExpression("attribute_not_exists(#pk)")
                    .expressionAttributeNames(Map.of("#pk", Keys.PK))
                    .build());
            return true;
        } catch (ConditionalCheckFailedException ex) {
            return false;
        }
    }

    public Optional<Map<String, AttributeValue>> get(String pk, String sk) {
        var response = client.getItem(GetItemRequest.builder()
                .tableName(tableName)
                .key(Map.of(Keys.PK, Attrs.s(pk), Keys.SK, Attrs.s(sk)))
                .build());
        if (!response.hasItem() || response.item().isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(response.item());
    }

    public void delete(String pk, String sk) {
        client.deleteItem(DeleteItemRequest.builder()
                .tableName(tableName)
                .key(Map.of(Keys.PK, Attrs.s(pk), Keys.SK, Attrs.s(sk)))
                .build());
    }

    public Map<String, AttributeValue> update(String pk, String sk, String updateExpression,
                                              Map<String, String> names, Map<String, AttributeValue> values) {
        var response = client.updateItem(UpdateItemRequest.builder()
                .tableName(tableName)
                .key(Map.of(Keys.PK, Attrs.s(pk), Keys.SK, Attrs.s(sk)))
                .updateExpression(updateExpression)
                .expressionAttributeNames(names)
                .expressionAttributeValues(values)
                .returnValues(ReturnValue.ALL_NEW)
                .build());
        return response.attributes();
    }

    public <T> PageResult<T> queryByPk(String pk, String skPrefix, int limit, String cursor, boolean forward,
                                       Function<Map<String, AttributeValue>, T> mapper) {
        Map<String, AttributeValue> values = new HashMap<>();
        values.put(":pk", Attrs.s(pk));
        String condition = "PK = :pk";
        if (skPrefix != null && !skPrefix.isBlank()) {
            values.put(":sk", Attrs.s(skPrefix));
            condition = "PK = :pk AND begins_with(SK, :sk)";
        }
        QueryRequest.Builder builder = QueryRequest.builder()
                .tableName(tableName)
                .keyConditionExpression(condition)
                .expressionAttributeValues(values)
                .scanIndexForward(forward)
                .limit(limit);
        Map<String, AttributeValue> start = CursorCodec.decode(cursor);
        if (start != null) {
            builder.exclusiveStartKey(start);
        }
        QueryResponse response = client.query(builder.build());
        List<T> items = response.items().stream().map(mapper).toList();
        return new PageResult<>(items, CursorCodec.encode(response.lastEvaluatedKey()));
    }

    public <T> PageResult<T> queryGsi(String indexName, String pkAttr, String pkValue, int limit, String cursor,
                                      boolean forward, Function<Map<String, AttributeValue>, T> mapper) {
        QueryRequest.Builder builder = QueryRequest.builder()
                .tableName(tableName)
                .indexName(indexName)
                .keyConditionExpression("#pk = :pk")
                .expressionAttributeNames(Map.of("#pk", pkAttr))
                .expressionAttributeValues(Map.of(":pk", Attrs.s(pkValue)))
                .scanIndexForward(forward)
                .limit(limit);
        Map<String, AttributeValue> start = CursorCodec.decode(cursor);
        if (start != null) {
            builder.exclusiveStartKey(start);
        }
        QueryResponse response = client.query(builder.build());
        List<T> items = response.items().stream().map(mapper).toList();
        return new PageResult<>(items, CursorCodec.encode(response.lastEvaluatedKey()));
    }
}
