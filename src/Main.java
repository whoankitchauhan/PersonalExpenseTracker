import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        // Welcome message
        System.out.println("======================================");
        System.out.println(" Welcome to Daily Expenses Manager ");
        System.out.println(" Track your money smartly & easily! ");
        System.out.println("======================================");

        String loggedInUser = null;

        while (true) {
            System.out.println("\n--- Main Menu ---");
            System.out.println("1. Login");
            System.out.println("2. Register");
            System.out.println("3. Exit");
            System.out.print("Enter choice: ");
            int authChoice = scanner.nextInt();
            scanner.nextLine(); // consume newline

            switch (authChoice) {
                case 1:
                    loggedInUser = AuthManager.loginUser();
                    break;
                case 2:
                    loggedInUser = AuthManager.registerUser();
                    break;
                case 3:
                    System.out.println("Thank you for using Daily Expenses Manager. Goodbye!");
                    return;
                default:
                    System.out.println("Invalid choice. Please try again.");
                    continue;
            }

            if (loggedInUser != null) {
                System.out.println("\nWelcome, " + loggedInUser + "!");
                BudgetManager.showDashboard(loggedInUser); // 👈 Show dashboard
                break; // proceed to expense menu
            } else {
                System.out.println("Authentication failed. Try again.");
            }
        }

        // Logged-in expense management menu
        while (true) {
            System.out.println("\n--- Expense Manager ---");
            System.out.println("1. Add Expense");
            System.out.println("2. View Expenses");
            System.out.println("3. Delete Expense");
            System.out.println("4. Update Expense");
            System.out.println("5. Set Monthly Budget");
            System.out.println("6. View Budget Status");
            System.out.println("7. Top 3 Spending Categories");
            System.out.println("8. View Deleted Expenses");
            System.out.println("9. Logout / Exit");
            System.out.print("Enter choice: ");
            int choice = scanner.nextInt();

            switch (choice) {
                case 1:
                    ExpenseManager.addExpense(loggedInUser);
                    break;
                case 2:
                    ExpenseManager.viewExpenses(loggedInUser);
                    break;
                case 3:
                    ExpenseManager.deleteExpense(loggedInUser);
                    break;
                case 4:
                    ExpenseManager.updateExpense(loggedInUser);
                    break;
                case 5:
                    BudgetManager.setMonthlyBudget(loggedInUser);
                    break;
                case 6:
                    BudgetManager.viewBudgetStatus(loggedInUser);
                    break;
                case 7:
                    ExpenseManager.showTopCategories(loggedInUser);
                    break;
                case 8:
                    ExpenseManager.viewDeletedExpenses(loggedInUser);
                    break;
                case 9:
                    System.out.println("Logging out. Goodbye, " + loggedInUser + "!");
                    return;

                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }
}
