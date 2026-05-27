module org.example.com.igirepay {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires net.synedra.validatorfx;
    requires org.kordamp.bootstrapfx.core;

    requires java.sql;
    requires com.opencsv;

    // Base package
    opens org.example.com.igirepay to javafx.fxml;
    exports org.example.com.igirepay;

    // UI package – opened for @FXML injection
    opens org.example.com.igirepay.ui to javafx.fxml;
    exports org.example.com.igirepay.ui;

    // Lab 1 – OOP model
    exports org.example.com.igirepay.lab1.model;
    exports org.example.com.igirepay.lab1.service;

    // Lab 2 – JDBC / DAO
    exports org.example.com.igirepay.lab2.db;
    exports org.example.com.igirepay.lab2.dao;

    // Lab 3 – Console + Auth + Reports
    exports org.example.com.igirepay.lab3;
    exports org.example.com.igirepay.lab3.auth;
    exports org.example.com.igirepay.lab3.report;
}
