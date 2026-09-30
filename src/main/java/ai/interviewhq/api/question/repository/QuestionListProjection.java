package ai.interviewhq.api.question.repository;
import java.time.Instant;
import java.util.List;
public record QuestionListProjection(Integer id,String questionText,List<String> questionTypes,Float confidence,String company,String role,String sourcePlatform,Integer seedId,String problemUrl,String originalPostUrl,Instant extractedAt,Instant postedAt,Integer experienceId,Integer crawlRunId) {}
