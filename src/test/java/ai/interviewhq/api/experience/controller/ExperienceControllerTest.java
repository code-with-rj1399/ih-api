package ai.interviewhq.api.experience.controller;
import ai.interviewhq.api.common.exception.PublicApiExceptionHandler; import ai.interviewhq.api.experience.dto.ExperienceResponse; import ai.interviewhq.api.experience.service.ExperienceService; import ai.interviewhq.api.question.service.QuestionService; import org.junit.jupiter.api.Test; import org.springframework.beans.factory.annotation.Autowired; import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest; import org.springframework.boot.test.mock.mockito.MockBean; import org.springframework.context.annotation.Import; import org.springframework.test.web.servlet.MockMvc;
import static org.mockito.Mockito.*; import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get; import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
@WebMvcTest(ExperienceController.class) @Import(PublicApiExceptionHandler.class)
class ExperienceControllerTest {
 @Autowired MockMvc mvc; @MockBean ExperienceService experiences; @MockBean QuestionService questions;
 @Test void getsExperience(){when(experiences.find(7)).thenReturn(new ExperienceResponse(7,null,null,"glassdoor","Interview",null,null,null,null,"Acme","SWE",null,"Seattle",null,3,null));try{mvc.perform(get("/api/v1/experiences/7")).andExpect(status().isOk()).andExpect(jsonPath("$.item.id").value(7)).andExpect(jsonPath("$.item.company").value("Acme"));}catch(Exception e){throw new AssertionError(e);}}
 @Test void rejectsInvalidId(){try{mvc.perform(get("/api/v1/experiences/0")).andExpect(status().isBadRequest()).andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));verifyNoInteractions(experiences);}catch(Exception e){throw new AssertionError(e);}}
}
