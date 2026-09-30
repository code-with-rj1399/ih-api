package ai.interviewhq.api.experience.mapper;
import org.junit.jupiter.api.Test; import java.util.Map; import static org.junit.jupiter.api.Assertions.*;
class ExperienceMapperTest {
 @Test void mapsPublicExperienceFields(){var r=new ExperienceMapper().toResponse(Map.of("id","7","title","SDE Interview","company","Acme","role","SWE","postedAt","2026-09-28T00:00:00Z"));assertEquals(7,r.id());assertEquals("Acme",r.company());assertEquals("SWE",r.role());assertNotNull(r.postedAt());}
 @Test void optionalFieldsRemainNull(){var r=new ExperienceMapper().toResponse(Map.of("id","7","title","SDE Interview"));assertNull(r.company());assertNull(r.location());assertNull(r.createdAt());}
}
