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

package fr.pasteque.pos.forms;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Locale;
import java.util.Properties;
import java.util.logging.Logger;

/**
 *
 * @author adrianromero
 */
public class AppConfig implements AppProperties {

    private static Logger logger = Logger.getLogger("fr.pasteque.pos.forms.AppConfig");

    public static AppConfig loadedInstance;

    private Properties m_propsconfig;
    private File configfile;

    public AppConfig(String[] args) {
        if (args.length == 0) {
            this.init(this.getDefaultConfig());
        } else {
            this.init(new File(args[0]));
        }
    }

    public AppConfig(File configfile) {
        this.init(configfile);
    }

    private void init(File configfile) {
        this.configfile = configfile;
        m_propsconfig = new Properties();

        logger.info("Reading configuration file: " + configfile.getAbsolutePath());
    }

    private File getDefaultConfig() {
        return new File(new File(System.getProperty("user.home")),
                "." + AppLocal.APP_ID + ".properties");
    }
    private File getDefaultRestoreConfig() {
        return new File(new File(System.getProperty("user.home")),
                "." + AppLocal.APP_ID + ".properties.restore");
    }

    public String getProperty(String sKey) {
        String prop = m_propsconfig.getProperty(sKey);
        if (prop == null) {
            prop = DEFAULT_VALUES.get(sKey);
        }
        return prop;
    }

    /** Shortcut to getProperty("users.activated") with parsing.
     * @returns The list of activated user ids or null if all.
     */
    public String[] getEnabledUsers() {
        String prop = getProperty("users.activated");
        if (prop.equals("all")) {
            return null;
        }
        String ids[] = prop.split(",");
        for (String id : ids) {
            id = id.trim();
        }
        return ids;
    }

    public String getHost() {
        return this.getProperty("machine.hostname");
    }

    public File getConfigFile() {
        return this.configfile;
    }

    public void setProperty(String sKey, String sValue) {
        if (sValue == null) {
            m_propsconfig.remove(sKey);
        } else {
            m_propsconfig.setProperty(sKey, sValue);
        }
    }

    private String getLocalHostName() {
        try {
            return java.net.InetAddress.getLocalHost().getHostName();
        } catch (java.net.UnknownHostException eUH) {
            return "localhost";
        }
    }

    public String getLocale() {
        String slang = this.getProperty("user.language");
        String scountry = this.getProperty("user.country");
        //String svariant = this.getProperty("user.variant"); Unused
        String locCode = slang;
        if (scountry != null) {
            locCode += "_" + scountry;
        }
        return locCode;
    }

    public boolean delete() {
        this.loadDefault();
        return this.configfile.delete();
    }
    public void restore() throws IOException {
        File restore = new File(this.configfile.getAbsolutePath() + ".restore");
        File currentConfig = this.configfile;
        if (restore.exists()) {
            this.configfile = restore;
            this.load();
            this.configfile = currentConfig; // keep same file for save
            logger.info("Restored configuration from: " + restore.getAbsolutePath());
        } else {
            // Try with default restore file
            File defaultConfig = this.getDefaultRestoreConfig();
            if (defaultConfig.exists()) {
                this.configfile = defaultConfig;
                this.load();
                this.configfile = currentConfig;
                logger.info("Restored configuration from: " + defaultConfig.getAbsolutePath());
            } else {
                // No custom restore settings, use default.
                this.loadDefault();
                logger.info("Restored default configuration");
            }
        }
        this.save();
    }

    public void load() {
        loadDefault();
        try {
            InputStream in = new FileInputStream(configfile);
            if (in != null) {
                m_propsconfig.load(in);
                in.close();
            }
        } catch (IOException e){
            this.loadDefault();
        }
        AppConfig.loadedInstance = this;
    }

    public void save() throws IOException {
        OutputStream out = new FileOutputStream(configfile);
        if (out != null) {
            m_propsconfig.store(out, AppLocal.APP_NAME
                    + ". Configuration file.");
            out.close();
        }
    }

    /** The defaults values. These values are taken when the property is not
     * found in file. These values are not saved in properties files if they
     * are not set.
     */
    private static HashMap<String, String> DEFAULT_VALUES;
    static {
        DEFAULT_VALUES = new HashMap<String, String>();
        DEFAULT_VALUES.put("ui.touchbtnminwidth", "0.4"); // in inches
        DEFAULT_VALUES.put("ui.touchbtnminheight", "0.4"); // in inches
        DEFAULT_VALUES.put("ui.touchbigbtnminwidth", "0.5"); // in inches
        DEFAULT_VALUES.put("ui.touchbigbtnminheight", "0.5"); // in inches
        DEFAULT_VALUES.put("ui.touchsmallbtnminwidth", "0.3"); // in inches
        DEFAULT_VALUES.put("ui.touchsmallbtnminheight", "0.3"); // in inches
        DEFAULT_VALUES.put("ui.touchbtnspacing", "0.08"); // in inches
        DEFAULT_VALUES.put("ui.fontsize", "12");
        DEFAULT_VALUES.put("ui.fontsizebig", "14");
        DEFAULT_VALUES.put("ui.fontsizesmall", "10");
        DEFAULT_VALUES.put("ui.showupdownbuttons", "1");
        DEFAULT_VALUES.put("ui.margintype", "percent");
        DEFAULT_VALUES.put("prices.setmode", "taxed");
        DEFAULT_VALUES.put("prices.roundto", "0");
        DEFAULT_VALUES.put("server.backoffice", "http://pt.scil.coop/pasteque");
        DEFAULT_VALUES.put("db.user", "pt_demo");
        DEFAULT_VALUES.put("db.password", "demo");
        DEFAULT_VALUES.put("ui.printticketbydefault", "1");
    }

