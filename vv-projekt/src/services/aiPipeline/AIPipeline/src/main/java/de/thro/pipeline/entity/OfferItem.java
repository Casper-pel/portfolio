package de.thro.pipeline.entity;

import jakarta.persistence.*;
import lombok.Setter;

import java.math.BigDecimal;

@Setter
@Entity
public class OfferItem {

    @EmbeddedId
    private  OfferItemId id;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private Integer amount;

    @Column(nullable = false)
    private BigDecimal price;

    @ManyToOne
    @JoinColumn(name = "offer_id", nullable = false)
    private Offer offer;
}
