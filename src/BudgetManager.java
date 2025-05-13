import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public class BudgetManager {

    public static void setMonthlyBudget(String username) {
        try (Scanner scanner = new Scanner(System.in)) {
            System.out.print("Enter month (YYYY-MM): ");
            String month = scanner.nextLine();

            System.out.print("Enter your budget for " + month + ": ₹");
            double budget = scanner.nextDouble();

            String sql = "REPLACE INTO budgets (username, month, budget) VALUES (?, ?, ?)";

            try (Connection conn = DBConnection.getConnection();
                    PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setString(1, username);
                stmt.setString(2, month);
                stmt.setDouble(3, budget);
                stmt.executeUpdate();

                System.out.println("Budget set for " + month + " successfully.");

            } catch (SQLException e) {
                System.out.println("Error setting budget: " + e.getMessage());
            }
        }
    }

    public static void viewBudgetStatus(String username) {
        try (Scanner scanner = new Scanner(System.in)) {
            System.out.print("Enter month to view budget status (YYYY-MM): ");
            String month = scanner.nextLine();

            String budgetSql = "SELECT budget FROM budgets WHERE username = ? AND month = ?";
            String expenseSql = "SELECT SUM(amount) AS total_spent FROM expenses WHERE username = ? AND DATE_FORMAT(date, '%Y-%m') = ?";

            try (Connection conn = DBConnection.getConnection()) {

                PreparedStatement budgetStmt = conn.prepareStatement(budgetSql);
                budgetStmt.setString(1, username);
                budgetStmt.setString(2, month);
                ResultSet budgetRs = budgetStmt.executeQuery();

                if (!budgetRs.next()) {
                    System.out.println("No budget set for " + month + ". Please set one first.");
                    return;
                }

                double budget = budgetRs.getDouble("budget");

                PreparedStatement expenseStmt = conn.prepareStatement(expenseSql);
                expenseStmt.setString(1, username);
                expenseStmt.setString(2, month);
                ResultSet expenseRs = expenseStmt.executeQuery();

                double spent = 0;
                if (expenseRs.next()) {
                    spent = expenseRs.getDouble("total_spent");
                }

                double remaining = budget - spent;
                double percent = (spent / budget) * 100;

                System.out.printf("Budget: ₹%.2f\n", budget);
                System.out.printf("Spent: ₹%.2f\n", spent);
                System.out.printf("Remaining: ₹%.2f\n", remaining);
                System.out.printf("Usage: %.2f%%\n", percent);

                if (percent > 100) {
                    System.out.println("You've exceeded your budget!");
                } else if (percent > 90) {
                    System.out.println("Warning: You are close to exceeding your budget!");
                } else {
                    System.out.println("You're on track with your budget.");
                }

            } catch (SQLException e) {
                System.out.println("Error checking budget status: " + e.getMessage());
            }
        }
    }

    public static void showDashboard(String username) {
        String month = java.time.LocalDate.now().toString().substring(0, 7); // e.g., "2025-05"

        double budget = 0;
        double spent = 0;

        try (Connection conn = DBConnection.getConnection()) {
            // Get budget
            String budgetSql = "SELECT budget FROM budgets WHERE username = ? AND month = ?";
            try (PreparedStatement stmt = conn.prepareStatement(budgetSql)) {
                stmt.setString(1, username);
                stmt.setString(2, month);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    budget = rs.getDouble("budget");
                }
            }

            // Get total spent
            String spentSql = "SELECT SUM(amount) AS total FROM expenses WHERE username = ? AND DATE_FORMAT(date, '%Y-%m') = ?";
            try (PreparedStatement stmt = conn.prepareStatement(spentSql)) {
                stmt.setString(1, username);
                stmt.setString(2, month);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    spent = rs.getDouble("total");
                }
            }

            // Display dashboard
            System.out.println("\n--- Dashboard Summary for " + month + " ---");
            System.out.println("Total Spent: " + String.format("%.2f", spent));
            if (budget > 0) {
                double remaining = budget - spent;
                System.out.println("Budget: " + budget);
                System.out.println("Remaining: " + String.format("%.2f", remaining));
                if (remaining < 0) {
                    System.out.println("You have exceeded your budget!");
                }
            } else {
                System.out.println("No budget set for this month.");
            }

        } catch (SQLException e) {
            System.out.println("❌ Error loading dashboard: " + e.getMessage());
        }
    }

}
