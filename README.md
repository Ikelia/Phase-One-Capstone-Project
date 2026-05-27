# IgirePay Digital Wallet System

**Client:** IgirePay Technologies Ltd.
**Organization:** Igire Rwanda Organization (IRO)
**Technologies:** Java 21 · JavaFX 21 · JDBC · PostgreSQL · Maven · Git/GitHub

---

## What I Built

This project is a secure desktop-based digital wallet system inspired by MTN Mobile Money. It was built across three labs, each adding a new layer of functionality.

---

## Lab 1 — Object-Oriented Design in Action

### What I Did
I designed the core business logic using Java OOP principles.

### Classes Created

**Account.java** (abstract base class)
- Fields: accountId, customerId, accountType, balance, createdAt
- Abstract methods: deposit(), withdraw(), processTransaction()
- Shared helpers: validatePositive(), validateSufficientBalance()

**WalletAccount.java** (extends Account)
- Supports instant transfers with no withdrawal fee
- Has a transfer limit of 5,000,000 RWF
- Overrides: deposit(), withdraw(), processTransaction()

**SavingsAccount.java** (extends Account)
- Applies a 2% withdrawal fee on every withdrawal
- Enforces a minimum balance of 500 RWF
- Overrides: deposit(), withdraw(), processTransaction()

**Transaction.java**
- Fields: transactionId, referenceId, accountId, amount, transactionType, timestamp, status

**Customer.java**
- Fields: customerId, fullName, email, phoneNumber, pinHash, accounts (List)

### PaymentService.java
- Uses **Map** to store customers and accounts (fast O(1) lookup)
- Uses **List** to store transaction history and failed transaction logs
- Uses **Set** of processed reference IDs to detect and reject duplicate transactions
- Implements idempotency: same reference ID cannot be processed twice

### Key OOP Concepts Applied
- **Encapsulation**: all fields are private, accessed via getters/setters
- **Inheritance**: WalletAccount and SavingsAccount extend Account
- **Polymorphism**: same method names behave differently per account type
- **Abstraction**: Account is abstract, cannot be instantiated directly

---

## Lab 2 — Database Integration with JDBC

### What I Did
I connected the application to PostgreSQL using JDBC and implemented persistent storage.

### Database Schema (4 tables)

**customers** — id, full_name, email, phone_number, pin_hash, created_at
**accounts** — id, customer_id, account_type, balance, created_at
**transactions** — id, account_id, reference_id, transaction_type, amount, status, created_at
**processed_requests** — id, reference_id, processed_at

### DAO Classes Created

**CustomerDAO** — Create, Read (by ID/email/phone/all), Update, Delete, countAll
**AccountDAO** — Create, Read (by ID/customerId/all), Update balance, Delete, countAll
**TransactionDAO** — Create, Read (by ID/accountId/date/all), Update status, Delete, countAll
**ProcessedRequestDAO** — markProcessed, exists, delete

### AccountService.java (Service Layer)
- Coordinates all DAOs for atomic operations
- Uses `conn.setAutoCommit(false)` for JDBC transactions
- Implements `conn.rollback()` on failure (Bonus: transaction rollback)
- Checks for duplicate reference IDs before processing any transaction

### Security
- All SQL queries use **PreparedStatements** to prevent SQL injection
- Reference IDs validated before processing
- Duplicate transactions rejected at both Java and database level (UNIQUE constraint)

### Idempotency Protection
- Every transaction requires a unique reference ID
- Before processing: checks if reference ID already exists in processed_requests table
- After success: stores reference ID so it cannot be used again
- First request processes successfully; repeated requests are rejected

---

## Lab 3 — Console Application + JavaFX UI (Capstone)

### What I Did
I combined Labs 1 and 2 into a complete payment management platform with both a console interface and a JavaFX graphical interface.

### Console Application (ConsoleApp.java)

**Lab 1 Mode (no database required)**
- Register customer
- Create Wallet or Savings account
- Deposit, Withdraw, Transfer
- View all customers and their accounts
- View transaction history with processed reference IDs (Set)
- View failed/duplicate transaction log

**Lab 2/3 Mode (requires PostgreSQL)**

Customer Management:
- Register customer (with duplicate email/phone check)
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

