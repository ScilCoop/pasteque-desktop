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

package fr.pasteque.pos.panels;

import fr.pasteque.basic.BasicException;
import fr.pasteque.data.loader.IKeyed;
import fr.pasteque.data.loader.ImageLoader;
import fr.pasteque.data.gui.ComboBoxValModel;
import fr.pasteque.pos.admin.CurrencyInfo;
import fr.pasteque.pos.forms.AppConfig;
import fr.pasteque.pos.forms.AppLocal;
import fr.pasteque.pos.forms.AppView;
import fr.pasteque.pos.forms.BeanFactoryApp;
import fr.pasteque.pos.forms.BeanFactoryException;
import fr.pasteque.pos.forms.DataLogicSales;
import fr.pasteque.pos.forms.JPanelView;
import fr.pasteque.pos.ticket.CashMove;
import fr.pasteque.pos.widgets.WidgetsBuilder;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Date;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 *
 * @author adrianromero
 */
public class JPanelPayments extends JPanel implements JPanelView, BeanFactoryApp {
    private AppView app;
    private DataLogicSales m_dlSales = null;
    private ComboBoxValModel reasonModel;

    /** Creates a new instance of JPanelPayments */
    public JPanelPayments() {
        this.initComponents();

        this.reasonModel = new ComboBoxValModel();
        this.reasonModel.add(new PaymentReasonPositive("cashin",
                        AppLocal.getIntString("transpayment.cashin")));
        this.reasonModel.add(new PaymentReasonNegative("cashout",
                        AppLocal.getIntString("transpayment.cashout")));
        this.reason.setModel(this.reasonModel);
        this.total.addEditorKeys(this.keypad);
        this.notes.addEditorKeys(this.keypad);
    }

    public void init(AppView app) throws BeanFactoryException {
        this.app = app;
    }

    public String getTitle() {
        return AppLocal.getIntString("Menu.Payments");
    }

    public Object getBean() {
        return this;
    }
    public JComponent getComponent() {
        return this;
    }

    public void activate() {
        this.reasonModel.setSelectedKey("cashin");
    }

    public boolean deactivate() {
        return true;
    }

    public boolean requiresOpenedCash() {
        return true;
    }

    private static abstract class PaymentReason implements IKeyed {
        private String m_sKey;
        private String m_sText;

        public PaymentReason(String key, String text) {
            m_sKey = key;
            m_sText = text;
        }
        public Object getKey() {
            return m_sKey;
        }
        public abstract Double positivize(Double d);
        public abstract Double addSignum(Double d);

        @Override
        public String toString() {
            return m_sText;
        }
    }
    private static class PaymentReasonPositive extends PaymentReason {
        public PaymentReasonPositive(String key, String text) {
            super(key, text);
        }
        public Double positivize(Double d) {
            return d;
        }
        public Double addSignum(Double d) {
            if (d == null) {
                return null;
            } else if (d.doubleValue() < 0.0) {
                return new Double(-d.doubleValue());
            } else {
                return d;
            }
        }
    }
    private static class PaymentReasonNegative extends PaymentReason {
        public PaymentReasonNegative(String key, String text) {
            super(key, text);
        }
        public Double positivize(Double d) {
            return d == null ? null : new Double(-d.doubleValue());
        }
        public Double addSignum(Double d) {
            if (d == null) {
                return null;
            } else if (d.doubleValue() > 0.0) {
                return new Double(-d.doubleValue());
            } else {
                return d;
            }
        }
    }

    private void initComponents() {
        AppConfig cfg = AppConfig.loadedInstance;
        int btnSpacing = WidgetsBuilder.pixelSize(Float.parseFloat(cfg.getProperty("ui.touchbtnspacing")));

        this.setLayout(new GridBagLayout());
        GridBagConstraints c = null;
        JLabel reasonLbl = WidgetsBuilder.createLabel(AppLocal.getIntString("label.paymentreason"));
        this.reason = WidgetsBuilder.createComboBox();
        JLabel totalLbl = WidgetsBuilder.createLabel(AppLocal.getIntString("label.value"));
        this.total = new fr.pasteque.pos.widgets.JEditorCurrency();
        JLabel notesLbl = WidgetsBuilder.createLabel(AppLocal.getIntString("Label.Notes"));
        this.notes = new fr.pasteque.pos.widgets.JEditorString();
        this.keypad = new fr.pasteque.pos.widgets.JEditorKeys();
        JButton ok = WidgetsBuilder.createButton(ImageLoader.readImageIcon("button_ok.png"),
                AppLocal.getIntString("Button.OK"),
                WidgetsBuilder.SIZE_MEDIUM);
        ok.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okActionPerformed(evt);
            }
        });


        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.anchor = GridBagConstraints.LINE_START;
        c.insets = new Insets(btnSpacing, btnSpacing, 0, 0);
        this.add(reasonLbl, c);
        c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = 0;
        c.insets = new Insets(btnSpacing, btnSpacing, 0, btnSpacing);
        c.fill = GridBagConstraints.HORIZONTAL;
        this.add(this.reason, c);

        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 1;
        c.anchor = GridBagConstraints.LINE_START;
        c.insets = new Insets(btnSpacing, btnSpacing, 0, 0);
        this.add(totalLbl, c);
        c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = 1;
        c.insets = new Insets(btnSpacing, btnSpacing, 0, btnSpacing);
        c.fill = GridBagConstraints.HORIZONTAL;
        this.add(this.total, c);

        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 2;
        c.anchor = GridBagConstraints.LINE_START;
        c.insets = new Insets(btnSpacing, btnSpacing, 0, 0);
        this.add(notesLbl, c);
        c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = 2;
        c.insets = new Insets(btnSpacing, btnSpacing, 0, btnSpacing);
        c.fill = GridBagConstraints.HORIZONTAL;
        this.add(this.notes, c);

        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 3;
        c.gridwidth = 2;
        c.insets = new Insets(btnSpacing, btnSpacing, btnSpacing, btnSpacing);
        this.add(ok, c);

        c = new GridBagConstraints();
        c.gridx = 2;
        c.gridy = 0;
        c.gridheight = 4;
        this.add(this.keypad, c);
    }

    private void okActionPerformed(java.awt.event.ActionEvent evt) {
        String cashId = this.app.getActiveCashIndex();
        String reasonCode = (String) this.reasonModel.getSelectedKey();
        PaymentReason r = (PaymentReason) this.reasonModel.getSelectedItem();
        double amount = this.total.getDoubleValue();
        if (r != null) {
            amount = r.addSignum(amount);
        }
        String notes =  this.notes.getText();
        if (notes == null) {
            notes = "";
        }
        CashMove move = new CashMove(cashId, reasonCode, amount, notes);
        DataLogicSales dlSales = new DataLogicSales();
        try {
            if (!dlSales.saveMove(move)) {
                throw new BasicException("saveMove failed");
            }
        } catch (BasicException e) {
            // TODO display error
        }
    }


    private fr.pasteque.pos.widgets.JEditorCurrency total;
    private fr.pasteque.pos.widgets.JEditorKeys keypad;
    private javax.swing.JComboBox reason;
    private fr.pasteque.pos.widgets.JEditorText notes;

}
