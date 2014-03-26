//    Openbravo POS is a point of sales application designed for touch screens.
//    Copyright (C) 2007-2009 Openbravo, S.L.
//    http://www.openbravo.com/product/pos
//
//    This file is part of Openbravo POS.
//
//    Openbravo POS is free software: you can redistribute it and/or modify
//    it under the terms of the GNU General Public License as published by
//    the Free Software Foundation, either version 3 of the License, or
//    (at your option) any later version.
//
//    Openbravo POS is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//    GNU General Public License for more details.
//
//    You should have received a copy of the GNU General Public License
//    along with Openbravo POS.  If not, see <http://www.gnu.org/licenses/>.

package fr.pasteque.pos.customers;

import fr.pasteque.basic.BasicException;
import fr.pasteque.data.gui.MessageInf;
import fr.pasteque.data.loader.ImageLoader;
import fr.pasteque.data.user.DirtyManager;
import fr.pasteque.format.Formats;
import fr.pasteque.pos.forms.AppLocal;
import fr.pasteque.pos.forms.AppView;
import fr.pasteque.pos.forms.BeanFactoryApp;
import fr.pasteque.pos.forms.BeanFactoryException;
import fr.pasteque.pos.forms.DataLogicSales;
import fr.pasteque.pos.forms.DataLogicSystem;
import fr.pasteque.pos.forms.JPanelView;
import fr.pasteque.pos.payment.JPaymentSelect;
import fr.pasteque.pos.payment.JPaymentSelectCustomer;
import fr.pasteque.pos.payment.PaymentInfo;
import fr.pasteque.pos.payment.PaymentInfoTicket;
import fr.pasteque.pos.printer.TicketParser;
import fr.pasteque.pos.printer.TicketPrinterException;
import fr.pasteque.pos.scripting.ScriptEngine;
import fr.pasteque.pos.scripting.ScriptException;
import fr.pasteque.pos.scripting.ScriptFactory;
import fr.pasteque.pos.ticket.TicketInfo;
import fr.pasteque.pos.widgets.WidgetsBuilder;
import java.util.Date;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JOptionPane;

/**
 *
 * @author  adrianromero
 */
public class CustomersPayment extends javax.swing.JPanel implements JPanelView, BeanFactoryApp {

    private AppView app;
    private DataLogicCustomers dlcustomers;
    private DataLogicSales dlsales;
    private DataLogicSystem dlsystem;
    private TicketParser ttp;    
    private JPaymentSelect paymentdialog;
    
    private CustomerInfoExt customerext;
    private DirtyManager dirty;

    /** Creates new form CustomersPayment */
    public CustomersPayment() {

        initComponents();
        
        editorcard.addEditorKeys(m_jKeys);
        txtNotes.addEditorKeys(m_jKeys);

        dirty = new DirtyManager();
        txtNotes.addPropertyChangeListener("Text", dirty);
    }

    public void init(AppView app) throws BeanFactoryException {

        this.app = app;
        dlcustomers = new DataLogicCustomers();
        dlsales = new DataLogicSales();
        dlsystem = new DataLogicSystem();
        ttp = new TicketParser(app.getDeviceTicket(), dlsystem);
    }

    public Object getBean() {
        return this;
    }

    public String getTitle() {
        return AppLocal.getIntString("Menu.CustomersPayment");
    }

    public void activate() throws BasicException {

        paymentdialog = JPaymentSelectCustomer.getDialog(this);        
        paymentdialog.init(app);

        resetCustomer();

        editorcard.reset();
        editorcard.activate();
    }

    public boolean deactivate() {
        if (dirty.isDirty()) {
            int res = JOptionPane.showConfirmDialog(this,
                    AppLocal.getIntString("message.wannasave"),
                    AppLocal.getIntString("title.editor"),
                    JOptionPane.YES_NO_CANCEL_OPTION,
                    JOptionPane.QUESTION_MESSAGE);
            if (res == JOptionPane.YES_OPTION) {
                save();
                return true;
            } else {
                return res == JOptionPane.NO_OPTION;
            }
        } else {
            return true;
        }
    }

