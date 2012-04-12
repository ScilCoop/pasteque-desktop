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

import com.openbravo.pos.customers.CustomerInfoExt;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JLabel;
import javax.swing.JPanel;
import com.openbravo.pos.forms.AppLocal;
import com.openbravo.format.Formats;
import com.openbravo.pos.util.RoundUtils;
import com.openbravo.pos.widgets.JEditorCurrencyPositive;
import com.openbravo.pos.widgets.JEditorKeys;
import com.openbravo.pos.widgets.WidgetsBuilder;

public class JPaymentFree extends javax.swing.JPanel implements JPaymentInterface {
    
    private double m_dPaid;
    private double m_dTotal;
    private JPaymentNotifier m_notifier;
    
    /** Creates new form JPaymentFree */
    public JPaymentFree(JPaymentNotifier notifier) {
        m_notifier = notifier;
        initComponents();
        
        m_jTendered.addPropertyChangeListener("Edition", new RecalculateState());
        m_jTendered.addEditorKeys(m_jKeys);
    }
    public void activate(CustomerInfoExt customerext, double dTotal, String transID) {
        
        m_dTotal = dTotal;
        
        // m_jTotal.setText(Formats.CURRENCY.formatValue(new Double(m_dTotal)));
        m_jTendered.reset();
        m_jTendered.activate();
        
        printState();
    }
    
    public PaymentInfo executePayment() {
        return new PaymentInfoFree(m_dPaid);
    }
    public Component getComponent() {
        return this;
    }
    
    private void printState() {
        
        Double value = m_jTendered.getDoubleValue();
        if (value == null) {
            m_dPaid = m_dTotal;
        } else {
            m_dPaid = value;
        } 

        m_jMoneyEuros.setText(Formats.CURRENCY.formatValue(new Double(m_dPaid)));
        
        int iCompare = RoundUtils.compare(m_dPaid, m_dTotal);
        
        // if iCompare > 0 then the payment is not valid
        m_notifier.setStatus(m_dPaid > 0.0 && iCompare <= 0, iCompare == 0);
    }
    
    private class RecalculateState implements PropertyChangeListener {
        public void propertyChange(PropertyChangeEvent evt) {
            printState();
        }
    }
    
    private void initComponents() {
        setLayout(new java.awt.BorderLayout());

        // Setup right part (inputContainer): keyboard
        inputContainer = new javax.swing.JPanel();
        m_jKeys = new com.openbravo.pos.widgets.JEditorKeys();
        m_jTendered = new JEditorCurrencyPositive();
        jPanel1 = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        
        jPanel1.setLayout(new javax.swing.BoxLayout(jPanel1, javax.swing.BoxLayout.Y_AXIS));
        jPanel1.add(m_jKeys);

        jPanel3.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        jPanel3.setLayout(new java.awt.BorderLayout());
        jPanel3.add(m_jTendered, java.awt.BorderLayout.CENTER);

        jPanel1.add(jPanel3);
        
        inputContainer.setLayout(new java.awt.BorderLayout());
        inputContainer.add(jPanel1, java.awt.BorderLayout.NORTH);

        // Setup left part (paymentInfoContainer): payment info (amount)
        paymentInfoContainer = new javax.swing.JPanel();
        givenLabel = WidgetsBuilder.createLabel(AppLocal.getIntString("Label.InputCash"));
        m_jMoneyEuros = WidgetsBuilder.createLabel();
        
        paymentInfoContainer.setLayout(new BorderLayout());

        JPanel changeContainer = new JPanel();
        changeContainer.setLayout(new GridLayout(1, 2, 8 ,8));
        givenLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        changeContainer.add(givenLabel);
        m_jMoneyEuros.setBackground(new java.awt.Color(153, 153, 255));
        m_jMoneyEuros.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        m_jMoneyEuros.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createLineBorder(javax.swing.UIManager.getDefaults().getColor("Button.darkShadow")), javax.swing.BorderFactory.createEmptyBorder(1, 4, 1, 4)));
        m_jMoneyEuros.setOpaque(true);
        changeContainer.add(m_jMoneyEuros);
        paymentInfoContainer.add(changeContainer, BorderLayout.NORTH);
        
        // Add all to main container
        add(paymentInfoContainer, java.awt.BorderLayout.CENTER);
        add(inputContainer, java.awt.BorderLayout.LINE_END);
    }
    
    private JLabel givenLabel;
    private JPanel jPanel1;
    private JPanel inputContainer;
    private JPanel jPanel3;
    private JPanel paymentInfoContainer;
    private JEditorKeys m_jKeys;
    private JLabel m_jMoneyEuros;
    private JEditorCurrencyPositive m_jTendered;
}
