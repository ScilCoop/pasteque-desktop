//    POS-Tech
//
//    Copyright (C) 2012 SARL SCOP Scil (http://scil.coop)
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

import com.openbravo.basic.BasicException;
import com.openbravo.data.user.DirtyManager;
import com.openbravo.pos.forms.AppConfig;
import com.openbravo.pos.forms.AppLocal;
import com.openbravo.pos.forms.AppUser;
import com.openbravo.pos.forms.DataLogicSystem;

import java.awt.Component;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.GroupLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/** Configuration panel section to deal with users parameters */
public class JPanelConfigUsers extends javax.swing.JPanel implements PanelConfig {

    private DataLogicSystem dlSystem;
    private DirtyManager dirty = new DirtyManager();
    private JCheckBox allUsersCheckBox;
    private Map<String, JCheckBox> userCheckBoxes;
    
    public JPanelConfigUsers(DataLogicSystem dls) {
        
        this.dlSystem = dls;
        this.userCheckBoxes = new HashMap<String, JCheckBox>();
        
        // Load users list and display
        try {
            List<AppUser> users = this.dlSystem.listPeople();
            initComponents(users);
        } catch (BasicException e) {
            initComponents(null);
        }
        
    }
    
    public Component getConfigComponent() {
        return this;
    }
    
    public boolean hasChanged() {
        return dirty.isDirty();
    }
    
    public void loadProperties(AppConfig config) {
        String[] activatedUsers = config.getEnabledUsers();
        if (activatedUsers == null) {
            this.setAllUsers(true);
        } else {
            for (String id : activatedUsers) {
                JCheckBox userBox = this.userCheckBoxes.get(id);
                if (userBox != null) {
                    userBox.setSelected(true);
                }
            }
        }
        dirty.setDirty(false);
    }
   
    public void saveProperties(AppConfig config) {
        boolean allUsers = this.allUsersCheckBox.isSelected();
        if (allUsers) {
            config.setProperty("users.activated", "all");
        } else {
            String value = "";
            for (String key : this.userCheckBoxes.keySet()) {
                if (this.userCheckBoxes.get(key).isSelected()) {
                    if (value.length() == 0) {
                        value = key;
                    } else {
                        value += "," + key;
                    }
                }
            }
            config.setProperty("users.activated", value);
        }
        dirty.setDirty(false);
    }
    
    private void setAllUsers(boolean selected) {
        if (selected) {
            this.allUsersCheckBox.setSelected(true);
            for (String key : this.userCheckBoxes.keySet()) {
                 this.userCheckBoxes.get(key).setEnabled(false);
            }
        } else {
            this.allUsersCheckBox.setSelected(false);
            for (String key : this.userCheckBoxes.keySet()) {
                 this.userCheckBoxes.get(key).setEnabled(true);
            }
        }
    }
    
    public void initComponents(List<AppUser> users) {
        JPanel container = new JPanel();
        JLabel allLabel = new JLabel(AppLocal.getIntString("Label.AllUsers"));
        this.allUsersCheckBox = new JCheckBox();
        this.allUsersCheckBox.addChangeListener(new ChangeListener(){
            public void stateChanged(ChangeEvent e) {
                boolean selected = allUsersCheckBox.isSelected();
                setAllUsers(selected);
            }
        });
        
        if (users == null) {
            return; // Don't display anything
        }

        container.setBorder(javax.swing.BorderFactory.createTitledBorder(AppLocal.getIntString("Label.Users")));

        GroupLayout containerLayout = new GroupLayout(container);
        container.setLayout(containerLayout);
        
        GroupLayout.ParallelGroup horizontalGroup = containerLayout.createParallelGroup(GroupLayout.Alignment.LEADING);
        GroupLayout.ParallelGroup verticalGroup = containerLayout.createParallelGroup(GroupLayout.Alignment.LEADING);
        GroupLayout.SequentialGroup vg = containerLayout.createSequentialGroup();

        GroupLayout.SequentialGroup hgAll = containerLayout.createSequentialGroup();
        hgAll.addContainerGap();
        hgAll.addComponent(allLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE);
        hgAll.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED);
        hgAll.addComponent(this.allUsersCheckBox);
        horizontalGroup.addGroup(hgAll);
        GroupLayout.ParallelGroup vgAll = containerLayout.createParallelGroup(GroupLayout.Alignment.BASELINE);
        vgAll.addComponent(allLabel);
        vgAll.addComponent(this.allUsersCheckBox);
        vg.addGroup(vgAll);
        vg.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE);
        
        for (AppUser user : users) {
            JLabel userName = new JLabel(user.getName());
            JCheckBox visible = new JCheckBox();
            this.userCheckBoxes.put(user.getId(), visible);
            visible.addActionListener(dirty);

            GroupLayout.SequentialGroup hg = containerLayout.createSequentialGroup();
            hg.addContainerGap();
            hg.addComponent(userName, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE);
            hg.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED);
            hg.addComponent(visible);
            horizontalGroup.addGroup(hg);
            
            GroupLayout.ParallelGroup vg2 = containerLayout.createParallelGroup(GroupLayout.Alignment.BASELINE);
            vg2.addComponent(userName);
            vg2.addComponent(visible);
            vg.addGroup(vg2);
            vg.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE);
        }
        containerLayout.setHorizontalGroup(horizontalGroup);
        verticalGroup.addGroup(vg);
        containerLayout.setVerticalGroup(verticalGroup);
        
        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(container, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(container, GroupLayout.PREFERRED_SIZE, 183, GroupLayout.PREFERRED_SIZE)
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }

}

