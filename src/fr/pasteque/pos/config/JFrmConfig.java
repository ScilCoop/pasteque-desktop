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

import fr.pasteque.pos.forms.BeanFactory;
import fr.pasteque.pos.forms.DataLogicSystem;


import java.awt.*;
import java.awt.event.*;
import fr.pasteque.basic.BasicException;
import fr.pasteque.data.loader.ImageLoader;
import fr.pasteque.pos.forms.*;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.UIManager;

/**
 * The standalone configuration panel.
 */
public class JFrmConfig extends javax.swing.JFrame {
    
    private JPanelConfiguration config;
    
    /** Creates new form JFrmConfig */
    public JFrmConfig(AppProperties props) {
        
        initComponents();
        
        this.setIconImage(ImageLoader.readImage("favicon.png"));
        setTitle(AppLocal.APP_NAME + " - " + AppLocal.APP_VERSION + " - " + AppLocal.getIntString("Menu.Configuration"));
        addWindowListener(new MyFrameListener()); 
        DataLogicSystem dls = new DataLogicSystem();
        try {
            String dbVersion = dls.findDbVersion();
            if (!AppLocal.DB_VERSION.equals(dbVersion)) {
                dls = null;
            }
        } catch (Exception e) {
            dls = null;
        }
        config = new JPanelConfiguration(props, dls);
        
        getContentPane().add(config, BorderLayout.CENTER);
       
        try {
            config.activate();
        } catch (BasicException e) { // never thrown ;-)
        }
    }
    
    private class MyFrameListener extends WindowAdapter{
        
        public void windowClosing(WindowEvent evt) {
            if (config.deactivate()) {
                dispose();
            }
        }
        public void windowClosed(WindowEvent evt) {
            System.exit(0);
        }
    }    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);

        java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        setBounds((screenSize.width-731)/2, (screenSize.height-679)/2, 731, 679);
    }// </editor-fold>//GEN-END:initComponents

    /**
     * @param args the command line arguments
     */
    public static void main(final String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                
                AppConfig config = new AppConfig(args);
                config.load();    
                
                // Set the look and feel.
                try {                    
                    UIManager.setLookAndFeel(config.getProperty("swing.defaultlaf"));
                } catch (Exception e) {
                }
                
                new JFrmConfig(config).setVisible(true);
            }
        });
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
    
}
