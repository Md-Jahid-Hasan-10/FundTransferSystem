<div align="center">

# 🏦 Fund Transfer System

**A command-line banking simulator in Java, built to practise Object-Oriented Programming, design patterns and SOLID principles.**

![Java](https://img.shields.io/badge/Java-17%2B-orange?logo=openjdk&logoColor=white)
![Interface](https://img.shields.io/badge/Interface-CLI-blue)
![Concepts](https://img.shields.io/badge/Concepts-OOP%20%7C%20SOLID%20%7C%20Design%20Patterns-green)
![Dependencies](https://img.shields.io/badge/Dependencies-None-lightgrey)

</div>

---

## 📖 About the project

The Fund Transfer System simulates a small bank that runs entirely in the console. Customers can register, open accounts, deposit, withdraw and transfer money. Every sensitive operation is protected by an **OTP** and a **final confirmation**, transfers carry a **fee that goes to the bank's own account**, and bank staff can **freeze accounts** from an admin menu.

It is also a learning project. Each design pattern in the code solves a real problem. Nothing was added just to use a pattern.

> 💡 No database and no external libraries. Everything is stored in memory, so you can run it right after cloning.

---

## ✨ Features

### For customers
- 👤 **Register and log in** with a customer ID and PIN
- 🏧 **Open multiple accounts** and view balances
- 💵 **Deposit** money instantly
- 💸 **Withdraw and transfer** with OTP verification and a confirmation screen showing amount, fee and total
- 🧾 **Transaction history**, including failed and cancelled attempts
- 📊 **View my limits** (per-transaction limit, daily limit, used today, remaining)
- 🔔 **Notifications** for deposits, transfers, failures, cancellations and account freezes

### Banking rules
| Rule | How it works |
|---|---|
| **Transfer fee** | Tiered fee for transfers to other customers (default: ৳5.00, ৳9.25, ৳15.00, ৳25.00 by amount). The fee is credited to the bank's fee account. |
| **Own-account transfers** | Moving money between your own accounts is **free** and does not count toward limits. |
| **Per-transaction limit** | A single withdrawal or transfer cannot exceed **৳2,000** (configurable). |
| **Daily limit** | At most **৳3,000** can be sent out per customer per day (configurable). |
| **Minimum balance** | Every account must keep **৳100** after a withdrawal or transfer, including the fee (configurable). |
| **OTP security** | OTP is tied to one request and one customer, expires after 120 seconds, and locks after 3 wrong attempts. |
| **Confirmation** | After the OTP, the customer must approve the exact amount, fee and recipient. |
| **Safe execution** | All checks run before any balance changes, so a transfer can never leave only one account updated. |

### For bank staff (Admin)
- 👥 View all customers
- 🗂️ View all accounts with owner, role, status and balance
- 🧊 **Freeze / unfreeze** an account (the customer is notified)
- 📜 View every transaction in the system
- 💰 **Bank fee report** showing total fees collected

---

## 🚀 Getting started

### What you need
- **JDK 17 or newer** (the code uses `record` and `switch ->`)
- **IntelliJ IDEA** (recommended) or any terminal

### Option 1: IntelliJ IDEA
1. Clone the repository:
   ```bash
   git clone https://github.com/YOUR-USERNAME/FundTransferSystem.git
   ```
2. In IntelliJ choose **File → Open** and select the project folder.
3. Set **File → Project Structure → SDK** to JDK 17 or newer.
4. Mark `src` as **Sources Root** (right-click → Mark Directory as) if it is not already.
5. Open `src/bank/Main.java` and click the green ▶ next to `main`.

### Option 2: Command line
**macOS / Linux**
```bash
javac -encoding UTF-8 -d out $(find src -name "*.java")
java -Dfile.encoding=UTF-8 -cp out bank.Main
```

**Windows (PowerShell)**
```powershell
javac -encoding UTF-8 -d out (Get-ChildItem -Recurse src -Filter *.java).FullName
java -Dfile.encoding=UTF-8 -cp out bank.Main
```

> If the **৳** symbol shows as `?`, add `-Dfile.encoding=UTF-8 -Dstdout.encoding=UTF-8` to the run options, or change `currencySymbol` in `AppConfig` to `"BDT "`.

---

## 🔑 Try it out

The app starts with sample data:

| Role | Login | Details |
|---|---|---|
| Customer: Alice | `CUST1001` / PIN `1234` | Accounts `ACC1001` (৳5,000) and `ACC1003` (৳0) |
| Customer: Bob | `CUST1002` / PIN `5678` | Account `ACC1002` (৳1,000) |
| Admin | `admin` / `admin123` | Option 4 on the start menu |

The fixed OTP for this CLI version is **`123456`**. It is printed as a console notification.

### Example: a transfer
```text
From account: ACC1001
To account number: ACC1002
Amount: 500

[NOTIFICATION -> Alice (01700000001)] Your OTP for request REQ1001 is 123456. It is valid for 120 seconds.
Enter OTP (or type 'cancel'): 123456
OTP verified.

--- Please review ---
Type          : TRANSFER
From account  : ACC1001
To account    : ACC1002 (Bob)
Amount        : ৳500.00
Fee           : ৳9.25
Total deducted: ৳509.25
Are you sure you want to complete this transaction? (yes/no): yes
Transaction successful. ID: TXN1003
```

### Things worth trying
- Enter a **wrong OTP** three times and watch the request fail.
- Transfer **৳2,500** to see the per-transaction limit.
- Send ৳1,900 twice to hit the **daily limit**.
- Transfer between **ACC1001 and ACC1003** to see the free own-account transfer.
- Log in as admin, **freeze ACC1002**, then try to deposit into it as Bob.

---

## 🖥️ Menus

```text
=== Fund Transfer System ===        --- Alice ---                      --- Admin ---
1. Login                            1. View my accounts                1. View all customers
2. Register                         2. Open a new account              2. View all accounts
3. Bank fee report                  3. Deposit                         3. Freeze an account
4. Admin login                      4. Withdraw                        4. Unfreeze an account
0. Exit                             5. Transfer                        5. View all transactions
                                    6. Transaction history             0. Logout
                                    7. View my limits
                                    0. Logout
```

---

## 🧩 Design at a glance

### Design patterns
| Pattern | Where | Why |
|---|---|---|
| **Strategy** | `TransferFeePolicy` (Standard / Promotional / Flat) | Change how fees are calculated without touching the transfer logic |
| **Strategy** | `TransactionLimitPolicy` (`StandardLimitPolicy`) | Swap limit rules without touching the transfer logic |
| **Strategy** | `OtpGenerator` (Fixed / Random) | Fixed OTP for the CLI demo, random OTP when needed |
| **Factory** | `NotificationFactory` | Creates a console or file notification channel from configuration |
| **Singleton** | `AppConfig` | One shared, read-only set of settings (services are deliberately *not* singletons) |

### SOLID in practice
| Principle | Example |
|---|---|
| **S**ingle Responsibility | `Account` protects its balance, `OtpService` verifies OTPs, `NotificationService` builds messages, `FundTransferService` runs the workflow, `AdminService` handles staff operations |
| **O**pen/Closed | Add a new fee policy, limit rule or notification channel without editing `FundTransferService` |
| **L**iskov Substitution | Any policy or channel implementation works wherever its interface is expected |
| **I**nterface Segregation | Small, focused interfaces with one method each |
| **D**ependency Inversion | Services depend on interfaces, and `Main` wires the concrete classes through constructors |

### Transaction flow
```text
Request  →  Validate + calculate fee  →  Send OTP  →  Verify OTP  →  Confirm  →  Execute  →  Record + notify
```
A pending operation is a mutable `TransactionRequest`. Once it finishes, an **immutable** `Transaction` record is created.

---

## 📁 Project structure

```text
FundTransferSystem/
├── src/
    └── bank/
       ├── Main.java
       ├── model/          Customer, Account, Transaction, TransactionRequest, enums
       ├── core/           Bank (in-memory data and ID generation)
       ├── service/        FundTransferService, OtpService, NotificationService, AdminService
       ├── policy/         TransferFeePolicy (+ Standard, Promotional, Flat),
       │                   TransactionLimitPolicy (+ Standard)
       ├── otp/            OtpGenerator (+ Fixed, Random)
       ├── notification/   NotificationChannel (+ Console, File), NotificationFactory
       ├── config/         AppConfig
       ├── exception/      BankException and its subclasses
       └── ui/             ConsoleUI

```

---

## ⚙️ Configuration

All settings are in `src/bank/config/AppConfig.java`:

| Setting | Default | Meaning |
|---|---|---|
| `fixedOtp` / `fixedOtpValue` | `true` / `123456` | Use a fixed OTP, or set `false` for random 6-digit OTPs |
| `otpExpirySeconds` | `120` | How long an OTP is valid |
| `maxOtpAttempts` | `3` | Wrong attempts before the request fails |
| `requestExpirySeconds` | `300` | How long a pending request stays valid |
| `minimumBalance` | `100.00` | Balance every customer account must keep |
| `perTransactionLimit` | `2000.00` | Maximum for one withdrawal or transfer |
| `dailyLimit` | `3000.00` | Maximum sent out per customer per day |
| `notificationChannel` | `CONSOLE` | `CONSOLE` or `FILE` (writes to `notifications.log`) |
| `currencySymbol` | `৳` | Currency shown in the CLI |
| `adminUsername` / `adminPassword` | `admin` / `admin123` | Demo admin login |

The fee tiers are in `StandardTransferFeePolicy`.

---

## 🧪 Running the tests

The project includes **23 JUnit 5 tests** covering fees, OTP rules, transfer flow, limits, minimum balance, frozen accounts and the admin service.

In IntelliJ: right-click `BankSystemTest` → **Run 'BankSystemTest'**. If `@Test` is underlined, press **Alt+Enter** → **Add 'JUnit5.x' to classpath**.

---

## 🗺️ Ideas for the future

- [ ] Save and load data from files
- [ ] PIN lock after 3 wrong attempts and PIN change
- [ ] Saved beneficiaries with nicknames
- [ ] Different limits for different customer types
- [ ] Statement filters and export to CSV
- [ ] Savings and current account types
- [ ] Hashed PINs and admin passwords

---

## 🤝 Contributing

1. Create a branch from `main`: `git checkout -b feature/your-feature`
2. Make your changes and run the tests.
3. Commit with a clear message and push the branch.
4. Open a Pull Request into `main` and ask your teammate to review it.

---

## 👨‍💻 Authors

| | |
|---|---|
| **Jahid Hasan** | Developer |
| **Ashabul Yamin** | Developer |

Built together as a Java OOP learning project.

---

<div align="center">

⭐ If you found this project helpful, consider giving it a star.

</div>