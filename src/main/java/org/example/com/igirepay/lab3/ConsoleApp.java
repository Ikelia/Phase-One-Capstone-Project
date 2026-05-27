package org.example.com.igirepay.lab3;

import org.example.com.igirepay.lab1.model.*;
import org.example.com.igirepay.lab1.service.PaymentService;
import org.example.com.igirepay.lab2.dao.*;
import org.example.com.igirepay.lab2.db.DatabaseConnection;
import org.example.com.igirepay.lab2.db.SchemaInitializer;
import org.example.com.igirepay.lab3.auth.PinService;
import org.example.com.igirepay.lab3.report.ReportService;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.UUID;

public class ConsoleApp {

    private static final Scanner scanner = new Scanner(System.in);

    private static int customerCounter = 0;
    private static int accountCounter  = 0;

    private static int db2CustomerCounter = 0;
    private static int db2AccountCounter  = 0;

    private static int depCounter = 0;
    private static int witCounter = 0;
    private static int trfCounter = 0;

    private static String nextDepRef() { return String.format("DEP-%03d", ++depCounter); }

    private static String nextWitRef() { return String.format("WIT-%03d", ++witCounter); }

    private static String nextTrfRef() { return String.format("TRF-%03d", ++trfCounter); }

    private static final CustomerDAO    customerDAO    = new CustomerDAO();
    private static final AccountDAO     accountDAO     = new AccountDAO();
    private static final AccountService accountService = new AccountService();
    private static final PinService     pinService     = new PinService();
    private static final ReportService  reportService  = new ReportService();

    private static final PaymentService paymentService = new PaymentService();

    private static String nextCustomerId() {
        return String.format("CUS-%03d", ++customerCounter);
    }

    private static String nextAccountId() {
        return String.format("ACC-%03d", ++accountCounter);
    }

    private static String nextDbCustomerId() {
        return String.format("CUS-%03d", ++db2CustomerCounter);
    }

    private static String nextDbAccountId() {
        return String.format("ACC-%03d", ++db2AccountCounter);
    }

    private static int refCounter = 0;
    private static String nextRefId(String prefix) {
        return String.format("%s-%03d", prefix, ++refCounter);
    }

    public static void main(String[] args) {
        System.out.println("╔══════════════════════════════════════╗");
        System.out.println("║   IgirePay Digital Wallet System     ║");
        System.out.println("╚══════════════════════════════════════╝");
        System.out.println("\nSelect which lab to run:");
        System.out.println("  1. Lab 1 – OOP (no database required)");
        System.out.println("  2. Lab 2 / Lab 3 – Full app with database");
        System.out.println("  0. Exit");

        int labChoice = readInt("Choice");
        switch (labChoice) {
            case 1:
                runLab1();
                break;
            case 2:
                runLab2And3();
                break;
            case 0:
                System.out.println("Goodbye!");
                break;
            default:
                System.out.println("Invalid choice. Exiting.");
        }
    }

    private static void runLab1() {
        System.out.println("\n╔══════════════════════════════════════╗");
        System.out.println("║   LAB 1 – OOP Mode (In-Memory)       ║");
        System.out.println("╚══════════════════════════════════════╝");
        System.out.println("All data is stored in memory only.");
        System.out.println("Nothing is saved to a database.\n");

        boolean running = true;
        while (running) {
            printLab1Menu();
            int choice = readInt("Choice");
            try {
                switch (choice) {
                    case 1: lab1RegisterCustomer();     break;
                    case 2: lab1CreateAccount();        break;
                    case 3: lab1Deposit();              break;
                    case 4: lab1Withdraw();             break;
                    case 5: lab1Transfer();             break;
                    case 6: lab1ViewCustomers();        break;
                    case 7: lab1ViewTransactionHistory(); break;
                    case 8: lab1ViewFailedLog();        break;
                    case 0: running = false;            break;
                    default: System.out.println("Invalid option. Try again.");
                }
            } catch (IllegalStateException | IllegalArgumentException e) {
                System.out.println("[ERROR] " + e.getMessage());
            } catch (Exception e) {
                System.out.println("[UNEXPECTED ERROR] " + e.getMessage());
            }
        }
        System.out.println("Exiting Lab 1. Goodbye!");
    }

