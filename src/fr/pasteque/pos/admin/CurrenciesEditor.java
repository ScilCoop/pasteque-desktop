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

package fr.pasteque.pos.admin;

import fr.pasteque.pos.forms.AppLocal;
import fr.pasteque.format.Formats;
import fr.pasteque.basic.BasicException;
import fr.pasteque.data.gui.ComboBoxValModel;
import fr.pasteque.data.gui.JMessageDialog;
import fr.pasteque.data.gui.MessageInf;
import fr.pasteque.data.loader.SentenceExec;
import fr.pasteque.data.loader.SentenceList;
import fr.pasteque.data.user.EditorRecord;
import fr.pasteque.data.user.DirtyManager;
import fr.pasteque.pos.forms.AppView;
import fr.pasteque.pos.forms.DataLogicSales;

import java.awt.Component;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class CurrenciesEditor extends JPanel implements EditorRecord {
       
    private Object m_id;
    private boolean main;
    private DirtyManager dirty;
    
    /** Creates new form JPanelCategories */
    public CurrenciesEditor(AppView app, DirtyManager dirty) {
        
        DataLogicSales dlSales = (DataLogicSales) app.getBean("fr.pasteque.pos.forms.DataLogicSales");
             
        initComponents();
             
        nameField.getDocument().addDocumentListener(dirty);
        symbolField.getDocument().addDocumentListener(dirty);
        decimalField.getDocument().addDocumentListener(dirty);
        thousandsField.getDocument().addDocumentListener(dirty);
        formatField.getDocument().addDocumentListener(dirty);
        rateField.getDocument().addDocumentListener(dirty);
        this.dirty = dirty;

        writeValueEOF();
    }
    
    public void refresh() {
    }
    
    public void writeValueEOF() {
        m_id = null;
        nameField.setText(null);
        nameField.setEnabled(false);
        symbolField.setText(null);
        symbolField.setEnabled(false);
        decimalField.setText(null);
        decimalField.setEnabled(false);
        thousandsField.setText(null);
        thousandsField.setEnabled(false);
        formatField.setText(null);
        formatField.setEnabled(false);
        rateField.setText(null);
        rateField.setEnabled(false);
        makeMainBtn.setEnabled(false);
    }
    public void writeValueInsert() {
        m_id = (int) (System.currentTimeMillis() / 100);
        nameField.setText(null);
        nameField.setEnabled(true);
        symbolField.setText(null);
        symbolField.setEnabled(true);
        decimalField.setText(null);
        decimalField.setEnabled(true);
        thousandsField.setText(null);
        thousandsField.setEnabled(true);
        formatField.setText(null);
        formatField.setEnabled(true);
        rateField.setText(null);
        rateField.setEnabled(true);
        makeMainBtn.setEnabled(true);
        this.main = false;
    }
    public void writeValueDelete(Object value) {
        Object[] cat = (Object[]) value;
        m_id = cat[0];
        nameField.setText(Formats.STRING.formatValue(cat[1]));
        nameField.setEnabled(false);
    }    
    public void writeValueEdit(Object value) {
        Object[] currency = (Object[]) value;
        m_id = currency[0];
        nameField.setText(Formats.STRING.formatValue(currency[1]));
        nameField.setEnabled(true);
        symbolField.setText(Formats.STRING.formatValue(currency[2]));
        symbolField.setEnabled(true);
        decimalField.setText(Formats.STRING.formatValue(currency[3]));
        decimalField.setEnabled(true);
        thousandsField.setText(Formats.STRING.formatValue(currency[4]));
        thousandsField.setEnabled(true);
        formatField.setText(Formats.STRING.formatValue(currency[5]));
        formatField.setEnabled(true);
        rateField.setText(Formats.DOUBLE.formatValue(currency[6]));
        rateField.setEnabled(true);
        makeMainBtn.setEnabled(!(Boolean) currency[7]);
        this.main = (Boolean) currency[7];
    }

    private String firstChar(String value) {
        if (value == null || value.length() == 0) {
            return null;
        } else {
            return value.substring(0, 1);
        }
    }

    public Object createValue() throws BasicException {
        Object[] currency = new Object[8];
        currency[0] = m_id;
        currency[1] = nameField.getText();
        currency[2] = firstChar(symbolField.getText());
        currency[3] = firstChar(decimalField.getText());
        currency[4] = firstChar(thousandsField.getText());
        currency[5] = formatField.getText();
        currency[6] = Formats.DOUBLE.parseValue(rateField.getText());
        currency[7] = main;
        return currency;
    }    
    
    public Component getComponent() {
        return this;
    }
    
    private void initComponents() {
        setLayout(null);

        // Name
        JLabel nameLbl = new JLabel();
        nameField = new javax.swing.JTextField();
        nameLbl.setText(AppLocal.getIntString("Label.Name"));
        add(nameLbl);
        nameLbl.setBounds(20, 20, 80, 14);
        add(nameField);
        nameField.setBounds(100, 20, 180, 18);

        // Symbol
        JLabel symbolLbl = new JLabel();
        symbolField = new javax.swing.JTextField();
        symbolLbl.setText(AppLocal.getIntString("Label.Symbol"));
        add(symbolLbl);
        symbolLbl.setBounds(20, 50, 80, 14);
        add(symbolField);
        symbolField.setBounds(100, 50, 180, 18);

        // Decimal
        JLabel decimalLbl = new JLabel();
        decimalField = new javax.swing.JTextField();
        decimalLbl.setText(AppLocal.getIntString("Label.DecimalSeparator"));
        add(decimalLbl);
        decimalLbl.setBounds(20, 80, 80, 14);
        add(decimalField);
        decimalField.setBounds(100, 80, 180, 18);

        // Thousands
        JLabel thousandsLbl = new JLabel();
        thousandsField = new javax.swing.JTextField();
        thousandsLbl.setText(AppLocal.getIntString("Label.ThousandsSeparator"));
        add(thousandsLbl);
        thousandsLbl.setBounds(20, 110, 80, 14);
        add(thousandsField);
        thousandsField.setBounds(100, 110, 180, 18);

        // Format
        JLabel formatLbl = new JLabel();
        formatField = new javax.swing.JTextField();
        formatLbl.setText(AppLocal.getIntString("Label.Format"));
        add(formatLbl);
        formatLbl.setBounds(20, 140, 80, 14);
        add(formatField);
        formatField.setBounds(100, 140, 180, 18);

        // Rate
        JLabel rateLbl = new JLabel();
        rateField = new javax.swing.JTextField();
        rateLbl.setText(AppLocal.getIntString("Label.Rate"));
        add(rateLbl);
        rateLbl.setBounds(20, 170, 80, 14);
        add(rateField);
        rateField.setBounds(100, 170, 180, 18);

        // Main
        makeMainBtn = new JButton();
        makeMainBtn.setText(AppLocal.getIntString("Button.MakeMainCurrency"));
        makeMainBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                makeMain(evt);
            }
        });
        makeMainBtn.setBounds(20, 200, 260, 18);
        add(makeMainBtn);
    }

    private void makeMain(java.awt.event.ActionEvent evt) {
        this.main = true;
        this.makeMainBtn.setEnabled(false);
        this.dirty.setDirty(true);
    }

    private JTextField nameField;
    private JTextField symbolField;
    private JTextField decimalField;
    private JTextField thousandsField;
    private JTextField formatField;
    private JTextField rateField;
    private JButton makeMainBtn;
}
