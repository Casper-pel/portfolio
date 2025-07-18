package ip.project.backend.backend.model;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;

@Document(collection = "urlaubsantraege")
public class UrlaubsAntrag {
    @Id
    private ObjectId _id;
    private Integer antragsId;
    private Integer employeeId; // ID des Mitarbeiters, der den Antrag gestellt hat
    private LocalDate startDatum;
    private LocalDate endDatum;
    private String status; // "genehmigt", "abgelehnt", "pending"
    private String type; // Art des Urlaubs (z.B. "Erholungsurlaub", "Sonderurlaub", ...)
    private String grund; // Grund für den Urlaub
    // Nach Review befüllte Felder:
    private LocalDate reviewDate; // Datum der Überprüfung des Antrags
    private Integer reviewerId; // Id des Prüfers
    public String comment; // Kommentar zu Genehmigung/Ablehnung des Antrags

    public UrlaubsAntrag() {
    }

    public UrlaubsAntrag(Integer antragsId, Integer employeeId, LocalDate startDatum, LocalDate endDatum, String status, String type, String grund, LocalDate reviewDate, Integer reviewerId, String comment) {
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

    public LocalDate getStartDatum() {
        return startDatum;
    }

    public Integer getEmployeeId() {
        return employeeId;
    }

    public String getStatus() {
        return status;
    }

    public LocalDate getEndDatum() {
        return endDatum;
    }

    public String getType() {
        return type;
    }

    public String getGrund() {
        return grund;
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

    public void setAntragsId(Integer antragsId) {
        this.antragsId = antragsId;
    }

    public void setEmployeeId(Integer employeeId) {
        this.employeeId = employeeId;
    }

    public void setStartDatum(LocalDate startDatum) {
        this.startDatum = startDatum;
    }

    public void setEndDatum(LocalDate endDatum) {
        this.endDatum = endDatum;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setGrund(String grund) {
        this.grund = grund;
    }

    public void setReviewDate(LocalDate reviewDate) {
        this.reviewDate = reviewDate;
    }

    public void setReviewerId(Integer reviewerId) {
        this.reviewerId = reviewerId;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
