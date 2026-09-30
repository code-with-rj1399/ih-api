package ai.interviewhq.api.experience.repository;

import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.BatchGetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.BatchGetItemResponse;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.GetItemResponse;
import software.amazon.awssdk.services.dynamodb.model.QueryRequest;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;

class DynamoDbExperienceRepositoryTest {
    @Test
    void detailUsesCanonicalGetItem() {
        DynamoDbClient client = mock(DynamoDbClient.class);
        when(client.getItem(any(GetItemRequest.class))).thenReturn(
                GetItemResponse.builder()
                        .item(Map.of(
                                "pk", AttributeValue.builder().s("EXPERIENCE#4").build(),
                                "sk", AttributeValue.builder().s("ENTITY").build(),
                                "data", AttributeValue.builder().m(Map.of(
                                        "id", AttributeValue.builder().n("4").build())).build()))
                        .build());

        var result = new DynamoDbExperienceRepository(client, "table").findById(4);

        assertTrue(result.isPresent());
        var captor = org.mockito.ArgumentCaptor.forClass(GetItemRequest.class);
        verify(client).getItem(captor.capture());
        assertEquals("EXPERIENCE#4", captor.getValue().key().get("pk").s());
        assertEquals("ENTITY", captor.getValue().key().get("sk").s());
    }

    @Test
    void listByCrawlRunUsesQueryAndBatchGetWithCursor() {
        DynamoDbClient client = mock(DynamoDbClient.class);
        when(client.query(any(QueryRequest.class))).thenReturn(
                software.amazon.awssdk.services.dynamodb.model.QueryResponse.builder()
                        .items(List.of(
                                Map.of(
                                        "pk", AttributeValue.builder().s("EXPERIENCE_RUN#9").build(),
                                        "sk", AttributeValue.builder().s("EXPERIENCE#4").build(),
                                        "entityType", AttributeValue.builder().s("CrawlRunExperienceIndex").build())))
                        .lastEvaluatedKey(Map.of(
                                "pk", AttributeValue.builder().s("EXPERIENCE_RUN#9").build(),
                                "sk", AttributeValue.builder().s("EXPERIENCE#4").build()))
                        .build());

        when(client.batchGetItem(any(BatchGetItemRequest.class))).thenReturn(
                BatchGetItemResponse.builder()
                        .responses(Map.of("table", List.of(
                                Map.of(
                                        "pk", AttributeValue.builder().s("EXPERIENCE#4").build(),
                                        "sk", AttributeValue.builder().s("ENTITY").build(),
                                        "data", AttributeValue.builder().m(Map.of(
                                                "id", AttributeValue.builder().n("4").build(),
                                                "title", AttributeValue.builder().s("SDE Interview").build())).build()))))
                        .build());

        var result = new DynamoDbExperienceRepository(client, "table")
                .listByCrawlRun(9, 25, Map.of());

        assertEquals(1, result.items().size());
        assertEquals("4", result.items().getFirst().get("id"));
        assertEquals("EXPERIENCE#4", result.lastEvaluatedKey().get("sk"));

        var captor = org.mockito.ArgumentCaptor.forClass(QueryRequest.class);
        verify(client).query(captor.capture());
        assertEquals("EXPERIENCE_RUN#9", captor.getValue().expressionAttributeValues().get(":pk").s());
        assertEquals("EXPERIENCE#", captor.getValue().expressionAttributeValues().get(":sk").s());
        assertEquals(25, captor.getValue().limit());
        assertTrue(!captor.getValue().scanIndexForward());

        var batchCaptor = org.mockito.ArgumentCaptor.forClass(BatchGetItemRequest.class);
        verify(client).batchGetItem(batchCaptor.capture());
        assertEquals("EXPERIENCE#4",
                batchCaptor.getValue().requestItems().get("table").keys().getFirst().get("pk").s());
        verify(client, never()).scan(any());
    }

    @Test
    void nullCrawlRunDoesNotQuery() {
        DynamoDbClient client = mock(DynamoDbClient.class);

        var result = new DynamoDbExperienceRepository(client, "table")
                .listByCrawlRun(null, 25, Map.of());

        assertTrue(result.items().isEmpty());
        verify(client, never()).query(any(QueryRequest.class));
        verify(client, never()).batchGetItem(any(BatchGetItemRequest.class));
    }
}
