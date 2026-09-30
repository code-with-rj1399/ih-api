package ai.interviewhq.api.config;

import ai.interviewhq.api.system.controller.PublicApiController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PublicApiController.class)
@Import(WebConfig.class)
class WebConfigTest {
 @Autowired MockMvc mvc;
 @Test void allowsConfiguredLocalOrigin(){try{mvc.perform(options("/api/v1/health").header("Origin","http://localhost:3000").header("Access-Control-Request-Method","GET")).andExpect(status().isOk()).andExpect(header().string("Access-Control-Allow-Origin","http://localhost:3000"));}catch(Exception e){throw new AssertionError(e);}}
}
