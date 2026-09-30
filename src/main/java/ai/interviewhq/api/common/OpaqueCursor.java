package ai.interviewhq.api.common;

import java.util.Map;

public record OpaqueCursor(Map<String, String> lastEvaluatedKey) {
}
