package library;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class BookFrame extends JFrame {
    private final JTextField id = new JTextField();
    private final JTextField title = new JTextField();
    private final JTextField author = new JTextField();
    private final JTextField category = new JTextField();
    private final JTextField quantity = new JTextField();
    private final JTextField search = new JTextField();
    private final DefaultTableModel model = new DefaultTableModel(
            new String[]{"ID","Title","Author","Category","Quantity","Available"}, 0) {
        public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable table = new JTable(model);

    public BookFrame() {
        setTitle("Book Management");
        setSize(900, 550);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        id.setEditable(false);

        JPanel form = new JPanel(new GridLayout(2, 6, 8, 8));
        form.setBorder(BorderFactory.createTitledBorder("Book Details"));
        form.add(new JLabel("ID")); form.add(id);
        form.add(new JLabel("Title")); form.add(title);
        form.add(new JLabel("Author")); form.add(author);
        form.add(new JLabel("Category")); form.add(category);
        form.add(new JLabel("Quantity")); form.add(quantity);

        JButton add = new JButton("Add");
        JButton update = new JButton("Update");
        JButton delete = new JButton("Delete");
        JButton clear = new JButton("Clear");
        form.add(add); form.add(update);
        form.add(delete); form.add(clear);

        JPanel top = new JPanel(new BorderLayout(8,8));
        top.add(form, BorderLayout.CENTER);
        JPanel searchPanel = new JPanel(new BorderLayout(5,5));
        searchPanel.add(new JLabel("Search title/author: "), BorderLayout.WEST);
        searchPanel.add(search, BorderLayout.CENTER);
        JButton searchBtn = new JButton("Search");
        searchPanel.add(searchBtn, BorderLayout.EAST);
        top.add(searchPanel, BorderLayout.SOUTH);

        add(new JScrollPane(table), BorderLayout.CENTER);
        add(top, BorderLayout.NORTH);

        add.addActionListener(e -> addBook());
        update.addActionListener(e -> updateBook());
        delete.addActionListener(e -> deleteBook());
        clear.addActionListener(e -> clearForm());
        searchBtn.addActionListener(e -> loadBooks(search.getText().trim()));
        search.addActionListener(e -> loadBooks(search.getText().trim()));
        table.getSelectionModel().addListSelectionListener(e -> fillFromTable());

        loadBooks("");
    }

    private void addBook() {
        try {
            String sql = "INSERT INTO books(title,author,category,quantity,available_quantity) VALUES(?,?,?,?,?)";
            try (Connection con = DBConnection.getConnection();
                 PreparedStatement ps = con.prepareStatement(sql)) {
                int q = Integer.parseInt(quantity.getText().trim());
                if (q < 1) throw new IllegalArgumentException("Quantity must be at least 1.");
                ps.setString(1, title.getText().trim());
                ps.setString(2, author.getText().trim());
                ps.setString(3, category.getText().trim());
                ps.setInt(4, q);
                ps.setInt(5, q);
                ps.executeUpdate();
            }
            message("Book added successfully.");
            clearForm(); loadBooks("");
        } catch (Exception ex) { error(ex); }
    }

    private void updateBook() {
        if (id.getText().isBlank()) { message("Select a book first."); return; }
        try {
            int bookId = Integer.parseInt(id.getText());
            int q = Integer.parseInt(quantity.getText().trim());

            String getSql = "SELECT quantity, available_quantity FROM books WHERE id=?";
            String sql = "UPDATE books SET title=?,author=?,category=?,quantity=?,available_quantity=? WHERE id=?";

            try (Connection con = DBConnection.getConnection();
                 PreparedStatement get = con.prepareStatement(getSql);
                 PreparedStatement ps = con.prepareStatement(sql)) {

                get.setInt(1, bookId);
                int oldQ, oldAvailable;
                try (ResultSet rs = get.executeQuery()) {
                    if (!rs.next()) throw new IllegalArgumentException("Book not found.");
                    oldQ = rs.getInt(1);
                    oldAvailable = rs.getInt(2);
                }

                int issued = oldQ - oldAvailable;
                if (q < issued) throw new IllegalArgumentException(
                        "Quantity cannot be less than currently issued copies (" + issued + ").");

                int newAvailable = q - issued;
                ps.setString(1, title.getText().trim());
                ps.setString(2, author.getText().trim());
                ps.setString(3, category.getText().trim());
                ps.setInt(4, q);
                ps.setInt(5, newAvailable);
                ps.setInt(6, bookId);
                ps.executeUpdate();
            }
            message("Book updated successfully.");
            clearForm(); loadBooks("");
        } catch (Exception ex) { error(ex); }
    }

    private void deleteBook() {
        if (id.getText().isBlank()) { message("Select a book first."); return; }
        if (JOptionPane.showConfirmDialog(this, "Delete this book?",
                "Confirm", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement("DELETE FROM books WHERE id=?")) {
            ps.setInt(1, Integer.parseInt(id.getText()));
            ps.executeUpdate();
            message("Book deleted successfully.");
            clearForm(); loadBooks("");
        } catch (SQLException ex) {
            error(new Exception("Cannot delete this book if it has issue history.\n" + ex.getMessage()));
        }
    }

    private void loadBooks(String term) {
        model.setRowCount(0);
        String sql = "SELECT id,title,author,category,quantity,available_quantity FROM books " +
                "WHERE title LIKE ? OR author LIKE ? ORDER BY id DESC";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            String s = "%" + term + "%";
            ps.setString(1, s); ps.setString(2, s);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) model.addRow(new Object[]{
                        rs.getInt(1), rs.getString(2), rs.getString(3),
                        rs.getString(4), rs.getInt(5), rs.getInt(6)});
            }
        } catch (SQLException ex) { error(ex); }
    }

    private void fillFromTable() {
        int r = table.getSelectedRow();
        if (r < 0) return;
        id.setText(model.getValueAt(r,0).toString());
        title.setText(model.getValueAt(r,1).toString());
        author.setText(model.getValueAt(r,2).toString());
        category.setText(model.getValueAt(r,3).toString());
        quantity.setText(model.getValueAt(r,4).toString());
    }

    private void clearForm() {
        id.setText(""); title.setText(""); author.setText("");
        category.setText(""); quantity.setText("");
        table.clearSelection();
    }

    private void message(String s) { JOptionPane.showMessageDialog(this, s); }
    private void error(Exception e) { JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE); }
}
