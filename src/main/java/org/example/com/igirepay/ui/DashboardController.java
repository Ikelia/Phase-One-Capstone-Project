package org.example.com.igirepay.ui;

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
import org.example.com.igirepay.lab2.dao.AccountDAO;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

public class DashboardController implements Initializable {

    @FXML private Label             welcomeLabel;
    @FXML private Label             customerIdLabel;
    @FXML private Label             customerNameLabel;
    @FXML private ComboBox<Account> accountComboBox;
    @FXML private Label             balanceLabel;
    @FXML private Label             accountIdLabel;
    @FXML private Label             statusLabel;

    private final AccountDAO    accountDAO     = new AccountDAO();
    private final NumberFormat  currencyFormat = NumberFormat.getNumberInstance(Locale.US);

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        currencyFormat.setMinimumFractionDigits(2);
        currencyFormat.setMaximumFractionDigits(2);

        Customer customer = SessionManager.getCurrentCustomer();
        if (customer == null) return;

        welcomeLabel.setText("Welcome, " + customer.getFullName());
        customerIdLabel.setText(customer.getCustomerId());
        customerNameLabel.setText(customer.getFullName());

        accountComboBox.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(Account item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null
                        : item.getAccountId() + "  [" + item.getAccountType() + "]");
            }
        });
        accountComboBox.setButtonCell(new ListCell<>() {
            @Override protected void updateItem(Account item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null
                        : item.getAccountId() + "  [" + item.getAccountType() + "]");
            }
        });

        loadAccounts(customer);
    }

    private void loadAccounts(Customer customer) {
        try {
            List<Account> accounts = accountDAO.findByCustomerId(customer.getCustomerId());
            if (accounts.isEmpty()) {
                statusLabel.setText("No accounts found.");
                return;
            }
            accountComboBox.setItems(FXCollections.observableArrayList(accounts));

            Account previouslySelected = SessionManager.getSelectedAccount();
            Account toSelect = accounts.get(0);

            if (previouslySelected != null) {
                for (Account a : accounts) {
                    if (a.getAccountId().equals(previouslySelected.getAccountId())) {
                        toSelect = a;
                        break;
                    }
                }
            }

            accountComboBox.getSelectionModel().select(toSelect);
            updateBalance(toSelect);
            SessionManager.setSelectedAccount(toSelect);

        } catch (SQLException e) {
            showError("Database Error", "Could not load accounts.", e.getMessage());
        }
    }

    @FXML
    private void handleAccountSelected() {
        Account selected = accountComboBox.getSelectionModel().getSelectedItem();
        if (selected != null) {
            SessionManager.setSelectedAccount(selected);
            updateBalance(selected);
        }
    }

    private void updateBalance(Account account) {
        balanceLabel.setText("RWF " + currencyFormat.format(account.getBalance()));
        accountIdLabel.setText("Account ID: " + account.getAccountId()
                + "  \u00b7  Type: " + account.getAccountType());
    }

    @FXML private void handleDeposit()  { if (check()) navigate("/org/example/com/igirepay/ui/DepositView.fxml",           "IgirePay \u2013 Deposit",  500, 420); }
    @FXML private void handleWithdraw() { if (check()) navigate("/org/example/com/igirepay/ui/WithdrawView.fxml",          "IgirePay \u2013 Withdraw", 500, 420); }
    @FXML private void handleTransfer() { if (check()) navigate("/org/example/com/igirepay/ui/TransferView.fxml",          "IgirePay \u2013 Transfer", 500, 460); }
    @FXML private void handleHistory()  { if (check()) navigate("/org/example/com/igirepay/ui/TransactionHistoryView.fxml","IgirePay \u2013 History",  900, 600); }

    @FXML
    private void handleLogout() {
        SessionManager.clear();
        navigate("/org/example/com/igirepay/ui/LoginView.fxml", "IgirePay \u2013 Login", 500, 600);
    }

    private boolean check() {
        if (SessionManager.getSelectedAccount() == null) {
            statusLabel.setText("Please select an account first.");
            return false;
        }
        return true;
    }

    private void navigate(String fxml, String title, int w, int h) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Parent root = loader.load();
            Stage stage = (Stage) welcomeLabel.getScene().getWindow();
            stage.setScene(new Scene(root, w, h));
            stage.setTitle(title);
            stage.centerOnScreen();
        } catch (IOException e) {
            showError("Navigation Error", "Could not open screen.", e.getMessage());
        }
    }

    private void showError(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title); alert.setHeaderText(header); alert.setContentText(content);
        alert.showAndWait();
    }
}
