package ai.interviewhq.api.infrastructure.dynamodb;
import java.util.List;
import java.util.Map;
public record DynamoDbPage<T>(List<T> items, Map<String,String> lastEvaluatedKey) {
    public boolean hasMore(){return lastEvaluatedKey!=null&&!lastEvaluatedKey.isEmpty();}
}
