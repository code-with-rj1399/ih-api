package ai.interviewhq.api.question.service;
import ai.interviewhq.api.common.exception.PublicApiException;
import ai.interviewhq.api.common.pagination.*;
import ai.interviewhq.api.common.response.ApiCollectionResponse;
import ai.interviewhq.api.question.dto.*;
import ai.interviewhq.api.question.mapper.QuestionMapper;
import ai.interviewhq.api.question.repository.*;
import ai.interviewhq.api.infrastructure.dynamodb.DynamoDbPage;
import org.springframework.http.HttpStatus; import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import java.util.*;

@Service
public class QuestionService {
 private final QuestionRepository repo; private final QuestionMapper mapper; private final OpaqueCursorCodec cursors;
 public QuestionService(QuestionRepository repo,QuestionMapper mapper,OpaqueCursorCodec cursors){this.repo=repo;this.mapper=mapper;this.cursors=cursors;}
 public QuestionResponse find(Integer id){return repo.findById(id).found()?mapper.toResponse(repo.findById(id).data()):throwNotFound();}
 public ApiCollectionResponse<QuestionSummaryResponse> list(Integer limit,String cursor,String company,String type,String sort){
   PaginationRequest p=PaginationRequest.of(limit,cursor); String normalizedCompany=normalize(company); String normalizedType=blank(type);
   validateSort(sort); boolean newest=!"oldest".equals(sort);
   String pk=normalizedCompany!=null?"QINDEX#COMPANY#"+normalizedCompany:normalizedType!=null?"QINDEX#TYPE#"+normalizedType:"QINDEX#EXTRACTED";
   String filter=null; Map<String,AttributeValue> values=Map.of();
   if(normalizedCompany!=null&&normalizedType!=null){filter="contains(data.questionTypes, :type)";values=Map.of(":type",AttributeValue.builder().s(normalizedType).build());}
   DynamoDbPage<QuestionListProjection> page=repo.list(pk,newest,p.limit(),cursors.decode(p.cursor()),filter,values);
   return collection(page,p.limit());
 }
 public ApiCollectionResponse<QuestionSummaryResponse> byExperience(Integer experienceId,Integer limit,String cursor){
   PaginationRequest p=PaginationRequest.of(limit,cursor); DynamoDbPage<QuestionListProjection> page=repo.listByExperience(experienceId,p.limit(),cursors.decode(p.cursor())); return collection(page,p.limit());
 }
 private ApiCollectionResponse<QuestionSummaryResponse> collection(DynamoDbPage<QuestionListProjection> page,int limit){return new ApiCollectionResponse<>(page.items().stream().map(mapper::toSummary).toList(),new ApiCollectionResponse.Pagination(limit,cursors.encode(page.lastEvaluatedKey()),page.hasMore()));}
 private static String blank(String s){if(s==null||s.isBlank())return null;return s.trim();}
 private static String normalize(String s){String v=blank(s);return v==null?null:v.toLowerCase(Locale.ROOT).replaceAll("\s+"," ");}
 private static void validateSort(String sort){if(sort!=null&&!sort.isBlank()&&!sort.equals("newest")&&!sort.equals("oldest"))throw new PublicApiException("INVALID_SORT","Unsupported sort value",HttpStatus.BAD_REQUEST);}
 private static <T>T throwNotFound(){throw new PublicApiException("NOT_FOUND","Interview question not found",HttpStatus.NOT_FOUND);}
}
