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

package com.openbravo.pos.panels;

import com.openbravo.pos.forms.JPanelView;
import com.openbravo.pos.forms.JRootApp;
import com.openbravo.pos.forms.AppView;
import com.openbravo.pos.forms.AppConfig;
import com.openbravo.pos.forms.AppLocal;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.text.ParseException;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import java.util.Date;
import java.util.UUID;
import javax.swing.table.*;
import com.openbravo.data.loader.StaticSentence;
import com.openbravo.data.loader.SerializerWriteBasic;
import com.openbravo.format.Formats;
import com.openbravo.basic.BasicException;
import com.openbravo.data.loader.Datas;
import com.openbravo.data.gui.MessageInf;
import com.openbravo.data.gui.TableRendererBasic;
import com.openbravo.pos.forms.BeanFactoryApp;
import com.openbravo.pos.forms.BeanFactoryException;
import com.openbravo.pos.scripting.ScriptEngine;
import com.openbravo.pos.scripting.ScriptException;
import com.openbravo.pos.scripting.ScriptFactory;
import com.openbravo.pos.forms.DataLogicSystem;
import com.openbravo.pos.printer.TicketParser;
import com.openbravo.pos.printer.TicketPrinterException;
import com.openbravo.pos.widgets.WidgetsBuilder;

/**
 *
 * @author adrianromero
 */
public class JPanelCloseMoney extends JPanel implements JPanelView, BeanFactoryApp {
    
    private AppView m_App;
    private DataLogicSystem m_dlSystem;
    
    private PaymentsModel m_PaymentsToClose = null;   
    
    private TicketParser m_TTP;
    
    /** Creates new form JPanelCloseMoney */
    public JPanelCloseMoney() {
        
        initComponents();                   
    }
    
    public void init(AppView app) throws BeanFactoryException {
        
        m_App = app;        
        m_dlSystem = (DataLogicSystem) m_App.getBean("com.openbravo.pos.forms.DataLogicSystem");
        m_TTP = new TicketParser(m_App.getDeviceTicket(), m_dlSystem);

        m_jTicketTable.setDefaultRenderer(Object.class, new TableRendererBasic(
                new Formats[] {new FormatsPayment(), Formats.CURRENCY}));
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
        loadData();
    }   
    
    public boolean deactivate() {
        // se me debe permitir cancelar el deactivate   
        return true;
    }  
    
    private void loadData() throws BasicException {
        
        // Reset
        m_jSequence.setText(null);
        m_jMinDate.setText(null);
        m_jMaxDate.setText(null);
        m_jPrintCash.setEnabled(false);
        m_jCloseCash.setEnabled(false);
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
            m_jCloseCash.setEnabled(true);

            m_jCount.setText(m_PaymentsToClose.printPayments());
            m_jCash.setText(m_PaymentsToClose.printPaymentsTotal());
            
            m_jSales.setText(m_PaymentsToClose.printSales());
            m_jSalesSubtotal.setText(m_PaymentsToClose.printSalesBase());
            m_jSalesTaxes.setText(m_PaymentsToClose.printSalesTaxes());
            m_jSalesTotal.setText(m_PaymentsToClose.printSalesTotal());
        }          
        
        m_jTicketTable.setModel(m_PaymentsToClose.getPaymentsModel());
                
        TableColumnModel jColumns = m_jTicketTable.getColumnModel();
        jColumns.getColumn(0).setPreferredWidth(200);
        jColumns.getColumn(0).setResizable(false);
        jColumns.getColumn(1).setPreferredWidth(100);
        jColumns.getColumn(1).setResizable(false);
        
        m_jsalestable.setModel(m_PaymentsToClose.getSalesModel());
        
        jColumns = m_jsalestable.getColumnModel();
        jColumns.getColumn(0).setPreferredWidth(200);
        jColumns.getColumn(0).setResizable(false);
        jColumns.getColumn(1).setPreferredWidth(100);
        jColumns.getColumn(1).setResizable(false);

        this.categoriesTable.setModel(m_PaymentsToClose.getCategoriesModel());
        
