package com.sporekart.modules.seller.domain;

import java.time.OffsetDateTime;
import java.util.UUID;

public class SellerProfile {
    private final UUID id;
    private final String userId;
    private String businessName;
    private String contactEmail;
    private String contactPhone;
    private String address;
    private String bankAccountLast4;
    private final OffsetDateTime createdAt;

    public SellerProfile(UUID id, String userId, String businessName, String contactEmail, String contactPhone, String address, String bankAccountLast4, OffsetDateTime createdAt) {
        this.id = id != null ? id : UUID.randomUUID();
        this.userId = userId;
        this.businessName = businessName;
        this.contactEmail = contactEmail;
        this.contactPhone = contactPhone;
        this.address = address;
        this.bankAccountLast4 = bankAccountLast4 != null ? bankAccountLast4 : "4321";
        this.createdAt = createdAt != null ? createdAt : OffsetDateTime.now();
    }

    public static SellerProfile createDefault(String userId, String email, String businessName) {
        return new SellerProfile(
                UUID.randomUUID(),
                userId,
                businessName != null ? businessName : "Seller Storefront",
                email,
                "+1-555-0199",
                "123 Seller Hub, Suite 100",
                "4321",
                OffsetDateTime.now()
        );
    }

    public UUID getId() { return id; }
    public String getUserId() { return userId; }
    public String getBusinessName() { return businessName; }
    public String getContactEmail() { return contactEmail; }
    public String getContactPhone() { return contactPhone; }
    public String getAddress() { return address; }
    public String getBankAccountLast4() { return bankAccountLast4; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
}
