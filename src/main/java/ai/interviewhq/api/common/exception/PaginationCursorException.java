package ai.interviewhq.api.common.exception;

public class PaginationCursorException extends RuntimeException {
    public PaginationCursorException(String message) { super(message); }
    public PaginationCursorException(String message, Throwable cause) { super(message, cause); }
}
