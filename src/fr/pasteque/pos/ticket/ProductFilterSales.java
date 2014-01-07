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

package fr.pasteque.pos.ticket;

import java.awt.Insets;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.util.List;
import fr.pasteque.basic.BasicException;
import fr.pasteque.data.gui.ComboBoxValModel;
import fr.pasteque.data.gui.ListQBFModelNumber;
import fr.pasteque.data.loader.QBFCompareEnum;
import fr.pasteque.data.loader.SentenceList;
import fr.pasteque.data.user.EditorCreator;
import fr.pasteque.pos.forms.AppConfig;
import fr.pasteque.pos.widgets.JEditorKeys;
import fr.pasteque.pos.widgets.JEditorString;
import fr.pasteque.pos.forms.AppLocal;
import fr.pasteque.pos.forms.DataLogicSales;
import fr.pasteque.pos.widgets.JEditorCurrency;
import fr.pasteque.pos.widgets.JEditorKeys;
import fr.pasteque.pos.widgets.JEditorString;
import fr.pasteque.pos.widgets.WidgetsBuilder;

public class ProductFilterSales extends javax.swing.JPanel implements EditorCreator {
    
    private SentenceList m_sentcat;
    private ComboBoxValModel m_CategoryModel;
    
    /** Creates new form ProductFilterSales */
    public ProductFilterSales(DataLogicSales dlSales, JEditorKeys jKeys) {
        initComponents();
        
        // El modelo de categorias
        m_sentcat = dlSales.getCategoriesList();
        m_CategoryModel = new ComboBoxValModel();           
        
        m_jCboPriceBuy.setModel(ListQBFModelNumber.getMandatoryNumber());
        m_jPriceBuy.addEditorKeys(jKeys);
        
        m_jCboPriceSell.setModel(ListQBFModelNumber.getMandatoryNumber());
        m_jPriceSell.addEditorKeys(jKeys);
        
        m_jtxtName.addEditorKeys(jKeys);
        
        m_jtxtBarCode.addEditorKeys(jKeys);
    }
    
    public void activate() {
        
        m_jtxtBarCode.reset();
        m_jtxtBarCode.setEditModeEnum(JEditorString.MODE_123);
        m_jtxtName.reset();
        m_jPriceBuy.reset();
        m_jPriceSell.reset();
        m_jtxtName.activate();
        
        try {
            List catlist = m_sentcat.list();
            catlist.add(0, null);
            m_CategoryModel = new ComboBoxValModel(catlist);
            m_jCategory.setModel(m_CategoryModel);
        } catch (BasicException eD) {
            // no hay validacion
        }
    }
    
    public Object createValue() throws BasicException {
        
        Object[] afilter = new Object[10];
        
        // Nombre
        if (m_jtxtName.getText() == null || m_jtxtName.getText().equals("")) {
            afilter[0] = QBFCompareEnum.COMP_NONE;
            afilter[1] = null;
        } else {
            afilter[0] = QBFCompareEnum.COMP_RE;
            afilter[1] = "%" + m_jtxtName.getText() + "%";
        }
        
        // Precio de compra
        afilter[3] = m_jPriceBuy.getDoubleValue();
        afilter[2] = afilter[3] == null ? QBFCompareEnum.COMP_NONE : m_jCboPriceBuy.getSelectedItem();

        // Precio de venta
        afilter[5] = m_jPriceSell.getDoubleValue();
        afilter[4] = afilter[5] == null ? QBFCompareEnum.COMP_NONE : m_jCboPriceSell.getSelectedItem();
        
        // Categoria
        if (m_CategoryModel.getSelectedKey() == null) {
            afilter[6] = QBFCompareEnum.COMP_NONE;
            afilter[7] = null;
        } else {
            afilter[6] = QBFCompareEnum.COMP_EQUALS;
            afilter[7] = m_CategoryModel.getSelectedKey();
        }
        
        // el codigo de barras
        if (m_jtxtBarCode.getText() == null || m_jtxtBarCode.getText().equals("")) {
            afilter[8] = QBFCompareEnum.COMP_NONE;
            afilter[9] = null;
        } else{
            afilter[8] = QBFCompareEnum.COMP_RE;
            afilter[9] = "%" + m_jtxtBarCode.getText() + "%";
        }
        
        return afilter;
    } 
    
