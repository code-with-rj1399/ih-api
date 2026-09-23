package ai.interviewhq.api.web;

import ai.interviewhq.api.domain.Experience;
import ai.interviewhq.api.domain.Question;
import ai.interviewhq.api.dynamo.ExperienceRepository;
import ai.interviewhq.api.service.QuestionQueryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/experiences")
public class ExperienceController {

    private final ExperienceRepository experienceRepository;
    private final QuestionQueryService questions;

    public ExperienceController(ExperienceRepository experienceRepository, QuestionQueryService questions) {
        this.experienceRepository = experienceRepository;
        this.questions = questions;
    }

    @GetMapping("/{experienceId}")
    public Experience get(@PathVariable String experienceId) {
        return experienceRepository.findById(experienceId)
                .orElseThrow(() -> new NotFoundException("Experience not found: " + experienceId));
    }

    @GetMapping("/{experienceId}/questions")
    public Map<String, List<Question>> questions(@PathVariable String experienceId) {
        return Map.of("items", questions.forExperience(experienceId));
    }
}
