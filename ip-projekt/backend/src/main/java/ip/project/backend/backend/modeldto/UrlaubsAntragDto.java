package ip.project.backend.backend.modeldto;

import jakarta.validation.constraints.*;

import java.time.LocalDate;

public class UrlaubsAntragDto {

    private Integer antragsId;

    @NotNull(message = "Benutzer darf nicht null sein")
    private Integer employeeId;

    @NotNull(message = "Startdatum darf nicht null sein")
    @FutureOrPresent(message = "Startdatum muss heute oder in der Zukunft liegen")
    private LocalDate startDatum;

    @NotNull(message = "Enddatum darf nicht null sein")
    @FutureOrPresent(message = "Enddatum muss heute oder in der Zukunft liegen")
    private LocalDate endDatum;

    @NotBlank(message = "Status darf nicht leer sein")
    @Pattern(regexp = "genehmigt|abgelehnt|pending", message = "Status muss 'genehmigt', 'abgelehnt' oder 'pending' sein")
    private String status;

    @NotBlank(message = "Urlaubsart darf nicht leer sein")
    @Pattern(
            regexp = "Erholungsurlaub|Krankheit|Bildungsurlaub|Mutterschutz|Elternzeit|Pflegezeit|Unbezahlter Urlaub|Sonderurlaub|Sonstiges",
            message = "Ungültige Urlaubsart"
    )
    private String type;

    @NotBlank(message = "Grund darf nicht leer sein")
    @Size(max = 255, message = "Grund darf maximal 255 Zeichen lang sein")
    private String grund;

    // reviewDate und reviewerId sind optional beim Erstellen, werden erst beim Review gesetzt
    private LocalDate reviewDate;

    private Integer reviewerId;

    @Size(max = 500, message = "Kommentar darf maximal 500 Zeichen lang sein")
    private String comment;

    public UrlaubsAntragDto() {
    }
    public UrlaubsAntragDto(Integer antragsId, Integer employeeId, LocalDate startDatum, LocalDate endDatum, String status, String type, String grund, LocalDate reviewDate, Integer reviewerId, String comment) {
        this.antragsId = antragsId;
        this.employeeId = employeeId;
        this.startDatum = startDatum;
        this.endDatum = endDatum;
        this.status = status;
        this.type = type;
        this.grund = grund;
        this.reviewDate = reviewDate;
        this.reviewerId = reviewerId;
        this.comment = comment;
    }

    public Integer getAntragsId() {
        return antragsId;
    }
    public void setAntragsId(Integer antragsId) {
        this.antragsId = antragsId;
    }
    public Integer getEmployeeId() {
        return employeeId;
    }
    public void setEmployeeId(Integer employeeId) {
        this.employeeId = employeeId;
    }
    public LocalDate getStartDatum() {
        return startDatum;
    }
    public void setStartDatum(LocalDate startDatum) {
        this.startDatum = startDatum;
    }
    public LocalDate getEndDatum() {
        return endDatum;
    }
    public void setEndDatum(LocalDate endDatum) {
        this.endDatum = endDatum;
    }
    public String getStatus() {
        return status;
    }
    public LocalDate getReviewDate() {
        return reviewDate;
    }
    public Integer getReviewerId() {
        return reviewerId;
    }
    public String getComment() {
        return comment;
    }

    public void setStatus(String status) {
        this.status = status;
    }
    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }
    public String getGrund() {
        return grund;
    }
    public void setGrund(String grund) {
        this.grund = grund;
    }
    public void setComment(String comment) {
        this.comment = comment;
    }
    public void setReviewDate(LocalDate reviewDate) {
        this.reviewDate = reviewDate;
    }
    public void setReviewerId(Integer reviewerId) {
        this.reviewerId = reviewerId;
    }

    @Override
    public String toString() {
        return "UrlaubsAntragDto{" +
                "antragsId=" + antragsId +
                ", employeeId=" + employeeId +
                ", startDatum=" + startDatum +
                ", endDatum=" + endDatum +
                ", status='" + status + '\'' +
                ", type='" + type + '\'' +
                ", grund='" + grund + '\'' +
                ", reviewDate=" + reviewDate +
                ", reviewerId=" + reviewerId +
                ", comment='" + comment + '\'' +
                '}';
    }
}
