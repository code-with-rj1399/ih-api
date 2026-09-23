package ai.interviewhq.api.service;

import ai.interviewhq.api.domain.Question;
import ai.interviewhq.api.dynamo.ExperienceRepository;
import ai.interviewhq.api.dynamo.QuestionRepository;
import ai.interviewhq.api.web.dto.IngestDtos.ExperiencePayload;
import ai.interviewhq.api.web.dto.IngestDtos.IngestRequest;
import ai.interviewhq.api.web.dto.IngestDtos.IngestResponse;
import ai.interviewhq.api.web.dto.IngestDtos.QuestionPayload;
import ai.interviewhq.api.web.dto.IngestDtos.Source;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IngestServiceTest {

    @Mock
    QuestionRepository questionRepository;
    @Mock
    ExperienceRepository experienceRepository;

    IngestService service;

    @BeforeEach
    void setUp() {
        service = new IngestService(questionRepository, experienceRepository);
    }

    @Test
    void persistsValidQuestionAndIsIdempotentOnDuplicate() {
        when(experienceRepository.createIfAbsent(any())).thenReturn(true);
        when(questionRepository.createIfAbsent(any())).thenReturn(true, false);

        IngestRequest request = sampleRequest("System Design", 0.9, "https://leetcode.com/problems/lru");
        IngestResponse first = service.ingest(request);
        IngestResponse second = service.ingest(request);

        assertThat(first.questions()).hasSize(1);
        assertThat(first.questions().get(0).status()).isEqualTo("CREATED");
        assertThat(second.questions().get(0).status()).isEqualTo("DUPLICATE");
        assertThat(first.questions().get(0).questionId()).isEqualTo(second.questions().get(0).questionId());
        verify(experienceRepository, org.mockito.Mockito.times(2)).linkQuestion(any(), any());
    }

    @Test
    void doesNotCopyExperienceUrlIntoProblemUrl() {
        when(experienceRepository.createIfAbsent(any())).thenReturn(true);
        when(questionRepository.createIfAbsent(any())).thenReturn(true);

        String sourceUrl = "https://leetcode.com/discuss/interview-experience/123";
        service.ingest(sampleRequest("System Design", 0.9, sourceUrl));

        ArgumentCaptor<Question> captor = ArgumentCaptor.forClass(Question.class);
        verify(questionRepository).createIfAbsent(captor.capture());
        assertThat(captor.getValue().getProblemUrl()).isNull();
    }

    @Test
    void rejectsUnknownTypeAndOutOfRangeConfidence() {
        when(experienceRepository.createIfAbsent(any())).thenReturn(true);
        IngestRequest request = new IngestRequest(
                new Source("LeetCode", "https://leetcode.com/discuss/1"),
                new ExperiencePayload("Google Interview", Instant.parse("2026-09-23T10:20:00Z"),
                        "anonymous", "Google", null),
                List.of(new QuestionPayload("Design X", "Design X well", "Behavioral",
                        List.of(), null, null, 1.5, 0.2)));

        assertThatThrownBy(() -> service.ingest(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("All questions were rejected");
        verify(questionRepository, never()).createIfAbsent(any());
    }

    @Test
    void sanitizeProblemUrlDropsSourceUrl() {
        assertThat(IngestService.sanitizeProblemUrl(
                "https://leetcode.com/discuss/interview-experience/123/",
                "https://leetcode.com/discuss/interview-experience/123")).isNull();
        assertThat(IngestService.sanitizeProblemUrl(
                "https://leetcode.com/problems/two-sum",
                "https://leetcode.com/discuss/interview-experience/123"))
                .isEqualTo("https://leetcode.com/problems/two-sum");
    }

    private static IngestRequest sampleRequest(String type, double confidence, String problemUrl) {
        return new IngestRequest(
                new Source("LeetCode", "https://leetcode.com/discuss/interview-experience/123"),
                new ExperiencePayload("Google Interview Experience", Instant.parse("2026-09-23T10:20:00Z"),
                        "anonymous", "Google", null),
                List.of(new QuestionPayload(
                        "Design a notification system.",
                        "Design a notification system that supports reliable delivery of notifications.",
                        type,
                        List.of("Notifications"),
                        null,
                        problemUrl,
                        confidence,
                        0.86)));
    }
}
