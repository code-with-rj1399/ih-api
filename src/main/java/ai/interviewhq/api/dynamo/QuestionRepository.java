package ai.interviewhq.api.dynamo;

import ai.interviewhq.api.domain.Keys;
import ai.interviewhq.api.domain.Question;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class QuestionRepository {

    private final DynamoOperations dynamo;

    public QuestionRepository(DynamoOperations dynamo) {
        this.dynamo = dynamo;
    }

    public Optional<Question> findById(String questionId) {
        return dynamo.get(Keys.questionPk(questionId), Keys.ENTITY).map(ItemMapper::toQuestion);
    }

    public boolean createIfAbsent(Question question) {
        return dynamo.putIfAbsent(ItemMapper.fromQuestion(question));
    }

    public void save(Question question) {
        dynamo.put(ItemMapper.fromQuestion(question));
    }

    public PageResult<Question> findRecent(int limit, String cursor) {
        return dynamo.queryGsi(Keys.GSI4, Keys.GSI4PK, Keys.gsi4pkEntityType(Keys.ENTITY_QUESTION),
                limit, cursor, false, ItemMapper::toQuestion);
    }

    public PageResult<Question> findByCompany(String company, int limit, String cursor) {
        return dynamo.queryGsi(Keys.GSI1, Keys.GSI1PK, Keys.gsi1pkCompany(company),
                limit, cursor, false, ItemMapper::toQuestion);
    }

    public PageResult<Question> findByType(String questionType, int limit, String cursor) {
        return dynamo.queryGsi(Keys.GSI2, Keys.GSI2PK, Keys.gsi2pkType(questionType),
                limit, cursor, false, ItemMapper::toQuestion);
    }

    public PageResult<Question> findBySource(String sourceName, int limit, String cursor) {
        return dynamo.queryGsi(Keys.GSI3, Keys.GSI3PK, Keys.gsi3pkSource(sourceName),
                limit, cursor, false, ItemMapper::toQuestion);
    }
}
