//    POS-Tech
//    Based upon Openbravo POS
//
//    Copyright (C) 2007-2009 Openbravo, S.L.
//                       2012 Scil (http://scil.coop)
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
import java.awt.Dimension;
import java.awt.GridLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import com.openbravo.format.Formats;
import com.openbravo.pos.forms.AppLocal;
import com.openbravo.pos.customers.CustomerInfoExt;
import com.openbravo.pos.util.RoundUtils;
import com.openbravo.pos.widgets.JEditorCurrencyPositive;
import com.openbravo.pos.widgets.JEditorKeys;
import com.openbravo.pos.widgets.WidgetsBuilder;

/**
 *
 * @author  adrianromero
 */
public class JPaymentDebt extends javax.swing.JPanel implements JPaymentInterface {
    
    private JPaymentNotifier notifier;
    private CustomerInfoExt customerext;
    
    private double m_dPaid;
    private double m_dTotal;

    /** Creates new form JPaymentDebt */
    public JPaymentDebt(JPaymentNotifier notifier) {
        
        this.notifier = notifier;
        
        initComponents();  
        
        m_jTendered.addPropertyChangeListener("Edition", new RecalculateState());
        m_jTendered.addEditorKeys(m_jKeys);
        
    }
    
    public void activate(CustomerInfoExt customerext, double dTotal, String transID) {
        
        this.customerext = customerext;
        m_dTotal = dTotal;
        
        m_jTendered.reset();
        
        // 
        if (customerext == null) {
            m_jName.setText(null);
            m_jNotes.setText(null);
            txtMaxdebt.setText(null);
            txtCurdate.setText(null);        
            txtCurdebt.setText(null);
            
            m_jKeys.setEnabled(false);
            m_jTendered.setEnabled(false);
            
            
        } else {            
            m_jName.setText(customerext.getName());
            m_jNotes.setText(customerext.getNotes());
            txtMaxdebt.setText(Formats.CURRENCY.formatValue(RoundUtils.getValue(customerext.getMaxdebt())));
            txtCurdate.setText(Formats.DATE.formatValue(customerext.getCurdate()));        
            txtCurdebt.setText(Formats.CURRENCY.formatValue(RoundUtils.getValue(customerext.getCurdebt())));   
                
            if (RoundUtils.compare(RoundUtils.getValue(customerext.getCurdebt()), RoundUtils.getValue(customerext.getMaxdebt())) >= 0)  {
                m_jKeys.setEnabled(false);
                m_jTendered.setEnabled(false);                
            } else {    
                m_jKeys.setEnabled(true);
                m_jTendered.setEnabled(true);
                m_jTendered.activate();  
            }
        }        
        
        printState();
        
    }
    public PaymentInfo executePayment() {
        return new PaymentInfoTicket(m_dPaid, "debt");      
    }
    public Component getComponent() {
        return this;
    }

    private void printState() {
        
        if (customerext == null) {
            m_jMoneyEuros.setText(null);
            jlblMessage.setText(AppLocal.getIntString("message.nocustomernodebt"));
            notifier.setStatus(false, false);
        } else {
            Double value = m_jTendered.getDoubleValue();
            if (value == null || value == 0.0) {
                m_dPaid = m_dTotal;
            } else {
                m_dPaid = value;
            } 

            m_jMoneyEuros.setText(Formats.CURRENCY.formatValue(new Double(m_dPaid)));
            
            
            if (RoundUtils.compare(RoundUtils.getValue(customerext.getCurdebt()) + m_dPaid, RoundUtils.getValue(customerext.getMaxdebt())) >= 0)  { 
                // maximum debt exceded
                jlblMessage.setText(AppLocal.getIntString("message.customerdebtexceded"));
                notifier.setStatus(false, false);
            } else {
                jlblMessage.setText(null);
                int iCompare = RoundUtils.compare(m_dPaid, m_dTotal);
                // if iCompare > 0 then the payment is not valid
                notifier.setStatus(m_dPaid > 0.0 && iCompare <= 0, iCompare == 0);
            }
        }        
    }
    
    private class RecalculateState implements PropertyChangeListener {
        public void propertyChange(PropertyChangeEvent evt) {
            printState();
        }
    }     
    
