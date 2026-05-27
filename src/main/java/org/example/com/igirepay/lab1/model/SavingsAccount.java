package org.example.com.igirepay.lab1.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * SavingsAccount – applies a withdrawal fee and enforces a minimum balance.
 * Inherits from Account and overrides deposit / withdraw / processTransaction.
 */
public class SavingsAccount extends Account {

    /** Percentage fee charged on every withdrawal (e.g. 0.02 = 2 %). */
    private BigDecimal withdrawalFeeRate;

    /** Minimum balance that must remain after any withdrawal. */
    private BigDecimal minimumBalance;

    // ── Constructors ──────────────────────────────────────────────────────────

    public SavingsAccount() {
        super();
        setAccountType("SAVINGS");
        this.withdrawalFeeRate = new BigDecimal("0.02");
        this.minimumBalance    = new BigDecimal("500.00");
    }

    public SavingsAccount(String accountId, String customerId,
                          BigDecimal balance,
                          BigDecimal withdrawalFeeRate,
                          BigDecimal minimumBalance) {
        super(accountId, customerId, "SAVINGS", balance);
        this.withdrawalFeeRate = withdrawalFeeRate;
        this.minimumBalance    = minimumBalance;
    }

    // ── Overridden operations ─────────────────────────────────────────────────

    @Override
    public void deposit(BigDecimal amount) {
        validatePositive(amount);
        setBalance(getBalance().add(amount));
        System.out.println("[SavingsAccount] Deposited " + amount + ". New balance: " + getBalance());
    }

    @Override
    public void withdraw(BigDecimal amount) {
        validatePositive(amount);

        // Calculate fee
        BigDecimal fee   = amount.multiply(withdrawalFeeRate).setScale(2, RoundingMode.HALF_UP);
        BigDecimal total = amount.add(fee);

        // Ensure minimum balance is maintained
        BigDecimal balanceAfter = getBalance().subtract(total);
        if (balanceAfter.compareTo(minimumBalance) < 0) {
            throw new IllegalStateException(
                    "Withdrawal denied. Balance after withdrawal (" + balanceAfter +
                    ") would fall below minimum balance (" + minimumBalance + ").");
        }

        setBalance(balanceAfter);
        System.out.println("[SavingsAccount] Withdrew " + amount +
                           " + fee " + fee + ". New balance: " + getBalance());
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
        System.out.println("[SavingsAccount] Processed transaction: " + transaction.getReferenceId());
    }

    // ── Getters & Setters ─────────────────────────────────────────────────────

    public BigDecimal getWithdrawalFeeRate() { return withdrawalFeeRate; }
    public void       setWithdrawalFeeRate(BigDecimal withdrawalFeeRate) { this.withdrawalFeeRate = withdrawalFeeRate; }

    public BigDecimal getMinimumBalance() { return minimumBalance; }
    public void       setMinimumBalance(BigDecimal minimumBalance) { this.minimumBalance = minimumBalance; }

    // ── toString ──────────────────────────────────────────────────────────────

    @Override
    public String toString() {
        return "SavingsAccount{" +
               "accountId='"       + getAccountId()    + '\'' +
               ", customerId='"    + getCustomerId()   + '\'' +
               ", balance="        + getBalance()      +
               ", withdrawalFeeRate=" + withdrawalFeeRate +
               ", minimumBalance=" + minimumBalance    +
               ", createdAt="      + getCreatedAt()    +
               '}';
    }
}
