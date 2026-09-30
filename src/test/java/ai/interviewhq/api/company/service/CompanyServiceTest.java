package ai.interviewhq.api.company.service;

import ai.interviewhq.api.common.pagination.OpaqueCursorCodec;
import ai.interviewhq.api.company.dto.MergeCompanyResponse;
import ai.interviewhq.api.company.repository.CompanyRepository;
import ai.interviewhq.api.experience.repository.ExperienceRepository;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CompanyServiceTest {

    @Test
    void mergeMovesExperiencesAndDeletesSourceCompany() {
        CompanyRepository companies = mock(CompanyRepository.class);
        ExperienceRepository experiences = mock(ExperienceRepository.class);
        OpaqueCursorCodec cursors = mock(OpaqueCursorCodec.class);

        when(companies.find("Google LLC")).thenReturn(Optional.of(Map.of(
                "name", "Google LLC", "slug", "google llc")));
        when(companies.find("Google")).thenReturn(Optional.of(Map.of(
                "name", "Google", "slug", "google")));
        when(experiences.mergeCompany("Google LLC", "Google")).thenReturn(3);

        CompanyService service = new CompanyService(companies, experiences, cursors);

        MergeCompanyResponse result = service.merge("Google LLC", "Google");

        assertEquals("Google LLC", result.sourceCompany());
        assertEquals("Google", result.targetCompany());
        assertEquals(3, result.experiencesMoved());
        verify(experiences).mergeCompany("Google LLC", "Google");
        verify(companies).delete("Google LLC");
    }

    @Test
    void mergeRejectsSameCompany() {
        CompanyRepository companies = mock(CompanyRepository.class);
        ExperienceRepository experiences = mock(ExperienceRepository.class);
        OpaqueCursorCodec cursors = mock(OpaqueCursorCodec.class);

        CompanyService service = new CompanyService(companies, experiences, cursors);

        assertThrows(RuntimeException.class, () -> service.merge("Google", "google"));
        verifyNoInteractions(companies, experiences);
    }
}
