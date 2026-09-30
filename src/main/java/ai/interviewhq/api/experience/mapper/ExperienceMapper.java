package ai.interviewhq.api.experience.mapper;

import ai.interviewhq.api.experience.dto.ExperienceResponse;
import ai.interviewhq.api.experience.dto.ExperienceSummaryResponse;

import java.time.Instant;
import java.util.Map;

public class ExperienceMapper {

    public ExperienceResponse toResponse(Map<String, Object> data) {
        return new ExperienceResponse(
                asInteger(data, "id"),
                asString(data, "sourcePlatform"),
                asString(data, "title"),
                asString(data, "summary"),
                asString(data, "author"),
                asInstant(data, "postedAt"),
                asString(data, "originalPostUrl"),
                asString(data, "company"),
                asString(data, "role"),
                asString(data, "level"),
                asString(data, "location"),
                asFloat(data, "candidateYoE"),
                asInteger(data, "questionCount"),
                asInstant(data, "createdAt"));
    }

    public ExperienceSummaryResponse toSummary(Map<String, Object> data) {
        return new ExperienceSummaryResponse(
                asInteger(data, "id"),
                asString(data, "title"),
                asString(data, "company"),
                asString(data, "role"),
                asString(data, "level"),
                asString(data, "location"),
                asString(data, "sourcePlatform"),
                asInstant(data, "postedAt"),
                asString(data, "originalPostUrl"),
                asInteger(data, "questionCount"));
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
}
