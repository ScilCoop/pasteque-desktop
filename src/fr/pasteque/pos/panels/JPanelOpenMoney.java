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
import fr.pasteque.data.loader.StaticSentence;
import fr.pasteque.data.loader.SerializerWriteBasic;
import fr.pasteque.data.gui.MessageInf;
import fr.pasteque.pos.forms.AppView;
import fr.pasteque.pos.forms.AppLocal;
import fr.pasteque.pos.forms.DataLogicSystem;
import fr.pasteque.pos.forms.JPanelView;
import fr.pasteque.pos.forms.JPrincipalApp;
import fr.pasteque.pos.ticket.CashSession;
import fr.pasteque.pos.widgets.WidgetsBuilder;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.Date;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class JPanelOpenMoney
extends JPanel
implements JPanelView {

    private AppView appView;
    private JPrincipalApp principalApp;
    private DataLogicSystem dlSystem;
    private String targetTask;

    public JPanelOpenMoney(AppView appView, JPrincipalApp principalApp,
            String targetTask) {
        this.appView = appView;
        this.principalApp = principalApp;
        this.targetTask = targetTask;
        this.dlSystem = (DataLogicSystem) appView.getBean("fr.pasteque.pos.forms.DataLogicSystem");
        initComponents();
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
        
    }   
    
    public boolean deactivate() {
        return true;
    }
    
    private void initComponents() {
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints cstr = null;

        this.setLayout(gridbag);
        
        JLabel cashClosed = WidgetsBuilder.createLabel(AppLocal.getIntString("message.cashisclosed"));
        cstr = new GridBagConstraints();
        cstr.gridx = 0;
        cstr.gridy = 0;
        cstr.weighty = 0.5;
        this.add(cashClosed, cstr);

        if (this.principalApp.getUser().hasPermission("button.openmoney")) {
            JButton openCash = WidgetsBuilder.createButton(WidgetsBuilder.createIcon("open_cash.png"), AppLocal.getIntString("label.opencash"), WidgetsBuilder.SIZE_BIG);
            openCash.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    openCash(evt);
                }
            });
            cstr = new GridBagConstraints();
            cstr.gridx = 0;
            cstr.gridy = 1;
            cstr.weighty = 0.5;
            this.add(openCash, cstr);
        }
    }
    
    private void openCash(java.awt.event.ActionEvent evt) {
        // Open cash
        Date now = new Date();
        CashSession cashSess = this.appView.getActiveCashSession();
        cashSess.open(now);
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
