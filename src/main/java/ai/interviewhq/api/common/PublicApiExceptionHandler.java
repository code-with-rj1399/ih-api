package ai.interviewhq.api.common;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.List;

@RestControllerAdvice(basePackages = "ai.interviewhq.api")
public class PublicApiExceptionHandler {

    @ExceptionHandler(PublicApiException.class)
    public org.springframework.http.ResponseEntity<ApiErrorResponse> handlePublicApiException(
            PublicApiException exception,
            HttpServletRequest request) {
        return org.springframework.http.ResponseEntity
                .status(exception.getStatus())
                .body(error(exception.getCode(), exception.getMessage(), exception.getDetails(), request));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public org.springframework.http.ResponseEntity<ApiErrorResponse> handleValidation(
            MethodArgumentNotValidException exception,
            HttpServletRequest request) {
        List<ApiErrorResponse.ErrorDetail> details = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> new ApiErrorResponse.ErrorDetail(error.getField(), error.getDefaultMessage()))
                .toList();

        return org.springframework.http.ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(error("VALIDATION_ERROR", "Request validation failed", details, request));
    }

    @ExceptionHandler(Exception.class)
    public org.springframework.http.ResponseEntity<ApiErrorResponse> handleUnexpected(
            Exception exception,
            HttpServletRequest request) {
        return org.springframework.http.ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(error(
                        "INTERNAL_ERROR",
                        "An unexpected error occurred",
                        List.of(),
                        request
                ));
    }

    private ApiErrorResponse error(
            String code,
            String message,
            List<ApiErrorResponse.ErrorDetail> details,
            HttpServletRequest request) {
        return new ApiErrorResponse(
                new ApiErrorResponse.ErrorBody(code, message, details),
                Instant.now(),
                request.getRequestURI()
        );
    }
}
