package ai.interviewhq.api.metadata.service;

import ai.interviewhq.api.common.pagination.OpaqueCursorCodec;
import ai.interviewhq.api.common.pagination.PaginationRequest;
import ai.interviewhq.api.common.response.ApiCollectionResponse;
import ai.interviewhq.api.metadata.dto.MetadataItemResponse;
import ai.interviewhq.api.metadata.repository.MetadataRepository;
import ai.interviewhq.api.infrastructure.dynamodb.DynamoDbPage;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class MetadataService {
    private static final String COMPANIES = "META#COMPANIES";
    private static final String QUESTION_TYPES = "META#QUESTION_TYPES";

    private final MetadataRepository repo;
    private final OpaqueCursorCodec cursors;

    public MetadataService(MetadataRepository repo, OpaqueCursorCodec cursors) {
        this.repo = repo;
        this.cursors = cursors;
    }

    public ApiCollectionResponse<MetadataItemResponse> companies(Integer limit, String cursor) {
        return list(COMPANIES, limit, cursor);
    }

    public ApiCollectionResponse<MetadataItemResponse> questionTypes(Integer limit, String cursor) {
        return list(QUESTION_TYPES, limit, cursor);
    }

    private ApiCollectionResponse<MetadataItemResponse> list(
            String partitionKey,
            Integer limit,
            String cursor) {
        PaginationRequest pageRequest = new PaginationRequest(limit == null ? 100 : limit, cursor);
        DynamoDbPage<Map<String, Object>> page = repo.list(
                partitionKey,
                pageRequest.limit(),
                cursors.decode(pageRequest.cursor()));

        return new ApiCollectionResponse<>(
                page.items().stream()
                        .map(item -> new MetadataItemResponse(
                                stringValue(item, "name"),
                                stringValue(item, "slug")))
                        .toList(),
                new ApiCollectionResponse.Pagination(
                        pageRequest.limit(),
                        cursors.encode(page.lastEvaluatedKey()),
                        page.hasMore()));
    }

    private static String stringValue(Map<String, Object> item, String key) {
        Object value = item.get(key);
        return value == null ? null : String.valueOf(value);
    }
}
