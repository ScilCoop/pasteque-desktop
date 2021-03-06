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

import fr.pasteque.pos.ticket.ProductFilterSales;
import fr.pasteque.pos.ticket.ProductInfoExt;
import fr.pasteque.pos.ticket.ProductRenderer;
import javax.swing.*;
import java.awt.*;
import fr.pasteque.pos.forms.AppLocal;
import fr.pasteque.basic.BasicException;
import fr.pasteque.data.loader.ImageLoader;
import fr.pasteque.pos.forms.DataLogicSales;
import fr.pasteque.pos.widgets.JEditorKeys;
import fr.pasteque.pos.widgets.WidgetsBuilder;

/**
 *
 * @author adrianromero
 */
public class JProductFinder extends javax.swing.JDialog {

    private ProductInfoExt m_ReturnProduct;
    private ProductFilterSales filter;
    
    public final static int PRODUCT_ALL = 0;
    public final static int PRODUCT_NORMAL = 1;
    public final static int PRODUCT_AUXILIAR = 2;
    
    /** Creates new form JProductFinder */
    private JProductFinder(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
    }
    /** Creates new form JProductFinder */
    private JProductFinder(java.awt.Dialog parent, boolean modal) {
        super(parent, modal);
    }    
    
    private ProductInfoExt init(DataLogicSales dlSales, int productsType) {
        
        initComponents();
        
        jScrollPane1.getVerticalScrollBar().setPreferredSize(new Dimension(35, 35));

        //ProductFilter jproductfilter = new ProductFilter(app);
        this.filter = new ProductFilterSales(dlSales, m_jKeys);
        this.filter.activate();
        m_jProductSelect.add(this.filter, BorderLayout.CENTER);
        jListProducts.setCellRenderer(new ProductRenderer());
        
        getRootPane().setDefaultButton(jcmdOK);   
   
        m_ReturnProduct = null;
        
        //show();
        setVisible(true);
        
        return m_ReturnProduct;
    }
    
    
    private static Window getWindow(Component parent) {
        if (parent == null) {
            return new JFrame();
        } else if (parent instanceof Frame || parent instanceof Dialog) {
            return (Window)parent;
        } else {
            return getWindow(parent.getParent());
        }
    }    
    
    public static ProductInfoExt showMessage(Component parent, DataLogicSales dlSales) {
        return showMessage(parent, dlSales, PRODUCT_ALL);
    }

