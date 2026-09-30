package ai.interviewhq.api.company.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateCompanyRequest(
        @NotBlank @Size(max = 200) String companyName,
        @Size(max = 2048) String website,
        @Size(max = 2048) String logoUrl,
        @Size(max = 5000) String description) {}
