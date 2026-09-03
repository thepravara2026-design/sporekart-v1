package com.sporekart.modules.seller.domain;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public class SellerPayout {
    private final UUID id;
    private final String sellerId;
    private final String payoutReference;
    private final String period;
    private final BigDecimal amount;
    private final String currency;
    private final String status;
    private final String bankAccountLast4;
    private final OffsetDateTime payoutDate;

    public SellerPayout(UUID id, String sellerId, String payoutReference, String period, BigDecimal amount, String currency, String status, String bankAccountLast4, OffsetDateTime payoutDate) {
        this.id = id != null ? id : UUID.randomUUID();
        this.sellerId = sellerId;
        this.payoutReference = payoutReference;
        this.period = period;
        this.amount = amount;
        this.currency = currency != null ? currency : "INR";
        this.status = status != null ? status : "COMPLETED";
        this.bankAccountLast4 = bankAccountLast4 != null ? bankAccountLast4 : "4321";
        this.payoutDate = payoutDate != null ? payoutDate : OffsetDateTime.now();
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
