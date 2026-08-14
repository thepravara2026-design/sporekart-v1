package com.sporekart.modules.order.domain;

import java.util.Objects;

public final class AddressSnapshot {

    private final String fullName;
    private final String phone;
    private final String addressLine1;
    private final String addressLine2;
    private final String city;
    private final String state;
    private final String postalCode;
    private final String country;

    public AddressSnapshot(
            String fullName,
            String phone,
            String addressLine1,
            String addressLine2,
            String city,
            String state,
            String postalCode,
            String country
    ) {
        this.fullName = Objects.requireNonNull(fullName, "Full name cannot be null");
        this.phone = Objects.requireNonNull(phone, "Phone cannot be null");
        this.addressLine1 = Objects.requireNonNull(addressLine1, "Address line 1 cannot be null");
        this.addressLine2 = addressLine2;
        this.city = Objects.requireNonNull(city, "City cannot be null");
        this.state = Objects.requireNonNull(state, "State cannot be null");
        this.postalCode = Objects.requireNonNull(postalCode, "Postal code cannot be null");
        this.country = country != null && !country.isBlank() ? country : "India";
    }

    public String getFullName() { return fullName; }
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
        AddressSnapshot that = (AddressSnapshot) o;
        return Objects.equals(fullName, that.fullName) &&
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
        return Objects.hash(fullName, phone, addressLine1, addressLine2, city, state, postalCode, country);
    }
}
