package ai.interviewhq.api.service;

import ai.interviewhq.api.domain.Question;
import ai.interviewhq.api.domain.QuestionType;
import ai.interviewhq.api.dynamo.ExperienceRepository;
import ai.interviewhq.api.dynamo.PageResult;
import ai.interviewhq.api.dynamo.QuestionRepository;
import ai.interviewhq.api.web.NotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class QuestionQueryService {

    private final QuestionRepository questionRepository;
    private final ExperienceRepository experienceRepository;

    public QuestionQueryService(QuestionRepository questionRepository, ExperienceRepository experienceRepository) {
        this.questionRepository = questionRepository;
        this.experienceRepository = experienceRepository;
    }

    public Question get(String questionId) {
        return questionRepository.findById(questionId)
                .orElseThrow(() -> new NotFoundException("Question not found: " + questionId));
    }

    public PageResult<Question> list(String company, String type, String source, int limit, String cursor) {
        int pageSize = clamp(limit);
        if (company != null && !company.isBlank()) {
            return questionRepository.findByCompany(company.trim(), pageSize, cursor);
        }
        if (type != null && !type.isBlank()) {
            QuestionType parsed = QuestionType.fromCanonical(type);
            return questionRepository.findByType(parsed.canonical(), pageSize, cursor);
        }
        if (source != null && !source.isBlank()) {
            return questionRepository.findBySource(source.trim(), pageSize, cursor);
        }
        return questionRepository.findRecent(pageSize, cursor);
    }

    public List<Question> forExperience(String experienceId) {
        experienceRepository.findById(experienceId)
                .orElseThrow(() -> new NotFoundException("Experience not found: " + experienceId));
        List<Question> questions = new ArrayList<>();
        experienceRepository.findQuestionLinks(experienceId).forEach(link ->
                questionRepository.findById(link.getQuestionId()).ifPresent(questions::add));
        return questions;
    }

    public List<String> canonicalTypes() {
        List<String> types = new ArrayList<>();
        for (QuestionType type : QuestionType.values()) {
            types.add(type.canonical());
        }
        return types;
    }

    private static int clamp(int limit) {
        if (limit <= 0) {
            return 25;
        }
        return Math.min(limit, 100);
    }
}
