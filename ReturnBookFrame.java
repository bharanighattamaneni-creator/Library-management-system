package library;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class ReturnBookFrame extends JFrame {
    private final JTextField issueId = new JTextField();

    public ReturnBookFrame() {
        setTitle("Return Book");
        setSize(430, 230);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel p=new JPanel(new GridLayout(2,2,10,10));
        p.setBorder(BorderFactory.createEmptyBorder(30,30,30,30));
        p.add(new JLabel("Issue ID:"));
        p.add(issueId);
        JButton ret=new JButton("Return Book");
        JButton clear=new JButton("Clear");
        p.add(ret);p.add(clear);
        add(p);

        ret.addActionListener(e->returnBook());
        clear.addActionListener(e->issueId.setText(""));
    }

    private void returnBook() {
        Connection con=null;
        try {
            int iid=Integer.parseInt(issueId.getText().trim());
            con=DBConnection.getConnection();
            con.setAutoCommit(false);

            int bookId;
            Date due;
            try(PreparedStatement ps=con.prepareStatement(
                    "SELECT book_id,due_date,return_date FROM issued_books WHERE id=? FOR UPDATE")){
                ps.setInt(1,iid);
                try(ResultSet rs=ps.executeQuery()){
                    if(!rs.next())throw new IllegalArgumentException("Issue record not found.");
                    if(rs.getDate("return_date")!=null)
                        throw new IllegalArgumentException("This book is already returned.");
                    bookId=rs.getInt("book_id");
                    due=rs.getDate("due_date");
                }
            }

            LocalDate today=LocalDate.now();
            LocalDate dueDate=due.toLocalDate();
            long overdue=Math.max(0,ChronoUnit.DAYS.between(dueDate,today));
            double fine=overdue*5.0;

            try(PreparedStatement ps=con.prepareStatement(
                    "UPDATE issued_books SET return_date=?,fine=? WHERE id=?")){
                ps.setDate(1,Date.valueOf(today));
                ps.setDouble(2,fine);
                ps.setInt(3,iid);
                ps.executeUpdate();
            }

            try(PreparedStatement ps=con.prepareStatement(
                    "UPDATE books SET available_quantity=available_quantity+1 WHERE id=?")){
                ps.setInt(1,bookId);
                ps.executeUpdate();
            }

            con.commit();
            msg(String.format("Book returned successfully.%nFine: Rs. %.2f",fine));
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
