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

package fr.pasteque.beans;

import fr.pasteque.data.loader.ImageLoader;
import fr.pasteque.pos.widgets.WidgetsBuilder;
import fr.pasteque.pos.forms.AppLocal;

import java.awt.*;
import javax.swing.*;
import java.util.*;

public class JPasswordDialog extends javax.swing.JDialog {
    
    // private static ResourceBundle m_Intl;
    private static LocaleResources m_resources;

    private String m_sPassword;
        
    /** Creates new form JCalendarDialog */
    public JPasswordDialog(java.awt.Frame parent, boolean modal) {
        super(parent, modal);            
        init();
    }
    /** Creates new form JCalendarDialog */
    public JPasswordDialog(java.awt.Dialog parent, boolean modal) {
        super(parent, modal);            
        init();
    }    
    
    private void init() {
        
        if (m_resources == null) {
            m_resources = new LocaleResources();
            m_resources.addBundleName("beans_messages");
        }
        
        initComponents();        
        getRootPane().setDefaultButton(jcmdOK);   
        
        m_jpassword.addEditorKeys(m_jKeys);
        m_jpassword.reset();
        m_jpassword.activate();
        
        m_jPanelTitle.setBorder(RoundedBorder.createGradientBorder());

        m_sPassword = null;
    }
    
    private void setTitle(String title, String message, Icon icon) {
        setTitle(title);
        m_lblMessage.setText(message);
        m_lblMessage.setIcon(icon);
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
    
    public static String showEditPassword(Component parent, String title) {
        return showEditPassword(parent, title, null, null);
    }
    public static String showEditPassword(Component parent, String title, String message) {
        return showEditPassword(parent, title, message, null);
    }
    public static String showEditPassword(Component parent, String title, String message, Icon icon) {
        
        Window window = getWindow(parent);      
        
        JPasswordDialog myMsg;
        if (window instanceof Frame) { 
            myMsg = new JPasswordDialog((Frame) window, true);
        } else {
            myMsg = new JPasswordDialog((Dialog) window, true);
        }
        
        myMsg.setTitle(title, message, icon);
        myMsg.setVisible(true);
        return myMsg.m_sPassword;
    }

    private void initComponents() {

        btnsContainer = new javax.swing.JPanel();
        jcmdOK = WidgetsBuilder.createButton(ImageLoader.readImageIcon("button_ok.png"),
                AppLocal.getIntString("Button.OK"), WidgetsBuilder.SIZE_MEDIUM);
        jcmdCancel = WidgetsBuilder.createButton(ImageLoader.readImageIcon("button_cancel.png"),
                AppLocal.getIntString("Button.Cancel"),
                WidgetsBuilder.SIZE_MEDIUM);
        jPanel2 = new javax.swing.JPanel();
        jPanelGrid = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        m_jKeys = new fr.pasteque.pos.widgets.JEditorKeys();
        jPanel4 = new javax.swing.JPanel();
        m_jpassword = new fr.pasteque.pos.widgets.JEditorPassword();
        m_jPanelTitle = new javax.swing.JPanel();
        m_lblMessage = WidgetsBuilder.createLabel();

        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                closeWindow(evt);
            }
        });

        btnsContainer.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

        jcmdOK.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jcmdOKActionPerformed(evt);
            }
        });
        btnsContainer.add(jcmdOK);

        jcmdCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jcmdCancelActionPerformed(evt);
            }
        });
        btnsContainer.add(jcmdCancel);

        getContentPane().add(btnsContainer, java.awt.BorderLayout.SOUTH);

        jPanel2.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        jPanel2.setLayout(new java.awt.BorderLayout());

        jPanel3.setLayout(new javax.swing.BoxLayout(jPanel3, javax.swing.BoxLayout.Y_AXIS));
        jPanel3.add(m_jKeys);

        jPanel4.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        jPanel4.setLayout(new java.awt.BorderLayout());
        jPanel4.add(m_jpassword, java.awt.BorderLayout.CENTER);

        jPanel3.add(jPanel4);

        jPanelGrid.add(jPanel3);

        jPanel2.add(jPanelGrid, java.awt.BorderLayout.CENTER);

        getContentPane().add(jPanel2, java.awt.BorderLayout.CENTER);

        m_jPanelTitle.setLayout(new java.awt.BorderLayout());

        m_lblMessage.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, java.awt.Color.darkGray), javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        m_jPanelTitle.add(m_lblMessage, java.awt.BorderLayout.CENTER);

        getContentPane().add(m_jPanelTitle, java.awt.BorderLayout.NORTH);

        java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        int minWidth = 256;
        int minHeight = 360;
        int width = WidgetsBuilder.dipToPx(200);
        int height = WidgetsBuilder.dipToPx(320);
        width = Math.max(minWidth, width);
        height = Math.max(minHeight, height);
        setBounds((screenSize.width-width)/2, (screenSize.height-height)/2,
                width, height);
    }

    private void jNumberKeys21KeyPerformed(fr.pasteque.beans.JNumberEvent evt) {//GEN-FIRST:event_jNumberKeys21KeyPerformed
 
    }//GEN-LAST:event_jNumberKeys21KeyPerformed

    private void jcmdOKActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jcmdOKActionPerformed
          
        m_sPassword = m_jpassword.getPassword(); 
        setVisible(false);
        dispose();     
        
    }//GEN-LAST:event_jcmdOKActionPerformed

    private void jcmdCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jcmdCancelActionPerformed

        setVisible(false);
        dispose();    
        
    }//GEN-LAST:event_jcmdCancelActionPerformed

    private void closeWindow(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_closeWindow

        setVisible(false);
        dispose();
        
    }//GEN-LAST:event_closeWindow
       
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel btnsContainer;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanelGrid;
    private javax.swing.JButton jcmdCancel;
    private javax.swing.JButton jcmdOK;
    private fr.pasteque.pos.widgets.JEditorKeys m_jKeys;
    private javax.swing.JPanel m_jPanelTitle;
    private fr.pasteque.pos.widgets.JEditorPassword m_jpassword;
    private javax.swing.JLabel m_lblMessage;
    // End of variables declaration//GEN-END:variables
    
}
