package ai.interviewhq.api.experience.repository;
import ai.interviewhq.api.infrastructure.dynamodb.DynamoDbDataMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value; import org.springframework.stereotype.Repository;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient; import software.amazon.awssdk.services.dynamodb.model.*;
import java.util.Map; import java.util.Optional;
@Repository public class DynamoDbExperienceRepository implements ExperienceRepository {
 private final DynamoDbClient client; private final String table;
 public DynamoDbExperienceRepository(DynamoDbClient client,@Value("${aws.dynamodb.table}")String table){this.client=client;this.table=table;}
 public Optional<Map<String,Object>> findById(Integer id){if(id==null)return Optional.empty();GetItemResponse r=client.getItem(GetItemRequest.builder().tableName(table).key(Map.of("pk",AttributeValue.builder().s("EXPERIENCE#"+id).build(),"sk",AttributeValue.builder().s("ENTITY").build())).build());return r.hasItem()?Optional.of(DynamoDbDataMapper.unwrapMap(r.item())):Optional.empty();}
}
