package ip.project.backend.backend.controller;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import ip.project.backend.backend.modeldto.UrlaubsAntragDto;
import ip.project.backend.backend.service.JwtService;
import ip.project.backend.backend.service.UrlaubsAntragService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

@RestController
@RequestMapping("/api/urlaubsantrag")
@CrossOrigin(origins = {"http://localhost:3000"}, allowCredentials = "true", maxAge = 3600)
public class UrlaubsAntragController {
    private final Logger logger = LoggerFactory.getLogger(UrlaubsAntragController.class);
    private final UrlaubsAntragService urlaubsAntragService;
    @Autowired
    private final JwtService jwtService;

    @Autowired
    public UrlaubsAntragController(UrlaubsAntragService urlaubsAntragService, JwtService jwtService) {
        this.urlaubsAntragService = urlaubsAntragService;
        this.jwtService = jwtService;
    }

    @Operation(summary = "Get all Urlaubsanträge", description = "Get all existing Urlaubsanträge from the system")
    @ApiResponse(responseCode = "200", description = "Urlaubsanträge found")
    @ApiResponse(responseCode = "204", description = "No Urlaubsanträge found")
    @ApiResponse(responseCode = "400", description = "No Urlaubsanträge found. Error message provided")
    @GetMapping("/all")
    public ResponseEntity<List<UrlaubsAntragDto>> getAllUrlaubsantraege() {
        List<UrlaubsAntragDto> urlaubsantraege = urlaubsAntragService.getAllUrlaubsAntraege();
        if (urlaubsantraege.isEmpty()) {
            logger.info("Keine Urlaubsanträge gefuden");
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.ok(urlaubsantraege);
        }
    }