    private static void printLab1Menu() {
        System.out.println("\n──────────────────────────────────────");
        System.out.println("  LAB 1 MENU");
        System.out.println("──────────────────────────────────────");
        System.out.println("  1. Register customer");
        System.out.println("  2. Create account (Wallet or Savings)");
        System.out.println("  3. Deposit money");
        System.out.println("  4. Withdraw money");
        System.out.println("  5. Transfer money");
        System.out.println("  6. View all customers");
        System.out.println("  7. View transaction history");
        System.out.println("  8. View failed / duplicate log");
        System.out.println("  0. Exit");
        System.out.println("──────────────────────────────────────");
    }

    private static void lab1RegisterCustomer() {
        System.out.println("\n── Register Customer (Lab 1) ──");
        String id    = nextCustomerId();
        String name  = readString("Full name");
        String email = readString("Email");
        String phone = readString("Phone number");

        Customer customer = new Customer(id, name, email, phone);
        paymentService.registerCustomer(customer);
        System.out.println("Customer registered. ID: " + id);
        System.out.println(customer);
    }

    private static void lab1CreateAccount() {
        System.out.println("\n── Create Account (Lab 1) ──");
        System.out.println("  1. Wallet Account");
        System.out.println("  2. Savings Account");
        int type = readInt("Account type");

        String customerId = readString("Customer ID (e.g. CUS-001)");
        String initialStr = readString("Initial balance");
        BigDecimal balance = new BigDecimal(initialStr.isEmpty() ? "0" : initialStr);
        String accountId  = nextAccountId();

        Account account;
        if (type == 2) {
            account = new SavingsAccount(accountId, customerId, balance,
                    new BigDecimal("0.02"), new BigDecimal("500.00"));
            System.out.println("SavingsAccount created (2% withdrawal fee, min balance 500).");
        } else {
            account = new WalletAccount(accountId, customerId, balance,
                    new BigDecimal("5000000.00"));
            System.out.println("WalletAccount created (instant transfers, limit 5,000,000).");
        }

        paymentService.addAccount(account);
        System.out.println("Account ID: " + accountId);
        System.out.println(account);
    }

    private static void lab1Deposit() {
        System.out.println("\n── Deposit (Lab 1) ──");
        String accountId   = readString("Account ID");
        BigDecimal amount  = readAmount("Amount");
        String suggested   = nextDepRef();
        String referenceId = readString("Reference ID (e.g. " + suggested + ")");
        if (referenceId.isEmpty()) referenceId = suggested;

        Transaction tx = new Transaction(
                UUID.randomUUID().toString(), referenceId, accountId, amount, "DEPOSIT");
        boolean success = paymentService.processTransaction(tx);
        System.out.println("Result: " + (success ? "SUCCESS" : "FAILED/DUPLICATE"));
    }

    private static void lab1Withdraw() {
        System.out.println("\n── Withdraw (Lab 1) ──");
        String accountId   = readString("Account ID");
        BigDecimal amount  = readAmount("Amount");
        String suggested   = nextWitRef();
        String referenceId = readString("Reference ID (e.g. " + suggested + ")");
        if (referenceId.isEmpty()) referenceId = suggested;

        Transaction tx = new Transaction(
                UUID.randomUUID().toString(), referenceId, accountId, amount, "WITHDRAWAL");
        boolean success = paymentService.processTransaction(tx);
        System.out.println("Result: " + (success ? "SUCCESS" : "FAILED/DUPLICATE"));
    }

