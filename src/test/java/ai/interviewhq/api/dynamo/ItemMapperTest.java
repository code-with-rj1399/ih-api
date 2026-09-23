package ai.interviewhq.api.dynamo;

import ai.interviewhq.api.domain.Keys;
import ai.interviewhq.api.domain.Question;
import ai.interviewhq.api.domain.QuestionType;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ItemMapperTest {

    @Test
    void questionRoundTripIncludesGsiKeys() {
        Question question = new Question();
        question.setQuestionId("abc123");
        question.setQuestionText("Design a notification system.");
        question.setQuestionDescription("Design a notification system that can deliver notifications reliably.");
        question.setQuestionType(QuestionType.SYSTEM_DESIGN);
        question.setTopics(List.of("Notifications", "Distributed Systems"));
        question.setCompany("Amazon");
        question.setSourceName("LeetCode");
        question.setSourceUrl("https://example.com/123");
        question.setPostedAt(Instant.parse("2026-09-23T10:20:00Z"));
        question.setCreatedAt(Instant.parse("2026-09-23T12:00:00Z"));
        question.setConfidence(0.92);
        question.setQuestionSpecificity(0.88);

        Map<String, AttributeValue> item = ItemMapper.fromQuestion(question);

        assertThat(item.get(Keys.PK).s()).isEqualTo("QUESTION#abc123");
        assertThat(item.get(Keys.SK).s()).isEqualTo("ENTITY");
        assertThat(item.get(Keys.GSI1PK).s()).isEqualTo("COMPANY#Amazon");
        assertThat(item.get(Keys.GSI2PK).s()).isEqualTo("TYPE#System Design");
        assertThat(item.get(Keys.GSI3PK).s()).isEqualTo("SOURCE#LeetCode");
        assertThat(item.get(Keys.GSI4PK).s()).isEqualTo("ENTITY_TYPE#Question");
        assertThat(item.get("entityType").s()).isEqualTo("Question");

        Question restored = ItemMapper.toQuestion(item);
        assertThat(restored.getQuestionText()).isEqualTo(question.getQuestionText());
        assertThat(restored.getQuestionType()).isEqualTo(QuestionType.SYSTEM_DESIGN);
        assertThat(restored.getTopics()).containsExactly("Notifications", "Distributed Systems");
        assertThat(restored.getConfidence()).isEqualTo(0.92);
    }
}
