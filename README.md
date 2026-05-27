# IgirePay Digital Wallet System

**Client:** IgirePay Technologies Ltd.  
**Built by:** Igire Rwanda Organization (IRO)  
**Technologies:** Java 21 · JavaFX 21 · JDBC · PostgreSQL · Maven

---

## Project Overview

IgirePay is a secure desktop-based digital wallet system inspired by MTN Mobile Money. It allows customers to log in using a PIN, check balances, send money, manage savings, and view transaction history. The system prevents duplicate transactions using unique reference IDs and provides a clean JavaFX user interface.

---

## Project Structure

```
src/main/java/org/example/com/igirepay/
├── lab1/
│   ├── model/          # OOP classes: Account, WalletAccount, SavingsAccount, Transaction, Customer
│   └── service/        # PaymentService (in-memory, uses List/Set/Map)
├── lab2/
│   ├── dao/            # CustomerDAO, AccountDAO, TransactionDAO, ProcessedRequestDAO, AccountService
│   └── db/             # DatabaseConnection, SchemaInitializer
├── lab3/
│   ├── auth/           # PinService (SHA-256 hashing, account locking)
│   ├── report/         # ReportService (CSV export, daily summary, statement)
│   └── ConsoleApp.java # Menu-driven console application
└── ui/                 # JavaFX controllers and FXML views
```

---

## Labs Summary

### Lab 1 — Object-Oriented Design
- **Account** (abstract base class) with `deposit()`, `withdraw()`, `processTransaction()`
- **WalletAccount** — instant transfers, no fee, 5M transfer limit
- **SavingsAccount** — 2% withdrawal fee, minimum balance of 500 RWF
- **Transaction** — transactionId, referenceId, amount, type, timestamp
- **Customer** — holds a list of accounts
- **PaymentService** — uses `Map` for customers/accounts, `List` for history, `Set` for duplicate detection

### Lab 2 — Database Integration with JDBC
- PostgreSQL schema with 4 tables: `customers`, `accounts`, `transactions`, `processed_requests`
- Full CRUD operations via DAO pattern
- All queries use `PreparedStatements` to prevent SQL injection
- Idempotency protection: every transaction requires a unique reference ID
- JDBC transaction rollback on failure (Bonus)

### Lab 3 — Console Application + JavaFX UI
- Menu-driven console app combining Labs 1 and 2
- Exception handling for all error types
- CSV export, daily summaries, transaction statements
- PIN authentication with SHA-256 hashing
- Account locking after 3 failed PIN attempts (Bonus)
- Role-based access: Customer and Admin (Bonus)
- JavaFX UI with Login, Register, Dashboard, Deposit, Withdraw, Transfer, History screens

---

## Setup Instructions

### Prerequisites
- Java 21 (JDK)
- PostgreSQL 14+
- Maven 3.8+

### Database Setup
1. Open pgAdmin or psql
2. Create a database:
   ```sql
   CREATE DATABASE igirepay;
   ```
3. Tables are created automatically on first run

### Configuration
Edit `src/main/java/org/example/com/igirepay/lab2/db/DatabaseConnection.java`:
```java
private static final String DEFAULT_URL      = "jdbc:postgresql://localhost:5432/igirepay";
private static final String DEFAULT_USER     = "postgres";
private static final String DEFAULT_PASSWORD = "your_password_here";
```

### Running the Application

**JavaFX UI (recommended):**
```
Run HelloApplication.java in IntelliJ
```

**Console App:**
```
Run ConsoleApp.java in IntelliJ
Select 1 for Lab 1 (no database) or 2 for Lab 2/3 (requires PostgreSQL)
```

---

## Default Credentials

**Admin Login:**
- Username: `admin`
- Password: `admin123`

**Customer Login:**
- Phone number used during registration
- PIN set during registration (4–6 digits)

---

## Phone Number Format (Rwanda)
- Local: `0790563487` (10 digits starting with 0)
- International: `+250790563487` (+250 followed by 9 digits)

---

## Bonus Features Implemented
- ✅ JDBC transaction rollback
- ✅ Role-based access (Admin/User)
- ✅ Account locking after 3 failed PIN attempts
- ✅ Transaction search and filtering (Admin panel)
- ✅ IRO logo integration

---

## GitHub Repository
[https://github.com/Ikelia/Phase-One-Capstone-Project](https://github.com/Ikelia/Phase-One-Capstone-Project)

### Branch Structure
- `main` — complete project
- `Lab-1` — OOP design (Exercise 1.1–1.3)
- `Lab-2` — JDBC integration (Exercise 2.1–2.5)
- `Lab-3` — Console + JavaFX capstone (Exercise 3.1–3.5)
