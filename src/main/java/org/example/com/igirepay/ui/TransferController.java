package org.example.com.igirepay.ui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.example.com.igirepay.lab1.model.Account;
import org.example.com.igirepay.lab2.dao.AccountService;
import org.example.com.igirepay.lab2.dao.TransactionDAO;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class TransferController implements Initializable {

    @FXML private TextField fromAccountField;
    @FXML private TextField toAccountField;
    @FXML private TextField amountField;
    @FXML private TextField referenceIdField;
    @FXML private Label     messageLabel;

    private final AccountService accountService = new AccountService();
    private final TransactionDAO transactionDAO = new TransactionDAO();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Account account = SessionManager.getSelectedAccount();
        if (account != null) {
            fromAccountField.setText(account.getAccountId());
            referenceIdField.setText(suggestRef("TRF"));
        }
    }

    @FXML
    private void handleTransfer() {
        messageLabel.setStyle("-fx-text-fill: #c0392b;");
        messageLabel.setText("");

        String fromId     = fromAccountField.getText().trim();
        String toId       = toAccountField.getText().trim();
        String amountText = amountField.getText().trim();
        String refId      = referenceIdField.getText().trim();

        if (fromId.isEmpty() || toId.isEmpty() || amountText.isEmpty() || refId.isEmpty()) {
            messageLabel.setText("All fields are required.");
            return;
        }
        if (fromId.equals(toId)) {
            messageLabel.setText("Source and destination accounts must be different.");
            return;
        }

        BigDecimal amount;
        try {
            amount = new BigDecimal(amountText);
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                messageLabel.setText("Amount must be greater than zero.");
                return;
            }
        } catch (NumberFormatException e) {
            messageLabel.setText("Please enter a valid numeric amount.");
            return;
        }

        try {
            accountService.transfer(fromId, toId, amount, refId);
            messageLabel.setStyle("-fx-text-fill: #2D6A2D;");
            messageLabel.setText("\u2714 Transfer of RWF " + amount.toPlainString()
                    + " to " + toId + " successful! Ref: " + refId);
            toAccountField.clear();
            amountField.clear();
            referenceIdField.setText(suggestRef("TRF"));
        } catch (IllegalStateException e) {
            messageLabel.setText("Error: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            messageLabel.setText("Error: " + e.getMessage());
        } catch (SQLException e) {
            showError("Database Error", "Transfer failed.", e.getMessage());
        }
    }

    @FXML
    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/org/example/com/igirepay/ui/DashboardView.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) fromAccountField.getScene().getWindow();
            stage.setScene(new Scene(root, 900, 600));
            stage.setTitle("IgirePay \u2013 Dashboard");
            stage.centerOnScreen();
        } catch (IOException e) {
            showError("Navigation Error", "Could not return to dashboard.", e.getMessage());
        }
    }

    private String suggestRef(String prefix) {
        try {
            Account account = SessionManager.getSelectedAccount();
            if (account == null) return prefix + "-001";
            long count = transactionDAO.findByAccountId(account.getAccountId()).stream()
                    .filter(t -> t.getReferenceId() != null && t.getReferenceId().startsWith(prefix))
                    .count();
            return String.format("%s-%03d", prefix, count + 1);
        } catch (SQLException e) {
            return prefix + "-001";
        }
    }

    private void showError(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title); alert.setHeaderText(header); alert.setContentText(content);
        alert.showAndWait();
    }
}