    private void initComponents() {
        setLayout(new java.awt.BorderLayout());

        // Setup right part (inputContainer): keyboard
        JPanel inputContainer = new javax.swing.JPanel();
        m_jKeys = new com.openbravo.pos.widgets.JEditorKeys();
        m_jTendered = new JEditorCurrencyPositive();
        JPanel jPanel1 = new javax.swing.JPanel();
        JPanel jPanel3 = new javax.swing.JPanel();
        
        jPanel1.setLayout(new javax.swing.BoxLayout(jPanel1, javax.swing.BoxLayout.Y_AXIS));
        jPanel1.add(m_jKeys);

        jPanel3.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        jPanel3.setLayout(new java.awt.BorderLayout());
        jPanel3.add(m_jTendered, java.awt.BorderLayout.CENTER);

        jPanel1.add(jPanel3);
        
        inputContainer.setLayout(new java.awt.BorderLayout());
        inputContainer.add(jPanel1, java.awt.BorderLayout.NORTH);

        // Setup left part (paymentInfoContainer): payment info (amount)
        JPanel paymentInfoContainer = new JPanel();
        paymentInfoContainer.setLayout(new BoxLayout(paymentInfoContainer, BoxLayout.Y_AXIS));
        JPanel debtInfoContainer = new JPanel(new GridLayout(6, 2, 8, 8));
        
        // First line: debt amount
        JLabel debtLabel = WidgetsBuilder.createLabel(AppLocal.getIntString("label.debt"));
        debtInfoContainer.add(debtLabel);
        m_jMoneyEuros = WidgetsBuilder.createLabel();
        m_jMoneyEuros.setBackground(new java.awt.Color(153, 153, 255));
        m_jMoneyEuros.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        m_jMoneyEuros.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createLineBorder(javax.swing.UIManager.getDefaults().getColor("Button.darkShadow")), javax.swing.BorderFactory.createEmptyBorder(1, 4, 1, 4)));
        m_jMoneyEuros.setOpaque(true);
        debtInfoContainer.add(m_jMoneyEuros);
        // Second line: debitor name
        JLabel name = WidgetsBuilder.createLabel(AppLocal.getIntString("label.name"));
        debtInfoContainer.add(name);
        m_jName = new javax.swing.JTextField();
        m_jName.setFont(WidgetsBuilder.getFont(WidgetsBuilder.SIZE_MEDIUM));
        m_jName.setPreferredSize(new Dimension(m_jName.getPreferredSize().width, m_jName.getPreferredSize().height * 3));
        m_jName.setEditable(false);        
        debtInfoContainer.add(m_jName);
        // Third line: notes
        JLabel notes = WidgetsBuilder.createLabel(AppLocal.getIntString("label.notes"));
        debtInfoContainer.add(notes);
        m_jNotes = new javax.swing.JTextArea();
        m_jNotes.setFont(WidgetsBuilder.getFont(WidgetsBuilder.SIZE_SMALL));
        m_jNotes.setEditable(false);
        debtInfoContainer.add(m_jNotes);
        // Fourth line: max debt
        JLabel maxDebt = WidgetsBuilder.createLabel(AppLocal.getIntString("label.maxdebt"));
        debtInfoContainer.add(maxDebt);
        txtMaxdebt = new javax.swing.JTextField();
        txtMaxdebt.setFont(WidgetsBuilder.getFont(WidgetsBuilder.SIZE_MEDIUM));
        txtMaxdebt.setEditable(false);
        txtMaxdebt.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        debtInfoContainer.add(txtMaxdebt);
        // Fifth line: current debt
        JLabel currentDebt = WidgetsBuilder.createLabel(AppLocal.getIntString("label.curdebt"));
        debtInfoContainer.add(currentDebt);
        txtCurdebt = new javax.swing.JTextField();
        txtCurdebt.setFont(WidgetsBuilder.getFont(WidgetsBuilder.SIZE_MEDIUM));
        txtCurdebt.setEditable(false);
        txtCurdebt.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        debtInfoContainer.add(txtCurdebt);
        // Sixth line: debt date
        JLabel debtDate = WidgetsBuilder.createLabel(AppLocal.getIntString("label.curdate"));
        debtInfoContainer.add(debtDate);
        txtCurdate = new javax.swing.JTextField();
        txtCurdate.setFont(WidgetsBuilder.getFont(WidgetsBuilder.SIZE_MEDIUM));
        txtCurdate.setEditable(false);
        txtCurdate.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        debtInfoContainer.add(txtCurdate);
        // Footer message
        jlblMessage = new javax.swing.JTextArea();
        jlblMessage.setFont(WidgetsBuilder.getFont(WidgetsBuilder.SIZE_MEDIUM));
        jlblMessage.setBackground(javax.swing.UIManager.getDefaults().getColor("Label.background"));
        jlblMessage.setEditable(false);
        jlblMessage.setLineWrap(true);
        jlblMessage.setWrapStyleWord(true);
        jlblMessage.setFocusable(false);
        jlblMessage.setRequestFocusEnabled(false);
        // Add all these stuff to container
        paymentInfoContainer.add(debtInfoContainer);
        paymentInfoContainer.add(jlblMessage);
        
        // Add all to main container
        add(paymentInfoContainer, java.awt.BorderLayout.CENTER);
        add(inputContainer, java.awt.BorderLayout.LINE_END);

    }

    private javax.swing.JTextArea jlblMessage;
    private JEditorKeys m_jKeys;
    private JLabel m_jMoneyEuros;
    private javax.swing.JTextField m_jName;
    private JTextArea m_jNotes;
    private JEditorCurrencyPositive m_jTendered;
    private javax.swing.JTextField txtCurdate;
    private javax.swing.JTextField txtCurdebt;
    private javax.swing.JTextField txtMaxdebt;
    
}
