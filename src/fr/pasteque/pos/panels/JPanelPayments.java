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
import fr.pasteque.data.gui.MessageInf;
import fr.pasteque.format.Formats;
import fr.pasteque.pos.admin.CurrencyInfo;
import fr.pasteque.pos.forms.AppConfig;
import fr.pasteque.pos.forms.AppLocal;
import fr.pasteque.pos.forms.AppView;
import fr.pasteque.pos.forms.BeanFactoryApp;
import fr.pasteque.pos.forms.BeanFactoryException;
import fr.pasteque.pos.forms.DataLogicSales;
import fr.pasteque.pos.forms.DataLogicSystem;
import fr.pasteque.pos.forms.JPanelView;
import fr.pasteque.pos.scripting.ScriptEngine;
import fr.pasteque.pos.scripting.ScriptException;
import fr.pasteque.pos.scripting.ScriptFactory;
import fr.pasteque.pos.ticket.CashMove;
import fr.pasteque.pos.util.ThumbNailBuilder;
import fr.pasteque.pos.widgets.CoinCountButton;
import fr.pasteque.pos.widgets.WidgetsBuilder;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 *
 * @author adrianromero
 */
public class JPanelPayments extends JPanel
    implements JPanelView, BeanFactoryApp, CoinCountButton.Listener {
    private AppView app;
    private DataLogicSales m_dlSales = null;
    private ComboBoxValModel reasonModel;
    private double amount;

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
        // Init coin buttons
        this.coinButtons = new ArrayList<CoinCountButton>();
        DataLogicSystem dlSys = new DataLogicSystem();
        String code = dlSys.getResourceAsXML("payment.cash");
        if (code != null) {
            try {
                ScriptEngine script = ScriptFactory.getScriptEngine(ScriptFactory.BEANSHELL);
                script.put("payment", new ScriptCash());
                script.eval(code);
            } catch (ScriptException e) {
                MessageInf msg = new MessageInf(MessageInf.SGN_NOTICE, AppLocal.getIntString("message.cannotexecute"), e);
                msg.show(this);
            }
        }
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
        // Open drawer
        DataLogicSystem dlSys = new DataLogicSystem();
        String code = dlSys.getResourceAsXML("Printer.OpenDrawer");
        if (code != null) {
            try {
                ScriptEngine script = ScriptFactory.getScriptEngine(ScriptFactory.VELOCITY);
                script.eval(code);
            } catch (ScriptException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean deactivate() {
        return true;
    }

    public boolean requiresOpenedCash() {
        return true;
    }

    private void reset() {
        this.reasonModel.setSelectedKey("cashin");
        this.total.setDoubleValue(null);
        this.notes.setText(null);
        this.amount = 0.0;
        for (CoinCountButton btn : this.coinButtons) {
            btn.reset();
        }
    }

    public void coinAdded(double amount, int newCount) {
        this.amount += amount;
        this.total.setDoubleValue(this.amount);
    }
    public void countUpdated() {
        this.updateAmount();
    }
    public void updateAmount() {
        this.amount = 0.0;
        for (CoinCountButton btn : this.coinButtons) {
            this.amount += btn.getAmount();
        }
        this.total.setDoubleValue(this.amount);
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

    public class ScriptCash {
        private int x, y;
        private int btnSpacing;
        private ThumbNailBuilder tnb;
        public ScriptCash() {
            AppConfig cfg = AppConfig.loadedInstance;
            this.btnSpacing = WidgetsBuilder.pixelSize(Float.parseFloat(cfg.getProperty("ui.touchbtnspacing")));
            this.tnb = new ThumbNailBuilder(64, 54, "cash.png");
        }
        public void addButton(String image, double amount) {
            DataLogicSystem dlSys = new DataLogicSystem();
            ImageIcon icon = new ImageIcon(this.tnb.getThumbNailText(dlSys.getResourceAsImage(image), Formats.CURRENCY.formatValue(amount)));
            JPanelPayments parent = JPanelPayments.this;
            CoinCountButton btn = new CoinCountButton(icon, amount,
                    parent.keypad, parent);
            parent.coinButtons.add(btn);
            GridBagConstraints cstr = new GridBagConstraints();
            cstr.gridx = this.x;
            cstr.gridy = this.y;
            cstr.insets = new Insets(btnSpacing, btnSpacing, btnSpacing,
                    btnSpacing);
            parent.coinCountBtnsContainer.add(btn.getComponent(), cstr);
            if (this.x == 3) {
                this.x = 0;
                this.y++;
            } else {
                this.x++;
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

        // coin buttons
        this.coinCountBtnsContainer = new JPanel();
        this.coinCountBtnsContainer.setLayout(new GridBagLayout());
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.weighty = 0.5;
        c.weightx = 1.0;
        c.gridwidth = 2;
        c.fill = GridBagConstraints.BOTH;
        this.add(this.coinCountBtnsContainer, c);

        // Notes
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 1;
        c.anchor = GridBagConstraints.LINE_START;
        c.insets = new Insets(btnSpacing, btnSpacing, 0, 0);
        this.add(notesLbl, c);
        c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = 1;
        c.insets = new Insets(btnSpacing, btnSpacing, 0, btnSpacing);
        c.fill = GridBagConstraints.HORIZONTAL;
        this.add(this.notes, c);

        // Keypad
        c = new GridBagConstraints();
        c.gridx = 2;
        c.gridy = 0;
        c.gridwidth = 2;
        this.add(this.keypad, c);

        // Reason
        c = new GridBagConstraints();
        c.gridx = 2;
        c.gridy = 1;
        c.anchor = GridBagConstraints.LINE_START;
        c.insets = new Insets(btnSpacing, btnSpacing, 0, 0);
        this.add(reasonLbl, c);
        c = new GridBagConstraints();
        c.gridx = 3;
        c.gridy = 1;
        c.insets = new Insets(btnSpacing, btnSpacing, 0, btnSpacing);
        c.fill = GridBagConstraints.HORIZONTAL;
        this.add(this.reason, c);

        // Total
        c = new GridBagConstraints();
        c.gridx = 2;
        c.gridy = 2;
        c.anchor = GridBagConstraints.LINE_START;
        c.insets = new Insets(btnSpacing, btnSpacing, 0, 0);
        this.add(totalLbl, c);
        c = new GridBagConstraints();
        c.gridx = 3;
        c.gridy = 2;
        c.insets = new Insets(btnSpacing, btnSpacing, 0, btnSpacing);
        c.fill = GridBagConstraints.HORIZONTAL;
        this.add(this.total, c);

        // Ok
        c = new GridBagConstraints();
        c.gridx = 2;
        c.gridy = 3;
        c.gridwidth = 2;
        c.insets = new Insets(btnSpacing, btnSpacing, btnSpacing, btnSpacing);
        this.add(ok, c);

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
            } else {
                this.reset();
                MessageInf msg = new MessageInf(MessageInf.SGN_SUCCESS,
                        AppLocal.getIntString("Message.CashMovementSaved"));
                msg.show(this);
            }
        } catch (BasicException e) {
            e.printStackTrace();
            MessageInf msg = new MessageInf(MessageInf.SGN_NOTICE,
                    AppLocal.getIntString("message.cannotexecute"), e);
            msg.show(this);
        }
    }


    private fr.pasteque.pos.widgets.JEditorCurrency total;
    private fr.pasteque.pos.widgets.JEditorKeys keypad;
    private javax.swing.JComboBox reason;
    private fr.pasteque.pos.widgets.JEditorText notes;
    private JPanel coinCountBtnsContainer;
    private List<CoinCountButton> coinButtons;

}
