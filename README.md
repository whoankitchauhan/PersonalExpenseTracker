# 💸 PersonalExpenseTracker

A powerful **Java + MySQL-based console application** to manage, analyze, and track personal expenses. Designed as a **complete DBMS project**, it uses advanced MySQL features like **Views, Stored Procedures, Triggers, Joins**, and includes user-friendly functionalities like **CSV export, budgeting, and visual charts**.

---

## 📋 Features

### 🔐 User Account Management

- Secure user login & registration with validation
- Password is stored securely (optionally hashed)

---

### 📦 Expense Management

- Add new expenses with:
  - Category (Food, Transport, etc.)
  - Amount
  - Date
  - Notes
- View all expenses or apply filters:
  - By category
  - By date range
- Update or delete expenses

---

### 🧾 Budget Management

- Set monthly budget
- View monthly budget status:
  - Total spent vs Budget
  - Remaining or over budget alert
- Displayed dynamically in dashboard

---

### 📈 Advanced Analytics

- **Monthly Spending Summary** using **SQL View**
- **Detailed Monthly Expense Breakdown** using **Stored Procedure**
- **Top 3 Spending Categories** (Auto-calculated by category sum)
- **Graphical Charts** (via text-based bar chart representation)

---

### 📁 Export & Backup

- Export all expenses to a `.csv` file
- Easy for record-keeping or importing to spreadsheets

---

### 🗑️ Deleted Expense Tracking

- Deleted expenses stored in separate table
- Achieved via **AFTER DELETE Trigger**
- View deleted expenses anytime

---

### 📊 Charts & Visualization

- View expense distribution chart by category
- Monthly chart to see spending trends

---

## 🛠️ Technologies Used

| Tech          | Details                            |
| ------------- | ---------------------------------- |
| **Language**  | Java (JDK 8+)                      |
| **Database**  | MySQL 8.x                          |
| **Connector** | JDBC                               |
| **IDE**       | IntelliJ / Eclipse / VS Code       |
| **External**  | CSV writer (optional Java library) |

---

## 🔧 DBMS Concepts Implemented

| Concept               | Used? | Description                                    |
| --------------------- | ----- | ---------------------------------------------- |
| Views                 | ✅    | `monthly_spending_summary` for quick summary   |
| Stored Procedures     | ✅    | `get_user_monthly_summary(username, month)`    |
| Triggers              | ✅    | `AFTER DELETE` trigger to log deleted expenses |
| Joins                 | ✅    | Budget comparison, user-expense relations      |
| Functions (optional)  | ⚠️    | Can be added to return totals                  |
| Constraints           | ✅    | Foreign key for `username` in `expenses`       |
| Grouping + Aggregates | ✅    | `SUM()`, `GROUP BY`, `ORDER BY`                |

---

## 🧪 How to Run

### 1. 🖥️ Set Up Database

1. Open MySQL and create the database:

   ```sql
   CREATE DATABASE expenses_db;
   USE expenses_db;
   ```

2. Import the SQL schema:

   - Tables: `users`, `expenses`, `deleted_expenses`, `budgets`
   - Views, Triggers, Procedures

3. (Optional) Use this:
   ```bash
   mysql -u root -p expenses_db < expenses_backup.sql
   ```

---

### 2. 🧑‍💻 Set Up Java Project

1. Create a Java project folder (`PersonalExpenseTracker`)
2. Add your `.java` files (main class, manager classes, DBConnection.java, etc.)
3. Add MySQL JDBC connector (`mysql-connector-java-x.x.xx.jar`) to classpath
4. Run `Main.java`

---

### 3. 🔑 Sample Login

- You’ll be prompted to Register or Login
- Add expenses, set budget, view stats via menu

---

## 📌 Sample SQL Snippets

### View:

```sql
CREATE OR REPLACE VIEW monthly_spending_summary AS
SELECT
    username,
    DATE_FORMAT(date, '%Y-%m') AS month,
    SUM(amount) AS total_spent
FROM
    expenses
GROUP BY
    username, month
ORDER BY
    username, month DESC;
```

### Stored Procedure:

```sql
DELIMITER //
CREATE PROCEDURE get_user_monthly_summary(IN p_username VARCHAR(100), IN p_month VARCHAR(7))
BEGIN
    SELECT id, category, amount, note, date
    FROM expenses
    WHERE username = p_username
      AND DATE_FORMAT(date, '%Y-%m') = p_month
    ORDER BY date;
END //
DELIMITER ;
```

### Trigger

```sql
DELIMITER //
CREATE TRIGGER log_deleted_expense
AFTER DELETE ON expenses
FOR EACH ROW
BEGIN
    INSERT INTO deleted_expenses (username, category, amount, note, date)
    VALUES (OLD.username, OLD.category, OLD.amount, OLD.note, OLD.date);
END //
DELIMITER ;
```
