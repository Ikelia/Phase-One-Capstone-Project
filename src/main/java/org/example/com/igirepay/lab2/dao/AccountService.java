package org.example.com.igirepay.lab2.dao;

import org.example.com.igirepay.lab1.model.Account;
import org.example.com.igirepay.lab1.model.Transaction;
import org.example.com.igirepay.lab2.db.DatabaseConnection;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class AccountService {

    private final AccountDAO          accountDAO          = new AccountDAO();
    private final TransactionDAO      transactionDAO      = new TransactionDAO();
    private final ProcessedRequestDAO processedRequestDAO = new ProcessedRequestDAO();

    private String nextTransactionId() throws SQLException {
        int count = transactionDAO.countAll();
        return String.format("TRN-%03d", count + 1);
    }

    public void deposit(String accountId, BigDecimal amount, String referenceId) throws SQLException {
        checkDuplicate(referenceId);
        Connection conn = DatabaseConnection.getConnection();
        conn.setAutoCommit(false);
        try {
            Account account = accountDAO.findById(accountId)
                    .orElseThrow(() -> new IllegalArgumentException("Account not found: " + accountId));
            account.deposit(amount);
            accountDAO.updateBalance(accountId, account.getBalance());
            Transaction tx = new Transaction(nextTransactionId(), referenceId, accountId, amount, "DEPOSIT");
            tx.setStatus("SUCCESS");
            transactionDAO.create(tx);
            processedRequestDAO.markProcessed(referenceId);
            conn.commit();
            System.out.println("[AccountService] Deposit successful. New balance: " + account.getBalance());
        } catch (Exception e) {
            conn.rollback();
            throw new SQLException("Deposit failed: " + e.getMessage(), e);
        } finally {
            conn.setAutoCommit(true);
        }
    }

    public void withdraw(String accountId, BigDecimal amount, String referenceId) throws SQLException {
        checkDuplicate(referenceId);
        Connection conn = DatabaseConnection.getConnection();
        conn.setAutoCommit(false);
        try {
            Account account = accountDAO.findById(accountId)
                    .orElseThrow(() -> new IllegalArgumentException("Account not found: " + accountId));
            account.withdraw(amount);
            accountDAO.updateBalance(accountId, account.getBalance());
            Transaction tx = new Transaction(nextTransactionId(), referenceId, accountId, amount, "WITHDRAWAL");
            tx.setStatus("SUCCESS");
            transactionDAO.create(tx);
            processedRequestDAO.markProcessed(referenceId);
            conn.commit();
            System.out.println("[AccountService] Withdrawal successful. New balance: " + account.getBalance());
        } catch (Exception e) {
            conn.rollback();
            throw new SQLException("Withdrawal failed: " + e.getMessage(), e);
        } finally {
            conn.setAutoCommit(true);
        }
    }

    public void transfer(String fromAccountId, String toAccountId,
                         BigDecimal amount, String referenceId) throws SQLException {
        checkDuplicate(referenceId);
        Connection conn = DatabaseConnection.getConnection();
        conn.setAutoCommit(false);
        try {
            Account from = accountDAO.findById(fromAccountId)
                    .orElseThrow(() -> new IllegalArgumentException("Source account not found: " + fromAccountId));
            Account to   = accountDAO.findById(toAccountId)
                    .orElseThrow(() -> new IllegalArgumentException("Destination account not found: " + toAccountId));
            from.withdraw(amount);
            to.deposit(amount);
            accountDAO.updateBalance(fromAccountId, from.getBalance());
            accountDAO.updateBalance(toAccountId,   to.getBalance());
            Transaction debit = new Transaction(nextTransactionId(), referenceId + "-DEBIT", fromAccountId, amount, "TRANSFER");
            debit.setStatus("SUCCESS");
            transactionDAO.create(debit);
            Transaction credit = new Transaction(nextTransactionId(), referenceId + "-CREDIT", toAccountId, amount, "TRANSFER");
            credit.setStatus("SUCCESS");
            transactionDAO.create(credit);
            processedRequestDAO.markProcessed(referenceId);
            conn.commit();
            System.out.println("[AccountService] Transfer of " + amount + " completed.");
        } catch (Exception e) {
            conn.rollback();
            throw new SQLException("Transfer failed: " + e.getMessage(), e);
        } finally {
            conn.setAutoCommit(true);
        }
    }

    public List<Transaction> getHistory(String accountId) throws SQLException {
        return transactionDAO.findByAccountId(accountId);
    }

    private void checkDuplicate(String referenceId) throws SQLException {
        if (processedRequestDAO.exists(referenceId)) {
            throw new IllegalStateException(
                    "Duplicate transaction rejected. Reference ID already processed: " + referenceId);
        }
    }
}
