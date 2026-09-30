package ai.interviewhq.api.experience.mapper;
import ai.interviewhq.api.experience.dto.*;
import java.time.Instant; import java.util.Map;
public class ExperienceMapper {
 public ExperienceResponse toResponse(Map<String,Object>d){return new ExperienceResponse(i(d,"id"),s(d,"sourcePlatform"),s(d,"title"),s(d,"summary"),s(d,"author"),t(d,"postedAt"),s(d,"originalPostUrl"),s(d,"company"),s(d,"role"),s(d,"level"),s(d,"location"),f(d,"candidateYoE"),i(d,"questionCount"),t(d,"createdAt"));}
 public ExperienceSummaryResponse toSummary(Map<String,Object>d){return new ExperienceSummaryResponse(i(d,"id"),s(d,"title"),s(d,"company"),s(d,"role"),s(d,"level"),s(d,"location"),s(d,"sourcePlatform"),t(d,"postedAt"),s(d,"originalPostUrl"),i(d,"questionCount"));}
 private static String s(Map<String,Object>d,String k){Object v=d.get(k);return v==null?null:String.valueOf(v);} private static Integer i(Map<String,Object>d,String k){Object v=d.get(k);return v==null?null:Integer.valueOf(String.valueOf(v));} private static Float f(Map<String,Object>d,String k){Object v=d.get(k);return v==null?null:Float.valueOf(String.valueOf(v));} private static Instant t(Map<String,Object>d,String k){String v=s(d,k);return v==null?null:Instant.parse(v);}
}
