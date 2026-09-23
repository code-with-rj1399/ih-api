package ai.interviewhq.api.dynamo;

import ai.interviewhq.api.domain.Experience;
import ai.interviewhq.api.domain.ExperienceQuestion;
import ai.interviewhq.api.domain.Keys;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class ExperienceRepository {

    private final DynamoOperations dynamo;

    public ExperienceRepository(DynamoOperations dynamo) {
        this.dynamo = dynamo;
    }

    public Optional<Experience> findById(String experienceId) {
        return dynamo.get(Keys.experiencePk(experienceId), Keys.ENTITY).map(ItemMapper::toExperience);
    }

    public boolean createIfAbsent(Experience experience) {
        return dynamo.putIfAbsent(ItemMapper.fromExperience(experience));
    }

    public void save(Experience experience) {
        dynamo.put(ItemMapper.fromExperience(experience));
    }

    public void linkQuestion(String experienceId, String questionId) {
        dynamo.putIfAbsent(ItemMapper.fromExperienceQuestion(new ExperienceQuestion(experienceId, questionId)));
    }

    public List<ExperienceQuestion> findQuestionLinks(String experienceId) {
        return dynamo.queryByPk(Keys.experiencePk(experienceId), "QUESTION#", 200, null, true,
                ItemMapper::toExperienceQuestion).getItems();
    }
}
