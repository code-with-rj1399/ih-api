package ai.interviewhq.api.question.mapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
@Configuration
public class QuestionMapperConfig { @Bean QuestionMapper questionMapper(){return new QuestionMapper();} }
