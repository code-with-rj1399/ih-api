package ai.interviewhq.api.common.exception;

import ai.interviewhq.api.common.response.ApiErrorResponse;
import org.springframework.http.HttpStatus;
import java.util.List;

public class PublicApiException extends RuntimeException {
    private final String code;
    private final HttpStatus status;
    private final List<ApiErrorResponse.ErrorDetail> details;
    public PublicApiException(String code, String message, HttpStatus status) { this(code,message,status,List.of()); }
    public PublicApiException(String code, String message, HttpStatus status, List<ApiErrorResponse.ErrorDetail> details) {
        super(message); this.code=code; this.status=status; this.details=details==null?List.of():List.copyOf(details);
    }
    public String getCode(){return code;} public HttpStatus getStatus(){return status;} public List<ApiErrorResponse.ErrorDetail> getDetails(){return details;}
}
