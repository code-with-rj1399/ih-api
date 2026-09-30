package ai.interviewhq.api.company.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateCompanyRequest(@NotBlank @Size(max = 200) String companyName) {}
