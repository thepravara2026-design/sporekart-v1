package com.sporekart.modules.seller.infrastructure.persistence;

import com.sporekart.modules.seller.domain.SellerPayout;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "seller_payouts")
public class SellerPayoutEntity {

    @Id
    private UUID id;

    @Column(name = "seller_id", nullable = false)
    private String sellerId;

    @Column(name = "payout_reference", nullable = false)
    private String payoutReference;

    @Column(name = "period", nullable = false)
    private String period;

    @Column(name = "amount", nullable = false)
    private BigDecimal amount;

    @Column(name = "currency", nullable = false)
    private String currency;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "bank_account_last4")
    private String bankAccountLast4;

    @Column(name = "payout_date", nullable = false)
    private OffsetDateTime payoutDate;

    public SellerPayoutEntity() {}

    public SellerPayoutEntity(UUID id, String sellerId, String payoutReference, String period, BigDecimal amount, String currency, String status, String bankAccountLast4, OffsetDateTime payoutDate) {
        this.id = id;
        this.sellerId = sellerId;
        this.payoutReference = payoutReference;
        this.period = period;
        this.amount = amount;
        this.currency = currency;
        this.status = status;
        this.bankAccountLast4 = bankAccountLast4;
        this.payoutDate = payoutDate;
    }

    public static SellerPayoutEntity fromDomain(SellerPayout domain) {
        return new SellerPayoutEntity(
                domain.getId(),
                domain.getSellerId(),
                domain.getPayoutReference(),
                domain.getPeriod(),
                domain.getAmount(),
                domain.getCurrency(),
                domain.getStatus(),
                domain.getBankAccountLast4(),
                domain.getPayoutDate()
        );
    }

    public SellerPayout toDomain() {
        return new SellerPayout(id, sellerId, payoutReference, period, amount, currency, status, bankAccountLast4, payoutDate);
    }

    public UUID getId() { return id; }
    public String getSellerId() { return sellerId; }
    public String getPayoutReference() { return payoutReference; }
    public String getPeriod() { return period; }
    public BigDecimal getAmount() { return amount; }
    public String getCurrency() { return currency; }
    public String getStatus() { return status; }
    public String getBankAccountLast4() { return bankAccountLast4; }
    public OffsetDateTime getPayoutDate() { return payoutDate; }
}
