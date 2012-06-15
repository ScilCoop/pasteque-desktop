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
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import com.openbravo.data.gui.MessageInf;
import com.openbravo.pos.forms.AppConfig;
import com.openbravo.pos.forms.AppLocal;
import com.openbravo.format.Formats;
import com.openbravo.pos.customers.CustomerInfoExt;
import com.openbravo.pos.forms.DataLogicSystem;
import com.openbravo.pos.scripting.ScriptEngine;
import com.openbravo.pos.scripting.ScriptException;
import com.openbravo.pos.scripting.ScriptFactory;
import com.openbravo.pos.util.RoundUtils;
import com.openbravo.pos.util.ThumbNailBuilder;
import com.openbravo.pos.widgets.JEditorCurrencyPositive;
import com.openbravo.pos.widgets.JEditorKeys;
import com.openbravo.pos.widgets.WidgetsBuilder;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.SwingConstants;

/**
 *
 * @author adrianromero
 */
public class JPaymentCashPos extends javax.swing.JPanel implements JPaymentInterface {
    
    private JPaymentNotifier m_notifier;

    private double m_dPaid;
    private double m_dTotal;
    private double partAmount;
    
    /** Creates new form JPaymentCash */
    public JPaymentCashPos(JPaymentNotifier notifier, DataLogicSystem dlSystem) {
        
        m_notifier = notifier;
        
        initComponents();  
        
        m_jTendered.addPropertyChangeListener("Edition", new RecalculateState());
        m_jTendered.addEditorKeys(m_jKeys);
        
        String code = dlSystem.getResourceAsXML("payment.cash");
        if (code != null) {
            try {
                ScriptEngine script = ScriptFactory.getScriptEngine(ScriptFactory.BEANSHELL);
                script.put("payment", new ScriptPaymentCash(dlSystem));    
                script.eval(code);
            } catch (ScriptException e) {
                MessageInf msg = new MessageInf(MessageInf.SGN_NOTICE, AppLocal.getIntString("message.cannotexecute"), e);
                msg.show(this);
            }
        }
        
    }
    
    public void activate(CustomerInfoExt customerext, double dTotal, double partAmount, String transID) {
        
        m_dTotal = dTotal;
        this.partAmount = partAmount;
        
        m_jTendered.reset();
        m_jTendered.activate();
        
        printState();        
    }
    public PaymentInfo executePayment() {
        if (m_dPaid - m_dTotal >= 0.0) {
            // pago completo
            return new PaymentInfoCash(m_dTotal, m_dPaid);
        } else {
            // pago parcial
            return new PaymentInfoCash(m_dPaid, m_dPaid);
        }        
    }
    public Component getComponent() {
        return this;
    }
    
    private void printState() {

        Double value = m_jTendered.getDoubleValue();
        if (value == null || value == 0.0) {
            m_dPaid = this.partAmount;
        } else {            
            m_dPaid = value;
        }   

        int iCompare = RoundUtils.compare(m_dPaid, this.m_dTotal);
        
        m_jMoneyEuros.setText(Formats.CURRENCY.formatValue(new Double(m_dPaid)));
        m_jChangeEuros.setText(iCompare > 0 
                ? Formats.CURRENCY.formatValue(new Double(m_dPaid - this.partAmount))
                : null); 
        
        m_notifier.setStatus(m_dPaid > 0.0, iCompare >= 0);
    }
    
    private class RecalculateState implements PropertyChangeListener {
        public void propertyChange(PropertyChangeEvent evt) {
            printState();
        }
    }    
    
    public class ScriptPaymentCash {
        
        private DataLogicSystem dlSystem;
        private ThumbNailBuilder tnbbutton;
        
        public ScriptPaymentCash(DataLogicSystem dlSystem) {
            this.dlSystem = dlSystem;
            tnbbutton = new ThumbNailBuilder(64, 54, "com/openbravo/images/cash.png");
        }
        
