//    POS-Tech
//    Based upon Openbravo POS
//
//    Copyright (C) 2007-2009 Openbravo, S.L.
//                       2012 SARL SCOP Scil (http://scil.coop)
//
//    This file is part of POS-Tech.
//
//    POS-Tech is free software: you can redistribute it and/or modify
//    it under the terms of the GNU General Public License as published by
//    the Free Software Foundation, either version 3 of the License, or
//    (at your option) any later version.
//
//    POS-Tech is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//    GNU General Public License for more details.
//
//    You should have received a copy of the GNU General Public License
//    along with POS-Tech.  If not, see <http://www.gnu.org/licenses/>.

package fr.pasteque.pos.panels;

import fr.pasteque.pos.forms.JPanelView;
import fr.pasteque.pos.forms.JRootApp;
import fr.pasteque.pos.forms.AppView;
import fr.pasteque.pos.forms.AppConfig;
import fr.pasteque.pos.forms.AppLocal;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import java.text.ParseException;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import java.util.Date;
import java.util.UUID;
import javax.swing.table.*;
import fr.pasteque.data.loader.StaticSentence;
import fr.pasteque.data.loader.SerializerWriteBasic;
import fr.pasteque.format.Formats;
import fr.pasteque.basic.BasicException;
import fr.pasteque.data.loader.Datas;
import fr.pasteque.data.gui.MessageInf;
import fr.pasteque.data.gui.TableRendererBasic;
import fr.pasteque.pos.forms.BeanFactoryApp;
import fr.pasteque.pos.forms.BeanFactoryException;
import fr.pasteque.pos.scripting.ScriptEngine;
import fr.pasteque.pos.scripting.ScriptException;
import fr.pasteque.pos.scripting.ScriptFactory;
import fr.pasteque.pos.forms.DataLogicSystem;
import fr.pasteque.pos.printer.TicketParser;
import fr.pasteque.pos.printer.TicketPrinterException;
import fr.pasteque.pos.ticket.CashSession;
import fr.pasteque.pos.util.ThumbNailBuilder;
import fr.pasteque.pos.widgets.CoinCountButton;
import fr.pasteque.pos.widgets.JEditorKeys;
import fr.pasteque.pos.widgets.WidgetsBuilder;

/**
 *
 * @author adrianromero
 */
