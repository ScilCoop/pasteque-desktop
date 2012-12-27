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

package com.openbravo.pos.sales;

import com.openbravo.basic.BasicException;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JFrame;
import com.openbravo.pos.forms.AppConfig;
import com.openbravo.pos.forms.AppLocal;
import com.openbravo.pos.forms.AppView;
import com.openbravo.pos.ticket.TicketLineInfo;
import com.openbravo.pos.widgets.JEditorCurrency;
import com.openbravo.pos.widgets.JEditorDouble;
import com.openbravo.pos.widgets.JEditorKeys;
import com.openbravo.pos.widgets.JEditorString;
import com.openbravo.pos.widgets.WidgetsBuilder;

/**
 *
 * @author adrianromero
 */
public class JProductLineEdit extends javax.swing.JDialog {
    
    private TicketLineInfo returnLine;
    private TicketLineInfo m_oLine;
    private boolean m_bunitsok;
    private boolean m_bpriceok;
            
    /** Creates new form JProductLineEdit */
    private JProductLineEdit(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
    }
    /** Creates new form JProductLineEdit */
    private JProductLineEdit(java.awt.Dialog parent, boolean modal) {
        super(parent, modal);
    }
    
    private TicketLineInfo init(AppView app, TicketLineInfo oLine) throws BasicException {
        // Inicializo los componentes
        initComponents();

        if (oLine.getTaxInfo() == null) {
            throw new BasicException(AppLocal.getIntString("message.cannotcalculatetaxes"));
        }

        m_oLine = new TicketLineInfo(oLine);
        m_bunitsok = true;
        m_bpriceok = true;

        m_jName.setEnabled(m_oLine.getProductID() == null && app.getAppUserView().getUser().hasPermission("com.openbravo.pos.sales.JPanelTicketEdits"));
        m_jPrice.setEnabled(app.getAppUserView().getUser().hasPermission("com.openbravo.pos.sales.JPanelTicketEdits"));
        m_jPriceTax.setEnabled(app.getAppUserView().getUser().hasPermission("com.openbravo.pos.sales.JPanelTicketEdits"));
        
        m_jName.setText(m_oLine.getProperty("product.name"));
        m_jUnits.setDoubleValue(oLine.getMultiply());
        m_jPrice.setDoubleValue(oLine.getPrice()); 
        m_jPriceTax.setDoubleValue(oLine.getPriceTax());
        m_jTaxrate.setText(oLine.getTaxInfo().getName());
        
        m_jName.addPropertyChangeListener("Edition", new RecalculateName());
        m_jUnits.addPropertyChangeListener("Edition", new RecalculateUnits());
        m_jPrice.addPropertyChangeListener("Edition", new RecalculatePrice());
        m_jPriceTax.addPropertyChangeListener("Edition", new RecalculatePriceTax());

        m_jName.addEditorKeys(m_jKeys);
        m_jUnits.addEditorKeys(m_jKeys);
        m_jPrice.addEditorKeys(m_jKeys);
        m_jPriceTax.addEditorKeys(m_jKeys);
        
        if (m_jName.isEnabled()) {
            m_jName.activate();
        } else {
            m_jUnits.activate();
        }
        
        printTotals();

        getRootPane().setDefaultButton(m_jButtonOK);   
        returnLine = null;
        setVisible(true);
      
        return returnLine;
    }
    
    private void printTotals() {
        
        if (m_bunitsok && m_bpriceok) {
            m_jSubtotal.setText(m_oLine.printSubValue());
            m_jTotal.setText(m_oLine.printValue());
            m_jButtonOK.setEnabled(true);
       } else {
            m_jSubtotal.setText(null);
            m_jTotal.setText(null);
            m_jButtonOK.setEnabled(false);
        }
    }
    
    private class RecalculateUnits implements PropertyChangeListener {
        public void propertyChange(PropertyChangeEvent evt) {
            Double value = m_jUnits.getDoubleValue();
            if (value == null || value == 0.0) {
                m_bunitsok = false;
            } else {
                m_oLine.setMultiply(value);
                m_bunitsok = true;                
            }

            printTotals();
        }
    }
    
