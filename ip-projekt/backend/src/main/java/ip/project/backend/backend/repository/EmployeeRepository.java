package ip.project.backend.backend.repository;

import ip.project.backend.backend.model.Employee;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface EmployeeRepository extends MongoRepository<Employee, Integer> {
    Optional<Employee> findEmployeeByEmployeeId(Integer employeeId);
    List<Employee> findByRole_RoleId(Integer roleId);
}