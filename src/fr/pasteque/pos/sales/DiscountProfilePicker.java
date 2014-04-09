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

package fr.pasteque.pos.sales;

import fr.pasteque.basic.BasicException;
import fr.pasteque.data.loader.ImageLoader;
import fr.pasteque.pos.catalog.DiscountProfileCatalog;
import fr.pasteque.pos.customers.DiscountProfile;
import fr.pasteque.pos.forms.AppConfig;
import fr.pasteque.pos.forms.AppLocal;
import fr.pasteque.pos.widgets.JEditorDouble;
import fr.pasteque.pos.widgets.JEditorKeys;
import fr.pasteque.pos.widgets.WidgetsBuilder;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public class DiscountProfilePicker extends javax.swing.JDialog {

    private DiscountProfile selectedProfile;
    private double selectedRate;

    private DiscountProfilePicker(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
    }

    private DiscountProfilePicker(java.awt.Dialog parent, boolean modal) {
        super(parent, modal);
    }

    public static DiscountProfilePicker getDiscountProfilePicker(Component parent) {
        Window window = SwingUtilities.getWindowAncestor(parent);
        DiscountProfilePicker myMsg;
        if (window instanceof Frame) {
            myMsg = new DiscountProfilePicker((Frame) window, true);
        } else {
            myMsg = new DiscountProfilePicker((Dialog) window, true);
        }
        myMsg.init();
        myMsg.applyComponentOrientation(parent.getComponentOrientation());
        return myMsg;
    }

    private void init() {
        initComponents();
        DiscountProfileCatalog cat = new DiscountProfileCatalog();
        cat.addActionListener(new CatalogListener());
        this.catalogContainer.add(cat.getComponent());
        this.rateField.addEditorKeys(m_jKeys);
        this.rateField.activate();
        getRootPane().setDefaultButton(m_jButtonOK);
    }

    public DiscountProfile getSelectedProfile() {
        return this.selectedProfile;
    }

    protected class CatalogListener implements ActionListener {
        private void reloadCatalog () {}

        public void actionPerformed(ActionEvent e) {
            if (e.getSource() instanceof DiscountProfile) {
                DiscountProfile profile = ((DiscountProfile) e.getSource());
                selectedProfile = profile;
                DiscountProfilePicker.this.dispose();
            }
        }
    }

    private void initComponents() {
        AppConfig cfg = AppConfig.loadedInstance;
        int btnSpacing = WidgetsBuilder.pixelSize(Float.parseFloat(cfg.getProperty("ui.touchbtnspacing")));

        m_jButtonOK = WidgetsBuilder.createButton(ImageLoader.readImageIcon("button_ok.png"),
                AppLocal.getIntString("Button.OK"),
                WidgetsBuilder.SIZE_MEDIUM);
        m_jButtonCancel = WidgetsBuilder.createButton(ImageLoader.readImageIcon("button_cancel.png"),
                AppLocal.getIntString("Button.Cancel"),
                WidgetsBuilder.SIZE_MEDIUM);
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle(AppLocal.getIntString("Form.DiscountProfile"));

        java.awt.Container container = this.getContentPane();
        container.setLayout(new GridBagLayout());
        GridBagConstraints cstr;

        this.rateField = new JEditorDouble();
        JLabel rateLabel = WidgetsBuilder.createLabel(AppLocal.getIntString("Label.discountRate"));
        cstr = new GridBagConstraints();
        cstr.gridx = 0;
        cstr.gridy = 0;
        container.add(rateLabel, cstr);
        cstr = new GridBagConstraints();
        cstr.gridx = 1;
        cstr.gridy = 0;
        cstr.insets = new Insets(btnSpacing, btnSpacing,
                btnSpacing, btnSpacing);
        cstr.fill = GridBagConstraints.HORIZONTAL;
        container.add(this.rateField, cstr);

        m_jKeys = new JEditorKeys();

        cstr = new GridBagConstraints();
        cstr.gridx = 2;
        cstr.gridy = 0;
        cstr.gridheight = 2;
        container.add(m_jKeys, cstr);

        this.catalogContainer = new JPanel();
        this.catalogContainer.setLayout(new java.awt.BorderLayout());
        cstr = new GridBagConstraints();
        cstr.gridx = 0;
        cstr.gridy = 1;
        cstr.gridwidth = 2;
        cstr.fill = GridBagConstraints.BOTH;
        container.add(this.catalogContainer, cstr);

        JPanel buttonsContainer = new JPanel();
        m_jButtonOK.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_jButtonOKActionPerformed(evt);
            }
        });
        buttonsContainer.add(m_jButtonOK);

        m_jButtonCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_jButtonCancelActionPerformed(evt);
            }
        });
        buttonsContainer.add(m_jButtonCancel);

        cstr = new GridBagConstraints();
        cstr.gridx = 0;
        cstr.gridy = 2;
        cstr.gridwidth = 3;
        cstr.fill = GridBagConstraints.HORIZONTAL;
        cstr.anchor = GridBagConstraints.LINE_END;
        container.add(buttonsContainer, cstr);

        java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        setBounds((screenSize.width-609)/2, (screenSize.height-388)/2, 609, 388);
    }

    private void m_jButtonOKActionPerformed(java.awt.event.ActionEvent evt) {
        double rate = this.rateField.getDoubleValue();
        rate = rate / 100;
        this.selectedProfile = new DiscountProfile(rate);
        dispose();
    }

    private void m_jButtonCancelActionPerformed(ActionEvent evt) {
        dispose();
    }

    private javax.swing.JButton m_jButtonCancel;
    private javax.swing.JButton m_jButtonOK;
    private JPanel catalogContainer;
    private JEditorKeys m_jKeys;
    private JEditorDouble rateField;

}
