package org.example.com.igirepay.lab1.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Base class representing a bank/wallet account.
 * Encapsulates common account fields and behaviour shared by all account types.
 */
public abstract class Account {

    private String accountId;
    private String customerId;
    private String accountType;
    private BigDecimal balance;
    private LocalDateTime createdAt;

    // ── Constructors ──────────────────────────────────────────────────────────

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

    // ── Abstract operations (polymorphism hooks) ──────────────────────────────

    /**
     * Deposit an amount into this account.
     * Subclasses may apply rules (e.g. interest, caps).
     */
    public abstract void deposit(BigDecimal amount);

    /**
     * Withdraw an amount from this account.
     * Subclasses may apply rules (e.g. fees, limits).
     */
    public abstract void withdraw(BigDecimal amount);

    /**
     * Process a transaction against this account.
     */
    public abstract void processTransaction(Transaction transaction);

    // ── Shared helper ─────────────────────────────────────────────────────────

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

    // ── Getters & Setters ─────────────────────────────────────────────────────

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

    // ── toString ──────────────────────────────────────────────────────────────

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
