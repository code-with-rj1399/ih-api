package ai.interviewhq.api.common.pagination;
import java.util.Map;
public record OpaqueCursor(Map<String,String> lastEvaluatedKey) { }
