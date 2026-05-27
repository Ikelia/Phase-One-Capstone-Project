# IgirePay Payment Gateway Project
## Building Secure Data-Driven JavaFX Applications with JDBC & OOP

**Client:** IgirePay Technologies Ltd. (A Fast-Growing Payment Processor)
**Organization:** Igire Rwanda Organization (IRO)

---

## Problem Statement

IgirePay Technologies Ltd. wants to build a secure desktop-based digital wallet system inspired by MTN mobile money. The application allows users to log in using a PIN, check balances, send money, manage savings, and view transaction history. The system prevents duplicate transactions, securely manages financial data, and provides a clean user experience using JavaFX, JDBC, PostgreSQL, and Object-Oriented Programming principles.

---

## Technologies Used

| Technology | Purpose |
|---|---|
| Java 21 | Core programming language |
| JavaFX 21 | Desktop UI framework |
| JDBC + PostgreSQL | Database connectivity |
| DAO Pattern | Data access layer |
| Service Layer Architecture | Business logic layer |
| Maven | Build and dependency management |
| Git & GitHub | Version control and collaboration |

---

## Lab 1: Object-Oriented Design in Action

### Scenario
IgirePay is a fast-growing fintech company that processes digital payments for e-commerce platforms. Merchants experienced network delays during payment processing. When payment requests timeout, client systems retry the same request multiple times, resulting in duplicate charges. The task was to build the core business logic for a secure payment management system that prevents duplicate transactions using unique transaction reference IDs.

### Exercise 1.1 — Class Design with Inheritance and Encapsulation

**Classes implemented:**

**`Account.java`** (abstract base class)
- Fields: `accountId`, `customerId`, `accountType`, `balance`, `createdAt`
- Constructors: default + parameterized
- Getters and setters for all fields
- `toString()` method
- Abstract methods: `deposit()`, `withdraw()`, `processTransaction()`
- Protected helpers: `validatePositive()`, `validateSufficientBalance()`

**`WalletAccount.java`** (extends Account)
- Additional field: `transferLimit` (5,000,000 RWF default)
- Supports instant transfers with no withdrawal fee
- Overrides: `deposit()`, `withdraw()`, `processTransaction()`
- `toString()` includes transferLimit

**`SavingsAccount.java`** (extends Account)
- Additional fields: `withdrawalFeeRate` (2%), `minimumBalance` (500 RWF)
- Applies 2% fee on every withdrawal
- Enforces minimum balance after withdrawal
- Overrides: `deposit()`, `withdraw()`, `processTransaction()`
- `toString()` includes fee rate and minimum balance

**`Transaction.java`**
- Fields: `transactionId`, `referenceId`, `accountId`, `amount`, `transactionType`, `timestamp`, `status`
- Constructors: default (sets timestamp to now, status to PENDING) + parameterized
- Getters and setters for all fields
- `toString()` method

**`Customer.java`**
- Fields: `customerId`, `fullName`, `email`, `phoneNumber`, `pinHash`, `accounts` (List)
- Constructors: default + parameterized
- Getters and setters for all fields
- Helper methods: `addAccount()`, `removeAccount()`
- `toString()` method

### Exercise 1.2 — Polymorphism in Transaction Processing

- `WalletAccount.withdraw()` — no fee, only checks transfer limit and sufficient balance
- `SavingsAccount.withdraw()` — calculates 2% fee, checks minimum balance after deduction
- `WalletAccount.deposit()` — adds amount directly
- `SavingsAccount.deposit()` — adds amount directly (no cap)
- `processTransaction()` — routes DEPOSIT, WITHDRAWAL, TRANSFER to correct method per account type
- Same method name, completely different behaviour per subclass = **polymorphism**

### Exercise 1.3 — Java Collections

**`PaymentService.java`** uses:
- `Map<String, Customer>` — stores all customers keyed by customerId (O(1) lookup)
- `Map<String, Account>` — stores all accounts keyed by accountId
- `List<Transaction>` — stores full transaction history in order
- `Set<String>` — stores processed reference IDs for duplicate detection (O(1) lookup)
- `List<Transaction>` — stores failed and duplicate transaction logs

**Duplicate detection:**
- Before processing any transaction, the Set is checked for the reference ID
- If found → transaction marked DUPLICATE, added to failed log, rejected
- If not found → transaction processed, reference ID added to Set

### Key Takeaways Demonstrated
- OOP principles: encapsulation, inheritance, polymorphism, abstraction
- Collections for financial record management
- Idempotency in payment systems using Set-based duplicate detection

---

## Lab 2: Database Integration with JDBC

