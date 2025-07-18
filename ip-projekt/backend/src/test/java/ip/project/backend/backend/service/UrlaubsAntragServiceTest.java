package ip.project.backend.backend.service;

import ip.project.backend.backend.checker.PermissionManager;
import ip.project.backend.backend.mapper.UrlaubsAntragMapper;
import ip.project.backend.backend.model.Employee;
import ip.project.backend.backend.model.Role;
import ip.project.backend.backend.model.UrlaubsAntrag;
import ip.project.backend.backend.modeldto.UrlaubsAntragDto;
import ip.project.backend.backend.repository.EmployeeRepository;
import ip.project.backend.backend.repository.UrlaubsAntragRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UrlaubsAntragServiceTest {

    @Mock
    private UrlaubsAntragRepository repository;

    @Mock
    private PermissionManager permissionManager;

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private UrlaubsAntragService service;

    private UrlaubsAntrag exampleEntity;
    private UrlaubsAntragDto exampleDto;
    private Employee exampleEmployee;

    @BeforeEach
    void setUp() {
        exampleEntity = new UrlaubsAntrag();
        exampleEntity.setAntragsId(1);
        exampleEntity.setEmployeeId(10);
        exampleEntity.setStartDatum(LocalDate.of(2025, 7, 1));
        exampleEntity.setEndDatum(LocalDate.of(2025, 7, 5));
        exampleEntity.setStatus("PENDING");
        exampleEntity.setType("VACATION");
        exampleEntity.setGrund("Erholung");
        exampleEntity.setReviewDate(null);
        exampleEntity.setReviewerId(null);
        exampleEntity.setComment(null);

        exampleDto = UrlaubsAntragMapper.INSTANCE.urlaubsAntragToUrlaubsAntragDto(exampleEntity);

        // Role mit Review-Berechtigung für Tests
        Role roleWithReviewPermission = new Role();
        roleWithReviewPermission.setRoleId(1);
        roleWithReviewPermission.setRoleName("Admin");
        roleWithReviewPermission.setRolePermissions(List.of("REVIEW_URLAUBSANTRAG"));

        // Employee für Tests
        exampleEmployee = new Employee();
        exampleEmployee.setEmployeeId(10);
        exampleEmployee.setRole(roleWithReviewPermission);
    }

    @Test
    void getUrlaubsAntragById_EmployeeNotFound() {
        when(employeeRepository.findEmployeeByEmployeeId(10)).thenReturn(Optional.empty());

        Optional<UrlaubsAntragDto> result = service.getUrlaubsAntragById(10, 1);
        assertTrue(result.isEmpty());
    }

    @Test
    void getUrlaubsAntragById_UrlaubsAntragNotFound() {
        when(employeeRepository.findEmployeeByEmployeeId(10)).thenReturn(Optional.of(exampleEmployee));
        when(repository.findByAntragsId(1)).thenReturn(Optional.empty());

        Optional<UrlaubsAntragDto> result = service.getUrlaubsAntragById(10, 1);
        assertTrue(result.isEmpty());
    }

    @Test
    void getUrlaubsAntragById_NoPermission() {
        // Employee ohne Review-Berechtigung
        Role roleWithoutPermission = new Role();
        roleWithoutPermission.setRoleId(2);
        roleWithoutPermission.setRoleName("Basic");
        roleWithoutPermission.setRolePermissions(List.of()); // Keine Berechtigungen

        Employee employeeWithoutPermission = new Employee();
        employeeWithoutPermission.setEmployeeId(99); // Different employee ID
        employeeWithoutPermission.setRole(roleWithoutPermission);

        when(employeeRepository.findEmployeeByEmployeeId(99)).thenReturn(Optional.of(employeeWithoutPermission));
        when(repository.findByAntragsId(1)).thenReturn(Optional.of(exampleEntity));
        when(permissionManager.findRequiredPermission("/api/urlaubsantrag/review", "PUT")).thenReturn("REVIEW_URLAUBSANTRAG");

        Optional<UrlaubsAntragDto> result = service.getUrlaubsAntragById(99, 1);
        assertTrue(result.isEmpty());
    }

    @Test
    void getUrlaubsAntragById_Found_OwnUrlaubsAntrag() {
        when(employeeRepository.findEmployeeByEmployeeId(10)).thenReturn(Optional.of(exampleEmployee));
        when(repository.findByAntragsId(1)).thenReturn(Optional.of(exampleEntity));
        when(permissionManager.findRequiredPermission("/api/urlaubsantrag/review", "PUT")).thenReturn("REVIEW_URLAUBSANTRAG");

        Optional<UrlaubsAntragDto> result = service.getUrlaubsAntragById(10, 1);
        assertTrue(result.isPresent());
        UrlaubsAntragDto dto = result.get();
        assertEquals(1, dto.getAntragsId());
        assertEquals(10, dto.getEmployeeId());
        assertEquals(LocalDate.of(2025, 7, 1), dto.getStartDatum());
        assertEquals(LocalDate.of(2025, 7, 5), dto.getEndDatum());
        assertEquals("PENDING", dto.getStatus());
        assertEquals("VACATION", dto.getType());
        assertEquals("Erholung", dto.getGrund());
    }

    @Test
    void getUrlaubsAntragById_Found_WithReviewPermission() {
        // Employee mit Review-Berechtigung kann andere Urlaubsanträge sehen
        Role reviewerRole = new Role();
        reviewerRole.setRoleId(3);
        reviewerRole.setRoleName("Reviewer");
        reviewerRole.setRolePermissions(List.of("urlaub.review"));

        Employee reviewer = new Employee();
        reviewer.setEmployeeId(99);
        reviewer.setRole(reviewerRole);

        // Create an Urlaubsantrag from different employee
        UrlaubsAntrag otherEmployeeAntrag = new UrlaubsAntrag();
        otherEmployeeAntrag.setAntragsId(1);
        otherEmployeeAntrag.setEmployeeId(20); // Different employee ID
        otherEmployeeAntrag.setStatus("pending");

        when(employeeRepository.findEmployeeByEmployeeId(99)).thenReturn(Optional.of(reviewer));
        when(repository.findByAntragsId(1)).thenReturn(Optional.of(otherEmployeeAntrag));
        when(permissionManager.findRequiredPermission("/api/urlaubsantrag/review", "PUT")).thenReturn("urlaub.review");

        Optional<UrlaubsAntragDto> result = service.getUrlaubsAntragById(99, 1);
        assertTrue(result.isPresent());
        assertEquals(20, result.get().getEmployeeId());
    }

    @Test
    void getAllUrlaubsAntraegeByEmployeeId_Empty() {
        when(repository.findAllByEmployeeId(10)).thenReturn(Collections.emptyList());

        List<UrlaubsAntragDto> result = service.getAllUrlaubsAntraegeByEmployeeId(10);
        assertTrue(result.isEmpty());
    }

    @Test
    void getAllUrlaubsAntraegeByEmployeeId_NonEmpty() {
        when(repository.findAllByEmployeeId(10)).thenReturn(List.of(exampleEntity));

        List<UrlaubsAntragDto> result = service.getAllUrlaubsAntraegeByEmployeeId(10);
        assertEquals(1, result.size());
        assertEquals(1, result.get(0).getAntragsId());
    }

    @Test
    void getAllUrlaubsAntraege_Empty() {
        when(repository.findAll()).thenReturn(Collections.emptyList());

        List<UrlaubsAntragDto> result = service.getAllUrlaubsAntraege();
        assertTrue(result.isEmpty());
    }

    @Test
    void getAllUrlaubsAntraege_NonEmpty() {
        when(repository.findAll()).thenReturn(List.of(exampleEntity));

        List<UrlaubsAntragDto> result = service.getAllUrlaubsAntraege();
        assertEquals(1, result.size());
    }

    @Test
    void addUrlaubsAntrag_GenerateId() {
        // DTO ohne ID
        UrlaubsAntragDto dto = exampleDto;
        dto.setAntragsId(null);

        // bestehende IDs: 5 und 2 → max = 5
        UrlaubsAntrag other = new UrlaubsAntrag();
        other.setAntragsId(5);
        when(repository.findAll()).thenReturn(List.of(other, exampleEntity));

        Optional<String> err = service.addUrlaubsAntrag(dto);
        assertTrue(err.isEmpty());

        // Beim Speichern erhält der Mapper das neue ID-Feld 6
        ArgumentCaptor<UrlaubsAntrag> captor = ArgumentCaptor.forClass(UrlaubsAntrag.class);
        verify(repository).save(captor.capture());
        assertEquals(6, captor.getValue().getAntragsId());
    }

    @Test
    void addUrlaubsAntrag_IdAlreadyExists() {
        UrlaubsAntragDto dto = exampleDto; // hat ID=1
        when(repository.findByAntragsId(1)).thenReturn(Optional.of(exampleEntity));

        Optional<String> err = service.addUrlaubsAntrag(dto);
        assertTrue(err.isPresent());
        assertEquals("Urlaubsantrag already exists with id: 1", err.get());
        verify(repository, never()).save(any());
    }

    @Test
    void addUrlaubsAntrag_WithProvidedId() {
        UrlaubsAntragDto dto = exampleDto; // ID=1
        when(repository.findByAntragsId(1)).thenReturn(Optional.empty());

        Optional<String> err = service.addUrlaubsAntrag(dto);
        assertTrue(err.isEmpty());
        verify(repository).save(any(UrlaubsAntrag.class));
    }

    @Test
    void updateUrlaubsAntrag_NullId() {
        UrlaubsAntragDto dto = exampleDto;
        dto.setAntragsId(null);

        Optional<String> err = service.updateUrlaubsAntrag(dto);
        assertTrue(err.isPresent());
        assertEquals("Urlaubsantrag ID is required for update", err.get());
        verify(repository, never()).save(any());
    }

    @Test
    void updateUrlaubsAntrag_NotFound() {
        when(repository.findByAntragsId(1)).thenReturn(Optional.empty());

        Optional<String> err = service.updateUrlaubsAntrag(exampleDto);
        assertTrue(err.isPresent());
        assertEquals("Urlaubsantrag not found with id: 1", err.get());
        verify(repository, never()).save(any());
    }

    @Test
    void updateUrlaubsAntrag_Success() {
        // vorhandener Antrag
        when(repository.findByAntragsId(1)).thenReturn(Optional.of(exampleEntity));

        UrlaubsAntragDto dto = exampleDto;
        dto.setStatus("APPROVED");
        dto.setComment("OK");
        dto.setReviewerId(99);
        dto.setReviewDate(LocalDate.of(2025, 6, 30));

        Optional<String> err = service.updateUrlaubsAntrag(dto);
        assertTrue(err.isEmpty());

        // Entity wurde aktualisiert
        assertEquals("APPROVED", exampleEntity.getStatus());
        assertEquals("OK", exampleEntity.getComment());
        assertEquals(99, exampleEntity.getReviewerId());
        assertEquals(LocalDate.of(2025, 6, 30), exampleEntity.getReviewDate());
        verify(repository).save(exampleEntity);
    }

    @Test
    void deleteUrlaubsAntrag_NotFound() {
        when(repository.findByAntragsId(2)).thenReturn(Optional.empty());

        Optional<String> err = service.deleteUrlaubsAntrag(2);
        assertTrue(err.isPresent());
        assertEquals("Urlaubsantrag not found with id: 2", err.get());
        verify(repository, never()).delete(any());
    }

    @Test
    void deleteUrlaubsAntrag_Success() {
        when(repository.findByAntragsId(1)).thenReturn(Optional.of(exampleEntity));

        Optional<String> err = service.deleteUrlaubsAntrag(1);
        assertTrue(err.isEmpty());
        verify(repository).delete(exampleEntity);
    }

    @Test
    void reviewUrlaubsAntrag_NotFound() {
        when(repository.findByAntragsId(3)).thenReturn(Optional.empty());

        Optional<String> err = service.reviewUrlaubsAntrag(3, "REJECTED", "No", 5);
        assertTrue(err.isPresent());
        assertEquals("Urlaubsantrag not found with id: 3", err.get());
        verify(repository, never()).save(any());
    }

    @Test
    void reviewUrlaubsAntrag_Success() {
        when(repository.findByAntragsId(1)).thenReturn(Optional.of(exampleEntity));
        LocalDate before = LocalDate.now();

        Optional<String> err = service.reviewUrlaubsAntrag(1, "APPROVED", "Alles gut", 55);
        assertTrue(err.isEmpty());

        // Entity-Felder wurden gesetzt
        assertEquals("APPROVED", exampleEntity.getStatus());
        assertEquals("Alles gut", exampleEntity.getComment());
        assertEquals(55, exampleEntity.getReviewerId());
        assertNotNull(exampleEntity.getReviewDate());
        assertFalse(exampleEntity.getReviewDate().isBefore(before));

        verify(repository).save(exampleEntity);
    }

    @Test
    void deleteAllUrlaubsAntraegeByEmployeeId_WithExistingAntraege() {
        List<UrlaubsAntrag> existingAntraege = List.of(exampleEntity);
        when(repository.findAllByEmployeeId(10)).thenReturn(existingAntraege);

        service.deleteAllUrlaubsAntraegeByEmployeeId(10);

        verify(repository).deleteAllByEmployeeId(10);
    }

    @Test
    void deleteAllUrlaubsAntraegeByEmployeeId_NoExistingAntraege() {
        when(repository.findAllByEmployeeId(10)).thenReturn(Collections.emptyList());

        service.deleteAllUrlaubsAntraegeByEmployeeId(10);

        verify(repository, never()).deleteAllByEmployeeId(10);
    }
}
