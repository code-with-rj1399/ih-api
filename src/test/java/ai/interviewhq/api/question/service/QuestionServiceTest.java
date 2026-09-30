package ai.interviewhq.api.question.service;

import ai.interviewhq.api.common.pagination.OpaqueCursorCodec;
import ai.interviewhq.api.question.mapper.QuestionMapper;
import ai.interviewhq.api.question.repository.*;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class QuestionServiceTest {
    @Test
    void defaultListUsesRecentExtractedProjectionNewestFirst() {
        QuestionRepository repo = mock(QuestionRepository.class);
        when(repo.list(anyString(), anyBoolean(), anyInt(), any(), any(), any()))
                .thenReturn(new ai.interviewhq.api.infrastructure.dynamodb.DynamoDbPage<>(List.of(), Map.of()));

        var service = new QuestionService(repo, new QuestionMapper(),
                new OpaqueCursorCodec(new com.fasterxml.jackson.databind.ObjectMapper()));

        service.list(null, null, null, null, null);

        verify(repo).list(eq("QINDEX#EXTRACTED"), eq(false), eq(25), isNull(), isNull(), eq(Map.of()));
    }

    @Test
    void companyFilterUsesCompanyProjection() {
        QuestionRepository repo = mock(QuestionRepository.class);
        when(repo.list(anyString(), anyBoolean(), anyInt(), any(), any(), any()))
                .thenReturn(new ai.interviewhq.api.infrastructure.dynamodb.DynamoDbPage<>(List.of(), Map.of()));

        var service = new QuestionService(repo, new QuestionMapper(),
                new OpaqueCursorCodec(new com.fasterxml.jackson.databind.ObjectMapper()));

        service.list(null, null, "Acme", null, "newest");

        ArgumentCaptor<String> pk = ArgumentCaptor.forClass(String.class);
        verify(repo).list(pk.capture(), eq(false), eq(25), isNull(), isNull(), eq(Map.of()));
        assertEquals("QINDEX#COMPANY#acme", pk.getValue());
    }

    @Test
    void typeFilterUsesTypeProjection() {
        QuestionRepository repo = mock(QuestionRepository.class);
        when(repo.list(anyString(), anyBoolean(), anyInt(), any(), any(), any()))
                .thenReturn(new ai.interviewhq.api.infrastructure.dynamodb.DynamoDbPage<>(List.of(), Map.of()));

        var service = new QuestionService(repo, new QuestionMapper(),
                new OpaqueCursorCodec(new com.fasterxml.jackson.databind.ObjectMapper()));

        service.list(null, null, null, "System Design", "oldest");

        verify(repo).list(eq("QINDEX#TYPE#System Design"), eq(true), eq(25), isNull(), isNull(), eq(Map.of()));
    }

    @Test
    void companyAndTypeUsesBoundedFilter() {
        QuestionRepository repo = mock(QuestionRepository.class);
        when(repo.list(anyString(), anyBoolean(), anyInt(), any(), any(), any()))
                .thenReturn(new ai.interviewhq.api.infrastructure.dynamodb.DynamoDbPage<>(List.of(), Map.of()));

        var service = new QuestionService(repo, new QuestionMapper(),
                new OpaqueCursorCodec(new com.fasterxml.jackson.databind.ObjectMapper()));

        service.list(null, null, "Acme", "Coding", "newest");

        ArgumentCaptor<String> filter = ArgumentCaptor.forClass(String.class);
        verify(repo).list(eq("QINDEX#COMPANY#acme"), eq(false), eq(25), isNull(), filter.capture(), anyMap());
        assertEquals("contains(data.questionTypes, :type)", filter.getValue());
    }
    @Test
    void combinedFiltersPreserveOpaqueCursor() {
        QuestionRepository repo = mock(QuestionRepository.class);
        when(repo.list(anyString(), anyBoolean(), anyInt(), any(), any(), any()))
                .thenReturn(new ai.interviewhq.api.infrastructure.dynamodb.DynamoDbPage<>(List.of(), Map.of()));

        var service = new QuestionService(repo, new QuestionMapper(),
                new OpaqueCursorCodec(new com.fasterxml.jackson.databind.ObjectMapper()));

        var codec = new OpaqueCursorCodec(new com.fasterxml.jackson.databind.ObjectMapper());
        String cursor = codec.encode(Map.of("pk", "QINDEX#COMPANY#acme", "sk", "2026-09-30T00:00:00Z#7"));

        service.list(10, cursor, "Acme", "Coding", "newest");

        verify(repo).list(eq("QINDEX#COMPANY#acme"), eq(false), eq(10),
                eq(Map.of("pk", "QINDEX#COMPANY#acme", "sk", "2026-09-30T00:00:00Z#7")),
                eq("contains(data.questionTypes, :type)"), anyMap());
    }
}

