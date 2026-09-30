package ai.interviewhq.api.question.mapper;
import ai.interviewhq.api.question.dto.QuestionResponse;
import ai.interviewhq.api.question.dto.QuestionSummaryResponse;
import ai.interviewhq.api.question.repository.QuestionListProjection;
import java.util.List;
public class QuestionMapper {
    public QuestionResponse toResponse(java.util.Map<String,Object> d){
        return new QuestionResponse(asInt(d,"id"),asInt(d,"experienceId"),asString(d,"problemUrl"),strings(d,"questionTypes"),asString(d,"difficulty"),asString(d,"questionText"),asString(d,"questionDescription"),asString(d,"candidateApproach"),asFloat(d,"confidence"),asString(d,"questionParticularity"),instant(d,"extractedAt"),instant(d,"createdAt"));
    }
    public QuestionSummaryResponse toSummary(QuestionListProjection p){return new QuestionSummaryResponse(p.id(),p.experienceId(),p.questionText(),p.questionTypes()==null?List.of():List.copyOf(p.questionTypes()),p.confidence(),p.problemUrl(),p.extractedAt(),p.postedAt(),p.company(),p.role(),p.sourcePlatform());}
    private static String asString(java.util.Map<String,Object>d,String k){Object v=d.get(k);return v==null?null:String.valueOf(v);}
    private static Integer asInt(java.util.Map<String,Object>d,String k){Object v=d.get(k);return v==null?null:Integer.valueOf(String.valueOf(v));}
    private static Float asFloat(java.util.Map<String,Object>d,String k){Object v=d.get(k);return v==null?null:Float.valueOf(String.valueOf(v));}
    private static java.time.Instant instant(java.util.Map<String,Object>d,String k){String v=asString(d,k);return v==null?null:java.time.Instant.parse(v);}
    private static List<String> strings(java.util.Map<String,Object>d,String k){Object v=d.get(k);if(!(v instanceof List<?> l))return List.of();return l.stream().map(String::valueOf).toList();}
}