### Scenario
The IgirePay platform required persistent storage for customers, accounts, and transactions. PostgreSQL was integrated using JDBC.

### Exercise 2.1 — PostgreSQL Schema

Four tables created automatically on startup via `SchemaInitializer.java`:

**`customers`** — `id`, `full_name`, `email`, `phone_number`, `pin_hash`, `created_at`

**`accounts`** — `id`, `customer_id`, `account_type`, `balance`, `created_at`
- Foreign key: `customer_id` references `customers(id)` ON DELETE CASCADE
- Check constraint: `account_type IN ('WALLET', 'SAVINGS')`

**`transactions`** — `id`, `account_id`, `reference_id`, `transaction_type`, `amount`, `status`, `created_at`
- Foreign key: `account_id` references `accounts(id)`

**`processed_requests`** — `id`, `reference_id`, `processed_at`
- UNIQUE constraint on `reference_id` (database-level idempotency guard)

### Exercise 2.2 — CRUD Operations

All operations implemented:
- Adding customers ✅
- Creating accounts (wallet and savings) ✅
- Depositing money ✅
- Withdrawing money ✅
- Sending money between accounts ✅
- Viewing transaction history ✅
- Updating customer details ✅
- Deleting inactive accounts ✅

### Exercise 2.3 — PreparedStatements

Every single SQL query in all DAO classes uses `PreparedStatement` with `?` placeholders:
- Prevents SQL injection attacks
- Validates transaction reference IDs before processing
- Secures all account queries

### Exercise 2.4 — DAO Classes

**`CustomerDAO`** — `create`, `findById`, `findByEmail`, `findByPhone`, `findAll`, `update`, `updatePin`, `delete`, `countAll`

**`AccountDAO`** — `create`, `findById`, `findByCustomerId`, `findAll`, `updateBalance`, `delete`, `countAll`

**`TransactionDAO`** — `create`, `findById`, `findByAccountId`, `findAll`, `findByAccountAndDate`, `updateStatus`, `delete`, `countAll`

**`ProcessedRequestDAO`** — `markProcessed`, `exists`, `delete`

**`AccountService`** — coordinates all DAOs for atomic operations using JDBC transactions (`setAutoCommit(false)`, `commit()`, `rollback()`)

### Exercise 2.5 — Idempotency Protection

- Every transaction requires a unique reference ID
- Before processing: `ProcessedRequestDAO.exists(referenceId)` checks the database
- If exists → `IllegalStateException` thrown, transaction rejected
- After success: `ProcessedRequestDAO.markProcessed(referenceId)` stores the reference ID
- Database-level safety net: `UNIQUE` constraint on `processed_requests.reference_id`
- First request processes successfully; repeated requests are rejected permanently

### Key Takeaways Demonstrated
- JDBC fundamentals with connection management
- DAO architecture with clean separation of concerns
- PreparedStatements for SQL injection prevention
- Reliable fintech transaction workflows with idempotency

---

## Lab 3: Putting It All Together (Mini Capstone Project)

### Scenario
A complete Java application combining Labs 1 and 2 to simulate a real payment processing platform.

### Exercise 3.1 — Menu-Driven Console Application

**Lab 1 Mode (no database required):**
- Register customer, create accounts, deposit, withdraw, transfer
- View customers, transaction history, failed/duplicate log
- Duplicate detection using in-memory Set

**Lab 2/3 Mode (with PostgreSQL):**

Customer Management:
- Register customer (with duplicate email/phone validation)
- Update customer information
- View all customers
- View customer accounts

Account Management:
- Create wallet account
- Create savings account
- View account balance
- Delete inactive account

Transaction Management:
- Deposit money
- Withdraw money
- Transfer money between accounts
- View transaction history
- Duplicate transaction prevention using reference IDs

Reports:
- Export transaction history to CSV
- View daily transaction summary
- Display full transaction statement

Authentication / PIN:
- Set/reset PIN
- Validate PIN
- Change PIN
- Unlock account

### Exercise 3.2 — Exception Handling

| Exception Type | How Handled |
|---|---|
| Invalid transaction amounts | `NumberFormatException` caught, user prompted again |
| Duplicate transaction requests | `IllegalStateException` caught, clear error message shown |
| Insufficient balance | `IllegalStateException` from `validateSufficientBalance()` |
| Invalid account IDs | `IllegalArgumentException` from `orElseThrow()` |
| Database connection failures | `SQLException` caught at startup with clear message |
| SQL exceptions | Caught in every DAO and service method |

### Exercise 3.3 — Transaction Reports (`ReportService.java`)

