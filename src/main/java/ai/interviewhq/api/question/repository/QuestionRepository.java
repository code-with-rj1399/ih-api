package ai.interviewhq.api.question.repository;
import ai.interviewhq.api.infrastructure.dynamodb.DynamoDbPage;
public interface QuestionRepository {
    QuestionResult findById(Integer id);
    DynamoDbPage<QuestionListProjection> list(String partitionKey,boolean scanForward,int limit,java.util.Map<String,String> startKey,String filterExpression,java.util.Map<String,software.amazon.awssdk.services.dynamodb.model.AttributeValue> filterValues);
    DynamoDbPage<QuestionListProjection> listByExperience(Integer experienceId,int limit,java.util.Map<String,String> startKey);
    record QuestionResult(java.util.Map<String,Object> data){ public boolean found(){return data!=null&&!data.isEmpty();} }
}
