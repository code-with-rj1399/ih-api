package ai.interviewhq.api;

import ai.interviewhq.api.common.exception.PublicApiException;
import ai.interviewhq.api.common.exception.PublicApiExceptionHandler;
import ai.interviewhq.api.common.response.ApiCollectionResponse;
import ai.interviewhq.api.experience.controller.ExperienceController;
import ai.interviewhq.api.experience.dto.ExperienceResponse;
import ai.interviewhq.api.experience.dto.ExperienceSummaryResponse;
import ai.interviewhq.api.experience.service.ExperienceService;
import ai.interviewhq.api.metadata.controller.MetadataController;
import ai.interviewhq.api.metadata.dto.MetadataItemResponse;
import ai.interviewhq.api.metadata.service.MetadataService;
import ai.interviewhq.api.question.controller.QuestionController;
import ai.interviewhq.api.question.dto.QuestionResponse;
import ai.interviewhq.api.question.dto.QuestionSummaryResponse;
import ai.interviewhq.api.question.service.QuestionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest({
        QuestionController.class,
        ExperienceController.class,
        MetadataController.class
})
@Import(PublicApiExceptionHandler.class)
class PublicApiContractTest {
    @Autowired MockMvc mvc;

    @MockBean QuestionService questions;
    @MockBean ExperienceService experiences;
    @MockBean MetadataService metadata;

    @Test
    void questionCollectionSupportsFilterAndPagination() throws Exception {
        when(questions.list(10, "opaque", "Amazon", "System Design", "newest"))
                .thenReturn(new ApiCollectionResponse<>(
                        List.of(new QuestionSummaryResponse(
                                7, 4, "Design a cache", List.of("System Design"),
                                .9f, null, null, null, "Amazon", "SDE II", "Glassdoor")),
                        new ApiCollectionResponse.Pagination(10, "next", true)));

        mvc.perform(get("/api/v1/questions")
                        .param("limit", "10")
                        .param("cursor", "opaque")
                        .param("company", "Amazon")
                        .param("type", "System Design"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].id").value(7))
                .andExpect(jsonPath("$.pagination.nextCursor").value("next"))
                .andExpect(jsonPath("$.pagination.hasMore").value(true));
    }

    @Test
    void questionDetailAndNotFoundContract() throws Exception {
        when(questions.find(7)).thenReturn(new QuestionResponse(
                7, 4, null, List.of("Coding"), "Easy", "Two Sum",
                null, null, .99f, "SPECIFIC", null, null));

        mvc.perform(get("/api/v1/questions/7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.item.id").value(7))
                .andExpect(jsonPath("$.item.questionText").value("Two Sum"));

        when(questions.find(99)).thenThrow(new PublicApiException(
                "NOT_FOUND", "Interview question not found", HttpStatus.NOT_FOUND));

        mvc.perform(get("/api/v1/questions/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("NOT_FOUND"));
    }

    @Test
    void experienceCollectionAndCompanyFilterContract() throws Exception {
        when(experiences.list(25, null, "newest", "Amazon"))
                .thenReturn(new ApiCollectionResponse<>(
                        List.of(new ExperienceSummaryResponse(
                                4, "SDE Interview", "Amazon", "SDE II", "L5",
                                "Seattle", "Glassdoor", null, "https://example.com/4", 5)),
                        new ApiCollectionResponse.Pagination(25, null, false)));

        mvc.perform(get("/api/v1/experiences").param("company", "Amazon"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].company").value("Amazon"))
                .andExpect(jsonPath("$.pagination.hasMore").value(false));
    }

    @Test
    void experienceDetailAndQuestionsContract() throws Exception {
        when(experiences.find(4)).thenReturn(new ExperienceResponse(
                4, "Glassdoor", "SDE Interview", null, null, null, null,
                "Amazon", "SDE II", "L5", "Seattle", 5f, 2, null));
        when(questions.byExperience(4, 25, null))
                .thenReturn(new ApiCollectionResponse<>(
                        List.of(new QuestionSummaryResponse(
                                7, 4, "Two Sum", List.of("Coding"), .9f,
                                null, null, null, "Amazon", "SDE II", "Glassdoor")),
                        new ApiCollectionResponse.Pagination(25, null, false)));

        mvc.perform(get("/api/v1/experiences/4"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.item.id").value(4));

        mvc.perform(get("/api/v1/experiences/4/questions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].experienceId").value(4));
    }

    @Test
    void metadataContractsAreBoundedAndPaginated() throws Exception {
        when(metadata.companies(100, null))
                .thenReturn(new ApiCollectionResponse<>(
                        List.of(new MetadataItemResponse("Amazon", "amazon")),
                        new ApiCollectionResponse.Pagination(100, null, false)));
        when(metadata.questionTypes(10, "opaque"))
                .thenReturn(new ApiCollectionResponse<>(
                        List.of(new MetadataItemResponse("Coding", "coding")),
                        new ApiCollectionResponse.Pagination(10, "next", true)));

        mvc.perform(get("/api/v1/meta/companies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].slug").value("amazon"));

        mvc.perform(get("/api/v1/meta/question-types")
                        .param("limit", "10")
                        .param("cursor", "opaque"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pagination.nextCursor").value("next"));
    }

    @Test
    void invalidRequestsReturn400() throws Exception {
        mvc.perform(get("/api/v1/questions").param("limit", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));

        mvc.perform(get("/api/v1/experiences").param("limit", "101"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));

        mvc.perform(get("/api/v1/meta/companies").param("limit", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));
    }
}
