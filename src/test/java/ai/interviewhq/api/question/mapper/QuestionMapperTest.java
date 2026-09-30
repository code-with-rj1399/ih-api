package ai.interviewhq.api.question.mapper;
import ai.interviewhq.api.question.dto.QuestionResponse; import org.junit.jupiter.api.Test; import java.util.Map; import static org.junit.jupiter.api.Assertions.*;
class QuestionMapperTest {
 @Test void hidesUnsupportedPersistenceFields(){var d=Map.<String,Object>of("id","12","experienceId","4","questionText","Design a cache","questionTypes",java.util.List.of("System Design"),"confidence","0.92","modelName","internal","dedupeHash","secret","extractedAt","2026-09-30T08:00:00Z","createdAt","2026-09-30T08:00:00Z");QuestionResponse r=new QuestionMapper().toResponse(d);assertEquals(12,r.id());assertEquals("Design a cache",r.questionText());assertEquals(0.92f,r.confidence());}
 @Test void optionalFieldsRemainNull(){QuestionResponse r=new QuestionMapper().toResponse(Map.of("id","1","questionText","Q"));assertNull(r.problemUrl());assertNull(r.candidateApproach());assertNull(r.extractedAt());}
}
