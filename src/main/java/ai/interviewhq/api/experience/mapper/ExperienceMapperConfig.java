package ai.interviewhq.api.experience.mapper;
import org.springframework.context.annotation.Bean; import org.springframework.context.annotation.Configuration;
@Configuration public class ExperienceMapperConfig { @Bean ExperienceMapper experienceMapper(){return new ExperienceMapper();} }
