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

package fr.pasteque.pos.payment;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import fr.pasteque.data.gui.MessageInf;
import fr.pasteque.pos.forms.AppConfig;
import fr.pasteque.pos.forms.AppLocal;
import fr.pasteque.format.Formats;
import fr.pasteque.pos.admin.CurrencyInfo;
import fr.pasteque.pos.customers.CustomerInfoExt;
import fr.pasteque.pos.forms.DataLogicSystem;
import fr.pasteque.pos.scripting.ScriptEngine;
import fr.pasteque.pos.scripting.ScriptException;
import fr.pasteque.pos.scripting.ScriptFactory;
import fr.pasteque.pos.util.RoundUtils;
import fr.pasteque.pos.util.ThumbNailBuilder;
import fr.pasteque.pos.widgets.JEditorCurrencyPositive;
import fr.pasteque.pos.widgets.JEditorKeys;
import fr.pasteque.pos.widgets.WidgetsBuilder;
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
    private CurrencyInfo currency;

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

    public void activate(CustomerInfoExt customerext, double dTotal,
            double partAmount, CurrencyInfo currency, String transID) {

        m_dTotal = dTotal;
        this.partAmount = partAmount;
        this.currency = currency;

        m_jTendered.reset();
        m_jTendered.activate();

        printState();
    }
    public PaymentInfo executePayment() {
        if (this.m_dPaid - this.partAmount >= 0.0) {
            // Full part payment
            return new PaymentInfoCash(this.partAmount, this.m_dPaid,
                    this.currency);
        } else {
            // Partial part amount
            return new PaymentInfoCash(m_dPaid, m_dPaid,
                    this.currency);
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

        int iCompare = RoundUtils.compare(m_dPaid, this.partAmount);
        boolean fullPayment = RoundUtils.compare(m_dPaid, this.m_dTotal) >= 0
                              && this.m_dTotal - this.partAmount < 0.005;
        Formats.setAltCurrency(this.currency);
        m_jMoneyEuros.setText(Formats.CURRENCY.formatValue(new Double(m_dPaid)));
        // Set change in main currency
        if (iCompare > 0) {
            double change = m_dPaid - this.partAmount;
            if (!this.currency.isMain()) {
                change /= this.currency.getRate();
            }
            m_jChangeEuros.setText(Formats.CURRENCY.formatValue(change));
        } else {
            m_jChangeEuros.setText(null);
        }
        m_notifier.setStatus(m_dPaid > 0.0, fullPayment);
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
            tnbbutton = new ThumbNailBuilder(64, 54, "fr.pasteque.images/cash.png");
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
        m_jChangeEuros.setForeground(java.awt.Color.darkGray);
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
