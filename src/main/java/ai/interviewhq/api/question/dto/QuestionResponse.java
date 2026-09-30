package ai.interviewhq.api.question.dto;

import java.time.Instant;
import java.util.List;

public record QuestionResponse(
        Integer id,
        Integer experienceId,
        String problemUrl,
        List<String> questionTypes,
        String difficulty,
        String questionText,
        String questionDescription,
        String candidateApproach,
        Float confidence,
        String questionParticularity,
        Instant extractedAt,
        Instant createdAt) {
}