    public static ProductInfoExt showMessage(Component parent, DataLogicSales dlSales, int productsType) {

        Window window = getWindow(parent);

        JProductFinder myMsg;
        if (window instanceof Frame) {
            myMsg = new JProductFinder((Frame) window, true);
        } else {
            myMsg = new JProductFinder((Dialog) window, true);
        }
        return myMsg.init(dlSales, productsType);
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
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel4 = new javax.swing.JPanel();
        m_jKeys = new JEditorKeys();
        jPanel2 = new javax.swing.JPanel();
        m_jProductSelect = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        searchBtn = WidgetsBuilder.createButton(ImageLoader.readImageIcon("execute.png"),
                                               AppLocal.getIntString("button.executefilter"),
                                               WidgetsBuilder.SIZE_MEDIUM);
        jPanel5 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jListProducts = new javax.swing.JList();
        jPanel1 = new javax.swing.JPanel();
        jcmdOK = WidgetsBuilder.createButton(ImageLoader.readImageIcon("button_ok.png"),
                                             AppLocal.getIntString("Button.OK"),
                                             WidgetsBuilder.SIZE_MEDIUM);
        jcmdCancel = WidgetsBuilder.createButton(ImageLoader.readImageIcon("button_cancel.png"),
                                                 AppLocal.getIntString("Button.Cancel"),
                                                 WidgetsBuilder.SIZE_MEDIUM);

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle(AppLocal.getIntString("form.productslist")); // NOI18N

        jPanel4.setLayout(new java.awt.BorderLayout());
        jPanel4.add(m_jKeys, java.awt.BorderLayout.NORTH);

        getContentPane().add(jPanel4, java.awt.BorderLayout.LINE_END);

        jPanel2.setLayout(new java.awt.BorderLayout());

        m_jProductSelect.setLayout(new java.awt.BorderLayout());

        searchBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                searchBtnActionPerformed(evt);
            }
        });
        jPanel3.add(searchBtn);

        m_jProductSelect.add(jPanel3, java.awt.BorderLayout.SOUTH);

        jPanel2.add(m_jProductSelect, java.awt.BorderLayout.NORTH);

        jPanel5.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        jPanel5.setLayout(new java.awt.BorderLayout());

        jScrollPane1.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        jListProducts.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jListProducts.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                jListProductsValueChanged(evt);
            }
        });
        jListProducts.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jListProductsMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(jListProducts);

        jPanel5.add(jScrollPane1, java.awt.BorderLayout.CENTER);

        jPanel2.add(jPanel5, java.awt.BorderLayout.CENTER);

        jPanel1.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

        jcmdOK.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jcmdOKActionPerformed(evt);
            }
        });
        jPanel1.add(jcmdOK);

        jcmdCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jcmdCancelActionPerformed(evt);
            }
        });
        jPanel1.add(jcmdCancel);

        jPanel2.add(jPanel1, java.awt.BorderLayout.SOUTH);

        getContentPane().add(jPanel2, java.awt.BorderLayout.CENTER);

        java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        int minWidth = 665;
        int minHeight = 565;
        int width = WidgetsBuilder.dipToPx(400);
        int height = WidgetsBuilder.dipToPx(320);
        width = Math.max(minWidth, width);
        height = Math.max(minHeight, height);
        // If popup is big enough, make it fullscreen
        if (width > 0.8 * screenSize.width || height > 0.8 * screenSize.height) {
            width = screenSize.width;
            height = screenSize.height;
            this.setUndecorated(true);
        }
        setBounds((screenSize.width-width)/2, (screenSize.height-height)/2, width, height);
    }// </editor-fold>//GEN-END:initComponents

    private void jListProductsMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jListProductsMouseClicked

        if (evt.getClickCount() == 2) {
            m_ReturnProduct = (ProductInfoExt) jListProducts.getSelectedValue();
            dispose();
        }
        
    }//GEN-LAST:event_jListProductsMouseClicked

    private void jcmdOKActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jcmdOKActionPerformed
        
        m_ReturnProduct = (ProductInfoExt) jListProducts.getSelectedValue();
        dispose();
        
    }//GEN-LAST:event_jcmdOKActionPerformed

    private void jcmdCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jcmdCancelActionPerformed
        
        dispose();
        
    }//GEN-LAST:event_jcmdCancelActionPerformed

    private void jListProductsValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_jListProductsValueChanged

        jcmdOK.setEnabled(jListProducts.getSelectedValue() != null);
        
    }//GEN-LAST:event_jListProductsValueChanged

    private void searchBtnActionPerformed(java.awt.event.ActionEvent evt) {
        try {
            String[] filter = this.filter.getFilter();
            DataLogicSales dlSales = new DataLogicSales();
            java.util.List<ProductInfoExt> data = dlSales.searchProducts(filter[0],
                    filter[1]);
            jListProducts.setModel(new MyListData(data));
            if (jListProducts.getModel().getSize() > 0) {
                jListProducts.setSelectedIndex(0);
            }
        } catch (BasicException e) {
            e.printStackTrace();
        }
        
    }
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton searchBtn;
    private javax.swing.JList jListProducts;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JButton jcmdCancel;
    private javax.swing.JButton jcmdOK;
    private JEditorKeys m_jKeys;
    private javax.swing.JPanel m_jProductSelect;
    // End of variables declaration//GEN-END:variables
    
}
