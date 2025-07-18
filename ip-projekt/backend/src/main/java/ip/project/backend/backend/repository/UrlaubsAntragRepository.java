package ip.project.backend.backend.repository;

import ip.project.backend.backend.model.UrlaubsAntrag;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface UrlaubsAntragRepository extends MongoRepository<UrlaubsAntrag, String> {
    Optional<UrlaubsAntrag> findByAntragsId(Integer antragsId);
    Optional<UrlaubsAntrag> findByStatus(String status);
    List<UrlaubsAntrag> findAllByEmployeeId(Integer employeeId);
    void deleteAllByEmployeeId(Integer employeeId);
}
