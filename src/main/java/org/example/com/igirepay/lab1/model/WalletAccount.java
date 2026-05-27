package org.example.com.igirepay.lab1.model;

import java.math.BigDecimal;

/**
 * WalletAccount – supports instant transfers with no withdrawal fee.
 * Inherits from Account and overrides deposit / withdraw / processTransaction.
 */
public class WalletAccount extends Account {

    /** Maximum single-transfer limit (configurable). */
    private BigDecimal transferLimit;

    // ── Constructors ──────────────────────────────────────────────────────────

    public WalletAccount() {
        super();
        setAccountType("WALLET");
        this.transferLimit = new BigDecimal("5000000.00"); // 5 million default
    }

    public WalletAccount(String accountId, String customerId, BigDecimal balance, BigDecimal transferLimit) {
        super(accountId, customerId, "WALLET", balance);
        this.transferLimit = transferLimit;
    }

    // ── Overridden operations ─────────────────────────────────────────────────

    @Override
    public void deposit(BigDecimal amount) {
        validatePositive(amount);
        setBalance(getBalance().add(amount));
        System.out.println("[WalletAccount] Deposited " + amount + ". New balance: " + getBalance());
    }

    @Override
    public void withdraw(BigDecimal amount) {
        validatePositive(amount);
        if (amount.compareTo(transferLimit) > 0) {
            throw new IllegalArgumentException(
                    "Amount " + amount + " exceeds wallet transfer limit of " + transferLimit);
        }
        validateSufficientBalance(amount);
        setBalance(getBalance().subtract(amount));
        System.out.println("[WalletAccount] Withdrew " + amount + ". New balance: " + getBalance());
    }

    @Override
    public void processTransaction(Transaction transaction) {
        if (transaction == null) throw new IllegalArgumentException("Transaction cannot be null");
        switch (transaction.getTransactionType().toUpperCase()) {
            case "DEPOSIT":
                deposit(transaction.getAmount());
                break;
            case "WITHDRAWAL":
                withdraw(transaction.getAmount());
                break;
            case "TRANSFER":
                withdraw(transaction.getAmount()); // debit side
                break;
            default:
                throw new IllegalArgumentException("Unknown transaction type: " + transaction.getTransactionType());
        }
        System.out.println("[WalletAccount] Processed transaction: " + transaction.getReferenceId());
    }

    // ── Getters & Setters ─────────────────────────────────────────────────────

    public BigDecimal getTransferLimit() { return transferLimit; }
    public void       setTransferLimit(BigDecimal transferLimit) { this.transferLimit = transferLimit; }

    // ── toString ──────────────────────────────────────────────────────────────

    @Override
    public String toString() {
        return "WalletAccount{" +
               "accountId='"    + getAccountId()   + '\'' +
               ", customerId='" + getCustomerId()  + '\'' +
               ", balance="     + getBalance()     +
               ", transferLimit=" + transferLimit  +
               ", createdAt="   + getCreatedAt()   +
               '}';
    }
}