    private static void lab1Transfer() {
        System.out.println("\n── Transfer (Lab 1) ──");
        String fromId      = readString("Source Account ID");
        String toId        = readString("Destination Account ID");
        BigDecimal amount  = readAmount("Amount");
        String suggested   = nextTrfRef();
        String referenceId = readString("Reference ID (e.g. " + suggested + ")");
        if (referenceId.isEmpty()) referenceId = suggested;

        boolean success = paymentService.transfer(fromId, toId, amount, referenceId);
        System.out.println("Result: " + (success ? "SUCCESS" : "FAILED/DUPLICATE"));
    }

    private static void lab1ViewCustomers() {
        System.out.println("\n── All Customers (Lab 1) ──");
        if (paymentService.getAllCustomers().isEmpty()) {
            System.out.println("No customers registered yet.");
            return;
        }
        for (Customer c : paymentService.getAllCustomers()) {
            System.out.println(c);
            System.out.println("  Accounts:");
            for (Account a : c.getAccounts()) {
                System.out.println("    " + a);
            }
        }
    }

    private static void lab1ViewTransactionHistory() {
        System.out.println("\n── Transaction History (Lab 1) ──");
        List<Transaction> history = paymentService.getTransactionHistory();
        if (history.isEmpty()) {
            System.out.println("No transactions yet.");
            return;
        }
        System.out.printf("%-38s %-12s %-12s %-10s%n", "Reference ID", "Type", "Amount", "Status");
        System.out.println("-".repeat(75));
        for (Transaction t : history) {
            System.out.printf("%-38s %-12s %-12s %-10s%n",
                    t.getReferenceId(), t.getTransactionType(),
                    t.getAmount().toPlainString(), t.getStatus());
        }
        System.out.println("\nProcessed Reference IDs (Set – duplicate detection):");
        for (String refId : paymentService.getProcessedReferenceIds()) {
            System.out.println("  " + refId);
        }
    }

    private static void lab1ViewFailedLog() {
        System.out.println("\n── Failed / Duplicate Transaction Log (Lab 1) ──");
        List<Transaction> failed = paymentService.getFailedTransactionLog();
        if (failed.isEmpty()) {
            System.out.println("No failed or duplicate transactions.");
            return;
        }
        for (Transaction t : failed) {
            System.out.println(t);
        }
    }

    private static void runLab2And3() {
        try {
            SchemaInitializer.initialize();
            db2CustomerCounter = customerDAO.countAll();
            db2AccountCounter  = accountDAO.countAll();
        } catch (SQLException e) {
            System.err.println("[FATAL] Cannot connect to database: " + e.getMessage());
            System.err.println("Please ensure PostgreSQL is running and credentials are correct.");
            return;
        }

        boolean running = true;
        while (running) {
            printMainMenu();
            int choice = readInt("Choice");
            try {
                switch (choice) {
                    case 1: customerMenu();    break;
                    case 2: accountMenu();     break;
                    case 3: transactionMenu(); break;
                    case 4: reportMenu();      break;
                    case 5: authMenu();        break;
                    case 0: running = false;   break;
                    default: System.out.println("Invalid option. Try again.");
                }
            } catch (IllegalStateException | IllegalArgumentException e) {
                System.out.println("[ERROR] " + e.getMessage());
            } catch (SecurityException e) {
                System.out.println("[SECURITY] " + e.getMessage());
            } catch (SQLException e) {
                System.out.println("[DB ERROR] " + e.getMessage());
            } catch (Exception e) {
                System.out.println("[UNEXPECTED ERROR] " + e.getMessage());
            }
        }

        DatabaseConnection.close();
        System.out.println("Goodbye!");
    }

    private static void printMainMenu() {
        System.out.println("\n──────────────────────────────────────");
        System.out.println("  MAIN MENU (Lab 2 / Lab 3)");
        System.out.println("──────────────────────────────────────");
        System.out.println("  1. Customer Management");
        System.out.println("  2. Account Management");
        System.out.println("  3. Transaction Management");
        System.out.println("  4. Reports");
        System.out.println("  5. Authentication / PIN");
        System.out.println("  0. Exit");
        System.out.println("──────────────────────────────────────");
    }

