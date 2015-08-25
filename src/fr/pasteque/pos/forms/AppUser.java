//    Pasteque is based uppon OpenBravo POS
//    Openbravo POS is a point of sales application designed for touch screens.
//    Copyright (C) 2008-2009 Openbravo, S.L.
//                  2015 Scil
//    Philippe Pary
//
//    This file is part of Openbravo POS.
//
//    Openbravo POS is free software: you can redistribute it and/or modify
//    it under the terms of the GNU General Public License as published by
//    the Free Software Foundation, either version 3 of the License, or
//    (at your option) any later version.
//
//    Openbravo POS is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//    GNU General Public License for more details.
//
//    You should have received a copy of the GNU General Public License
//    along with Openbravo POS.  If not, see <http://www.gnu.org/licenses/>.

package fr.pasteque.pos.forms;

import fr.pasteque.basic.BasicException;
import fr.pasteque.data.loader.LocalRes;
import fr.pasteque.pos.ticket.UserInfo;
import fr.pasteque.pos.util.Hashcypher;

import java.io.IOException;
import java.io.Serializable;
import java.io.StringReader;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Icon;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * @author adrianromero
 */
public class AppUser implements Serializable {

    static final long serialVersionUID = 2043301290925208037L;
    private static Logger logger = Logger.getLogger("fr.pasteque.pos.forms.AppUser");

    private static SAXParser m_sp = null;
    private static HashMap<String, String> m_oldclasses; // This is for backwards compatibility purposes

    private String m_sId;
    private String m_sName;
    private String m_sCard;
    private String m_sPassword;
    private String roleId;
    private boolean visible;
    private Icon m_Icon;

    private Set<String> m_apermissions;

    static {
        initOldClasses();
    }

    /** Creates a new instance of AppUser */
    public AppUser(String id, String name, String password, String card,
            String roleId, Icon icon, boolean visible) {
        m_sId = id;
        m_sName = name;
        m_sPassword = password;
        m_sCard = card;
        this.roleId = roleId;
        m_Icon = icon;
        m_apermissions = null;
        this.visible = visible;
    }

    public Icon getIcon() {
        return m_Icon;
    }

    public String getId() {
        return m_sId;
    }

    public String getName() {
        return m_sName;
    }

    public void setPassword(String sValue) {
        m_sPassword = sValue;
    }

    public String getPassword() {
        return m_sPassword;
    }

    public String getRoleId() {
        return this.roleId;
    }

    public String getCard() {
        return m_sCard;
    }

    public boolean isVisible() {
        return this.visible;
    }

    public boolean authenticate() {
        return m_sPassword == null || m_sPassword.equals("")
                || m_sPassword.startsWith("empty:");
    }
    public boolean authenticate(String sPwd) {
        return Hashcypher.authenticate(sPwd, m_sPassword);
    }

    public void fillPermissions(DataLogicSystem dlSystem) {

        // inicializamos los permisos
        m_apermissions = new HashSet<String>();
        // Y lo que todos tienen permisos
        m_apermissions.add("fr.pasteque.pos.forms.JPanelMenu");
        m_apermissions.add("Menu.Exit");

        String sRolePermisions = null;
        try {
            sRolePermisions = dlSystem.findRolePermissions(this.roleId);
        } catch (BasicException e) {
            logger.log(Level.SEVERE, "Unable to load permissions", e);
        }

        if (sRolePermisions != null) {
            try {
                if (m_sp == null) {
                    SAXParserFactory spf = SAXParserFactory.newInstance();
                    m_sp = spf.newSAXParser();
                }
                m_sp.parse(new InputSource(new StringReader(sRolePermisions)), new ConfigurationHandler());

            } catch (ParserConfigurationException ePC) {
                logger.log(Level.WARNING, LocalRes.getIntString("exception.parserconfig"), ePC);
            } catch (SAXException eSAX) {
                logger.log(Level.WARNING, LocalRes.getIntString("exception.xmlfile"), eSAX);
            } catch (IOException eIO) {
                logger.log(Level.WARNING, LocalRes.getIntString("exception.iofile"), eIO);
            }
        }

    }

    public boolean hasPermission(String classname) {
        if (m_apermissions == null) {
            return false;
        } else {
            return m_apermissions.contains(classname);
        }
    }

    public UserInfo getUserInfo() {
        return new UserInfo(m_sId, m_sName);
    }

    private static String mapNewClass(String classname) {
        String newclass = m_oldclasses.get(classname);
        return newclass == null ? classname : newclass;
    }

