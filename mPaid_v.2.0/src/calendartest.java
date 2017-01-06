
import javax.swing.*; 

import java.awt.*; 
import java.awt.event.*; 

import net.sourceforge.jdatepicker.*; 
import net.sourceforge.jdatepicker.graphics.*; 
import net.sourceforge.jdatepicker.impl.*; 
import net.sourceforge.jdatepicker.util.*;

import java.text.DateFormat; 
import java.text.SimpleDateFormat;
import java.util.Calendar; 
import java.util.Date;
public class calendartest extends JFrame implements ActionListener
{
JLabel CheckDate; JButton check;
public UtilDateModel model;
public JDatePanelImpl datePanel;
public JDatePickerImpl datePicker;
public calendartest()
    {
    model = new UtilDateModel();  
    datePanel = new JDatePanelImpl(model);  
    datePicker = new JDatePickerImpl(datePanel);
    JPanel panel=new JPanel();
    CheckDate=new JLabel("Date:");
    check=new JButton("CHECK"); 
    check.addActionListener(this);
    panel.add(CheckDate);
    panel.add(datePicker);
    panel.add(check);
    add(panel);
    setBounds(200,150,400,300);
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setResizable(false);
    setVisible(true);
    
    }
public void actionPerformed(ActionEvent e) 
    {if(check==e.getSource())
    {
    Date selectedDate = (Date) datePicker.getModel().getValue();
    DateFormat df = new SimpleDateFormat("MM/dd/yyyy");
    String startDate = df.format(selectedDate);
    
    JOptionPane.showMessageDialog(null,startDate);
    }}
public static void main(String args[])
{new calendartest();}
}