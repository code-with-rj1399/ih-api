package ai.interviewhq.api.domain;

import java.time.Instant;

/**
 * Single-table key construction. The crawler must not invent these; ih-api owns them.
 *
 * <pre>
 * interview-hq
 * ├── QUESTION#<hash>      / ENTITY
 * ├── EXPERIENCE#<id>      / ENTITY
 * ├── EXPERIENCE#<id>      / QUESTION#<hash>
 * ├── SOURCE#<source>      / SEED#<id>
 * ├── CRAWL_RUN#<runId>    / ENTITY
 * └── PAGE#<urlHash>       / ENTITY
 * </pre>
 */
public final class Keys {

    public static final String ENTITY = "ENTITY";
    public static final String PK = "PK";
    public static final String SK = "SK";
    public static final String GSI1 = "GSI1";
    public static final String GSI2 = "GSI2";
    public static final String GSI3 = "GSI3";
    public static final String GSI4 = "GSI4";
    public static final String GSI1PK = "GSI1PK";
    public static final String GSI1SK = "GSI1SK";
    public static final String GSI2PK = "GSI2PK";
    public static final String GSI2SK = "GSI2SK";
    public static final String GSI3PK = "GSI3PK";
    public static final String GSI3SK = "GSI3SK";
    public static final String GSI4PK = "GSI4PK";
    public static final String GSI4SK = "GSI4SK";

    public static final String ENTITY_QUESTION = "Question";
    public static final String ENTITY_EXPERIENCE = "Experience";
    public static final String ENTITY_SOURCE_SEED = "SourceSeed";
    public static final String ENTITY_CRAWL_RUN = "CrawlRun";
    public static final String ENTITY_CRAWL_PAGE = "CrawlPage";
    public static final String ENTITY_EXPERIENCE_QUESTION = "ExperienceQuestion";

    private Keys() {
    }

    public static String questionPk(String questionId) {
        return "QUESTION#" + questionId;
    }

    public static String experiencePk(String experienceId) {
        return "EXPERIENCE#" + experienceId;
    }

    public static String experienceQuestionSk(String questionId) {
        return "QUESTION#" + questionId;
    }

    public static String sourcePk(String sourceId) {
        return "SOURCE#" + sourceId;
    }

    public static String seedSk(String seedId) {
        return "SEED#" + seedId;
    }

    public static String crawlRunPk(String runId) {
        return "CRAWL_RUN#" + runId;
    }

    public static String pagePk(String urlHash) {
        return "PAGE#" + urlHash;
    }

    public static String gsi1pkCompany(String company) {
        return "COMPANY#" + company;
    }

    public static String gsi2pkType(String questionType) {
        return "TYPE#" + questionType;
    }

    public static String gsi3pkSource(String sourceName) {
        return "SOURCE#" + sourceName;
    }

    public static String gsi4pkEntityType(String entityType) {
        return "ENTITY_TYPE#" + entityType;
    }

    /**
     * Lexicographically sortable ISO-8601 timestamp plus id, used as GSI sort keys
     * so "recent first" is a reverse query.
     */
    public static String gsiSk(Instant timestamp, String id) {
        String ts = timestamp == null ? "0000-00-00T00:00:00Z" : timestamp.toString();
        return ts + "#" + id;
    }
}
