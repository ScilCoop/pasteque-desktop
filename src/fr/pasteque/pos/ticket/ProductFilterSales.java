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
import fr.pasteque.pos.forms.AppConfig;
import fr.pasteque.pos.forms.AppLocal;
import fr.pasteque.pos.forms.DataLogicSales;
import fr.pasteque.pos.widgets.JEditorKeys;
import fr.pasteque.pos.widgets.JEditorString;
import fr.pasteque.pos.widgets.WidgetsBuilder;

public class ProductFilterSales extends javax.swing.JPanel {
    
    private ComboBoxValModel m_CategoryModel;
    
    /** Creates new form ProductFilterSales */
    public ProductFilterSales(DataLogicSales dlSales, JEditorKeys jKeys) {
        initComponents();
        m_jtxtName.addEditorKeys(jKeys);
        m_jtxtRef.addEditorKeys(jKeys);
    }
    
    public void activate() {
        m_jtxtName.reset();
        m_jtxtName.activate();
        m_jtxtRef.reset();
    }

    /** Get input values. [0] is name, [1] is category, null if not defined. */
    public String[] getFilter() throws BasicException {
        String[] afilter = new String[2];
        // Name
        if (m_jtxtName.getText() == null || m_jtxtName.getText().equals("")) {
            afilter[0] = null;
        } else {
            afilter[0] = m_jtxtName.getText();
        }
        // Ref
        if (m_jtxtRef.getText() == null || m_jtxtRef.getText().equals("")) {
            afilter[1] = null;
        } else {
            afilter[1] = m_jtxtRef.getText();
        }
        return afilter;
    } 
    
    private void initComponents() {
        AppConfig cfg = AppConfig.loadedInstance;
        int btnspacing = WidgetsBuilder.pixelSize(Float.parseFloat(cfg.getProperty("ui.touchbtnspacing")));

        nameLbl = WidgetsBuilder.createLabel(AppLocal.getIntString("label.prodname"));
        m_jtxtName = new JEditorString();
        refLbl = WidgetsBuilder.createLabel(AppLocal.getIntString("label.prodref"));
        m_jtxtRef = new JEditorString();

        this.setLayout(new GridBagLayout());
        GridBagConstraints cstr;

        // Name
        cstr = new GridBagConstraints();
        cstr.gridx = 0;
        cstr.gridy = 0;
        cstr.anchor = GridBagConstraints.LINE_START;
        cstr.insets = new Insets(btnspacing, btnspacing,
                btnspacing, btnspacing);
        add(nameLbl, cstr);
        cstr = new GridBagConstraints();
        cstr.gridx = 1;
        cstr.gridy = 0;
        cstr.weightx = 1.0;
        cstr.fill = GridBagConstraints.HORIZONTAL;
        cstr.insets = new Insets(btnspacing, 0, btnspacing, btnspacing);
        add(m_jtxtName, cstr);

        // Reference
        cstr = new GridBagConstraints();
        cstr.gridx = 0;
        cstr.gridy = 1;
        cstr.anchor = GridBagConstraints.LINE_START;
        cstr.insets = new Insets(0, btnspacing, btnspacing, btnspacing);
        add(refLbl, cstr);
        cstr = new GridBagConstraints();
        cstr.gridx = 1;
        cstr.gridy = 1;
        cstr.weightx = 1.0;
        cstr.fill = GridBagConstraints.HORIZONTAL;
        cstr.insets = new Insets(0, 0, btnspacing, btnspacing);
        add(m_jtxtRef, cstr);
    }

    private javax.swing.JLabel refLbl;
    private javax.swing.JLabel nameLbl;
    private JEditorString m_jtxtRef;
    private JEditorString m_jtxtName;
}
