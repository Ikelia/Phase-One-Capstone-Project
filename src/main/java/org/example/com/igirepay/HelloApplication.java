package org.example.com.igirepay;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.example.com.igirepay.lab2.db.SchemaInitializer;

import java.io.IOException;
import java.sql.SQLException;

public class HelloApplication extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        try {
            SchemaInitializer.initialize();
        } catch (SQLException e) {
            System.err.println("[HelloApplication] DB init warning: " + e.getMessage());
        }

        FXMLLoader loader = new FXMLLoader(
                HelloApplication.class.getResource(
                        "/org/example/com/igirepay/ui/LoginView.fxml"));
        Parent root = loader.load();
        stage.setTitle("IgirePay \u2013 Login");
        stage.setScene(new Scene(root, 500, 600));
        stage.setResizable(true);
        stage.centerOnScreen();

        try {
            java.io.InputStream iconStream = HelloApplication.class
                    .getResourceAsStream("/org/example/com/igirepay/ui/Capture.PNG");
            if (iconStream != null) {
                stage.getIcons().add(new javafx.scene.image.Image(iconStream));
            }
        } catch (Exception ignored) {}

        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