### Exception Handling Implemented
- Invalid transaction amounts → caught with NumberFormatException
- Duplicate transaction requests → caught with IllegalStateException
- Insufficient balance → caught with IllegalStateException from validateSufficientBalance()
- Invalid account IDs → caught with IllegalArgumentException from orElseThrow()
- Database connection failures → caught with SQLException at startup
- SQL exceptions → caught in every DAO and service method

### Authentication (PinService.java)
- PIN creation with SHA-256 hashing (never stored in plain text)
- PIN validation with failed attempt tracking
- PIN change (requires old PIN verification)
- Account locking after 3 consecutive failed PIN attempts (Bonus)
- Admin can lock/unlock accounts

### Reports (ReportService.java)
- CSV export using OpenCSV library
- Daily transaction summary with totals per type
- Full transaction statement printed to console

### JavaFX UI

**Login Screen**
- Toggle between Customer login (phone + PIN) and Admin login (username + password)
- IRO logo displayed
- Register link for new customers

**Register Screen**
- Full name, email, phone number, PIN, confirm PIN
- Validates: empty fields, email format, Rwandan phone format (0XXXXXXXXX or +250XXXXXXXXX), PIN length (4-6 digits), duplicate email/phone
- Creates both a Wallet and Savings account automatically
- Shows Customer ID and Account IDs after registration

**Dashboard**
- Displays Customer ID and full name
- Account selector dropdown (Wallet and Savings)
- Live balance card with green gradient
- Quick action buttons: Deposit, Withdraw, Transfer, History
- Balance refreshes correctly after each transaction

**Deposit Screen**
- Pre-filled account ID
- Amount validation (must be positive)
- Auto-suggested reference ID (DEP-001, DEP-002...)
- Success/error message displayed inline

**Withdraw Screen**
- Same structure as deposit
- Handles insufficient balance error
- Handles duplicate reference ID error

**Transfer Screen**
- Source account pre-filled
- Destination account ID input
- Validates source ≠ destination
- Auto-suggested reference ID (TRF-001...)

**Transaction History Screen**
- Table with all required columns: Transaction ID, Reference ID, Type, Amount, Status, Timestamp
- Export to CSV button with file chooser dialog

**Admin Panel**
- View all customers in a table
- Click customer → see all their accounts with balances
- Click account → see all transactions
- Admin Menu (9 options):
  1. View all customers
  2. View customer accounts
  3. View wallet balance
  4. View savings balance
  5. View recent transactions
  6. Lock account
  7. Unlock account
  8. View failed transactions
  9. Generate report (CSV)

### Bonus Features Implemented
- JDBC transaction rollback on failure
- Role-based access: Customer and Admin
- Account locking after 3 failed PIN attempts
- Transaction ID format: TRN-001, TRN-002...
- Customer ID format: CUS-001, CUS-002...
- Account ID format: ACC-001, ACC-002...

---

## Setup Instructions

### Prerequisites
- Java 21 (JDK)
- PostgreSQL 14+
- Maven 3.8+
- IntelliJ IDEA

### Database Setup
1. Open pgAdmin
2. Create database: `CREATE DATABASE igirepay;`
3. Tables are created automatically on first run

### Configuration
Edit `src/main/java/org/example/com/igirepay/lab2/db/DatabaseConnection.java`:
```java
private static final String DEFAULT_URL      = "jdbc:postgresql://localhost:5432/igirepay";
private static final String DEFAULT_USER     = "postgres";
private static final String DEFAULT_PASSWORD = "your_password_here";
```

### Running
- **JavaFX UI**: Run `HelloApplication.java`
- **Console**: Run `ConsoleApp.java` → choose Lab 1 or Lab 2/3

### Default Admin Credentials
- Username: `admin`
- Password: `admin123`

### Phone Number Format (Rwanda)
- Local: `0790563487` (10 digits)
- International: `+250790563487`

---

## GitHub Repository
[https://github.com/Ikelia/Phase-One-Capstone-Project](https://github.com/Ikelia/Phase-One-Capstone-Project)

### Branch Structure
| Branch | Contents |
|--------|----------|
| `main` | Complete project with all labs and JavaFX UI |
| `Lab-1` | OOP design — Account, WalletAccount, SavingsAccount, Transaction, Customer, PaymentService |
| `Lab-2` | JDBC integration — DAOs, SchemaInitializer, AccountService, idempotency |
| `Lab-3` | Console app + JavaFX UI + Auth + Reports + Admin panel |
