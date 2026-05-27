package org.example.com.igirepay.lab1.service;

import org.example.com.igirepay.lab1.model.Account;
import org.example.com.igirepay.lab1.model.Customer;
import org.example.com.igirepay.lab1.model.Transaction;

import java.math.BigDecimal;
import java.util.*;

/**
 * Core in-memory service for Lab 1.
 *
 * Responsibilities:
 *  - Manage customers and their accounts (List / Map).
 *  - Track transaction history (List).
 *  - Detect duplicate transactions using a Set of processed reference IDs.
 *  - Log failed transactions.
 */
public class PaymentService {

    // ── Collections ───────────────────────────────────────────────────────────

    /** All registered customers, keyed by customerId. */
    private final Map<String, Customer> customerMap = new HashMap<>();

    /** All accounts, keyed by accountId. */
    private final Map<String, Account> accountMap = new HashMap<>();

    /** Full transaction history. */
    private final List<Transaction> transactionHistory = new ArrayList<>();

    /**
     * Set of already-processed reference IDs.
     * Using a HashSet gives O(1) lookup – critical for idempotency checks.
     */
    private final Set<String> processedReferenceIds = new HashSet<>();

    /** Transactions that failed (insufficient funds, duplicates, etc.). */
    private final List<Transaction> failedTransactionLog = new ArrayList<>();

    // ── Customer management ───────────────────────────────────────────────────

    public void registerCustomer(Customer customer) {
        if (customerMap.containsKey(customer.getCustomerId())) {
            throw new IllegalArgumentException("Customer already exists: " + customer.getCustomerId());
        }
        customerMap.put(customer.getCustomerId(), customer);
        System.out.println("Customer registered: " + customer.getFullName());
    }

    public Customer findCustomer(String customerId) {
        Customer c = customerMap.get(customerId);
        if (c == null) throw new NoSuchElementException("Customer not found: " + customerId);
        return c;
    }

    public Collection<Customer> getAllCustomers() {
        return Collections.unmodifiableCollection(customerMap.values());
    }

    // ── Account management ────────────────────────────────────────────────────

    public void addAccount(Account account) {
        accountMap.put(account.getAccountId(), account);
        Customer owner = customerMap.get(account.getCustomerId());
        if (owner != null) owner.addAccount(account);
    }

    public Account findAccount(String accountId) {
        Account a = accountMap.get(accountId);
        if (a == null) throw new NoSuchElementException("Account not found: " + accountId);
        return a;
    }

    // ── Transaction processing ────────────────────────────────────────────────

    /**
     * Process a transaction with idempotency protection.
     *
     * @param transaction the transaction to process
     * @return true if processed successfully, false if duplicate
     */
    public boolean processTransaction(Transaction transaction) {
        String refId = transaction.getReferenceId();

        // ── Duplicate check ───────────────────────────────────────────────────
        if (processedReferenceIds.contains(refId)) {
            transaction.setStatus("DUPLICATE");
            failedTransactionLog.add(transaction);
            System.out.println("[DUPLICATE] Transaction with reference " + refId + " already processed. Rejected.");
            return false;
        }

        // ── Find account and delegate to polymorphic processTransaction ───────
        try {
            Account account = findAccount(transaction.getAccountId());
            account.processTransaction(transaction);
            transaction.setStatus("SUCCESS");
            processedReferenceIds.add(refId);
            transactionHistory.add(transaction);
            System.out.println("[SUCCESS] Transaction " + refId + " processed.");
            return true;
        } catch (Exception e) {
            transaction.setStatus("FAILED");
            failedTransactionLog.add(transaction);
            System.out.println("[FAILED] Transaction " + refId + " failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * Transfer money between two accounts.
     */
    public boolean transfer(String fromAccountId, String toAccountId,
                            BigDecimal amount, String referenceId) {
        if (processedReferenceIds.contains(referenceId)) {
            System.out.println("[DUPLICATE] Transfer reference " + referenceId + " already processed.");
            return false;
        }
        try {
            Account from = findAccount(fromAccountId);
            Account to   = findAccount(toAccountId);
            from.withdraw(amount);
            to.deposit(amount);
            processedReferenceIds.add(referenceId);

            // Record debit transaction
            Transaction debit = new Transaction(
                    UUID.randomUUID().toString(), referenceId + "-DEBIT",
                    fromAccountId, amount, "TRANSFER");
            debit.setStatus("SUCCESS");
            transactionHistory.add(debit);

            // Record credit transaction
            Transaction credit = new Transaction(
                    UUID.randomUUID().toString(), referenceId + "-CREDIT",
                    toAccountId, amount, "TRANSFER");
            credit.setStatus("SUCCESS");
            transactionHistory.add(credit);

            System.out.println("[TRANSFER] " + amount + " moved from " + fromAccountId + " to " + toAccountId);
            return true;
        } catch (Exception e) {
            System.out.println("[TRANSFER FAILED] " + e.getMessage());
            return false;
        }
    }

    // ── Reporting helpers ─────────────────────────────────────────────────────

    public List<Transaction> getTransactionHistory() {
        return Collections.unmodifiableList(transactionHistory);
    }

    public List<Transaction> getFailedTransactionLog() {
        return Collections.unmodifiableList(failedTransactionLog);
    }

    public List<Transaction> getTransactionsForAccount(String accountId) {
        List<Transaction> result = new ArrayList<>();
        for (Transaction t : transactionHistory) {
            if (accountId.equals(t.getAccountId())) result.add(t);
        }
        return result;
    }

    public Set<String> getProcessedReferenceIds() {
        return Collections.unmodifiableSet(processedReferenceIds);
    }
}