    /** Load "default file", which values are expanded or overriden by the
     * actually property file.
     * The properties that then saved along other in the properties file.
     */
    private void loadDefault() {
        m_propsconfig = new Properties();
        String dirname = System.getProperty("dirname.path");
        dirname = dirname == null ? "./" : dirname;

        m_propsconfig.setProperty("db.driverlib", new File(new File(dirname), "lib/mysql-connector-java-5.1.16.jar").getAbsolutePath());
        m_propsconfig.setProperty("db.driver", "com.mysql.jdbc.Driver");
        m_propsconfig.setProperty("db.URL", "jdbc:mysql://pt.scil.coop:3306/pt_demo");

//        m_propsconfig.setProperty("db.driverlib", new File(new File(dirname), "lib/hsqldb.jar").getAbsolutePath());
//        m_propsconfig.setProperty("db.driver", "org.hsqldb.jdbcDriver");
//        m_propsconfig.setProperty("db.URL", "jdbc:hsqldb:file:" + new File(new File(System.getProperty("user.home")), AppLocal.APP_ID + "-db").getAbsolutePath() + ";shutdown=true");
//        m_propsconfig.setProperty("db.user", "sa");
//        m_propsconfig.setProperty("db.password", "");

//        m_propsconfig.setProperty("db.driver", "com.mysql.jdbc.Driver");
//        m_propsconfig.setProperty("db.URL", "jdbc:mysql://localhost:3306/database");
//        m_propsconfig.setProperty("db.user", "user");
//        m_propsconfig.setProperty("db.password", "password");

//        m_propsconfig.setProperty("db.driver", "org.postgresql.Driver");
//        m_propsconfig.setProperty("db.URL", "jdbc:postgresql://localhost:5432/database");
//        m_propsconfig.setProperty("db.user", "user");
//        m_propsconfig.setProperty("db.password", "password");

        m_propsconfig.setProperty("machine.hostname", getLocalHostName());

        Locale l = Locale.getDefault();
        m_propsconfig.setProperty("user.language", l.getLanguage());
        m_propsconfig.setProperty("user.country", l.getCountry());
        m_propsconfig.setProperty("user.variant", l.getVariant());

        m_propsconfig.setProperty("users.activated", "all");

        m_propsconfig.setProperty("swing.defaultlaf", System.getProperty("swing.defaultlaf", "javax.swing.plaf.metal.MetalLookAndFeel"));

        m_propsconfig.setProperty("machine.printer", "screen");
        m_propsconfig.setProperty("machine.printer.2", "Not defined");
        m_propsconfig.setProperty("machine.printer.3", "Not defined");
        m_propsconfig.setProperty("machine.display", "screen");
        m_propsconfig.setProperty("machine.scale", "Not defined");
        m_propsconfig.setProperty("machine.screenmode", "window"); // fullscreen / window
        m_propsconfig.setProperty("machine.screentype", "standard");
        m_propsconfig.setProperty("machine.ticketsbag", "standard");
        m_propsconfig.setProperty("machine.scanner", "Not defined");

        m_propsconfig.setProperty("payment.gateway", "external");
        m_propsconfig.setProperty("payment.magcardreader", "Not defined");
        m_propsconfig.setProperty("payment.testmode", "false");
        m_propsconfig.setProperty("payment.commerceid", "");
        m_propsconfig.setProperty("payment.commercepassword", "password");

        m_propsconfig.setProperty("machine.printername", "(Default)");

        // Receipt printer paper set to 72mmx200mm
        m_propsconfig.setProperty("paper.receipt.x", "10");
        m_propsconfig.setProperty("paper.receipt.y", "287");
        m_propsconfig.setProperty("paper.receipt.width", "190");
        m_propsconfig.setProperty("paper.receipt.height", "546");
        m_propsconfig.setProperty("paper.receipt.mediasizename", "A4");

        // Normal printer paper for A4
        m_propsconfig.setProperty("paper.standard.x", "72");
        m_propsconfig.setProperty("paper.standard.y", "72");
        m_propsconfig.setProperty("paper.standard.width", "451");
        m_propsconfig.setProperty("paper.standard.height", "698");
        m_propsconfig.setProperty("paper.standard.mediasizename", "A4");

        m_propsconfig.setProperty("machine.uniqueinstance", "false");

        // UI stuff
        m_propsconfig.setProperty("machine.screendensity", "72"); // In pixel per inch
        m_propsconfig.setProperty("ui.autohidemenu", "0");
        m_propsconfig.setProperty("ui.showtitlebar", "1");
        m_propsconfig.setProperty("ui.showfooterbar", "1");

    }
}
