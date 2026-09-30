package ai.interviewhq.api.experience.service;

import ai.interviewhq.api.common.exception.PublicApiException;
import ai.interviewhq.api.common.pagination.OpaqueCursorCodec;
import ai.interviewhq.api.common.pagination.PaginationRequest;
import ai.interviewhq.api.common.response.ApiCollectionResponse;
import ai.interviewhq.api.experience.dto.ExperienceResponse;
import ai.interviewhq.api.experience.dto.ExperienceSummaryResponse;
import ai.interviewhq.api.experience.mapper.ExperienceMapper;
import ai.interviewhq.api.experience.repository.ExperienceRepository;
import ai.interviewhq.api.infrastructure.dynamodb.DynamoDbPage;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.Map;

@Service
public class ExperienceService {
    private final ExperienceRepository repo;
    private final ExperienceMapper mapper;
    private final OpaqueCursorCodec cursors;

    public ExperienceService(
            ExperienceRepository repo,
            ExperienceMapper mapper,
            OpaqueCursorCodec cursors) {
        this.repo = repo;
        this.mapper = mapper;
        this.cursors = cursors;
    }

    public ExperienceResponse find(Integer id) {
        return repo.findById(id)
                .map(mapper::toResponse)
                .orElseThrow(() -> new PublicApiException(
                        "NOT_FOUND",
                        "Interview experience not found",
                        HttpStatus.NOT_FOUND));
    }

    public ApiCollectionResponse<ExperienceSummaryResponse> list(
            Integer limit, String cursor, String sort) {
        PaginationRequest pageRequest = PaginationRequest.of(limit, cursor);
        validateSort(sort);

        boolean scanForward = "oldest".equals(sort);
        DynamoDbPage<Map<String, Object>> page = repo.list(
                "EINDEX#POSTED",
                scanForward,
                pageRequest.limit(),
                cursors.decode(pageRequest.cursor()));

        return collection(page, pageRequest.limit());
    }

    private ApiCollectionResponse<ExperienceSummaryResponse> collection(
            DynamoDbPage<Map<String, Object>> page,
            int limit) {
        return new ApiCollectionResponse<>(
                page.items().stream().map(mapper::toSummary).toList(),
                new ApiCollectionResponse.Pagination(
                        limit,
                        cursors.encode(page.lastEvaluatedKey()),
                        page.hasMore()));
    }

    private static void validateSort(String sort) {
        if (sort != null && !sort.isBlank()
                && !sort.equals("newest") && !sort.equals("oldest")) {
            throw new PublicApiException(
                    "INVALID_SORT",
                    "Unsupported sort value",
                    HttpStatus.BAD_REQUEST);
        }
    }
}
