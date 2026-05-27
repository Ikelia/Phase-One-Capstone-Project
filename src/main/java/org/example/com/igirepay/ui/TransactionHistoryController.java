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
import org.example.com.igirepay.lab1.model.Transaction;
import org.example.com.igirepay.lab2.dao.AccountService;
import org.example.com.igirepay.lab3.report.ReportService;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public class TransactionHistoryController implements Initializable {

    @FXML private Label                            accountLabel;
    @FXML private Label                            accountInfoLabel;
    @FXML private Label                            countLabel;
    @FXML private Label                            messageLabel;
    @FXML private TableView<Transaction>           transactionTable;

    @FXML private TableColumn<Transaction, String> txIdCol;
    @FXML private TableColumn<Transaction, String> refIdCol;
    @FXML private TableColumn<Transaction, String> typeCol;
    @FXML private TableColumn<Transaction, String> amountCol;
    @FXML private TableColumn<Transaction, String> statusCol;
    @FXML private TableColumn<Transaction, String> dateCol;

    private final AccountService accountService = new AccountService();
    private final ReportService  reportService  = new ReportService();
    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        txIdCol.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getTransactionId()));

        refIdCol.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getReferenceId()));

        typeCol.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getTransactionType()));

        amountCol.setCellValueFactory(d ->
                new SimpleStringProperty(
                        d.getValue().getAmount() != null
                                ? String.format("%,.2f", d.getValue().getAmount()) : "0.00"));

        statusCol.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getStatus()));

        dateCol.setCellValueFactory(d ->
                new SimpleStringProperty(
                        d.getValue().getTimestamp() != null
                                ? d.getValue().getTimestamp().format(FMT) : "N/A"));

        Account account = SessionManager.getSelectedAccount();
        if (account == null) {
            messageLabel.setText("No account selected.");
            return;
        }

        accountLabel.setText(account.getAccountId() + "  [" + account.getAccountType() + "]");
        accountInfoLabel.setText(account.getAccountId()
                + "  \u00b7  " + account.getAccountType()
                + "  \u00b7  Balance: RWF " + String.format("%,.2f", account.getBalance()));

        try {
            List<Transaction> history = accountService.getHistory(account.getAccountId());
            transactionTable.setItems(FXCollections.observableArrayList(history));
            countLabel.setText(history.size() + " transaction(s)");
        } catch (SQLException e) {
            showError("Database Error", "Could not load history.", e.getMessage());
        }
    }

    @FXML
    private void handleExportCsv() {
        Account account = SessionManager.getSelectedAccount();
        if (account == null) { messageLabel.setText("No account selected."); return; }

        FileChooser fc = new FileChooser();
        fc.setTitle("Save Transaction History");
        fc.setInitialFileName("transactions_" + account.getAccountId() + ".csv");
        fc.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        File file = fc.showSaveDialog((Stage) transactionTable.getScene().getWindow());

        if (file != null) {
            try {
                reportService.exportToCsv(account.getAccountId(), file.getAbsolutePath());
                messageLabel.setStyle("-fx-text-fill: #2D6A2D;");
                messageLabel.setText("\u2714 Exported to: " + file.getAbsolutePath());
            } catch (SQLException | IOException e) {
                showError("Export Error", "Could not export.", e.getMessage());
            }
        }
    }

    @FXML
    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/org/example/com/igirepay/ui/DashboardView.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) transactionTable.getScene().getWindow();
            stage.setScene(new Scene(root, 900, 600));
            stage.setTitle("IgirePay \u2013 Dashboard");
            stage.centerOnScreen();
        } catch (IOException e) {
            showError("Navigation Error", "Could not return to dashboard.", e.getMessage());
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