    public JComponent getComponent() {
        return this;
    }

    private void editCustomer(CustomerInfoExt customer) {

        customerext = customer;

        txtTaxId.setText(customer.getTaxid());
        txtName.setText(customer.getName());
        txtCard.setText(customer.getCard());
        txtNotes.reset();
        txtNotes.setText(customer.getNotes());
        txtMaxdebt.setText(Formats.CURRENCY.formatValue(customer.getMaxdebt()));
        txtCurdebt.setText(Formats.CURRENCY.formatValue(customer.getCurdebt()));
        txtCurdate.setText(Formats.DATE.formatValue(customer.getCurdate()));

        txtNotes.setEnabled(true);

        dirty.setDirty(false);

        btnSave.setEnabled(true);    
        btnPay.setEnabled(customer.getCurdebt() != null && customer.getCurdebt().doubleValue() > 0.0);
    }

    private void resetCustomer() {

        customerext = null;

        txtTaxId.setText(null);
        txtName.setText(null);
        txtCard.setText(null);
        txtNotes.reset();
        txtMaxdebt.setText(null);
        txtCurdebt.setText(null);
        txtCurdate.setText(null);

        txtNotes.setEnabled(false);

        dirty.setDirty(false);

        btnSave.setEnabled(false);
        btnPay.setEnabled(false);

    }

    private void readCustomer() {

        try {
            CustomerInfoExt customer = dlcustomers.getCustomerByCard(editorcard.getText());
            if (customer == null) {
                MessageInf msg = new MessageInf(MessageInf.SGN_WARNING, AppLocal.getIntString("message.cannotfindcustomer"));
                msg.show(this);
            } else {
                editCustomer(customer);
            }

        } catch (BasicException ex) {
            MessageInf msg = new MessageInf(MessageInf.SGN_WARNING, AppLocal.getIntString("message.cannotfindcustomer"), ex);
            msg.show(this);
        }

        editorcard.reset();
        editorcard.activate();
    }

    private void save() {

        customerext.setNotes(txtNotes.getText());

        try {
            dlcustomers.updateCustomerExt(customerext);
            editCustomer(customerext);
        } catch (BasicException e) {
            MessageInf msg = new MessageInf(MessageInf.SGN_NOTICE, AppLocal.getIntString("message.nosave"), e);
            msg.show(this);
        }

    }

    private void printTicket(String resname, TicketInfo ticket, CustomerInfoExt customer) {

        String resource = dlsystem.getResourceAsXML(resname);
        if (resource == null) {
            MessageInf msg = new MessageInf(MessageInf.SGN_WARNING, AppLocal.getIntString("message.cannotprintticket"));
            msg.show(this);
        } else {
            try {
                ScriptEngine script = ScriptFactory.getScriptEngine(ScriptFactory.VELOCITY);
                script.put("ticket", ticket);
                script.put("customer", customer);
                ttp.printTicket(script.eval(resource).toString());
            } catch (ScriptException e) {
                MessageInf msg = new MessageInf(MessageInf.SGN_WARNING,
                        AppLocal.getIntString("message.cannotprintticket"), e);
                msg.show(this);
            } catch (TicketPrinterException e) {
                MessageInf msg = new MessageInf(MessageInf.SGN_WARNING,
                        AppLocal.getIntString("message.cannotprintticket"), e);
                msg.show(this);
            }
        }
    }
    
