package ai.interviewhq.api.service;

import ai.interviewhq.api.domain.Experience;
import ai.interviewhq.api.domain.Question;
import ai.interviewhq.api.domain.QuestionType;
import ai.interviewhq.api.dynamo.ExperienceRepository;
import ai.interviewhq.api.dynamo.QuestionRepository;
import ai.interviewhq.api.hashing.Hashes;
import ai.interviewhq.api.web.dto.IngestDtos.IngestRequest;
import ai.interviewhq.api.web.dto.IngestDtos.IngestResponse;
import ai.interviewhq.api.web.dto.IngestDtos.IngestedQuestion;
import ai.interviewhq.api.web.dto.IngestDtos.QuestionPayload;
import ai.interviewhq.api.web.dto.IngestDtos.RejectedQuestion;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class IngestService {

    private final QuestionRepository questionRepository;
    private final ExperienceRepository experienceRepository;

    public IngestService(QuestionRepository questionRepository, ExperienceRepository experienceRepository) {
        this.questionRepository = questionRepository;
        this.experienceRepository = experienceRepository;
    }

    public IngestResponse ingest(IngestRequest request) {
        Instant now = Instant.now();
        String sourceUrl = request.source().url();
        String sourceName = request.source().name();
        String experienceId = Hashes.experienceId(sourceUrl);

        Experience experience = new Experience();
        experience.setExperienceId(experienceId);
        experience.setSourceName(sourceName);
        experience.setSourceUrl(sourceUrl);
        experience.setCompany(blankToNull(request.experience().company()));
        experience.setTitle(blankToNull(request.experience().title()));
        experience.setAuthor(blankToNull(request.experience().author()));
        experience.setContentHash(blankToNull(request.experience().contentHash()));
        experience.setPostedAt(request.experience().postedAt());
        experience.setCrawledAt(now);
        experience.setCreatedAt(now);
        experience.setUpdatedAt(now);
        boolean experienceCreated = experienceRepository.createIfAbsent(experience);

        List<IngestedQuestion> accepted = new ArrayList<>();
        List<RejectedQuestion> rejected = new ArrayList<>();

        for (QuestionPayload payload : request.questions()) {
            List<String> errors = new ArrayList<>(ai.interviewhq.api.web.dto.IngestDtos.validateQuestion(payload));
            QuestionType type = null;
            try {
                type = QuestionType.fromCanonical(payload.questionType());
            } catch (IllegalArgumentException ex) {
                errors.add(ex.getMessage());
            }
            String company = firstNonBlank(payload.company(), request.experience().company());
            if (company == null) {
                errors.add("company is required on the experience or the question");
            }
            if (!errors.isEmpty()) {
                rejected.add(new RejectedQuestion(payload.questionText(), errors));
                continue;
            }

            String questionId = Hashes.questionId(company, payload.questionText(), type);
            Question question = new Question();
            question.setQuestionId(questionId);
            question.setQuestionText(payload.questionText().trim());
            question.setQuestionDescription(payload.questionDescription().trim());
            question.setQuestionType(type);
            question.setTopics(payload.topics() == null ? List.of() : payload.topics());
            question.setCompany(company);
            question.setSourceUrl(sourceUrl);
            question.setSourceName(sourceName);
            question.setExperienceId(experienceId);
            question.setProblemUrl(sanitizeProblemUrl(payload.problemUrl(), sourceUrl));
            question.setPostedAt(request.experience().postedAt());
            question.setCrawledAt(now);
            question.setConfidence(payload.confidence());
            question.setQuestionSpecificity(payload.questionSpecificity());
            question.setCreatedAt(now);
            question.setUpdatedAt(now);

            boolean created = questionRepository.createIfAbsent(question);
            experienceRepository.linkQuestion(experienceId, questionId);
            accepted.add(new IngestedQuestion(questionId, created ? "CREATED" : "DUPLICATE"));
        }

        if (accepted.isEmpty() && !rejected.isEmpty()) {
            throw new IllegalArgumentException("All questions were rejected: "
                    + rejected.get(0).errors());
        }

        return new IngestResponse(experienceId, experienceCreated, accepted, rejected);
    }

    /**
     * problemUrl is only stored when the source explicitly provided a distinct URL.
     * The experience URL is never copied into problemUrl.
     */
    static String sanitizeProblemUrl(String problemUrl, String sourceUrl) {
        if (problemUrl == null || problemUrl.isBlank()) {
            return null;
        }
        String canonicalProblem = Hashes.canonicalizeUrl(problemUrl);
        String canonicalSource = Hashes.canonicalizeUrl(sourceUrl);
        if (canonicalProblem.equals(canonicalSource)) {
            return null;
        }
        return problemUrl.trim();
    }

    private static String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value.trim();
            }
        }
        return null;
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
