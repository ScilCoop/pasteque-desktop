//    POS-Tech
//    Based upon Openbravo POS
//
//    Copyright (C) 2007-2009 Openbravo, S.L.
//                       2012 Scil (http://scil.coop)
//    Cédric Houbart
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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.EventListener;
import javax.swing.event.EventListenerList;
import fr.pasteque.data.loader.ImageLoader;
import fr.pasteque.pos.forms.AppConfig;

public class JEditorKeys extends javax.swing.JPanel implements EditorKeys {

    private final static char[] SET_DOUBLE = {'\u007f', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '.', '-'};
    private final static char[] SET_DOUBLE_POSITIVE = {'\u007f', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '.'};
    private final static char[] SET_INTEGER = {'\u007f', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '-'};
    private final static char[] SET_INTEGER_POSITIVE = {'\u007f', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};

    protected EventListenerList listeners = new EventListenerList();

    private EditorComponent editorcurrent ;

    private char[] keysavailable;
    private boolean m_bMinus;
    private boolean m_bKeyDot;

    /** Creates new form JEditorKeys */
    public JEditorKeys() {
        initComponents();

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
        m_jCE.addActionListener(new MyKeyNumberListener('\u007f'));
        m_jMinus.addActionListener(new MyKeyNumberListener('-'));
//        m_jBack.addActionListener(new MyKeyNumberListener('\u0008'));
//        m_jMode.addActionListener(new MyKeyNumberListener('\u0010'));

        editorcurrent = null;
        setMode(MODE_STRING);
        doEnabled(false);
    }

    @Override
    public void setComponentOrientation(ComponentOrientation o) {
        // Nothing to change
    }

    public void addActionListener(ActionListener l) {
        listeners.add(ActionListener.class, l);
    }
    public void removeActionListener(ActionListener l) {
        listeners.remove(ActionListener.class, l);
    }

