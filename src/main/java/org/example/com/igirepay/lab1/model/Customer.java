package org.example.com.igirepay.lab1.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents an IgirePay customer.
 * Holds personal details and a list of associated accounts.
 */
public class Customer {

    private String       customerId;
    private String       fullName;
    private String       email;
    private String       phoneNumber;
    /** Hashed PIN – never store plain-text PINs. */
    private String       pinHash;
    private List<Account> accounts;

    // ── Constructors ──────────────────────────────────────────────────────────

    public Customer() {
        this.accounts = new ArrayList<>();
    }

    public Customer(String customerId, String fullName, String email, String phoneNumber) {
        this.customerId  = customerId;
        this.fullName    = fullName;
        this.email       = email;
        this.phoneNumber = phoneNumber;
        this.accounts    = new ArrayList<>();
    }

    // ── Account helpers ───────────────────────────────────────────────────────

    public void addAccount(Account account) {
        if (account != null) accounts.add(account);
    }

    public void removeAccount(Account account) {
        accounts.remove(account);
    }

    // ── Getters & Setters ─────────────────────────────────────────────────────

    public String       getCustomerId()  { return customerId; }
    public void         setCustomerId(String customerId) { this.customerId = customerId; }

    public String       getFullName()    { return fullName; }
    public void         setFullName(String fullName) { this.fullName = fullName; }

    public String       getEmail()       { return email; }
    public void         setEmail(String email) { this.email = email; }

    public String       getPhoneNumber() { return phoneNumber; }
    public void         setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String       getPinHash()     { return pinHash; }
    public void         setPinHash(String pinHash) { this.pinHash = pinHash; }

    public List<Account> getAccounts()  { return accounts; }
    public void          setAccounts(List<Account> accounts) { this.accounts = accounts; }

    // ── toString ──────────────────────────────────────────────────────────────

    @Override
    public String toString() {
        return "Customer{" +
               "customerId='"  + customerId  + '\'' +
               ", fullName='"  + fullName    + '\'' +
               ", email='"     + email       + '\'' +
               ", phone='"     + phoneNumber + '\'' +
               ", accounts="   + accounts.size() +
               '}';
    }
}
