package ai.interviewhq.api.question.mapper;

import ai.interviewhq.api.question.dto.QuestionResponse;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class QuestionMapperTest {

    @Test
    void mapsAllSupportedQuestionFields() {
        Map<String, Object> persisted = Map.ofEntries(
                Map.entry("id", "12"),
                Map.entry("experienceId", "4"),
                Map.entry("problemUrl", "https://leetcode.com/problems/two-sum/"),
                Map.entry("questionTypes", List.of("Coding", "DSA")),
                Map.entry("difficulty", "Medium"),
                Map.entry("questionText", "Design a cache"),
                Map.entry("questionDescription", "Support TTL and LRU eviction."),
                Map.entry("candidateApproach", "Use a hash map plus doubly linked list."),
                Map.entry("confidence", "0.92"),
                Map.entry("questionParticularity", "SPECIFIC"),
                Map.entry("extractedAt", "2026-09-30T08:00:00Z"),
                Map.entry("createdAt", "2026-09-30T08:05:00Z"),
                Map.entry("modelName", "internal-model"),
                Map.entry("dedupeHash", "internal-hash"));

        QuestionResponse result = new QuestionMapper().toResponse(persisted);

        assertEquals(12, result.id());
        assertEquals(4, result.experienceId());
        assertEquals("https://leetcode.com/problems/two-sum/", result.problemUrl());
        assertEquals(List.of("Coding", "DSA"), result.questionTypes());
        assertEquals("Medium", result.difficulty());
        assertEquals("Design a cache", result.questionText());
        assertEquals("Support TTL and LRU eviction.", result.questionDescription());
        assertEquals("Use a hash map plus doubly linked list.", result.candidateApproach());
        assertEquals(0.92f, result.confidence());
        assertEquals("SPECIFIC", result.questionParticularity());
        assertEquals(Instant.parse("2026-09-30T08:00:00Z"), result.extractedAt());
        assertEquals(Instant.parse("2026-09-30T08:05:00Z"), result.createdAt());
    }

    @Test
    void doesNotExposeInternalPersistenceFields() {
        Map<String, Object> persisted = Map.of(
                "id", "12",
                "experienceId", "4",
                "questionText", "Design a cache",
                "modelName", "internal-model",
                "dedupeHash", "internal-hash");

        QuestionResponse result = new QuestionMapper().toResponse(persisted);

        assertEquals(12, result.id());
        assertEquals(4, result.experienceId());
        assertEquals("Design a cache", result.questionText());
        assertFalse(result.toString().contains("internal-model"));
        assertFalse(result.toString().contains("internal-hash"));
    }

    @Test
    void optionalFieldsRemainNull() {
        QuestionResponse result = new QuestionMapper().toResponse(
                Map.of("id", "1", "questionText", "Q"));

        assertNull(result.problemUrl());
        assertEquals(List.of(), result.questionTypes());
        assertNull(result.difficulty());
        assertNull(result.questionDescription());
        assertNull(result.candidateApproach());
        assertNull(result.confidence());
        assertNull(result.questionParticularity());
        assertNull(result.extractedAt());
        assertNull(result.createdAt());
    }

    @Test
    void nonListQuestionTypesAreNormalizedToEmptyList() {
        QuestionResponse result = new QuestionMapper().toResponse(
                Map.of("id", "1", "questionText", "Q", "questionTypes", "Coding"));

        assertEquals(List.of(), result.questionTypes());
    }
}
