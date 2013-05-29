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

package com.openbravo.pos.payment;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Window;
import java.awt.event.ActionListener;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JFrame;
import javax.swing.JPanel;
import com.openbravo.basic.BasicException;
import com.openbravo.pos.admin.CurrencyInfo;
import com.openbravo.data.gui.ComboBoxValModel;
import com.openbravo.pos.forms.AppConfig;
import com.openbravo.pos.forms.AppView;
import com.openbravo.pos.forms.AppLocal;
import com.openbravo.format.Formats;
import com.openbravo.pos.customers.CustomerInfoExt;
import com.openbravo.pos.forms.DataLogicSales;
import com.openbravo.pos.forms.DataLogicSystem;
import com.openbravo.pos.widgets.WidgetsBuilder;
import java.awt.ComponentOrientation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author adrianromero
 */
public abstract class JPaymentSelect extends javax.swing.JDialog 
    implements JPaymentNotifier, ActionListener {
    
    private PaymentInfoList m_aPaymentInfo;
    private boolean printselected;
    
    private boolean accepted;
    
    private AppView app;
    private ComponentOrientation orientation;
    private double m_dTotal;
    private int parts;
    private double nextPartAmount; // Rounded part amount to get exactly total
    private CustomerInfoExt customerext;
    private DataLogicSystem dlSystem;
    
    private Map<String, JPaymentInterface> payments = new HashMap<String, JPaymentInterface>();
    private String m_sTransactionID;

    private List<CurrencyInfo> currencies;
    private CurrencyInfo selectedCurrency;    
    
    /** Creates new form JPaymentSelect */
    protected JPaymentSelect(java.awt.Frame parent, boolean modal, ComponentOrientation o) {
        super(parent, modal);
        this.orientation = o;
    }
    /** Creates new form JPaymentSelect */
    protected JPaymentSelect(java.awt.Dialog parent, boolean modal, ComponentOrientation o) {
        super(parent, modal);
        this.orientation = o;
    }    
    
    public void init(AppView app) {
        this.app = app;
        dlSystem = (DataLogicSystem) app.getBean("com.openbravo.pos.forms.DataLogicSystem");
        printselected = !AppConfig.loadedInstance.getProperty("ui.printticketbydefault").equals("0");
        // Init currencies
        DataLogicSales dlSales = (DataLogicSales) app.getBean("com.openbravo.pos.forms.DataLogicSales");
        try {
            this.currencies = dlSales.getCurrenciesList().list();
        } catch (BasicException e) {
            this.currencies = new ArrayList<CurrencyInfo>();
            e.printStackTrace();
        }
        // Init components
        initComponents();    
        this.applyComponentOrientation(this.orientation);
        getRootPane().setDefaultButton(m_jButtonOK); 
        // Init currencies combo box
        ComboBoxValModel currenciesModel = new ComboBoxValModel(this.currencies);
        this.currencyCbx.setModel(currenciesModel);
        this.currencyCbx.setSelectedItem(this.currencies.get(0));
        this.selectedCurrency = this.currencies.get(0);
        currencyCbx.addActionListener(this);
    }
    
    public void setPrintSelected(boolean value) {
        printselected = value;
    }
    
    public boolean isPrintSelected() {
        return printselected;
    }

    public List<PaymentInfo> getSelectedPayments() {
        return m_aPaymentInfo.getPayments();
    }
            
    public boolean showDialog(double total, CustomerInfoExt customerext) {
        
        m_aPaymentInfo = new PaymentInfoList();
        accepted = false;
        
        m_dTotal = total;
        this.parts = 1;
        
        this.customerext = customerext;        

        m_jButtonPrint.setSelected(printselected);
        this.printTotal();
        
        addTabs();

        if (m_jTabPayment.getTabCount() == 0) {
            // No payment panels available            
            m_aPaymentInfo.add(getDefaultPayment(total));
            accepted = true;            
        } else {
            getRootPane().setDefaultButton(m_jButtonOK);
            refreshParts();
            printState();
            setVisible(true);
        }
        
        // gets the print button state
        printselected = m_jButtonPrint.isSelected();
        
        // remove all tabs        
        m_jTabPayment.removeAll();
        
        return accepted;
    }  
    
    protected abstract void addTabs();
    protected abstract void setStatusPanel(boolean isPositive, boolean isComplete);
    protected abstract PaymentInfo getDefaultPayment(double total);
    
    protected void setOKEnabled(boolean value) {
        m_jButtonOK.setEnabled(value);        
    }
    
    protected void setAddEnabled(boolean value) {
        m_jButtonAdd.setEnabled(value);
    }
        
    protected void addTabPayment(JPaymentCreator jpay) {
        if (app.getAppUserView().getUser().hasPermission(jpay.getKey())) {
            
            JPaymentInterface jpayinterface = payments.get(jpay.getKey());
            if (jpayinterface == null) {
                jpayinterface = jpay.createJPayment();
                payments.put(jpay.getKey(), jpayinterface);
            }
            
            jpayinterface.getComponent().applyComponentOrientation(getComponentOrientation());
            ImageIcon icon = WidgetsBuilder.createIcon(new ImageIcon(getClass().getResource(jpay.getIconKey())));
            m_jTabPayment.addTab(
                    AppLocal.getIntString(jpay.getLabelKey()),
                    icon,
                    jpayinterface.getComponent());
        }
    }
    
    
    public interface JPaymentCreator {
        public JPaymentInterface createJPayment();
        public String getKey();
        public String getLabelKey();
        public String getIconKey();
    }

    public class JPaymentCashCreator implements JPaymentCreator {
        public JPaymentInterface createJPayment() {
            return new JPaymentCashPos(JPaymentSelect.this, dlSystem);
        }
        public String getKey() { return "payment.cash"; }
        public String getLabelKey() { return "tab.cash"; }
        public String getIconKey() { return "/com/openbravo/images/cash.png"; }
    }
        
    public class JPaymentChequeCreator implements JPaymentCreator {
        public JPaymentInterface createJPayment() {
            return new JPaymentCheque(JPaymentSelect.this);
        }
        public String getKey() { return "payment.cheque"; }
        public String getLabelKey() { return "tab.cheque"; }
        public String getIconKey() { return "/com/openbravo/images/desktop.png"; }
    }  
        
    public class JPaymentPaperCreator implements JPaymentCreator {
        public JPaymentInterface createJPayment() {
            return new JPaymentPaper(JPaymentSelect.this, "paperin");
        }
        public String getKey() { return "payment.paper"; }
        public String getLabelKey() { return "tab.paper"; }
        public String getIconKey() { return "/com/openbravo/images/knotes.png"; }
    }
   
    public class JPaymentMagcardCreator implements JPaymentCreator {
        public JPaymentInterface createJPayment() {
            return new JPaymentMagcard(app, JPaymentSelect.this);
        }
        public String getKey() { return "payment.magcard"; }
        public String getLabelKey() { return "tab.magcard"; }
        public String getIconKey() { return "/com/openbravo/images/vcard.png"; }
    }
        
    public class JPaymentFreeCreator implements JPaymentCreator {
        public JPaymentInterface createJPayment() {
            return new JPaymentFree(JPaymentSelect.this);
        }
        public String getKey() { return "payment.free"; }
        public String getLabelKey() { return "tab.free"; }
        public String getIconKey() { return "/com/openbravo/images/package_toys.png"; }
    }
        
    public class JPaymentDebtCreator implements JPaymentCreator {
        public JPaymentInterface createJPayment() {
            return new JPaymentDebt(JPaymentSelect.this);
        }
        public String getKey() { return "payment.debt"; }
        public String getLabelKey() { return "tab.debt"; }
        public String getIconKey() { return "/com/openbravo/images/kdmconfig32.png"; }
    }   
        
    public class JPaymentCashRefundCreator implements JPaymentCreator {
        public JPaymentInterface createJPayment() {
            return new JPaymentRefund(JPaymentSelect.this, "cashrefund");
        }
        public String getKey() { return "refund.cash"; }
        public String getLabelKey() { return "tab.cashrefund"; }
        public String getIconKey() { return "/com/openbravo/images/cash.png"; }
    }
        
    public class JPaymentChequeRefundCreator implements JPaymentCreator {
        public JPaymentInterface createJPayment() {
            return new JPaymentRefund(JPaymentSelect.this, "chequerefund");
        }
        public String getKey() { return "refund.cheque"; }
        public String getLabelKey() { return "tab.chequerefund"; }
        public String getIconKey() { return "/com/openbravo/images/desktop.png"; }
    }
       
    public class JPaymentPaperRefundCreator implements JPaymentCreator {
        public JPaymentInterface createJPayment() {
            return new JPaymentRefund(JPaymentSelect.this, "paperout");
        }
        public String getKey() { return "refund.paper"; }
        public String getLabelKey() { return "tab.paper"; }
        public String getIconKey() { return "/com/openbravo/images/knotes.png"; }
    }    
       
    public class JPaymentMagcardRefundCreator implements JPaymentCreator {
       public JPaymentInterface createJPayment() {     
           return new JPaymentMagcard(app, JPaymentSelect.this);
        }
        public String getKey() { return "refund.magcard"; }
        public String getLabelKey() { return "tab.magcard"; }
        public String getIconKey() { return "/com/openbravo/images/vcard.png"; }
    }    
    
    protected void setHeaderVisible(boolean value) {
        jPanel6.setVisible(value);
    }

    private void switchCurrency(CurrencyInfo currency) {
        this.selectedCurrency = currency;
        this.printTotal();
        this.printState();
    }

    private void printTotal() {
        double totalCurrency = m_dTotal;
        if (!this.selectedCurrency.isMain()) {
            totalCurrency *= this.selectedCurrency.getRate();
        }
        Formats.setAltCurrency(this.selectedCurrency);
        m_jTotalEuros.setText(Formats.CURRENCY.formatValue(new Double(totalCurrency)));
    }

    private void printState() {
        double remainingMain = m_dTotal - m_aPaymentInfo.getTotal();
        double remainingCurrency = remainingMain;
        if (!this.selectedCurrency.isMain()) {
            remainingCurrency *= this.selectedCurrency.getRate();
        }
        Formats.setAltCurrency(this.selectedCurrency);
        m_jRemaininglEuros.setText(Formats.CURRENCY.formatValue(new Double(remainingCurrency)));
        m_jButtonRemove.setEnabled(!m_aPaymentInfo.isEmpty());
        m_jTabPayment.setSelectedIndex(0); // selecciono el primero
        // Refresh current payment panel
        double partCurrency = this.nextPartAmount;
        if (!this.selectedCurrency.isMain()) {
            partCurrency *= this.selectedCurrency.getRate();
        }
        ((JPaymentInterface) m_jTabPayment.getSelectedComponent()).activate(
                customerext, remainingCurrency, partCurrency,
                this.selectedCurrency, m_sTransactionID);
    }
    
    private void refreshParts() {
        double partAmount = this.m_dTotal / this.parts;
        int remainingParts = this.parts - this.m_aPaymentInfo.size();
        if (remainingParts <= 0) {
            // Not usefull isn't it ?
            this.nextPartAmount = m_dTotal - m_aPaymentInfo.getTotal();
        } else {
            double roundedPartAmount = (double)Math.round(partAmount * 100) / 100;
            if (remainingParts == 1) {
                // Last one is unlucky (or not)
                this.nextPartAmount = m_dTotal - m_aPaymentInfo.getTotal();
            } else {
                this.nextPartAmount = roundedPartAmount;
            }
        }
        // Refresh display
        this.jlblPartsNumber.setText(String.valueOf(this.parts));
        this.jlblPartAmount.setVisible(this.parts != 1);
        this.jlblPartAmountLabel.setVisible(this.parts != 1);
        this.jlblPartAmount.setText(Formats.CURRENCY.formatValue(new Double(partAmount)));
        // Reactivate current tab to refresh part
        double remainingCurrency = m_dTotal - m_aPaymentInfo.getTotal();
        double partCurrency = this.nextPartAmount;
        if (!this.selectedCurrency.isMain()) {
            remainingCurrency *= this.selectedCurrency.getRate();
            partCurrency *= this.selectedCurrency.getRate();
        }
        ((JPaymentInterface) m_jTabPayment.getSelectedComponent()).activate(
                customerext, remainingCurrency, partCurrency,
                this.selectedCurrency, m_sTransactionID);
    }
    
    protected static Window getWindow(Component parent) {
        if (parent == null) {
            return new JFrame();
        } else if (parent instanceof Frame || parent instanceof Dialog) {
            return (Window)parent;
        } else {
            return getWindow(parent.getParent());
        }
    }       
    
    public void setStatus(boolean isPositive, boolean isComplete) {
        
        setStatusPanel(isPositive, isComplete);
    }
    
    public void setTransactionID(String tID){
        this.m_sTransactionID = tID;
    }

    public void actionPerformed(java.awt.event.ActionEvent evt) {
        CurrencyInfo c = this.currencies.get(currencyCbx.getSelectedIndex());
        this.switchCurrency(c);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel4 = new javax.swing.JPanel();
        jPanel6 = new javax.swing.JPanel();
        m_jButtonAdd = WidgetsBuilder.createButton(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/btnplus.png")));
        m_jButtonRemove = WidgetsBuilder.createButton(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/btnminus.png")));
        jPanel3 = new javax.swing.JPanel();
        m_jTabPayment = new javax.swing.JTabbedPane();
        jPanel5 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        m_jButtonPrint = new javax.swing.JToggleButton();
        jPanel1 = new javax.swing.JPanel();
        m_jButtonOK = WidgetsBuilder.createButton(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/button_ok.png")),
                                                  AppLocal.getIntString("Button.OK"),
                                                  WidgetsBuilder.SIZE_MEDIUM);
        m_jButtonCancel = WidgetsBuilder.createButton(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/button_cancel.png")),
                                                  AppLocal.getIntString("Button.Cancel"),
                                                  WidgetsBuilder.SIZE_MEDIUM);
        currencyCbx = WidgetsBuilder.createComboBox();
        
        // Total/remaining/part amount init
        m_jLblTotalEuros1 = WidgetsBuilder.createImportantLabel(AppLocal.getIntString("label.totalcash"));
        m_jTotalEuros = WidgetsBuilder.createImportantLabel();
        m_jRemaininglEuros = WidgetsBuilder.createImportantLabel();
        m_jLblRemainingEuros = WidgetsBuilder.createImportantLabel(AppLocal.getIntString("label.remainingcash"));
        jlblPartAmountLabel = WidgetsBuilder.createImportantLabel(AppLocal.getIntString("label.partamount"));
        jlblPartAmount = WidgetsBuilder.createImportantLabel();
        WidgetsBuilder.inputStyle(jlblPartAmount);
        WidgetsBuilder.inputStyle(m_jTotalEuros);
        WidgetsBuilder.inputStyle(m_jRemaininglEuros);
        
        // Parts init
        JLabel jlblParts = WidgetsBuilder.createLabel(AppLocal.getIntString("label.parts"));
        jlblPartsNumber = WidgetsBuilder.createLabel(String.valueOf(parts));
        WidgetsBuilder.inputStyle(jlblPartsNumber);
        jbtnPartsPlus = WidgetsBuilder.createButton(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/btnplus.png")));
        jbtnPartsMinus = WidgetsBuilder.createButton(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/btnminus.png")));
        jbtnPartsPlus.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                partsPlusActionPerformed(evt);
            }
        });
        jbtnPartsMinus.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                partsMinusActionPerformed(evt);
            }
        });
        jbtnPartsPlus.setFocusable(false);
        jbtnPartsMinus.setFocusable(false);
        
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle(AppLocal.getIntString("payment.title")); // NOI18N
        setResizable(false);

        // Total/remaining/part amount line
        jPanel4.add(m_jLblTotalEuros1);
        jPanel4.add(m_jTotalEuros);

        jPanel6.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 5, 0));
        jPanel4.add(m_jLblRemainingEuros);
        jPanel4.add(m_jRemaininglEuros);
        
        jPanel4.add(jlblPartAmountLabel);
        jPanel4.add(jlblPartAmount);

        // Currency select
        if (this.currencies.size() > 1) {
            jPanel4.add(currencyCbx);
        }

        // Add/remove payment
        m_jButtonAdd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_jButtonAddActionPerformed(evt);
            }
        });
        jPanel6.add(m_jButtonAdd);

        m_jButtonRemove.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_jButtonRemoveActionPerformed(evt);
            }
        });
        jPanel6.add(m_jButtonRemove);

        jPanel4.add(jPanel6);

        getContentPane().add(jPanel4, java.awt.BorderLayout.NORTH);

        jPanel3.setLayout(new java.awt.BorderLayout());

        m_jTabPayment.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        m_jTabPayment.setTabPlacement(javax.swing.JTabbedPane.LEFT);
        m_jTabPayment.setFocusable(false);
        m_jTabPayment.setRequestFocusEnabled(false);
        m_jTabPayment.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                m_jTabPaymentStateChanged(evt);
            }
        });
        jPanel3.add(m_jTabPayment, java.awt.BorderLayout.CENTER);

        getContentPane().add(jPanel3, java.awt.BorderLayout.CENTER);

        jPanel5.setLayout(new java.awt.BorderLayout());

        // Parts line
        JPanel partsContainer = new JPanel();
        partsContainer.add(jlblParts);
        partsContainer.add(jbtnPartsMinus);
        partsContainer.add(jlblPartsNumber);
        partsContainer.add(jbtnPartsPlus);

        jPanel5.add(partsContainer, java.awt.BorderLayout.LINE_START);

        // Print/Ok/Cancel line
        m_jButtonPrint.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/fileprint.png"))); // NOI18N
        WidgetsBuilder.adaptSize(m_jButtonPrint, WidgetsBuilder.SIZE_MEDIUM);
        m_jButtonPrint.setSelected(true);
        m_jButtonPrint.setFocusPainted(false);
        m_jButtonPrint.setFocusable(false);
        m_jButtonPrint.setRequestFocusEnabled(false);
        jPanel2.add(m_jButtonPrint);
        jPanel2.add(jPanel1);

        m_jButtonOK.setFocusPainted(false);
        m_jButtonOK.setFocusable(false);
        m_jButtonOK.setRequestFocusEnabled(false);
        m_jButtonOK.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_jButtonOKActionPerformed(evt);
            }
        });
        jPanel2.add(m_jButtonOK);

        m_jButtonCancel.setFocusPainted(false);
        m_jButtonCancel.setFocusable(false);
        m_jButtonCancel.setRequestFocusEnabled(false);
        m_jButtonCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_jButtonCancelActionPerformed(evt);
            }
        });
        jPanel2.add(m_jButtonCancel);
        
        jPanel5.add(jPanel2, java.awt.BorderLayout.LINE_END);

        getContentPane().add(jPanel5, java.awt.BorderLayout.SOUTH);

        java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        // Icons are to big for a 72dpi density, they don't fit in a popup less than 640x480px
        int minWidth = 680;
        int minHeight = 480;
        int width = WidgetsBuilder.dipToPx(460);
        int height = WidgetsBuilder.dipToPx(350);
        width = Math.max(minWidth, width);
        height = Math.max(minHeight, height);
        // If popup is big enough, make it fullscreen
        if (width > 0.8 * screenSize.width || height > 0.8 * screenSize.height) {
            width = screenSize.width;
            height = screenSize.height;
            this.setUndecorated(true);
        }
        setBounds((screenSize.width-width)/2, (screenSize.height-height)/2, width, height);
    }// </editor-fold>//GEN-END:initComponents

    private void m_jButtonRemoveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_jButtonRemoveActionPerformed

        m_aPaymentInfo.removeLast();
        this.refreshParts(); // Recalculate next part amount
        printState();        
        
    }//GEN-LAST:event_m_jButtonRemoveActionPerformed

    private void m_jButtonAddActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_jButtonAddActionPerformed

        PaymentInfo returnPayment = ((JPaymentInterface) m_jTabPayment.getSelectedComponent()).executePayment();
        if (returnPayment != null) {
            m_aPaymentInfo.add(returnPayment);
            this.refreshParts(); // Recalculate next part amount
            printState();
        }        
        
    }//GEN-LAST:event_m_jButtonAddActionPerformed

    private void m_jTabPaymentStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_m_jTabPaymentStateChanged

        if (m_jTabPayment.getSelectedComponent() != null) {
            double remainingCurrency = m_dTotal - m_aPaymentInfo.getTotal();
            double partCurrency = this.nextPartAmount;
            if (!this.selectedCurrency.isMain()) {
                remainingCurrency *= this.selectedCurrency.getRate();
                partCurrency *= this.selectedCurrency.getRate();
            }
            ((JPaymentInterface) m_jTabPayment.getSelectedComponent()).activate(
                    customerext, remainingCurrency, partCurrency,
                    this.selectedCurrency, m_sTransactionID);
        }
        
    }//GEN-LAST:event_m_jTabPaymentStateChanged

    private void m_jButtonOKActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_jButtonOKActionPerformed
        
        PaymentInfo returnPayment = ((JPaymentInterface) m_jTabPayment.getSelectedComponent()).executePayment();
        if (returnPayment != null) {
            m_aPaymentInfo.add(returnPayment);
            accepted = true;
            dispose();
        }           
        
    }//GEN-LAST:event_m_jButtonOKActionPerformed

    private void m_jButtonCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_jButtonCancelActionPerformed

        dispose();
        
    }//GEN-LAST:event_m_jButtonCancelActionPerformed
    
    private void partsPlusActionPerformed(java.awt.event.ActionEvent evt) {
        this.parts++;
        this.refreshParts();
    }
    
    private void partsMinusActionPerformed(java.awt.event.ActionEvent evt) {
        if (this.parts > 1) {
            this.parts--;
            this.refreshParts();
        }
    }
    
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JButton m_jButtonAdd;
    private javax.swing.JButton m_jButtonCancel;
    private javax.swing.JButton m_jButtonOK;
    private javax.swing.JToggleButton m_jButtonPrint;
    private javax.swing.JButton m_jButtonRemove;
    private javax.swing.JLabel m_jLblRemainingEuros;
    private javax.swing.JLabel m_jLblTotalEuros1;
    private javax.swing.JLabel m_jRemaininglEuros;
    private javax.swing.JTabbedPane m_jTabPayment;
    private javax.swing.JLabel m_jTotalEuros;
    private javax.swing.JComboBox currencyCbx;
    
    private JLabel jlblPartsNumber;
    private JButton jbtnPartsPlus;
    private JButton jbtnPartsMinus;
    private JLabel jlblPartAmountLabel;
    private JLabel jlblPartAmount;
}
