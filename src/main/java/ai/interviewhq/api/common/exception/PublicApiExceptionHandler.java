package ai.interviewhq.api.common.exception;

import ai.interviewhq.api.common.response.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.time.Instant;
import java.util.List;

@RestControllerAdvice(basePackages = "ai.interviewhq.api")
public class PublicApiExceptionHandler {
    @ExceptionHandler(PublicApiException.class)
    public ResponseEntity<ApiErrorResponse> handlePublicApiException(PublicApiException e,HttpServletRequest r){return ResponseEntity.status(e.getStatus()).body(error(e.getCode(),e.getMessage(),e.getDetails(),r));}
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(MethodArgumentNotValidException e,HttpServletRequest r){List<ApiErrorResponse.ErrorDetail>d=e.getBindingResult().getFieldErrors().stream().map(x->new ApiErrorResponse.ErrorDetail(x.getField(),x.getDefaultMessage())).toList();return ResponseEntity.badRequest().body(error("VALIDATION_ERROR","Request validation failed",d,r));}
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleConstraintViolation(ConstraintViolationException e,HttpServletRequest r){List<ApiErrorResponse.ErrorDetail>d=e.getConstraintViolations().stream().map(x->new ApiErrorResponse.ErrorDetail(x.getPropertyPath().toString(),x.getMessage())).toList();return ResponseEntity.badRequest().body(error("VALIDATION_ERROR","Request validation failed",d,r));}
    @ExceptionHandler(PaginationCursorException.class)
    public ResponseEntity<ApiErrorResponse> handleCursor(PaginationCursorException e,HttpServletRequest r){return ResponseEntity.badRequest().body(error("INVALID_CURSOR",e.getMessage(),List.of(),r));}
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalArgument(IllegalArgumentException e,HttpServletRequest r){return ResponseEntity.badRequest().body(error("VALIDATION_ERROR",e.getMessage(),List.of(),r));}
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpected(Exception e,HttpServletRequest r){return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error("INTERNAL_ERROR","An unexpected error occurred",List.of(),r));}
    private ApiErrorResponse error(String code,String message,List<ApiErrorResponse.ErrorDetail>d,HttpServletRequest r){return new ApiErrorResponse(new ApiErrorResponse.ErrorBody(code,message,d),Instant.now(),r.getRequestURI());}
}
