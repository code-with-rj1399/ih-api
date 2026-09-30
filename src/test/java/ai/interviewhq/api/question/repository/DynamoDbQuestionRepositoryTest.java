package ai.interviewhq.api.question.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class DynamoDbQuestionRepositoryTest {
    @Test
    void detailUsesEntityLookupThenCanonicalItem() {
        DynamoDbClient client = mock(DynamoDbClient.class);
        when(client.getItem(any(GetItemRequest.class)))
                .thenReturn(GetItemResponse.builder().item(Map.of("data", AttributeValue.builder().m(Map.of(
                        "experienceId", AttributeValue.builder().n("4").build(),
                        "dedupeHash", AttributeValue.builder().s("abc").build())).build())).build())
                .thenReturn(GetItemResponse.builder().item(Map.of("data", AttributeValue.builder().m(Map.of(
                        "id", AttributeValue.builder().n("7").build(),
                        "questionText", AttributeValue.builder().s("Q").build())).build())).build());

        var result = new DynamoDbQuestionRepository(client, new ObjectMapper(), "table").findById(7);

        assertTrue(result.found());
        assertEquals("Q", result.data().get("questionText"));
        var captor = org.mockito.ArgumentCaptor.forClass(GetItemRequest.class);
        verify(client, times(2)).getItem(captor.capture());
        assertEquals("QUESTION_ID#7", captor.getAllValues().get(0).key().get("pk").s());
        assertEquals("ENTITY", captor.getAllValues().get(0).key().get("sk").s());
        assertEquals("EXPERIENCE#4", captor.getAllValues().get(1).key().get("pk").s());
        assertEquals("QUESTION#abc", captor.getAllValues().get(1).key().get("sk").s());
    }

    @Test
    void recentListUsesExtractedPartitionNewestFirstAndCursor() {
        DynamoDbClient client = mock(DynamoDbClient.class);
        when(client.query(any(QueryRequest.class))).thenReturn(
                QueryResponse.builder()
                        .items(List.of(
                                Map.of(
                                        "pk", AttributeValue.builder().s("QINDEX#EXTRACTED").build(),
                                        "sk", AttributeValue.builder().s("2026-09-30T10:00:00Z#7").build(),
                                        "entityType", AttributeValue.builder().s("InterviewQuestion").build(),
                                        "data", AttributeValue.builder().m(Map.of(
                                                "id", AttributeValue.builder().n("7").build(),
                                                "questionText", AttributeValue.builder().s("Q").build())).build())))
                        .lastEvaluatedKey(Map.of(
                                "pk", AttributeValue.builder().s("QINDEX#EXTRACTED").build(),
                                "sk", AttributeValue.builder().s("2026-09-30T10:00:00Z#7").build()))
                        .build());

        var result = new DynamoDbQuestionRepository(client, new ObjectMapper(), "table")
                .list("QINDEX#EXTRACTED", true, 25,
                        Map.of("pk", "QINDEX#EXTRACTED", "sk", "2026-09-30T11:00:00Z#8"),
                        null, Map.of());

        assertEquals(1, result.items().size());
        assertEquals("7", result.items().getFirst().id().toString());
        assertEquals("QINDEX#EXTRACTED", result.lastEvaluatedKey().get("pk"));

        var captor = org.mockito.ArgumentCaptor.forClass(QueryRequest.class);
        verify(client).query(captor.capture());
        QueryRequest request = captor.getValue();
        assertEquals("QINDEX#EXTRACTED", request.expressionAttributeValues().get(":pk").s());
        assertTrue(request.scanIndexForward());
        assertEquals(25, request.limit());
        assertEquals("QINDEX#EXTRACTED", request.exclusiveStartKey().get("pk").s());
        verify(client, never()).scan(any(ScanRequest.class));
    }

    @Test
    void experienceListUsesQueryNotScanAndReturnsCursor() {
        DynamoDbClient client = mock(DynamoDbClient.class);
        when(client.query(any(QueryRequest.class))).thenReturn(
                QueryResponse.builder()
                        .items(List.of())
                        .lastEvaluatedKey(Map.of(
                                "pk", AttributeValue.builder().s("EXPERIENCE#4").build(),
                                "sk", AttributeValue.builder().s("QUESTION#abc").build()))
                        .build());

        var result = new DynamoDbQuestionRepository(client, new ObjectMapper(), "table")
                .listByExperience(4, 25, Map.of());

        var captor = org.mockito.ArgumentCaptor.forClass(QueryRequest.class);
        verify(client).query(captor.capture());
        assertTrue(captor.getValue().keyConditionExpression().contains("begins_with"));
        assertEquals("EXPERIENCE#4", captor.getValue().expressionAttributeValues().get(":pk").s());
        assertEquals("QUESTION#", captor.getValue().expressionAttributeValues().get(":sk").s());
        assertEquals(Map.of("pk", "EXPERIENCE#4", "sk", "QUESTION#abc"), result.lastEvaluatedKey());
        verify(client, never()).scan(any(ScanRequest.class));
    }

    @Test
    void nullExperienceDoesNotQuery() {
        DynamoDbClient client = mock(DynamoDbClient.class);

        var result = new DynamoDbQuestionRepository(client, new ObjectMapper(), "table")
                .listByExperience(null, 25, Map.of());

        assertTrue(result.items().isEmpty());
        verify(client, never()).query(any(QueryRequest.class));
    }
}
