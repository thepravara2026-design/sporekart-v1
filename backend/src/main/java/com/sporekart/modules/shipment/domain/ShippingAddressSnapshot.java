package com.sporekart.modules.shipment.domain;

import java.util.Objects;

public final class ShippingAddressSnapshot {
    private final String recipientName;
    private final String phone;
    private final String addressLine1;
    private final String addressLine2;
    private final String city;
    private final String state;
    private final String postalCode;
    private final String country;

    public ShippingAddressSnapshot(
            String recipientName,
            String phone,
            String addressLine1,
            String addressLine2,
            String city,
            String state,
            String postalCode,
            String country
    ) {
        this.recipientName = Objects.requireNonNull(recipientName, "Recipient name required");
        this.phone = Objects.requireNonNull(phone, "Phone required");
        this.addressLine1 = Objects.requireNonNull(addressLine1, "Address line 1 required");
        this.addressLine2 = addressLine2;
        this.city = Objects.requireNonNull(city, "City required");
        this.state = Objects.requireNonNull(state, "State required");
        this.postalCode = Objects.requireNonNull(postalCode, "Postal code required");
        this.country = Objects.requireNonNull(country, "Country required");
    }

    public String getRecipientName() { return recipientName; }
    public String getPhone() { return phone; }
    public String getAddressLine1() { return addressLine1; }
    public String getAddressLine2() { return addressLine2; }
    public String getCity() { return city; }
    public String getState() { return state; }
    public String getPostalCode() { return postalCode; }
    public String getCountry() { return country; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ShippingAddressSnapshot that = (ShippingAddressSnapshot) o;
        return Objects.equals(recipientName, that.recipientName) &&
                Objects.equals(phone, that.phone) &&
                Objects.equals(addressLine1, that.addressLine1) &&
                Objects.equals(addressLine2, that.addressLine2) &&
                Objects.equals(city, that.city) &&
                Objects.equals(state, that.state) &&
                Objects.equals(postalCode, that.postalCode) &&
                Objects.equals(country, that.country);
    }

    @Override
    public int hashCode() {
        return Objects.hash(recipientName, phone, addressLine1, addressLine2, city, state, postalCode, country);
    }
}
