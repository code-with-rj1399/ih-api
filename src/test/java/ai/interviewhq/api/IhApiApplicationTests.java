package ai.interviewhq.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class IhApiApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void contextLoads() {
    }

    @Test
    void metaEndpointReportsLocalDynamoMode() throws Exception {
        mockMvc.perform(get("/api/v1/meta"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.service").value("ih-api"))
                .andExpect(jsonPath("$.status").value("up"))
                .andExpect(jsonPath("$.dynamodb.mode").value("local"))
                .andExpect(jsonPath("$.dynamodb.table").value("interview-hq-test"));
    }
}
