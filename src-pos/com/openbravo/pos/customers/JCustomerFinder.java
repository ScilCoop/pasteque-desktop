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

package com.openbravo.pos.customers;

import com.openbravo.basic.BasicException;
import com.openbravo.data.loader.QBFCompareEnum;
import com.openbravo.data.user.EditorCreator;
import com.openbravo.data.user.ListProvider;
import com.openbravo.data.user.ListProviderCreator;
import com.openbravo.pos.forms.AppLocal;
import com.openbravo.pos.widgets.JEditorKeys;
import com.openbravo.pos.widgets.JEditorString;
import com.openbravo.pos.widgets.WidgetsBuilder;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Window;
import java.util.ArrayList;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 *
 * @author  adrianromero
 */
public class JCustomerFinder extends javax.swing.JDialog implements EditorCreator {

    private CustomerInfo selectedCustomer;
    private ListProvider lpr;
   
    /** Creates new form JCustomerFinder */
    private JCustomerFinder(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
    }

    /** Creates new form JCustomerFinder */
    private JCustomerFinder(java.awt.Dialog parent, boolean modal) {
        super(parent, modal);
    }
    
    public static JCustomerFinder getCustomerFinder(Component parent, DataLogicCustomers dlCustomers) {
        Window window = getWindow(parent);
        
        JCustomerFinder myMsg;
        if (window instanceof Frame) { 
            myMsg = new JCustomerFinder((Frame) window, true);
        } else {
            myMsg = new JCustomerFinder((Dialog) window, true);
        }
        myMsg.init(dlCustomers);
        myMsg.applyComponentOrientation(parent.getComponentOrientation());
        return myMsg;
    }
    
    public CustomerInfo getSelectedCustomer() {
        return selectedCustomer;
    }

    private void init(DataLogicCustomers dlCustomers) {

        initComponents();

        jScrollPane1.getVerticalScrollBar().setPreferredSize(new Dimension(35, 35));

        m_jtxtTaxID.addEditorKeys(m_jKeys);
        m_jtxtSearchKey.addEditorKeys(m_jKeys);
        m_jtxtName.addEditorKeys(m_jKeys);

        m_jtxtTaxID.reset();
        m_jtxtSearchKey.reset();
        m_jtxtName.reset();
        
        m_jtxtTaxID.activate();

        lpr = new ListProviderCreator(dlCustomers.getCustomerList(), this);

        jListCustomers.setCellRenderer(new CustomerRenderer());

        getRootPane().setDefaultButton(jcmdOK);

        selectedCustomer = null;
    }
    
    public void search(CustomerInfo customer) {
        
        if (customer == null || customer.getName() == null || customer.getName().equals("")) {
            
            m_jtxtTaxID.reset();
            m_jtxtSearchKey.reset();
            m_jtxtName.reset();

            m_jtxtTaxID.activate();    
            
            cleanSearch();
        } else {
            
            m_jtxtTaxID.setText(customer.getTaxid());
            m_jtxtSearchKey.setText(customer.getSearchkey());
            m_jtxtName.setText(customer.getName());

            m_jtxtTaxID.activate();
            
            executeSearch();
        }
    }
    
    private void cleanSearch() {
        jListCustomers.setModel(new MyListData(new ArrayList()));
    }
    
    public void executeSearch() {
        try {
            jListCustomers.setModel(new MyListData(lpr.loadData()));
            if (jListCustomers.getModel().getSize() > 0) {
                jListCustomers.setSelectedIndex(0);
            }
        } catch (BasicException e) {
            e.printStackTrace();
        }        
    }
    
