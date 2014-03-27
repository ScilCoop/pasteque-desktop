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

package fr.pasteque.pos.panels;

import fr.pasteque.basic.BasicException;
import fr.pasteque.data.loader.Datas;
import fr.pasteque.data.loader.ImageLoader;
import fr.pasteque.data.loader.SerializerWriteBasic;
import fr.pasteque.data.gui.MessageInf;
import fr.pasteque.format.Formats;
import fr.pasteque.pos.forms.AppConfig;
import fr.pasteque.pos.forms.AppView;
import fr.pasteque.pos.forms.AppLocal;
import fr.pasteque.pos.forms.DataLogicSystem;
import fr.pasteque.pos.forms.JPanelView;
import fr.pasteque.pos.forms.JPrincipalApp;
import fr.pasteque.pos.scripting.ScriptEngine;
import fr.pasteque.pos.scripting.ScriptException;
import fr.pasteque.pos.scripting.ScriptFactory;
import fr.pasteque.pos.ticket.CashSession;
import fr.pasteque.pos.util.ThumbNailBuilder;
import fr.pasteque.pos.widgets.CoinCountButton;
import fr.pasteque.pos.widgets.JEditorKeys;
import fr.pasteque.pos.widgets.WidgetsBuilder;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class JPanelOpenMoney
extends JPanel
implements JPanelView, CoinCountButton.Listener {

    private AppView appView;
    private JPrincipalApp principalApp;
    private DataLogicSystem dlSystem;
    private String targetTask;
    private JPanel coinCountBtnsContainer;
    private List<CoinCountButton> coinButtons;
    private JEditorKeys keypad;
    private JLabel totalAmount;
    private double total;

    public JPanelOpenMoney(AppView appView, JPrincipalApp principalApp,
            String targetTask) {
        this.appView = appView;
        this.principalApp = principalApp;
        this.targetTask = targetTask;
        this.dlSystem = new DataLogicSystem();
        this.coinButtons = new ArrayList<CoinCountButton>();
        initComponents();
        AppConfig cfg = AppConfig.loadedInstance;
        boolean showCount = cfg.getProperty("ui.countmoney").equals("1");
        boolean canOpen = this.principalApp.getUser().hasPermission("button.openmoney");
        if (showCount && canOpen) {
            String code = this.dlSystem.getResourceAsXML("payment.cash");
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
    }
    
    public JComponent getComponent() {
        return this;
    }

    public String getTitle() {
        return AppLocal.getIntString("Menu.OpenTPV");
    }
    
    public boolean requiresOpenedCash() {
        return false;
    }
    
    public void activate() throws BasicException {
        this.updateAmount();
        // Open drawer if allowed to open and counting
        AppConfig cfg = AppConfig.loadedInstance;
        if (this.principalApp.getUser().hasPermission("button.openmoney")
                && cfg.getProperty("ui.countmoney").equals("1")) {
            String code = this.dlSystem.getResourceAsXML("Printer.OpenDrawer");
            if (code != null) {
                try {
                    ScriptEngine script = ScriptFactory.getScriptEngine(ScriptFactory.VELOCITY);
                    script.eval(code);
                } catch (ScriptException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public boolean deactivate() {
        return true;
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
            ImageIcon icon = new ImageIcon(this.tnb.getThumbNailText(dlSystem.getResourceAsImage(image), Formats.CURRENCY.formatValue(amount)));
            JPanelOpenMoney parent = JPanelOpenMoney.this;
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

    public void coinAdded(double amount, int newCount) {
        this.total += amount;
        this.totalAmount.setText(Formats.CURRENCY.formatValue(this.total));
    }
    public void countUpdated() {
        this.updateAmount();
    }

    public void updateAmount() {
        if (this.totalAmount == null) {
            return;
        }
        this.total = 0.0;
        for (CoinCountButton btn : this.coinButtons) {
            this.total += btn.getAmount();
        }
        this.totalAmount.setText(Formats.CURRENCY.formatValue(this.total));
    }

    private void initComponents() {
        AppConfig cfg = AppConfig.loadedInstance;
        boolean showCount = cfg.getProperty("ui.countmoney").equals("1");
        int btnSpacing = WidgetsBuilder.pixelSize(Float.parseFloat(cfg.getProperty("ui.touchbtnspacing")));
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints cstr = null;

        this.setLayout(gridbag);
        
        JLabel cashClosed = WidgetsBuilder.createLabel(AppLocal.getIntString("message.cashisclosed"));

        boolean canOpen = this.principalApp.getUser().hasPermission("button.openmoney");
        if (canOpen) {
            JButton openCash = WidgetsBuilder.createButton(WidgetsBuilder.createIcon("open_cash.png"), AppLocal.getIntString("label.opencash"), WidgetsBuilder.SIZE_BIG);
            openCash.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    openCash(evt);
                }
            });
            if (!showCount) {
                // Single button ui
                cstr = new GridBagConstraints();
                cstr.gridx = 0;
                cstr.gridy = 0;
                cstr.weighty = 0.5;
                this.add(cashClosed, cstr);
                cstr = new GridBagConstraints();
                cstr.gridx = 0;
                cstr.gridy = 1;
                cstr.weighty = 0.5;
                this.add(openCash, cstr);
            } else {
                // Show count ui
                cstr = new GridBagConstraints();
                cstr.gridx = 0;
                cstr.gridy = 0;
                cstr.gridwidth = 2;
                cstr.weighty = 0.2;
                this.add(cashClosed, cstr);
                // coin buttons
                this.coinCountBtnsContainer = new JPanel();
                this.coinCountBtnsContainer.setLayout(new GridBagLayout());
                cstr = new GridBagConstraints();
                cstr.gridx = 0;
                cstr.gridy = 1;
                cstr.weighty = 0.5;
                cstr.weightx = 1.0;
                cstr.fill = GridBagConstraints.BOTH;
                this.add(this.coinCountBtnsContainer, cstr);
                // keypad and input
                JPanel inputContainer = new JPanel();
                inputContainer.setLayout(new GridBagLayout());
                this.keypad = new JEditorKeys();
                cstr = new GridBagConstraints();
                cstr.gridx = 0;
                cstr.gridy = 0;
                cstr.insets = new Insets(btnSpacing, btnSpacing, btnSpacing,
                        btnSpacing);
                inputContainer.add(this.keypad, cstr);
                JLabel amountLabel = WidgetsBuilder.createLabel(AppLocal.getIntString("label.openCashAmount"));
                cstr = new GridBagConstraints();
                cstr.gridx = 0;
                cstr.gridy = 1;
                cstr.anchor = GridBagConstraints.FIRST_LINE_START;
                inputContainer.add(amountLabel, cstr);
                this.totalAmount = WidgetsBuilder.createImportantLabel("");
                WidgetsBuilder.inputStyle(totalAmount);
                cstr = new GridBagConstraints();
                cstr.gridx = 0;
                cstr.gridy = 2;
                cstr.weightx = 1.0;
                cstr.fill = GridBagConstraints.BOTH;
                cstr.anchor = GridBagConstraints.CENTER;
                inputContainer.add(totalAmount, cstr);
                cstr = new GridBagConstraints();
                cstr.gridx = 0;
                cstr.gridy = 3;
                cstr.insets = new Insets(btnSpacing, btnSpacing, btnSpacing,
                        btnSpacing);
                inputContainer.add(openCash, cstr);
                cstr = new GridBagConstraints();
                cstr.gridx = 1;
                cstr.gridy = 1;
                cstr.weightx = 0.5;
                cstr.anchor = GridBagConstraints.CENTER;
                cstr.insets = new Insets(0, btnSpacing, btnSpacing, btnSpacing);
                this.add(inputContainer, cstr);
            }
        } else {
            cstr = new GridBagConstraints();
            cstr.gridx = 0;
            cstr.gridy = 0;
            this.add(cashClosed, cstr);
        }
    }
    
    private void openCash(java.awt.event.ActionEvent evt) {
        // Open cash
        Date now = new Date();
        CashSession cashSess = this.appView.getActiveCashSession();
        cashSess.open(now);
        if (AppConfig.loadedInstance.getProperty("ui.countmoney").equals("1")) {
            cashSess.setOpenCash(this.total);
        }
        // Send cash to server and update from answer
        try {
            cashSess = this.dlSystem.saveCashSession(cashSess);
            this.appView.setActiveCash(cashSess);
        } catch (BasicException e) {
                MessageInf msg = new MessageInf(MessageInf.SGN_NOTICE,
                        AppLocal.getIntString("message.cannotopencash"), e);
                msg.show(this);
                return;
        }
        // Go to original task
        this.principalApp.showTask(this.targetTask);
    }
}
