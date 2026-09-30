package ai.interviewhq.api.company.repository;

import ai.interviewhq.api.infrastructure.dynamodb.DynamoDbPage;
import java.util.Map;
import java.util.Optional;

public interface CompanyRepository {
    DynamoDbPage<Map<String, Object>> list(int limit, Map<String, String> startKey);
    Optional<Map<String, Object>> find(String companyName);
    void save(Map<String, Object> company);
    void delete(String companyName);
}
