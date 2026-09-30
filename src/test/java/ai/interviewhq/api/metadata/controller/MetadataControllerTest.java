package ai.interviewhq.api.metadata.controller;

import ai.interviewhq.api.common.exception.PublicApiExceptionHandler;
import ai.interviewhq.api.common.response.ApiCollectionResponse;
import ai.interviewhq.api.metadata.dto.MetadataItemResponse;
import ai.interviewhq.api.metadata.service.MetadataService;
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

@WebMvcTest(MetadataController.class)
@Import(PublicApiExceptionHandler.class)
class MetadataControllerTest {
    @Autowired MockMvc mvc;
    @MockBean MetadataService service;

    @Test
    void listsCompanies() throws Exception {
        when(service.companies(null, null))
                .thenReturn(new ApiCollectionResponse<>(
                        List.of(new MetadataItemResponse("Amazon", "amazon")),
                        new ApiCollectionResponse.Pagination(100, null, false)));

        mvc.perform(get("/api/v1/meta/companies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].name").value("Amazon"))
                .andExpect(jsonPath("$.items[0].slug").value("amazon"))
                .andExpect(jsonPath("$.pagination.limit").value(100));
    }

    @Test
    void listsQuestionTypesWithCursor() throws Exception {
        when(service.questionTypes(10, "opaque"))
                .thenReturn(new ApiCollectionResponse<>(
                        List.of(new MetadataItemResponse("Coding", "coding")),
                        new ApiCollectionResponse.Pagination(10, "next", true)));

        mvc.perform(get("/api/v1/meta/question-types")
                        .param("limit", "10")
                        .param("cursor", "opaque"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pagination.nextCursor").value("next"));
    }

    @Test
    void rejectsInvalidLimitBeforeService() throws Exception {
        mvc.perform(get("/api/v1/meta/companies").param("limit", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));

        verifyNoInteractions(service);
    }
}
