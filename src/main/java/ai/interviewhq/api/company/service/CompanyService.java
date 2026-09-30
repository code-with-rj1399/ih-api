package ai.interviewhq.api.company.service;

import ai.interviewhq.api.common.exception.PublicApiException;
import ai.interviewhq.api.common.pagination.OpaqueCursorCodec;
import ai.interviewhq.api.common.pagination.PaginationRequest;
import ai.interviewhq.api.common.response.ApiCollectionResponse;
import ai.interviewhq.api.company.dto.CompanyResponse;
import ai.interviewhq.api.company.dto.MergeCompanyResponse;
import ai.interviewhq.api.company.dto.UpdateCompanyRequest;
import ai.interviewhq.api.company.repository.CompanyRepository;
import ai.interviewhq.api.experience.repository.ExperienceRepository;
import ai.interviewhq.api.infrastructure.dynamodb.DynamoDbPage;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;

@Service
public class CompanyService {
    private final CompanyRepository companies;
    private final ExperienceRepository experiences;
    private final OpaqueCursorCodec cursors;

    public CompanyService(
            CompanyRepository companies,
            ExperienceRepository experiences,
            OpaqueCursorCodec cursors) {
        this.companies = companies;
        this.experiences = experiences;
        this.cursors = cursors;
    }

    public ApiCollectionResponse<CompanyResponse> list(Integer limit, String cursor) {
        PaginationRequest page = PaginationRequest.of(limit, cursor);
        DynamoDbPage<Map<String, Object>> result =
                companies.list(page.limit(), cursors.decode(page.cursor()));

        return new ApiCollectionResponse<>(
                result.items().stream().map(this::toResponse).toList(),
                new ApiCollectionResponse.Pagination(
                        page.limit(),
                        cursors.encode(result.lastEvaluatedKey()),
                        result.hasMore()));
    }

    public CompanyResponse find(String companyName) {
        return companies.find(companyName)
                .map(this::toResponse)
                .orElseThrow(() -> notFound("Company not found"));
    }

    public CompanyResponse update(String currentCompanyName, UpdateCompanyRequest request) {
        String current = normalize(currentCompanyName);
        String updated = request.companyName().trim();

        if (current == null) throw notFound("Company not found");
        if (normalize(updated) == null) throw badRequest("Company name is required");

        Map<String, Object> existing = companies.find(currentCompanyName)
                .orElseThrow(() -> notFound("Company not found"));

        if (!current.equals(normalize(updated)) && companies.find(updated).isPresent()) {
            throw new PublicApiException(
                    "COMPANY_ALREADY_EXISTS",
                    "A company with the new name already exists. Use merge instead.",
                    HttpStatus.CONFLICT);
        }

        existing = new java.util.LinkedHashMap<>(existing);
        existing.put("companyName", updated);
        existing.put("website", request.website());
        existing.put("logoUrl", request.logoUrl());
        existing.put("description", request.description());
        existing.put("updatedAt", Instant.now().toString());

        if (!current.equals(normalize(updated))) {
            companies.delete(currentCompanyName);
        }
        companies.save(existing);
        return toResponse(existing);
    }

    public MergeCompanyResponse merge(String sourceCompany, String targetCompany) {
        String source = requireName(sourceCompany, "Source company is required");
        String target = requireName(targetCompany, "Target company is required");

        if (normalize(source).equals(normalize(target))) {
            throw badRequest("Source and target companies must be different");
        }

        CompanyResponse sourceRecord = find(source);
        CompanyResponse targetRecord = find(target);

        int moved = experiences.mergeCompany(sourceRecord.companyName(), targetRecord.companyName());
        companies.delete(sourceRecord.companyName());

        return new MergeCompanyResponse(
                sourceRecord.companyName(),
                targetRecord.companyName(),
                moved,
                "Company merged successfully. All experiences were moved to the target company.");
    }

    private CompanyResponse toResponse(Map<String, Object> data) {
        return new CompanyResponse(
                asString(data, "companyName"),
                asString(data, "website"),
                asString(data, "logoUrl"),
                asString(data, "description"),
                asInstant(data, "createdAt"),
                asInstant(data, "updatedAt"));
    }

    private static String asString(Map<String, Object> data, String key) {
        Object value = data.get(key);
        return value == null ? null : String.valueOf(value);
    }

    private static Instant asInstant(Map<String, Object> data, String key) {
        String value = asString(data, key);
        return value == null ? null : Instant.parse(value);
    }

    private static String requireName(String value, String message) {
        if (value == null || value.isBlank()) throw badRequest(message);
        return value.trim();
    }

    private static String normalize(String value) {
        if (value == null || value.isBlank()) return null;
        return value.trim().toLowerCase(java.util.Locale.ROOT).replaceAll("\\s+", " ");
    }

    private static PublicApiException notFound(String message) {
        return new PublicApiException("NOT_FOUND", message, HttpStatus.NOT_FOUND);
    }

    private static PublicApiException badRequest(String message) {
        return new PublicApiException("INVALID_REQUEST", message, HttpStatus.BAD_REQUEST);
    }
}