    public Object createValue() throws BasicException {
        
        Object[] afilter = new Object[6];
        
        // TaxID
        if (m_jtxtTaxID.getText() == null || m_jtxtTaxID.getText().equals("")) {
            afilter[0] = QBFCompareEnum.COMP_NONE;
            afilter[1] = null;
        } else {
            afilter[0] = QBFCompareEnum.COMP_RE;
            afilter[1] = "%" + m_jtxtTaxID.getText() + "%";
        }
        
        // SearchKey
        if (m_jtxtSearchKey.getText() == null || m_jtxtSearchKey.getText().equals("")) {
            afilter[2] = QBFCompareEnum.COMP_NONE;
            afilter[3] = null;
        } else {
            afilter[2] = QBFCompareEnum.COMP_RE;
            afilter[3] = "%" + m_jtxtSearchKey.getText() + "%";
        }
        
        // Name
        if (m_jtxtName.getText() == null || m_jtxtName.getText().equals("")) {
            afilter[4] = QBFCompareEnum.COMP_NONE;
            afilter[5] = null;
        } else {
            afilter[4] = QBFCompareEnum.COMP_RE;
            afilter[5] = "%" + m_jtxtName.getText() + "%";
        }
        
        return afilter;
    } 

    private static Window getWindow(Component parent) {
        if (parent == null) {
            return new JFrame();
        } else if (parent instanceof Frame || parent instanceof Dialog) {
            return (Window) parent;
        } else {
            return getWindow(parent.getParent());
        }
    }
    
    private static class MyListData extends javax.swing.AbstractListModel {
        
        private java.util.List m_data;
        
        public MyListData(java.util.List data) {
            m_data = data;
        }
        
        public Object getElementAt(int index) {
            return m_data.get(index);
        }
        
        public int getSize() {
            return m_data.size();
        } 
    }   
    
