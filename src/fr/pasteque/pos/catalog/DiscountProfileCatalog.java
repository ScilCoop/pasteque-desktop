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

package fr.pasteque.pos.catalog;

import fr.pasteque.pos.forms.JRootApp;
import fr.pasteque.pos.util.ThumbNailBuilder;

import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.event.*;
import java.awt.*;
import fr.pasteque.pos.forms.AppLocal;
import fr.pasteque.basic.BasicException;
import fr.pasteque.data.loader.ImageLoader;
import fr.pasteque.data.gui.JMessageDialog;
import fr.pasteque.data.gui.MessageInf;
import fr.pasteque.pos.customers.DataLogicCustomers;
import fr.pasteque.pos.customers.DiscountProfile;
import fr.pasteque.pos.forms.AppConfig;
import fr.pasteque.pos.widgets.WidgetsBuilder;

public class DiscountProfileCatalog extends JPanel implements ListSelectionListener, CatalogSelector {

    protected EventListenerList listeners = new EventListenerList();

    private ThumbNailBuilder tnbbutton;

    /** Creates new form JCatalog */
    public DiscountProfileCatalog() {
        this(64, 54);
    }

    public DiscountProfileCatalog(int width, int height) {

        initComponents();

        AppConfig cfg = AppConfig.loadedInstance;
        int widthCfg = WidgetsBuilder.pixelSize(Float.parseFloat(cfg.getProperty("ui.touchhudgebtnminwidth")));
        int heightCfg = WidgetsBuilder.pixelSize(Float.parseFloat(cfg.getProperty("ui.touchhudgebtnminheight")));
        tnbbutton = new ThumbNailBuilder(widthCfg, heightCfg,
                "discount_default.png");

        try {
            this.loadCatalog();
        } catch (BasicException e) {
            e.printStackTrace();
        }
    }

    public Component getComponent() {
        return this;
    }

    public void showCatalogPanel(String id) {
        this.showProfilesPanel();
    }

    public void loadCatalog() throws BasicException {
        this.profilesPanel.removeAll();
        this.showProfilesPanel();
    }

    public void setComponentEnabled(boolean enabled) {}

    public void addActionListener(ActionListener l) {
        listeners.add(ActionListener.class, l);
    }
    public void removeActionListener(ActionListener l) {
        listeners.remove(ActionListener.class, l);
    }

    public void valueChanged(ListSelectionEvent evt) {
    }

    protected void fireSelectedProfile(DiscountProfile profile) {
        EventListener[] l = listeners.getListeners(ActionListener.class);
        ActionEvent e = null;
        for (int i = 0; i < l.length; i++) {
            if (e == null) {
                e = new ActionEvent(profile, ActionEvent.ACTION_PERFORMED,
                        String.valueOf(profile.getId()));
            }
            ((ActionListener) l[i]).actionPerformed(e);
        }
    }

    private void showProfilesPanel() {
        try {
            // Load profiles
            DataLogicCustomers dlCust = new DataLogicCustomers();
            java.util.List<DiscountProfile> profiles = dlCust.getDiscountProfiles();
            JCatalogTab jcurrTab = new JCatalogTab();
            jcurrTab.applyComponentOrientation(getComponentOrientation());
            this.profilesPanel.add(jcurrTab, "profiles");
            // Add buttons
            for (DiscountProfile profile : profiles) {
                jcurrTab.addButton(new ImageIcon(tnbbutton.getThumbNailText(null, profile.getName())), new SelectedAction(profile));
            }
            CardLayout cl = (CardLayout)(this.profilesPanel.getLayout());
            cl.show(this.profilesPanel, "profiles");
        } catch (BasicException eb) {
            eb.printStackTrace();
        }
    }

    private class SelectedAction implements ActionListener {
        private DiscountProfile profile;
        public SelectedAction(DiscountProfile profile) {
            this.profile = profile;
        }
        public void actionPerformed(ActionEvent e) {
            fireSelectedProfile(profile);
        }
    }

    private void initComponents() {
        AppConfig cfg = AppConfig.loadedInstance;

        profilesPanel = new javax.swing.JPanel();

        setLayout(new java.awt.BorderLayout());

        profilesPanel.setLayout(new java.awt.CardLayout());
        add(profilesPanel, java.awt.BorderLayout.CENTER);
    }

    private javax.swing.JPanel profilesPanel;

}
