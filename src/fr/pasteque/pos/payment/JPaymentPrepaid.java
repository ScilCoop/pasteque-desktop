//    POS-Tech
//    Based upon Openbravo POS
//
//    Copyright (C) 2007-2009 Openbravo, S.L.
//                       2012 Scil (http://scil.coop)
//                       2015 Scil (http://scil.coop)
//    Cédric Houbart, Philippe Pary
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

import java.awt.Panel;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import fr.pasteque.format.Formats;
import fr.pasteque.pos.admin.CurrencyInfo;
import fr.pasteque.pos.forms.AppLocal;
import fr.pasteque.pos.customers.CustomerInfoExt;
import fr.pasteque.pos.util.RoundUtils;
import fr.pasteque.pos.widgets.JEditorCurrencyPositive;
import fr.pasteque.pos.widgets.JEditorKeys;
import fr.pasteque.pos.widgets.WidgetsBuilder;

/**
 *
 * @author  adrianromero
 */
public class JPaymentPrepaid extends javax.swing.JPanel implements JPaymentInterface {

    private JPaymentNotifier notifier;
    private CustomerInfoExt customerext;

    private double m_dPaid;
    private double partAmount;
    private double m_dTotal;
    private CurrencyInfo currency;

    /** Creates new form JPaymentDebt */
    public JPaymentPrepaid(JPaymentNotifier notifier) {

        this.notifier = notifier;

        initComponents();

        m_jTendered.addPropertyChangeListener("Edition", new RecalculateState());
        m_jTendered.addEditorKeys(m_jKeys);

    }

    public void activate(CustomerInfoExt customerext, double dTotal,
            double partAmount, CurrencyInfo currency, String transID) {

        this.customerext = customerext;
        m_dTotal = dTotal;
        this.partAmount = partAmount;
        this.currency = currency;

        m_jTendered.reset();

        if (customerext == null) {
            m_jName.setText(null);
            txtCurdebt.setText(null);
            m_jKeys.setEnabled(false);
            m_jTendered.setEnabled(false);
        } else {
            m_jName.setText(customerext.getName());
            txtCurdebt.setText(Formats.CURRENCY.formatValue(RoundUtils.getValue(customerext.getPrepaid())));

            m_jKeys.setEnabled(true);
            m_jTendered.setEnabled(true);
            m_jTendered.activate();
        }

        printState();

    }
    public PaymentInfo executePayment() {
        return new PaymentInfoTicket(m_dPaid, this.currency, "prepaid");
    }
    public JPanel getPanel() {
        return this;
    }

    public JPanel getComponent() {
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
                m_dPaid = this.partAmount;
            } else {
                m_dPaid = value;
            }
            Formats.setAltCurrency(this.currency);
            m_jMoneyEuros.setText(Formats.CURRENCY.formatValue(new Double(m_dPaid)));
            if (RoundUtils.getValue(customerext.getPrepaid()) - m_dPaid < -0.005)  {
                // not enough prepaid
                jlblMessage.setText(AppLocal.getIntString("Message.CustomerNotEnoughPrepaid"));
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
        m_jKeys = new fr.pasteque.pos.widgets.JEditorKeys();
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
        JLabel debtLabel = WidgetsBuilder.createLabel(AppLocal.getIntString("Label.InputCash"));
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
        m_jName.setFocusable(false);
        debtInfoContainer.add(m_jName);
        // Fifth line: current debt
        JLabel currentDebt = WidgetsBuilder.createLabel(AppLocal.getIntString("Label.CurPrepaid"));
        debtInfoContainer.add(currentDebt);
        txtCurdebt = new javax.swing.JTextField();
        txtCurdebt.setFont(WidgetsBuilder.getFont(WidgetsBuilder.SIZE_MEDIUM));
        txtCurdebt.setEditable(false);
        txtCurdebt.setFocusable(false);
        txtCurdebt.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        debtInfoContainer.add(txtCurdebt);
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
    private javax.swing.JTextField txtCurdebt;

}
