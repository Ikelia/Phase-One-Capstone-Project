package org.example.com.igirepay.lab3.report;

import com.opencsv.CSVWriter;
import org.example.com.igirepay.lab1.model.Transaction;
import org.example.com.igirepay.lab2.dao.TransactionDAO;

import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ReportService {

    private final TransactionDAO transactionDAO = new TransactionDAO();

    public void exportToCsv(String accountId, String outputPath) throws SQLException, IOException {
        List<Transaction> transactions = transactionDAO.findByAccountId(accountId);

        try (CSVWriter writer = new CSVWriter(new FileWriter(outputPath))) {
            writer.writeNext(new String[]{"Transaction ID", "Reference ID", "Account ID",
                                          "Type", "Amount", "Status", "Timestamp"});
            for (Transaction t : transactions) {
                writer.writeNext(new String[]{
                        t.getTransactionId(),
                        t.getReferenceId(),
                        t.getAccountId(),
                        t.getTransactionType(),
                        t.getAmount().toPlainString(),
                        t.getStatus(),
                        t.getTimestamp() != null ? t.getTimestamp().toString() : ""
                });
            }
        }
        System.out.println("[Report] Exported " + transactions.size() + " transactions to " + outputPath);
    }

    public void printDailySummary(String accountId, String date) throws SQLException {
        String targetDate = (date != null) ? date
                : LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);

        List<Transaction> transactions = transactionDAO.findByAccountAndDate(accountId, targetDate);

        BigDecimal totalDeposits    = BigDecimal.ZERO;
        BigDecimal totalWithdrawals = BigDecimal.ZERO;
        BigDecimal totalTransfers   = BigDecimal.ZERO;

        for (Transaction t : transactions) {
            if ("SUCCESS".equalsIgnoreCase(t.getStatus())) {
                switch (t.getTransactionType().toUpperCase()) {
                    case "DEPOSIT":    totalDeposits    = totalDeposits.add(t.getAmount());    break;
                    case "WITHDRAWAL": totalWithdrawals = totalWithdrawals.add(t.getAmount()); break;
                    case "TRANSFER":   totalTransfers   = totalTransfers.add(t.getAmount());   break;
                }
            }
        }

        System.out.println("\n========== Daily Summary: " + targetDate + " ==========");
        System.out.println("Account       : " + accountId);
        System.out.println("Transactions  : " + transactions.size());
        System.out.printf ("Total Deposits    : %,.2f%n", totalDeposits);
        System.out.printf ("Total Withdrawals : %,.2f%n", totalWithdrawals);
        System.out.printf ("Total Transfers   : %,.2f%n", totalTransfers);
        System.out.println("=====================================================\n");
    }

    public void printStatement(String accountId) throws SQLException {
        List<Transaction> transactions = transactionDAO.findByAccountId(accountId);

        System.out.println("\n========== Transaction Statement ==========");
        System.out.println("Account: " + accountId);
        System.out.printf("%-38s %-12s %-12s %-14s %-10s%n",
                "Reference ID", "Type", "Amount", "Status", "Date");
        System.out.println("-".repeat(90));

        for (Transaction t : transactions) {
            System.out.printf("%-38s %-12s %-12s %-14s %-10s%n",
                    t.getReferenceId(),
                    t.getTransactionType(),
                    t.getAmount().toPlainString(),
                    t.getStatus(),
                    t.getTimestamp() != null ? t.getTimestamp().toLocalDate() : "N/A");
        }
        System.out.println("===========================================\n");
    }
}
