package de.thro.pipeline.modelDto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class InvoiceDto {

    private Long invoiceId;
    private String invoiceNumber;
    private LocalDate invoiceDate;
    private BigDecimal invoiceTotalSum;

    private LocalDate isChecked;
    private boolean isValid;

    private CustomerDto customer;
    private List<InvoiceItemDto> invoiceItems;

    @JsonProperty("offerId")
    private String offerId;
}
