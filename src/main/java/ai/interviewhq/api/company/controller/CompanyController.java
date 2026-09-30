package ai.interviewhq.api.company.controller;

import ai.interviewhq.api.common.response.ApiCollectionResponse;
import ai.interviewhq.api.common.response.ApiItemResponse;
import ai.interviewhq.api.company.dto.CompanyResponse;
import ai.interviewhq.api.company.dto.MergeCompanyRequest;
import ai.interviewhq.api.company.dto.MergeCompanyResponse;
import ai.interviewhq.api.company.dto.UpdateCompanyRequest;
import ai.interviewhq.api.company.service.CompanyService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/companies")
@Validated
public class CompanyController {
    private final CompanyService companies;

    public CompanyController(CompanyService companies) {
        this.companies = companies;
    }

    @GetMapping
    public ApiCollectionResponse<CompanyResponse> list(
            @RequestParam(required = false) @Min(1) @Max(100) Integer limit,
            @RequestParam(required = false) @Size(max = 2048) String cursor) {
        return companies.list(limit, cursor);
    }

    @GetMapping("/{companyName}")
    public ApiItemResponse<CompanyResponse> get(
            @PathVariable @Size(max = 200) String companyName) {
        return new ApiItemResponse<>(companies.find(companyName));
    }

    @PutMapping("/{companyName}")
    public ApiItemResponse<CompanyResponse> update(
            @PathVariable @Size(max = 200) String companyName,
            @Valid @RequestBody UpdateCompanyRequest request) {
        return new ApiItemResponse<>(companies.update(companyName, request));
    }

    @PostMapping("/merge")
    public ApiItemResponse<MergeCompanyResponse> merge(
            @Valid @RequestBody MergeCompanyRequest request) {
        return new ApiItemResponse<>(companies.merge(request.sourceCompany(), request.targetCompany()));
    }
}