    private static void customerMenu() throws SQLException {
        System.out.println("\n── Customer Management ──");
        System.out.println("  1. Register customer");
        System.out.println("  2. Update customer information");
        System.out.println("  3. View all customers");
        System.out.println("  4. View customer accounts");
        System.out.println("  0. Back");
        int choice = readInt("Choice");
        switch (choice) {
            case 1: registerCustomer();     break;
            case 2: updateCustomer();       break;
            case 3: viewAllCustomers();     break;
            case 4: viewCustomerAccounts(); break;
            case 0: break;
            default: System.out.println("Invalid option.");
        }
    }

    private static void registerCustomer() throws SQLException {
        System.out.println("\n── Register New Customer ──");
        String name  = readString("Full name");
        String email = readString("Email");
        String phone = readString("Phone number");

        Optional<Customer> existing = customerDAO.findByEmail(email);
        if (existing.isPresent()) {
            System.out.println("[ERROR] A customer with this email already exists.");
            return;
        }

        String id = nextDbCustomerId();
        Customer customer = new Customer(id, name, email, phone);
        customerDAO.create(customer);
        System.out.println("Customer registered. ID: " + id);

        String pin = readString("Set PIN (4-6 digits, or press Enter to skip)");
        if (!pin.isEmpty()) {
            pinService.createPin(id, pin);
        }
    }

    private static void updateCustomer() throws SQLException {
        String id = readString("Customer ID");
        Optional<Customer> opt = customerDAO.findById(id);
        if (opt.isEmpty()) { System.out.println("Customer not found."); return; }

        Customer c = opt.get();
        System.out.println("Current: " + c);
        String name  = readString("New full name  (Enter to keep: " + c.getFullName() + ")");
        String email = readString("New email      (Enter to keep: " + c.getEmail() + ")");
        String phone = readString("New phone      (Enter to keep: " + c.getPhoneNumber() + ")");

        if (!name.isEmpty())  c.setFullName(name);
        if (!email.isEmpty()) c.setEmail(email);
        if (!phone.isEmpty()) c.setPhoneNumber(phone);

        customerDAO.update(c);
        System.out.println("Customer updated.");
    }

    private static void viewAllCustomers() throws SQLException {
        List<Customer> customers = customerDAO.findAll();
        if (customers.isEmpty()) { System.out.println("No customers found."); return; }
        System.out.printf("%n%-36s %-25s %-30s %-15s%n", "ID", "Name", "Email", "Phone");
        System.out.println("-".repeat(110));
        for (Customer c : customers) {
            System.out.printf("%-36s %-25s %-30s %-15s%n",
                    c.getCustomerId(), c.getFullName(), c.getEmail(), c.getPhoneNumber());
        }
    }

    private static void viewCustomerAccounts() throws SQLException {
        String customerId = readString("Customer ID");
        List<Account> accounts = accountDAO.findByCustomerId(customerId);
        if (accounts.isEmpty()) { System.out.println("No accounts found for this customer."); return; }
        System.out.printf("%n%-36s %-10s %-15s%n", "Account ID", "Type", "Balance");
        System.out.println("-".repeat(65));
        for (Account a : accounts) {
            System.out.printf("%-36s %-10s %,.2f%n",
                    a.getAccountId(), a.getAccountType(), a.getBalance());
        }
    }

    private static void accountMenu() throws SQLException {
        System.out.println("\n── Account Management ──");
        System.out.println("  1. Create wallet account");
        System.out.println("  2. Create savings account");
        System.out.println("  3. View account balance");
        System.out.println("  4. Delete inactive account");
        System.out.println("  0. Back");
        int choice = readInt("Choice");
        switch (choice) {
            case 1: createAccount("WALLET");  break;
            case 2: createAccount("SAVINGS"); break;
            case 3: viewBalance();            break;
            case 4: deleteAccount();          break;
            case 0: break;
            default: System.out.println("Invalid option.");
        }
    }

