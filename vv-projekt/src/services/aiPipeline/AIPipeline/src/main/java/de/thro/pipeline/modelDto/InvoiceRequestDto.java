package de.thro.pipeline.modelDto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class InvoiceRequestDto {

    @NotNull(message = "OfferId darf nicht null sein")
    @JsonProperty("offerId")
    private String offerId;

    @NotBlank(message = "Rechnungsnummer darf nicht leer sein")
    private String invoiceNumber;

    @NotBlank(message = "Rechnungsdatum darf nicht leer sein")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate invoiceDate;

    @NotNull(message = "Gesamtbetrag darf nicht null sein")
    @DecimalMin(value = "0.0", inclusive = false, message = "Gesamtbetrag muss positiv sein")
    private BigDecimal invoiceTotalSum;

    @Valid
    @NotNull(message = "Kundendaten dürfen nicht fehlen")
    private CustomerDto customer;

    @Valid
    @NotEmpty(message = "Mindestens ein Rechnungs-Posten ist erforderlich")
    private List<InvoiceItemDto> invoiceItems;

}
