package ip.project.backend.backend.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ip.project.backend.backend.checker.PermissionManager;
import ip.project.backend.backend.mapper.UrlaubsAntragMapper;
import ip.project.backend.backend.model.Employee;
import ip.project.backend.backend.model.UrlaubsAntrag;
import ip.project.backend.backend.modeldto.UrlaubsAntragDto;
import ip.project.backend.backend.repository.EmployeeRepository;
import ip.project.backend.backend.repository.UrlaubsAntragRepository;

@Service
public class UrlaubsAntragService {
    private final Logger logger = LoggerFactory.getLogger(UrlaubsAntragService.class);
    private final UrlaubsAntragRepository urlaubsAntragRepository;
    private final PermissionManager permissionManager;

    private final String PATH = "/api/urlaubsantrag/review";
    private final EmployeeRepository employeeRepository;

    @Autowired
    public UrlaubsAntragService(UrlaubsAntragRepository urlaubsAntragRepository, PermissionManager permissionManager, EmployeeRepository employeeRepository) {
        this.urlaubsAntragRepository = urlaubsAntragRepository;
        this.permissionManager = permissionManager;
        this.employeeRepository = employeeRepository;
    }

    /**
     * Retrieves a vacation request by its ID.
     * This method checks if the vacation request exists and if the employee has permission to access it.
     *
     * @param employeeId The ID of the employee requesting the vacation request
     * @param id The ID of the vacation request to retrieve
     * @return An Optional containing the UrlaubsAntragDto if found, or an empty Optional if not found or permission denied
     */
    public Optional<UrlaubsAntragDto> getUrlaubsAntragById(Integer employeeId, Integer id) {
        Optional<UrlaubsAntrag> urlaubsAntrag = urlaubsAntragRepository.findByAntragsId(id);
        String permissions = permissionManager.findRequiredPermission(PATH, "PUT");
        Optional<Employee> employee = employeeRepository.findEmployeeByEmployeeId(employeeId);
        if(employee.isEmpty()) {
            logger.error("Employee with id {} not found", employeeId);
            return Optional.empty();
        }
        if (urlaubsAntrag.isEmpty()) {
            logger.error("Urlaubsantrag with id {} not found", id);
            return Optional.empty();
        }

        boolean isOwner = employeeId.equals(urlaubsAntrag.get().getEmployeeId());
        boolean hasReviewPermission = employee.get().getRolePermissions().contains(permissions);

        if(!isOwner && !hasReviewPermission) {
            logger.error("Employee with id {} does not have permission to access this Urlaubsantrag", employeeId);
            return Optional.empty();
        }

        return Optional.of(UrlaubsAntragMapper.INSTANCE.urlaubsAntragToUrlaubsAntragDto(urlaubsAntrag.get()));
    }

    /**
     * Retrieves all vacation requests for a specific employee.
     * This method fetches vacation requests based on the employee's ID and maps them to DTOs.
     *
     * @param employeeId The ID of the employee whose vacation requests are to be retrieved
     * @return A list of UrlaubsAntragDto objects representing the vacation requests for the specified employee
     */
    public List<UrlaubsAntragDto> getAllUrlaubsAntraegeByEmployeeId(Integer employeeId) {
        List<UrlaubsAntrag> antraege = urlaubsAntragRepository.findAllByEmployeeId(employeeId);
        List<UrlaubsAntragDto> antraegeDtos = new ArrayList<>();
        if(antraege.isEmpty()) {
            logger.info("No Urlaubsanträge found for employee with id {}", employeeId);
            return antraegeDtos;
        } else {
            for (UrlaubsAntrag antrag : antraege) {
                antraegeDtos.add(UrlaubsAntragMapper.INSTANCE.urlaubsAntragToUrlaubsAntragDto(antrag));
            }
            return antraegeDtos;
        }
    }

    /**
     * Retrieves all vacation requests.
     * This method fetches all vacation requests from the repository and maps them to DTOs.
     *
     * @return A list of UrlaubsAntragDto objects representing all vacation requests
     */
    public List<UrlaubsAntragDto> getAllUrlaubsAntraege() {
        List<UrlaubsAntrag> antraege = urlaubsAntragRepository.findAll();
        List<UrlaubsAntragDto> antraegeDtos = new ArrayList<>();
        if(antraege.isEmpty()) {
            logger.info("No Urlaubsanträge found");
            return antraegeDtos;
        } else {
            for (UrlaubsAntrag antrag : antraege) {
                antraegeDtos.add(UrlaubsAntragMapper.INSTANCE.urlaubsAntragToUrlaubsAntragDto(antrag));
            }
            return antraegeDtos;
        }
    }

    /**
     * Adds a new vacation request.
     * This method checks if the vacation request already exists based on the antragsId.
     * If it does not exist, it assigns a new antragsId based on the maximum existing antragsId.
     *
     * @param urlaubsAntragDto The UrlaubsAntragDto object containing the vacation request details
     * @return An empty Optional if the request was added successfully, or an Optional containing an error message if it already exists
     */
    public Optional<String> addUrlaubsAntrag(UrlaubsAntragDto urlaubsAntragDto) {
        if (urlaubsAntragDto.getAntragsId() == null) {
            // Finde die höchste antragsId und setze die neue antragsId auf maxId + 1
            Integer maxId = urlaubsAntragRepository.findAll()
                    .stream()
                    .map(UrlaubsAntrag::getAntragsId)
                    .filter(Objects::nonNull)
                    .max(Integer::compareTo)
                    .orElse(0);
            urlaubsAntragDto.setAntragsId(maxId + 1);
        } else {
            // Überprüfe, ob die antragsId bereits existiert
            if (urlaubsAntragRepository.findByAntragsId(urlaubsAntragDto.getAntragsId()).isPresent()) {
                logger.error("Urlaubsantrag with id {} already exists", urlaubsAntragDto.getAntragsId());
                return Optional.of("Urlaubsantrag already exists with id: " + urlaubsAntragDto.getAntragsId());
            }
        }

        UrlaubsAntrag urlaubsAntrag = UrlaubsAntragMapper.INSTANCE.urlaubsAntragDtoToUrlaubsAntrag(urlaubsAntragDto);
        urlaubsAntragRepository.save(urlaubsAntrag);
        logger.info("Urlaubsantrag created with ID: {}", urlaubsAntragDto.getAntragsId());
        return Optional.empty();
    }

