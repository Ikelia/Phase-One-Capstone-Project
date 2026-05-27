package org.example.com.igirepay.ui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.example.com.igirepay.lab1.model.Customer;
import org.example.com.igirepay.lab1.model.SavingsAccount;
import org.example.com.igirepay.lab1.model.WalletAccount;
import org.example.com.igirepay.lab2.dao.AccountDAO;
import org.example.com.igirepay.lab2.dao.CustomerDAO;
import org.example.com.igirepay.lab3.auth.PinService;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Optional;

public class RegisterController {

    @FXML private TextField     fullNameField;
    @FXML private TextField     emailField;
    @FXML private TextField     phoneField;
    @FXML private PasswordField pinField;
    @FXML private PasswordField confirmPinField;
    @FXML private Label         messageLabel;

    private final CustomerDAO customerDAO = new CustomerDAO();
    private final AccountDAO  accountDAO  = new AccountDAO();
    private final PinService  pinService  = new PinService();

    @FXML
    private void handleRegister() {
        messageLabel.setStyle("-fx-text-fill: #c0392b;");
        messageLabel.setText("");

        String fullName   = fullNameField.getText().trim();
        String email      = emailField.getText().trim();
        String phone      = phoneField.getText().trim();
        String pin        = pinField.getText().trim();
        String confirmPin = confirmPinField.getText().trim();

        if (fullName.isEmpty() || email.isEmpty() || phone.isEmpty()
                || pin.isEmpty() || confirmPin.isEmpty()) {
            messageLabel.setText("All fields are required.");
            return;
        }
        if (!email.contains("@")) {
            messageLabel.setText("Please enter a valid email address.");
            return;
        }
        if (!phone.matches("\\d{8,15}")) {
            messageLabel.setText("Phone number must be 8–15 digits.");
            return;
        }
        if (!pin.matches("\\d{4,6}")) {
            messageLabel.setText("PIN must be 4–6 digits.");
            return;
        }
        if (!pin.equals(confirmPin)) {
            messageLabel.setText("PINs do not match.");
            return;
        }

        try {
            Optional<Customer> existing = customerDAO.findByPhone(phone);
            if (existing.isPresent()) {
                messageLabel.setText("A customer with this phone number already exists.");
                return;
            }

            String customerId = String.format("CUS-%03d", customerDAO.countAll() + 1);
            Customer customer = new Customer(customerId, fullName, email, phone);
            customerDAO.create(customer);
            pinService.createPin(customerId, pin);

            // Create a default WalletAccount
            String walletId = String.format("ACC-%03d", accountDAO.countAll() + 1);
            WalletAccount wallet = new WalletAccount(walletId, customerId,
                    BigDecimal.ZERO, new BigDecimal("5000000.00"));
            accountDAO.create(wallet);

            // Create a default SavingsAccount
            String savingsId = String.format("ACC-%03d", accountDAO.countAll() + 1);
            SavingsAccount savings = new SavingsAccount(
                            savingsId, customerId, BigDecimal.ZERO,
                            new BigDecimal("0.02"), new BigDecimal("500.00"));
            accountDAO.create(savings);

            messageLabel.setStyle("-fx-text-fill: #2D6A2D; -fx-font-size: 13px;");
            messageLabel.setText(
                "✔ Registration successful!\n\n" +
                "📋 Customer ID : " + customerId + "\n" +
                "💰 Wallet ID   : " + walletId  + "\n" +
                "🏦 Savings ID  : " + savingsId + "\n\n" +
                "Please save these IDs — you will need them to deposit,\n" +
                "withdraw, and transfer money.\n\n" +
                "Redirecting to login in 5 seconds…");

            javafx.animation.PauseTransition pause =
                    new javafx.animation.PauseTransition(javafx.util.Duration.seconds(5));
            pause.setOnFinished(e -> {
                try { navigateToLogin(); }
                catch (IOException ex) { showError("Error", "Navigation failed.", ex.getMessage()); }
            });
            pause.play();

        } catch (IllegalArgumentException e) {
            messageLabel.setText("Invalid input: " + e.getMessage());
        } catch (SQLException e) {
            showError("Database Error", "Registration failed.", e.getMessage());
        }
    }

    @FXML
    private void handleGoToLogin() {
        try { navigateToLogin(); }
        catch (IOException e) { showError("Error", "Navigation failed.", e.getMessage()); }
    }

    private void navigateToLogin() throws IOException {
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/org/example/com/igirepay/ui/LoginView.fxml"));
        Parent root = loader.load();
        Stage stage = (Stage) fullNameField.getScene().getWindow();
        stage.setScene(new Scene(root, 500, 600));
        stage.setTitle("IgirePay – Login");
        stage.centerOnScreen();
    }

    private void showError(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