    private void initComponents() {

        JPanel jPanel2 = new JPanel();
        m_jKeys = new JEditorKeys();
        JPanel jPanel3 = new JPanel();
        JPanel jPanel5 = new JPanel();
        JPanel jPanel7 = new JPanel();
        JLabel custNameLbl = WidgetsBuilder.createLabel(AppLocal.getIntString("label.prodname"));
        m_jtxtName = new JEditorString();
        JLabel searchLbl = WidgetsBuilder.createLabel(AppLocal.getIntString("label.searchkey"));
        m_jtxtSearchKey = new JEditorString();
        JLabel custNumLbl = WidgetsBuilder.createLabel(AppLocal.getIntString("label.taxid"));
        m_jtxtTaxID = new JEditorString();
        JPanel jPanel6 = new JPanel();
        JButton clearBtn = WidgetsBuilder.createButton(null,
                                                       AppLocal.getIntString("button.clean"),
                                                       WidgetsBuilder.SIZE_MEDIUM);
        JButton searchBtn = WidgetsBuilder.createButton(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/launch.png")),
                                                        AppLocal.getIntString("button.executefilter"),
                                                        WidgetsBuilder.SIZE_MEDIUM);
        JPanel jPanel4 = new JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jListCustomers = new javax.swing.JList();
        JPanel jPanel8 = new JPanel();
        JPanel jPanel1 = new JPanel();
        jcmdOK = WidgetsBuilder.createButton(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/button_ok.png")),
                                             AppLocal.getIntString("Button.OK"),
                                             WidgetsBuilder.SIZE_MEDIUM);
        jcmdCancel = WidgetsBuilder.createButton(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/button_cancel.png")),
                                                 AppLocal.getIntString("Button.Cancel"),
                                                 WidgetsBuilder.SIZE_MEDIUM);

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle(AppLocal.getIntString("form.customertitle")); // NOI18N

        jPanel2.setLayout(new java.awt.BorderLayout());
        jPanel2.add(m_jKeys, java.awt.BorderLayout.NORTH);

        getContentPane().add(jPanel2, java.awt.BorderLayout.LINE_END);

        jPanel3.setLayout(new java.awt.BorderLayout());

        jPanel5.setLayout(new java.awt.BorderLayout());

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel7Layout.createSequentialGroup()
                        .addComponent(custNumLbl, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(m_jtxtTaxID, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel7Layout.createSequentialGroup()
                        .addComponent(custNameLbl, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(m_jtxtName, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel7Layout.createSequentialGroup()
                        .addComponent(searchLbl, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(m_jtxtSearchKey, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(custNumLbl)
                    .addComponent(m_jtxtTaxID, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(searchLbl)
                    .addComponent(m_jtxtSearchKey, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(custNameLbl)
                    .addComponent(m_jtxtName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel5.add(jPanel7, java.awt.BorderLayout.CENTER);

        clearBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearBtnActionPerformed(evt);
            }
        });
        jPanel6.add(clearBtn);

        searchBtn.setFocusPainted(false);
        searchBtn.setFocusable(false);
        searchBtn.setRequestFocusEnabled(false);
        searchBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                searchBtnActionPerformed(evt);
            }
        });
        jPanel6.add(searchBtn);

        jPanel5.add(jPanel6, java.awt.BorderLayout.SOUTH);

        jPanel3.add(jPanel5, java.awt.BorderLayout.PAGE_START);

        jPanel4.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        jPanel4.setLayout(new java.awt.BorderLayout());

        jListCustomers.setFocusable(false);
        jListCustomers.setRequestFocusEnabled(false);
        jListCustomers.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jListCustomersMouseClicked(evt);
            }
        });
        jListCustomers.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                jListCustomersValueChanged(evt);
            }
        });
        jScrollPane1.setViewportView(jListCustomers);

        jPanel4.add(jScrollPane1, java.awt.BorderLayout.CENTER);

        jPanel3.add(jPanel4, java.awt.BorderLayout.CENTER);

        jPanel8.setLayout(new java.awt.BorderLayout());

        jcmdOK.setEnabled(false);
        jcmdOK.setFocusPainted(false);
        jcmdOK.setFocusable(false);
        jcmdOK.setRequestFocusEnabled(false);
        jcmdOK.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jcmdOKActionPerformed(evt);
            }
        });
        jPanel1.add(jcmdOK);

        jcmdCancel.setFocusPainted(false);
        jcmdCancel.setFocusable(false);
        jcmdCancel.setRequestFocusEnabled(false);
        jcmdCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jcmdCancelActionPerformed(evt);
            }
        });
        jPanel1.add(jcmdCancel);

        jPanel8.add(jPanel1, java.awt.BorderLayout.LINE_END);

        jPanel3.add(jPanel8, java.awt.BorderLayout.SOUTH);

        getContentPane().add(jPanel3, java.awt.BorderLayout.CENTER);

        java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        setBounds((screenSize.width-613)/2, (screenSize.height-610)/2, 613, 610);
    }

    private void jcmdOKActionPerformed(java.awt.event.ActionEvent evt) {

        selectedCustomer = (CustomerInfo) jListCustomers.getSelectedValue();
        dispose();
        
    }

    private void jcmdCancelActionPerformed(java.awt.event.ActionEvent evt) {

        dispose();
        
    }

    private void searchBtnActionPerformed(java.awt.event.ActionEvent evt) {

        executeSearch();
        
    }

    private void jListCustomersValueChanged(javax.swing.event.ListSelectionEvent evt) {

        jcmdOK.setEnabled(jListCustomers.getSelectedValue() != null);

    }

    private void jListCustomersMouseClicked(java.awt.event.MouseEvent evt) {
        
        if (evt.getClickCount() == 2) {
            selectedCustomer = (CustomerInfo) jListCustomers.getSelectedValue();
            dispose();
        }
        
    }

    private void clearBtnActionPerformed(java.awt.event.ActionEvent evt) {
 
        m_jtxtTaxID.reset();
        m_jtxtSearchKey.reset();
        m_jtxtName.reset();

        m_jtxtTaxID.activate();    

        cleanSearch();           
    }

    private javax.swing.JList jListCustomers;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JButton jcmdCancel;
    private javax.swing.JButton jcmdOK;
    private JEditorKeys m_jKeys;
    private JEditorString m_jtxtName;
    private JEditorString m_jtxtSearchKey;
    private JEditorString m_jtxtTaxID;

}
