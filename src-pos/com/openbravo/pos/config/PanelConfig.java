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

package com.openbravo.pos.config;

import java.awt.CardLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JPasswordField;
import com.openbravo.data.loader.ImageLoader;
import com.openbravo.data.user.DirtyManager;
import com.openbravo.pos.forms.AppConfig;
import com.openbravo.pos.forms.AppLocal;
import com.openbravo.pos.util.DirectoryEvent;
import com.openbravo.pos.widgets.WidgetsBuilder;

/**
 * Abstract configuration section
 */
public abstract class PanelConfig extends javax.swing.JPanel {

    protected DirtyManager dirty = new DirtyManager();

    public abstract void loadProperties(AppConfig config);
    public abstract void saveProperties(AppConfig config);
    
    protected int y;
    protected JPanel currentContainer;
    
    public PanelConfig() {
        super();
        this.setLayout(new GridBagLayout());
    }
    
    public Component getConfigComponent() {
        return this;
    }
    
    public boolean hasChanged() {
        return this.dirty.isDirty();
    }
    
    protected void cstrInsets(GridBagConstraints cstr) {
        AppConfig cfg = AppConfig.loadedInstance;
        int btnspacing = WidgetsBuilder.pixelSize(Float.parseFloat(cfg.getProperty("ui.touchbtnspacing")));
        if (this.y != 0) {
            cstr.insets = new Insets(btnspacing, 0, 0, 0); 
        }
    }
    
    protected JPanel addOptionsContainer(String titleCode) {
        JPanel container = new JPanel();
        container.setBorder(BorderFactory.createTitledBorder(AppLocal.getIntString(titleCode)));
        container.setLayout(new GridBagLayout());
        GridBagConstraints cstr = new GridBagConstraints();
        cstr.gridx = 0;
        cstr.fill = GridBagConstraints.HORIZONTAL;
        cstr.weightx = 1;
        this.add(container, cstr);
        this.currentContainer = container;
        this.y = 0;
        return container;
    }
    
    private JTextField addJTextFieldParam(String labelCode, boolean password) {
        JTextField field = null;
        if (password) {
            field = WidgetsBuilder.createPasswordField();
        } else {
            field = WidgetsBuilder.createTextField();
        }
        field.getDocument().addDocumentListener(this.dirty);
        JLabel label = WidgetsBuilder.createLabel(AppLocal.getIntString(labelCode));
        if (this.currentContainer != null) {
            GridBagConstraints cstr = new GridBagConstraints();
            cstr.gridy = this.y;
            cstr.gridx = 0;
            cstr.weightx = 0.25;
            cstr.anchor = GridBagConstraints.LINE_START;
            this.cstrInsets(cstr);
            this.currentContainer.add(label, cstr);
            cstr = new GridBagConstraints();
            cstr.gridy = this.y;
            cstr.gridx = 1;
            cstr.gridwidth = 3;
            cstr.weightx = 0.75;
            cstr.fill = GridBagConstraints.HORIZONTAL;
            this.cstrInsets(cstr);
            this.currentContainer.add(field, cstr);
            this.y++;
        }
        return field;
    }
    
    protected JTextField addTextParam(String labelCode) {
        return this.addJTextFieldParam(labelCode, false);
    }
    protected JPasswordField addPasswordParam(String labelCode) {
        return (JPasswordField) this.addJTextFieldParam(labelCode, true);
    }
    
    protected JTextField addFileParam(String labelCode) {
        JTextField field = WidgetsBuilder.createTextField();
        field.getDocument().addDocumentListener(this.dirty);
        JButton open = WidgetsBuilder.createButton(ImageLoader.readImageIcon("file_open.png"));
        open.addActionListener(new DirectoryEvent(field));
        JLabel label = WidgetsBuilder.createLabel(AppLocal.getIntString(labelCode));
        if (this.currentContainer != null) {
            GridBagConstraints cstr = new GridBagConstraints();
            cstr.gridy = this.y;
            cstr.gridx = 0;
            cstr.weightx = 0.25;
            cstr.anchor = GridBagConstraints.LINE_START;
            this.cstrInsets(cstr);
            this.currentContainer.add(label, cstr);
            cstr = new GridBagConstraints();
            cstr.gridy = this.y;
            cstr.gridx = 1;
            cstr.gridwidth = 2;
            cstr.weightx = 0.5;
            this.cstrInsets(cstr);
            cstr.fill = GridBagConstraints.HORIZONTAL;
            this.currentContainer.add(field, cstr);
            cstr = new GridBagConstraints();
            cstr.gridy = this.y;
            cstr.gridx = 3;
            cstr.weightx = 0.25;
            this.cstrInsets(cstr);
            cstr.anchor = GridBagConstraints.LINE_START;
            this.currentContainer.add(open, cstr);
            this.y++;
        }
        return field;
    }
    
