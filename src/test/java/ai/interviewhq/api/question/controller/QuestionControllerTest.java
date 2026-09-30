package ai.interviewhq.api.question.controller;

import ai.interviewhq.api.common.exception.PublicApiExceptionHandler;
import ai.interviewhq.api.common.exception.PublicApiException;
import org.springframework.http.HttpStatus;
import ai.interviewhq.api.common.response.ApiCollectionResponse;
import ai.interviewhq.api.question.dto.*;
import ai.interviewhq.api.question.service.QuestionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(QuestionController.class)
@Import(PublicApiExceptionHandler.class)
class QuestionControllerTest {
    @Autowired MockMvc mvc;
    @MockBean QuestionService service;

    @Test
    void listsQuestions() {
        when(service.list(null, null, null, null, "newest"))
                .thenReturn(new ApiCollectionResponse<>(
                        List.of(new QuestionSummaryResponse(1, 2, "Q", List.of("Coding"), .9f,
                                null, null, null, null, null, null)),
                        new ApiCollectionResponse.Pagination(25, null, false)));

        try {
            mvc.perform(get("/api/v1/questions"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.items[0].id").value(1))
                    .andExpect(jsonPath("$.pagination.limit").value(25));
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    @Test
    void supportsCursorPagination() {
        when(service.list(10, "opaque-cursor", null, null, "newest"))
                .thenReturn(new ApiCollectionResponse<>(
                        List.of(),
                        new ApiCollectionResponse.Pagination(10, "next-cursor", true)));

        try {
            mvc.perform(get("/api/v1/questions")
                            .param("limit", "10")
                            .param("cursor", "opaque-cursor"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.pagination.limit").value(10))
                    .andExpect(jsonPath("$.pagination.nextCursor").value("next-cursor"))
                    .andExpect(jsonPath("$.pagination.hasMore").value(true));
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    @Test
    void getsQuestion() {
        when(service.find(7)).thenReturn(new QuestionResponse(7, 2, null, List.of("Coding"),
                "Easy", "Two sum", null, null, .99f, "SPECIFIC", null, null));

        try {
            mvc.perform(get("/api/v1/questions/7"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.item.id").value(7))
                    .andExpect(jsonPath("$.item.questionText").value("Two sum"));
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    @Test
    void mapsMissingQuestionTo404() {
        when(service.find(7)).thenThrow(new PublicApiException(
                "NOT_FOUND", "Interview question not found", HttpStatus.NOT_FOUND));

        try {
            mvc.perform(get("/api/v1/questions/7"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error.code").value("NOT_FOUND"));
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    @Test
    void rejectsInvalidId() {
        try {
            mvc.perform(get("/api/v1/questions/0"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));
            verifyNoInteractions(service);
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }
}
