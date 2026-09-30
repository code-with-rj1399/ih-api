package ai.interviewhq.api.experience.controller;

import ai.interviewhq.api.common.response.ApiCollectionResponse;
import ai.interviewhq.api.common.response.ApiItemResponse;
import ai.interviewhq.api.experience.dto.ExperienceResponse;
import ai.interviewhq.api.experience.dto.ExperienceSummaryResponse;
import ai.interviewhq.api.experience.service.ExperienceService;
import ai.interviewhq.api.question.dto.QuestionSummaryResponse;
import ai.interviewhq.api.question.service.QuestionService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/experiences")
@Validated
public class ExperienceController {
    private final ExperienceService experiences;
    private final QuestionService questions;

    public ExperienceController(ExperienceService experiences, QuestionService questions) {
        this.experiences = experiences;
        this.questions = questions;
    }

    @GetMapping
    public ApiCollectionResponse<ExperienceSummaryResponse> list(
            @RequestParam(required = false) @Min(1) @Max(100) Integer limit,
            @RequestParam(required = false) @Size(max = 2048) String cursor,
            @RequestParam(defaultValue = "newest") @Size(max = 20) String sort,
            @RequestParam(required = false) @Size(max = 200) String company) {
        return experiences.list(limit, cursor, sort, company);
    }

    @GetMapping("/{id}")
    public ApiItemResponse<ExperienceResponse> get(
            @PathVariable @Min(1) Integer id) {
        return new ApiItemResponse<>(experiences.find(id));
    }

    @GetMapping("/{id}/questions")
    public ApiCollectionResponse<QuestionSummaryResponse> questions(
            @PathVariable @Min(1) Integer id,
            @RequestParam(required = false) @Min(1) @Max(100) Integer limit,
            @RequestParam(required = false) @Size(max = 2048) String cursor) {
        experiences.find(id);
        return questions.byExperience(id, limit, cursor);
    }
}
