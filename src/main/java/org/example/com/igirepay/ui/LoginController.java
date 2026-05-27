package org.example.com.igirepay.ui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.example.com.igirepay.lab1.model.Customer;
import org.example.com.igirepay.lab2.dao.CustomerDAO;
import org.example.com.igirepay.lab3.auth.PinService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Optional;

public class LoginController {

    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_PASSWORD = "admin123";

    @FXML private RadioButton   userRadio;
    @FXML private RadioButton   adminRadio;

    @FXML private VBox          userFields;
    @FXML private TextField     phoneField;
    @FXML private PasswordField pinField;

    @FXML private VBox          adminFields;
    @FXML private TextField     usernameField;
    @FXML private PasswordField passwordField;

    @FXML private Label         errorLabel;
    @FXML private HBox          registerBox;

    private final CustomerDAO customerDAO = new CustomerDAO();
    private final PinService  pinService  = new PinService();

    @FXML
    private void handleToggle() {
        boolean isAdmin = adminRadio.isSelected();

        userFields.setVisible(!isAdmin);
        userFields.setManaged(!isAdmin);
        adminFields.setVisible(isAdmin);
        adminFields.setManaged(isAdmin);

        registerBox.setVisible(!isAdmin);
        registerBox.setManaged(!isAdmin);

        errorLabel.setText("");
    }

    @FXML
    private void handleLogin() {
        errorLabel.setText("");

        if (adminRadio.isSelected()) {
            handleAdminLogin();
        } else {
            handleUserLogin();
        }
    }

    private void handleAdminLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Please enter username and password.");
            return;
        }

        if (username.equals(ADMIN_USERNAME) && password.equals(ADMIN_PASSWORD)) {
            openAdminDashboard();
        } else {
            errorLabel.setText("Invalid admin credentials.");
        }
    }

    private void handleUserLogin() {
        String phone = phoneField.getText().trim();
        String pin   = pinField.getText().trim();

        if (phone.isEmpty() || pin.isEmpty()) {
            errorLabel.setText("Please enter phone number and PIN.");
            return;
        }

        try {
            Optional<Customer> opt = customerDAO.findByPhone(phone);
            if (opt.isEmpty()) {
                errorLabel.setText("No account found for this phone number.");
                return;
            }
            Customer customer = opt.get();
            if (customer.getPinHash() == null) {
                errorLabel.setText("No PIN set for this account. Contact support.");
                return;
            }
            boolean valid = pinService.validatePin(customer.getCustomerId(), pin);
            if (!valid) {
                errorLabel.setText("Incorrect PIN. Please try again.");
                return;
            }
            SessionManager.setCurrentCustomer(customer);
            openUserDashboard();
        } catch (SecurityException e) {
            errorLabel.setText("Account locked: " + e.getMessage());
        } catch (SQLException e) {
            showError("Database Error", "Could not connect.", e.getMessage());
        } catch (IOException e) {
            showError("Navigation Error", "Could not open dashboard.", e.getMessage());
        }
    }

    @FXML
    private void handleGoToRegister() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/org/example/com/igirepay/ui/RegisterView.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) phoneField.getScene().getWindow();
            stage.setScene(new Scene(root, 500, 640));
            stage.setTitle("IgirePay \u2013 Register");
            stage.centerOnScreen();
        } catch (IOException e) {
            showError("Navigation Error", "Could not open register screen.", e.getMessage());
        }
    }

    private void openUserDashboard() throws IOException {
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/org/example/com/igirepay/ui/DashboardView.fxml"));
        Parent root = loader.load();
        Stage stage = (Stage) phoneField.getScene().getWindow();
        stage.setScene(new Scene(root, 900, 600));
        stage.setTitle("IgirePay \u2013 Dashboard");
        stage.centerOnScreen();
    }

    private void openAdminDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource(
                            "/org/example/com/igirepay/ui/AdminDashboardView.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(new Scene(root, 1200, 700));
            stage.setTitle("IgirePay \u2013 Admin Panel");
            stage.centerOnScreen();
        } catch (IOException e) {
            showError("Navigation Error", "Could not open admin panel.", e.getMessage());
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
