package org.example.com.igirepay.lab1.model;

import java.math.BigDecimal;

// WalletAccount — child class of Account, supports instant transfers with no fee
public class WalletAccount extends Account {

    // Maximum amount allowed per single transfer
    private BigDecimal transferLimit;

    public WalletAccount() {
        super();
        setAccountType("WALLET");
        this.transferLimit = new BigDecimal("5000000.00");
    }

    public WalletAccount(String accountId, String customerId, BigDecimal balance, BigDecimal transferLimit) {
        // super() calls the parent Account constructor — inheritance in action
        super(accountId, customerId, "WALLET", balance);
        this.transferLimit = transferLimit;
    }

    // @Override — polymorphism: replaces Account's abstract deposit with wallet-specific logic
    @Override
    public void deposit(BigDecimal amount) {
        validatePositive(amount);
        setBalance(getBalance().add(amount));
        System.out.println("[WalletAccount] Deposited " + amount + ". New balance: " + getBalance());
    }

    // No fee applied — wallet allows instant withdrawal up to the transfer limit
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
                withdraw(transaction.getAmount());
                break;
            default:
                throw new IllegalArgumentException("Unknown transaction type: " + transaction.getTransactionType());
        }
        System.out.println("[WalletAccount] Processed transaction: " + transaction.getReferenceId());
    }

    public BigDecimal getTransferLimit() { return transferLimit; }
    public void       setTransferLimit(BigDecimal transferLimit) { this.transferLimit = transferLimit; }

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
