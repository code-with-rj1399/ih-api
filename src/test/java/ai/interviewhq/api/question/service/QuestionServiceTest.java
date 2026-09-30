package ai.interviewhq.api.question.service;

import ai.interviewhq.api.common.pagination.OpaqueCursorCodec;
import ai.interviewhq.api.question.mapper.QuestionMapper;
import ai.interviewhq.api.question.repository.*;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class QuestionServiceTest {
 @Test void companyFilterUsesCompanyProjection(){QuestionRepository repo=mock(QuestionRepository.class);when(repo.list(anyString(),anyBoolean(),anyInt(),any(),any(),any())).thenReturn(new ai.interviewhq.api.infrastructure.dynamodb.DynamoDbPage<>(List.of(),Map.of()));var s=new QuestionService(repo,new QuestionMapper(),new OpaqueCursorCodec(new com.fasterxml.jackson.databind.ObjectMapper()));s.list(null,null,"Acme",null,"newest");ArgumentCaptor<String> pk=ArgumentCaptor.forClass(String.class);verify(repo).list(pk.capture(),eq(true),eq(25),isNull(),isNull(),eq(Map.of()));assertEquals("QINDEX#COMPANY#acme",pk.getValue());}
 @Test void typeFilterUsesTypeProjection(){QuestionRepository repo=mock(QuestionRepository.class);when(repo.list(anyString(),anyBoolean(),anyInt(),any(),any(),any())).thenReturn(new ai.interviewhq.api.infrastructure.dynamodb.DynamoDbPage<>(List.of(),Map.of()));var s=new QuestionService(repo,new QuestionMapper(),new OpaqueCursorCodec(new com.fasterxml.jackson.databind.ObjectMapper()));s.list(null,null,null,"System Design","oldest");verify(repo).list(eq("QINDEX#TYPE#System Design"),eq(false),eq(25),isNull(),isNull(),eq(Map.of()));}
 @Test void companyAndTypeUsesBoundedFilter(){QuestionRepository repo=mock(QuestionRepository.class);when(repo.list(anyString(),anyBoolean(),anyInt(),any(),any(),any())).thenReturn(new ai.interviewhq.api.infrastructure.dynamodb.DynamoDbPage<>(List.of(),Map.of()));var s=new QuestionService(repo,new QuestionMapper(),new OpaqueCursorCodec(new com.fasterxml.jackson.databind.ObjectMapper()));s.list(null,null,"Acme","Coding","newest");ArgumentCaptor<String> filter=ArgumentCaptor.forClass(String.class);verify(repo).list(eq("QINDEX#COMPANY#acme"),eq(true),eq(25),isNull(),filter.capture(),anyMap());assertEquals("contains(data.questionTypes, :type)",filter.getValue());}
}
