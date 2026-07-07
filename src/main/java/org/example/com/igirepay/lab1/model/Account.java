package org.example.com.igirepay.lab1.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// Abstract base class — demonstrates abstraction and encapsulation (Exercise 1.1)
public abstract class Account {

    // Private fields — encapsulation: direct access is not allowed from outside
    private String accountId;
    private String customerId;
    private String accountType;
    private BigDecimal balance; // BigDecimal used for precise financial calculations
    private LocalDateTime createdAt;

    public Account() {
        this.createdAt = LocalDateTime.now();
        this.balance   = BigDecimal.ZERO;
    }

    public Account(String accountId, String customerId, String accountType, BigDecimal balance) {
        this.accountId   = accountId;
        this.customerId  = customerId;
        this.accountType = accountType;
        this.balance     = balance;
        this.createdAt   = LocalDateTime.now();
    }

    // Abstract methods — polymorphism hooks, each subclass provides its own implementation
    public abstract void deposit(BigDecimal amount);

    public abstract void withdraw(BigDecimal amount);

    public abstract void processTransaction(Transaction transaction);

    // Shared validation helpers available to all subclasses
    protected void validatePositive(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive, got: " + amount);
        }
    }

    protected void validateSufficientBalance(BigDecimal amount) {
        if (balance.compareTo(amount) < 0) {
            throw new IllegalStateException(
                    "Insufficient balance. Available: " + balance + ", Requested: " + amount);
        }
    }

    public String getAccountId()   { return accountId; }
    public void   setAccountId(String accountId) { this.accountId = accountId; }

    public String getCustomerId()  { return customerId; }
    public void   setCustomerId(String customerId) { this.customerId = customerId; }

    public String getAccountType() { return accountType; }
    public void   setAccountType(String accountType) { this.accountType = accountType; }

    public BigDecimal getBalance() { return balance; }
    public void       setBalance(BigDecimal balance) { this.balance = balance; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void          setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return "Account{" +
               "accountId='"   + accountId   + '\'' +
               ", customerId='" + customerId  + '\'' +
               ", accountType='" + accountType + '\'' +
               ", balance="    + balance      +
               ", createdAt="  + createdAt    +
               '}';
    }
}
