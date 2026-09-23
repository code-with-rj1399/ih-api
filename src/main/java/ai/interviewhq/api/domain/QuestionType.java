package ai.interviewhq.api.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Canonical question types stored in DynamoDB. DSA / algorithm / LeetCode coding
 * problems map to {@link #CODING} — there is no separate DSA type.
 */
public enum QuestionType {
    SYSTEM_DESIGN("System Design"),
    CODING("Coding"),
    DATABASE("Database"),
    LLD("LLD"),
    CLOUD("Cloud"),
    SECURITY("Security"),
    DEVOPS("DevOps"),
    AI_ML("AI/ML"),
    DATA_ENGINEERING("Data Engineering"),
    DISTRIBUTED_SYSTEMS("Distributed Systems"),
    NETWORKING("Networking"),
    OPERATING_SYSTEMS("Operating Systems"),
    PROGRAMMING_LANGUAGE("Programming Language"),
    WEB_FRONTEND("Web Frontend"),
    MOBILE("Mobile"),
    TESTING("Testing"),
    TECHNICAL_CONCEPT("Technical Concept");

    private static final Map<String, QuestionType> BY_CANONICAL = Arrays.stream(values())
            .collect(Collectors.toMap(type -> normalize(type.canonical), Function.identity()));

    static {
        BY_CANONICAL.put("dsa", CODING);
        BY_CANONICAL.put("algorithm", CODING);
        BY_CANONICAL.put("algorithms", CODING);
        BY_CANONICAL.put("leetcode", CODING);
        BY_CANONICAL.put("data structures", CODING);
        BY_CANONICAL.put("data structure", CODING);
        BY_CANONICAL.put("machine learning", AI_ML);
        BY_CANONICAL.put("ml", AI_ML);
        BY_CANONICAL.put("ai", AI_ML);
        BY_CANONICAL.put("frontend", WEB_FRONTEND);
        BY_CANONICAL.put("front end", WEB_FRONTEND);
        BY_CANONICAL.put("os", OPERATING_SYSTEMS);
        BY_CANONICAL.put("low level design", LLD);
        BY_CANONICAL.put("low-level design", LLD);
    }

    private final String canonical;

    QuestionType(String canonical) {
        this.canonical = canonical;
    }

    @JsonValue
    public String canonical() {
        return canonical;
    }

    @JsonCreator
    public static QuestionType fromCanonical(String value) {
        return tryParse(value)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Unknown questionType '" + value + "'. Allowed: "
                                + Arrays.stream(values()).map(QuestionType::canonical).toList()));
    }

    public static Optional<QuestionType> tryParse(String value) {
        if (value == null || value.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(BY_CANONICAL.get(normalize(value)));
    }

    private static String normalize(String value) {
        return value.trim().toLowerCase(Locale.ROOT).replace('_', ' ').replace('-', ' ')
                .replaceAll("\\s+", " ");
    }
}
