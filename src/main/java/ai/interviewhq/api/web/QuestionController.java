package ai.interviewhq.api.web;

import ai.interviewhq.api.domain.Question;
import ai.interviewhq.api.dynamo.PageResult;
import ai.interviewhq.api.service.QuestionQueryService;
import ai.interviewhq.api.web.dto.PageResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/questions")
public class QuestionController {

    private final QuestionQueryService questions;

    public QuestionController(QuestionQueryService questions) {
        this.questions = questions;
    }

    @GetMapping
    public PageResponse<Question> list(
            @RequestParam(required = false) String company,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String source,
            @RequestParam(defaultValue = "25") int limit,
            @RequestParam(required = false) String cursor) {
        PageResult<Question> page = questions.list(company, type, source, limit, cursor);
        return PageResponse.of(page.getItems(), page.getNextCursor());
    }

    @GetMapping("/types")
    public Map<String, List<String>> types() {
        return Map.of("questionTypes", questions.canonicalTypes());
    }

    @GetMapping("/{questionId}")
    public Question get(@PathVariable String questionId) {
        return questions.get(questionId);
    }
}
