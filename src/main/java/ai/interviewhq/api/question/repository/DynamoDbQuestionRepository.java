package ai.interviewhq.api.question.repository;

import ai.interviewhq.api.infrastructure.dynamodb.DynamoDbDataMapper;
import ai.interviewhq.api.infrastructure.dynamodb.DynamoDbPage;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;
import java.util.*;

@Repository
public class DynamoDbQuestionRepository implements QuestionRepository {
    private final DynamoDbClient client; private final ObjectMapper mapper; private final String table;
    public DynamoDbQuestionRepository(DynamoDbClient client,ObjectMapper mapper,@Value("${aws.dynamodb.table}") String table){this.client=client;this.mapper=mapper;this.table=table;}

    @Override public QuestionResult findById(Integer id){
        if(id==null)return new QuestionResult(Map.of());
        GetItemResponse lookup=client.getItem(GetItemRequest.builder().tableName(table).key(key("QUESTION_ID#"+id,"LOOKUP")).build());
        if(!lookup.hasItem())return new QuestionResult(Map.of());
        QuestionIdLookup ref=mapper.convertValue(DynamoDbDataMapper.unwrapMap(lookup.item()),QuestionIdLookup.class);
        if(ref.experienceId()==null||ref.dedupeHash()==null||ref.dedupeHash().isBlank())return new QuestionResult(Map.of());
        GetItemResponse canonical=client.getItem(GetItemRequest.builder().tableName(table).key(key("EXPERIENCE#"+ref.experienceId(),"QUESTION#"+ref.dedupeHash())).build());
        return canonical.hasItem()?new QuestionResult(DynamoDbDataMapper.unwrapMap(canonical.item())):new QuestionResult(Map.of());
    }

    @Override public DynamoDbPage<QuestionListProjection> list(String partitionKey,boolean scanForward,int limit,Map<String,String> startKey,String filterExpression,Map<String,AttributeValue> filterValues){
        QueryRequest.Builder b=QueryRequest.builder().tableName(table).keyConditionExpression("#pk = :pk").expressionAttributeNames(Map.of("#pk","pk")).expressionAttributeValues(Map.of(":pk",AttributeValue.builder().s(partitionKey).build())).limit(limit).scanIndexForward(scanForward);
        if(startKey!=null&&!startKey.isEmpty())b.exclusiveStartKey(key(startKey));
        if(filterExpression!=null&&!filterExpression.isBlank())b.filterExpression(filterExpression).expressionAttributeValues(merge(b.build().expressionAttributeValues(),filterValues));
        QueryResponse response=client.query(b.build());
        List<QuestionListProjection> items=response.items().stream().map(i->mapper.convertValue(DynamoDbDataMapper.unwrapMap(i),QuestionListProjection.class)).toList();
        return new DynamoDbPage<>(items,stringKey(response.lastEvaluatedKey()));
    }

    @Override public DynamoDbPage<QuestionListProjection> listByExperience(Integer experienceId,int limit,Map<String,String> startKey){
        QueryRequest.Builder b=QueryRequest.builder().tableName(table).keyConditionExpression("#pk = :pk AND begins_with(#sk,:sk)").expressionAttributeNames(Map.of("#pk","pk","#sk","sk")).expressionAttributeValues(Map.of(":pk",AttributeValue.builder().s("EXPERIENCE#"+experienceId).build(),":sk",AttributeValue.builder().s("QUESTION#").build())).limit(limit).scanIndexForward(true);
        if(startKey!=null&&!startKey.isEmpty())b.exclusiveStartKey(key(startKey));
        QueryResponse response=client.query(b.build());
        List<QuestionListProjection> items=response.items().stream().filter(i->"InterviewQuestion".equals(i.getOrDefault("entityType",AttributeValue.builder().s("")).s())).map(i->mapper.convertValue(DynamoDbDataMapper.unwrapMap(i),QuestionListProjection.class)).toList();
        return new DynamoDbPage<>(items,stringKey(response.lastEvaluatedKey()));
    }

    private static Map<String,AttributeValue> key(String pk,String sk){return Map.of("pk",AttributeValue.builder().s(pk).build(),"sk",AttributeValue.builder().s(sk).build());}
    private static Map<String,AttributeValue> key(Map<String,String> k){Map<String,AttributeValue> out=new HashMap<>();k.forEach((n,v)->out.put(n,AttributeValue.builder().s(v).build()));return out;}
    private static Map<String,String> stringKey(Map<String,AttributeValue> k){if(k==null||k.isEmpty())return Map.of();Map<String,String> out=new HashMap<>();k.forEach((n,v)->{if(v.s()!=null)out.put(n,v.s());});return Map.copyOf(out);}
    private static Map<String,AttributeValue> merge(Map<String,AttributeValue>a,Map<String,AttributeValue>b){Map<String,AttributeValue>m=new HashMap<>(a);if(b!=null)m.putAll(b);return Map.copyOf(m);}
}
