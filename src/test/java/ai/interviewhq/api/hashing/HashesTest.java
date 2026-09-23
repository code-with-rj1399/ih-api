package ai.interviewhq.api.hashing;

import ai.interviewhq.api.domain.QuestionType;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class HashesTest {

    @Test
    void questionIdIsStableAcrossWhitespaceAndCase() {
        String a = Hashes.questionId("Amazon", "Design a notification system.", QuestionType.SYSTEM_DESIGN);
        String b = Hashes.questionId(" amazon ", "  Design   a Notification System. ", QuestionType.SYSTEM_DESIGN);
        assertThat(a).isEqualTo(b).hasSize(64);
    }

    @Test
    void differentCompaniesProduceDifferentQuestionIds() {
        String amazon = Hashes.questionId("Amazon", "Design a notification system.", QuestionType.SYSTEM_DESIGN);
        String google = Hashes.questionId("Google", "Design a notification system.", QuestionType.SYSTEM_DESIGN);
        assertThat(amazon).isNotEqualTo(google);
    }

    @Test
    void dsaAliasMapsToCodingCanonicalForHashing() {
        QuestionType coding = QuestionType.fromCanonical("DSA");
        assertThat(coding).isEqualTo(QuestionType.CODING);
        String fromDsa = Hashes.questionId("Meta", "Two sum", coding);
        String fromCoding = Hashes.questionId("Meta", "Two sum", QuestionType.CODING);
        assertThat(fromDsa).isEqualTo(fromCoding);
    }

    @Test
    void urlHashIgnoresTrailingSlashAndFragment() {
        String a = Hashes.urlHash("https://LeetCode.com/discuss/interview-experience/123/");
        String b = Hashes.urlHash("https://leetcode.com/discuss/interview-experience/123#comments");
        assertThat(a).isEqualTo(b);
    }
}
