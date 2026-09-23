package ai.interviewhq.api.web.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public final class IngestDtos {

    private IngestDtos() {
    }

    public record IngestRequest(
            @NotNull @Valid Source source,
            @NotNull @Valid ExperiencePayload experience,
            @NotEmpty @Valid List<QuestionPayload> questions
    ) {
    }

    public record Source(
            @NotBlank String name,
            @NotBlank String url
    ) {
    }

    public record ExperiencePayload(
            String title,
            Instant postedAt,
            String author,
            String company,
            String contentHash
    ) {
    }

    public record QuestionPayload(
            @NotBlank String questionText,
            @NotBlank String questionDescription,
            @NotBlank String questionType,
            List<String> topics,
            String company,
            String problemUrl,
            Double confidence,
            Double questionSpecificity
    ) {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record IngestResponse(
            String experienceId,
            boolean experienceCreated,
            List<IngestedQuestion> questions,
            List<RejectedQuestion> rejected
    ) {
        public IngestResponse {
            questions = questions == null ? List.of() : List.copyOf(questions);
            rejected = rejected == null ? List.of() : List.copyOf(rejected);
        }
    }

    public record IngestedQuestion(String questionId, String status) {
    }

    public record RejectedQuestion(String questionText, List<String> errors) {
        public RejectedQuestion {
            errors = errors == null ? List.of() : List.copyOf(errors);
        }
    }

    public static List<String> validateQuestion(QuestionPayload payload) {
        List<String> errors = new ArrayList<>();
        if (payload == null) {
            errors.add("question is required");
            return errors;
        }
        if (payload.questionText() == null || payload.questionText().isBlank()) {
            errors.add("questionText is required");
        }
        if (payload.questionDescription() == null || payload.questionDescription().isBlank()) {
            errors.add("questionDescription is required");
        }
        if (payload.questionType() == null || payload.questionType().isBlank()) {
            errors.add("questionType is required");
        }
        if (payload.confidence() != null && (payload.confidence() < 0 || payload.confidence() > 1)) {
            errors.add("confidence must be between 0 and 1");
        }
        if (payload.questionSpecificity() != null
                && (payload.questionSpecificity() < 0 || payload.questionSpecificity() > 1)) {
            errors.add("questionSpecificity must be between 0 and 1");
        }
        return errors;
    }
}
