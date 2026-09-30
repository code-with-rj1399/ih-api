package ai.interviewhq.api.question.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.GetItemResponse;
import software.amazon.awssdk.services.dynamodb.model.QueryRequest;
import software.amazon.awssdk.services.dynamodb.model.QueryResponse;
import software.amazon.awssdk.services.dynamodb.model.ScanRequest;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class DynamoDbQuestionRepositoryTest {

    @Test
    void detailUsesIdLookupThenCanonicalItem() {
        DynamoDbClient client = mock(DynamoDbClient.class);
        when(client.getItem(any(GetItemRequest.class)))
                .thenReturn(GetItemResponse.builder()
                        .item(Map.of(
                                "data",
                                AttributeValue.builder().m(Map.of(
                                        "experienceId", AttributeValue.builder().n("4").build(),
                                        "dedupeHash", AttributeValue.builder().s("abc").build()))
                                        .build()))
                        .build())
                .thenReturn(GetItemResponse.builder()
                        .item(Map.of(
                                "data",
                                AttributeValue.builder().m(Map.of(
                                        "id", AttributeValue.builder().n("7").build(),
                                        "questionText", AttributeValue.builder().s("Q").build()))
                                        .build()))
                        .build());

        var result = new DynamoDbQuestionRepository(client, new ObjectMapper(), "table").findById(7);

        assertTrue(result.found());
        assertEquals("Q", result.data().get("questionText"));

        var captor = org.mockito.ArgumentCaptor.forClass(GetItemRequest.class);
        verify(client, times(2)).getItem(captor.capture());

        assertEquals("QUESTION_ID#7", captor.getAllValues().get(0).key().get("pk").s());
        assertEquals("LOOKUP", captor.getAllValues().get(0).key().get("sk").s());
        assertEquals("EXPERIENCE#4", captor.getAllValues().get(1).key().get("pk").s());
        assertEquals("QUESTION#abc", captor.getAllValues().get(1).key().get("sk").s());
    }

    @Test
    void missingLookupReturnsNotFoundWithoutCanonicalRead() {
        DynamoDbClient client = mock(DynamoDbClient.class);
        when(client.getItem(any(GetItemRequest.class)))
                .thenReturn(GetItemResponse.builder().build());

        var result = new DynamoDbQuestionRepository(client, new ObjectMapper(), "table").findById(7);

        assertFalse(result.found());
        verify(client, times(1)).getItem(any(GetItemRequest.class));
    }

    @Test
    void experienceListUsesQueryNotScanAndPropagatesCursor() {
        DynamoDbClient client = mock(DynamoDbClient.class);
        when(client.query(any(QueryRequest.class)))
                .thenReturn(QueryResponse.builder()
                        .items(List.of())
                        .lastEvaluatedKey(Map.of(
                                "pk", AttributeValue.builder().s("EXPERIENCE#4").build(),
                                "sk", AttributeValue.builder().s("QUESTION#abc").build()))
                        .build());

        var repository = new DynamoDbQuestionRepository(client, new ObjectMapper(), "table");
        var page = repository.listByExperience(
                4,
                25,
                Map.of("pk", "EXPERIENCE#4", "sk", "QUESTION#prev"));

        var captor = org.mockito.ArgumentCaptor.forClass(QueryRequest.class);
        verify(client).query(captor.capture());

        QueryRequest request = captor.getValue();
        assertEquals("EXPERIENCE#4", request.expressionAttributeValues().get(":pk").s());
        assertEquals("QUESTION#", request.expressionAttributeValues().get(":sk").s());
        assertEquals(25, request.limit());
        assertTrue(request.scanIndexForward());
        assertEquals("EXPERIENCE#4", request.exclusiveStartKey().get("pk").s());
        assertEquals("QUESTION#prev", request.exclusiveStartKey().get("sk").s());
        assertTrue(page.hasMore());
        assertEquals("QUESTION#abc", page.lastEvaluatedKey().get("sk"));

        verify(client, never()).scan(any(ScanRequest.class));
    }

    @Test
    void listUsesQueryWithPartitionAndSortDirection() {
        DynamoDbClient client = mock(DynamoDbClient.class);
        when(client.query(any(QueryRequest.class)))
                .thenReturn(QueryResponse.builder().items(List.of()).build());

        var repository = new DynamoDbQuestionRepository(client, new ObjectMapper(), "table");
        repository.list("QUESTIONS#RECENT", false, 25, Map.of(), null, null);

        var captor = org.mockito.ArgumentCaptor.forClass(QueryRequest.class);
        verify(client).query(captor.capture());

        QueryRequest request = captor.getValue();
        assertEquals("QUESTIONS#RECENT", request.expressionAttributeValues().get(":pk").s());
        assertFalse(request.scanIndexForward());
        assertEquals(25, request.limit());
        verify(client, never()).scan(any(ScanRequest.class));
    }
}
