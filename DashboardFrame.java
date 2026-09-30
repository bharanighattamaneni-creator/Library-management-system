package library;

import javax.swing.*;
import java.awt.*;

public class DashboardFrame extends JFrame {
    public DashboardFrame() {
        setTitle("Library Management System - Dashboard");
        setSize(650, 430);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JLabel title = new JLabel("Library Management Dashboard", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 26));

        JPanel grid = new JPanel(new GridLayout(3, 2, 15, 15));
        grid.setBorder(BorderFactory.createEmptyBorder(25, 50, 25, 50));

        JButton books = new JButton("Book Management");
        JButton members = new JButton("Member Management");
        JButton issue = new JButton("Issue Book");
        JButton returns = new JButton("Return Book");
        JButton refresh = new JButton("Database Test");
        JButton logout = new JButton("Logout");

        grid.add(books);
        grid.add(members);
        grid.add(issue);
        grid.add(returns);
        grid.add(refresh);
        grid.add(logout);

        books.addActionListener(e -> new BookFrame().setVisible(true));
        members.addActionListener(e -> new MemberFrame().setVisible(true));
        issue.addActionListener(e -> new IssueBookFrame().setVisible(true));
        returns.addActionListener(e -> new ReturnBookFrame().setVisible(true));
        refresh.addActionListener(e -> testDatabase());
        logout.addActionListener(e -> {
            dispose();
            new LoginFrame().setVisible(true);
        });

        setLayout(new BorderLayout(10, 10));
        add(title, BorderLayout.NORTH);
        add(grid, BorderLayout.CENTER);
    }

    private void testDatabase() {
        try (var con = DBConnection.getConnection()) {
            JOptionPane.showMessageDialog(this, "Database connection is working.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
