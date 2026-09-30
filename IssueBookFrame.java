package library;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.time.LocalDate;

public class IssueBookFrame extends JFrame {
    private final JTextField bookId = new JTextField();
    private final JTextField memberId = new JTextField();
    private final JTextField dueDate = new JTextField(LocalDate.now().plusDays(14).toString());

    public IssueBookFrame() {
        setTitle("Issue Book");
        setSize(450, 300);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel p=new JPanel(new GridLayout(4,2,10,10));
        p.setBorder(BorderFactory.createEmptyBorder(25,25,25,25));
        p.add(new JLabel("Book ID:")); p.add(bookId);
        p.add(new JLabel("Member ID:")); p.add(memberId);
        p.add(new JLabel("Due Date (YYYY-MM-DD):")); p.add(dueDate);
        JButton issue=new JButton("Issue Book");
        JButton clear=new JButton("Clear");
        p.add(issue);p.add(clear);
        add(p);

        issue.addActionListener(e->issueBook());
        clear.addActionListener(e->{bookId.setText("");memberId.setText("");
            dueDate.setText(LocalDate.now().plusDays(14).toString());});
    }

    private void issueBook() {
        Connection con=null;
        try {
            int b=Integer.parseInt(bookId.getText().trim());
            int m=Integer.parseInt(memberId.getText().trim());
            Date due=Date.valueOf(dueDate.getText().trim());

            con=DBConnection.getConnection();
            con.setAutoCommit(false);

            String checkBook="SELECT available_quantity FROM books WHERE id=? FOR UPDATE";
            int available;
            try(PreparedStatement ps=con.prepareStatement(checkBook)){
                ps.setInt(1,b);
                try(ResultSet rs=ps.executeQuery()){
                    if(!rs.next())throw new IllegalArgumentException("Book not found.");
                    available=rs.getInt(1);
                }
            }
            if(available<=0)throw new IllegalArgumentException("No available copies.");

            try(PreparedStatement ps=con.prepareStatement("SELECT id FROM members WHERE id=?")){
                ps.setInt(1,m);
                try(ResultSet rs=ps.executeQuery()){
                    if(!rs.next())throw new IllegalArgumentException("Member not found.");
                }
            }

            String insert="INSERT INTO issued_books(book_id,member_id,issue_date,due_date) VALUES(?,?,?,?)";
            try(PreparedStatement ps=con.prepareStatement(insert)){
                ps.setInt(1,b);ps.setInt(2,m);
                ps.setDate(3,Date.valueOf(LocalDate.now()));
                ps.setDate(4,due);
                ps.executeUpdate();
            }

            try(PreparedStatement ps=con.prepareStatement(
                    "UPDATE books SET available_quantity=available_quantity-1 WHERE id=?")){
                ps.setInt(1,b);ps.executeUpdate();
            }

            con.commit();
            msg("Book issued successfully.");
        }catch(Exception e){
            if(con!=null)try{con.rollback();}catch(SQLException ignored){}
            err(e);
        }finally{
            if(con!=null)try{con.setAutoCommit(true);con.close();}catch(SQLException ignored){}
        }
    }
    private void msg(String s){JOptionPane.showMessageDialog(this,s);}
    private void err(Exception e){JOptionPane.showMessageDialog(this,e.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);}
}
