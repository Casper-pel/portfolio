package de.thro.pipeline.modelDto;

import jakarta.validation.constraints.Email;
import lombok.Getter;
import lombok.Setter;
import jakarta.validation.constraints.NotBlank;


@Setter
@Getter
public class CustomerDto {

   @NotBlank(message = "Firmenname darf nicht leer sein")
    private String companyName;

   @NotBlank(message = "Straße darf nicht leer sein")
    private String addressStreet;

   @NotBlank(message = "Hausnummer darf nicht leer sein")
    private String addressHouseNumber;

   @NotBlank(message = "PLZ darf nicht leer sein")
    private String postCode;

   @NotBlank(message = "Stadt darf nicht leer sein")
    private String city;

   @NotBlank(message = "Telefonnummer darf nicht leer sein")
    private String phone;

   @NotBlank(message = "Email darf nicht leer sein")
    @Email(message = "Email ungültig")
    private String mail;
}
