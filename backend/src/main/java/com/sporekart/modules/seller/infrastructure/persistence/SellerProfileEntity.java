package com.sporekart.modules.seller.infrastructure.persistence;

import com.sporekart.modules.seller.domain.SellerProfile;
import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "seller_profiles")
public class SellerProfileEntity {

    @Id
    private UUID id;

    @Column(name = "user_id", nullable = false, unique = true)
    private String userId;

    @Column(name = "business_name", nullable = false)
    private String businessName;

    @Column(name = "contact_email", nullable = false)
    private String contactEmail;

    @Column(name = "contact_phone")
    private String contactPhone;

    @Column(name = "address")
    private String address;

    @Column(name = "bank_account_last4")
    private String bankAccountLast4;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    public SellerProfileEntity() {}

    public SellerProfileEntity(UUID id, String userId, String businessName, String contactEmail, String contactPhone, String address, String bankAccountLast4, OffsetDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.businessName = businessName;
        this.contactEmail = contactEmail;
        this.contactPhone = contactPhone;
        this.address = address;
        this.bankAccountLast4 = bankAccountLast4;
        this.createdAt = createdAt;
    }

    public static SellerProfileEntity fromDomain(SellerProfile domain) {
        return new SellerProfileEntity(
                domain.getId(),
                domain.getUserId(),
                domain.getBusinessName(),
                domain.getContactEmail(),
                domain.getContactPhone(),
                domain.getAddress(),
                domain.getBankAccountLast4(),
                domain.getCreatedAt()
        );
    }

    public SellerProfile toDomain() {
        return new SellerProfile(id, userId, businessName, contactEmail, contactPhone, address, bankAccountLast4, createdAt);
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