        jColumns = this.categoriesTable.getColumnModel();
        jColumns.getColumn(0).setPreferredWidth(200);
        jColumns.getColumn(0).setResizable(false);
        jColumns.getColumn(1).setPreferredWidth(100);
        jColumns.getColumn(1).setResizable(false);
        
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
            return AppLocal.getIntString("transpayment." + (String) value);
        }   
        protected Object parseValueInt(String value) throws ParseException {
            return value;
        }
        public int getAlignment() {
            return javax.swing.SwingConstants.LEFT;
        }         
    }    
   
    private void initComponents() {
        AppConfig cfg = AppConfig.loadedInstance;
        int btnSpacing = WidgetsBuilder.pixelSize(Float.parseFloat(cfg.getProperty("ui.touchbtnspacing")));

        m_jSequence = new javax.swing.JTextField();
        m_jMinDate = new javax.swing.JTextField();
        m_jMaxDate = new javax.swing.JTextField();
        m_jScrollTableTicket = new javax.swing.JScrollPane();
        m_jTicketTable = new javax.swing.JTable();
        m_jCount = new javax.swing.JTextField();
        m_jCash = new javax.swing.JTextField();
        m_jSalesTotal = new javax.swing.JTextField();
        m_jScrollSales = new javax.swing.JScrollPane();
        m_jsalestable = new javax.swing.JTable();
        this.scrollTableCategories = new javax.swing.JScrollPane();
        this.categoriesTable = new javax.swing.JTable();
        m_jSalesTaxes = new javax.swing.JTextField();
        m_jSalesSubtotal = new javax.swing.JTextField();
        m_jSales = new javax.swing.JTextField();
        m_jCloseCash = WidgetsBuilder.createButton(AppLocal.getIntString("Button.CloseCash"));
        m_jPrintCash = WidgetsBuilder.createButton(AppLocal.getIntString("Button.PrintCash"));
        GridBagConstraints cstr = null;

        this.setLayout(new GridBagLayout());

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

        JLabel cashLabel = WidgetsBuilder.createLabel(AppLocal.getIntString("Label.Cash"));
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
        cstr.gridwidth = 3;
        cstr.weightx = 1;
        cstr.weighty = 1;
        cstr.fill = GridBagConstraints.HORIZONTAL;
        add(datesPanel, cstr);
        add(paymentsPanel, cstr);
        add(salesPanel, cstr);
        cstr = new GridBagConstraints();
        cstr.gridx = 0;
        cstr.gridy = 3;
        cstr.weightx = 1;
        add(new JPanel(), cstr);
        cstr = new GridBagConstraints();
        cstr.gridx = 1;
        cstr.gridy = 3;
        cstr.insets = new Insets(0, 0, btnSpacing, btnSpacing);
        add(m_jPrintCash, cstr);
        cstr = new GridBagConstraints();
        cstr.gridx = 2;
        cstr.gridy = 3;
        cstr.insets = new Insets(0, 0, btnSpacing, btnSpacing);
        add(m_jCloseCash, cstr);
    }

    private void m_jCloseCashActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_jCloseCashActionPerformed
        // TODO add your handling code here:
        int res = JOptionPane.showConfirmDialog(this, AppLocal.getIntString("message.wannaclosecash"), AppLocal.getIntString("message.title"), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (res == JOptionPane.YES_OPTION) {
            
            Date dNow = new Date();
            
            try {               
                // Close cash in database
                if (m_App.getActiveCashDateEnd() == null) {
                    new StaticSentence(m_App.getSession()
                        , "UPDATE CLOSEDCASH SET DATEEND = ? WHERE HOST = ? AND MONEY = ?"
                        , new SerializerWriteBasic(new Datas[] {Datas.TIMESTAMP, Datas.STRING, Datas.STRING}))
                        .exec(new Object[] {dNow, m_App.getProperties().getHost(), m_App.getActiveCashIndex()}); 
                }
            } catch (BasicException e) {
                MessageInf msg = new MessageInf(MessageInf.SGN_NOTICE, AppLocal.getIntString("message.cannotclosecash"), e);
                msg.show(this);
            }
            
            try {
                // Create new cash token
                m_App.setActiveCash(UUID.randomUUID().toString(), m_App.getActiveCashSequence() + 1, null, null);
                
                // Insert new cash token in database
                m_dlSystem.execInsertCash(
                        new Object[] {m_App.getActiveCashIndex(), m_App.getProperties().getHost(), m_App.getActiveCashSequence(), m_App.getActiveCashDateStart(), m_App.getActiveCashDateEnd()});                  
               
                // ponemos la fecha de fin
                m_PaymentsToClose.setDateEnd(dNow);
                
                // print report
                printPayments("Printer.CloseCash");
                
                // Show confirmation message
                JOptionPane.showMessageDialog(this, AppLocal.getIntString("message.closecashok"), AppLocal.getIntString("message.title"), JOptionPane.INFORMATION_MESSAGE);
            } catch (BasicException e) {
                MessageInf msg = new MessageInf(MessageInf.SGN_NOTICE, AppLocal.getIntString("message.cannotclosecash"), e);
                msg.show(this);
            }
            
            try {
                // Refresh screen
                loadData();
            } catch (BasicException e) {
                MessageInf msg = new MessageInf(MessageInf.SGN_NOTICE, AppLocal.getIntString("label.noticketstoclose"), e);
                msg.show(this);
            }
            // Log out
            if (m_App instanceof JRootApp) {
                ((JRootApp)m_App).closeAppView();
            }
        }         
    }//GEN-LAST:event_m_jCloseCashActionPerformed

private void m_jPrintCashActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_jPrintCashActionPerformed

    // print report
    printPayments("Printer.PartialCash");
    
}//GEN-LAST:event_m_jPrintCashActionPerformed
    

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
