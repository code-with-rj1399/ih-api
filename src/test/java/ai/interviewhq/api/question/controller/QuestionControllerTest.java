package ai.interviewhq.api.question.controller;
import ai.interviewhq.api.common.exception.PublicApiExceptionHandler; import ai.interviewhq.api.common.response.ApiCollectionResponse; import ai.interviewhq.api.question.dto.*; import ai.interviewhq.api.question.service.QuestionService; import org.junit.jupiter.api.Test; import org.springframework.beans.factory.annotation.Autowired; import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest; import org.springframework.boot.test.mock.mockito.MockBean; import org.springframework.context.annotation.Import; import org.springframework.test.web.servlet.MockMvc;
import java.util.List; import static org.mockito.Mockito.*; import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get; import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
@WebMvcTest(QuestionController.class) @Import(PublicApiExceptionHandler.class)
class QuestionControllerTest {
 @Autowired MockMvc mvc; @MockBean QuestionService service;
 @Test void listsQuestions(){when(service.list(null,null,null,null,"newest")).thenReturn(new ApiCollectionResponse<>(List.of(new QuestionSummaryResponse(1,2,"Q",List.of("Coding"),.9f,null,null,null,null,null)),new ApiCollectionResponse.Pagination(25,null,false))); try{mvc.perform(get("/api/v1/questions")).andExpect(status().isOk()).andExpect(jsonPath("$.items[0].id").value(1)).andExpect(jsonPath("$.pagination.limit").value(25));}catch(Exception e){throw new AssertionError(e);}}
 @Test void rejectsInvalidId(){try{mvc.perform(get("/api/v1/questions/0")).andExpect(status().isBadRequest()).andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));verifyNoInteractions(service);}catch(Exception e){throw new AssertionError(e);}}
}
