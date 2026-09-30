package ai.interviewhq.api;

import ai.interviewhq.api.common.exception.PublicApiException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/test-support")
class PublicApiErrorProbeController {

    @GetMapping("/bad-request")
    void badRequest() {
        throw new PublicApiException(
                "BAD_REQUEST",
                "Example bad request",
                HttpStatus.BAD_REQUEST
        );
    }

    @GetMapping("/not-found")
    void notFound() {
        throw new PublicApiException(
                "NOT_FOUND",
                "Example resource not found",
                HttpStatus.NOT_FOUND
        );
    }

    @GetMapping("/internal-error")
    void internalError() {
        throw new IllegalStateException("internal details");
    }

    @GetMapping("/validation")
    void validation(@RequestParam @org.springframework.validation.annotation.Validated String value) {
    }
}