    @Operation(summary = "Get Urlaubsantrag by ID", description = "Get an existing Urlaubsantrag by its ID")
    @ApiResponse(responseCode = "200", description = "Urlaubsantrag found")
    @ApiResponse(responseCode = "204", description = "Urlaubsantrag not found for the given ID")
    @ApiResponse(responseCode = "400", description = "Urlaubsantrag not found. Error message provided")
    @GetMapping("/get/{id}")
    public ResponseEntity<UrlaubsAntragDto> getUrlaubsantragById(@CookieValue String token, @PathVariable("id") Integer id) {
        if (id == null) {
            logger.error("Urlaubsantrag ID is null");
            return ResponseEntity.badRequest().build();
        }

        if (token == null || token.isEmpty()) {
            logger.error("JWT Token fehlt im Cookie");
            return ResponseEntity.status(401).build();
        }

        Integer employeeId = jwtService.extractEmployeeId(token);

        Optional<UrlaubsAntragDto> urlaubsAntrag = urlaubsAntragService.getUrlaubsAntragById(employeeId, id);

        if (urlaubsAntrag.isEmpty()) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.ok(urlaubsAntrag.get());
        }
    }

    @Operation(summary = "Get all Urlaubsanträge by User ID", description = "Get all existing Urlaubsanträge for a specific user")
    @ApiResponse(responseCode = "200", description = "Urlaubsanträge found for user")
    @ApiResponse(responseCode = "204", description = "No Urlaubsanträge found for user ID")
    @ApiResponse(responseCode = "400", description = "No Urlaubsanträge found for user. Error message provided")
    @ApiResponse(responseCode = "401", description = "Valider JWT Token fehlt im Cookie")
    @GetMapping("/user")
    public ResponseEntity<List<UrlaubsAntragDto>> getUrlaubsantraegeByUserId(@CookieValue(value = "token", required = false) String token) {
        if (token == null || token.isEmpty()) {
            logger.error("JWT Token fehlt im Cookie");
            return ResponseEntity.status(401).build();
        }
        Integer employeeId;
        try {
            employeeId = jwtService.extractEmployeeId(token);
        } catch (Exception e) {
            logger.error("Invalid JWT Token: {}", e.getMessage());
            return ResponseEntity.status(401).build();
        }

        List<UrlaubsAntragDto> urlaubsantraege = urlaubsAntragService.getAllUrlaubsAntraegeByEmployeeId(employeeId);

        if (urlaubsantraege.isEmpty()) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.ok(urlaubsantraege);
        }
    }

    @Operation(summary = "Add Urlaubsantrag", description = "Add a new Urlaubsantrag to the system")
    @ApiResponse(responseCode = "200", description = "Urlaubsantrag added successfully")
    @ApiResponse(responseCode = "400", description = "Urlaubsantrag not added. Error message provided")
    @ApiResponse(responseCode = "401", description = "Valid JWT Token missing in Cookie")
    @PostMapping("/add")
    public ResponseEntity<String> addUrlaubsantrag(@CookieValue(value = "token", required = false) String token, @NotNull @Valid @RequestBody UrlaubsAntragDto urlaubsAntragDto) {
        if (urlaubsAntragDto == null) {
            return ResponseEntity.badRequest().body("Urlaubsantrag data is null");
        }
        if (token == null || token.isEmpty()) {
            logger.error("JWT Token fehlt im Cookie");
            return ResponseEntity.status(401).body("JWT Token fehlt");
        }
        try {
            Integer employeeId = jwtService.extractEmployeeId(token);
            urlaubsAntragDto.setEmployeeId(employeeId);
        } catch (Exception e) {
            logger.error("Ungültiges JWT Token: {}", e.getMessage());
            return ResponseEntity.status(401).body("Ungültiges JWT Token");
        }

        Optional<String> answer = urlaubsAntragService.addUrlaubsAntrag(urlaubsAntragDto);

        if (answer.isEmpty()) {
            logger.info("Urlaubsantrag added successfully");
            return ResponseEntity.ok("Urlaubsantrag added successfully");
        } else {
            logger.error("Urlaubsantrag not added. Error message: {}", answer.get());
            return ResponseEntity.status(400).body(answer.get());
        }
    }

    @Operation(summary = "Delete Urlaubsantrag", description = "Delete an existing Urlaubsantrag from the system")
    @ApiResponse(responseCode = "200", description = "Urlaubsantrag deleted successfully")
    @ApiResponse(responseCode = "400", description = "Urlaubsantrag not deleted. Error message provided")
    @ApiResponse(responseCode = "401", description = "Valid JWT Token missing in Cookie")
    @ApiResponse(responseCode = "403", description = "EmployeeId in Antrag does not match with Token")
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteUrlaubsantrag(
            @CookieValue(value = "token", required = false) String token,
            @PathVariable("id") Integer id) {
        if (id == null) {
            logger.error("Urlaubsantrag ID is null");
            return ResponseEntity.badRequest().body("Urlaubsantrag ID is null");
        }
        if (token == null || token.isEmpty()) {
            logger.error("JWT Token fehlt im Cookie");
            return ResponseEntity.status(401).body("JWT Token fehlt");
        }
        Integer employeeIdFromToken;
        try {
            employeeIdFromToken = jwtService.extractEmployeeId(token);
        } catch (Exception e) {
            logger.error("Ungültiges JWT Token: {}", e.getMessage());
            return ResponseEntity.status(401).body("Ungültiges JWT Token");
        }

        Optional<UrlaubsAntragDto> urlaubsAntragOpt = urlaubsAntragService.getUrlaubsAntragById(employeeIdFromToken, id);
        if (urlaubsAntragOpt.isEmpty()) {
            logger.error("Urlaubsantrag not found for ID: {}", id);
            return ResponseEntity.status(404).body("Urlaubsantrag nicht gefunden");
        }
        UrlaubsAntragDto urlaubsAntrag = urlaubsAntragOpt.get();
        if (!employeeIdFromToken.equals(urlaubsAntrag.getEmployeeId())) {
            logger.error("EmployeeId im Antrag ({}) stimmt nicht mit Token ({}) überein", urlaubsAntrag.getEmployeeId(), employeeIdFromToken);
            return ResponseEntity.status(403).body("EmployeeId stimmt nicht mit Token überein");
        }

        Optional<String> answer = urlaubsAntragService.deleteUrlaubsAntrag(id);

        if (answer.isEmpty()) {
            logger.info("Urlaubsantrag deleted successfully for ID: {}", id);
            return ResponseEntity.ok("Urlaubsantrag deleted successfully");
        } else {
            logger.error("Urlaubsantrag not deleted. Error message: {}", answer.get());
            return ResponseEntity.status(400).body(answer.get());
        }
    }

    @Operation(summary = "Review an Urlaubsantrag with id", description = "Review an existing Urlaubsantrag in the system")
    @ApiResponse(responseCode = "200", description = "Urlaubsantrag reviewed successfully")
    @ApiResponse(responseCode = "400", description = "Urlaubsantrag not reviewed. Error message provided")
    @ApiResponse(responseCode = "401", description = "Valid JWT Token missing in Cookie")
    @PutMapping("/review")
    public ResponseEntity<String> reviewUrlaubsantrag(@CookieValue(value = "token", required = false) String token,
                                                      @NotNull @Valid @RequestBody UrlaubsAntragDto urlaubsAntragDto) {
        if (urlaubsAntragDto == null) {
            return ResponseEntity.badRequest().body("Urlaubsantrag data is null");
        }
        if (urlaubsAntragDto.getAntragsId() == null) {
            logger.error("Urlaubsantrag ID is null");
            return ResponseEntity.badRequest().body("Urlaubsantrag ID is required for review");
        }

        if (token == null || token.isEmpty()) {
            logger.error("JWT Token fehlt im Cookie");
            return ResponseEntity.status(401).body("JWT Token fehlt");
        }

        try {
            Integer reviewerId = jwtService.extractEmployeeId(token);
            urlaubsAntragDto.setReviewerId(reviewerId);
        } catch (Exception e) {
            logger.error("Ungültiges JWT Token: {}", e.getMessage());
            return ResponseEntity.status(401).body("Ungültiges JWT Token");
        }

        Optional<String> answer = urlaubsAntragService.reviewUrlaubsAntrag(
                urlaubsAntragDto.getAntragsId(),
                urlaubsAntragDto.getStatus(),
                urlaubsAntragDto.getComment(),
                urlaubsAntragDto.getReviewerId()
        );

        if (answer.isEmpty()) {
            logger.info("Urlaubsantrag {} reviewed successfully by {}", urlaubsAntragDto.getAntragsId(), urlaubsAntragDto.getReviewerId());
            return ResponseEntity.ok("Urlaubsantrag reviewed successfully");
        } else {
            logger.error("Urlaubsantrag not reviewed. Error message: {}", answer.get());
            return ResponseEntity.status(400).body(answer.get());
        }
    }

    @Operation(summary = "Update Urlaubsantrag", description = "Update an existing Urlaubsantrag in the system")
    @ApiResponse(responseCode = "200", description = "Urlaubsantrag updated successfully")
    @ApiResponse(responseCode = "400", description = "Urlaubsantrag not updated. Error message provided")
    @PutMapping("/update")
    public ResponseEntity<String> updateUrlaubsantrag(
            @CookieValue(value = "token", required = false) String token,
            @NotNull @Valid @RequestBody UrlaubsAntragDto urlaubsAntragDto) {
        if (urlaubsAntragDto == null) {
            return ResponseEntity.badRequest().body("Urlaubsantrag data is null");
        }
        if (token == null || token.isEmpty()) {
            logger.error("JWT Token fehlt im Cookie");
            return ResponseEntity.status(401).body("JWT Token fehlt");
        }
        Integer employeeIdFromToken;
        try {
            employeeIdFromToken = jwtService.extractEmployeeId(token);
        } catch (Exception e) {
            logger.error("Ungültiges JWT Token: {}", e.getMessage());
            return ResponseEntity.status(401).body("Ungültiges JWT Token");
        }
        if (!employeeIdFromToken.equals(urlaubsAntragDto.getEmployeeId())) {
            logger.error("EmployeeId im Antrag ({}) stimmt nicht mit Token ({}) überein", urlaubsAntragDto.getEmployeeId(), employeeIdFromToken);
            return ResponseEntity.status(403).body("EmployeeId stimmt nicht mit Token überein");
        }

        Optional<String> answer = urlaubsAntragService.updateUrlaubsAntrag(urlaubsAntragDto);

        if (answer.isEmpty()) {
            logger.info("Urlaubsantrag updated successfully");
            return ResponseEntity.ok("Urlaubsantrag updated successfully");
        } else {
            logger.error("Urlaubsantrag not updated. Error message: {}", answer.get());
            return ResponseEntity.status(400).body(answer.get());
        }
    }
}
