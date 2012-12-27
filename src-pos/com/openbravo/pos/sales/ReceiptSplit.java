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

package com.openbravo.pos.sales;

import com.openbravo.data.loader.ImageLoader;
import com.openbravo.pos.customers.DataLogicCustomers;
import com.openbravo.pos.forms.AppLocal;
import com.openbravo.pos.forms.DataLogicSales;
import com.openbravo.pos.ticket.TicketInfo;
import com.openbravo.pos.ticket.TicketLineInfo;
import com.openbravo.pos.widgets.WidgetsBuilder;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.Window;
import javax.swing.JFrame;

/**
 *
 * @author  adrianromero
 */
public class ReceiptSplit extends javax.swing.JDialog {
    
    private boolean accepted;
    
    SimpleReceipt receiptone;
    SimpleReceipt receipttwo;
    
    /** Creates new form ReceiptSplit */
    protected ReceiptSplit(java.awt.Frame parent) {
        super(parent, true);
    }
    /** Creates new form ReceiptSplit */
    protected ReceiptSplit(java.awt.Dialog parent) {
        super(parent, true);
    } 
    
    private void init(String ticketline, DataLogicSales dlSales, DataLogicCustomers dlCustomers, TaxesLogic taxeslogic) {
        
        initComponents();        
        getRootPane().setDefaultButton(m_jButtonOK); 
        
        receiptone = new SimpleReceipt(ticketline, dlSales, dlCustomers, taxeslogic);
        receiptone.setCustomerEnabled(false);
        jPanel5.add(receiptone, BorderLayout.CENTER);
        
        receipttwo = new SimpleReceipt(ticketline, dlSales, dlCustomers, taxeslogic);
        jPanel3.add(receipttwo, BorderLayout.CENTER);
    }
    
    public static ReceiptSplit getDialog(Component parent, String ticketline, DataLogicSales dlSales, DataLogicCustomers dlCustomers, TaxesLogic taxeslogic) {
         
        Window window = getWindow(parent);
        
        ReceiptSplit myreceiptsplit;
        
        if (window instanceof Frame) { 
            myreceiptsplit = new ReceiptSplit((Frame) window);
        } else {
            myreceiptsplit = new ReceiptSplit((Dialog) window);
        }
        
        myreceiptsplit.init(ticketline, dlSales, dlCustomers, taxeslogic);         
        
        return myreceiptsplit;
    } 
    
    protected static Window getWindow(Component parent) {
        if (parent == null) {
            return new JFrame();
        } else if (parent instanceof Frame || parent instanceof Dialog) {
            return (Window)parent;
        } else {
            return getWindow(parent.getParent());
        }
    }
    
    public boolean showDialog(TicketInfo ticket, TicketInfo ticket2, Object ticketext) {

        receiptone.setTicket(ticket, ticketext);
        receipttwo.setTicket(ticket2, ticketext);
        
        setVisible(true);    
        return accepted;
    }    
    
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jPanel2 = new javax.swing.JPanel();
        m_jButtonOK = WidgetsBuilder.createButton(ImageLoader.readImageIcon("button_ok.png"),
                                                  AppLocal.getIntString("Button.OK"),
                                                  WidgetsBuilder.SIZE_MEDIUM);
        m_jButtonCancel = WidgetsBuilder.createButton(ImageLoader.readImageIcon("button_cancel.png"),
                                                  AppLocal.getIntString("Button.Cancel"),
                                                  WidgetsBuilder.SIZE_MEDIUM);
        jPanel1 = new javax.swing.JPanel();
        jPanel5 = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        jBtnToRightAll = WidgetsBuilder.createButton(ImageLoader.readImageIcon("right_twice.png"));
        jBtnToRightOne = WidgetsBuilder.createButton(ImageLoader.readImageIcon("right_once.png"));
        jBtnToLeftOne = WidgetsBuilder.createButton(ImageLoader.readImageIcon("left_once.png"));
        jBtnToLeftAll = WidgetsBuilder.createButton(ImageLoader.readImageIcon("left_twice.png"));
        jPanel3 = new javax.swing.JPanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle(AppLocal.getIntString("caption.split")); // NOI18N
        setResizable(false);

        jPanel2.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

