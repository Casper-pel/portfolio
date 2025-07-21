package de.thro.pipeline.modelDto;

import de.thro.pipeline.entity.OfferItem;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Setter
@Getter
public class OfferDto {

    @NotBlank(message = "Angebotsnummer darf nicht leer sein")
    private String offerNumber;

    @NotBlank(message = "Gesamtpreis darf nicht leer sein")
    private BigDecimal offerValue;

    @NotBlank(message = "Gültigkeitsdatum darf nicht leer sein")
    private LocalDate offerValidTill;

    @NotBlank(message = "Angebotsdatum darf nicht leer sein")
    private LocalDate offerDate;

    @NotBlank(message = "Kunde darf nicht leer sein")
    private CustomerDto customerDto;

    private List<OfferItemDto> offerItemsDto;
}
