package ai.interviewhq.api.common.response;
import java.time.Instant;
import java.util.List;
public record ApiErrorResponse(ErrorBody error, Instant timestamp, String path) {
    public record ErrorBody(String code,String message,List<ErrorDetail> details) { }
    public record ErrorDetail(String field,String message) { }
}
