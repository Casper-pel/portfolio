package ip.project.backend.backend.controller;

import ip.project.backend.backend.modeldto.UrlaubsAntragDto;
import ip.project.backend.backend.service.JwtService;
import ip.project.backend.backend.service.UrlaubsAntragService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UrlaubsAntragControllerTest {

    @Mock
    private UrlaubsAntragService service;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private UrlaubsAntragController controller;

    private UrlaubsAntragDto sampleDto;

    @BeforeEach
    void setUp() {
        LocalDate today = LocalDate.now();
        LocalDate nextWeek = today.plusWeeks(1);
        LocalDate reviewDate = today.plusDays(2);

        sampleDto = new UrlaubsAntragDto();
        sampleDto.setAntragsId(1);
        sampleDto.setEmployeeId(42);
        sampleDto.setStartDatum(today);
        sampleDto.setEndDatum(nextWeek);
        sampleDto.setStatus("pending");
        sampleDto.setType("Erholungsurlaub");
        sampleDto.setGrund("Erholung am Meer");
        sampleDto.setReviewDate(reviewDate);
        sampleDto.setReviewerId(99);
        sampleDto.setComment("OK");
    }

    @Test
    void testGetAllEmpty() {
        when(service.getAllUrlaubsAntraege()).thenReturn(Collections.emptyList());
        ResponseEntity<List<UrlaubsAntragDto>> resp = controller.getAllUrlaubsantraege();
        assertThat(resp.getStatusCodeValue()).isEqualTo(204);
        verify(service).getAllUrlaubsAntraege();
    }

    @Test
    void testGetAllNonEmpty() {
        when(service.getAllUrlaubsAntraege()).thenReturn(List.of(sampleDto));
        ResponseEntity<List<UrlaubsAntragDto>> resp = controller.getAllUrlaubsantraege();
        assertThat(resp.getStatusCodeValue()).isEqualTo(200);
        assertThat(resp.getBody()).containsExactly(sampleDto);
    }

    @Test
    void testGetByIdNull() {
        ResponseEntity<UrlaubsAntragDto> resp = controller.getUrlaubsantragById("token", null);
        assertThat(resp.getStatusCodeValue()).isEqualTo(400);
    }

    @Test
    void testGetByIdTokenMissing() {
        ResponseEntity<UrlaubsAntragDto> resp = controller.getUrlaubsantragById(null, 1);
        assertThat(resp.getStatusCodeValue()).isEqualTo(401);
    }

    @Test
    void testGetByIdTokenEmpty() {
        ResponseEntity<UrlaubsAntragDto> resp = controller.getUrlaubsantragById("", 1);
        assertThat(resp.getStatusCodeValue()).isEqualTo(401);
    }

    @Test
    void testGetByIdNotFound() {
        when(jwtService.extractEmployeeId("token")).thenReturn(42);
        when(service.getUrlaubsAntragById(42, 1)).thenReturn(Optional.empty());
        ResponseEntity<UrlaubsAntragDto> resp = controller.getUrlaubsantragById("token", 1);
        assertThat(resp.getStatusCodeValue()).isEqualTo(204);
    }

    @Test
    void testGetByIdFound() {
        when(jwtService.extractEmployeeId("token")).thenReturn(42);
        when(service.getUrlaubsAntragById(42, 1)).thenReturn(Optional.of(sampleDto));
        ResponseEntity<UrlaubsAntragDto> resp = controller.getUrlaubsantragById("token", 1);
        assertThat(resp.getStatusCodeValue()).isEqualTo(200);
        assertThat(resp.getBody()).isEqualTo(sampleDto);
    }

    @Test
    void testGetByUserTokenMissing() {
        ResponseEntity<List<UrlaubsAntragDto>> resp = controller.getUrlaubsantraegeByUserId(null);
        assertThat(resp.getStatusCodeValue()).isEqualTo(401);
    }

    @Test
    void testGetByUserTokenEmpty() {
        ResponseEntity<List<UrlaubsAntragDto>> resp = controller.getUrlaubsantraegeByUserId("");
        assertThat(resp.getStatusCodeValue()).isEqualTo(401);
    }

    @Test
    void testGetByUserTokenInvalid() {
        when(jwtService.extractEmployeeId("bad"))
                .thenThrow(new RuntimeException("invalid token"));

        ResponseEntity<List<UrlaubsAntragDto>> resp =
                controller.getUrlaubsantraegeByUserId("bad");

        assertThat(resp.getStatusCodeValue()).isEqualTo(401);
    }

    @Test
    void testGetByUserEmpty() {
        when(jwtService.extractEmployeeId("token")).thenReturn(42);
        when(service.getAllUrlaubsAntraegeByEmployeeId(42))
                .thenReturn(Collections.emptyList());

        ResponseEntity<List<UrlaubsAntragDto>> resp =
                controller.getUrlaubsantraegeByUserId("token");

        assertThat(resp.getStatusCodeValue()).isEqualTo(204);
    }

    @Test
    void testGetByUserNonEmpty() {
        when(jwtService.extractEmployeeId("token")).thenReturn(42);
        when(service.getAllUrlaubsAntraegeByEmployeeId(42)).thenReturn(List.of(sampleDto));
        ResponseEntity<List<UrlaubsAntragDto>> resp = controller.getUrlaubsantraegeByUserId("token");
        assertThat(resp.getStatusCodeValue()).isEqualTo(200);
        assertThat(resp.getBody()).containsExactly(sampleDto);
    }

    @Test
    void testAddNullDto() {
        ResponseEntity<String> resp = controller.addUrlaubsantrag("token", null);
        assertThat(resp.getStatusCodeValue()).isEqualTo(400);
        assertThat(resp.getBody()).isEqualTo("Urlaubsantrag data is null");
    }

    @Test
    void testAddTokenMissing() {
        ResponseEntity<String> resp = controller.addUrlaubsantrag(null, sampleDto);
        assertThat(resp.getStatusCodeValue()).isEqualTo(401);
        assertThat(resp.getBody()).isEqualTo("JWT Token fehlt");
    }

    @Test
    void testAddTokenEmpty() {
        ResponseEntity<String> resp = controller.addUrlaubsantrag("", sampleDto);
        assertThat(resp.getStatusCodeValue()).isEqualTo(401);
        assertThat(resp.getBody()).isEqualTo("JWT Token fehlt");
    }

    @Test
    void testAddTokenInvalid() {
        when(jwtService.extractEmployeeId("bad")).thenThrow(new RuntimeException());
        ResponseEntity<String> resp = controller.addUrlaubsantrag("bad", sampleDto);
        assertThat(resp.getStatusCodeValue()).isEqualTo(401);
        assertThat(resp.getBody()).isEqualTo("Ungültiges JWT Token");
    }

    @Test
    void testAddSuccess() {
        when(jwtService.extractEmployeeId("token")).thenReturn(42);
        UrlaubsAntragDto newDto = new UrlaubsAntragDto();
        newDto.setAntragsId(2);
        newDto.setStatus("PENDING");
        newDto.setComment("Test");
        when(service.addUrlaubsAntrag(any(UrlaubsAntragDto.class))).thenReturn(Optional.empty());
        ResponseEntity<String> resp = controller.addUrlaubsantrag("token", newDto);
        assertThat(resp.getStatusCodeValue()).isEqualTo(200);
        assertThat(resp.getBody()).isEqualTo("Urlaubsantrag added successfully");
    }

    @Test
    void testAddFailure() {
        when(jwtService.extractEmployeeId("token")).thenReturn(42);
        when(service.addUrlaubsAntrag(any(UrlaubsAntragDto.class))).thenReturn(Optional.of("Error"));
        ResponseEntity<String> resp = controller.addUrlaubsantrag("token", sampleDto);
        assertThat(resp.getStatusCodeValue()).isEqualTo(400);
        assertThat(resp.getBody()).isEqualTo("Error");
    }

    @Test
    void testDeleteNullId() {
        ResponseEntity<String> resp = controller.deleteUrlaubsantrag("token", null);
        assertThat(resp.getStatusCodeValue()).isEqualTo(400);
        assertThat(resp.getBody()).isEqualTo("Urlaubsantrag ID is null");
    }

    @Test
    void testDeleteTokenMissing() {
        ResponseEntity<String> resp = controller.deleteUrlaubsantrag(null, 1);
        assertThat(resp.getStatusCodeValue()).isEqualTo(401);
        assertThat(resp.getBody()).isEqualTo("JWT Token fehlt");
    }

    @Test
    void testDeleteTokenEmpty() {
        ResponseEntity<String> resp = controller.deleteUrlaubsantrag("", 1);
        assertThat(resp.getStatusCodeValue()).isEqualTo(401);
        assertThat(resp.getBody()).isEqualTo("JWT Token fehlt");
    }

    @Test
    void testDeleteTokenInvalid() {
        when(jwtService.extractEmployeeId("bad")).thenThrow(new RuntimeException());
        ResponseEntity<String> resp = controller.deleteUrlaubsantrag("bad", 1);
        assertThat(resp.getStatusCodeValue()).isEqualTo(401);
        assertThat(resp.getBody()).isEqualTo("Ungültiges JWT Token");
    }

    @Test
    void testDeleteNotFound() {
        when(jwtService.extractEmployeeId("token")).thenReturn(42);
        when(service.getUrlaubsAntragById(42, 1)).thenReturn(Optional.empty());
        ResponseEntity<String> resp = controller.deleteUrlaubsantrag("token", 1);
        assertThat(resp.getStatusCodeValue()).isEqualTo(404);
        assertThat(resp.getBody()).isEqualTo("Urlaubsantrag nicht gefunden");
    }

    @Test
    void testDeleteForbidden() {
        when(jwtService.extractEmployeeId("token")).thenReturn(42);
        UrlaubsAntragDto dto = new UrlaubsAntragDto();
        dto.setEmployeeId(99); // Different employee ID
        when(service.getUrlaubsAntragById(42, 1)).thenReturn(Optional.of(dto));
        ResponseEntity<String> resp = controller.deleteUrlaubsantrag("token", 1);
        assertThat(resp.getStatusCodeValue()).isEqualTo(403);
        assertThat(resp.getBody()).isEqualTo("EmployeeId stimmt nicht mit Token überein");
    }

    @Test
    void testDeleteSuccess() {
        when(jwtService.extractEmployeeId("token")).thenReturn(42);
        UrlaubsAntragDto dto = new UrlaubsAntragDto();
        dto.setEmployeeId(42);
        when(service.getUrlaubsAntragById(42, 1)).thenReturn(Optional.of(dto));
        when(service.deleteUrlaubsAntrag(1)).thenReturn(Optional.empty());
        ResponseEntity<String> resp = controller.deleteUrlaubsantrag("token", 1);
        assertThat(resp.getStatusCodeValue()).isEqualTo(200);
        assertThat(resp.getBody()).isEqualTo("Urlaubsantrag deleted successfully");
    }

    @Test
    void testDeleteFailure() {
        when(jwtService.extractEmployeeId("token")).thenReturn(42);
        UrlaubsAntragDto dto = new UrlaubsAntragDto();
        dto.setEmployeeId(42);
        when(service.getUrlaubsAntragById(42, 1)).thenReturn(Optional.of(dto));
        when(service.deleteUrlaubsAntrag(1)).thenReturn(Optional.of("Error"));
        ResponseEntity<String> resp = controller.deleteUrlaubsantrag("token", 1);
        assertThat(resp.getStatusCodeValue()).isEqualTo(400);
        assertThat(resp.getBody()).isEqualTo("Error");
    }

    @Test
    void testReviewNullDto() {
        ResponseEntity<String> resp = controller.reviewUrlaubsantrag("token", null);
        assertThat(resp.getStatusCodeValue()).isEqualTo(400);
        assertThat(resp.getBody()).isEqualTo("Urlaubsantrag data is null");
    }

    @Test
    void testReviewIdNull() {
        UrlaubsAntragDto dto = new UrlaubsAntragDto();
        ResponseEntity<String> resp = controller.reviewUrlaubsantrag("token", dto);
        assertThat(resp.getStatusCodeValue()).isEqualTo(400);
        assertThat(resp.getBody()).isEqualTo("Urlaubsantrag ID is required for review");
    }

    @Test
    void testReviewTokenMissing() {
        UrlaubsAntragDto dto = new UrlaubsAntragDto();
        dto.setAntragsId(1);
        ResponseEntity<String> resp = controller.reviewUrlaubsantrag(null, dto);
        assertThat(resp.getStatusCodeValue()).isEqualTo(401);
        assertThat(resp.getBody()).isEqualTo("JWT Token fehlt");
    }

    @Test
    void testReviewTokenEmpty() {
        UrlaubsAntragDto dto = new UrlaubsAntragDto();
        dto.setAntragsId(1);
        ResponseEntity<String> resp = controller.reviewUrlaubsantrag("", dto);
        assertThat(resp.getStatusCodeValue()).isEqualTo(401);
        assertThat(resp.getBody()).isEqualTo("JWT Token fehlt");
    }

    @Test
    void testReviewTokenInvalid() {
        UrlaubsAntragDto dto = new UrlaubsAntragDto();
        dto.setAntragsId(1);
        when(jwtService.extractEmployeeId("bad")).thenThrow(new RuntimeException());
        ResponseEntity<String> resp = controller.reviewUrlaubsantrag("bad", dto);
        assertThat(resp.getStatusCodeValue()).isEqualTo(401);
        assertThat(resp.getBody()).isEqualTo("Ungültiges JWT Token");
    }

    @Test
    void testReviewSuccess() {
        UrlaubsAntragDto dto = new UrlaubsAntragDto();
        dto.setAntragsId(1);
        dto.setStatus("APPROVED");
        dto.setComment("OK");
        when(jwtService.extractEmployeeId("token")).thenReturn(100);
        when(service.reviewUrlaubsAntrag(eq(1), eq("APPROVED"), eq("OK"), eq(100)))
                .thenReturn(Optional.empty());
        ResponseEntity<String> resp = controller.reviewUrlaubsantrag("token", dto);
        assertThat(resp.getStatusCodeValue()).isEqualTo(200);
        assertThat(resp.getBody()).isEqualTo("Urlaubsantrag reviewed successfully");
    }

    @Test
    void testReviewFailure() {
        UrlaubsAntragDto dto = new UrlaubsAntragDto();
        dto.setAntragsId(1);
        dto.setStatus("APPROVED");
        dto.setComment("OK");
        when(jwtService.extractEmployeeId("token")).thenReturn(100);
        when(service.reviewUrlaubsAntrag(eq(1), eq("APPROVED"), eq("OK"), eq(100)))
                .thenReturn(Optional.of("Error"));
        ResponseEntity<String> resp = controller.reviewUrlaubsantrag("token", dto);
        assertThat(resp.getStatusCodeValue()).isEqualTo(400);
        assertThat(resp.getBody()).isEqualTo("Error");
    }

    @Test
    void testUpdateNullDto() {
        ResponseEntity<String> resp = controller.updateUrlaubsantrag("token", null);
        assertThat(resp.getStatusCodeValue()).isEqualTo(400);
        assertThat(resp.getBody()).isEqualTo("Urlaubsantrag data is null");
    }

    @Test
    void testUpdateTokenMissing() {
        ResponseEntity<String> resp = controller.updateUrlaubsantrag(null, sampleDto);
        assertThat(resp.getStatusCodeValue()).isEqualTo(401);
        assertThat(resp.getBody()).isEqualTo("JWT Token fehlt");
    }

    @Test
    void testUpdateTokenEmpty() {
        ResponseEntity<String> resp = controller.updateUrlaubsantrag("", sampleDto);
        assertThat(resp.getStatusCodeValue()).isEqualTo(401);
        assertThat(resp.getBody()).isEqualTo("JWT Token fehlt");
    }

    @Test
    void testUpdateTokenInvalid() {
        when(jwtService.extractEmployeeId("bad")).thenThrow(new RuntimeException());
        ResponseEntity<String> resp = controller.updateUrlaubsantrag("bad", sampleDto);
        assertThat(resp.getStatusCodeValue()).isEqualTo(401);
        assertThat(resp.getBody()).isEqualTo("Ungültiges JWT Token");
    }

    @Test
    void testUpdateForbidden() {
        when(jwtService.extractEmployeeId("token")).thenReturn(99);
        sampleDto.setEmployeeId(42);
        ResponseEntity<String> resp = controller.updateUrlaubsantrag("token", sampleDto);
        assertThat(resp.getStatusCodeValue()).isEqualTo(403);
        assertThat(resp.getBody()).isEqualTo("EmployeeId stimmt nicht mit Token überein");
    }

    @Test
    void testUpdateSuccess() {
        when(jwtService.extractEmployeeId("token")).thenReturn(42);
        sampleDto.setEmployeeId(42);
        when(service.updateUrlaubsAntrag(sampleDto)).thenReturn(Optional.empty());
        ResponseEntity<String> resp = controller.updateUrlaubsantrag("token", sampleDto);
        assertThat(resp.getStatusCodeValue()).isEqualTo(200);
        assertThat(resp.getBody()).isEqualTo("Urlaubsantrag updated successfully");
    }

    @Test
    void testUpdateFailure() {
        when(jwtService.extractEmployeeId("token")).thenReturn(42);
        sampleDto.setEmployeeId(42);
        when(service.updateUrlaubsAntrag(sampleDto)).thenReturn(Optional.of("Error"));
        ResponseEntity<String> resp = controller.updateUrlaubsantrag("token", sampleDto);
        assertThat(resp.getStatusCodeValue()).isEqualTo(400);
        assertThat(resp.getBody()).isEqualTo("Error");
    }
}
