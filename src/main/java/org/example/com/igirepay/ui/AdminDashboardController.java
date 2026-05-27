package org.example.com.igirepay.ui;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.example.com.igirepay.lab1.model.Account;
import org.example.com.igirepay.lab1.model.Customer;
import org.example.com.igirepay.lab1.model.Transaction;
import org.example.com.igirepay.lab2.dao.AccountDAO;
import org.example.com.igirepay.lab2.dao.CustomerDAO;
import org.example.com.igirepay.lab2.dao.TransactionDAO;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Admin Dashboard – read-only view of all customers, accounts, and transactions.
 * Bonus Challenge: Role-based access (Admin/User).
 */
public class AdminDashboardController implements Initializable {

    // ── Customer table ────────────────────────────────────────────────────────
    @FXML private TableView<Customer>              customerTable;
    @FXML private TableColumn<Customer, String>    colCustId;
    @FXML private TableColumn<Customer, String>    colCustName;
    @FXML private TableColumn<Customer, String>    colCustEmail;
    @FXML private TableColumn<Customer, String>    colCustPhone;

    // ── Account table ─────────────────────────────────────────────────────────
    @FXML private TableView<Account>               accountTable;
    @FXML private TableColumn<Account, String>     colAccId;
    @FXML private TableColumn<Account, String>     colAccType;
    @FXML private TableColumn<Account, String>     colAccBalance;

    // ── Transaction table ─────────────────────────────────────────────────────
    @FXML private TableView<Transaction>           transactionTable;
    @FXML private TableColumn<Transaction, String> colTxRef;
    @FXML private TableColumn<Transaction, String> colTxType;
    @FXML private TableColumn<Transaction, String> colTxAmount;
    @FXML private TableColumn<Transaction, String> colTxStatus;
    @FXML private TableColumn<Transaction, String> colTxDate;

    @FXML private Label statusLabel;
    @FXML private Label selectedCustomerLabel;
    @FXML private Label selectedAccountLabel;

    private final CustomerDAO    customerDAO    = new CustomerDAO();
    private final AccountDAO     accountDAO     = new AccountDAO();
    private final TransactionDAO transactionDAO = new TransactionDAO();

    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupCustomerTable();
        setupAccountTable();
        setupTransactionTable();
        loadAllCustomers();

        // When a customer row is clicked → load their accounts
        customerTable.setOnMouseClicked(event -> {
            Customer selected = customerTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                selectedCustomerLabel.setText(
                        "Customer: " + selected.getFullName()
                        + "  [" + selected.getCustomerId() + "]");
                loadAccountsForCustomer(selected.getCustomerId());
                transactionTable.setItems(FXCollections.observableArrayList());
                selectedAccountLabel.setText("");
            }
        });

        // When an account row is clicked → load its transactions
        accountTable.setOnMouseClicked(event -> {
            Account selected = accountTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                selectedAccountLabel.setText(
                        "Account: " + selected.getAccountId()
                        + "  [" + selected.getAccountType() + "]"
                        + "  Balance: RWF " + String.format("%,.2f", selected.getBalance()));
                loadTransactionsForAccount(selected.getAccountId());
            }
        });
    }

    // ── Setup columns ─────────────────────────────────────────────────────────

    private void setupCustomerTable() {
        colCustId.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getCustomerId()));
        colCustName.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getFullName()));
        colCustEmail.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getEmail()));
        colCustPhone.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getPhoneNumber()));
    }

    private void setupAccountTable() {
        colAccId.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getAccountId()));
        colAccType.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getAccountType()));
        colAccBalance.setCellValueFactory(d ->
                new SimpleStringProperty(
                        String.format("RWF %,.2f", d.getValue().getBalance())));
    }

    private void setupTransactionTable() {
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

    // ── Data loaders ──────────────────────────────────────────────────────────

    private void loadAllCustomers() {
        try {
            List<Customer> customers = customerDAO.findAll();
            customerTable.setItems(FXCollections.observableArrayList(customers));
            statusLabel.setText(customers.size() + " customer(s) registered.");
        } catch (SQLException e) {
            showError("Database Error", "Could not load customers.", e.getMessage());
        }
    }

    private void loadAccountsForCustomer(String customerId) {
        try {
            List<Account> accounts = accountDAO.findByCustomerId(customerId);
            accountTable.setItems(FXCollections.observableArrayList(accounts));
        } catch (SQLException e) {
            showError("Database Error", "Could not load accounts.", e.getMessage());
        }
    }

    private void loadTransactionsForAccount(String accountId) {
        try {
            List<Transaction> transactions = transactionDAO.findByAccountId(accountId);
            transactionTable.setItems(FXCollections.observableArrayList(transactions));
        } catch (SQLException e) {
            showError("Database Error", "Could not load transactions.", e.getMessage());
        }
    }

    // ── Refresh button ────────────────────────────────────────────────────────

    @FXML
    private void handleRefresh() {
        loadAllCustomers();
        accountTable.setItems(FXCollections.observableArrayList());
        transactionTable.setItems(FXCollections.observableArrayList());
        selectedCustomerLabel.setText("");
        selectedAccountLabel.setText("");
        statusLabel.setText("Refreshed.");
    }

    // ── Logout ────────────────────────────────────────────────────────────────

    @FXML
    private void handleLogout() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/org/example/com/igirepay/ui/LoginView.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) customerTable.getScene().getWindow();
            stage.setScene(new Scene(root, 500, 600));
            stage.setTitle("IgirePay – Login");
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
}