    private class RecalculatePrice implements PropertyChangeListener {
        public void propertyChange(PropertyChangeEvent evt) {

            Double value = m_jPrice.getDoubleValue();
            if (value == null || value == 0.0) {
                m_bpriceok = false;
            } else {
                m_oLine.setPrice(value);
                m_jPriceTax.setDoubleValue(m_oLine.getPriceTax());
                m_bpriceok = true;
            }

            printTotals();
        }
    }    
    
    private class RecalculatePriceTax implements PropertyChangeListener {
        public void propertyChange(PropertyChangeEvent evt) {

            Double value = m_jPriceTax.getDoubleValue();
            if (value == null || value == 0.0) {
                // m_jPriceTax.setValue(m_oLine.getPriceTax());
                m_bpriceok = false;
            } else {
                m_oLine.setPriceTax(value);
                m_jPrice.setDoubleValue(m_oLine.getPrice());
                m_bpriceok = true;
            }

            printTotals();
        }
    }   
    
    private class RecalculateName implements PropertyChangeListener {
        public void propertyChange(PropertyChangeEvent evt) {
            m_oLine.setProperty("product.name", m_jName.getText());
        }
    }   
    
    private static Window getWindow(Component parent) {
        if (parent == null) {
            return new JFrame();
        } else if (parent instanceof Frame || parent instanceof Dialog) {
            return (Window)parent;
        } else {
            return getWindow(parent.getParent());
        }
    }       
    
    public static TicketLineInfo showMessage(Component parent, AppView app, TicketLineInfo oLine) throws BasicException {
         
        Window window = getWindow(parent);
        
        JProductLineEdit myMsg;
        if (window instanceof Frame) { 
            myMsg = new JProductLineEdit((Frame) window, true);
        } else {
            myMsg = new JProductLineEdit((Dialog) window, true);
        }
        return myMsg.init(app, oLine);
    }        

