package ai.interviewhq.api.experience.service;
import ai.interviewhq.api.common.exception.PublicApiException;
import ai.interviewhq.api.experience.dto.ExperienceResponse;
import ai.interviewhq.api.experience.mapper.ExperienceMapper;
import ai.interviewhq.api.experience.repository.ExperienceRepository;
import org.springframework.http.HttpStatus; import org.springframework.stereotype.Service;
@Service public class ExperienceService {
 private final ExperienceRepository repo; private final ExperienceMapper mapper;
 public ExperienceService(ExperienceRepository repo,ExperienceMapper mapper){this.repo=repo;this.mapper=mapper;}
 public ExperienceResponse find(Integer id){return repo.findById(id).map(mapper::toResponse).orElseThrow(()->new PublicApiException("NOT_FOUND","Interview experience not found",HttpStatus.NOT_FOUND));}
}
