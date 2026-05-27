package org.example.com.igirepay.ui;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.example.com.igirepay.lab1.model.Account;
import org.example.com.igirepay.lab1.model.Customer;
import org.example.com.igirepay.lab1.model.Transaction;
import org.example.com.igirepay.lab2.dao.AccountDAO;
import org.example.com.igirepay.lab2.dao.CustomerDAO;
import org.example.com.igirepay.lab2.dao.TransactionDAO;
import org.example.com.igirepay.lab3.auth.PinService;
import org.example.com.igirepay.lab3.report.ReportService;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class AdminDashboardController implements Initializable {

    @FXML private TableView<Customer>              customerTable;
    @FXML private TableColumn<Customer, String>    colCustId;
    @FXML private TableColumn<Customer, String>    colCustName;
    @FXML private TableColumn<Customer, String>    colCustEmail;
    @FXML private TableColumn<Customer, String>    colCustPhone;

    @FXML private TableView<Account>               accountTable;
    @FXML private TableColumn<Account, String>     colAccId;
    @FXML private TableColumn<Account, String>     colAccType;
    @FXML private TableColumn<Account, String>     colAccBalance;

    @FXML private TableView<Transaction>           transactionTable;
    @FXML private TableColumn<Transaction, String> colTxId;
    @FXML private TableColumn<Transaction, String> colTxRef;
    @FXML private TableColumn<Transaction, String> colTxType;
    @FXML private TableColumn<Transaction, String> colTxAmount;
    @FXML private TableColumn<Transaction, String> colTxStatus;
    @FXML private TableColumn<Transaction, String> colTxDate;

    @FXML private Label statusLabel;
    @FXML private Label selectedCustomerLabel;
    @FXML private Label selectedAccountLabel;
    @FXML private Label walletBalanceLabel;
    @FXML private Label savingsBalanceLabel;
    @FXML private Label transactionPanelTitle;

    private final CustomerDAO    customerDAO    = new CustomerDAO();
    private final AccountDAO     accountDAO     = new AccountDAO();
    private final TransactionDAO transactionDAO = new TransactionDAO();
    private final PinService     pinService     = new PinService();
    private final ReportService  reportService  = new ReportService();

    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private List<Account> currentAccounts = new ArrayList<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupColumns();
        loadAllCustomers();

        customerTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldVal, newVal) -> {
                    if (newVal != null) {
                        selectedCustomerLabel.setText(
                                newVal.getFullName() + "  [" + newVal.getCustomerId() + "]");
                        loadAccountsAndTransactions(newVal.getCustomerId());
                    }
                });

        accountTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldVal, newVal) -> {
                    if (newVal != null) {
                        selectedAccountLabel.setText(
                                newVal.getAccountId() + "  [" + newVal.getAccountType()
                                + "]  RWF " + String.format("%,.2f", newVal.getBalance()));
                        loadTransactionsForAccount(newVal.getAccountId());
                    }
                });
    }

    private void setupColumns() {
        colCustId.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getCustomerId()));
        colCustName.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getFullName()));
        colCustEmail.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getEmail()));
        colCustPhone.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getPhoneNumber()));

        colAccId.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getAccountId()));
        colAccType.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getAccountType()));
        colAccBalance.setCellValueFactory(d ->
                new SimpleStringProperty(
                        String.format("RWF %,.2f", d.getValue().getBalance())));

        colTxId.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getTransactionId()));
        colTxRef.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getReferenceId()));
        colTxType.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getTransactionType()));
        colTxAmount.setCellValueFactory(d ->
                new SimpleStringProperty(
                        d.getValue().getAmount() != null
                                ? String.format("%,.2f", d.getValue().getAmount()) : "0.00"));
        colTxStatus.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getStatus()));
        colTxDate.setCellValueFactory(d ->
                new SimpleStringProperty(
                        d.getValue().getTimestamp() != null
                                ? d.getValue().getTimestamp().format(FMT) : "N/A"));
    }

    private void loadAllCustomers() {
        try {
            List<Customer> customers = customerDAO.findAll();
            customerTable.setItems(FXCollections.observableArrayList(customers));
            statusLabel.setText(customers.size() + " customer(s) registered.");
        } catch (SQLException e) {
            showError("Database Error", "Could not load customers.", e.getMessage());
        }
    }

    private void loadAccountsAndTransactions(String customerId) {
        try {
            currentAccounts = accountDAO.findByCustomerId(customerId);
            accountTable.setItems(FXCollections.observableArrayList(currentAccounts));

            walletBalanceLabel.setText("");
            savingsBalanceLabel.setText("");
            for (Account a : currentAccounts) {
                if ("WALLET".equals(a.getAccountType())) {
                    walletBalanceLabel.setText("\uD83D\uDCB0 Wallet: RWF " +
                            String.format("%,.2f", a.getBalance()));
                } else if ("SAVINGS".equals(a.getAccountType())) {
                    savingsBalanceLabel.setText("\uD83C\uDFE6 Savings: RWF " +
                            String.format("%,.2f", a.getBalance()));
                }
            }

            List<Transaction> all = new ArrayList<>();
            for (Account a : currentAccounts) {
                all.addAll(transactionDAO.findByAccountId(a.getAccountId()));
            }
            transactionTable.setItems(FXCollections.observableArrayList(all));
            transactionPanelTitle.setText("\uD83D\uDCCB All Transactions");
            statusLabel.setText(all.size() + " transaction(s) found.");
            selectedAccountLabel.setText("Showing all \u2014 click account to filter");

        } catch (SQLException e) {
            showError("Database Error", "Could not load data.", e.getMessage());
        }
    }

    private void loadTransactionsForAccount(String accountId) {
        try {
            List<Transaction> transactions = transactionDAO.findByAccountId(accountId);
            transactionTable.setItems(FXCollections.observableArrayList(transactions));
            transactionPanelTitle.setText("\uD83D\uDCCB Transactions for " + accountId);
            statusLabel.setText(transactions.size() + " transaction(s) for " + accountId);
        } catch (SQLException e) {
            showError("Database Error", "Could not load transactions.", e.getMessage());
        }
    }

    @FXML
    private void handleViewAllCustomers() {
        loadAllCustomers();
        accountTable.setItems(FXCollections.observableArrayList());
        transactionTable.setItems(FXCollections.observableArrayList());
        walletBalanceLabel.setText("");
        savingsBalanceLabel.setText("");
        selectedCustomerLabel.setText("\u2190 Click a customer");
        selectedAccountLabel.setText("");
    }

    @FXML
    private void handleViewAccounts() {
        Customer c = customerTable.getSelectionModel().getSelectedItem();
        if (c == null) { statusLabel.setText("Select a customer first."); return; }
        loadAccountsAndTransactions(c.getCustomerId());
    }

    @FXML
    private void handleViewWalletBalance() {
        Customer c = customerTable.getSelectionModel().getSelectedItem();
        if (c == null) { statusLabel.setText("Select a customer first."); return; }
        try {
            List<Account> accounts = accountDAO.findByCustomerId(c.getCustomerId());
            StringBuilder sb = new StringBuilder();
            for (Account a : accounts) {
                if ("WALLET".equals(a.getAccountType())) {
                    sb.append("Wallet ").append(a.getAccountId())
                      .append(": RWF ").append(String.format("%,.2f", a.getBalance()));
                }
            }
            showInfo("Wallet Balance \u2013 " + c.getFullName(),
                    sb.length() > 0 ? sb.toString() : "No wallet account found.");
        } catch (SQLException e) {
            showError("Error", "Could not load balance.", e.getMessage());
        }
    }

    @FXML
    private void handleViewSavingsBalance() {
        Customer c = customerTable.getSelectionModel().getSelectedItem();
        if (c == null) { statusLabel.setText("Select a customer first."); return; }
        try {
            List<Account> accounts = accountDAO.findByCustomerId(c.getCustomerId());
            StringBuilder sb = new StringBuilder();
            for (Account a : accounts) {
                if ("SAVINGS".equals(a.getAccountType())) {
                    sb.append("Savings ").append(a.getAccountId())
                      .append(": RWF ").append(String.format("%,.2f", a.getBalance()));
                }
            }
            showInfo("Savings Balance \u2013 " + c.getFullName(),
                    sb.length() > 0 ? sb.toString() : "No savings account found.");
        } catch (SQLException e) {
            showError("Error", "Could not load balance.", e.getMessage());
        }
    }

    @FXML
    private void handleViewTransactions() {
        Customer c = customerTable.getSelectionModel().getSelectedItem();
        if (c == null) { statusLabel.setText("Select a customer first."); return; }
        loadAccountsAndTransactions(c.getCustomerId());
    }

    @FXML
    private void handleLockAccount() {
        Customer c = customerTable.getSelectionModel().getSelectedItem();
        if (c == null) { statusLabel.setText("Select a customer first."); return; }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Lock Account");
        confirm.setHeaderText("Lock account for " + c.getFullName() + "?");
        confirm.setContentText("The customer will not be able to log in until unlocked.");
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                pinService.lockAccount(c.getCustomerId());
                statusLabel.setText("Account LOCKED: " + c.getCustomerId());
                showInfo("Account Locked",
                        c.getFullName() + " [" + c.getCustomerId() + "] has been locked.");
            }
        });
    }

    @FXML
    private void handleUnlockAccount() {
        Customer c = customerTable.getSelectionModel().getSelectedItem();
        if (c == null) { statusLabel.setText("Select a customer first."); return; }
        pinService.unlockAccount(c.getCustomerId());
        statusLabel.setText("Account UNLOCKED: " + c.getCustomerId());
        showInfo("Account Unlocked",
                c.getFullName() + " [" + c.getCustomerId() + "] has been unlocked.");
    }

    @FXML
    private void handleViewFailed() {
        try {
            List<Transaction> all = transactionDAO.findAll();
            List<Transaction> failed = new ArrayList<>();
            for (Transaction t : all) {
                if ("FAILED".equalsIgnoreCase(t.getStatus())
                        || "DUPLICATE".equalsIgnoreCase(t.getStatus())) {
                    failed.add(t);
                }
            }
            transactionTable.setItems(FXCollections.observableArrayList(failed));
            transactionPanelTitle.setText("\u274C Failed / Duplicate Transactions");
            statusLabel.setText(failed.size() + " failed/duplicate transaction(s).");
        } catch (SQLException e) {
            showError("Error", "Could not load failed transactions.", e.getMessage());
        }
    }

    @FXML
    private void handleGenerateReport() {
        Customer c = customerTable.getSelectionModel().getSelectedItem();
        if (c == null) { statusLabel.setText("Select a customer first."); return; }

        try {
            List<Account> accounts = accountDAO.findByCustomerId(c.getCustomerId());
            if (accounts.isEmpty()) {
                statusLabel.setText("No accounts found for this customer.");
                return;
            }

            FileChooser fc = new FileChooser();
            fc.setTitle("Save Report");
            fc.setInitialFileName("report_" + c.getCustomerId() + ".csv");
            fc.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
            File file = fc.showSaveDialog(
                    (Stage) customerTable.getScene().getWindow());

            if (file != null) {
                for (Account a : accounts) {
                    String path = file.getAbsolutePath()
                            .replace(".csv", "_" + a.getAccountId() + ".csv");
                    reportService.exportToCsv(a.getAccountId(), path);
                }
                statusLabel.setText("Report generated for " + c.getFullName());
                showInfo("Report Generated",
                        "CSV report saved for all accounts of " + c.getFullName());
            }
        } catch (Exception e) {
            showError("Report Error", "Could not generate report.", e.getMessage());
        }
    }

    @FXML
    private void handleRefresh() {
        loadAllCustomers();
        accountTable.setItems(FXCollections.observableArrayList());
        transactionTable.setItems(FXCollections.observableArrayList());
        walletBalanceLabel.setText("");
        savingsBalanceLabel.setText("");
        selectedCustomerLabel.setText("\u2190 Click a customer");
        selectedAccountLabel.setText("");
        statusLabel.setText("Refreshed.");
    }

    @FXML
    private void handleLogout() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/org/example/com/igirepay/ui/LoginView.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) customerTable.getScene().getWindow();
            stage.setScene(new Scene(root, 500, 600));
            stage.setTitle("IgirePay \u2013 Login");
            stage.centerOnScreen();
        } catch (IOException e) {
            showError("Navigation Error", "Could not return to login.", e.getMessage());
        }
    }

    private void showError(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showInfo(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
