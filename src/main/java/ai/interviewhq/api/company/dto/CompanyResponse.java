package ai.interviewhq.api.company.dto;

import java.time.Instant;

public record CompanyResponse(
        String companyName,
        String website,
        String logoUrl,
        String description,
        Instant createdAt,
        Instant updatedAt) {}