    public boolean requiresOpenedCash() {
        return true;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jPanel2 = new javax.swing.JPanel();
        jPanel6 = new javax.swing.JPanel();
        btnCustomer = WidgetsBuilder.createButton(ImageLoader.readImageIcon("tkt_assign_customer.png"),
                AppLocal.getIntString("Button.m_jList.toolTip"));
        btnSave = WidgetsBuilder.createButton(ImageLoader.readImageIcon("save.png"),
                AppLocal.getIntString("Button.save.toolTip"));
        jSeparator1 = new javax.swing.JSeparator();
        btnPay = WidgetsBuilder.createButton(ImageLoader.readImageIcon("button_generic.png"),
                AppLocal.getIntString("button.pay"),
                WidgetsBuilder.SIZE_MEDIUM);
        jPanel3 = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        m_jKeys = new fr.pasteque.pos.widgets.JEditorKeys();
        jPanel5 = new javax.swing.JPanel();
        editorcard = new fr.pasteque.pos.widgets.JEditorString();
        jButton1 = WidgetsBuilder.createButton(ImageLoader.readImageIcon("button_ok.png"),
                AppLocal.getIntString("Button.validate.toolTip"));
        jPanel1 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        txtCard = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        txtCurdebt = new javax.swing.JTextField();
        txtCurdate = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        txtName = new javax.swing.JTextField();
        txtMaxdebt = new javax.swing.JTextField();
        txtNotes = new fr.pasteque.pos.widgets.JEditorString();
        txtTaxId = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();

        setLayout(new java.awt.BorderLayout());

        jPanel2.setLayout(new java.awt.BorderLayout());

        btnCustomer.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCustomerActionPerformed(evt);
            }
        });
        jPanel6.add(btnCustomer);

        btnSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSaveActionPerformed(evt);
            }
        });
        jPanel6.add(btnSave);
        jPanel6.add(jSeparator1);

        btnPay.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnPayActionPerformed(evt);
            }
        });
        jPanel6.add(btnPay);

        jPanel2.add(jPanel6, java.awt.BorderLayout.LINE_START);

        add(jPanel2, java.awt.BorderLayout.PAGE_START);

        jPanel3.setLayout(new java.awt.BorderLayout());

        jPanel4.setLayout(new javax.swing.BoxLayout(jPanel4, javax.swing.BoxLayout.Y_AXIS));

        m_jKeys.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_jKeysActionPerformed(evt);
            }
        });
        jPanel4.add(m_jKeys);

        jPanel5.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        jPanel5.setLayout(new java.awt.GridBagLayout());
        jPanel5.add(editorcard, new java.awt.GridBagConstraints());

        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        jPanel5.add(jButton1, gridBagConstraints);

        jPanel4.add(jPanel5);

        jPanel3.add(jPanel4, java.awt.BorderLayout.NORTH);

        add(jPanel3, java.awt.BorderLayout.LINE_END);

        jLabel3.setText(AppLocal.getIntString("label.name")); // NOI18N

        jLabel12.setText(AppLocal.getIntString("label.notes")); // NOI18N

        jLabel5.setText(AppLocal.getIntString("label.card")); // NOI18N

        txtCard.setEditable(false);
        txtCard.setFocusable(false);
        txtCard.setRequestFocusEnabled(false);

        jLabel1.setText(AppLocal.getIntString("label.maxdebt")); // NOI18N

        jLabel2.setText(AppLocal.getIntString("label.curdebt")); // NOI18N

        txtCurdebt.setEditable(false);
        txtCurdebt.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        txtCurdebt.setFocusable(false);
        txtCurdebt.setRequestFocusEnabled(false);

        txtCurdate.setEditable(false);
        txtCurdate.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        txtCurdate.setFocusable(false);
        txtCurdate.setRequestFocusEnabled(false);

        jLabel6.setText(AppLocal.getIntString("label.curdate")); // NOI18N

        txtName.setEditable(false);
        txtName.setFocusable(false);
        txtName.setRequestFocusEnabled(false);

        txtMaxdebt.setEditable(false);
        txtMaxdebt.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        txtMaxdebt.setFocusable(false);
        txtMaxdebt.setRequestFocusEnabled(false);

        txtTaxId.setEditable(false);
        txtTaxId.setFocusable(false);
        txtTaxId.setRequestFocusEnabled(false);

        jLabel7.setText(AppLocal.getIntString("label.taxid")); // NOI18N

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtTaxId, javax.swing.GroupLayout.PREFERRED_SIZE, 240, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtName, javax.swing.GroupLayout.PREFERRED_SIZE, 240, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtCard, javax.swing.GroupLayout.PREFERRED_SIZE, 240, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel12, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtNotes, javax.swing.GroupLayout.PREFERRED_SIZE, 270, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtMaxdebt, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtCurdebt, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtCurdate, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(97, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(txtTaxId, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(txtName, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(txtCard, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel12)
                    .addComponent(txtNotes, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(txtMaxdebt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(txtCurdebt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(txtCurdate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(89, Short.MAX_VALUE))
        );

        add(jPanel1, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {
        readCustomer();
    }

    private void m_jKeysActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_jKeysActionPerformed

        readCustomer();
        
    }//GEN-LAST:event_m_jKeysActionPerformed

    private void btnCustomerActionPerformed(java.awt.event.ActionEvent evt) {
        JCustomerFinder finder = JCustomerFinder.getCustomerFinder(this, dlcustomers);
        finder.search(null);
        finder.setVisible(true);
        CustomerInfoExt customer = finder.getSelectedCustomer();
        if (customer != null) {
            editCustomer(customer);
        }
        editorcard.reset();
        editorcard.activate();
    }

    private void btnPayActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPayActionPerformed

        paymentdialog.setPrintSelected(true);
        
        if (paymentdialog.showDialog(customerext.getCurdebt(), null)) {

            // Save the ticket
            TicketInfo ticket = new TicketInfo();
            ticket.setTicketType(TicketInfo.RECEIPT_PAYMENT);

            // Get all payments and add the reverse operation in debtpaid
            // for a neutral result
            List<PaymentInfo> payments = paymentdialog.getSelectedPayments();
            double total = 0.0;
            for (PaymentInfo p : payments) {
                total += p.getTotal();
            }
            payments.add(new PaymentInfoTicket(-total, null, "debtpaid"));
            ticket.setPayments(payments);

            ticket.setUser(app.getAppUserView().getUser().getUserInfo());
            ticket.setActiveCash(app.getActiveCashIndex());
            ticket.setTicketId(app.getCashRegister().getNextTicketId());
            ticket.setDate(new Date());
            ticket.setCustomer(customerext);

            try {
                dlsales.saveTicket(ticket, app.getInventoryLocation(),
                        app.getActiveCashSession().getId());
            } catch (BasicException eData) {
                MessageInf msg = new MessageInf(MessageInf.SGN_NOTICE, AppLocal.getIntString("message.nosaveticket"), eData);
                msg.show(this);
            }

            // reload customer
            editCustomer(customerext);

            printTicket(paymentdialog.isPrintSelected()
                    ? "Printer.CustomerPaid"
                    : "Printer.CustomerPaid2",
                    ticket, customerext);

            app.getCashRegister().incrementNextTicketId();
        }
        editorcard.reset();
        editorcard.activate();
    }

    private void btnSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSaveActionPerformed

        if (dirty.isDirty()) {
            save();

            editorcard.reset();
            editorcard.activate();
        }
        
}//GEN-LAST:event_btnSaveActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCustomer;
    private javax.swing.JButton btnPay;
    private javax.swing.JButton btnSave;
    private fr.pasteque.pos.widgets.JEditorString editorcard;
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JSeparator jSeparator1;
    private fr.pasteque.pos.widgets.JEditorKeys m_jKeys;
    private javax.swing.JTextField txtCard;
    private javax.swing.JTextField txtCurdate;
    private javax.swing.JTextField txtCurdebt;
    private javax.swing.JTextField txtMaxdebt;
    private javax.swing.JTextField txtName;
    private fr.pasteque.pos.widgets.JEditorString txtNotes;
    private javax.swing.JTextField txtTaxId;
    // End of variables declaration//GEN-END:variables
}