- **CSV Export** — exports Transaction ID, Reference ID, Account ID, Type, Amount, Status, Timestamp using OpenCSV library
- **Daily Summary** — totals deposits, withdrawals, transfers for a given date
- **Transaction Statement** — formatted table of all transactions for an account

### Exercise 3.4 — Authentication (`PinService.java`)

- **PIN creation** — validates 4–6 digit format, hashes with SHA-256, stores hash only
- **PIN validation** — hashes input and compares to stored hash, tracks failed attempts
- **PIN change** — verifies old PIN before allowing new PIN to be set
- **Account locking** — locks after 3 consecutive failed attempts (Bonus)
- **Admin unlock** — admin can unlock any account

### Exercise 3.5 — Git/GitHub Collaboration

- Feature branches created: `Lab-1`, `Lab-2`, `Lab-3`
- Pull requests submitted from each branch to `main`
- Merge conflicts resolved
- Clean commit history maintained

---

## JavaFX UI (Built on top of Lab 3)

### Login Screen
- Toggle between **Customer** (phone + PIN) and **Admin** (username + password)
- IRO logo displayed
- Register link for new customers

### Register Screen
- Validates: empty fields, email format, Rwandan phone format (`0XXXXXXXXX` or `+250XXXXXXXXX`), PIN length (4–6 digits), duplicate email, duplicate phone
- Creates both Wallet and Savings accounts automatically
- Displays Customer ID and Account IDs after registration

### Dashboard
- Shows Customer ID and full name
- Account selector dropdown (Wallet and Savings)
- Live balance card
- Quick actions: Deposit, Withdraw, Transfer, History
- Balance refreshes correctly after each transaction

### Transaction Screens
- Deposit, Withdraw, Transfer — all with validation and auto-suggested reference IDs
- Transaction History — table with Transaction ID, Reference ID, Type, Amount, Status, Timestamp
- CSV export with file chooser dialog

### Admin Panel
- View all customers, accounts, and transactions
- Admin Menu (9 options): View customers, View accounts, View wallet balance, View savings balance, View recent transactions, Lock account, Unlock account, View failed transactions, Generate report

---

## Bonus Challenges Implemented

| Bonus | Status |
|---|---|
| Transaction rollback using JDBC transactions | ✅ Implemented in AccountService.java |
| Role-based access (Admin/User) | ✅ Login screen with toggle |
| Account locking after multiple failed PIN attempts | ✅ Locks after 3 failures |
| Transaction search and filtering | ✅ Admin can filter by customer/account |

---

## Setup Instructions

### Prerequisites
- Java 21 (JDK)
- PostgreSQL 14+
- Maven 3.8+
- IntelliJ IDEA

### Database Setup
```sql
CREATE DATABASE igirepay;
```
Tables are created automatically on first run.

### Configuration
Edit `DatabaseConnection.java`:
```java
private static final String DEFAULT_URL      = "jdbc:postgresql://localhost:5432/igirepay";
private static final String DEFAULT_USER     = "postgres";
private static final String DEFAULT_PASSWORD = "your_password";
```

### Running
- **JavaFX UI**: Run `HelloApplication.java`
- **Console**: Run `ConsoleApp.java` → choose Lab 1 or Lab 2/3

### Default Admin Credentials
- Username: `admin`
- Password: `admin123`

### Phone Number Format (Rwanda)
- Local: `0790563487` (10 digits starting with 0)
- International: `+250790563487` (+250 followed by 9 digits)

---

## Project Structure

```
src/main/java/org/example/com/igirepay/
├── lab1/
│   ├── model/          Account, WalletAccount, SavingsAccount, Transaction, Customer
│   └── service/        PaymentService (in-memory, List/Set/Map)
├── lab2/
│   ├── dao/            CustomerDAO, AccountDAO, TransactionDAO, ProcessedRequestDAO, AccountService
│   └── db/             DatabaseConnection, SchemaInitializer
├── lab3/
│   ├── auth/           PinService (SHA-256, account locking)
│   ├── report/         ReportService (CSV, daily summary, statement)
│   └── ConsoleApp.java Menu-driven console application
└── ui/                 JavaFX controllers, FXML views, styles.css
```

---

## GitHub Repository

[https://github.com/Ikelia/Phase-One-Capstone-Project](https://github.com/Ikelia/Phase-One-Capstone-Project)

| Branch | Contents |
|--------|----------|
| `main` | Complete project — all labs + JavaFX UI |
| `Lab-1` | OOP design — Exercise 1.1, 1.2, 1.3 |
| `Lab-2` | JDBC integration — Exercise 2.1 to 2.5 |
| `Lab-3` | Console app + JavaFX UI — Exercise 3.1 to 3.5 |
