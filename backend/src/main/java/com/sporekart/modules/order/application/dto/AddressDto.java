package com.sporekart.modules.order.application.dto;

import com.sporekart.modules.order.domain.AddressSnapshot;
import jakarta.validation.constraints.NotBlank;

public record AddressDto(
        @NotBlank(message = "Full name is required")
        String fullName,

        @NotBlank(message = "Phone is required")
        String phone,

        @NotBlank(message = "Address line 1 is required")
        String addressLine1,

        String addressLine2,

        @NotBlank(message = "City is required")
        String city,

        @NotBlank(message = "State is required")
        String state,

        @NotBlank(message = "Postal code is required")
        String postalCode,

        String country
) {
    public AddressSnapshot toDomain() {
        return new AddressSnapshot(
                fullName, phone, addressLine1, addressLine2, city, state, postalCode, country
        );
    }

    public static AddressDto fromDomain(AddressSnapshot snapshot) {
        if (snapshot == null) return null;
        return new AddressDto(
                snapshot.getFullName(),
                snapshot.getPhone(),
                snapshot.getAddressLine1(),
                snapshot.getAddressLine2(),
                snapshot.getCity(),
                snapshot.getState(),
                snapshot.getPostalCode(),
                snapshot.getCountry()
        );
    }
}
