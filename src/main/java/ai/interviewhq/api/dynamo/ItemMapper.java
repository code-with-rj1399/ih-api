package ai.interviewhq.api.dynamo;

import ai.interviewhq.api.domain.CrawlPage;
import ai.interviewhq.api.domain.CrawlPageStatus;
import ai.interviewhq.api.domain.CrawlRun;
import ai.interviewhq.api.domain.CrawlRunStatus;
import ai.interviewhq.api.domain.Experience;
import ai.interviewhq.api.domain.ExperienceQuestion;
import ai.interviewhq.api.domain.Keys;
import ai.interviewhq.api.domain.Question;
import ai.interviewhq.api.domain.QuestionType;
import ai.interviewhq.api.domain.SourceSeed;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

final class ItemMapper {

    private ItemMapper() {
    }

    static Map<String, AttributeValue> fromQuestion(Question question) {
        Map<String, AttributeValue> item = new LinkedHashMap<>();
        item.put(Keys.PK, Attrs.s(Keys.questionPk(question.getQuestionId())));
        item.put(Keys.SK, Attrs.s(Keys.ENTITY));
        item.put("entityType", Attrs.s(Keys.ENTITY_QUESTION));
        Attrs.putS(item, "questionId", question.getQuestionId());
        Attrs.putS(item, "questionText", question.getQuestionText());
        Attrs.putS(item, "questionDescription", question.getQuestionDescription());
        if (question.getQuestionType() != null) {
            Attrs.putS(item, "questionType", question.getQuestionType().canonical());
        }
        Attrs.putStringList(item, "topics", question.getTopics());
        Attrs.putS(item, "company", question.getCompany());
        Attrs.putS(item, "sourceUrl", question.getSourceUrl());
        Attrs.putS(item, "problemUrl", question.getProblemUrl());
        Attrs.putS(item, "sourceName", question.getSourceName());
        Attrs.putS(item, "experienceId", question.getExperienceId());
        Attrs.putInstant(item, "postedAt", question.getPostedAt());
        Attrs.putInstant(item, "crawledAt", question.getCrawledAt());
        Attrs.putN(item, "confidence", question.getConfidence());
        Attrs.putN(item, "questionSpecificity", question.getQuestionSpecificity());
        Attrs.putInstant(item, "createdAt", question.getCreatedAt());
        Attrs.putInstant(item, "updatedAt", question.getUpdatedAt());

        Instant sortTime = question.getPostedAt() != null ? question.getPostedAt() : question.getCreatedAt();
        if (question.getCompany() != null && !question.getCompany().isBlank()) {
            item.put(Keys.GSI1PK, Attrs.s(Keys.gsi1pkCompany(question.getCompany())));
            item.put(Keys.GSI1SK, Attrs.s(Keys.gsiSk(sortTime, question.getQuestionId())));
        }
        if (question.getQuestionType() != null) {
            item.put(Keys.GSI2PK, Attrs.s(Keys.gsi2pkType(question.getQuestionType().canonical())));
            item.put(Keys.GSI2SK, Attrs.s(Keys.gsiSk(sortTime, question.getQuestionId())));
        }
        if (question.getSourceName() != null && !question.getSourceName().isBlank()) {
            item.put(Keys.GSI3PK, Attrs.s(Keys.gsi3pkSource(question.getSourceName())));
            item.put(Keys.GSI3SK, Attrs.s(Keys.gsiSk(sortTime, question.getQuestionId())));
        }
        item.put(Keys.GSI4PK, Attrs.s(Keys.gsi4pkEntityType(Keys.ENTITY_QUESTION)));
        item.put(Keys.GSI4SK, Attrs.s(Keys.gsiSk(sortTime, question.getQuestionId())));
        return item;
    }

