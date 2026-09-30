package ai.interviewhq.api.experience.repository;

import ai.interviewhq.api.infrastructure.dynamodb.DynamoDbPage;
import java.util.Map;
import java.util.Optional;

public interface ExperienceRepository {
    Optional<Map<String, Object>> findById(Integer id);

    DynamoDbPage<Map<String, Object>> list(
            String partitionKey,
            boolean scanForward,
            int limit,
            Map<String, String> startKey);

    DynamoDbPage<Map<String, Object>> listByCrawlRun(
            Integer crawlRunId,
            int limit,
            Map<String, String> startKey);

    int mergeCompany(String sourceCompany, String targetCompany);
}
