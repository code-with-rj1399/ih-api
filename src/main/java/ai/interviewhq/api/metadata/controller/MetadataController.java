package ai.interviewhq.api.metadata.controller;

import ai.interviewhq.api.common.response.ApiCollectionResponse;
import ai.interviewhq.api.metadata.dto.MetadataItemResponse;
import ai.interviewhq.api.metadata.service.MetadataService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/meta")
@Validated
public class MetadataController {
    private final MetadataService service;

    public MetadataController(MetadataService service) {
        this.service = service;
    }

    @GetMapping("/companies")
    public ApiCollectionResponse<MetadataItemResponse> companies(
            @RequestParam(required = false) @Min(1) @Max(100) Integer limit,
            @RequestParam(required = false) @Size(max = 2048) String cursor) {
        return service.companies(limit, cursor);
    }

    @GetMapping("/question-types")
    public ApiCollectionResponse<MetadataItemResponse> questionTypes(
            @RequestParam(required = false) @Min(1) @Max(100) Integer limit,
            @RequestParam(required = false) @Size(max = 2048) String cursor) {
        return service.questionTypes(limit, cursor);
    }
}
