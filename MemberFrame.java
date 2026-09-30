package library;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class MemberFrame extends JFrame {
    private final JTextField id = new JTextField();
    private final JTextField name = new JTextField();
    private final JTextField email = new JTextField();
    private final JTextField phone = new JTextField();
    private final JTextField search = new JTextField();
    private final DefaultTableModel model = new DefaultTableModel(
            new String[]{"ID","Name","Email","Phone"}, 0) {
        public boolean isCellEditable(int r,int c){return false;}
    };
    private final JTable table = new JTable(model);

    public MemberFrame() {
        setTitle("Member Management");
        setSize(800, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        id.setEditable(false);

        JPanel form = new JPanel(new GridLayout(2,5,8,8));
        form.setBorder(BorderFactory.createTitledBorder("Member Details"));
        form.add(new JLabel("ID")); form.add(id);
        form.add(new JLabel("Name")); form.add(name);
        form.add(new JLabel("Email")); form.add(email);
        form.add(new JLabel("Phone")); form.add(phone);
        JButton add = new JButton("Add"), update = new JButton("Update"),
                delete = new JButton("Delete"), clear = new JButton("Clear");
        form.add(add); form.add(update); form.add(delete); form.add(clear);

        JPanel top = new JPanel(new BorderLayout(8,8));
        top.add(form,BorderLayout.CENTER);
        JPanel sp = new JPanel(new BorderLayout(5,5));
        sp.add(new JLabel("Search name: "),BorderLayout.WEST);
        sp.add(search,BorderLayout.CENTER);
        JButton sb = new JButton("Search");
        sp.add(sb,BorderLayout.EAST);
        top.add(sp,BorderLayout.SOUTH);

        add(top,BorderLayout.NORTH);
        add(new JScrollPane(table),BorderLayout.CENTER);

        add.addActionListener(e->addMember());
        update.addActionListener(e->updateMember());
        delete.addActionListener(e->deleteMember());
        clear.addActionListener(e->clearForm());
        sb.addActionListener(e->loadMembers(search.getText().trim()));
        search.addActionListener(e->loadMembers(search.getText().trim()));
        table.getSelectionModel().addListSelectionListener(e->fill());

        loadMembers("");
    }

    private void addMember() {
        try (Connection con=DBConnection.getConnection();
             PreparedStatement ps=con.prepareStatement(
                     "INSERT INTO members(name,email,phone) VALUES(?,?,?)")) {
            ps.setString(1,name.getText().trim());
            ps.setString(2,email.getText().trim());
            ps.setString(3,phone.getText().trim());
            ps.executeUpdate();
            msg("Member added.");
            clearForm(); loadMembers("");
        } catch(Exception e){err(e);}
    }

    private void updateMember() {
        if(id.getText().isBlank()){msg("Select a member first.");return;}
        try(Connection con=DBConnection.getConnection();
            PreparedStatement ps=con.prepareStatement(
                    "UPDATE members SET name=?,email=?,phone=? WHERE id=?")){
            ps.setString(1,name.getText().trim());
            ps.setString(2,email.getText().trim());
            ps.setString(3,phone.getText().trim());
            ps.setInt(4,Integer.parseInt(id.getText()));
            ps.executeUpdate();
            msg("Member updated.");
            clearForm(); loadMembers("");
        }catch(Exception e){err(e);}
    }

    private void deleteMember() {
        if(id.getText().isBlank()){msg("Select a member first.");return;}
        if(JOptionPane.showConfirmDialog(this,"Delete this member?","Confirm",
                JOptionPane.YES_NO_OPTION)!=JOptionPane.YES_OPTION)return;
        try(Connection con=DBConnection.getConnection();
            PreparedStatement ps=con.prepareStatement("DELETE FROM members WHERE id=?")){
            ps.setInt(1,Integer.parseInt(id.getText()));
            ps.executeUpdate();
            msg("Member deleted.");
            clearForm(); loadMembers("");
        }catch(Exception e){err(new Exception("Cannot delete a member with issue history.\n"+e.getMessage()));}
    }

    private void loadMembers(String term) {
        model.setRowCount(0);
        String sql="SELECT id,name,email,phone FROM members WHERE name LIKE ? ORDER BY id DESC";
        try(Connection con=DBConnection.getConnection();
            PreparedStatement ps=con.prepareStatement(sql)){
            ps.setString(1,"%"+term+"%");
            try(ResultSet rs=ps.executeQuery()){
                while(rs.next()) model.addRow(new Object[]{
                        rs.getInt(1),rs.getString(2),rs.getString(3),rs.getString(4)});
            }
        }catch(SQLException e){err(e);}
    }

    private void fill(){
        int r=table.getSelectedRow(); if(r<0)return;
        id.setText(model.getValueAt(r,0).toString());
        name.setText(model.getValueAt(r,1).toString());
        email.setText(model.getValueAt(r,2).toString());
        phone.setText(model.getValueAt(r,3).toString());
    }

    private void clearForm(){
        id.setText("");name.setText("");email.setText("");phone.setText("");
        table.clearSelection();
    }
    private void msg(String s){JOptionPane.showMessageDialog(this,s);}
    private void err(Exception e){JOptionPane.showMessageDialog(this,e.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);}
}