    protected JComboBox addComboBoxParam(String labelCode) {
        JComboBox field = WidgetsBuilder.createComboBox();
        field.addActionListener(this.dirty);
        JLabel label = WidgetsBuilder.createLabel(AppLocal.getIntString(labelCode));
        if (this.currentContainer != null) {
            GridBagConstraints cstr = new GridBagConstraints();
            cstr.gridy = this.y;
            cstr.gridx = 0;
            cstr.weightx = 0.25;
            cstr.anchor = GridBagConstraints.LINE_START;
            this.cstrInsets(cstr);
            this.currentContainer.add(label, cstr);
            cstr = new GridBagConstraints();
            cstr.gridy = this.y;
            cstr.gridx = 1;
            cstr.gridwidth = 3;
            cstr.weightx = 0.75;
            this.cstrInsets(cstr);
            cstr.fill = GridBagConstraints.HORIZONTAL;
            this.currentContainer.add(field, cstr);
            this.y++;
        }
        return field;
    }
    protected JPanel addSubparamZone() {
        JPanel zone = new JPanel();
        zone.setLayout(new CardLayout());
        if (this.currentContainer != null) {
            GridBagConstraints cstr = new GridBagConstraints();
            cstr.gridy = this.y;
            cstr.gridx = 1;
            cstr.gridwidth = 3;
            cstr.weightx = 0.75;
            this.cstrInsets(cstr);
            this.currentContainer.add(zone, cstr);
            this.y++;
        }
        return zone;
    }
    
    protected JCheckBox addCheckBoxParam(String labelCode) {
        JCheckBox checkBox = WidgetsBuilder.createCheckBox();
        checkBox.addActionListener(this.dirty);
        JLabel label = WidgetsBuilder.createLabel(AppLocal.getIntString(labelCode));
        if (this.currentContainer != null) {
            GridBagConstraints cstr = new GridBagConstraints();
            cstr.gridy = this.y;
            cstr.gridx = 0;
            cstr.weightx = 0.25;
            cstr.anchor = GridBagConstraints.LINE_START;
            this.cstrInsets(cstr);
            this.currentContainer.add(label, cstr);
            cstr = new GridBagConstraints();
            cstr.gridy = this.y;
            cstr.gridx = 3;
            cstr.weightx = 0.75;
            this.cstrInsets(cstr);
            cstr.anchor = GridBagConstraints.LINE_START;
            this.currentContainer.add(checkBox, cstr);
            this.y++;
        }
        return checkBox;
    }
    protected JCheckBox addStaticCheckBoxParam(String label) {
        JCheckBox checkBox = WidgetsBuilder.createCheckBox();
        checkBox.addActionListener(this.dirty);
        JLabel lbl = WidgetsBuilder.createLabel(label);
        if (this.currentContainer != null) {
            GridBagConstraints cstr = new GridBagConstraints();
            cstr.gridy = this.y;
            cstr.gridx = 0;
            cstr.weightx = 0.25;
            cstr.anchor = GridBagConstraints.LINE_START;
            this.cstrInsets(cstr);
            this.currentContainer.add(lbl, cstr);
            cstr = new GridBagConstraints();
            cstr.gridy = this.y;
            cstr.gridx = 3;
            cstr.weightx = 0.75;
            this.cstrInsets(cstr);
            cstr.anchor = GridBagConstraints.LINE_START;
            this.currentContainer.add(checkBox, cstr);
            this.y++;
        }
        return checkBox;
    }
}
