import java.io.FileWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Scanner;
import java.sql.ResultSet;
import java.sql.Statement;

public class ExpenseManager {

    public static void addExpense(String username) {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter category: ");
        String category = scanner.nextLine();

        System.out.print("Enter amount: ");
        double amount = scanner.nextDouble();
        scanner.nextLine(); // consume newline

        System.out.print("Enter note: ");
        String note = scanner.nextLine();

        LocalDate date = LocalDate.now();
        LocalTime time = LocalTime.now();

        try (Connection conn = DBConnection.getConnection()) {
            String sql = "INSERT INTO expenses (username, date, time, category, amount, note) VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);
            stmt.setDate(2, java.sql.Date.valueOf(date));
            stmt.setTime(3, java.sql.Time.valueOf(time));
            stmt.setString(4, category);
            stmt.setDouble(5, amount);
            stmt.setString(6, note);

            int rows = stmt.executeUpdate();
            if (rows > 0) {
                System.out.println("Expense added successfully!");
            }
        } catch (SQLException e) {
            System.out.println("Error adding expense: " + e.getMessage());
        }
    }

    public static void viewExpenses(String username) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("\n--- View Expenses ---");
        System.out.println("1. View All Expenses");
        System.out.println("2. Filter by Category");
        System.out.println("3. Filter by Date Range");
        System.out.println("4. Monthly Summary");
        System.out.println("5. Export to CSV");
        System.out.println("6. View Charts");

        System.out.print("Enter your choice: ");
        int choice = scanner.nextInt();
        scanner.nextLine(); // consume newline

