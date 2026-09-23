package ai.interviewhq.api.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "ih.security.enabled=true",
        "ih.security.crawler-key=crawler-secret",
        "ih.security.ui-key=ui-secret"
})
class SecurityEnabledTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    void healthIsPublic() throws Exception {
        mockMvc.perform(get("/actuator/health")).andExpect(status().isOk());
    }

    @Test
    void apiWithoutKeyIsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/questions/types"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void readKeyCanGetButCannotIngest() throws Exception {
        mockMvc.perform(get("/api/v1/questions/types").header("X-API-Key", "ui-secret"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.questionTypes").isArray());
        mockMvc.perform(post("/api/v1/questions/ingest").header("X-API-Key", "ui-secret"))
                .andExpect(status().isForbidden());
    }

    @Test
    void crawlerKeyIsAccepted() throws Exception {
        mockMvc.perform(get("/api/v1/questions/types").header("X-API-Key", "crawler-secret"))
                .andExpect(status().isOk());
    }

    @Test
    void invalidKeyIsRejected() throws Exception {
        mockMvc.perform(get("/api/v1/questions/types").header("X-API-Key", "nope"))
                .andExpect(status().isUnauthorized());
    }
}
