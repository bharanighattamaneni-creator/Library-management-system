package library;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}

            try (Connection ignored = DBConnection.getConnection()) {
                new LoginFrame().setVisible(true);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(
                        null,
                        "Cannot connect to MySQL.\n\n" +
                        "Check MySQL is running, database.sql was executed,\n" +
                        "and DBConnection.java has the correct password.\n\n" +
                        "Error: " + e.getMessage(),
                        "Database Connection Error",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        });
    }
}
