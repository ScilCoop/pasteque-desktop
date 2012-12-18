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

package com.openbravo.pos.sales;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JPanel;
import com.openbravo.pos.forms.AppConfig;
import com.openbravo.pos.forms.AppLocal;
import com.openbravo.pos.forms.DataLogicSystem;
import com.openbravo.pos.ticket.TicketLineInfo;
import com.openbravo.pos.widgets.WidgetsBuilder;

public class JRefundLines extends javax.swing.JPanel {
    
    private JTicketLines ticketlines;
    private List m_aLines;

    private JPanelTicketEdits m_jTicketEdit;
    
    /** Creates new form JRefundLines */
    public JRefundLines(DataLogicSystem dlSystem, JPanelTicketEdits jTicketEdit) {
        
        m_jTicketEdit = jTicketEdit;
        
        initComponents();
        
        ticketlines = new JTicketLines(dlSystem.getResourceAsXML("Ticket.Line"));
        
        this.linesContainer.add(ticketlines, BorderLayout.CENTER);
    }
    
    public void setLines(List aRefundLines) {
        
        m_aLines = aRefundLines;
        ticketlines.clearTicketLines();
        
        if (m_aLines != null) {
            for (int i = 0; i < m_aLines.size(); i++) {
                ticketlines.addTicketLine((TicketLineInfo) m_aLines.get(i));
            }
        }
    }
     
    private void initComponents() {
        this.setLayout(new GridBagLayout());
        GridBagConstraints cstr = null;
        AppConfig cfg = AppConfig.loadedInstance;
        int btnspacing = WidgetsBuilder.pixelSize(Float.parseFloat(cfg.getProperty("ui.touchbtnspacing")));
        
        // Buttons column
        JPanel buttonsLayout = new JPanel();
        buttonsLayout.setLayout(new GridBagLayout());
        JButton m_jbtnAddOne = WidgetsBuilder.createButton(null, AppLocal.getIntString("button.refundone"), WidgetsBuilder.SIZE_MEDIUM);
        JButton m_jbtnAddLine = WidgetsBuilder.createButton(null, AppLocal.getIntString("button.refundline"), WidgetsBuilder.SIZE_MEDIUM);
        JButton m_jbtnAddAll = WidgetsBuilder.createButton(null, AppLocal.getIntString("button.refundall"), WidgetsBuilder.SIZE_MEDIUM);
        cstr = new GridBagConstraints();
        cstr.gridx = 0;
        cstr.gridy = 0;
        cstr.fill = GridBagConstraints.HORIZONTAL;
        cstr.insets = new Insets(0, btnspacing, btnspacing, 0);
        buttonsLayout.add(m_jbtnAddOne, cstr);
        cstr = new GridBagConstraints();
        cstr.gridx = 0;
        cstr.gridy = 1;
        cstr.fill = GridBagConstraints.HORIZONTAL;
        cstr.insets = new Insets(0, btnspacing, btnspacing, 0);
        buttonsLayout.add(m_jbtnAddLine, cstr);
        cstr = new GridBagConstraints();
        cstr.gridx = 0;
        cstr.gridy = 2;
        cstr.fill = GridBagConstraints.HORIZONTAL;
        cstr.insets = new Insets(0, btnspacing, 0, 0);
        buttonsLayout.add(m_jbtnAddAll, cstr);
        cstr = new GridBagConstraints();
        cstr.gridx = 1;
        cstr.gridy = 0;
        cstr.anchor = GridBagConstraints.NORTH;
        this.add(buttonsLayout, cstr);
        m_jbtnAddOne.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_jbtnAddOneActionPerformed(evt);
            }
        });
        m_jbtnAddLine.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_jbtnAddLineActionPerformed(evt);
            }
        });
        m_jbtnAddAll.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_jbtnAddAllActionPerformed(evt);
            }
        });

        // Lines container
        this.linesContainer = new javax.swing.JPanel();
        this.linesContainer.setLayout(new java.awt.BorderLayout());
        cstr = new GridBagConstraints();
        cstr.gridx = 0;
        cstr.gridy = 0;
        cstr.weightx = 1;
        cstr.weighty = 1;
        cstr.fill = GridBagConstraints.BOTH;
        this.add(this.linesContainer, cstr);
    }

    private void m_jbtnAddAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_jbtnAddAllActionPerformed

        for (int i = 0; i < m_aLines.size(); i++) {
            TicketLineInfo oLine = (TicketLineInfo) m_aLines.get(i);
            TicketLineInfo oNewLine = new TicketLineInfo(oLine);            
            oNewLine.setMultiply(-oLine.getMultiply());
            m_jTicketEdit.addTicketLine(oNewLine);
        }
        
    }//GEN-LAST:event_m_jbtnAddAllActionPerformed

    private void m_jbtnAddOneActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_jbtnAddOneActionPerformed

        int index = ticketlines.getSelectedIndex();
        if (index >= 0) {
            TicketLineInfo oLine = (TicketLineInfo) m_aLines.get(index);
            TicketLineInfo oNewLine = new TicketLineInfo(oLine);
            oNewLine.setMultiply(-1.0);
            m_jTicketEdit.addTicketLine(oNewLine);
        }   
        
    }//GEN-LAST:event_m_jbtnAddOneActionPerformed

    private void m_jbtnAddLineActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_jbtnAddLineActionPerformed

        int index = ticketlines.getSelectedIndex();
        if (index >= 0) {
            TicketLineInfo oLine = (TicketLineInfo) m_aLines.get(index);
            TicketLineInfo oNewLine = new TicketLineInfo(oLine);            
            oNewLine.setMultiply(-oLine.getMultiply());
            m_jTicketEdit.addTicketLine(oNewLine);
        }        
    }//GEN-LAST:event_m_jbtnAddLineActionPerformed
    
    
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private JPanel linesContainer;
    
}
