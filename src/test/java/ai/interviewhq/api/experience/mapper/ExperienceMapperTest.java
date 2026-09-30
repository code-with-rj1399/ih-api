package ai.interviewhq.api.experience.mapper;

import ai.interviewhq.api.experience.dto.ExperienceResponse;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ExperienceMapperTest {

    @Test
    void mapsAllSupportedExperienceFields() {
        Map<String, Object> persisted = Map.of(
                "id", "7",
                "sourcePlatform", "Glassdoor",
                "title", "SDE Interview Experience",
                "summary", "Four-round interview.",
                "author", "candidate-1",
                "postedAt", "2026-09-28T00:00:00Z",
                "originalPostUrl", "https://example.com/interview/7",
                "company", "Acme",
                "role", "SWE",
                "level", "L4",
                "location", "Seattle, WA",
                "candidateYoE", "4.5",
                "questionCount", "6",
                "createdAt", "2026-09-28T01:00:00Z",
                "sourceId", "internal-source",
                "crawlRunId", "internal-run",
                "dedupeHash", "internal-hash");

        ExperienceResponse result = new ExperienceMapper().toResponse(persisted);

        assertEquals(7, result.id());
        assertEquals("Glassdoor", result.sourcePlatform());
        assertEquals("SDE Interview Experience", result.title());
        assertEquals("Four-round interview.", result.summary());
        assertEquals("candidate-1", result.author());
        assertEquals(Instant.parse("2026-09-28T00:00:00Z"), result.postedAt());
        assertEquals("https://example.com/interview/7", result.originalPostUrl());
        assertEquals("Acme", result.company());
        assertEquals("SWE", result.role());
        assertEquals("L4", result.level());
        assertEquals("Seattle, WA", result.location());
        assertEquals(4.5f, result.candidateYoE());
        assertEquals(6, result.questionCount());
        assertEquals(Instant.parse("2026-09-28T01:00:00Z"), result.createdAt());
    }

    @Test
    void publicResponseDoesNotExposeInternalStorageFields() {
        ExperienceResponse result = new ExperienceMapper().toResponse(
                Map.of(
                        "id", "7",
                        "title", "SDE Interview Experience",
                        "sourceId", "internal-source",
                        "crawlRunId", "internal-run",
                        "dedupeHash", "internal-hash"));

        assertEquals(7, result.id());
        assertEquals("SDE Interview Experience", result.title());
        assertFalse(result.toString().contains("internal-source"));
        assertFalse(result.toString().contains("internal-run"));
        assertFalse(result.toString().contains("internal-hash"));
    }

    @Test
    void optionalFieldsRemainNull() {
        ExperienceResponse result = new ExperienceMapper().toResponse(
                Map.of("id", "7", "title", "SDE Interview Experience"));

        assertNull(result.sourcePlatform());
        assertNull(result.summary());
        assertNull(result.author());
        assertNull(result.postedAt());
        assertNull(result.originalPostUrl());
        assertNull(result.company());
        assertNull(result.role());
        assertNull(result.level());
        assertNull(result.location());
        assertNull(result.candidateYoE());
        assertNull(result.questionCount());
        assertNull(result.createdAt());
    }
}
