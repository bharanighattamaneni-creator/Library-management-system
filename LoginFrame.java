package library;

import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class LoginFrame extends JFrame {
    private final JTextField username = new JTextField("admin", 18);
    private final JPasswordField password = new JPasswordField("admin123", 18);

    public LoginFrame() {
        setTitle("Library Management System - Login");
        setSize(430, 280);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(25, 30, 25, 30));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(8, 8, 8, 8);
        g.fill = GridBagConstraints.HORIZONTAL;

        JLabel title = new JLabel("LIBRARY MANAGEMENT SYSTEM", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 20));

        g.gridx=0; g.gridy=0; g.gridwidth=2;
        panel.add(title, g);

        g.gridwidth=1; g.gridy++;
        panel.add(new JLabel("Username:"), g);
        g.gridx=1;
        panel.add(username, g);

        g.gridx=0; g.gridy++;
        panel.add(new JLabel("Password:"), g);
        g.gridx=1;
        panel.add(password, g);

        JButton login = new JButton("Login");
        g.gridx=0; g.gridy++; g.gridwidth=2;
        panel.add(login, g);

        login.addActionListener(e -> authenticate());
        password.addActionListener(e -> authenticate());

        add(panel);
    }

    private void authenticate() {
        String sql = "SELECT id FROM users WHERE username=? AND password=?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, username.getText().trim());
            ps.setString(2, new String(password.getPassword()));

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    dispose();
                    new DashboardFrame().setVisible(true);
                } else {
                    JOptionPane.showMessageDialog(this,
                            "Invalid username or password.",
                            "Login Failed", JOptionPane.WARNING_MESSAGE);
                }
            }
        } catch (SQLException ex) {
            showError(ex);
        }
    }

    private void showError(Exception ex) {
        JOptionPane.showMessageDialog(this, ex.getMessage(),
                "Database Error", JOptionPane.ERROR_MESSAGE);
    }
}
