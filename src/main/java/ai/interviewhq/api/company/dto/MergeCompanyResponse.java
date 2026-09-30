package ai.interviewhq.api.company.dto;

public record MergeCompanyResponse(
        String sourceCompany,
        String targetCompany,
        int experiencesMoved,
        String message) {}
