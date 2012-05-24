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

package com.openbravo.pos.panels;

import com.openbravo.basic.BasicException;
import com.openbravo.data.loader.Datas;
import com.openbravo.data.loader.StaticSentence;
import com.openbravo.data.loader.SerializerWriteBasic;
import com.openbravo.data.gui.MessageInf;
import com.openbravo.pos.forms.AppView;
import com.openbravo.pos.forms.AppLocal;
import com.openbravo.pos.forms.DataLogicSystem;
import com.openbravo.pos.forms.JPanelView;
import com.openbravo.pos.forms.JPrincipalApp;
import com.openbravo.pos.widgets.WidgetsBuilder;
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
        this.dlSystem = (DataLogicSystem) appView.getBean("com.openbravo.pos.forms.DataLogicSystem");
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
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.CENTER;
        gridbag.setConstraints(this, constraints);
        this.setLayout(gridbag);
        
        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        
        JLabel cashClosed = WidgetsBuilder.createLabel(AppLocal.getIntString("message.cashisclosed"));
        cashClosed.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        cashClosed.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        cashClosed.setVerticalTextPosition(javax.swing.SwingConstants.TOP);
        cashClosed.setAlignmentX(Component.CENTER_ALIGNMENT);
        container.add(cashClosed);

        if (this.principalApp.getUser().hasPermission("button.openmoney")) {
            JButton openCash = WidgetsBuilder.createButton(WidgetsBuilder.createIcon("/com/openbravo/images/password.png"), AppLocal.getIntString("label.opencash"), WidgetsBuilder.SIZE_BIG);
            openCash.setAlignmentX(Component.CENTER_ALIGNMENT);
            openCash.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    openCash(evt);
                }
            });
            container.add(openCash);
        }
        this.add(container);
    }
    
    private void openCash(java.awt.event.ActionEvent evt) {
        // Open a new cash
        Date now = new Date();
        // Open cash in database
        try {
            new StaticSentence(this.appView.getSession()
                    , "UPDATE CLOSEDCASH SET DATESTART = ? WHERE HOST = ? AND MONEY = ?"
                    , new SerializerWriteBasic(new Datas[] {Datas.TIMESTAMP, Datas.STRING, Datas.STRING})
            ).exec(new Object[] {now, appView.getProperties().getHost(), appView.getActiveCashIndex()});
        } catch (BasicException e) {
                MessageInf msg = new MessageInf(MessageInf.SGN_NOTICE,
                        AppLocal.getIntString("message.cannotopencash"), e);
                msg.show(this);
                return;
        }
        this.appView.setActiveCash(
                this.appView.getActiveCashIndex(),
                this.appView.getActiveCashSequence(),
                now, null);
        // Go to original task
        this.principalApp.showTask(this.targetTask);
    }
}
