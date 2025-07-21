package de.thro.pipeline.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Data
@Entity
public class InvoiceItem {

    @EmbeddedId
    private InvoiceItemId id;

//    private Integer posNumber;
    private String description;
    private Integer amount;
    private BigDecimal price;

    @MapsId("invoiceId")
    @ManyToOne
    @JoinColumn(name = "invoice_id")
    private Invoice invoice;
}