        switch (choice) {
            case 1:
                viewAllExpenses(username);
                break;
            case 2:
                System.out.print("Enter category: ");
                String category = scanner.nextLine();
                viewByCategory(username, category);
                break;
            case 3:
                System.out.print("Enter start date (YYYY-MM-DD): ");
                String start = scanner.nextLine();
                System.out.print("Enter end date (YYYY-MM-DD): ");
                String end = scanner.nextLine();
                viewByDateRange(username, start, end);
                break;
            case 4:
                viewMonthlySummary(username);
                break;
            case 5:
                exportToCSV(username);
                break;
            case 6:
                showChartMenu(username);
                break;

            default:
                System.out.println("Invalid choice.");
        }
    }

    private static void viewAllExpenses(String username) {
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT * FROM expenses WHERE username = ? ORDER BY date DESC, time DESC";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            System.out.println("\n--- Your Expenses ---");
            while (rs.next()) {
                int id = rs.getInt("id");
                String category = rs.getString("category");
                double amount = rs.getDouble("amount");
                String note = rs.getString("note");
                String date = rs.getString("date");
                String time = rs.getString("time");

                System.out.printf("ID: %d | %s %s | %.2f | %s | %s\n",
                        id, date, time, amount, category, note);
            }
        } catch (SQLException e) {
            System.out.println("Error viewing expenses: " + e.getMessage());
        }
    }

    private static void viewByCategory(String username, String category) {
        String sql = "SELECT * FROM expenses WHERE username = ? AND category = ? ORDER BY date DESC, time DESC";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setString(2, category);
            ResultSet rs = stmt.executeQuery();
            printExpenses(rs);
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void viewByDateRange(String username, String startDate, String endDate) {
        String sql = "SELECT * FROM expenses WHERE username = ? AND date BETWEEN ? AND ? ORDER BY date DESC, time DESC";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setString(2, startDate);
            stmt.setString(3, endDate);
            ResultSet rs = stmt.executeQuery();
            printExpenses(rs);
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void viewMonthlySummary(String username) {
        String sql = "SELECT DATE_FORMAT(date, '%Y-%m') AS month, SUM(amount) AS total " +
                "FROM expenses WHERE username = ? GROUP BY month ORDER BY month DESC";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            System.out.println("\n--- Monthly Summary ---");
            while (rs.next()) {
                System.out.println("Month: " + rs.getString("month") + " | Total: ₹" + rs.getDouble("total"));
            }
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void showChartMenu(String username) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("\n--- Chart Options ---");
        System.out.println("1. Expense Distribution by Category (Pie Chart)");
        System.out.println("2. Monthly Expense Summary (Bar Chart)");
        System.out.print("Enter your choice: ");
        int chartChoice = scanner.nextInt();

        switch (chartChoice) {
            case 1:
                ChartManager.showCategoryPieChart(username);
                break;
            case 2:
                ChartManager.showMonthlyBarChart(username);
                break;
            default:
                System.out.println("Invalid chart option.");
        }
    }

    private static void printExpenses(ResultSet rs) throws SQLException {
        System.out.println("\n--- Filtered Expenses ---");
        while (rs.next()) {
            System.out.printf("ID: %d | %s %s | ₹%.2f | %s | %s\n",
                    rs.getInt("id"),
                    rs.getString("date"),
                    rs.getString("time"),
                    rs.getDouble("amount"),
                    rs.getString("category"),
                    rs.getString("note"));
        }
    }

    public static void deleteExpense(String username) {
        Scanner scanner = new Scanner(System.in);

        // Show all expenses before asking for ID
        try (Connection conn = DBConnection.getConnection()) {
            String fetchSql = "SELECT * FROM expenses WHERE username = ? ORDER BY date DESC, time DESC";
            PreparedStatement fetchStmt = conn.prepareStatement(fetchSql);
            fetchStmt.setString(1, username);
            ResultSet rs = fetchStmt.executeQuery();

            System.out.println("\n--- Your Recent Expenses ---");
            while (rs.next()) {
                int id = rs.getInt("id");
                String date = rs.getString("date");
                String time = rs.getString("time");
                String category = rs.getString("category");
                double amount = rs.getDouble("amount");
                String note = rs.getString("note");

                System.out.printf("ID: %d | %s %s | ₹%.2f | %s | %s\n",
                        id, date, time, amount, category, note);
            }
        } catch (SQLException e) {
            System.out.println("Error fetching expenses: " + e.getMessage());
            return;
        }

        // Now ask for the ID to delete
        System.out.print("\nEnter the ID of the expense to delete: ");
        int idToDelete = scanner.nextInt();

        try (Connection conn = DBConnection.getConnection()) {
            String deleteSql = "DELETE FROM expenses WHERE id = ? AND username = ?";
            PreparedStatement deleteStmt = conn.prepareStatement(deleteSql);
            deleteStmt.setInt(1, idToDelete);
            deleteStmt.setString(2, username);

            int rows = deleteStmt.executeUpdate();
            if (rows > 0) {
                System.out.println("Expense deleted successfully.");
            } else {
                System.out.println("No such expense found for this user.");
            }
        } catch (SQLException e) {
            System.out.println("Error deleting expense: " + e.getMessage());
        }
    }

    public static void updateExpense(String username) {
        Scanner scanner = new Scanner(System.in);

        // Show recent expenses
        try (Connection conn = DBConnection.getConnection()) {
            String fetchSql = "SELECT * FROM expenses WHERE username = ? ORDER BY date DESC, time DESC";
            PreparedStatement fetchStmt = conn.prepareStatement(fetchSql);
            fetchStmt.setString(1, username);
            ResultSet rs = fetchStmt.executeQuery();

            System.out.println("\n--- Your Recent Expenses ---");
            while (rs.next()) {
                int id = rs.getInt("id");
                String date = rs.getString("date");
                String time = rs.getString("time");
                String category = rs.getString("category");
                double amount = rs.getDouble("amount");
                String note = rs.getString("note");

                System.out.printf("ID: %d | %s %s | ₹%.2f | %s | %s\n",
                        id, date, time, amount, category, note);
            }
        } catch (SQLException e) {
            System.out.println("Error fetching expenses: " + e.getMessage());
            return;
        }

        // Ask for ID to update
        System.out.print("\nEnter the ID of the expense to update: ");
        int id = scanner.nextInt();
        scanner.nextLine(); // consume newline

        // Fetch current values for the selected ID
        String currentCategory = "";
        double currentAmount = 0;
        String currentNote = "";

        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT category, amount, note FROM expenses WHERE id = ? AND username = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, id);
            stmt.setString(2, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                currentCategory = rs.getString("category");
                currentAmount = rs.getDouble("amount");
                currentNote = rs.getString("note");

                System.out.println("\n--- Current Details ---");
                System.out.println("Category: " + currentCategory);
                System.out.println("Amount  : ₹" + currentAmount);
                System.out.println("Note    : " + currentNote);
            } else {
                System.out.println("No such expense found for this user.");
                return;
            }
        } catch (SQLException e) {
            System.out.println("Error retrieving expense details: " + e.getMessage());
            return;
        }

        // Ask for new values
        System.out.print("Enter new category (leave blank to keep current): ");
        String categoryInput = scanner.nextLine();
        String newCategory = categoryInput.isEmpty() ? currentCategory : categoryInput;

        System.out.print("Enter new amount (or -1 to keep current): ");
        double amountInput = scanner.nextDouble();
        scanner.nextLine(); // consume newline
        double newAmount = amountInput < 0 ? currentAmount : amountInput;

        System.out.print("Enter new note (leave blank to keep current): ");
        String noteInput = scanner.nextLine();
        String newNote = noteInput.isEmpty() ? currentNote : noteInput;

        // Perform update
        try (Connection conn = DBConnection.getConnection()) {
            String updateSql = "UPDATE expenses SET category = ?, amount = ?, note = ? WHERE id = ? AND username = ?";
            PreparedStatement stmt = conn.prepareStatement(updateSql);
            stmt.setString(1, newCategory);
            stmt.setDouble(2, newAmount);
            stmt.setString(3, newNote);
            stmt.setInt(4, id);
            stmt.setString(5, username);

            int rows = stmt.executeUpdate();
            if (rows > 0) {
                System.out.println("Expense updated successfully.");
            } else {
                System.out.println("No changes were made.");
            }
        } catch (SQLException e) {
            System.out.println("Error updating expense: " + e.getMessage());
        }
    }

    private static void exportToCSV(String username) {
        String sql = "SELECT * FROM expenses WHERE username = ? ORDER BY date DESC, time DESC";
        String fileName = "expenses_" + username + ".csv";

        try (Connection conn = DBConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            FileWriter csvWriter = new FileWriter(fileName);
            csvWriter.append("ID,Date,Time,Category,Amount,Note\n");

            while (rs.next()) {
                csvWriter.append(rs.getInt("id") + ",");
                csvWriter.append(rs.getString("date") + ",");
                csvWriter.append(rs.getString("time") + ",");
                csvWriter.append(rs.getString("category") + ",");
                csvWriter.append(rs.getDouble("amount") + ",");
                csvWriter.append(rs.getString("note").replace(",", " ") + "\n"); // Remove commas from notes
            }

            csvWriter.flush();
            csvWriter.close();

            System.out.println("Exported to " + fileName + " successfully.");

        } catch (Exception e) {
            System.out.println("Error exporting to CSV: " + e.getMessage());
        }
    }

    public static void showTopCategories(String username) {
        String sql = "SELECT category, SUM(amount) AS total " +
                "FROM expenses WHERE username = ? " +
                "GROUP BY category ORDER BY total DESC LIMIT 3";

        try (Connection conn = DBConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            System.out.println("\n--- Top 3 Spending Categories ---");
            int rank = 1;
            while (rs.next()) {
                String category = rs.getString("category");
                double total = rs.getDouble("total");
                System.out.printf("%d. %s - ₹%.2f\n", rank++, category, total);
            }

        } catch (SQLException e) {
            System.out.println("Error retrieving top categories: " + e.getMessage());
        }
    }

    public static void viewDeletedExpenses(String username) {
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT * FROM deleted_expenses_log WHERE username = ? ORDER BY deleted_at DESC";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            System.out.println("\n--- Deleted Expenses Log ---");
            while (rs.next()) {
                System.out.printf("ID: %d | %s %s | ₹%.2f | %s | %s | Deleted on: %s\n",
                        rs.getInt("id"),
                        rs.getString("date"),
                        rs.getString("time"),
                        rs.getDouble("amount"),
                        rs.getString("category"),
                        rs.getString("note"),
                        rs.getTimestamp("deleted_at"));
            }
        } catch (SQLException e) {
            System.out.println("Error retrieving deleted logs: " + e.getMessage());
        }
    }

}
