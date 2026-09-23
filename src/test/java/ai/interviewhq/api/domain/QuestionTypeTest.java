package ai.interviewhq.api.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class QuestionTypeTest {

    @Test
    void parsesCanonicalLabels() {
        assertThat(QuestionType.fromCanonical("System Design")).isEqualTo(QuestionType.SYSTEM_DESIGN);
        assertThat(QuestionType.fromCanonical("AI/ML")).isEqualTo(QuestionType.AI_ML);
        assertThat(QuestionType.fromCanonical("Web Frontend")).isEqualTo(QuestionType.WEB_FRONTEND);
    }

    @Test
    void mapsDsaFamilyToCoding() {
        assertThat(QuestionType.fromCanonical("DSA")).isEqualTo(QuestionType.CODING);
        assertThat(QuestionType.fromCanonical("leetcode")).isEqualTo(QuestionType.CODING);
        assertThat(QuestionType.fromCanonical("data-structures")).isEqualTo(QuestionType.CODING);
    }

    @Test
    void rejectsUnknownTypes() {
        assertThatThrownBy(() -> QuestionType.fromCanonical("Behavioral"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unknown questionType");
    }
}
