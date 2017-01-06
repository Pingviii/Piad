
import javax.swing.*; 
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import org.firebirdsql.jdbc.FBSQLException;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.TextField;
import java.awt.Toolkit;
import java.awt.event.*; 

import net.sourceforge.jdatepicker.impl.*; 

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat; 
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;

public class Paid extends JFrame implements ActionListener
{
	
JLabel fromDate, toDate; JButton but;
public UtilDateModel fromModel, toModel;
public JDatePanelImpl fromDatePanel, toDatePanel;
public JDatePickerImpl fromDatePicker, toDatePicker;
public static String isPaid = " IS_PAID = 0 ";
private static String connectionWay = "src/connect.ini";
static String dateType = " D.date_supp ";
static String Supp ="";
static String Supplier ="";
static String Structure ="";
static String Struct ="";
static JTable table;
static JScrollPane scrollPane;
static String startDate="";
static String endDate="";
static DefaultTableModel dtm;
static Vector data_rows;
static double resultSum=0.00; //змінна в якій буде зберігатись сума по документах
static JTextField sum;

public Paid() throws FileNotFoundException, ClassNotFoundException
    {
	
	//створюємо панель і грід компановки обєктів
    JPanel panel=new JPanel();
    GridBagLayout gbl = new GridBagLayout();//створюємо обєкт 
	GridBagConstraints c = new GridBagConstraints(); //його поля будуть визначати розміщення обєктів
	panel.setLayout(gbl);
	
	//компоненти календаря
	c.anchor = GridBagConstraints.CENTER;
   	c.fill = GridBagConstraints.NONE;
   	c.gridheight = 1;
   	c.gridwidth = 1;
   	c.gridx = 1;
   	c.gridy = 0;
    fromModel = new UtilDateModel();  
    fromDatePanel = new JDatePanelImpl(fromModel);
    fromDatePicker = new JDatePickerImpl(fromDatePanel);
    gbl.setConstraints(fromDatePicker, c);
   
    c.anchor = GridBagConstraints.CENTER;
   	c.fill = GridBagConstraints.NONE;
   	c.gridheight = 1;
   	c.gridwidth = 1;
   	c.gridx = 3;
   	c.gridy = 0;
    toModel = new UtilDateModel();  
    toDatePanel = new JDatePanelImpl(toModel);
    toDatePicker = new JDatePickerImpl(toDatePanel);
    gbl.setConstraints(toDatePicker, c);
    
    //мітки
    c.anchor = GridBagConstraints.CENTER;
	c.fill = GridBagConstraints.NONE;
	c.gridheight = 1;
	c.gridwidth = 1;
	c.gridx = 0;
	c.gridy = 0;
    fromDate=new JLabel("Дата: від ");
    gbl.setConstraints(fromDate, c);
    
    c.anchor = GridBagConstraints.CENTER;
   	c.fill = GridBagConstraints.NONE;
   	c.gridheight = 1;
   	c.gridwidth = 1;
   	c.gridx = 2;
   	c.gridy = 0;
    toDate = new JLabel(" до ");
    gbl.setConstraints(toDate, c);
    
    //кнопка
    c.anchor = GridBagConstraints.CENTER;
   	c.fill = GridBagConstraints.NONE;
   	c.gridheight = 1;
   	c.gridwidth = 2;
   	c.gridx = 2;
   	c.gridy = 3;
    but=new JButton("Розрахувати");
    but.setPreferredSize(new Dimension(130, 30));
    but.addActionListener(this);
    gbl.setConstraints(but, c);
    
    //пустий лейбл для відступу
    c.anchor = GridBagConstraints.CENTER;
   	c.fill = GridBagConstraints.NONE;
   	c.gridheight = 1;
   	c.gridwidth = 1;
   	c.gridx = 0;
   	c.gridy = 4;
    JLabel empty=new JLabel(" ");
    gbl.setConstraints(empty, c);
   
    //чек-бокс полачено чи ні
    c.anchor = GridBagConstraints.CENTER;
	c.fill = GridBagConstraints.NONE;
	c.gridheight = 2;
	c.gridwidth = 2;
	c.gridx = 2;
	c.gridy = 1;
	JCheckBox chexBox = new JCheckBox("Опл./Не опл.");
	gbl.setConstraints(chexBox, c);
	
	chexBox.addItemListener(new ItemListener() //отримуємо признак оплати, по замовчуванню не вибрано і не оплачено
	{
	   public void itemStateChanged(ItemEvent e) 
		    {
		    	if(chexBox.isSelected()) {
		    		isPaid  = " IS_PAID = 1 ";
		    	} else {isPaid  = " IS_PAID = 0 ";}
		    }
	});
	
	//комбо-бокс вибору типу дат
	c.anchor = GridBagConstraints.EAST;
	c.fill = GridBagConstraints.NONE;
	c.gridheight = 1;
	c.gridwidth = 2;
	c.gridx = 0;
	c.gridy = 2;
	
	String [] chooseDateType = { //масив пунктів меню
		"Дата документа постачальника",
		"Дата документа в пк \"Аптека\"",
		"По даті оплати"
		};
	JComboBox dateTypeCB = new JComboBox(chooseDateType);
	
	ActionListener alForDateType = new ActionListener()//прослуховуємо вибір користувача
	{
	public void actionPerformed(ActionEvent e) {
		JComboBox cb = (JComboBox)e.getSource();
        String test = (String)cb.getSelectedItem();
	if (test.equals("Дата документа в пк \"Аптека\"")) dateType=" DE.date_time ";
	else if (test.equals("По даті оплати")) dateType=" D.date_pay ";
	else if (test.equals("Дата документа постачальника")) dateType=" D.date_supp ";
	}
	};
	
	dateTypeCB.setPreferredSize(new Dimension(257, 25));
	dateTypeCB.addActionListener(alForDateType);
	gbl.setConstraints(dateTypeCB, c);
	
	//комбо-бокс списку постачальників
	c.anchor = GridBagConstraints.WEST;
	c.fill = GridBagConstraints.NONE;
	c.gridheight = 1;
	c.gridwidth = 2;
	c.gridx = 0;
	c.gridy = 1;
	
	Vector comboBoxItems=new Vector();
	comboBoxItems.add("Всі постачальники");
    DefaultComboBoxModel model = new DefaultComboBoxModel(comboBoxItems);
   
    JComboBox supplierCB = new JComboBox(model);
    
    try {
    	Class.forName("org.firebirdsql.jdbc.FBDriver"); //робимо зєднання
    	Connection connection = DriverManager.getConnection(
    	 //"jdbc:firebirdsql:localhost/3050:E:/Morion/Test/Database/mdo.fdb", для теста, потім забрати
    	"jdbc:firebirdsql:"+GetWay.getWay(connectionWay),
    	 "SYSDBA", "masterkey");
	
	 Statement st = connection.createStatement(); //створюємо стейтмент, виконуємо скрипт для отримання списка постач
	 ResultSet res = st.executeQuery("select C.name_ext from da_move D left join c_contractor C on C.id=D.id_supplier  where D.id <>0 AND c.name_ext != '' group by C.name_ext");
   
	 while (res.next()) { 
		Supplier = res.getString(1);
		comboBoxItems.add(Supplier);
		}
	
	 connection.close();
    } catch (SQLException ex) { //перевірка на з'єднання з базою
		JOptionPane.showMessageDialog(null, "Неможливо з'єднатися з базою");
	}
	
	ActionListener alSupplierCB = new ActionListener(){
	public void actionPerformed(ActionEvent e) {
		JComboBox cb2 = (JComboBox)e.getSource();
       String test2 = (String)cb2.getSelectedItem();
       if (test2.equals("Всі постачальники")) Supp ="";
       else Supp="and C.name_ext = '"+test2+"' ";
       
 	}	
	};
	supplierCB.addActionListener(alSupplierCB);
		
	supplierCB.setPreferredSize(new Dimension(257, 25));
	gbl.setConstraints(supplierCB, c);
	
	//комбо-бокс списку cкладів
		c.anchor = GridBagConstraints.EAST;
		c.fill = GridBagConstraints.NONE;
		c.gridheight = 1;
		c.gridwidth = 2;
		c.gridx = 0;
		c.gridy = 3;
		
		Vector comboBoxItems2=new Vector();
		comboBoxItems2.add("Всі склади");
	    DefaultComboBoxModel model2 = new DefaultComboBoxModel(comboBoxItems2);
	   
	    JComboBox structureCB = new JComboBox(model2);
	    
	    try {
	    	Class.forName("org.firebirdsql.jdbc.FBDriver"); //робимо зєднання
	    	Connection connection3 = DriverManager.getConnection(
	    	"jdbc:firebirdsql:"+GetWay.getWay(connectionWay),
	    	 "SYSDBA", "masterkey");
		
		 Statement st = connection3.createStatement(); //створюємо стейтмент, виконуємо скрипт для отримання списка складів
		 ResultSet res = st.executeQuery("select S.name from da_move D left join c_structure S on S.id=D.id_structure where D.id <>0 group by S.name");
	   
		 while (res.next()) { 
			Structure = res.getString(1);
			comboBoxItems2.add(Structure);
			}
		
		 connection3.close();
	    } catch (SQLException ex) { //перевірка на з'єднання з базою
		}
		
		ActionListener alStructureCB = new ActionListener(){
		public void actionPerformed(ActionEvent e) {
			JComboBox cb = (JComboBox)e.getSource();
	       String test = (String)cb.getSelectedItem();
	       if (test.equals("Всі склади")) Struct ="";
	       else Struct=" and S.name = '"+test+"' ";
	     //System.out.println(Struct);
	 	}	
		};
		structureCB.addActionListener(alStructureCB);
			
		structureCB.setPreferredSize(new Dimension(257, 25));
		gbl.setConstraints(structureCB, c);
		
	//створюємо таблицю даних
		 c.anchor = GridBagConstraints.CENTER;
			c.fill = GridBagConstraints.NONE;
			c.gridheight = 1;
			c.gridwidth = 4;
			c.gridx = 0;
			c.gridy = 5;
			
		table = new JTable();
		
		DefaultTableCellRenderer r = (DefaultTableCellRenderer) table.getDefaultRenderer(String.class);//формат ячеек таблиці
	    r.setHorizontalAlignment(JLabel.CENTER);
	    r.setVerticalAlignment(JLabel.TOP);
	    
	     dtm = new DefaultTableModel();
		 data_rows = new Vector();
		 Vector colum = new Vector();
		 colum.addElement("Назва постачальника");
		 colum.addElement("Назва документа");
		 colum.addElement("Дата док. постачальника");
		 colum.addElement("Сума");
		 colum.addElement("Дата оплати");
		 dtm.setColumnIdentifiers(colum);
		 table.setModel(dtm); 
		 
	   	scrollPane = new JScrollPane(table);//добавляємо прокрутку для таблиці
		gbl.setConstraints(scrollPane, c);
		
		//Суми по документах
		sum = new JTextField();
		sum.setHorizontalAlignment(JTextField.CENTER);
		sum.setPreferredSize(new Dimension(80, 25));

		c.anchor = GridBagConstraints.EAST;
		c.fill = GridBagConstraints.NONE;
		c.gridheight = 1;
		c.gridwidth = 2;
		c.gridx = 1;
		c.gridy = 7;

		JLabel label = new JLabel("       Сума закупівельна з ПДВ:");
		//label.setPreferredSize(new Dimension(220, 25));
		gbl.setConstraints(label,c);

		c.anchor = GridBagConstraints.WEST;
		c.fill = GridBagConstraints.NONE;
		c.gridheight = 1;
		c.gridwidth = 1;
		c.gridx = 3;
		c.gridy = 7;

		sum.setEditable(false);
		gbl.setConstraints(sum, c);
		    	
    //добавляємо компоненти на панель
    panel.add(fromDate);
    panel.add(fromDatePicker);
    panel.add(toDate);
    panel.add(toDatePicker);
    panel.add(but);
    //panel.add(chexBox);
    //panel.add(dateTypeCB);
    panel.add(supplierCB);
    panel.add(structureCB);
    panel.add(scrollPane);
    panel.add(sum);
	panel.add(label);
	panel.add(empty);
        
    //Встановлюємо іконку
  	ImageIcon icon = new ImageIcon("src/pin.png");
  	setIconImage(icon.getImage());
	
    //Вираховуємо позицію вікна
	Toolkit kit = Toolkit.getDefaultToolkit();
	Dimension screensize = kit.getScreenSize();
	int Lx = (int) (screensize.width*0.3);
	int Ly = (int) (screensize.height*0.03);
	setLocation(Lx,Ly);
  	
    //параметри фрейма
    add(panel);
    setSize(530, 640);
    setTitle("Аналіз оплат v.3.0 SA edition");
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setResizable(false);
    setVisible(true);
       
    }

public void actionPerformed(ActionEvent e) 
    { 
	if(but==e.getSource())
    {
    //отримуємо дату початку періоду аналіза
    Date fromSelectedDate = (Date) fromDatePicker.getModel().getValue();
    DateFormat df = new SimpleDateFormat("dd.MM.yyyy");
    startDate = df.format(fromSelectedDate);
    
    //отримуємо дату кінця періоду аналіза
    Date toSelectedDate = (Date) toDatePicker.getModel().getValue();
    endDate = df.format(toSelectedDate);
    
    //перевіряємо чи дата початку не більша за дату завершення
    if (fromSelectedDate.after(toSelectedDate)) {
    	JOptionPane.showMessageDialog(null, "Дата початку повинна бути непізніша ніж дата завершення!");
     } else
     {
    
  	try {
		Grid();
	} catch (ClassNotFoundException | FileNotFoundException | SQLException e1) {
		e1.printStackTrace();
	} catch (IOException e1) {
		e1.printStackTrace();
	}
	
    }
    }
    }


public static void Grid () throws ClassNotFoundException, SQLException, FBSQLException, IOException
{
	//Определяем файл для запису даних з таблиці, якщо файла не існує то він створиться
  	 FileWriter writeFile = null; 
  	 try {
              File dataFile = new File("src/data.xls");
              writeFile = new FileWriter(dataFile);
              
	try {
	Class.forName("org.firebirdsql.jdbc.FBDriver"); //робимо зєднання
	Connection connection2 = DriverManager.getConnection(
	 //"jdbc:firebirdsql:localhost/3050:E:/Morion/Test/Database/mdo.fdb", для теста, потім забрати
	"jdbc:firebirdsql:"+GetWay.getWay(connectionWay),
	 "SYSDBA", "masterkey");
	
	//робимо запит
	Statement st = connection2.createStatement();
	ResultSet res;
	//System.out.println("select C.name_ext, D.name_supp, D.date_supp, sum (DI.amount_buy_sum), D.date_pay from d_buy D inner join c_structure S on S.id=D.id_structure inner join d_buy_item DI on DI.id_buy=D.id inner join c_contractor C on C.id=D.id_supplier inner join de_doc DE on DE.id=D.id where ("+dateType+" BETWEEN '"+startDate+"' AND '"+endDate+"') and "+isPaid+Supp+Struct+" group by DI.id_buy, C.name_ext, D.name_supp, D.date_supp, D.is_paid, D.date_pay order by D.date_pay ");
	res = st.executeQuery("select C.name_ext, dp.name_supp, dp.date_supp, sum (DI.amount_buy_sum), dp.date_pay  from da_parsel dp inner join c_structure S on S.id=dp.id_structure inner join da_move_item DI on DI.id_parsel=dp.id  inner join c_contractor C on C.id=dp.id_supplier  where (dp.date_pay BETWEEN '"+startDate+"' AND '"+endDate+"')  "+Supp+Struct+"  group by C.name_ext, dp.name_supp, dp.date_supp, dp.date_pay order by dp.date_pay ");
		
	DecimalFormat formatter = new DecimalFormat("#,###,##0.00"); //округлюємо до 2 знаків
	SimpleDateFormat formatter3 = new SimpleDateFormat("dd.MM.yyyy");
	 dtm = new DefaultTableModel();
	 data_rows = new Vector();
	 Vector colum = new Vector();
	 colum.addElement("Назва постачальника");
	 colum.addElement("Назва документа");
	 colum.addElement("Дата док. постачальника");
	 colum.addElement("Сума");
	 colum.addElement("Дата оплати");
	 dtm.setColumnIdentifiers(colum);
	 while(res.next()){
		
        String data2 =" "+res.getString(1)+"\t"+res.getString(2)+"\t"+res.getDate(3)+"\t"+res.getDouble(4)+"\t"+res.getString(5)+"\n"; //переводимо на нову строку
        writeFile.append(data2);	//пишемо в файл 
        data_rows = new Vector();
        for( int i=1; i<=5; i++){
     	   if (i==4) data_rows.addElement(formatter.format(res.getDouble(i)));
     	   else if (i==3 & res.getDate(3) !=null) data_rows.addElement(formatter3.format(res.getDate(3)));
     	   else if (i==5 & res.getDate(5) !=null) data_rows.addElement(formatter3.format(res.getDate(5)));
     	   else data_rows.addElement(res.getString(i));
        }
        dtm.addRow(data_rows);
		}table.setModel(dtm);
		writeFile.close();
		
	Statement st2 = connection2.createStatement(); 
	ResultSet res2;
	//System.out.println("select sum ( DI.amount_buy_sum) from d_buy_item  DI inner join d_buy D on D.id=DI.id_buy inner join c_structure S on S.id=D.id_structure inner join de_doc DE on DE.id=D.id inner join c_contractor C on C.id=D.id_supplier where ("+dateType+" BETWEEN '"+startDate+"' AND '"+endDate+"') and "+isPaid+Supp+Struct);
	res2 = st2.executeQuery("select sum ( di.amount_buy_sum) from da_move_item  DI inner join da_move D on D.id=DI.id_move inner join c_structure S on S.id=D.id_structure inner join da_parsel Dp on Dp.id=DI.id_parsel inner join c_contractor C on C.id=D.id_supplier where (dp.date_pay BETWEEN '"+startDate+"' AND '"+endDate+"') "+Supp+Struct);
	while (res2.next()) { 
		if (res2.getString(1)==null) resultSum=0.00; //якщо документів немає, то суму ставимо 0
		else resultSum=res2.getDouble(1);
		String f = formatter.format(resultSum);
		sum.setText(f);
		//System.out.println(f);
	}
	
	/*
	System.out.println(isPaid); //перевірка признака оплати, потім забрати!
	System.out.println(dateType); //перевірка типу дат, потім забрати!
    System.out.println(Supp); //перевірка вибору постачальника, потім забрати!
    System.out.println(Struct); //перевірка вибору складу, потім забрати!
	*/
    connection2.close();
	} catch (SQLException ex) { //перевірка на з'єднання з базою
		//JOptionPane.showMessageDialog(null, "Неможливо з'єднатися з базою");
	}
	
  	} catch (IOException e) {
        e.printStackTrace();
  	} 
}

public static void main(String args[]) throws FileNotFoundException, ClassNotFoundException
{new Paid();}
}
     


        /*
        //---------------------------------------------
        //Побудова графіка
        // Создаём новый график
        JFreeChart chart = createChart(createDataset());
        // На панеле
        ChartPanel chartPanel = new ChartPanel(chart);
        // С размерами 450*450
        chartPanel.setPreferredSize(new Dimension(350, 350));
        // И ползунками если необходимо
        JScrollPane sp = new JScrollPane(chartPanel);
        sp.setPreferredSize(new Dimension(300, 300));
        sp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        */
       
    /**
     * Наполняет Set данными для построения графика
     * @return Данные для построения
     */
    
 /*
  
    private static TableXYDataset createDataset() {
        // Типа данные
    	
        TimeTableXYDataset dataset = new TimeTableXYDataset();
        dataset.add(new Day(3, 11, 2014), 1000, "Blue");
        dataset.add(new Day(4, 11, 2014), 1200, "Blue");
        dataset.add(new Day(5, 11, 2014), 1600, "Blue");
      ;
        return dataset;
    }
 
    /**
     * Создаёт новый график по данным
     * @param dataset данные для построения
     * @return график
     */
    
    /*
    private static JFreeChart createChart(TableXYDataset dataset) {
 
        // OX - ось абсцисс
        // задаем название оси
        DateAxis domainAxis = new DateAxis("Дата");
        // Показываем стрелочку вправо
        domainAxis.setPositiveArrowVisible(true);
        // Задаем отступ от графика
        domainAxis.setUpperMargin(0.2);
 
        // OY - ось ординат
        // Задаём название оси
        NumberAxis rangeAxis = new NumberAxis("Cума");
        // Задаём величину деления
        rangeAxis.setStandardTickUnits(NumberAxis.createStandardTickUnits());
        rangeAxis.setTickUnit(new NumberTickUnit(200));
        // Показываем стрелочку вверх
        rangeAxis.setPositiveArrowVisible(true);
 
 
        // Render
        // Создаем стопковый (не знаю как лучше перевести) график
        // 0.02 - расстояние между столбиками
        StackedXYBarRenderer renderer = new StackedXYBarRenderer(0.02);
        // без рамки
        renderer.setDrawBarOutline(false);
        // цвета для каждого элемента стопки
        renderer.setSeriesPaint(0, Color.darkGray);
        
        // Задаём формат и текст подсказки
        renderer.setBaseToolTipGenerator(new StandardXYToolTipGenerator("{0} : {1} = {2} tonnes", new SimpleDateFormat("yyyy"), new DecimalFormat("#,##0")));
        renderer.setSeriesItemLabelGenerator(0, new StandardXYItemLabelGenerator());
        renderer.setSeriesItemLabelGenerator(1, new StandardXYItemLabelGenerator());
        // Делаем её видимой
        renderer.setSeriesItemLabelsVisible(0, true);
        renderer.setSeriesItemLabelsVisible(1, true);
        // И описываем её шрифт
        renderer.setSeriesItemLabelFont(0, new Font("Serif", Font.BOLD, 10));
        renderer.setSeriesItemLabelFont(1, new Font("Serif", Font.BOLD, 10));
 
        // Plot
        // Создаем область рисования
        XYPlot plot = new XYPlot(dataset, domainAxis, rangeAxis, renderer);
        // Закрашиваем
        plot.setBackgroundPaint(Color.white);
        // Закрашиваем сетку
        plot.setDomainGridlinePaint(Color.white);
        plot.setRangeGridlinePaint(Color.white);
        // Отступ от осей
        plot.setAxisOffset(new RectangleInsets(0D, 0D, 10D, 10D));
        plot.setOutlinePaint(null);
 
        // Chart
        // Создаем новый график
        JFreeChart chart = new JFreeChart(plot);
        // Закрашиваем
        chart.setBackgroundPaint(Color.white);
        //видаляємо легенду
        chart.removeLegend();
         
        return chart;
    }
    */
   