        m_jButtonOK.setFocusPainted(false);
        m_jButtonOK.setFocusable(false);
        m_jButtonOK.setRequestFocusEnabled(false);
        m_jButtonOK.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_jButtonOKActionPerformed(evt);
            }
        });
        jPanel2.add(m_jButtonOK);

        m_jButtonCancel.setFocusPainted(false);
        m_jButtonCancel.setFocusable(false);
        m_jButtonCancel.setRequestFocusEnabled(false);
        m_jButtonCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_jButtonCancelActionPerformed(evt);
            }
        });
        jPanel2.add(m_jButtonCancel);

        getContentPane().add(jPanel2, java.awt.BorderLayout.SOUTH);

        jPanel1.setLayout(new javax.swing.BoxLayout(jPanel1, javax.swing.BoxLayout.LINE_AXIS));

        jPanel5.setLayout(new java.awt.BorderLayout());
        jPanel1.add(jPanel5);

        jPanel4.setLayout(new java.awt.GridBagLayout());

        jBtnToRightAll.setFocusPainted(false);
        jBtnToRightAll.setFocusable(false);
        jBtnToRightAll.setRequestFocusEnabled(false);
        jBtnToRightAll.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnToRightAllActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 0;
        jPanel4.add(jBtnToRightAll, gridBagConstraints);

        jBtnToRightOne.setFocusPainted(false);
        jBtnToRightOne.setFocusable(false);
        jBtnToRightOne.setRequestFocusEnabled(false);
        jBtnToRightOne.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnToRightOneActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        jPanel4.add(jBtnToRightOne, gridBagConstraints);

        jBtnToLeftOne.setFocusPainted(false);
        jBtnToLeftOne.setFocusable(false);
        jBtnToLeftOne.setRequestFocusEnabled(false);
        jBtnToLeftOne.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnToLeftOneActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        jPanel4.add(jBtnToLeftOne, gridBagConstraints);

        jBtnToLeftAll.setFocusPainted(false);
        jBtnToLeftAll.setFocusable(false);
        jBtnToLeftAll.setRequestFocusEnabled(false);
        jBtnToLeftAll.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnToLeftAllActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 3;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        jPanel4.add(jBtnToLeftAll, gridBagConstraints);

        jPanel1.add(jPanel4);

        jPanel3.setLayout(new java.awt.BorderLayout());
        jPanel1.add(jPanel3);

        getContentPane().add(jPanel1, java.awt.BorderLayout.CENTER);

        java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        int minWidth = 680;
        int minHeight = 480;
        int width = WidgetsBuilder.dipToPx(460);
        int height = WidgetsBuilder.dipToPx(350);
        width = Math.max(minWidth, width);
        height = Math.max(minHeight, height);
        // If popup is big enough, make it fullscreen
        if (width > 0.8 * screenSize.width || height > 0.8 * screenSize.height) {
            width = screenSize.width;
            height = screenSize.height;
            this.setUndecorated(true);
        }
        setBounds((screenSize.width-width)/2, (screenSize.height-height)/2, width, height);
    }

    private void m_jButtonOKActionPerformed(java.awt.event.ActionEvent evt) {

        if (receipttwo.getTicket().getLinesCount() > 0) {
            accepted = true;
            dispose();
        }

    }

    private void m_jButtonCancelActionPerformed(java.awt.event.ActionEvent evt) {
        
        dispose();
        
    }

    private void jBtnToRightAllActionPerformed(java.awt.event.ActionEvent evt) {
       
        TicketLineInfo[] lines = receiptone.getSelectedLines();
        if (lines != null) {
            receipttwo.addSelectedLines(lines);
        }
            
    }

    private void jBtnToRightOneActionPerformed(java.awt.event.ActionEvent evt) {
        
        TicketLineInfo[] lines = receiptone.getSelectedLinesUnit();
        if (lines != null) {
            receipttwo.addSelectedLines(lines);
        }

    }

    private void jBtnToLeftOneActionPerformed(java.awt.event.ActionEvent evt) {
      
        TicketLineInfo[] lines = receipttwo.getSelectedLinesUnit();
        if (lines != null) {
            receiptone.addSelectedLines(lines);
        }
       
    }

    private void jBtnToLeftAllActionPerformed(java.awt.event.ActionEvent evt) {
       
        TicketLineInfo[] lines = receipttwo.getSelectedLines();
        if (lines != null) {
            receiptone.addSelectedLines(lines);
        }

    }
    
    private javax.swing.JButton jBtnToLeftAll;
    private javax.swing.JButton jBtnToLeftOne;
    private javax.swing.JButton jBtnToRightAll;
    private javax.swing.JButton jBtnToRightOne;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JButton m_jButtonCancel;
    private javax.swing.JButton m_jButtonOK;
    
}