    private void initComponents() {
        AppConfig cfg = AppConfig.loadedInstance;
        int btnspacing = WidgetsBuilder.pixelSize(Float.parseFloat(cfg.getProperty("ui.touchbtnspacing")));
        int marginInset = 10;

        nameLbl = WidgetsBuilder.createLabel(AppLocal.getIntString("label.prodname"));
        m_jtxtName = new JEditorString();
        categoryLbl = WidgetsBuilder.createLabel(AppLocal.getIntString("label.prodcategory"));
        m_jCategory = new javax.swing.JComboBox();
        priceBuyLbl = WidgetsBuilder.createLabel(AppLocal.getIntString("label.prodpricebuy"));
        m_jCboPriceBuy = new javax.swing.JComboBox();
        m_jPriceBuy = new JEditorCurrency();
        priceSellLbl = WidgetsBuilder.createLabel(AppLocal.getIntString("label.prodpricesell"));
        m_jCboPriceSell = new javax.swing.JComboBox();
        m_jPriceSell = new JEditorCurrency();
        m_jtxtBarCode = new JEditorString();
        barcodeLbl = WidgetsBuilder.createLabel(AppLocal.getIntString("label.prodbarcode"));

        this.setLayout(new GridBagLayout());
        GridBagConstraints cstr;

        cstr = new GridBagConstraints();
        cstr.gridx = 0;
        cstr.gridy = 0;
        cstr.anchor = GridBagConstraints.LINE_START;
        cstr.insets = new Insets(marginInset, marginInset, btnspacing, marginInset);
        add(barcodeLbl, cstr);
        cstr = new GridBagConstraints();
        cstr.gridx = 1;
        cstr.gridy = 0;
        cstr.gridwidth = 2;
        cstr.fill = GridBagConstraints.HORIZONTAL;
        cstr.insets = new Insets(marginInset, 0, btnspacing, marginInset);
        add(m_jtxtBarCode, cstr);

        cstr = new GridBagConstraints();
        cstr.gridx = 0;
        cstr.gridy = 1;
        cstr.anchor = GridBagConstraints.LINE_START;
        cstr.insets = new Insets(0, marginInset, btnspacing, marginInset);
        add(nameLbl, cstr);
        cstr = new GridBagConstraints();
        cstr.gridx = 1;
        cstr.gridy = 1;
        cstr.gridwidth = 2;
        cstr.fill = GridBagConstraints.HORIZONTAL;
        cstr.insets = new Insets(0, 0, btnspacing, marginInset);
        add(m_jtxtName, cstr);

        cstr = new GridBagConstraints();
        cstr.gridx = 0;
        cstr.gridy = 2;
        cstr.anchor = GridBagConstraints.LINE_START;
        cstr.insets = new Insets(0, marginInset, btnspacing, marginInset);
        add(categoryLbl, cstr);
        cstr = new GridBagConstraints();
        cstr.gridx = 1;
        cstr.gridy = 2;
        cstr.gridwidth = 2;
        cstr.fill = GridBagConstraints.HORIZONTAL;
        cstr.insets = new Insets(0, 0, btnspacing, marginInset);
        add(m_jCategory, cstr);

        cstr = new GridBagConstraints();
        cstr.gridx = 0;
        cstr.gridy = 3;
        cstr.anchor = GridBagConstraints.LINE_START;
        cstr.insets = new Insets(0, marginInset, btnspacing, marginInset);
        add(priceBuyLbl, cstr);
        cstr = new GridBagConstraints();
        cstr.gridx = 1;
        cstr.gridy = 3;
        cstr.insets = new Insets(0, 0, btnspacing, btnspacing);
        add(m_jCboPriceBuy, cstr);
        cstr = new GridBagConstraints();
        cstr.gridx = 2;
        cstr.gridy = 3;
        cstr.fill = GridBagConstraints.HORIZONTAL;
        cstr.weightx = 1;
        cstr.insets = new Insets(0, 0, btnspacing, marginInset);
        add(m_jPriceBuy, cstr);
        
        cstr = new GridBagConstraints();
        cstr.gridx = 0;
        cstr.gridy = 4;
        cstr.anchor = GridBagConstraints.LINE_START;
        cstr.insets = new Insets(0, marginInset, marginInset, marginInset);
        add(priceSellLbl, cstr);
        cstr = new GridBagConstraints();
        cstr.gridx = 1;
        cstr.gridy = 4;
        cstr.insets = new Insets(0, 0, marginInset, btnspacing);
        add(m_jCboPriceSell, cstr);
        cstr = new GridBagConstraints();
        cstr.gridx = 2;
        cstr.gridy = 4;
        cstr.fill = GridBagConstraints.HORIZONTAL;
        cstr.weightx = 1;
        cstr.insets = new Insets(0, 0, marginInset, marginInset);
        add(m_jPriceSell, cstr);
    }
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel barcodeLbl;
    private javax.swing.JLabel categoryLbl;
    private javax.swing.JLabel priceSellLbl;
    private javax.swing.JLabel priceBuyLbl;
    private javax.swing.JLabel nameLbl;
    private javax.swing.JComboBox m_jCategory;
    private javax.swing.JComboBox m_jCboPriceBuy;
    private javax.swing.JComboBox m_jCboPriceSell;
    private JEditorCurrency m_jPriceBuy;
    private JEditorCurrency m_jPriceSell;
    private JEditorString m_jtxtBarCode;
    private JEditorString m_jtxtName;
    // End of variables declaration//GEN-END:variables
    
}
