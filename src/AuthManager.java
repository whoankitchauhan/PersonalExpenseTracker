import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public class AuthManager {
    public static String registerUser() {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter a username: ");
        String username = scanner.nextLine();

        System.out.print("Enter a password: ");
        String password = scanner.nextLine();

        try (Connection conn = DBConnection.getConnection()) {
            String sql = "INSERT INTO users (username, password) VALUES (?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);
            stmt.setString(2, PasswordUtil.hashPassword(password));

            stmt.executeUpdate();
            System.out.println("Registration successful!");
            return username;

        } catch (SQLException e) {
            System.out.println("Registration failed: " + e.getMessage());
            return null;
        }

    }

    public static String loginUser() {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter your username: ");
        String username = scanner.nextLine();

        System.out.print("Enter your password: ");
        String password = scanner.nextLine();

        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT password FROM users WHERE username = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String storedHash = rs.getString("password");
                String inputHash = PasswordUtil.hashPassword(password);

                if (storedHash.equals(inputHash)) {
                    System.out.println("Login successful!");
                    return username;
                } else {
                    System.out.println("Incorrect password.");
                    return null;
                }
            } else {
                System.out.println("User not found.");
                return null;
            }

        } catch (SQLException e) {
            System.out.println("Login failed: " + e.getMessage());
            return null;
        }

    }

}