    private void initComponents() {
        AppConfig cfg = AppConfig.loadedInstance;
        int btnspacing = WidgetsBuilder.pixelSize(Float.parseFloat(cfg.getProperty("ui.touchbtnspacing")));
        int marginInset = 10;

        jPanel5 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        priceLbl = WidgetsBuilder.createLabel(AppLocal.getIntString("label.price"));
        unitsLbl = WidgetsBuilder.createLabel(AppLocal.getIntString("label.units"));
        pricetaxLbl = WidgetsBuilder.createLabel(AppLocal.getIntString("label.pricetax"));
        itemLbl = WidgetsBuilder.createLabel(AppLocal.getIntString("label.item"));
        m_jName = new JEditorString();
        m_jUnits = new JEditorDouble();
        m_jPrice = new JEditorCurrency();
        m_jPriceTax = new JEditorCurrency();
        m_jTaxrate = WidgetsBuilder.createLabel(null);
        taxLbl = WidgetsBuilder.createLabel(AppLocal.getIntString("label.tax"));
        totalLbl = WidgetsBuilder.createLabel(AppLocal.getIntString("label.totalcash"));
        m_jTotal = WidgetsBuilder.createLabel(null);
        subtotalLbl = WidgetsBuilder.createLabel(AppLocal.getIntString("label.subtotalcash"));
        m_jSubtotal = WidgetsBuilder.createLabel(null);
        jPanel1 = new javax.swing.JPanel();
        m_jButtonOK = WidgetsBuilder.createButton(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/button_ok.png")),
                                                  AppLocal.getIntString("Button.OK"),
                                                  WidgetsBuilder.SIZE_MEDIUM);
        m_jButtonCancel = WidgetsBuilder.createButton(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/button_cancel.png")),
                                                      AppLocal.getIntString("Button.Cancel"),
                                                      WidgetsBuilder.SIZE_MEDIUM);
        jPanel3 = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        m_jKeys = new JEditorKeys();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle(AppLocal.getIntString("label.editline")); // NOI18N

        jPanel5.setLayout(new java.awt.BorderLayout());

        jPanel2.setLayout(new GridBagLayout());

        GridBagConstraints cstr = new GridBagConstraints();
        cstr.gridx = 0;
        cstr.gridy = 0;
        cstr.anchor = GridBagConstraints.LINE_START;
        cstr.insets = new Insets(marginInset, marginInset, btnspacing, marginInset);
        jPanel2.add(itemLbl, cstr);
        cstr = new GridBagConstraints();
        cstr.gridx = 1;
        cstr.gridy = 0;
        cstr.insets = new Insets(marginInset, 0, btnspacing, marginInset);
        cstr.fill = GridBagConstraints.HORIZONTAL;
        cstr.weightx = 0.6;
        jPanel2.add(m_jName, cstr);

        cstr = new GridBagConstraints();
        cstr.gridx = 0;
        cstr.gridy = 1;
        cstr.insets = new Insets(0, marginInset, btnspacing, marginInset);
        cstr.anchor = GridBagConstraints.LINE_START;
        jPanel2.add(unitsLbl, cstr);
        cstr = new GridBagConstraints();
        cstr.gridx = 1;
        cstr.gridy = 1;
        cstr.insets = new Insets(0, 0, btnspacing, marginInset);
        cstr.fill = GridBagConstraints.HORIZONTAL;
        cstr.weightx = 0.6;
        jPanel2.add(m_jUnits, cstr);

        cstr = new GridBagConstraints();
        cstr.gridx = 0;
        cstr.gridy = 2;
        cstr.insets = new Insets(0, marginInset, btnspacing, marginInset);
        cstr.anchor = GridBagConstraints.LINE_START;
        jPanel2.add(priceLbl, cstr);

        cstr = new GridBagConstraints();
        cstr.gridx = 0;
        cstr.gridy = 3;
        cstr.insets = new Insets(0, marginInset, btnspacing, marginInset);
        cstr.anchor = GridBagConstraints.LINE_START;
        jPanel2.add(pricetaxLbl, cstr);
        
        cstr = new GridBagConstraints();
        cstr.gridx = 0;
        cstr.gridy = 4;
        cstr.insets = new Insets(0, marginInset, btnspacing, marginInset);
        cstr.anchor = GridBagConstraints.LINE_START;
        jPanel2.add(taxLbl, cstr);

        cstr = new GridBagConstraints();
        cstr.gridx = 0;
        cstr.gridy = 5;
        cstr.insets = new Insets(0, marginInset, btnspacing, marginInset);
        cstr.anchor = GridBagConstraints.LINE_START;
        jPanel2.add(subtotalLbl, cstr);

        cstr = new GridBagConstraints();
        cstr.gridx = 0;
        cstr.gridy = 6;
        cstr.insets = new Insets(0, marginInset, marginInset, marginInset);
        cstr.anchor = GridBagConstraints.LINE_START;
        jPanel2.add(totalLbl, cstr);

        cstr = new GridBagConstraints();
        cstr.gridx = 1;
        cstr.gridy = 2;
        cstr.fill = GridBagConstraints.HORIZONTAL;
        cstr.weightx = 0.6;
        cstr.insets = new Insets(0, 0, btnspacing, marginInset);
        jPanel2.add(m_jPrice, cstr);
        cstr = new GridBagConstraints();
        cstr.gridx = 1;
        cstr.gridy = 3;
        cstr.fill = GridBagConstraints.HORIZONTAL;
        cstr.weightx = 0.6;
        cstr.insets = new Insets(0, 0, btnspacing, marginInset);
        jPanel2.add(m_jPriceTax, cstr);

        m_jTaxrate.setBackground(javax.swing.UIManager.getDefaults().getColor("TextField.disabledBackground"));
        m_jTaxrate.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        m_jTaxrate.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createLineBorder(javax.swing.UIManager.getDefaults().getColor("Button.darkShadow")), javax.swing.BorderFactory.createEmptyBorder(1, 4, 1, 4)));
        m_jTaxrate.setOpaque(true);
        m_jTaxrate.setPreferredSize(new java.awt.Dimension(150, 25));
        m_jTaxrate.setRequestFocusEnabled(false);
        cstr = new GridBagConstraints();
        cstr.gridx = 1;
        cstr.gridy = 4;
        cstr.fill = GridBagConstraints.HORIZONTAL;
        cstr.weightx = 0.6;
        cstr.insets = new Insets(0, 0, btnspacing, marginInset);
        jPanel2.add(m_jTaxrate, cstr);

        m_jTotal.setBackground(javax.swing.UIManager.getDefaults().getColor("TextField.disabledBackground"));
        m_jTotal.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        m_jTotal.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createLineBorder(javax.swing.UIManager.getDefaults().getColor("Button.darkShadow")), javax.swing.BorderFactory.createEmptyBorder(1, 4, 1, 4)));
        m_jTotal.setOpaque(true);
        m_jTotal.setPreferredSize(new java.awt.Dimension(150, 25));
        m_jTotal.setRequestFocusEnabled(false);
        cstr = new GridBagConstraints();
        cstr.gridx = 1;
        cstr.gridy = 6;
        cstr.fill = GridBagConstraints.HORIZONTAL;
        cstr.weightx = 0.6;
        cstr.insets = new Insets(0, 0, marginInset, marginInset);
        jPanel2.add(m_jTotal, cstr);

        m_jSubtotal.setBackground(javax.swing.UIManager.getDefaults().getColor("TextField.disabledBackground"));
        m_jSubtotal.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        m_jSubtotal.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createLineBorder(javax.swing.UIManager.getDefaults().getColor("Button.darkShadow")), javax.swing.BorderFactory.createEmptyBorder(1, 4, 1, 4)));
        m_jSubtotal.setOpaque(true);
        m_jSubtotal.setPreferredSize(new java.awt.Dimension(150, 25));
        m_jSubtotal.setRequestFocusEnabled(false);
        cstr = new GridBagConstraints();
        cstr.gridx = 1;
        cstr.gridy = 5;
        cstr.fill = GridBagConstraints.HORIZONTAL;
        cstr.weightx = 0.6;
        cstr.insets = new Insets(0, 0, btnspacing, marginInset);
        jPanel2.add(m_jSubtotal, cstr);

        jPanel5.add(jPanel2, java.awt.BorderLayout.CENTER);

        jPanel1.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

        m_jButtonOK.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_jButtonOKActionPerformed(evt);
            }
        });
        jPanel1.add(m_jButtonOK);

        m_jButtonCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_jButtonCancelActionPerformed(evt);
            }
        });
        jPanel1.add(m_jButtonCancel);

        jPanel5.add(jPanel1, java.awt.BorderLayout.SOUTH);

        getContentPane().add(jPanel5, java.awt.BorderLayout.CENTER);

        jPanel3.setLayout(new java.awt.BorderLayout());

        jPanel4.setLayout(new javax.swing.BoxLayout(jPanel4, javax.swing.BoxLayout.Y_AXIS));
        jPanel4.add(m_jKeys);

        jPanel3.add(jPanel4, java.awt.BorderLayout.NORTH);

        getContentPane().add(jPanel3, java.awt.BorderLayout.EAST);

        java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        int minWidth = 580;
        int minHeight = 362;
        int width = WidgetsBuilder.dipToPx(460);
        int height = WidgetsBuilder.dipToPx(260);
        width = Math.max(minWidth, width);
        height = Math.max(minHeight, height);
        // If popup is big enough, make it fullscreen
        if (width > 0.8 * screenSize.width || height > 0.8 * screenSize.height) {
            width = screenSize.width;
            height = screenSize.height;
            this.setUndecorated(true);
        }
        setBounds((screenSize.width-width)/2, (screenSize.height-height)/2, width, height);
    }

    private void m_jButtonCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_jButtonCancelActionPerformed

        dispose();

    }//GEN-LAST:event_m_jButtonCancelActionPerformed

    private void m_jButtonOKActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_jButtonOKActionPerformed

        returnLine = m_oLine;
        
        dispose();

    }//GEN-LAST:event_m_jButtonOKActionPerformed
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel priceLbl;
    private javax.swing.JLabel unitsLbl;
    private javax.swing.JLabel pricetaxLbl;
    private javax.swing.JLabel itemLbl;
    private javax.swing.JLabel taxLbl;
    private javax.swing.JLabel totalLbl;
    private javax.swing.JLabel subtotalLbl;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JButton m_jButtonCancel;
    private javax.swing.JButton m_jButtonOK;
    private JEditorKeys m_jKeys;
    private JEditorString m_jName;
    private JEditorCurrency m_jPrice;
    private JEditorCurrency m_jPriceTax;
    private javax.swing.JLabel m_jSubtotal;
    private javax.swing.JLabel m_jTaxrate;
    private javax.swing.JLabel m_jTotal;
    private JEditorDouble m_jUnits;
    // End of variables declaration//GEN-END:variables
    
}
