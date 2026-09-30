package ai.interviewhq.api.question.mapper;

import ai.interviewhq.api.question.dto.QuestionResponse;
import ai.interviewhq.api.question.dto.QuestionSummaryResponse;
import ai.interviewhq.api.question.repository.QuestionListProjection;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public class QuestionMapper {

    public QuestionResponse toResponse(Map<String, Object> data) {
        return new QuestionResponse(
                asInteger(data, "id"),
                asInteger(data, "experienceId"),
                asString(data, "problemUrl"),
                asStrings(data, "questionTypes"),
                asString(data, "difficulty"),
                asString(data, "questionText"),
                asString(data, "questionDescription"),
                asString(data, "candidateApproach"),
                asFloat(data, "confidence"),
                asString(data, "questionParticularity"),
                asInstant(data, "extractedAt"),
                asInstant(data, "createdAt"));
    }

    public QuestionSummaryResponse toSummary(QuestionListProjection projection) {
        return new QuestionSummaryResponse(
                projection.id(),
                projection.experienceId(),
                projection.questionText(),
                projection.questionTypes() == null
                        ? List.of()
                        : List.copyOf(projection.questionTypes()),
                projection.confidence(),
                projection.problemUrl(),
                projection.extractedAt(),
                projection.postedAt(),
                projection.company(),
                projection.role(),
                projection.sourcePlatform());
    }

    private static String asString(Map<String, Object> data, String key) {
        Object value = data.get(key);
        return value == null ? null : String.valueOf(value);
    }

    private static Integer asInteger(Map<String, Object> data, String key) {
        Object value = data.get(key);
        return value == null ? null : Integer.valueOf(String.valueOf(value));
    }

    private static Float asFloat(Map<String, Object> data, String key) {
        Object value = data.get(key);
        return value == null ? null : Float.valueOf(String.valueOf(value));
    }

    private static Instant asInstant(Map<String, Object> data, String key) {
        String value = asString(data, key);
        return value == null ? null : Instant.parse(value);
    }

    private static List<String> asStrings(Map<String, Object> data, String key) {
        Object value = data.get(key);
        if (!(value instanceof List<?> values)) {
            return List.of();
        }
        return values.stream()
                .map(String::valueOf)
                .toList();
    }
}
