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

import fr.pasteque.data.user.DirtyManager;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import java.util.Map;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.LookAndFeel;
import javax.swing.SwingUtilities;
import org.jvnet.substance.SubstanceLookAndFeel;
import org.jvnet.substance.api.SubstanceSkin;
import org.jvnet.substance.skin.SkinInfo;

import fr.pasteque.pos.forms.AppConfig;
import fr.pasteque.pos.forms.AppLocal;
import fr.pasteque.pos.util.ReportUtils;
import fr.pasteque.pos.util.StringParser;
import fr.pasteque.pos.widgets.WidgetsBuilder;

/**
 *
 * @author adrianromero
 */
public class JPanelConfigGeneral extends PanelConfig {

    private DirtyManager dirty = new DirtyManager();

    private ParametersConfig printer1printerparams;

    private ParametersConfig printer2printerparams;

    private ParametersConfig printer3printerparams;

    /** Creates new form JPanelConfigGeneral */
    public JPanelConfigGeneral() {

        initComponents();

        String[] printernames = ReportUtils.getPrintNames();

        jtxtMachineHostname.getDocument().addDocumentListener(dirty);
        jcboLAF.addActionListener(dirty);
        m_jcboScreenType.addActionListener(dirty);
        jtxtScreenDensity.getDocument().addDocumentListener(dirty);
        jcboMachineScreenmode.addActionListener(dirty);
        jcboTicketsBag.addActionListener(dirty);
        jcboMarginType.addActionListener(dirty);
        
        jcbShowTitlebar.addActionListener(dirty);
        jcbShowFooterbar.addActionListener(dirty);
        jcbAutoHideMenu.addActionListener(dirty);

        jcboMachineDisplay.addActionListener(dirty);
        jcboConnDisplay.addActionListener(dirty);
        jcboSerialDisplay.addActionListener(dirty);
        m_jtxtJPOSName.getDocument().addDocumentListener(dirty);

        jcboMachinePrinter.addActionListener(dirty);
        jcboConnPrinter.addActionListener(dirty);
        jcboSerialPrinter.addActionListener(dirty);
        m_jtxtJPOSPrinter.getDocument().addDocumentListener(dirty);
        m_jtxtJPOSDrawer.getDocument().addDocumentListener(dirty);
        
        printer1printerparams = new ParametersPrinter(printernames);
        printer1printerparams.addDirtyManager(dirty);
        m_jPrinterParams1.add(printer1printerparams.getComponent(), "printer");

        jcboMachinePrinter2.addActionListener(dirty);
        jcboConnPrinter2.addActionListener(dirty);
        jcboSerialPrinter2.addActionListener(dirty);
        m_jtxtJPOSPrinter2.getDocument().addDocumentListener(dirty);
        m_jtxtJPOSDrawer2.getDocument().addDocumentListener(dirty);

        printer2printerparams = new ParametersPrinter(printernames);
        printer2printerparams.addDirtyManager(dirty);
        m_jPrinterParams2.add(printer2printerparams.getComponent(), "printer");

        jcboMachinePrinter3.addActionListener(dirty);
        jcboConnPrinter3.addActionListener(dirty);
        jcboSerialPrinter3.addActionListener(dirty);
        m_jtxtJPOSPrinter3.getDocument().addDocumentListener(dirty);
        m_jtxtJPOSDrawer3.getDocument().addDocumentListener(dirty);

        printer3printerparams = new ParametersPrinter(printernames);
        printer3printerparams.addDirtyManager(dirty);
        m_jPrinterParams3.add(printer3printerparams.getComponent(), "printer");

        jcbAutoPrint.addActionListener(dirty);

        jcboMachineScale.addActionListener(dirty);
        jcboSerialScale.addActionListener(dirty);

        jcboMachineScanner.addActionListener(dirty);
        jcboSerialScanner.addActionListener(dirty);

        cboPrinters.addActionListener(dirty);

//        // Openbravo Skin
//        jcboLAF.addItem(new UIManager.LookAndFeelInfo("Openbravo", "fr.pasteque.pos.skin.OpenbravoLookAndFeel"));

        // Installed skins
        LookAndFeelInfo[] lafs = UIManager.getInstalledLookAndFeels();
        for (int i = 0; i < lafs.length; i++) {
            jcboLAF.addItem(new LAFInfo(lafs[i].getName(), lafs[i].getClassName()));
        }

        // Substance skins
        // new SubstanceLookAndFeel(); // TODO: Remove in Substance 5.0. Workaround for Substance 4.3 to initialize static variables
        Map<String, SkinInfo> skins = SubstanceLookAndFeel.getAllSkins();
        for (SkinInfo skin : skins.values()) {
            jcboLAF.addItem(new LAFInfo(skin.getDisplayName(), skin.getClassName()));
        }

        jcboLAF.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                changeLAF();
            }
        });

        jcboMachineScreenmode.addItem("window");
        jcboMachineScreenmode.addItem("fullscreen");

        // Screen type values
        m_jcboScreenType.addItem("standard");
        m_jcboScreenType.addItem("touchscreen");
        
        

        jcboTicketsBag.addItem("simple");
        jcboTicketsBag.addItem("standard");
        jcboTicketsBag.addItem("restaurant");
        
        // Margin type values
        jcboMarginType.addItem("margin");
        jcboMarginType.addItem("rate");

        // Printer 1
        jcboMachinePrinter.addItem("screen");
        jcboMachinePrinter.addItem("printer");
        jcboMachinePrinter.addItem("epson");
        jcboMachinePrinter.addItem("tmu220");
        jcboMachinePrinter.addItem("star");
        jcboMachinePrinter.addItem("ithaca");
        jcboMachinePrinter.addItem("surepos");
        jcboMachinePrinter.addItem("plain");
        jcboMachinePrinter.addItem("javapos");
        jcboMachinePrinter.addItem("Not defined");

        jcboConnPrinter.addItem("serial");
        jcboConnPrinter.addItem("file");

        jcboSerialPrinter.addItem("COM1");
        jcboSerialPrinter.addItem("COM2");
        jcboSerialPrinter.addItem("COM3");
        jcboSerialPrinter.addItem("COM4");
        jcboSerialPrinter.addItem("LPT1");
        jcboSerialPrinter.addItem("/dev/ttyS0");
        jcboSerialPrinter.addItem("/dev/ttyS1");
        jcboSerialPrinter.addItem("/dev/ttyS2");
        jcboSerialPrinter.addItem("/dev/ttyS3");

        // Printer 2        
        jcboMachinePrinter2.addItem("screen");
        jcboMachinePrinter2.addItem("printer");
        jcboMachinePrinter2.addItem("epson");
        jcboMachinePrinter2.addItem("tmu220");
        jcboMachinePrinter2.addItem("star");
        jcboMachinePrinter2.addItem("ithaca");
        jcboMachinePrinter2.addItem("surepos");
        jcboMachinePrinter2.addItem("plain");
        jcboMachinePrinter2.addItem("javapos");
        jcboMachinePrinter2.addItem("Not defined");

        jcboConnPrinter2.addItem("serial");
        jcboConnPrinter2.addItem("file");

        jcboSerialPrinter2.addItem("COM1");
        jcboSerialPrinter2.addItem("COM2");
        jcboSerialPrinter2.addItem("COM3");
        jcboSerialPrinter2.addItem("COM4");
        jcboSerialPrinter2.addItem("LPT1");
        jcboSerialPrinter2.addItem("/dev/ttyS0");
        jcboSerialPrinter2.addItem("/dev/ttyS1");
        jcboSerialPrinter2.addItem("/dev/ttyS2");
        jcboSerialPrinter2.addItem("/dev/ttyS3");

        // Printer 3
        jcboMachinePrinter3.addItem("screen");
        jcboMachinePrinter3.addItem("printer");
        jcboMachinePrinter3.addItem("epson");
        jcboMachinePrinter3.addItem("tmu220");
        jcboMachinePrinter3.addItem("star");
        jcboMachinePrinter3.addItem("ithaca");
        jcboMachinePrinter3.addItem("surepos");
        jcboMachinePrinter3.addItem("plain");
        jcboMachinePrinter3.addItem("javapos");
        jcboMachinePrinter3.addItem("Not defined");

        jcboConnPrinter3.addItem("serial");
        jcboConnPrinter3.addItem("file");

        jcboSerialPrinter3.addItem("COM1");
        jcboSerialPrinter3.addItem("COM2");
        jcboSerialPrinter3.addItem("COM3");
        jcboSerialPrinter3.addItem("COM4");
        jcboSerialPrinter3.addItem("LPT1");
        jcboSerialPrinter3.addItem("/dev/ttyS0");
        jcboSerialPrinter3.addItem("/dev/ttyS1");
        jcboSerialPrinter3.addItem("/dev/ttyS2");
        jcboSerialPrinter3.addItem("/dev/ttyS3");

        // Display
        jcboMachineDisplay.addItem("screen");
        jcboMachineDisplay.addItem("window");
        jcboMachineDisplay.addItem("javapos");
        jcboMachineDisplay.addItem("epson");
        jcboMachineDisplay.addItem("ld200");
        jcboMachineDisplay.addItem("surepos");
        jcboMachineDisplay.addItem("Not defined");

        jcboConnDisplay.addItem("serial");
        jcboConnDisplay.addItem("file");

        jcboSerialDisplay.addItem("COM1");
        jcboSerialDisplay.addItem("COM2");
        jcboSerialDisplay.addItem("COM3");
        jcboSerialDisplay.addItem("COM4");
        jcboSerialDisplay.addItem("LPT1");
        jcboSerialDisplay.addItem("/dev/ttyS0");
        jcboSerialDisplay.addItem("/dev/ttyS1");
        jcboSerialDisplay.addItem("/dev/ttyS2");
        jcboSerialDisplay.addItem("/dev/ttyS3");

        // Scale
        jcboMachineScale.addItem("screen");
        jcboMachineScale.addItem("dialog1");
        jcboMachineScale.addItem("8217protocol");
        jcboMachineScale.addItem("samsungesp");
        jcboMachineScale.addItem("Not defined");

        jcboSerialScale.addItem("COM1");
        jcboSerialScale.addItem("COM2");
        jcboSerialScale.addItem("COM3");
        jcboSerialScale.addItem("COM4");
        jcboSerialScale.addItem("/dev/ttyS0");
        jcboSerialScale.addItem("/dev/ttyS1");
        jcboSerialScale.addItem("/dev/ttyS2");
        jcboSerialScale.addItem("/dev/ttyS3");

        // Scanner
        jcboMachineScanner.addItem("scanpal2");
        jcboMachineScanner.addItem("Not defined");

        jcboSerialScanner.addItem("COM1");
        jcboSerialScanner.addItem("COM2");
        jcboSerialScanner.addItem("COM3");
        jcboSerialScanner.addItem("COM4");
        jcboSerialScanner.addItem("/dev/ttyS0");
        jcboSerialScanner.addItem("/dev/ttyS1");
        jcboSerialScanner.addItem("/dev/ttyS2");
        jcboSerialScanner.addItem("/dev/ttyS3");

        // Printers
        cboPrinters.addItem("(Default)");
        cboPrinters.addItem("(Show dialog)");
        for (String name : printernames) {
            cboPrinters.addItem(name);
        }
    }

    public boolean hasChanged() {
        return dirty.isDirty();
    }

    public Component getConfigComponent() {
        return this;
    }

    public void loadProperties(AppConfig config) {

        jtxtMachineHostname.setText(config.getProperty("machine.hostname"));

        String lafclass = config.getProperty("swing.defaultlaf");
        jcboLAF.setSelectedItem(null);
        for (int i = 0; i < jcboLAF.getItemCount(); i++) {
            LAFInfo lafinfo = (LAFInfo) jcboLAF.getItemAt(i);
            if (lafinfo.getClassName().equals(lafclass)) {
                jcboLAF.setSelectedIndex(i);
                break;
            }
        }
        // jcboLAF.setSelectedItem(new LookAndFeelInfo());

        jcboMachineScreenmode.setSelectedItem(config.getProperty("machine.screenmode"));
        m_jcboScreenType.setSelectedItem(config.getProperty("machine.screentype"));
        jtxtScreenDensity.setText(config.getProperty("machine.screendensity"));
        jcboTicketsBag.setSelectedItem(config.getProperty("machine.ticketsbag"));
        jcboMarginType.setSelectedItem(config.getProperty("ui.margintype"));

        jcbShowTitlebar.setSelected(!config.getProperty("ui.showtitlebar").equals("0"));
        jcbShowFooterbar.setSelected(!config.getProperty("ui.showfooterbar").equals("0"));
        jcbAutoHideMenu.setSelected(!config.getProperty("ui.autohidemenu").equals("0"));

        StringParser p = new StringParser(config.getProperty("machine.printer"));
        String sparam = unifySerialInterface(p.nextToken(':'));
        if ("serial".equals(sparam) || "file".equals(sparam)) {
            jcboMachinePrinter.setSelectedItem("epson");
            jcboConnPrinter.setSelectedItem(sparam);
            jcboSerialPrinter.setSelectedItem(p.nextToken(','));
        } else if ("javapos".equals(sparam)) {
            jcboMachinePrinter.setSelectedItem(sparam);
            m_jtxtJPOSPrinter.setText(p.nextToken(','));
            m_jtxtJPOSDrawer.setText(p.nextToken(','));
        } else if ("printer".equals(sparam)) {
            jcboMachinePrinter.setSelectedItem(sparam);
            printer1printerparams.setParameters(p);
        } else {
            jcboMachinePrinter.setSelectedItem(sparam);
            jcboConnPrinter.setSelectedItem(unifySerialInterface(p.nextToken(',')));
            jcboSerialPrinter.setSelectedItem(p.nextToken(','));
        }

        p = new StringParser(config.getProperty("machine.printer.2"));
        sparam = unifySerialInterface(p.nextToken(':'));
        if ("serial".equals(sparam) || "file".equals(sparam)) {
            jcboMachinePrinter2.setSelectedItem("epson");
            jcboConnPrinter2.setSelectedItem(sparam);
            jcboSerialPrinter2.setSelectedItem(p.nextToken(','));
        } else if ("javapos".equals(sparam)) {
            jcboMachinePrinter2.setSelectedItem(sparam);
            m_jtxtJPOSPrinter2.setText(p.nextToken(','));
            m_jtxtJPOSDrawer2.setText(p.nextToken(','));
        } else if ("printer".equals(sparam)) {
            jcboMachinePrinter2.setSelectedItem(sparam);
            printer2printerparams.setParameters(p);
        } else {
            jcboMachinePrinter2.setSelectedItem(sparam);
            jcboConnPrinter2.setSelectedItem(unifySerialInterface(p.nextToken(',')));
            jcboSerialPrinter2.setSelectedItem(p.nextToken(','));
        }

        p = new StringParser(config.getProperty("machine.printer.3"));
        sparam = unifySerialInterface(p.nextToken(':'));
        if ("serial".equals(sparam) || "file".equals(sparam)) {
            jcboMachinePrinter3.setSelectedItem("epson");
            jcboConnPrinter3.setSelectedItem(sparam);
            jcboSerialPrinter3.setSelectedItem(p.nextToken(','));
        } else if ("javapos".equals(sparam)) {
            jcboMachinePrinter3.setSelectedItem(sparam);
            m_jtxtJPOSPrinter3.setText(p.nextToken(','));
            m_jtxtJPOSDrawer3.setText(p.nextToken(','));
        } else if ("printer".equals(sparam)) {
            jcboMachinePrinter3.setSelectedItem(sparam);
            printer3printerparams.setParameters(p);
        } else {
            jcboMachinePrinter3.setSelectedItem(sparam);
            jcboConnPrinter3.setSelectedItem(unifySerialInterface(p.nextToken(',')));
            jcboSerialPrinter3.setSelectedItem(p.nextToken(','));
        }

        jcbAutoPrint.setSelected(!config.getProperty("ui.printticketbydefault").equals("0"));

        p = new StringParser(config.getProperty("machine.display"));
        sparam = unifySerialInterface(p.nextToken(':'));
        if ("serial".equals(sparam) || "file".equals(sparam)) {
            jcboMachineDisplay.setSelectedItem("epson");
            jcboConnDisplay.setSelectedItem(sparam);
            jcboSerialDisplay.setSelectedItem(p.nextToken(','));
        } else if ("javapos".equals(sparam)) {
            jcboMachineDisplay.setSelectedItem(sparam);
            m_jtxtJPOSName.setText(p.nextToken(','));
        } else {
            jcboMachineDisplay.setSelectedItem(sparam);
            jcboConnDisplay.setSelectedItem(unifySerialInterface(p.nextToken(',')));
            jcboSerialDisplay.setSelectedItem(p.nextToken(','));
        }

        p = new StringParser(config.getProperty("machine.scale"));
        sparam = p.nextToken(':');
        jcboMachineScale.setSelectedItem(sparam);
        if ("dialog1".equals(sparam) || "8217protocol".equals(sparam) || "samsungesp".equals(sparam)) {
            jcboSerialScale.setSelectedItem(p.nextToken(','));
        }

        p = new StringParser(config.getProperty("machine.scanner"));
        sparam = p.nextToken(':');
        jcboMachineScanner.setSelectedItem(sparam);
        if ("scanpal2".equals(sparam)) {
            jcboSerialScanner.setSelectedItem(p.nextToken(','));
        }

        cboPrinters.setSelectedItem(config.getProperty("machine.printername"));

        dirty.setDirty(false);
    }

    public void saveProperties(AppConfig config) {

        config.setProperty("machine.hostname", jtxtMachineHostname.getText());

        LAFInfo laf = (LAFInfo) jcboLAF.getSelectedItem();
        config.setProperty("swing.defaultlaf", laf == null
                ? System.getProperty("swing.defaultlaf", "javax.swing.plaf.metal.MetalLookAndFeel")
                : laf.getClassName());

        config.setProperty("machine.screenmode", comboValue(jcboMachineScreenmode.getSelectedItem()));
        config.setProperty("machine.screentype", comboValue(m_jcboScreenType.getSelectedItem()));
        config.setProperty("machine.screendensity", jtxtScreenDensity.getText());
        config.setProperty("machine.ticketsbag", comboValue(jcboTicketsBag.getSelectedItem()));
        config.setProperty("ui.margintype", comboValue(jcboMarginType.getSelectedItem()));

        config.setProperty("ui.showtitlebar", checkboxValue(jcbShowTitlebar));
        config.setProperty("ui.showfooterbar", checkboxValue(jcbShowFooterbar));
        config.setProperty("ui.autohidemenu", checkboxValue(jcbAutoHideMenu));
        
        String sMachinePrinter = comboValue(jcboMachinePrinter.getSelectedItem());
        if ("epson".equals(sMachinePrinter) || "tmu220".equals(sMachinePrinter) || "star".equals(sMachinePrinter) || "ithaca".equals(sMachinePrinter) || "surepos".equals(sMachinePrinter)) {
            config.setProperty("machine.printer", sMachinePrinter + ":" + comboValue(jcboConnPrinter.getSelectedItem()) + "," + comboValue(jcboSerialPrinter.getSelectedItem()));
        } else if ("javapos".equals(sMachinePrinter)) {
            config.setProperty("machine.printer", sMachinePrinter + ":" + m_jtxtJPOSPrinter.getText() + "," + m_jtxtJPOSDrawer.getText());
        } else if ("printer".equals(sMachinePrinter)) {
            config.setProperty("machine.printer", sMachinePrinter + ":" + printer1printerparams.getParameters());
        } else {
            config.setProperty("machine.printer", sMachinePrinter);
        }

        String sMachinePrinter2 = comboValue(jcboMachinePrinter2.getSelectedItem());
        if ("epson".equals(sMachinePrinter2) || "tmu220".equals(sMachinePrinter2) || "star".equals(sMachinePrinter2) || "ithaca".equals(sMachinePrinter2) || "surepos".equals(sMachinePrinter2)) {
            config.setProperty("machine.printer.2", sMachinePrinter2 + ":" + comboValue(jcboConnPrinter2.getSelectedItem()) + "," + comboValue(jcboSerialPrinter2.getSelectedItem()));
        } else if ("javapos".equals(sMachinePrinter2)) {
            config.setProperty("machine.printer.2", sMachinePrinter2 + ":" + m_jtxtJPOSPrinter2.getText() + "," + m_jtxtJPOSDrawer2.getText());
        } else if ("printer".equals(sMachinePrinter2)) {
            config.setProperty("machine.printer.2", sMachinePrinter2 + ":" + printer2printerparams.getParameters());
        } else {
            config.setProperty("machine.printer.2", sMachinePrinter2);
        }


        String sMachinePrinter3 = comboValue(jcboMachinePrinter3.getSelectedItem());
        if ("epson".equals(sMachinePrinter3) || "tmu220".equals(sMachinePrinter3) || "star".equals(sMachinePrinter3) || "ithaca".equals(sMachinePrinter3) || "surepos".equals(sMachinePrinter3)) {
            config.setProperty("machine.printer.3", sMachinePrinter3 + ":" + comboValue(jcboConnPrinter3.getSelectedItem()) + "," + comboValue(jcboSerialPrinter3.getSelectedItem()));
        } else if ("javapos".equals(sMachinePrinter3)) {
            config.setProperty("machine.printer.3", sMachinePrinter3 + ":" + m_jtxtJPOSPrinter3.getText() + "," + m_jtxtJPOSDrawer3.getText());
        } else if ("printer".equals(sMachinePrinter3)) {
            config.setProperty("machine.printer.3", sMachinePrinter3 + ":" + printer3printerparams.getParameters());
        } else {
            config.setProperty("machine.printer.3", sMachinePrinter3);
        }

        config.setProperty("ui.printticketbydefault", checkboxValue(jcbAutoPrint));

        String sMachineDisplay = comboValue(jcboMachineDisplay.getSelectedItem());
        if ("epson".equals(sMachineDisplay) || "ld200".equals(sMachineDisplay) || "surepos".equals(sMachineDisplay)) {
            config.setProperty("machine.display", sMachineDisplay + ":" + comboValue(jcboConnDisplay.getSelectedItem()) + "," + comboValue(jcboSerialDisplay.getSelectedItem()));
        } else if ("javapos".equals(sMachineDisplay)) {
            config.setProperty("machine.display", sMachineDisplay + ":" + m_jtxtJPOSName.getText());
        } else {
            config.setProperty("machine.display", sMachineDisplay);
        }

        // La bascula
        String sMachineScale = comboValue(jcboMachineScale.getSelectedItem());
        if ("dialog1".equals(sMachineScale) || "8217protocol".equals(sMachineScale) || "samsungesp".equals(sMachineScale)) {
            config.setProperty("machine.scale", sMachineScale + ":" + comboValue(jcboSerialScale.getSelectedItem()));
        } else {
            config.setProperty("machine.scale", sMachineScale);
        }

        // El scanner
        String sMachineScanner = comboValue(jcboMachineScanner.getSelectedItem());
        if ("scanpal2".equals(sMachineScanner)) {
            config.setProperty("machine.scanner", sMachineScanner + ":" + comboValue(jcboSerialScanner.getSelectedItem()));
        } else {
            config.setProperty("machine.scanner", sMachineScanner);
        }

        config.setProperty("machine.printername", comboValue(cboPrinters.getSelectedItem()));

        dirty.setDirty(false);
    }

    private String unifySerialInterface(String sparam) {

        // for backward compatibility
        return ("rxtx".equals(sparam))
                ? "serial"
                : sparam;
    }

    private String comboValue(Object value) {
        return value == null ? "" : value.toString();
    }
    
    private String checkboxValue(javax.swing.JCheckBox checkbox) {
        if (checkbox.isSelected()) {
            return "1";
        } else {
            return "0";
        }
    }

    private void changeLAF() {

        final LAFInfo laf = (LAFInfo) jcboLAF.getSelectedItem();
        if (laf != null && !laf.getClassName().equals(UIManager.getLookAndFeel().getClass().getName())) {
            // The selected look and feel is different from the current look and feel.
            SwingUtilities.invokeLater(new Runnable() {

                public void run() {
                    try {
                        String lafname = laf.getClassName();
                        Object laf = Class.forName(lafname).newInstance();

                        if (laf instanceof LookAndFeel) {
                            UIManager.setLookAndFeel((LookAndFeel) laf);
                        } else if (laf instanceof SubstanceSkin) {
                            SubstanceLookAndFeel.setSkin((SubstanceSkin) laf);
                        }

                        SwingUtilities.updateComponentTreeUI(JPanelConfigGeneral.this.getTopLevelAncestor());
                    } catch (Exception e) {
                    }
                }
            });
        }
    }

    private static class LAFInfo {

        private String name;
        private String classname;

        public LAFInfo(String name, String classname) {
            this.name = name;
            this.classname = classname;
        }

        public String getName() {
            return name;
        }

        public String getClassName() {
            return classname;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        this.addOptionsContainer("Label.CashMachine");
        this.jtxtMachineHostname = this.addTextParam("Label.MachineName");
        this.jcboLAF = this.addComboBoxParam("label.looknfeel");
        this.jcboMachineScreenmode = this.addComboBoxParam("Label.MachineScreen");
        this.m_jcboScreenType = this.addComboBoxParam("Label.MachineScreenType");
        this.jtxtScreenDensity = this.addTextParam("label.machinescreendensity");
        this.jcbShowTitlebar = this.addCheckBoxParam("label.showtitlebar");
        this.jcbShowFooterbar = this.addCheckBoxParam("label.showfooterbar");
        this.jcbAutoHideMenu = this.addCheckBoxParam("label.autohidemenu");
        this.jcboTicketsBag = this.addComboBoxParam("Label.Ticketsbag");
        this.jcboMarginType = this.addComboBoxParam("Label.MarginType");
        this.jcboMachineDisplay = this.addComboBoxParam("Label.MachineDisplay");
        this.m_jDisplayParams = this.addSubparamZone();
        this.jcboMachinePrinter = this.addComboBoxParam("Label.MachinePrinter");
        this.m_jPrinterParams1 = this.addSubparamZone();
        this.jcboMachinePrinter2 = this.addComboBoxParam("Label.MachinePrinter2");
        this.m_jPrinterParams2 = this.addSubparamZone();
        this.jcboMachinePrinter3 = this.addComboBoxParam("Label.MachinePrinter3");
        this.m_jPrinterParams3 = this.addSubparamZone();
        this.jcbAutoPrint = this.addCheckBoxParam("Label.PrintDefault");
        this.jcboMachineScale = this.addComboBoxParam("label.scale");
        this.m_jScaleParams = this.addSubparamZone();
        this.jcboMachineScanner = this.addComboBoxParam("label.scanner");
        this.m_jScannerParams = this.addSubparamZone();
        this.cboPrinters = this.addComboBoxParam("label.reportsprinter");
        
        GridBagConstraints cstr;

        // Customer display subparams
        jcboMachineDisplay.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jcboMachineDisplayActionPerformed(evt);
            }
        });
        // Empty
        jPanel2 = new JPanel();
        m_jDisplayParams.add(jPanel2, "empty");
        // comm (serial)
        jPanel1 = new JPanel();
        jPanel1.setLayout(new GridBagLayout());
        JLabel jlblConnDisplay = WidgetsBuilder.createLabel(AppLocal.getIntString("label.machinedisplayconn"));
        jcboConnDisplay = new javax.swing.JComboBox();
        JLabel jlblDisplayPort = WidgetsBuilder.createLabel(AppLocal.getIntString("label.machinedisplayport"));
        jcboSerialDisplay = new javax.swing.JComboBox();
        jcboSerialDisplay.setEditable(true);
        cstr = new GridBagConstraints();
        cstr.gridx = 0;
        cstr.gridy = 0;
        jPanel1.add(jlblConnDisplay, cstr);
        cstr = new GridBagConstraints();
        cstr.gridx = 1;
        cstr.gridy = 0;
        jPanel1.add(jcboConnDisplay, cstr);
        cstr = new GridBagConstraints();
        cstr.gridx = 0;
        cstr.gridy = 1;
        jPanel1.add(jlblDisplayPort, cstr);
        cstr = new GridBagConstraints();
        cstr.gridx = 1;
        cstr.gridy = 1;
        jPanel1.add(jcboSerialDisplay, cstr);
        m_jDisplayParams.add(jPanel1, "comm");
        // javapos
        jPanel3 = new JPanel();
        jPanel3.setLayout(new GridBagLayout());
        JLabel javaposNameLbl = WidgetsBuilder.createLabel(AppLocal.getIntString("Label.Name"));
        m_jtxtJPOSName = new javax.swing.JTextField();
        cstr = new GridBagConstraints();
        cstr.gridx = 0;
        cstr.gridy = 0;
        jPanel3.add(javaposNameLbl, cstr);
        cstr = new GridBagConstraints();
        cstr.gridx = 1;
        cstr.gridy = 0;
        jPanel3.add(m_jtxtJPOSName, cstr);
        m_jDisplayParams.add(jPanel3, "javapos");

        // Printer1 subparams
        jcboMachinePrinter.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jcboMachinePrinterActionPerformed(evt);
            }
        });
        // empty
        jPanel5 = new JPanel();
        m_jPrinterParams1.add(jPanel5, "empty");
        // comm (serial)
        jPanel6 = new JPanel();
        jPanel6.setLayout(new GridBagLayout());
        jlblConnPrinter = WidgetsBuilder.createLabel(AppLocal.getIntString("label.machinedisplayconn"));
        jcboConnPrinter = new javax.swing.JComboBox();
        jlblPrinterPort = WidgetsBuilder.createLabel(AppLocal.getIntString("label.machineprinterport"));
        jcboSerialPrinter = new javax.swing.JComboBox();
        jcboSerialPrinter.setEditable(true);
        cstr = new GridBagConstraints();
        cstr.gridx = 0;
        cstr.gridy = 0;
        jPanel6.add(jlblConnPrinter, cstr);
        cstr = new GridBagConstraints();
        cstr.gridx = 1;
        cstr.gridy = 0;
        jPanel6.add(jcboConnPrinter, cstr);
        cstr = new GridBagConstraints();
        cstr.gridx = 0;
        cstr.gridy = 1;
        jPanel6.add(jlblPrinterPort, cstr);
        cstr = new GridBagConstraints();
        cstr.gridx = 1;
        cstr.gridy = 1;
        jPanel6.add(jcboSerialPrinter, cstr);
        m_jPrinterParams1.add(jPanel6, "comm");
        // javapos
        jPanel4 = new JPanel();
        jPanel4.setLayout(new GridBagLayout());
        JLabel printer1javaposNameLbl = WidgetsBuilder.createLabel(AppLocal.getIntString("label.javapos.printer"));
        m_jtxtJPOSPrinter = new javax.swing.JTextField();
        JLabel printer1javaposDrawerLbl = WidgetsBuilder.createLabel(AppLocal.getIntString("label.javapos.drawer"));
        m_jtxtJPOSDrawer = new javax.swing.JTextField();
        cstr = new GridBagConstraints();
        cstr.gridx = 0;
        cstr.gridy = 0;
        jPanel4.add(printer1javaposNameLbl, cstr);
        cstr = new GridBagConstraints();
        cstr.gridx = 1;
        cstr.gridy = 0;
        jPanel4.add(m_jtxtJPOSPrinter, cstr);
        cstr = new GridBagConstraints();
        cstr.gridx = 0;
        cstr.gridy = 1;
        jPanel4.add(printer1javaposDrawerLbl, cstr);
        cstr = new GridBagConstraints();
        cstr.gridx = 1;
        cstr.gridy = 1;
        jPanel4.add(m_jtxtJPOSDrawer, cstr);
        m_jPrinterParams1.add(jPanel4, "javapos");

        // Printer2 subparams
        jcboMachinePrinter2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jcboMachinePrinter2ActionPerformed(evt);
            }
        });
        // empty
        jPanel7 = new JPanel();
        m_jPrinterParams2.add(jPanel7, "empty");
        // comm (serial)
        jPanel8 = new JPanel();
        jPanel8.setLayout(new GridBagLayout());
        jlblConnPrinter2 = WidgetsBuilder.createLabel(AppLocal.getIntString("label.machinedisplayconn"));
        jcboConnPrinter2 = new javax.swing.JComboBox();
        jlblPrinterPort2 = WidgetsBuilder.createLabel(AppLocal.getIntString("label.machineprinterport"));
        jcboSerialPrinter2 = new javax.swing.JComboBox();
        jcboSerialPrinter2.setEditable(true);
        cstr = new GridBagConstraints();
        cstr.gridx = 0;
        cstr.gridy = 0;
        jPanel8.add(jlblConnPrinter2, cstr);
        cstr = new GridBagConstraints();
        cstr.gridx = 1;
        cstr.gridy = 0;
        jPanel8.add(jcboConnPrinter2, cstr);
        cstr = new GridBagConstraints();
        cstr.gridx = 0;
        cstr.gridy = 1;
        jPanel8.add(jlblPrinterPort2, cstr);
        cstr = new GridBagConstraints();
        cstr.gridx = 1;
        cstr.gridy = 1;
        jPanel8.add(jcboSerialPrinter2, cstr);
        m_jPrinterParams2.add(jPanel8, "comm");
        // javapos
        jPanel11 = new JPanel();
        jPanel11.setLayout(new GridBagLayout());
        JLabel printer2javaposNameLbl = WidgetsBuilder.createLabel(AppLocal.getIntString("label.javapos.printer"));
        m_jtxtJPOSPrinter2 = new javax.swing.JTextField();
        JLabel printer2javaposDrawerLbl = WidgetsBuilder.createLabel(AppLocal.getIntString("label.javapos.drawer"));
        m_jtxtJPOSDrawer2 = new javax.swing.JTextField();
        cstr = new GridBagConstraints();
        cstr.gridx = 0;
        cstr.gridy = 0;
        jPanel11.add(printer2javaposNameLbl, cstr);
        cstr = new GridBagConstraints();
        cstr.gridx = 1;
        cstr.gridy = 0;
        jPanel11.add(m_jtxtJPOSPrinter2, cstr);
        cstr = new GridBagConstraints();
        cstr.gridx = 0;
        cstr.gridy = 1;
        jPanel11.add(printer2javaposDrawerLbl, cstr);
        cstr = new GridBagConstraints();
        cstr.gridx = 1;
        cstr.gridy = 1;
        jPanel11.add(m_jtxtJPOSDrawer2, cstr);
        m_jPrinterParams2.add(jPanel11, "javapos");

        // Printer3 subparams
        jcboMachinePrinter3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jcboMachinePrinter3ActionPerformed(evt);
            }
        });
        // empty
        jPanel9 = new JPanel();
        m_jPrinterParams3.add(jPanel9, "empty");
        // comm (serial)
        jPanel10 = new JPanel();
        jPanel10.setLayout(new GridBagLayout());
        jlblConnPrinter3 = WidgetsBuilder.createLabel(AppLocal.getIntString("label.machinedisplayconn"));
        jcboConnPrinter3 = new JComboBox();
        jlblPrinterPort3 = WidgetsBuilder.createLabel(AppLocal.getIntString("label.machineprinterport"));
        jcboSerialPrinter3 = new JComboBox();
        jcboSerialPrinter3.setEditable(true);
        cstr = new GridBagConstraints();
        cstr.gridx = 0;
        cstr.gridy = 0;
        jPanel10.add(jlblConnPrinter3, cstr);
        cstr = new GridBagConstraints();
        cstr.gridx = 1;
        cstr.gridy = 0;
        jPanel10.add(jcboConnPrinter3, cstr);
        cstr = new GridBagConstraints();
        cstr.gridx = 0;
        cstr.gridy = 1;
        jPanel10.add(jlblPrinterPort3, cstr);
        cstr = new GridBagConstraints();
        cstr.gridx = 1;
        cstr.gridy = 1;
        jPanel10.add(jcboSerialPrinter3, cstr);
        m_jPrinterParams3.add(jPanel10, "comm");
        // javapos
        jPanel12 = new JPanel();
        jPanel12.setLayout(new GridBagLayout());
        JLabel printer3javaposNameLbl = WidgetsBuilder.createLabel(AppLocal.getIntString("label.javapos.printer"));
        m_jtxtJPOSPrinter3 = new javax.swing.JTextField();
        JLabel printer3javaposDrawerLbl = WidgetsBuilder.createLabel(AppLocal.getIntString("label.javapos.drawer"));
        m_jtxtJPOSDrawer3 = new javax.swing.JTextField();
        cstr = new GridBagConstraints();
        cstr.gridx = 0;
        cstr.gridy = 0;
        jPanel12.add(printer3javaposNameLbl, cstr);
        cstr = new GridBagConstraints();
        cstr.gridx = 1;
        cstr.gridy = 0;
        jPanel12.add(m_jtxtJPOSPrinter3, cstr);
        cstr = new GridBagConstraints();
        cstr.gridx = 0;
        cstr.gridy = 1;
        jPanel12.add(printer3javaposDrawerLbl, cstr);
        cstr = new GridBagConstraints();
        cstr.gridx = 1;
        cstr.gridy = 1;
        jPanel12.add(m_jtxtJPOSDrawer3, cstr);
        m_jPrinterParams3.add(jPanel12, "javapos");
        
        // Scale subparams
        jcboMachineScale.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jcboMachineScaleActionPerformed(evt);
            }
        });
        // empty
        jPanel16 = new JPanel();
        m_jScaleParams.add(jPanel16, "empty");
        // comm
        jPanel17 = new JPanel();
        jPanel17.setLayout(new GridBagLayout());
        jlblPrinterPort4 = WidgetsBuilder.createLabel(AppLocal.getIntString("label.machineprinterport"));
        jcboSerialScale = new JComboBox();
        jcboSerialScale.setEditable(true);
        cstr = new GridBagConstraints();
        cstr.gridx = 0;
        cstr.gridy = 0;
        jPanel17.add(jlblPrinterPort4, cstr);
        cstr = new GridBagConstraints();
        cstr.gridx = 1;
        cstr.gridy = 0;
        jPanel17.add(jcboSerialScale, cstr);
        m_jScaleParams.add(jPanel17, "comm");

        // Scanner subparams
        jcboMachineScanner.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jcboMachineScannerActionPerformed(evt);
            }
        });
        // empty
        jPanel24 = new JPanel();
        m_jScannerParams.add(jPanel24, "empty");
        // comm (serial)
        jPanel19 = new JPanel();
        jPanel19.setLayout(new GridBagLayout());
        jlblPrinterPort5 = WidgetsBuilder.createLabel(AppLocal.getIntString("label.machineprinterport"));
        jcboSerialScanner = new JComboBox();
        jcboSerialScanner.setEditable(true);
        cstr = new GridBagConstraints();
        cstr.gridx = 0;
        cstr.gridy = 0;
        jPanel19.add(jlblPrinterPort5, cstr);
        cstr = new GridBagConstraints();
        cstr.gridx = 1;
        cstr.gridy = 0;
        jPanel19.add(jcboSerialScanner, cstr);
        m_jScannerParams.add(jPanel19, "comm");
    }

    private void jcboMachineScannerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jcboMachineScannerActionPerformed
        CardLayout cl = (CardLayout) (m_jScannerParams.getLayout());

        if ("scanpal2".equals(jcboMachineScanner.getSelectedItem())) {
            cl.show(m_jScannerParams, "comm");
        } else {
            cl.show(m_jScannerParams, "empty");
        }
    }//GEN-LAST:event_jcboMachineScannerActionPerformed

    private void jcboMachineScaleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jcboMachineScaleActionPerformed
        CardLayout cl = (CardLayout) (m_jScaleParams.getLayout());

        if ("dialog1".equals(jcboMachineScale.getSelectedItem()) || "8217protocol".equals(jcboMachineScale.getSelectedItem()) || "samsungesp".equals(jcboMachineScale.getSelectedItem())) {
            cl.show(m_jScaleParams, "comm");
        } else {
            cl.show(m_jScaleParams, "empty");
        }
    }//GEN-LAST:event_jcboMachineScaleActionPerformed

    private void jcboMachinePrinter3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jcboMachinePrinter3ActionPerformed
        CardLayout cl = (CardLayout) (m_jPrinterParams3.getLayout());

        if ("epson".equals(jcboMachinePrinter3.getSelectedItem()) || "tmu220".equals(jcboMachinePrinter3.getSelectedItem()) || "star".equals(jcboMachinePrinter3.getSelectedItem()) || "ithaca".equals(jcboMachinePrinter3.getSelectedItem()) || "surepos".equals(jcboMachinePrinter3.getSelectedItem())) {
            cl.show(m_jPrinterParams3, "comm");
        } else if ("javapos".equals(jcboMachinePrinter3.getSelectedItem())) {
            cl.show(m_jPrinterParams3, "javapos");
        } else if ("printer".equals(jcboMachinePrinter3.getSelectedItem())) {
            cl.show(m_jPrinterParams3, "printer");
        } else {
            cl.show(m_jPrinterParams3, "empty");
        }
    }//GEN-LAST:event_jcboMachinePrinter3ActionPerformed

    private void jcboMachinePrinter2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jcboMachinePrinter2ActionPerformed
        CardLayout cl = (CardLayout) (m_jPrinterParams2.getLayout());

        if ("epson".equals(jcboMachinePrinter2.getSelectedItem()) || "tmu220".equals(jcboMachinePrinter2.getSelectedItem()) || "star".equals(jcboMachinePrinter2.getSelectedItem()) || "ithaca".equals(jcboMachinePrinter2.getSelectedItem()) || "surepos".equals(jcboMachinePrinter2.getSelectedItem())) {
            cl.show(m_jPrinterParams2, "comm");
        } else if ("javapos".equals(jcboMachinePrinter2.getSelectedItem())) {
            cl.show(m_jPrinterParams2, "javapos");
        } else if ("printer".equals(jcboMachinePrinter2.getSelectedItem())) {
            cl.show(m_jPrinterParams2, "printer");
         } else {
            cl.show(m_jPrinterParams2, "empty");
        }
    }//GEN-LAST:event_jcboMachinePrinter2ActionPerformed

    private void jcboMachineDisplayActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jcboMachineDisplayActionPerformed
        CardLayout cl = (CardLayout) (m_jDisplayParams.getLayout());

        if ("epson".equals(jcboMachineDisplay.getSelectedItem()) || "ld200".equals(jcboMachineDisplay.getSelectedItem()) || "surepos".equals(jcboMachineDisplay.getSelectedItem())) {
            cl.show(m_jDisplayParams, "comm");
        } else if ("javapos".equals(jcboMachineDisplay.getSelectedItem())) {
            cl.show(m_jDisplayParams, "javapos");
        } else {
            cl.show(m_jDisplayParams, "empty");
        }
    }//GEN-LAST:event_jcboMachineDisplayActionPerformed

    private void jcboMachinePrinterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jcboMachinePrinterActionPerformed
        CardLayout cl = (CardLayout) (m_jPrinterParams1.getLayout());

        if ("epson".equals(jcboMachinePrinter.getSelectedItem()) || "tmu220".equals(jcboMachinePrinter.getSelectedItem()) || "star".equals(jcboMachinePrinter.getSelectedItem()) || "ithaca".equals(jcboMachinePrinter.getSelectedItem()) || "surepos".equals(jcboMachinePrinter.getSelectedItem())) {
            cl.show(m_jPrinterParams1, "comm");
        } else if ("javapos".equals(jcboMachinePrinter.getSelectedItem())) {
            cl.show(m_jPrinterParams1, "javapos");
        } else if ("printer".equals(jcboMachinePrinter.getSelectedItem())) {
            cl.show(m_jPrinterParams1, "printer");
        } else {
            cl.show(m_jPrinterParams1, "empty");
        }
    }//GEN-LAST:event_jcboMachinePrinterActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox cboPrinters;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel25;
    private javax.swing.JLabel jLabel26;
    private javax.swing.JLabel jLabel27;
    private javax.swing.JLabel jLabel28;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel11;
    private javax.swing.JPanel jPanel12;
    private javax.swing.JPanel jPanel13;
    private javax.swing.JPanel jPanel16;
    private javax.swing.JPanel jPanel17;
    private javax.swing.JPanel jPanel19;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel24;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JComboBox jcboConnDisplay;
    private javax.swing.JComboBox jcboConnPrinter;
    private javax.swing.JComboBox jcboConnPrinter2;
    private javax.swing.JComboBox jcboConnPrinter3;
    private javax.swing.JComboBox jcboLAF;
    private javax.swing.JComboBox jcboMachineDisplay;
    private javax.swing.JComboBox jcboMachinePrinter;
    private javax.swing.JComboBox jcboMachinePrinter2;
    private javax.swing.JComboBox jcboMachinePrinter3;
    private javax.swing.JComboBox jcboMachineScale;
    private javax.swing.JComboBox jcboMachineScanner;
    private javax.swing.JComboBox jcboMachineScreenmode;
    private javax.swing.JComboBox jcboSerialDisplay;
    private javax.swing.JComboBox jcboSerialPrinter;
    private javax.swing.JComboBox jcboSerialPrinter2;
    private javax.swing.JComboBox jcboSerialPrinter3;
    private javax.swing.JComboBox jcboSerialScale;
    private javax.swing.JComboBox jcboSerialScanner;
    private javax.swing.JComboBox jcboTicketsBag;
    private javax.swing.JLabel jlblConnPrinter;
    private javax.swing.JLabel jlblConnPrinter2;
    private javax.swing.JLabel jlblConnPrinter3;
    private javax.swing.JLabel jlblPrinterPort;
    private javax.swing.JLabel jlblPrinterPort2;
    private javax.swing.JLabel jlblPrinterPort3;
    private javax.swing.JLabel jlblPrinterPort4;
    private javax.swing.JLabel jlblPrinterPort5;
    private javax.swing.JTextField jtxtMachineHostname;
    private javax.swing.JPanel m_jDisplayParams;
    private javax.swing.JPanel m_jPrinterParams1;
    private javax.swing.JPanel m_jPrinterParams2;
    private javax.swing.JPanel m_jPrinterParams3;
    private javax.swing.JPanel m_jScaleParams;
    private javax.swing.JPanel m_jScannerParams;
    private javax.swing.JTextField m_jtxtJPOSDrawer;
    private javax.swing.JTextField m_jtxtJPOSDrawer2;
    private javax.swing.JTextField m_jtxtJPOSDrawer3;
    private javax.swing.JTextField m_jtxtJPOSName;
    private javax.swing.JTextField m_jtxtJPOSPrinter;
    private javax.swing.JTextField m_jtxtJPOSPrinter2;
    private javax.swing.JTextField m_jtxtJPOSPrinter3;
    // End of variables declaration//GEN-END:variables
    private javax.swing.JLabel jlblScreenType;
    private javax.swing.JComboBox m_jcboScreenType;
    private javax.swing.JCheckBox jAutoHideMenu;
    private javax.swing.JCheckBox jcbShowTitlebar;
    private javax.swing.JCheckBox jcbShowFooterbar;
    private javax.swing.JTextField jtxtScreenDensity;
    private javax.swing.JCheckBox jcbAutoHideMenu;
    private javax.swing.JComboBox jcboMarginType;
    private javax.swing.JCheckBox jcbAutoPrint;
}
