package org.example.com.igirepay.lab1.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

// SavingsAccount — child class of Account, applies 2% fee and enforces minimum balance
public class SavingsAccount extends Account {

    // 2% fee applied on every withdrawal
    private BigDecimal withdrawalFeeRate;

    // Account balance must never fall below this value after a withdrawal
    private BigDecimal minimumBalance;

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

    @Override
    public void deposit(BigDecimal amount) {
        validatePositive(amount);
        setBalance(getBalance().add(amount));
        System.out.println("[SavingsAccount] Deposited " + amount + ". New balance: " + getBalance());
    }

    // Polymorphism: different withdrawal rules from WalletAccount — fee + minimum balance check
    @Override
    public void withdraw(BigDecimal amount) {
        validatePositive(amount);
        // Calculate 2% fee using BigDecimal for precision
        BigDecimal fee   = amount.multiply(withdrawalFeeRate).setScale(2, RoundingMode.HALF_UP);
        BigDecimal total = amount.add(fee);

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

    public BigDecimal getWithdrawalFeeRate() { return withdrawalFeeRate; }
    public void       setWithdrawalFeeRate(BigDecimal withdrawalFeeRate) { this.withdrawalFeeRate = withdrawalFeeRate; }

    public BigDecimal getMinimumBalance() { return minimumBalance; }
    public void       setMinimumBalance(BigDecimal minimumBalance) { this.minimumBalance = minimumBalance; }

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
