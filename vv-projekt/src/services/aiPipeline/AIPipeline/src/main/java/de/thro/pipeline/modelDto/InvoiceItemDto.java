package de.thro.pipeline.modelDto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class InvoiceItemDto {

    @Min(1)
    private int posNumber;

    @NotBlank(message = "Beschreibung darf nicht leer sein")
    private String description;

    @Min(1)
    private int amount;

    @DecimalMin("0.0")
    private BigDecimal price;
}
