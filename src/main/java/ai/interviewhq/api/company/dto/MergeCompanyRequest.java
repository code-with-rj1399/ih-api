package ai.interviewhq.api.company.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MergeCompanyRequest(
        @NotBlank @Size(max = 200) String sourceCompany,
        @NotBlank @Size(max = 200) String targetCompany) {}