    private static void createAccount(String type) throws SQLException {
        String customerId = readString("Customer ID");
        Optional<Customer> opt = customerDAO.findById(customerId);
        if (opt.isEmpty()) { System.out.println("Customer not found."); return; }

        String initialStr = readString("Initial deposit amount (0 for none)");
        BigDecimal initial = new BigDecimal(initialStr.isEmpty() ? "0" : initialStr);

        String id = nextDbAccountId();
        Account account;
        if ("SAVINGS".equals(type)) {
            account = new SavingsAccount(id, customerId,
                    initial, new BigDecimal("0.02"), new BigDecimal("500.00"));
        } else {
            account = new WalletAccount(id, customerId,
                    initial, new BigDecimal("5000000.00"));
        }

        accountDAO.create(account);
        System.out.println(type + " account created. ID: " + id);
    }

    private static void viewBalance() throws SQLException {
        String accountId = readString("Account ID");
        Optional<Account> opt = accountDAO.findById(accountId);
        if (opt.isEmpty()) { System.out.println("Account not found."); return; }
        Account a = opt.get();
        System.out.printf("Account: %s | Type: %s | Balance: %,.2f%n",
                a.getAccountId(), a.getAccountType(), a.getBalance());
    }

    private static void deleteAccount() throws SQLException {
        String accountId = readString("Account ID to delete");
        Optional<Account> opt = accountDAO.findById(accountId);
        if (opt.isEmpty()) { System.out.println("Account not found."); return; }
        Account a = opt.get();
        if (a.getBalance().compareTo(BigDecimal.ZERO) > 0) {
            System.out.println("[WARNING] Account still has balance " + a.getBalance() +
                               ". Confirm deletion? (yes/no)");
            if (!"yes".equalsIgnoreCase(readString(""))) {
                System.out.println("Deletion cancelled.");
                return;
            }
        }
        accountDAO.delete(accountId);
        System.out.println("Account deleted.");
    }

    private static void transactionMenu() throws SQLException {
        System.out.println("\n── Transaction Management ──");
        System.out.println("  1. Deposit money");
        System.out.println("  2. Withdraw money");
        System.out.println("  3. Transfer money");
        System.out.println("  4. View transaction history");
        System.out.println("  0. Back");
        int choice = readInt("Choice");
        switch (choice) {
            case 1: doDeposit();   break;
            case 2: doWithdraw();  break;
            case 3: doTransfer();  break;
            case 4: viewHistory(); break;
            case 0: break;
            default: System.out.println("Invalid option.");
        }
    }

    private static void doDeposit() throws SQLException {
        String accountId   = readString("Account ID");
        BigDecimal amount  = readAmount("Amount to deposit");
        String suggested   = nextRefId("DEP");
        String referenceId = readString("Reference ID (e.g. " + suggested + ")");
        if (referenceId.isEmpty()) referenceId = suggested;
        accountService.deposit(accountId, amount, referenceId);
    }

    private static void doWithdraw() throws SQLException {
        String accountId   = readString("Account ID");
        BigDecimal amount  = readAmount("Amount to withdraw");
        String suggested   = nextRefId("WIT");
        String referenceId = readString("Reference ID (e.g. " + suggested + ")");
        if (referenceId.isEmpty()) referenceId = suggested;
        accountService.withdraw(accountId, amount, referenceId);
    }

    private static void doTransfer() throws SQLException {
        String fromId      = readString("Source account ID");
        String toId        = readString("Destination account ID");
        BigDecimal amount  = readAmount("Amount to transfer");
        String suggested   = nextRefId("TRF");
        String referenceId = readString("Reference ID (e.g. " + suggested + ")");
        if (referenceId.isEmpty()) referenceId = suggested;
        accountService.transfer(fromId, toId, amount, referenceId);
    }