    static Question toQuestion(Map<String, AttributeValue> item) {
        Question question = new Question();
        question.setQuestionId(Attrs.getS(item, "questionId"));
        question.setQuestionText(Attrs.getS(item, "questionText"));
        question.setQuestionDescription(Attrs.getS(item, "questionDescription"));
        String type = Attrs.getS(item, "questionType");
        if (type != null) {
            question.setQuestionType(QuestionType.fromCanonical(type));
        }
        question.setTopics(Attrs.getStringList(item, "topics"));
        question.setCompany(Attrs.getS(item, "company"));
        question.setSourceUrl(Attrs.getS(item, "sourceUrl"));
        question.setProblemUrl(Attrs.getS(item, "problemUrl"));
        question.setSourceName(Attrs.getS(item, "sourceName"));
        question.setExperienceId(Attrs.getS(item, "experienceId"));
        question.setPostedAt(Attrs.getInstant(item, "postedAt"));
        question.setCrawledAt(Attrs.getInstant(item, "crawledAt"));
        question.setConfidence(Attrs.getDouble(item, "confidence"));
        question.setQuestionSpecificity(Attrs.getDouble(item, "questionSpecificity"));
        question.setCreatedAt(Attrs.getInstant(item, "createdAt"));
        question.setUpdatedAt(Attrs.getInstant(item, "updatedAt"));
        return question;
    }

    static Map<String, AttributeValue> fromExperience(Experience experience) {
        Map<String, AttributeValue> item = new LinkedHashMap<>();
        item.put(Keys.PK, Attrs.s(Keys.experiencePk(experience.getExperienceId())));
        item.put(Keys.SK, Attrs.s(Keys.ENTITY));
        item.put("entityType", Attrs.s(Keys.ENTITY_EXPERIENCE));
        Attrs.putS(item, "experienceId", experience.getExperienceId());
        Attrs.putS(item, "sourceName", experience.getSourceName());
        Attrs.putS(item, "sourceUrl", experience.getSourceUrl());
        Attrs.putS(item, "company", experience.getCompany());
        Attrs.putS(item, "title", experience.getTitle());
        Attrs.putS(item, "author", experience.getAuthor());
        Attrs.putS(item, "contentHash", experience.getContentHash());
        Attrs.putInstant(item, "postedAt", experience.getPostedAt());
        Attrs.putInstant(item, "crawledAt", experience.getCrawledAt());
        Attrs.putInstant(item, "createdAt", experience.getCreatedAt());
        Attrs.putInstant(item, "updatedAt", experience.getUpdatedAt());
        Instant sortTime = experience.getPostedAt() != null ? experience.getPostedAt() : experience.getCreatedAt();
        item.put(Keys.GSI4PK, Attrs.s(Keys.gsi4pkEntityType(Keys.ENTITY_EXPERIENCE)));
        item.put(Keys.GSI4SK, Attrs.s(Keys.gsiSk(sortTime, experience.getExperienceId())));
        return item;
    }

    static Experience toExperience(Map<String, AttributeValue> item) {
        Experience experience = new Experience();
        experience.setExperienceId(Attrs.getS(item, "experienceId"));
        experience.setSourceName(Attrs.getS(item, "sourceName"));
        experience.setSourceUrl(Attrs.getS(item, "sourceUrl"));
        experience.setCompany(Attrs.getS(item, "company"));
        experience.setTitle(Attrs.getS(item, "title"));
        experience.setAuthor(Attrs.getS(item, "author"));
        experience.setContentHash(Attrs.getS(item, "contentHash"));
        experience.setPostedAt(Attrs.getInstant(item, "postedAt"));
        experience.setCrawledAt(Attrs.getInstant(item, "crawledAt"));
        experience.setCreatedAt(Attrs.getInstant(item, "createdAt"));
        experience.setUpdatedAt(Attrs.getInstant(item, "updatedAt"));
        return experience;
    }

    static Map<String, AttributeValue> fromExperienceQuestion(ExperienceQuestion link) {
        Map<String, AttributeValue> item = new LinkedHashMap<>();
        item.put(Keys.PK, Attrs.s(Keys.experiencePk(link.getExperienceId())));
        item.put(Keys.SK, Attrs.s(Keys.experienceQuestionSk(link.getQuestionId())));
        item.put("entityType", Attrs.s(Keys.ENTITY_EXPERIENCE_QUESTION));
        Attrs.putS(item, "experienceId", link.getExperienceId());
        Attrs.putS(item, "questionId", link.getQuestionId());
        return item;
    }