        public void addButton(String image, double amount) {
            JButton btn = WidgetsBuilder.createButton(new ImageIcon(tnbbutton.getThumbNailText(dlSystem.getResourceAsImage(image), Formats.CURRENCY.formatValue(amount))));
            btn.setFocusPainted(false);
            btn.setFocusable(false);
            btn.setRequestFocusEnabled(false);
            btn.setHorizontalTextPosition(SwingConstants.CENTER);
            btn.setVerticalTextPosition(SwingConstants.BOTTOM);
            btn.addActionListener(new AddAmount(amount));
            moneyBtnsContainer.add(btn);  
        }
    }
    
    private class AddAmount implements ActionListener {        
        private double amount;
        public AddAmount(double amount) {
            this.amount = amount;
        }
        public void actionPerformed(ActionEvent e) {
            Double tendered = m_jTendered.getDoubleValue();
            if (tendered == null) {
                m_jTendered.setDoubleValue(amount);
            } else {
                m_jTendered.setDoubleValue(tendered + amount);
            }

            printState();
        }
    }
    
    private void initComponents() {
        AppConfig cfg = AppConfig.loadedInstance;
        int btnSpacing = WidgetsBuilder.pixelSize(Float.parseFloat(cfg.getProperty("ui.touchbtnspacing")));
        
        jPanel5 = new javax.swing.JPanel();
        changeContainer = new javax.swing.JPanel();
        m_jChangeEuros = WidgetsBuilder.createLabel();
        changeLabel = WidgetsBuilder.createLabel(AppLocal.getIntString("Label.ChangeCash"));
        givenLabel = WidgetsBuilder.createLabel(AppLocal.getIntString("Label.InputCash"));
        m_jMoneyEuros = WidgetsBuilder.createLabel();
        moneyBtnsContainer = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        m_jKeys = new JEditorKeys();
        jPanel3 = new javax.swing.JPanel();
        m_jTendered = new JEditorCurrencyPositive();

        setLayout(new java.awt.BorderLayout());

        jPanel5.setLayout(new java.awt.BorderLayout());

        changeContainer.setLayout(new GridLayout(2, 2, 8, 8));
        
        
        changeLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        changeContainer.add(changeLabel);
        m_jChangeEuros.setBackground(java.awt.Color.white);
        m_jChangeEuros.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        m_jChangeEuros.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createLineBorder(javax.swing.UIManager.getDefaults().getColor("Button.darkShadow")), javax.swing.BorderFactory.createEmptyBorder(1, 4, 1, 4)));
        m_jChangeEuros.setOpaque(true);
        changeContainer.add(m_jChangeEuros);

        givenLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        changeContainer.add(givenLabel);
        m_jMoneyEuros.setBackground(new java.awt.Color(153, 153, 255));
        m_jMoneyEuros.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        m_jMoneyEuros.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createLineBorder(javax.swing.UIManager.getDefaults().getColor("Button.darkShadow")), javax.swing.BorderFactory.createEmptyBorder(1, 4, 1, 4)));
        m_jMoneyEuros.setOpaque(true);
        changeContainer.add(m_jMoneyEuros);

        jPanel5.add(changeContainer, java.awt.BorderLayout.NORTH);

        moneyBtnsContainer.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, btnSpacing, btnSpacing));
        jPanel5.add(moneyBtnsContainer, java.awt.BorderLayout.CENTER);

        add(jPanel5, java.awt.BorderLayout.CENTER);

        jPanel2.setLayout(new java.awt.BorderLayout());

        jPanel1.setLayout(new javax.swing.BoxLayout(jPanel1, javax.swing.BoxLayout.Y_AXIS));
        jPanel1.add(m_jKeys);

        jPanel3.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        jPanel3.setLayout(new java.awt.BorderLayout());
        jPanel3.add(m_jTendered, java.awt.BorderLayout.CENTER);

        jPanel1.add(jPanel3);

        jPanel2.add(jPanel1, java.awt.BorderLayout.NORTH);

        add(jPanel2, java.awt.BorderLayout.LINE_END);
    }
    
    private javax.swing.JLabel changeLabel;
    private javax.swing.JLabel givenLabel;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel changeContainer;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel moneyBtnsContainer;
    private javax.swing.JLabel m_jChangeEuros;
    private JEditorKeys m_jKeys;
    private javax.swing.JLabel m_jMoneyEuros;
    private JEditorCurrencyPositive m_jTendered;
    
}
