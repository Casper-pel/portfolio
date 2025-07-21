package de.thro.pipeline.modelDto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Setter
@Getter
public class OfferItemDto {

    @NotBlank(message = "posNumber darf nicht leer sein")
    private int posNumber;
    @NotBlank(message = "Beschreibung darf nicht leer sein")
    private String description;
    @NotBlank(message = "Menge darf nicht leer sein")
    private int amount;
    @NotBlank(message = "Preis darf nicht leer sein")
    private BigDecimal price;
}
