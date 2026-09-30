package ai.interviewhq.api.question.dto;
import java.time.Instant;
import java.util.List;
public record QuestionSummaryResponse(Integer id,Integer experienceId,String questionText,List<String> questionTypes,Float confidence,String problemUrl,Instant extractedAt,Instant postedAt,String company,String role,String sourcePlatform) {}