public class JPanelCloseMoney extends JPanel
    implements JPanelView, BeanFactoryApp, CoinCountButton.Listener {
    
    private AppView m_App;
    private DataLogicSystem m_dlSystem;
    
    private PaymentsModel m_PaymentsToClose = null;   
    
    private TicketParser m_TTP;

    private JPanel coinCountBtnsContainer;
    private List<CoinCountButton> coinButtons;
    private JEditorKeys keypad;
    private JLabel totalAmount;
    private double total;
    private JLabel expectedAmount;

    /** Creates new form JPanelCloseMoney */
    public JPanelCloseMoney() {
        initComponents();
    }

    public void init(AppView app) throws BeanFactoryException {
        
        m_App = app;
        m_dlSystem = new DataLogicSystem();
        // Init z ticket
        m_TTP = new TicketParser(m_App.getDeviceTicket(), m_dlSystem);

        m_jTicketTable.setDefaultRenderer(Object.class, new TableRendererBasic(
                new Formats[] {new FormatsPayment(), new FormatsPaymentValue()}));
        m_jTicketTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        m_jScrollTableTicket.getVerticalScrollBar().setPreferredSize(new Dimension(25,25));       
        m_jTicketTable.getTableHeader().setReorderingAllowed(false);         
        m_jTicketTable.setRowHeight(25);
        m_jTicketTable.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);         
        
        m_jsalestable.setDefaultRenderer(Object.class, new TableRendererBasic(
                new Formats[] {Formats.STRING, Formats.CURRENCY, Formats.CURRENCY, Formats.CURRENCY}));
        m_jsalestable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        m_jScrollSales.getVerticalScrollBar().setPreferredSize(new Dimension(25,25));       
        m_jsalestable.getTableHeader().setReorderingAllowed(false);         
        m_jsalestable.setRowHeight(25);
        m_jsalestable.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        this.categoriesTable.setDefaultRenderer(Object.class, new TableRendererBasic(
                new Formats[] {Formats.STRING, Formats.CURRENCY, Formats.CURRENCY, Formats.CURRENCY}));
        this.categoriesTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        this.scrollTableCategories.getVerticalScrollBar().setPreferredSize(new Dimension(25,25));       
        this.categoriesTable.getTableHeader().setReorderingAllowed(false);         
        this.categoriesTable.setRowHeight(25);
        this.categoriesTable.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Init cash count
        this.coinButtons = new ArrayList<CoinCountButton>();
        AppConfig cfg = AppConfig.loadedInstance;
        boolean showCount = cfg.getProperty("ui.countmoney").equals("1");
        if (showCount) {
            String code = this.m_dlSystem.getResourceAsXML("payment.cash");
            if (code != null) {
                try {
                    ScriptEngine script = ScriptFactory.getScriptEngine(ScriptFactory.BEANSHELL);
                    script.put("payment", new ScriptCash());
                    script.eval(code);
                } catch (ScriptException e) {
                    MessageInf msg = new MessageInf(MessageInf.SGN_NOTICE, AppLocal.getIntString("message.cannotexecute"), e);
                    msg.show(this);
                }
            }
        }
    }
    
    public Object getBean() {
        return this;
    }
    
    public JComponent getComponent() {
        return this;
    }

    public String getTitle() {
        return AppLocal.getIntString("Menu.CloseTPV");
    }
    
    public boolean requiresOpenedCash() {
        return true;
    }
    
    public void activate() throws BasicException {
        this.loadData();
        this.showZTicket();
    }   
    
    public boolean deactivate() {
        // se me debe permitir cancelar el deactivate   
        return true;
    }

    private void showCashCount() {
        CardLayout cl = (CardLayout) this.getLayout();
        cl.show(this, "cashcount");
        this.updateExpectedAmount();
        this.updateAmount();
        // Open drawer
        String code = this.m_dlSystem.getResourceAsXML("Printer.OpenDrawer");
        if (code != null) {
            try {
                ScriptEngine script = ScriptFactory.getScriptEngine(ScriptFactory.VELOCITY);
                script.eval(code);
            } catch (ScriptException e) {
                e.printStackTrace();
            }
        }
    }
    private void showZTicket() {
        CardLayout cl = (CardLayout) this.getLayout();
        cl.show(this, "zticket");
    }

    private void loadData() throws BasicException {
        
        // Reset
        m_jSequence.setText(null);
        m_jMinDate.setText(null);
        m_jMaxDate.setText(null);
        m_jPrintCash.setEnabled(false);
        m_jCount.setText(null); // AppLocal.getIntString("label.noticketstoclose");
        m_jCash.setText(null);

        m_jSales.setText(null);
        m_jSalesSubtotal.setText(null);
        m_jSalesTaxes.setText(null);
        m_jSalesTotal.setText(null);
        
        m_jTicketTable.setModel(new DefaultTableModel());
        m_jsalestable.setModel(new DefaultTableModel());
        this.categoriesTable.setModel(new DefaultTableModel());
            
        // LoadData
        m_PaymentsToClose = PaymentsModel.loadInstance(m_App);
        
        // Populate Data
        m_jSequence.setText(m_PaymentsToClose.printSequence());
        m_jMinDate.setText(m_PaymentsToClose.printDateStart());
        m_jMaxDate.setText(m_PaymentsToClose.printDateEnd());
        
        if (m_PaymentsToClose.getPayments() != 0 || m_PaymentsToClose.getSales() != 0) {

            m_jPrintCash.setEnabled(true);

            m_jCount.setText(m_PaymentsToClose.printPayments());
            m_jCash.setText(m_PaymentsToClose.printPaymentsTotal());
            
            m_jSales.setText(m_PaymentsToClose.printSales());
            m_jSalesSubtotal.setText(m_PaymentsToClose.printSalesBase());
            m_jSalesTaxes.setText(m_PaymentsToClose.printSalesTaxes());
            m_jSalesTotal.setText(m_PaymentsToClose.printSalesTotal());
        }          
        
        m_jTicketTable.setModel(m_PaymentsToClose.getPaymentsModel());
                
        TableColumnModel jColumns = m_jTicketTable.getColumnModel();
        jColumns.getColumn(0).setPreferredWidth(247);
        jColumns.getColumn(0).setResizable(false);
        jColumns.getColumn(1).setPreferredWidth(100);
        jColumns.getColumn(1).setResizable(false);
        
        m_jsalestable.setModel(m_PaymentsToClose.getSalesModel());
        
        jColumns = m_jsalestable.getColumnModel();
        jColumns.getColumn(0).setPreferredWidth(151);
        jColumns.getColumn(0).setResizable(false);
        jColumns.getColumn(1).setPreferredWidth(98);
        jColumns.getColumn(1).setResizable(false);
        jColumns.getColumn(2).setPreferredWidth(98);
        jColumns.getColumn(2).setResizable(false);

        this.categoriesTable.setModel(m_PaymentsToClose.getCategoriesModel());
        
        jColumns = this.categoriesTable.getColumnModel();
        jColumns.getColumn(0).setPreferredWidth(247);
        jColumns.getColumn(0).setResizable(false);
        jColumns.getColumn(1).setPreferredWidth(100);
        jColumns.getColumn(1).setResizable(false);
    }

    /** Show confirm popup to close cash and close cash if confirmed */
    private void confirmCloseCash() {
        int res = JOptionPane.showConfirmDialog(this,
                AppLocal.getIntString("message.wannaclosecash"),
                AppLocal.getIntString("message.title"),
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (res == JOptionPane.YES_OPTION) {
            this.closeCash();
        }
    }

    private void closeCash() {
        Date dNow = new Date();
        CashSession cashSess = m_App.getActiveCashSession();
        cashSess.close(dNow);
        AppConfig cfg = AppConfig.loadedInstance;
        boolean showCount = cfg.getProperty("ui.countmoney").equals("1");
        if (showCount) {
            cashSess.setCloseCash(this.total);
        }
        try {
            // Close cash in database
            cashSess = m_dlSystem.saveCashSession(cashSess);
            m_App.newActiveCash();
        } catch (BasicException e) {
            
            MessageInf msg = new MessageInf(MessageInf.SGN_NOTICE, AppLocal.getIntString("message.cannotclosecash"), e);
            msg.show(this);
            return;
        }

        // Prepare report and print
        m_PaymentsToClose.setDateEnd(dNow);
        printPayments("Printer.CloseCash");

        // Show confirmation message
        JOptionPane.showMessageDialog(this,
                AppLocal.getIntString("message.closecashok"),
                AppLocal.getIntString("message.title"),
                JOptionPane.INFORMATION_MESSAGE);
        // Log out
        if (m_App instanceof JRootApp) {
            ((JRootApp)m_App).closeAppView();
        }
        
    }

    /** Print cash summary */
    private void printPayments(String report) {
        // Get the resource
        String sresource = m_dlSystem.getResourceAsXML(report);
        if (sresource == null) {
            MessageInf msg = new MessageInf(MessageInf.SGN_WARNING, AppLocal.getIntString("message.cannotprintticket"));
            msg.show(this);
        } else {
            // Put objects references and run resource script
            try {
                ScriptEngine script = ScriptFactory.getScriptEngine(ScriptFactory.VELOCITY);
                script.put("payments", m_PaymentsToClose);
                m_TTP.printTicket(script.eval(sresource).toString());
            } catch (ScriptException e) {
                MessageInf msg = new MessageInf(MessageInf.SGN_WARNING, AppLocal.getIntString("message.cannotprintticket"), e);
                msg.show(this);
            } catch (TicketPrinterException e) {
                MessageInf msg = new MessageInf(MessageInf.SGN_WARNING, AppLocal.getIntString("message.cannotprintticket"), e);
                msg.show(this);
            }
        }
    }

    private class FormatsPayment extends Formats {
        protected String formatValueInt(Object value) {
            Object[] values = (Object[]) value;
            String paymentType = (String) values[0];
            String currencyName = (String) values[1];
            Boolean main = (Boolean) values[2];
            String ret = AppLocal.getIntString("transpayment." + (String) paymentType);
            if (main) {
                return ret;
            } else {
                return ret + " (" + currencyName + ")";
            }
        }   
        protected Object parseValueInt(String value) throws ParseException {
            return value;
        }
        public int getAlignment() {
            return javax.swing.SwingConstants.LEFT;
        }         
    }
    private class FormatsPaymentValue extends Formats {
        /** Format payment. Value is expected to be PaymentModel.PaymentLine */
        protected String formatValueInt(Object value) {
            PaymentsModel.PaymentsLine line = (PaymentsModel.PaymentsLine) value;
            return line.printValue();
        }   
        protected Object parseValueInt(String value) throws ParseException {
            return value;
        }
        public int getAlignment() {
            return javax.swing.SwingConstants.RIGHT;
        }         
    }

    public class ScriptCash {
        private int x, y;
        private int btnSpacing;
        private ThumbNailBuilder tnb;
        public ScriptCash() {
            AppConfig cfg = AppConfig.loadedInstance;
            this.btnSpacing = WidgetsBuilder.pixelSize(Float.parseFloat(cfg.getProperty("ui.touchbtnspacing")));
            this.tnb = new ThumbNailBuilder(64, 54, "cash.png");
        }
        public void addButton(String image, double amount) {
            ImageIcon icon = new ImageIcon(this.tnb.getThumbNailText(m_dlSystem.getResourceAsImage(image), Formats.CURRENCY.formatValue(amount)));
            JPanelCloseMoney parent = JPanelCloseMoney.this;
            CoinCountButton btn = new CoinCountButton(icon, amount,
                    parent.keypad, parent);
            parent.coinButtons.add(btn);
            GridBagConstraints cstr = new GridBagConstraints();
            cstr.gridx = this.x;
            cstr.gridy = this.y;
            cstr.insets = new Insets(btnSpacing, btnSpacing, btnSpacing,
                    btnSpacing);
            parent.coinCountBtnsContainer.add(btn.getComponent(), cstr);
            if (this.x == 3) {
                this.x = 0;
                this.y++;
            } else {
                this.x++;
            }
        }
    }

    public void coinAdded(double amount, int newCount) {
        this.total += amount;
        this.totalAmount.setText(Formats.CURRENCY.formatValue(this.total));
        this.updateMatchingCount();
    }
    public void countUpdated() {
        this.updateAmount();
    }

    public void updateAmount() {
        if (this.totalAmount == null) {
            return;
        }
        this.total = 0.0;
        for (CoinCountButton btn : this.coinButtons) {
            this.total += btn.getAmount();
        }
        this.totalAmount.setText(Formats.CURRENCY.formatValue(this.total));
        this.updateMatchingCount();
    }
    public void updateExpectedAmount() {
        this.expectedAmount.setText(this.m_PaymentsToClose.printExpectedCash());
    }
    /** Check if total and expected amount are equal
     * and update UI accordingly
     */
    public void updateMatchingCount() {
        if (this.total != this.m_PaymentsToClose.getExpectedCash()) {
            this.totalAmount.setForeground(Color.RED);
        } else {
            this.totalAmount.setForeground(Color.BLACK);
        }
    }

    private void initComponents() {
        AppConfig cfg = AppConfig.loadedInstance;
        int btnSpacing = WidgetsBuilder.pixelSize(Float.parseFloat(cfg.getProperty("ui.touchbtnspacing")));

        this.setLayout(new CardLayout());
        JPanel zTicketContainer = new JPanel();
        zTicketContainer.setLayout(new GridBagLayout());
        JPanel cashCountContainer = new JPanel();
        cashCountContainer.setLayout(new GridBagLayout());
        this.add(zTicketContainer, "zticket");
        this.add(cashCountContainer, "cashcount");

        // z ticket
        m_jSequence = new javax.swing.JTextField();
        m_jMinDate = new javax.swing.JTextField();
        m_jMaxDate = new javax.swing.JTextField();
        m_jScrollTableTicket = new JScrollPane();
        m_jTicketTable = new javax.swing.JTable();
        m_jCount = new javax.swing.JTextField();
        m_jCash = new javax.swing.JTextField();
        m_jSalesTotal = new javax.swing.JTextField();
        m_jScrollSales = new JScrollPane();
        m_jsalestable = new javax.swing.JTable();
        this.scrollTableCategories = new JScrollPane();
        this.categoriesTable = new javax.swing.JTable();
        m_jSalesTaxes = new javax.swing.JTextField();
        m_jSalesSubtotal = new javax.swing.JTextField();
        m_jSales = new javax.swing.JTextField();
        m_jCloseCash = WidgetsBuilder.createButton(AppLocal.getIntString("Button.CloseCash"));
        m_jPrintCash = WidgetsBuilder.createButton(AppLocal.getIntString("Button.PrintCash"));
        GridBagConstraints cstr = null;

        JPanel mainContainer = new JPanel();
        mainContainer.setLayout(new GridBagLayout());
        JScrollPane scroll = new JScrollPane();
        scroll.getVerticalScrollBar().setPreferredSize(new Dimension(25,25));
        scroll.setViewportView(mainContainer);

        // Dates frame
        JPanel datesPanel = new JPanel();
        datesPanel.setLayout(new GridBagLayout());
        datesPanel.setBorder(BorderFactory.createTitledBorder(AppLocal.getIntString("label.datestitle")));

        JLabel sequenceLabel = WidgetsBuilder.createLabel(AppLocal.getIntString("label.sequence"));
        cstr = new GridBagConstraints();
        cstr.gridx = 0;
        cstr.gridy = 0;
        cstr.insets = new Insets(5, 5, 5, 5);
        cstr.anchor = GridBagConstraints.WEST;
        datesPanel.add(sequenceLabel, cstr);
        m_jSequence.setEditable(false);
        m_jSequence.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        cstr = new GridBagConstraints();
        cstr.gridx = 1;
        cstr.gridy = 0;
        cstr.insets = new Insets(5, 0, 5, 0);
        cstr.fill = GridBagConstraints.HORIZONTAL;
        datesPanel.add(m_jSequence, cstr);

        JLabel startLabel = WidgetsBuilder.createLabel(AppLocal.getIntString("Label.StartDate"));
        cstr = new GridBagConstraints();
        cstr.gridx = 0;
        cstr.gridy = 1;
        cstr.insets = new Insets(0, 5, 5, 5);
        cstr.anchor = GridBagConstraints.WEST;
        datesPanel.add(startLabel, cstr);
        m_jMinDate.setEditable(false);
        m_jMinDate.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        cstr = new GridBagConstraints();
        cstr.gridx = 1;
        cstr.gridy = 1;
        cstr.insets = new Insets(0, 0, 5, 0);
        cstr.fill = GridBagConstraints.HORIZONTAL;
        datesPanel.add(m_jMinDate, cstr);

        JLabel endLabel = WidgetsBuilder.createLabel(AppLocal.getIntString("Label.EndDate"));
        cstr = new GridBagConstraints();
        cstr.gridx = 0;
        cstr.gridy = 2;
        cstr.insets = new Insets(0, 5, 5, 5);
        cstr.anchor = GridBagConstraints.WEST;
        datesPanel.add(endLabel, cstr);
        m_jMaxDate.setEditable(false);
        m_jMaxDate.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        cstr = new GridBagConstraints();
        cstr.gridx = 1;
        cstr.gridy = 2;
        cstr.insets = new Insets(0, 0, 5, 0);
        cstr.fill = GridBagConstraints.HORIZONTAL;
        datesPanel.add(m_jMaxDate, cstr);
        
        cstr = new GridBagConstraints();
        cstr.gridx = 2;
        cstr.gridy = 0;
        cstr.weightx = 1;
        datesPanel.add(new JPanel(), cstr);

        // Payments frame
        JPanel paymentsPanel = new JPanel();
        paymentsPanel.setLayout(new GridBagLayout());
        paymentsPanel.setBorder(BorderFactory.createTitledBorder(AppLocal.getIntString("label.paymentstitle")));

        m_jScrollTableTicket.setMinimumSize(new java.awt.Dimension(350, 140));
        m_jScrollTableTicket.setPreferredSize(new java.awt.Dimension(350, 140));
        m_jTicketTable.setFocusable(false);
        m_jTicketTable.setIntercellSpacing(new java.awt.Dimension(0, 1));
        m_jTicketTable.setRequestFocusEnabled(false);
        m_jTicketTable.setShowVerticalLines(false);
        m_jScrollTableTicket.setViewportView(m_jTicketTable);
        cstr = new GridBagConstraints();
        cstr.gridx = 0;
        cstr.gridy = 0;
        cstr.gridheight = 5;
        cstr.insets = new Insets(5, 5, 5, 5);
        paymentsPanel.add(m_jScrollTableTicket, cstr);

        JLabel paymentsLabel = WidgetsBuilder.createLabel(AppLocal.getIntString("Label.Tickets"));
        cstr = new GridBagConstraints();
        cstr.gridx = 1;
        cstr. gridy = 0;
        cstr.insets = new Insets(5, 0, 5, 5);
        cstr.anchor = GridBagConstraints.WEST;
        paymentsPanel.add(paymentsLabel, cstr);

        m_jCount.setEditable(false);
        m_jCount.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        cstr = new GridBagConstraints();
        cstr.gridx = 2;
        cstr.gridy = 0;
        cstr.insets = new Insets(5, 0, 5, 0);
        cstr.fill = GridBagConstraints.HORIZONTAL;
        paymentsPanel.add(m_jCount, cstr);

        JLabel cashLabel = WidgetsBuilder.createLabel(AppLocal.getIntString("label.paymenttotal"));
        cstr = new GridBagConstraints();
        cstr.gridx = 1;
        cstr. gridy = 1;
        cstr.insets = new Insets(0, 0, 5, 5);
        cstr.anchor = GridBagConstraints.WEST;
        paymentsPanel.add(cashLabel, cstr);

        m_jCash.setEditable(false);
        m_jCash.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        cstr = new GridBagConstraints();
        cstr.gridx = 2;
        cstr.gridy = 1;
        cstr.insets = new Insets(0, 0, 5, 0);
        cstr.fill = GridBagConstraints.HORIZONTAL;
        paymentsPanel.add(m_jCash, cstr);

        cstr = new GridBagConstraints();
        cstr.gridx = 3;
        cstr.gridy = 0;
        cstr.weightx = 1;
        paymentsPanel.add(new JPanel(), cstr);

        // Taxes and total frame
        JPanel salesPanel = new JPanel();
        salesPanel.setLayout(new GridBagLayout());
        salesPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(AppLocal.getIntString("label.salestitle")));

        m_jsalestable.setFocusable(false);
        m_jsalestable.setIntercellSpacing(new java.awt.Dimension(0, 1));
        m_jsalestable.setRequestFocusEnabled(false);
        m_jsalestable.setShowVerticalLines(false);
        m_jScrollSales.setMinimumSize(new java.awt.Dimension(350, 140));
        m_jScrollSales.setPreferredSize(new java.awt.Dimension(350, 140));
        m_jScrollSales.setViewportView(m_jsalestable);
        cstr = new GridBagConstraints();
        cstr.gridx = 0;
        cstr.gridy = 0;
        cstr.gridheight = 5;
        cstr.insets = new Insets(5, 5, 5, 5);
        salesPanel.add(m_jScrollSales, cstr);

        this.categoriesTable.setFocusable(false);
        this.categoriesTable.setIntercellSpacing(new java.awt.Dimension(0, 1));
        this.categoriesTable.setRequestFocusEnabled(false);
        this.categoriesTable.setShowVerticalLines(false);
        this.scrollTableCategories.setMinimumSize(new java.awt.Dimension(350, 140));
        this.scrollTableCategories.setPreferredSize(new java.awt.Dimension(350, 140));
        this.scrollTableCategories.setViewportView(this.categoriesTable);
        cstr = new GridBagConstraints();
        cstr.gridx = 0;
        cstr.gridy = 6;
        cstr.gridheight = 5;
        cstr.insets = new Insets(0, 5, 5, 5);
        salesPanel.add(this.scrollTableCategories, cstr);

        JLabel salesLabel = WidgetsBuilder.createLabel(AppLocal.getIntString("label.sales"));
        cstr = new GridBagConstraints();
        cstr.gridx = 1;
        cstr.gridy = 0;
        cstr.insets = new Insets(0, 0, 5, 5);
        cstr.anchor = GridBagConstraints.WEST;
        salesPanel.add(salesLabel, cstr);
        m_jSales.setEditable(false);
        m_jSales.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        cstr = new GridBagConstraints();
        cstr.gridx = 2;
        cstr.gridy = 0;
        cstr.insets = new Insets(0, 0, 5, 0);
        cstr.fill = GridBagConstraints.HORIZONTAL;
        salesPanel.add(m_jSales, cstr);

        JLabel subtotalLabel = WidgetsBuilder.createLabel(AppLocal.getIntString("label.subtotalcash"));
        cstr = new GridBagConstraints();
        cstr.gridx = 1;
        cstr.gridy = 1;
        cstr.insets = new Insets(0, 0, 5, 5);
        cstr.anchor = GridBagConstraints.WEST;
        salesPanel.add(subtotalLabel, cstr);
        m_jSalesSubtotal.setEditable(false);
        m_jSalesSubtotal.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        cstr = new GridBagConstraints();
        cstr.gridx = 2;
        cstr.gridy = 1;
        cstr.insets = new Insets(0, 0, 5, 0);
        cstr.fill = GridBagConstraints.HORIZONTAL;
        salesPanel.add(m_jSalesSubtotal, cstr);

        JLabel taxesLabel = WidgetsBuilder.createLabel(AppLocal.getIntString("label.taxcash"));
        cstr = new GridBagConstraints();
        cstr.gridx = 1;
        cstr.gridy = 2;
        cstr.insets = new Insets(0, 0, 5, 5);
        cstr.anchor = GridBagConstraints.WEST;
        salesPanel.add(taxesLabel, cstr);
        m_jSalesTaxes.setEditable(false);
        m_jSalesTaxes.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        cstr = new GridBagConstraints();
        cstr.gridx = 2;
        cstr.gridy = 2;
        cstr.insets = new Insets(0, 0, 5, 0);
        cstr.fill = GridBagConstraints.HORIZONTAL;
        salesPanel.add(m_jSalesTaxes, cstr);

        JLabel totalLabel = WidgetsBuilder.createLabel(AppLocal.getIntString("label.totalcash"));
        cstr = new GridBagConstraints();
        cstr.gridx = 1;
        cstr.gridy = 3;
        cstr.insets = new Insets(0, 0, 5, 5);
        cstr.anchor = GridBagConstraints.WEST;
        salesPanel.add(totalLabel, cstr);
        m_jSalesTotal.setEditable(false);
        m_jSalesTotal.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        cstr = new GridBagConstraints();
        cstr.gridx = 2;
        cstr.gridy = 3;
        cstr.insets = new Insets(0, 0, 5, 0);
        cstr.fill = GridBagConstraints.HORIZONTAL;
        salesPanel.add(m_jSalesTotal, cstr);

        cstr = new GridBagConstraints();
        cstr.gridx = 3;
        cstr.gridy = 0;
        cstr.weightx = 1;
        salesPanel.add(new JPanel(), cstr);

        // Buttons
        m_jCloseCash.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_jCloseCashActionPerformed(evt);
            }
        });
        m_jPrintCash.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_jPrintCashActionPerformed(evt);
            }
        });

        // General layout
        cstr = new GridBagConstraints();
        cstr.gridx = 0;
        cstr.weightx = 1;
        cstr.fill = GridBagConstraints.HORIZONTAL;
        mainContainer.add(datesPanel, cstr);
        mainContainer.add(paymentsPanel, cstr);
        mainContainer.add(salesPanel, cstr);
        cstr = new GridBagConstraints();
        cstr.gridx = 0;
        cstr.gridy = 0;
        cstr.gridwidth = 3;
        cstr.weightx = 1;
        cstr.weighty = 1;
        cstr.fill = GridBagConstraints.BOTH;
        zTicketContainer.add(scroll, cstr);
        cstr = new GridBagConstraints();
        cstr.gridx = 0;
        cstr.gridy = 3;
        cstr.weightx = 1;
        zTicketContainer.add(new JPanel(), cstr);
        cstr = new GridBagConstraints();
        cstr.gridx = 1;
        cstr.gridy = 3;
        cstr.insets = new Insets(btnSpacing, 0, btnSpacing, btnSpacing);
        zTicketContainer.add(m_jPrintCash, cstr);
        cstr = new GridBagConstraints();
        cstr.gridx = 2;
        cstr.gridy = 3;
        cstr.insets = new Insets(btnSpacing, 0, btnSpacing, btnSpacing);
        zTicketContainer.add(m_jCloseCash, cstr);


        // Cash count
        this.coinCountBtnsContainer = new JPanel();
        this.coinCountBtnsContainer.setLayout(new GridBagLayout());
        cstr = new GridBagConstraints();
        cstr.gridx = 0;
        cstr.gridy = 1;
        cstr.weighty = 0.5;
        cstr.weightx = 1.0;
        cstr.fill = GridBagConstraints.BOTH;
        cashCountContainer.add(this.coinCountBtnsContainer, cstr);
        // keypad and input
        JPanel inputContainer = new JPanel();
        inputContainer.setLayout(new GridBagLayout());
        this.keypad = new JEditorKeys();
        cstr = new GridBagConstraints();
        cstr.gridx = 0;
        cstr.gridy = 0;
        cstr.gridwidth = 2;
        cstr.insets = new Insets(btnSpacing, btnSpacing, btnSpacing,
                btnSpacing);
        inputContainer.add(this.keypad, cstr);
        JLabel amountLabel = WidgetsBuilder.createLabel(AppLocal.getIntString("label.openCashAmount"));
        cstr = new GridBagConstraints();
        cstr.gridx = 0;
        cstr.gridy = 1;
        cstr.gridwidth = 2;
        cstr.anchor = GridBagConstraints.FIRST_LINE_START;
        inputContainer.add(amountLabel, cstr);
        this.totalAmount = WidgetsBuilder.createImportantLabel("");
        WidgetsBuilder.inputStyle(totalAmount);
        cstr = new GridBagConstraints();
        cstr.gridx = 0;
        cstr.gridy = 2;
        cstr.gridwidth = 2;
        cstr.weightx = 1.0;
        cstr.fill = GridBagConstraints.BOTH;
        cstr.anchor = GridBagConstraints.CENTER;
        inputContainer.add(totalAmount, cstr);
        JLabel expectedAmountLabel = WidgetsBuilder.createLabel(AppLocal.getIntString("label.expectedCloseCashAmount"));
        cstr = new GridBagConstraints();
        cstr.gridx = 0;
        cstr.gridy = 3;
        cstr.gridwidth = 2;
        cstr.anchor = GridBagConstraints.FIRST_LINE_START;
        inputContainer.add(expectedAmountLabel, cstr);
        this.expectedAmount = WidgetsBuilder.createImportantLabel("");
        WidgetsBuilder.inputStyle(expectedAmount);
        cstr = new GridBagConstraints();
        cstr.gridx = 0;
        cstr.gridy = 4;
        cstr.gridwidth = 2;
        cstr.weightx = 1.0;
        cstr.fill = GridBagConstraints.BOTH;
        cstr.anchor = GridBagConstraints.CENTER;
        inputContainer.add(expectedAmount, cstr);
        JButton cancel = WidgetsBuilder.createButton(WidgetsBuilder.createIcon("button_cancel.png"),
                AppLocal.getIntString("Button.Cancel"),
                WidgetsBuilder.SIZE_BIG);
        cancel.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    countCancelActionPerformed(evt);
                }
            });
        cstr = new GridBagConstraints();
        cstr.gridx = 0;
        cstr.gridy = 5;
        cstr.insets = new Insets(btnSpacing, btnSpacing, btnSpacing,
                btnSpacing);
        inputContainer.add(cancel, cstr);
        JButton closeCash = WidgetsBuilder.createButton(WidgetsBuilder.createIcon("open_cash.png"),
                AppLocal.getIntString("Button.CloseCash"),
                WidgetsBuilder.SIZE_BIG);
        closeCash.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    countCloseCashActionPerformed(evt);
                }
            });
        cstr = new GridBagConstraints();
        cstr.gridx = 1;
        cstr.gridy = 5;
        cstr.insets = new Insets(btnSpacing, btnSpacing, btnSpacing,
                btnSpacing);
        inputContainer.add(closeCash, cstr);
        cstr = new GridBagConstraints();
        cstr.gridx = 1;
        cstr.gridy = 1;
        cstr.weightx = 0.5;
        cstr.anchor = GridBagConstraints.CENTER;
        cstr.insets = new Insets(0, btnSpacing, btnSpacing, btnSpacing);
        cashCountContainer.add(inputContainer, cstr);
    }

    private void m_jCloseCashActionPerformed(java.awt.event.ActionEvent evt) {
        AppConfig cfg = AppConfig.loadedInstance;
        boolean showCount = cfg.getProperty("ui.countmoney").equals("1");
        if (showCount) {
            this.showCashCount();
        } else {
            this.confirmCloseCash();
        }
    }
    private void countCloseCashActionPerformed(ActionEvent evt) {
        this.confirmCloseCash();
    }
    private void countCancelActionPerformed(ActionEvent evt) {
        this.showZTicket();
    }

    private void m_jPrintCashActionPerformed(java.awt.event.ActionEvent evt) {
        // print report
        printPayments("Printer.PartialCash");
    }

    private javax.swing.JTextField m_jCash;
    private javax.swing.JButton m_jCloseCash;
    private javax.swing.JTextField m_jCount;
    private javax.swing.JTextField m_jMaxDate;
    private javax.swing.JTextField m_jMinDate;
    private javax.swing.JButton m_jPrintCash;
    private javax.swing.JTextField m_jSales;
    private javax.swing.JTextField m_jSalesSubtotal;
    private javax.swing.JTextField m_jSalesTaxes;
    private javax.swing.JTextField m_jSalesTotal;
    private javax.swing.JScrollPane m_jScrollSales;
    private javax.swing.JScrollPane m_jScrollTableTicket;
    private javax.swing.JScrollPane scrollTableCategories;
    private javax.swing.JTextField m_jSequence;
    private javax.swing.JTable m_jTicketTable;
    private javax.swing.JTable m_jsalestable;
    private javax.swing.JTable categoriesTable;
}
