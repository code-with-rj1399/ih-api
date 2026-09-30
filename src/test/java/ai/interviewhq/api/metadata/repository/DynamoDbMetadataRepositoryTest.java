package ai.interviewhq.api.metadata.repository;

import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.QueryRequest;
import software.amazon.awssdk.services.dynamodb.model.QueryResponse;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class DynamoDbMetadataRepositoryTest {
    @Test
    void companiesUseBoundedQueryAndCursor() {
        DynamoDbClient client = mock(DynamoDbClient.class);
        when(client.query(any(QueryRequest.class))).thenReturn(
                QueryResponse.builder()
                        .items(List.of(Map.of(
                                "pk", AttributeValue.builder().s("META#COMPANIES").build(),
                                "sk", AttributeValue.builder().s("COMPANY#acme").build(),
                                "entityType", AttributeValue.builder().s("CompanyMetadata").build(),
                                "data", AttributeValue.builder().m(Map.of(
                                        "name", AttributeValue.builder().s("Acme").build(),
                                        "slug", AttributeValue.builder().s("acme").build())).build())))
                        .lastEvaluatedKey(Map.of(
                                "pk", AttributeValue.builder().s("META#COMPANIES").build(),
                                "sk", AttributeValue.builder().s("COMPANY#acme").build()))
                        .build());

        var result = new DynamoDbMetadataRepository(client, "table")
                .list("META#COMPANIES", 100,
                        Map.of("pk", "META#COMPANIES", "sk", "COMPANY#abc"));

        assertEquals("Acme", result.items().get(0).get("name"));
        assertEquals("META#COMPANIES", result.lastEvaluatedKey().get("pk"));

        var captor = org.mockito.ArgumentCaptor.forClass(QueryRequest.class);
        verify(client).query(captor.capture());
        assertEquals("META#COMPANIES",
                captor.getValue().expressionAttributeValues().get(":pk").s());
        assertEquals(100, captor.getValue().limit());
        assertTrue(captor.getValue().scanIndexForward());
        verify(client, never()).scan(any(software.amazon.awssdk.services.dynamodb.model.ScanRequest.class));
    }
}