    static ExperienceQuestion toExperienceQuestion(Map<String, AttributeValue> item) {
        return new ExperienceQuestion(Attrs.getS(item, "experienceId"), Attrs.getS(item, "questionId"));
    }

    static Map<String, AttributeValue> fromSourceSeed(SourceSeed seed) {
        Map<String, AttributeValue> item = new LinkedHashMap<>();
        item.put(Keys.PK, Attrs.s(Keys.sourcePk(seed.getSourceId())));
        item.put(Keys.SK, Attrs.s(Keys.seedSk(seed.getSeedId())));
        item.put("entityType", Attrs.s(Keys.ENTITY_SOURCE_SEED));
        Attrs.putS(item, "sourceId", seed.getSourceId());
        Attrs.putS(item, "seedId", seed.getSeedId());
        Attrs.putS(item, "name", seed.getName());
        Attrs.putS(item, "url", seed.getUrl());
        Attrs.putBool(item, "enabled", seed.isEnabled());
        Attrs.putS(item, "crawlStrategy", seed.getCrawlStrategy());
        Attrs.putN(item, "crawlIntervalMinutes", seed.getCrawlIntervalMinutes());
        Attrs.putInstant(item, "lastCrawledAt", seed.getLastCrawledAt());
        Attrs.putInstant(item, "createdAt", seed.getCreatedAt());
        Attrs.putInstant(item, "updatedAt", seed.getUpdatedAt());
        Instant sortTime = seed.getUpdatedAt() != null ? seed.getUpdatedAt() : seed.getCreatedAt();
        item.put(Keys.GSI4PK, Attrs.s(Keys.gsi4pkEntityType(Keys.ENTITY_SOURCE_SEED)));
        item.put(Keys.GSI4SK, Attrs.s(Keys.gsiSk(sortTime, seed.getSourceId() + "#" + seed.getSeedId())));
        return item;
    }

    static SourceSeed toSourceSeed(Map<String, AttributeValue> item) {
        SourceSeed seed = new SourceSeed();
        seed.setSourceId(Attrs.getS(item, "sourceId"));
        seed.setSeedId(Attrs.getS(item, "seedId"));
        seed.setName(Attrs.getS(item, "name"));
        seed.setUrl(Attrs.getS(item, "url"));
        Boolean enabled = Attrs.getBool(item, "enabled");
        seed.setEnabled(enabled == null || enabled);
        seed.setCrawlStrategy(Attrs.getS(item, "crawlStrategy"));
        seed.setCrawlIntervalMinutes(Attrs.getInt(item, "crawlIntervalMinutes"));
        seed.setLastCrawledAt(Attrs.getInstant(item, "lastCrawledAt"));
        seed.setCreatedAt(Attrs.getInstant(item, "createdAt"));
        seed.setUpdatedAt(Attrs.getInstant(item, "updatedAt"));
        return seed;
    }

    static Map<String, AttributeValue> fromCrawlRun(CrawlRun run) {
        Map<String, AttributeValue> item = new LinkedHashMap<>();
        item.put(Keys.PK, Attrs.s(Keys.crawlRunPk(run.getRunId())));
        item.put(Keys.SK, Attrs.s(Keys.ENTITY));
        item.put("entityType", Attrs.s(Keys.ENTITY_CRAWL_RUN));
        Attrs.putS(item, "runId", run.getRunId());
        Attrs.putInstant(item, "startedAt", run.getStartedAt());
        Attrs.putInstant(item, "completedAt", run.getCompletedAt());
        if (run.getStatus() != null) {
            Attrs.putS(item, "status", run.getStatus().canonical());
        }
        Attrs.putN(item, "sourcesAttempted", run.getSourcesAttempted());
        Attrs.putN(item, "pagesDiscovered", run.getPagesDiscovered());
        Attrs.putN(item, "pagesCrawled", run.getPagesCrawled());
        Attrs.putN(item, "questionsExtracted", run.getQuestionsExtracted());
        Attrs.putN(item, "questionsAccepted", run.getQuestionsAccepted());
        Attrs.putN(item, "questionsRejected", run.getQuestionsRejected());
        Attrs.putN(item, "errors", run.getErrors());
        Instant sortTime = run.getStartedAt();
        item.put(Keys.GSI4PK, Attrs.s(Keys.gsi4pkEntityType(Keys.ENTITY_CRAWL_RUN)));
        item.put(Keys.GSI4SK, Attrs.s(Keys.gsiSk(sortTime, run.getRunId())));
        return item;
    }

