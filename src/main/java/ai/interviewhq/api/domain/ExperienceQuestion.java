package ai.interviewhq.api.domain;

/**
 * Relationship item: {@code PK = EXPERIENCE#{id}, SK = QUESTION#{questionId}}.
 */
public class ExperienceQuestion {

    private String experienceId;
    private String questionId;

    public ExperienceQuestion() {
    }

    public ExperienceQuestion(String experienceId, String questionId) {
        this.experienceId = experienceId;
        this.questionId = questionId;
    }

    public String getExperienceId() {
        return experienceId;
    }

    public void setExperienceId(String experienceId) {
        this.experienceId = experienceId;
    }

    public String getQuestionId() {
        return questionId;
    }

    public void setQuestionId(String questionId) {
        this.questionId = questionId;
    }
}
