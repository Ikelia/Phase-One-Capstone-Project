package org.example.com.igirepay.lab1.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Transaction {

    private String        transactionId;
    private String        referenceId;
    private String        accountId;
    private BigDecimal    amount;
    private String        transactionType;
    private LocalDateTime timestamp;
    private String        status;

    public Transaction() {
        this.timestamp = LocalDateTime.now();
        this.status    = "PENDING";
    }

    public Transaction(String transactionId, String referenceId, String accountId,
                       BigDecimal amount, String transactionType) {
        this.transactionId   = transactionId;
        this.referenceId     = referenceId;
        this.accountId       = accountId;
        this.amount          = amount;
        this.transactionType = transactionType;
        this.timestamp       = LocalDateTime.now();
        this.status          = "PENDING";
    }

    public String        getTransactionId()   { return transactionId; }
    public void          setTransactionId(String transactionId) { this.transactionId = transactionId; }

    public String        getReferenceId()     { return referenceId; }
    public void          setReferenceId(String referenceId) { this.referenceId = referenceId; }

    public String        getAccountId()       { return accountId; }
    public void          setAccountId(String accountId) { this.accountId = accountId; }

    public BigDecimal    getAmount()          { return amount; }
    public void          setAmount(BigDecimal amount) { this.amount = amount; }

    public String        getTransactionType() { return transactionType; }
    public void          setTransactionType(String transactionType) { this.transactionType = transactionType; }

    public LocalDateTime getTimestamp()       { return timestamp; }
    public void          setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public String        getStatus()          { return status; }
    public void          setStatus(String status) { this.status = status; }

    @Override
    public String toString() {
        return "Transaction{" +
               "transactionId='"  + transactionId   + '\'' +
               ", referenceId='"  + referenceId     + '\'' +
               ", accountId='"    + accountId       + '\'' +
               ", amount="        + amount          +
               ", type='"         + transactionType + '\'' +
               ", status='"       + status          + '\'' +
               ", timestamp="     + timestamp       +
               '}';
    }
}
