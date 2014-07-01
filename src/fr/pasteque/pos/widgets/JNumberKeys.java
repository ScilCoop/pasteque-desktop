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

package fr.pasteque.pos.widgets;

import java.awt.ComponentOrientation;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.*;

import javax.swing.JButton;
import javax.swing.JComponent;

import fr.pasteque.data.loader.ImageLoader;
import fr.pasteque.pos.forms.AppConfig;

public class JNumberKeys extends javax.swing.JPanel {

    private Vector m_Listeners = new Vector();
    
    private boolean minusenabled = true;
    private boolean equalsenabled = true;
    
    private GridBagConstraints gbc= new GridBagConstraints();
    private GridBagLayout layout = new java.awt.GridBagLayout();
    
    /** Creates new form JNumberKeys */
    public JNumberKeys() {
        initComponents ();
        
        m_jKey0.addActionListener(new MyKeyNumberListener('0'));
        m_jKey1.addActionListener(new MyKeyNumberListener('1'));
        m_jKey2.addActionListener(new MyKeyNumberListener('2'));
        m_jKey3.addActionListener(new MyKeyNumberListener('3'));
        m_jKey4.addActionListener(new MyKeyNumberListener('4'));
        m_jKey5.addActionListener(new MyKeyNumberListener('5'));
        m_jKey6.addActionListener(new MyKeyNumberListener('6'));
        m_jKey7.addActionListener(new MyKeyNumberListener('7'));
        m_jKey8.addActionListener(new MyKeyNumberListener('8'));
        m_jKey9.addActionListener(new MyKeyNumberListener('9'));
        m_jKeyDot.addActionListener(new MyKeyNumberListener('.'));
        m_jMultiply.addActionListener(new MyKeyNumberListener('*'));
        m_jCE.addActionListener(new MyKeyNumberListener('\u007f'));
        m_jPlus.addActionListener(new MyKeyNumberListener('+'));
        m_jMinus.addActionListener(new MyKeyNumberListener('-'));
        m_jEquals.addActionListener(new MyKeyNumberListener('='));
        m_jBack.addActionListener(new MyKeyNumberListener('\u0008'));
    }

    public void setNumbersOnly(boolean value) {
        m_jEquals.setVisible(value);
        m_jMinus.setVisible(value);
        m_jPlus.setVisible(value);
        m_jMultiply.setVisible(value);
    }
    
    @Override
    public void setEnabled(boolean b) {
        super.setEnabled(b);
        
        m_jKey0.setEnabled(b);
        m_jKey1.setEnabled(b);
        m_jKey2.setEnabled(b);
        m_jKey3.setEnabled(b);
        m_jKey4.setEnabled(b);
        m_jKey5.setEnabled(b);
        m_jKey6.setEnabled(b);
        m_jKey7.setEnabled(b);
        m_jKey8.setEnabled(b);
        m_jKey9.setEnabled(b);
        m_jKeyDot.setEnabled(b);
        m_jMultiply.setEnabled(b);
        m_jCE.setEnabled(b);
        m_jPlus.setEnabled(b);
        m_jMinus.setEnabled(minusenabled && b);
        m_jEquals.setEnabled(equalsenabled && b);
        m_jBack.setEnabled(b);
    }
    
    @Override
    public void setComponentOrientation(ComponentOrientation o) {
        // Nothing to change
    }
    
    public void setMinusEnabled(boolean b) {
        minusenabled = b;
        m_jMinus.setEnabled(minusenabled && isEnabled());
    }
    
    public boolean isMinusEnabled() {
        return minusenabled;
    }
    
    public void setEqualsEnabled(boolean b) {
        equalsenabled = b;
        m_jEquals.setEnabled(equalsenabled && isEnabled());
    }
    
    public boolean isEqualsEnabled() {
        return equalsenabled;
    }

    
    public boolean isNumbersOnly() {
        return m_jEquals.isVisible();
    }
    
    public void addJNumberEventListener(JNumberEventListener listener) {
        m_Listeners.add(listener);
    }
    public void removeJNumberEventListener(JNumberEventListener listener) {
        m_Listeners.remove(listener);
    }
    
    private class MyKeyNumberListener implements java.awt.event.ActionListener {
        
        private char m_cCad;
        
        public MyKeyNumberListener(char cCad){
            m_cCad = cCad;
        }
        public void actionPerformed(java.awt.event.ActionEvent evt) {
           
            JNumberEvent oEv = new JNumberEvent(JNumberKeys.this, m_cCad);            
            JNumberEventListener oListener;
            
            for (Enumeration e = m_Listeners.elements(); e.hasMoreElements();) {
                oListener = (JNumberEventListener) e.nextElement();
                oListener.keyPerformed(oEv);
            }
        }
    }


