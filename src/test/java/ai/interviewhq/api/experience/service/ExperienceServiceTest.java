package ai.interviewhq.api.experience.service;

import ai.interviewhq.api.common.pagination.OpaqueCursorCodec;
import ai.interviewhq.api.experience.mapper.ExperienceMapper;
import ai.interviewhq.api.experience.repository.ExperienceRepository;
import ai.interviewhq.api.infrastructure.dynamodb.DynamoDbPage;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ExperienceServiceTest {
    @Test
    void defaultListQueriesPostedProjectionNewestFirst() {
        ExperienceRepository repo = mock(ExperienceRepository.class);
        when(repo.list(anyString(), anyBoolean(), anyInt(), any()))
                .thenReturn(new DynamoDbPage<>(List.of(), Map.of()));

        var service = new ExperienceService(repo, new ExperienceMapper(),
                new OpaqueCursorCodec(new com.fasterxml.jackson.databind.ObjectMapper()));

        service.list(null, null, null);

        verify(repo).list(eq("EINDEX#POSTED"), eq(false), eq(25), isNull());
    }

    @Test
    void oldestSortQueriesProjectionAscending() {
        ExperienceRepository repo = mock(ExperienceRepository.class);
        when(repo.list(anyString(), anyBoolean(), anyInt(), any()))
                .thenReturn(new DynamoDbPage<>(List.of(), Map.of()));

        var service = new ExperienceService(repo, new ExperienceMapper(),
                new OpaqueCursorCodec(new com.fasterxml.jackson.databind.ObjectMapper()));

        service.list(10, "opaque", "oldest");

        verify(repo).list(eq("EINDEX#POSTED"), eq(true), eq(10), any());
    }
}