    /**
     * Updates an existing vacation request.
     * This method checks if the vacation request exists before attempting to update it.
     *
     * @param urlaubsAntragDto The UrlaubsAntragDto object containing the updated vacation request details
     * @return An empty Optional if the update was successful, or an Optional containing an error message if the request was not found
     */
    public Optional<String> updateUrlaubsAntrag(UrlaubsAntragDto urlaubsAntragDto) {
        if (urlaubsAntragDto.getAntragsId() == null) {
            logger.error("Cannot update Urlaubsantrag: antragsId is null");
            return Optional.of("Urlaubsantrag ID is required for update");
        }
        
        Optional<UrlaubsAntrag> existingAntrag = urlaubsAntragRepository.findByAntragsId(urlaubsAntragDto.getAntragsId());
        if (existingAntrag.isEmpty()) {
            logger.error("Urlaubsantrag with id {} not found", urlaubsAntragDto.getAntragsId());
            return Optional.of("Urlaubsantrag not found with id: " + urlaubsAntragDto.getAntragsId());
        }

        // Existierenden Urlaubsantrag aktualisieren
        UrlaubsAntrag antragToUpdate = existingAntrag.get();
        antragToUpdate.setEmployeeId(urlaubsAntragDto.getEmployeeId());
        antragToUpdate.setStartDatum(urlaubsAntragDto.getStartDatum());
        antragToUpdate.setEndDatum(urlaubsAntragDto.getEndDatum());
        antragToUpdate.setStatus(urlaubsAntragDto.getStatus());
        antragToUpdate.setType(urlaubsAntragDto.getType());
        antragToUpdate.setGrund(urlaubsAntragDto.getGrund());
        antragToUpdate.setReviewDate(urlaubsAntragDto.getReviewDate());
        antragToUpdate.setReviewerId(urlaubsAntragDto.getReviewerId());
        antragToUpdate.setComment(urlaubsAntragDto.getComment());
        
        urlaubsAntragRepository.save(antragToUpdate);
        logger.info("Urlaubsantrag updated with ID: {}", urlaubsAntragDto.getAntragsId());
        return Optional.empty();
    }

    /**
     * Deletes a vacation request by its ID.
     * This method checks if the vacation request exists before attempting to delete it.
     *
     * @param id The ID of the vacation request to delete
     * @return An empty Optional if the deletion was successful, or an Optional containing an error message if the request was not found
     */
    public Optional<String> deleteUrlaubsAntrag(Integer id) {
        Optional<UrlaubsAntrag> urlaubsAntrag = urlaubsAntragRepository.findByAntragsId(id);
        if (urlaubsAntrag.isEmpty()) {
            logger.error("Urlaubsantrag with id {} not found", id);
            return Optional.of("Urlaubsantrag not found with id: " + id);
        }

        urlaubsAntragRepository.delete(urlaubsAntrag.get());
        return Optional.empty();
    }

    /**
     * Reviews a vacation request.
     * This method updates the status, comment, and reviewer ID of the vacation request.
     *
     * @param id The ID of the vacation request to review
     * @param status The new status of the vacation request
     * @param comment The comment provided by the reviewer
     * @param reviewerId The ID of the reviewer
     * @return An empty Optional if the review was successful, or an Optional containing an error message if the request was not found
     */
    public Optional<String> reviewUrlaubsAntrag(Integer id, String status, String comment, Integer reviewerId) {
        Optional<UrlaubsAntrag> urlaubsAntrag = urlaubsAntragRepository.findByAntragsId(id);
        if (urlaubsAntrag.isEmpty()) {
            logger.error("Urlaubsantrag with id {} not found", id);
            return Optional.of("Urlaubsantrag not found with id: " + id);
        }

        UrlaubsAntrag existingAntrag = urlaubsAntrag.get();
        existingAntrag.setStatus(status);
        existingAntrag.setComment(comment);
        existingAntrag.setReviewerId(reviewerId);
        existingAntrag.setReviewDate(java.time.LocalDate.now());

        urlaubsAntragRepository.save(existingAntrag);
        return Optional.empty();
    }

    /**
     * Deletes all vacation requests associated with an employee.
     * This method is typically called when an employee is deleted.
     *
     * @param employeeId The ID of the employee whose vacation requests are to be deleted
     */
    public void deleteAllUrlaubsAntraegeByEmployeeId(Integer employeeId) {
        List<UrlaubsAntrag> antraege = urlaubsAntragRepository.findAllByEmployeeId(employeeId);
        if (!antraege.isEmpty()) {
            urlaubsAntragRepository.deleteAllByEmployeeId(employeeId);
            logger.info("Deleted {} vacation requests for employee with id {}", antraege.size(), employeeId);
        } else {
            logger.info("No vacation requests found for employee with id {}", employeeId);
        }
    }
}
