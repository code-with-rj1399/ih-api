package ai.interviewhq.api.experience.dto;
import java.time.Instant;
public record ExperienceResponse(Integer id,String sourcePlatform,String title,String summary,String author,Instant postedAt,String originalPostUrl,String company,String role,String level,String location,Float candidateYoE,Integer questionCount,Instant createdAt) {}