    private static void initOldClasses() {
        m_oldclasses = new HashMap<String, String>();

        // update permissions from 0.0.24 to 2.20
        m_oldclasses.put("net.adrianromero.tpv.panelsales.JPanelTicketSales", "fr.pasteque.pos.sales.JPanelTicketSales");
        m_oldclasses.put("net.adrianromero.tpv.panelsales.JPanelTicketEdits", "fr.pasteque.pos.sales.JPanelTicketEdits");
        m_oldclasses.put("net.adrianromero.tpv.panels.JPanelPayments", "fr.pasteque.pos.panels.JPanelPayments");
        m_oldclasses.put("net.adrianromero.tpv.panels.JPanelCloseMoney", "fr.pasteque.pos.panels.JPanelCloseMoney");
        m_oldclasses.put("net.adrianromero.tpv.reports.JReportClosedPos", "/fr.pasteque.reports/closedpos.bs");

        m_oldclasses.put("Menu.StockManagement", "fr.pasteque.pos.forms.MenuStockManagement");
        m_oldclasses.put("net.adrianromero.tpv.inventory.ProductsPanel", "fr.pasteque.pos.inventory.ProductsPanel");
        m_oldclasses.put("net.adrianromero.tpv.inventory.CategoriesPanel", "fr.pasteque.pos.inventory.CategoriesPanel");
        m_oldclasses.put("net.adrianromero.tpv.panels.JPanelTax", "fr.pasteque.pos.inventory.TaxPanel");
        m_oldclasses.put("net.adrianromero.tpv.reports.JReportInventory", "/fr.pasteque.reports/inventory.bs");
        m_oldclasses.put("net.adrianromero.tpv.reports.JReportInventory2", "/fr.pasteque.reports/inventoryb.bs");
        m_oldclasses.put("net.adrianromero.tpv.reports.JReportInventoryBroken", "/fr.pasteque.reports/inventorybroken.bs");
        m_oldclasses.put("net.adrianromero.tpv.reports.JReportInventoryDiff", "/fr.pasteque.reports/inventorydiff.bs");

        m_oldclasses.put("Menu.Maintenance", "fr.pasteque.pos.forms.MenuMaintenance");
        m_oldclasses.put("net.adrianromero.tpv.admin.PeoplePanel", "fr.pasteque.pos.admin.PeoplePanel");
        m_oldclasses.put("net.adrianromero.tpv.admin.RolesPanel", "fr.pasteque.pos.admin.RolesPanel");
        m_oldclasses.put("net.adrianromero.tpv.admin.ResourcesPanel", "fr.pasteque.pos.admin.ResourcesPanel");
        m_oldclasses.put("net.adrianromero.tpv.mant.JPanelFloors", "fr.pasteque.pos.mant.JPanelFloors");
        m_oldclasses.put("net.adrianromero.tpv.mant.JPanelPlaces", "fr.pasteque.pos.mant.JPanelPlaces");
        m_oldclasses.put("fr.pasteque.possync.ProductsSync", "com.openbravo.possync.ProductsSyncCreate");
        m_oldclasses.put("fr.pasteque.possync.OrdersSync", "com.openbravo.possync.OrdersSyncCreate");

        m_oldclasses.put("Menu.ChangePassword", "Menu.ChangePassword");
        m_oldclasses.put("net.adrianromero.tpv.panels.JPanelPrinter", "fr.pasteque.pos.panels.JPanelPrinter");
        m_oldclasses.put("net.adrianromero.tpv.config.JPanelConfiguration", "fr.pasteque.pos.config.JPanelConfiguration");

//        m_oldclasses.put("button.print", "");
//        m_oldclasses.put("button.opendrawer", "");

        // update permissions from 2.00 to 2.20
        m_oldclasses.put("fr.pasteque.pos.reports.JReportClosedPos", "/com/openbravo/reports/closedpos.bs");
        m_oldclasses.put("fr.pasteque.pos.reports.JReportClosedProducts", "/com/openbravo/reports/closedproducts.bs");
        m_oldclasses.put("fr.pasteque.pos.reports.JChartSales", "/com/openbravo/reports/chartsales.bs");
        m_oldclasses.put("fr.pasteque.pos.reports.JReportInventory", "/com/openbravo/reports/inventory.bs");
        m_oldclasses.put("fr.pasteque.pos.reports.JReportInventory2", "/com/openbravo/reports/inventoryb.bs");
        m_oldclasses.put("fr.pasteque.pos.reports.JReportInventoryBroken", "/com/openbravo/reports/inventorybroken.bs");
        m_oldclasses.put("fr.pasteque.pos.reports.JReportInventoryDiff", "/com/openbravo/reports/inventorydiff.bs");
        m_oldclasses.put("fr.pasteque.pos.reports.JReportTaxes", "/com/openbravo/reports/taxes.bs");
        m_oldclasses.put("fr.pasteque.pos.reports.JReportUserSales", "/com/openbravo/reports/usersales.bs");

        // update permissions from 2.10 to 2.20
        m_oldclasses.put("fr.pasteque.pos.panels.JPanelTax", "com.openbravo.pos.inventory.TaxPanel");

    }

    private class ConfigurationHandler extends DefaultHandler {
        @Override
        public void startDocument() throws SAXException {}
        @Override
        public void endDocument() throws SAXException {}
        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException{
            if ("class".equals(qName)){
                m_apermissions.add(mapNewClass(attributes.getValue("name")));
            }
        }
        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {}
        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {}
    }

    @Override
    public String toString() {
        return this.m_sName;
    }
}
