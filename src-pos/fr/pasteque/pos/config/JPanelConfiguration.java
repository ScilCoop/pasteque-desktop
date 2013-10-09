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

package fr.pasteque.pos.config;

import javax.swing.*;
import java.awt.Insets;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import fr.pasteque.basic.BasicException;
import fr.pasteque.data.loader.ImageLoader;
import fr.pasteque.pos.forms.*;
import fr.pasteque.data.gui.MessageInf;
import fr.pasteque.data.gui.JMessageDialog;
import fr.pasteque.pos.widgets.WidgetsBuilder;

/**
 *
 * @author adrianromero
 */
public class JPanelConfiguration extends JPanel implements JPanelView {

    private static Logger logger = Logger.getLogger("fr.pasteque.pos.forms.AppConfig");

    private List<PanelConfig> m_panelconfig;

    private AppConfig config;
    
    /** Creates new form JPanelConfiguration */
    public JPanelConfiguration(AppView oApp) {
        this(oApp.getProperties(), (DataLogicSystem) oApp.getBean("fr.pasteque.pos.forms.DataLogicSystem"));  
    }
    
    public JPanelConfiguration(AppProperties props, DataLogicSystem dls) {
        
        config = new AppConfig(props.getConfigFile());
        
        initComponents();
        
        // Inicio lista de paneles
        m_panelconfig = new ArrayList<PanelConfig>();
        m_panelconfig.add(new JPanelConfigDatabase());
        m_panelconfig.add(new JPanelConfigGeneral());
        m_panelconfig.add(new JPanelConfigLocale());
        if (dls != null) {
            m_panelconfig.add(new JPanelConfigUsers(dls));
        }
        
        // paneles auxiliares
        GridBagConstraints cstr = new GridBagConstraints();
        cstr.gridx = 0;
        cstr.fill = GridBagConstraints.HORIZONTAL;
        for (PanelConfig c: m_panelconfig) {
            m_jConfigOptions.add(c.getConfigComponent(), cstr);
        }
    }
        
    private void restoreProperties() {
        try {
            config.restore();
        } catch (IOException e) {
            JMessageDialog.showMessage(this, new MessageInf(MessageInf.SGN_WARNING, AppLocal.getIntString("message.cannotdeleteconfig")));            
            logger.log(Level.SEVERE, "Unable to save configuration file", e);
        }
        loadProperties();

    }
    
    private void loadProperties() {
        
        config.load();
        
        // paneles auxiliares
        for (PanelConfig c: m_panelconfig) {
            c.loadProperties(config);
        }
    }
    
    private void saveProperties() {
        
        // paneles auxiliares
        for (PanelConfig c: m_panelconfig) {
            c.saveProperties(config);
        }
        
        try {
            config.save();
            JOptionPane.showMessageDialog(this, AppLocal.getIntString("message.restartchanges"), AppLocal.getIntString("message.title"), JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            JMessageDialog.showMessage(this, new MessageInf(MessageInf.SGN_WARNING, AppLocal.getIntString("message.cannotsaveconfig"), e));
            logger.log(Level.SEVERE, "Unable to save configuration file", e);
        }
    }

    public JComponent getComponent() {
        return this;
    }
    
    public String getTitle() {
        return AppLocal.getIntString("Menu.Configuration");
    } 
    
    public void activate() throws BasicException {
        loadProperties();        
    }
    
    public boolean deactivate() {
        
        boolean haschanged = false;
        for (PanelConfig c: m_panelconfig) {
            if (c.hasChanged()) {
                haschanged = true;
            }
        }        
        
        
        if (haschanged) {
            int res = JOptionPane.showConfirmDialog(this, AppLocal.getIntString("message.wannasave"), AppLocal.getIntString("title.editor"), JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (res == JOptionPane.YES_OPTION) {
                saveProperties();
                return true;
            } else {
                return res == JOptionPane.NO_OPTION;
            }
        } else {
            return true;
        }
    }
    
    public boolean requiresOpenedCash() {
        return false;
    }

    private void initComponents() {
        AppConfig cfg = AppConfig.loadedInstance;
        int btnSpacing = WidgetsBuilder.pixelSize(Float.parseFloat(cfg.getProperty("ui.touchbtnspacing")));
        
        jScrollPane1 = new javax.swing.JScrollPane();
        m_jConfigOptions = new javax.swing.JPanel();
        m_jConfigOptions.setLayout(new GridBagLayout());
        jbtnCancel = WidgetsBuilder.createButton(ImageLoader.readImageIcon("button_cancel.png"),
                                                 AppLocal.getIntString("Button.Restore"),
                                                 WidgetsBuilder.SIZE_MEDIUM);
        jbtnRestore = WidgetsBuilder.createButton(AppLocal.getIntString("Button.Factory"));
        jbtnSave = WidgetsBuilder.createButton(ImageLoader.readImageIcon("button_ok.png"),
                                               AppLocal.getIntString("Button.Save"),
                                               WidgetsBuilder.SIZE_MEDIUM);

        jScrollPane1.setViewportView(m_jConfigOptions);

        jbtnCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbtnCancelActionPerformed(evt);
            }
        });

        jbtnRestore.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbtnRestoreActionPerformed(evt);
            }
        });

        jbtnSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbtnSaveActionPerformed(evt);
            }
        });

        this.setLayout(new GridBagLayout());
        GridBagConstraints cstr = new GridBagConstraints();
        cstr.gridx = 0;
        cstr.fill = GridBagConstraints.BOTH;
        cstr.weighty = 1;
        cstr.weightx = 1;
        this.add(jScrollPane1, cstr);
        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new GridBagLayout());
        cstr = new GridBagConstraints();
        cstr.gridy = 0;
        cstr.insets = new Insets(btnSpacing, 0, btnSpacing, btnSpacing);
        buttonsPanel.add(jbtnSave, cstr);
        buttonsPanel.add(jbtnRestore, cstr);
        cstr.insets = new Insets(btnSpacing, 0, btnSpacing, btnSpacing);
        buttonsPanel.add(jbtnCancel, cstr);
        cstr = new GridBagConstraints();
        cstr.gridx = 0;
        cstr.anchor = GridBagConstraints.LINE_END;
        this.add(buttonsPanel, cstr);
    }

    private void jbtnCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbtnCancelActionPerformed

        if (JOptionPane.showConfirmDialog(this, AppLocal.getIntString("message.configrestore"), AppLocal.getIntString("message.title"), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {          
            loadProperties();
        }
        
    }//GEN-LAST:event_jbtnCancelActionPerformed

    private void jbtnRestoreActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbtnRestoreActionPerformed

        if (JOptionPane.showConfirmDialog(this, AppLocal.getIntString("message.configfactory"), AppLocal.getIntString("message.title"), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {          
            restoreProperties();
        }
        
    }//GEN-LAST:event_jbtnRestoreActionPerformed

    private void jbtnSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbtnSaveActionPerformed

        saveProperties();
        
    }//GEN-LAST:event_jbtnSaveActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JButton jbtnCancel;
    private javax.swing.JButton jbtnRestore;
    private javax.swing.JButton jbtnSave;
    private javax.swing.JPanel m_jConfigOptions;
    // End of variables declaration//GEN-END:variables
    
}
