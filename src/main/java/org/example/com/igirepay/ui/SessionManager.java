package org.example.com.igirepay.ui;

import org.example.com.igirepay.lab1.model.Account;
import org.example.com.igirepay.lab1.model.Customer;

/**
 * Simple static session holder that carries the logged-in customer and
 * the currently selected account between controllers.
 */
public class SessionManager {

    private static Customer currentCustomer;
    private static Account  selectedAccount;

    private SessionManager() {}

    public static Customer getCurrentCustomer() { return currentCustomer; }
    public static void setCurrentCustomer(Customer customer) { currentCustomer = customer; }

    public static Account getSelectedAccount() { return selectedAccount; }
    public static void setSelectedAccount(Account account) { selectedAccount = account; }

    public static void clear() {
        currentCustomer = null;
        selectedAccount = null;
    }
}
