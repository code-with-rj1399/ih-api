package ai.interviewhq.api.experience.dto;
import java.time.Instant;
public record ExperienceSummaryResponse(Integer id,String title,String company,String role,String level,String location,String sourcePlatform,Instant postedAt,String originalPostUrl,Integer questionCount) {}
