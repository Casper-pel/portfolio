package de.thro.pipeline.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class InvoiceItemId implements Serializable {
    private Long invoiceId;
    private int posNumber;
}
