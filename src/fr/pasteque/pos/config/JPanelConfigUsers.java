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

package fr.pasteque.pos.config;

import fr.pasteque.basic.BasicException;
import fr.pasteque.pos.forms.AppConfig;
import fr.pasteque.pos.forms.AppUser;
import fr.pasteque.pos.forms.DataLogicSystem;

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
public class JPanelConfigUsers extends PanelConfig {

    private DataLogicSystem dlSystem;
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
        if (users == null) {
            return; // Don't display anything
        }
        this.addOptionsContainer("Label.Users");
        this.allUsersCheckBox = this.addCheckBoxParam("Label.AllUsers");
        this.allUsersCheckBox.addChangeListener(new ChangeListener(){
            public void stateChanged(ChangeEvent e) {
                boolean selected = allUsersCheckBox.isSelected();
                setAllUsers(selected);
            }
        });
        
        for (AppUser user : users) {
            JCheckBox visible = this.addStaticCheckBoxParam(user.getName());
            this.userCheckBoxes.put(user.getId(), visible);
        }
    }

}