    private static void viewHistory() throws SQLException {
        String accountId = readString("Account ID");
        List<Transaction> history = accountService.getHistory(accountId);
        if (history.isEmpty()) { System.out.println("No transactions found."); return; }
        System.out.printf("%n%-38s %-12s %-12s %-10s %-10s%n",
                "Reference ID", "Type", "Amount", "Status", "Date");
        System.out.println("-".repeat(85));
        for (Transaction t : history) {
            System.out.printf("%-38s %-12s %-12s %-10s %-10s%n",
                    t.getReferenceId(), t.getTransactionType(),
                    t.getAmount().toPlainString(), t.getStatus(),
                    t.getTimestamp() != null ? t.getTimestamp().toLocalDate() : "N/A");
        }
    }

    private static void reportMenu() throws Exception {
        System.out.println("\n── Reports ──");
        System.out.println("  1. Export transaction history to CSV");
        System.out.println("  2. Daily transaction summary");
        System.out.println("  3. Full transaction statement");
        System.out.println("  0. Back");
        int choice = readInt("Choice");
        switch (choice) {
            case 1: exportCsv();     break;
            case 2: dailySummary();  break;
            case 3: fullStatement(); break;
            case 0: break;
            default: System.out.println("Invalid option.");
        }
    }

    private static void exportCsv() throws Exception {
        String accountId = readString("Account ID");
        String path      = readString("Output file path (e.g. transactions.csv)");
        reportService.exportToCsv(accountId, path);
    }

    private static void dailySummary() throws SQLException {
        String accountId = readString("Account ID");
        String date      = readString("Date (YYYY-MM-DD, or Enter for today)");
        reportService.printDailySummary(accountId, date.isEmpty() ? null : date);
    }

    private static void fullStatement() throws SQLException {
        String accountId = readString("Account ID");
        reportService.printStatement(accountId);
    }

    private static void authMenu() throws SQLException {
        System.out.println("\n── Authentication / PIN ──");
        System.out.println("  1. Set / reset PIN");
        System.out.println("  2. Validate PIN");
        System.out.println("  3. Change PIN");
        System.out.println("  4. Unlock account (admin)");
        System.out.println("  0. Back");
        int choice = readInt("Choice");
        switch (choice) {
            case 1: setPin();        break;
            case 2: validatePin();   break;
            case 3: changePin();     break;
            case 4: unlockAccount(); break;
            case 0: break;
            default: System.out.println("Invalid option.");
        }
    }

    private static void setPin() throws SQLException {
        String customerId = readString("Customer ID");
        String pin        = readString("New PIN (4-6 digits)");
        pinService.createPin(customerId, pin);
        System.out.println("PIN set successfully.");
    }

    private static void validatePin() throws SQLException {
        String customerId = readString("Customer ID");
        String pin        = readString("Enter PIN");
        boolean valid = pinService.validatePin(customerId, pin);
        System.out.println(valid ? "PIN is VALID." : "PIN is INVALID.");
    }

    private static void changePin() throws SQLException {
        String customerId = readString("Customer ID");
        String oldPin     = readString("Current PIN");
        String newPin     = readString("New PIN (4-6 digits)");
        pinService.changePin(customerId, oldPin, newPin);
    }

    private static void unlockAccount() {
        String customerId = readString("Customer ID to unlock");
        pinService.unlockAccount(customerId);
    }

    private static String readString(String prompt) {
        System.out.print(prompt + ": ");
        return scanner.nextLine().trim();
    }

    private static int readInt(String prompt) {
        System.out.print(prompt + ": ");
        String line = scanner.nextLine().trim();
        try {
            return Integer.parseInt(line);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private static BigDecimal readAmount(String prompt) {
        while (true) {
            System.out.print(prompt + ": ");
            String line = scanner.nextLine().trim();
            try {
                BigDecimal amount = new BigDecimal(line);
                if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                    System.out.println("Amount must be greater than zero.");
                    continue;
                }
                return amount;
            } catch (NumberFormatException e) {
                System.out.println("Invalid amount. Please enter a numeric value.");
            }
        }
    }
}