    protected void fireActionPerformed() {
        EventListener[] l = listeners.getListeners(ActionListener.class);
        ActionEvent e = null;
        for (int i = 0; i < l.length; i++) {
            if (e == null) {
                e = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, null);
            }
            ((ActionListener) l[i]).actionPerformed(e);	
        }
    }

    private void doEnabled(boolean b) {
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
        m_jKeyDot.setEnabled(b && m_bKeyDot);
        m_jCE.setEnabled(b);
        m_jMinus.setEnabled(b && m_bMinus);
    }

    public void setMode(int iMode) {
        switch (iMode) {
            case MODE_DOUBLE:
                m_bMinus = true;
                m_bKeyDot = true;
                keysavailable = SET_DOUBLE;
                break;
            case MODE_DOUBLE_POSITIVE:
                m_bMinus = false;
                m_bKeyDot = true;
                keysavailable = SET_DOUBLE_POSITIVE;
                break;
            case MODE_INTEGER:
                m_bMinus = true;
                m_bKeyDot = false;
                keysavailable = SET_INTEGER;
                break;
            case MODE_INTEGER_POSITIVE:
                m_bMinus = false;
                m_bKeyDot = false;
                keysavailable = SET_INTEGER_POSITIVE;
                break;
            case MODE_STRING:
            default:
                m_bMinus = true;
                m_bKeyDot = true;
                keysavailable = null;
                break;
        }
    }

    private class MyKeyNumberListener implements java.awt.event.ActionListener {

        private char m_cCad;

        public MyKeyNumberListener(char cCad){
            m_cCad = cCad;
        }
        public void actionPerformed(java.awt.event.ActionEvent evt) {

            // como contenedor de editores
            if (editorcurrent != null) {
                editorcurrent.transChar(m_cCad);
            }
        }
    }

    public void setActive(EditorComponent e, int iMode) {

        if (editorcurrent != null) {
            editorcurrent.deactivate();
        }
        editorcurrent = e;  // e != null
        setMode(iMode);
        doEnabled(true);

        // keyboard listener activation
        m_txtKeys.setText(null);
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                m_txtKeys.requestFocus();
            }
        });
    }

    public void setInactive(EditorComponent e) {

        if (e == editorcurrent && editorcurrent != null) { // e != null
            editorcurrent.deactivate();
            editorcurrent = null;
            setMode(MODE_STRING);
            doEnabled(false);
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
    	AppConfig cfg = AppConfig.loadedInstance;
        java.awt.GridBagConstraints gridBagConstraints;
        int btnspacing = WidgetsBuilder.pixelSize(Float.parseFloat(cfg.getProperty("ui.touchbtnspacing")));

        m_jKey0 = WidgetsBuilder.createButton(ImageLoader.readImageIcon("kpad_0.png"), WidgetsBuilder.SIZE_BIG);
        m_jKey1 = WidgetsBuilder.createButton(ImageLoader.readImageIcon("kpad_1.png"), WidgetsBuilder.SIZE_BIG);
        m_jKey4 = WidgetsBuilder.createButton(ImageLoader.readImageIcon("kpad_4a.png"), WidgetsBuilder.SIZE_BIG);
        m_jKey7 = WidgetsBuilder.createButton(ImageLoader.readImageIcon("kpad_7a.png"), WidgetsBuilder.SIZE_BIG);
        m_jCE = WidgetsBuilder.createButton(ImageLoader.readImageIcon("kpad_ce.png"), WidgetsBuilder.SIZE_BIG);
        m_jMinus = WidgetsBuilder.createButton(ImageLoader.readImageIcon("kpad_minus.png"), WidgetsBuilder.SIZE_BIG);
        m_jKey9 = WidgetsBuilder.createButton(ImageLoader.readImageIcon("kpad_9a.png"), WidgetsBuilder.SIZE_BIG);
        m_jKey8 = WidgetsBuilder.createButton(ImageLoader.readImageIcon("kpad_8a.png"), WidgetsBuilder.SIZE_BIG);
        m_jKey5 = WidgetsBuilder.createButton(ImageLoader.readImageIcon("kpad_5a.png"), WidgetsBuilder.SIZE_BIG);
        m_jKey6 = WidgetsBuilder.createButton(ImageLoader.readImageIcon("kpad_6a.png"), WidgetsBuilder.SIZE_BIG);
        m_jKey3 = WidgetsBuilder.createButton(ImageLoader.readImageIcon("kpad_3a.png"), WidgetsBuilder.SIZE_BIG);
        m_jKey2 = WidgetsBuilder.createButton(ImageLoader.readImageIcon("kpad_2a.png"), WidgetsBuilder.SIZE_BIG);
        m_jKeyDot = WidgetsBuilder.createButton(ImageLoader.readImageIcon("kpad_dot.png"), WidgetsBuilder.SIZE_BIG);
        m_txtKeys = new javax.swing.JTextField();

        setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        setLayout(new java.awt.GridBagLayout());

        m_jKey0.setFocusPainted(false);
        m_jKey0.setFocusable(false);
        m_jKey0.setRequestFocusEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(btnspacing, 0, 0, 0);
        add(m_jKey0, gridBagConstraints);

        m_jKey1.setFocusPainted(false);
        m_jKey1.setFocusable(false);
        m_jKey1.setRequestFocusEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(btnspacing, 0, 0, 0);
        add(m_jKey1, gridBagConstraints);

        m_jKey4.setFocusPainted(false);
        m_jKey4.setFocusable(false);
        m_jKey4.setRequestFocusEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(btnspacing, 0, 0, 0);
        add(m_jKey4, gridBagConstraints);

        m_jKey7.setFocusPainted(false);
        m_jKey7.setFocusable(false);
        m_jKey7.setRequestFocusEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(btnspacing, 0, 0, 0);
        add(m_jKey7, gridBagConstraints);

        m_jCE.setFocusPainted(false);
        m_jCE.setFocusable(false);
        m_jCE.setRequestFocusEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(m_jCE, gridBagConstraints);

        m_jMinus.setFocusPainted(false);
        m_jMinus.setFocusable(false);
        m_jMinus.setRequestFocusEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, btnspacing, 0, 0);
        add(m_jMinus, gridBagConstraints);

        m_jKey9.setFocusPainted(false);
        m_jKey9.setFocusable(false);
        m_jKey9.setRequestFocusEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(btnspacing, btnspacing, 0, 0);
        add(m_jKey9, gridBagConstraints);

        m_jKey8.setFocusPainted(false);
        m_jKey8.setFocusable(false);
        m_jKey8.setRequestFocusEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(btnspacing, btnspacing, 0, 0);
        add(m_jKey8, gridBagConstraints);

        m_jKey5.setFocusPainted(false);
        m_jKey5.setFocusable(false);
        m_jKey5.setRequestFocusEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(btnspacing, btnspacing, 0, 0);
        add(m_jKey5, gridBagConstraints);

        m_jKey6.setFocusPainted(false);
        m_jKey6.setFocusable(false);
        m_jKey6.setRequestFocusEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(btnspacing, btnspacing, 0, 0);
        add(m_jKey6, gridBagConstraints);

        m_jKey3.setFocusPainted(false);
        m_jKey3.setFocusable(false);
        m_jKey3.setRequestFocusEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(btnspacing, btnspacing, 0, 0);
        add(m_jKey3, gridBagConstraints);

        m_jKey2.setFocusPainted(false);
        m_jKey2.setFocusable(false);
        m_jKey2.setRequestFocusEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(btnspacing, btnspacing, 0, 0);
        add(m_jKey2, gridBagConstraints);

        m_jKeyDot.setFocusPainted(false);
        m_jKeyDot.setFocusable(false);
        m_jKeyDot.setRequestFocusEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(btnspacing, btnspacing, 0, 0);
        add(m_jKeyDot, gridBagConstraints);

        m_txtKeys.setPreferredSize(new java.awt.Dimension(0, 0));
        m_txtKeys.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                m_txtKeysFocusLost(evt);
            }
        });
        m_txtKeys.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                m_txtKeysKeyTyped(evt);
            }
        });
        add(m_txtKeys, new java.awt.GridBagConstraints());
    }// </editor-fold>//GEN-END:initComponents

    private void m_txtKeysKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_m_txtKeysKeyTyped

        // como contenedor de editores solo
        if (editorcurrent != null) {
            m_txtKeys.setText("0");

            // solo lo lanzamos si esta dentro del set de teclas
            char c = evt.getKeyChar();
            if (c == '\n') {
                fireActionPerformed();
            } else {
                if (keysavailable == null) {
                    // todo disponible
                    editorcurrent.typeChar(c);
                } else {
                    for (int i = 0; i < keysavailable.length; i++) {
                        if (c == keysavailable[i]) {
                            // todo disponible
                            editorcurrent.typeChar(c);
                        }
                    }
                }
            }
        }

    }//GEN-LAST:event_m_txtKeysKeyTyped

    private void m_txtKeysFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_m_txtKeysFocusLost

        setInactive(editorcurrent);

    }//GEN-LAST:event_m_txtKeysFocusLost


    // Variables declaration - do not modify//GEN-BEGIN:variables
    javax.swing.JButton m_jCE;
    javax.swing.JButton m_jKey0;
    javax.swing.JButton m_jKey1;
    javax.swing.JButton m_jKey2;
    javax.swing.JButton m_jKey3;
    javax.swing.JButton m_jKey4;
    javax.swing.JButton m_jKey5;
    javax.swing.JButton m_jKey6;
    javax.swing.JButton m_jKey7;
    javax.swing.JButton m_jKey8;
    javax.swing.JButton m_jKey9;
    javax.swing.JButton m_jKeyDot;
    javax.swing.JButton m_jMinus;
    javax.swing.JTextField m_txtKeys;
    // End of variables declaration//GEN-END:variables

}