    private void initComponents() {
        AppConfig cfg = AppConfig.loadedInstance;
        int btnspacing = WidgetsBuilder.pixelSize(Float.parseFloat(cfg.getProperty("ui.touchbtnspacing"))) / 2;
        gbc.insets = new java.awt.Insets(btnspacing, btnspacing,
                btnspacing, btnspacing);
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx= 1.0;
        gbc.weighty= 1.0;

        m_jBack = WidgetsBuilder.createButton(ImageLoader.readImageIcon("kpad_back.png"), WidgetsBuilder.SIZE_BIG);
        m_jCE = WidgetsBuilder.createButton(ImageLoader.readImageIcon("kpad_ce.png"), WidgetsBuilder.SIZE_BIG);
        m_jMultiply = WidgetsBuilder.createButton(ImageLoader.readImageIcon("kpad_mult.png"), WidgetsBuilder.SIZE_BIG);
        m_jMinus = WidgetsBuilder.createButton(ImageLoader.readImageIcon("kpad_minus.png"), WidgetsBuilder.SIZE_BIG);
        m_jPlus = WidgetsBuilder.createButton(ImageLoader.readImageIcon("kpad_plus.png"), WidgetsBuilder.SIZE_BIG);
        m_jKey9 = WidgetsBuilder.createButton(ImageLoader.readImageIcon("kpad_9.png"), WidgetsBuilder.SIZE_BIG);
        m_jKey8 = WidgetsBuilder.createButton(ImageLoader.readImageIcon("kpad_8.png"), WidgetsBuilder.SIZE_BIG);
        m_jKey7 = WidgetsBuilder.createButton(ImageLoader.readImageIcon("kpad_7.png"), WidgetsBuilder.SIZE_BIG);
        m_jKey4 = WidgetsBuilder.createButton(ImageLoader.readImageIcon("kpad_4.png"), WidgetsBuilder.SIZE_BIG);
        m_jKey5 = WidgetsBuilder.createButton(ImageLoader.readImageIcon("kpad_5.png"), WidgetsBuilder.SIZE_BIG);
        m_jKey6 = WidgetsBuilder.createButton(ImageLoader.readImageIcon("kpad_6.png"), WidgetsBuilder.SIZE_BIG);
        m_jKey3 = WidgetsBuilder.createButton(ImageLoader.readImageIcon("kpad_3.png"), WidgetsBuilder.SIZE_BIG);
        m_jKey2 = WidgetsBuilder.createButton(ImageLoader.readImageIcon("kpad_2.png"), WidgetsBuilder.SIZE_BIG);
        m_jKey1 = WidgetsBuilder.createButton(ImageLoader.readImageIcon("kpad_1.png"), WidgetsBuilder.SIZE_BIG);
        m_jKey0 = WidgetsBuilder.createButton(ImageLoader.readImageIcon("kpad_0.png"), WidgetsBuilder.SIZE_BIG);
        m_jKeyDot = WidgetsBuilder.createButton(ImageLoader.readImageIcon("kpad_dot.png"), WidgetsBuilder.SIZE_BIG);
        m_jEquals = WidgetsBuilder.createButton(ImageLoader.readImageIcon("encaisser.png"), WidgetsBuilder.SIZE_BIG);

        setLayout(layout);
        
        gbc.gridx=0;
        addButton(m_jKey7, 0);
        addButton(m_jKey4, 1);
        addButton(m_jKey1, 2);
        addButton(m_jKey0, 3);
        
        gbc.gridx=1;
        addButton(m_jKey8, 0);
        addButton(m_jKey5, 1);
        addButton(m_jKey2, 2);
        addButton(m_jKeyDot, 3);
        
        gbc.gridx=2;
        addButton(m_jKey9, 0);
        addButton(m_jKey6, 1);
        addButton(m_jKey3, 2);
        addButton(m_jBack, 3);
        
        gbc.gridx=3;
        addButton(m_jPlus, 0);
        addButton(m_jMultiply, 1);

        gbc.gridx=4;
        addButton(m_jMinus, 0);
        addButton(m_jCE, 1);

        gbc.gridx=3;
        gbc.gridheight=2;
        gbc.gridwidth=2;
        gbc.weightx = 2.0;
        gbc.weighty = 2.0;
        addButton(m_jEquals, 2);
        
        
        // Force maximum size to preferred to avoid keyboard from stretching
        // in dynamic layouts
        
        
        this.setMaximumSize(this.getPreferredSize());
    }
    
    /** add Button */
    private void addButton(JButton button, int ligne){
    	button.setFocusPainted(false);
        button.setFocusable(false);
        button.setMargin(new java.awt.Insets(8, 16, 8, 16));
        button.setRequestFocusEnabled(false);
        button.setFont(new Font(Font.DIALOG, Font.BOLD, 14));
        gbc.gridy = ligne;
        layout.setConstraints(button, gbc);
        add(button);
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton m_jBack;
    private javax.swing.JButton m_jCE;
    private javax.swing.JButton m_jEquals;
    private javax.swing.JButton m_jKey0;
    private javax.swing.JButton m_jKey1;
    private javax.swing.JButton m_jKey2;
    private javax.swing.JButton m_jKey3;
    private javax.swing.JButton m_jKey4;
    private javax.swing.JButton m_jKey5;
    private javax.swing.JButton m_jKey6;
    private javax.swing.JButton m_jKey7;
    private javax.swing.JButton m_jKey8;
    private javax.swing.JButton m_jKey9;
    private javax.swing.JButton m_jKeyDot;
    private javax.swing.JButton m_jMinus;
    private javax.swing.JButton m_jMultiply;
    private javax.swing.JButton m_jPlus;
    // End of variables declaration//GEN-END:variables

}
