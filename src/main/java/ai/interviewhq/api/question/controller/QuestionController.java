package ai.interviewhq.api.question.controller;
import ai.interviewhq.api.common.response.*; import ai.interviewhq.api.question.dto.*; import ai.interviewhq.api.question.service.QuestionService;
import jakarta.validation.constraints.*; import org.springframework.validation.annotation.Validated; import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/v1/questions") @Validated
public class QuestionController {
 private final QuestionService service; public QuestionController(QuestionService service){this.service=service;}
 @GetMapping public ApiCollectionResponse<QuestionSummaryResponse> list(@RequestParam(required=false)@Min(1)@Max(100)Integer limit,@RequestParam(required=false)@Size(max=2048)String cursor,@RequestParam(required=false)@Size(max=200)String company,@RequestParam(required=false)@Size(max=100)String type,@RequestParam(defaultValue="newest")@Size(max=20)String sort){return service.list(limit,cursor,company,type,sort);}
 @GetMapping("/{id}") public ApiItemResponse<QuestionResponse> get(@PathVariable @Min(1) Integer id){return new ApiItemResponse<>(service.find(id));}
}
