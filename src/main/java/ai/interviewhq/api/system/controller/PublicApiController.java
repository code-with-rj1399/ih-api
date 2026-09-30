package ai.interviewhq.api.system.controller;
import org.springframework.web.bind.annotation.GetMapping; import org.springframework.web.bind.annotation.RequestMapping; import org.springframework.web.bind.annotation.RestController;
import java.time.Instant; import java.util.Map;
@RestController @RequestMapping("/api/v1")
public class PublicApiController {
 @GetMapping("/health") public Map<String,Object> health(){return Map.of("service","ih-api","status","ok","timestamp",Instant.now());}
}