    static CrawlRun toCrawlRun(Map<String, AttributeValue> item) {
        CrawlRun run = new CrawlRun();
        run.setRunId(Attrs.getS(item, "runId"));
        run.setStartedAt(Attrs.getInstant(item, "startedAt"));
        run.setCompletedAt(Attrs.getInstant(item, "completedAt"));
        String status = Attrs.getS(item, "status");
        if (status != null) {
            run.setStatus(CrawlRunStatus.from(status));
        }
        run.setSourcesAttempted(Attrs.getInt(item, "sourcesAttempted"));
        run.setPagesDiscovered(Attrs.getInt(item, "pagesDiscovered"));
        run.setPagesCrawled(Attrs.getInt(item, "pagesCrawled"));
        run.setQuestionsExtracted(Attrs.getInt(item, "questionsExtracted"));
        run.setQuestionsAccepted(Attrs.getInt(item, "questionsAccepted"));
        run.setQuestionsRejected(Attrs.getInt(item, "questionsRejected"));
        run.setErrors(Attrs.getInt(item, "errors"));
        return run;
    }

    static Map<String, AttributeValue> fromCrawlPage(CrawlPage page) {
        Map<String, AttributeValue> item = new LinkedHashMap<>();
        item.put(Keys.PK, Attrs.s(Keys.pagePk(page.getUrlHash())));
        item.put(Keys.SK, Attrs.s(Keys.ENTITY));
        item.put("entityType", Attrs.s(Keys.ENTITY_CRAWL_PAGE));
        Attrs.putS(item, "url", page.getUrl());
        Attrs.putS(item, "urlHash", page.getUrlHash());
        Attrs.putS(item, "sourceId", page.getSourceId());
        Attrs.putN(item, "httpStatus", page.getHttpStatus());
        Attrs.putS(item, "contentHash", page.getContentHash());
        Attrs.putInstant(item, "lastCrawledAt", page.getLastCrawledAt());
        Attrs.putInstant(item, "firstSeenAt", page.getFirstSeenAt());
        Attrs.putInstant(item, "lastModifiedAt", page.getLastModifiedAt());
        if (page.getCrawlStatus() != null) {
            Attrs.putS(item, "crawlStatus", page.getCrawlStatus().canonical());
        }
        Instant sortTime = page.getLastCrawledAt() != null ? page.getLastCrawledAt() : page.getFirstSeenAt();
        item.put(Keys.GSI4PK, Attrs.s(Keys.gsi4pkEntityType(Keys.ENTITY_CRAWL_PAGE)));
        item.put(Keys.GSI4SK, Attrs.s(Keys.gsiSk(sortTime, page.getUrlHash())));
        return item;
    }

    static CrawlPage toCrawlPage(Map<String, AttributeValue> item) {
        CrawlPage page = new CrawlPage();
        page.setUrl(Attrs.getS(item, "url"));
        page.setUrlHash(Attrs.getS(item, "urlHash"));
        page.setSourceId(Attrs.getS(item, "sourceId"));
        page.setHttpStatus(Attrs.getInt(item, "httpStatus"));
        page.setContentHash(Attrs.getS(item, "contentHash"));
        page.setLastCrawledAt(Attrs.getInstant(item, "lastCrawledAt"));
        page.setFirstSeenAt(Attrs.getInstant(item, "firstSeenAt"));
        page.setLastModifiedAt(Attrs.getInstant(item, "lastModifiedAt"));
        String status = Attrs.getS(item, "crawlStatus");
        if (status != null) {
            page.setCrawlStatus(CrawlPageStatus.from(status));
        }
        return page;
    }
}
