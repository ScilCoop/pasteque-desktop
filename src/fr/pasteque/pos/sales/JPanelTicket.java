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

package fr.pasteque.pos.sales;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Date;

import fr.pasteque.format.Formats;
import fr.pasteque.data.gui.ComboBoxValModel;
import fr.pasteque.data.gui.MessageInf;
import fr.pasteque.pos.printer.*;

import fr.pasteque.pos.forms.JPanelView;
import fr.pasteque.pos.forms.AppView;
import fr.pasteque.pos.forms.AppLocal;
import fr.pasteque.pos.panels.JProductFinder;
import fr.pasteque.pos.scale.ScaleException;
import fr.pasteque.pos.payment.JPaymentSelect;
import fr.pasteque.basic.BasicException;
import fr.pasteque.data.gui.ListKeyed;
import fr.pasteque.data.loader.ImageLoader;
import fr.pasteque.data.loader.SentenceList;
import fr.pasteque.pos.catalog.CatalogSelector;
import fr.pasteque.pos.catalog.JCatalogSubgroups;
import fr.pasteque.pos.customers.CustomerInfoExt;
import fr.pasteque.pos.customers.DataLogicCustomers;
import fr.pasteque.pos.customers.DiscountProfile;
import fr.pasteque.pos.customers.JCustomerFinder;
import fr.pasteque.pos.scripting.ScriptEngine;
import fr.pasteque.pos.scripting.ScriptException;
import fr.pasteque.pos.scripting.ScriptFactory;
import fr.pasteque.pos.forms.DataLogicSystem;
import fr.pasteque.pos.forms.DataLogicSales;
import fr.pasteque.pos.forms.BeanFactoryApp;
import fr.pasteque.pos.forms.BeanFactoryException;
import fr.pasteque.pos.forms.AppConfig;
import fr.pasteque.pos.inventory.TaxCategoryInfo;
import fr.pasteque.pos.payment.JPaymentSelectReceipt;
import fr.pasteque.pos.payment.JPaymentSelectRefund;
import fr.pasteque.pos.sales.restaurant.JTicketsBagRestaurant;
import fr.pasteque.pos.sales.restaurant.JTicketsBagRestaurantMap;
import fr.pasteque.pos.ticket.ProductInfoExt;
import fr.pasteque.pos.ticket.TariffInfo;
import fr.pasteque.pos.ticket.TaxInfo;
import fr.pasteque.pos.ticket.TicketInfo;
import fr.pasteque.pos.ticket.TicketLineInfo;
import fr.pasteque.pos.util.JRPrinterAWT300;
import fr.pasteque.pos.util.ReportUtils;
import fr.pasteque.pos.widgets.JEditorCurrencyPositive;
import fr.pasteque.pos.widgets.JEditorKeys;
import fr.pasteque.pos.widgets.JNumberEvent;
import fr.pasteque.pos.widgets.JNumberEventListener;
import fr.pasteque.pos.widgets.JNumberKeys;
import fr.pasteque.pos.widgets.WidgetsBuilder;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.print.PrintService;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRMapArrayDataSource;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.xml.JRXmlLoader;
/**
 *
 * @author adrianromero
 */
public abstract class JPanelTicket extends JPanel implements JPanelView, BeanFactoryApp, TicketsEditor, DataLogicCustomers.CustomerListener {

    private static Logger logger = Logger.getLogger("fr.pasteque.pos.sales.JPanelTicket");

    protected JTicketLines m_ticketlines;

    // private Template m_tempLine;
    private TicketParser m_TTP;

    protected TicketInfo m_oTicket; 
    protected Object m_oTicketExt; 

    /** True when picking components */
    private boolean m_bIsSubproduct;
    /** Quantity of current composition (when picking a composition) */
    protected double m_dMultiply;
    /** The catalog currently displayed (for compositions) */
    protected Component m_catalog;
    protected CatalogSelector m_cat;
    /** Current composition state (PRODUCT_* values) */
    protected int m_iProduct;

    private StringBuffer m_sBarcode;

    private JTicketsBag m_ticketsbag;

    private ListKeyed taxcollection;
    // private ComboBoxValModel m_TaxModel;

    private ListKeyed taxcategoriescollection;
    private ComboBoxValModel taxcategoriesmodel;
    
    private TaxesLogic taxeslogic;

    private List m_TariffList;
    private ComboBoxValModel m_TariffModel;

//    private ScriptObject scriptobjinst;
    protected JPanelButtons m_jbtnconfig;

    protected AppView m_App;
    protected DataLogicSystem dlSystem;
    protected DataLogicSales dlSales;
    protected DataLogicCustomers dlCustomers;
    protected Timer clockTimer;

    private JPaymentSelect paymentdialogreceipt;
    private JPaymentSelect paymentdialogrefund;

    //State variables for new automate
    private int m_InputState;
    private static final int I_NOTHING = 0;
    private static final int I_PRICE = 1;
    private static final int I_QUANTITY = 2;

    private int m_PriceActualState;
    private int m_PricePreviousState;
    private int m_QuantityActualState;
    private int m_QuantityPreviousState;
    private static final int N_NOTHING = 0;
    private static final int N_ZERO = 1;
    private static final int N_DECIMALZERO = 2;
    private static final int N_NUMBER = 3;
    private static final int N_DECIMALNUMBER = 4;
    private static final int N_DECIMAL = 5;

    protected static final int PRODUCT_SINGLE = 0;
    protected static final int PRODUCT_COMPOSITION = 1;
    protected static final int PRODUCT_SUBGROUP = 2;

    private static final char keyBack = '\u0008';
    private static final char keyDel = '\u007f';
    private static final char keySection = '\u00a7';
    private static final char keyEnter = '\n';

    private static final int AUTO_PERFORMED = 0;
    private static final int BUTTON_PERFORMED = 1;


    /** Creates new form JTicketView */
    public JPanelTicket() {
        
        initComponents ();
    }

    public void init(AppView app) throws BeanFactoryException {
        m_App = app;
        this.dlSystem = new DataLogicSystem();
        this.dlSales = new DataLogicSales();
        this.dlCustomers = new DataLogicCustomers();

        m_ticketsbag = getJTicketsBag();
        m_jPanelBag.add(m_ticketsbag.getBagComponent(), BorderLayout.LINE_START);
        add(m_ticketsbag.getNullComponent(), "null");

        m_ticketlines.init(dlSystem.getResourceAsXML("Ticket.Line"));
        
        m_TTP = new TicketParser(m_App.getDeviceTicket(), dlSystem);
               
        // Los botones configurables...
        m_jbtnconfig = new JPanelButtons("Ticket.Buttons", this);
        m_jButtonsExt.add(m_jbtnconfig);           
       
        // El panel de los productos o de las lineas...
        m_catalog = getSouthComponent();
        catcontainer.add(getSouthComponent(), BorderLayout.CENTER);
        m_iProduct = PRODUCT_SINGLE;
        
        // Tariff areas
        m_TariffModel = new ComboBoxValModel();

        // ponemos a cero el estado
        eraseAutomator();

        // inicializamos
        m_oTicket = null;
        m_oTicketExt = null;

        m_PriceActualState = N_NOTHING;
        m_QuantityActualState = N_NOTHING;
        m_InputState = I_PRICE;
    }
    
    public Object getBean() {
        return this;
    }

    protected Component getSouthAuxComponent() {
        m_cat = new JCatalogSubgroups(dlSales,
                "true".equals(m_jbtnconfig.getProperty("pricevisible")),
                "true".equals(m_jbtnconfig.getProperty("taxesincluded")),
                Integer.parseInt(m_jbtnconfig.getProperty("img-width", "64")),
                Integer.parseInt(m_jbtnconfig.getProperty("img-height", "54")));
        m_cat.getComponent().setPreferredSize(new Dimension(
                0,
                Integer.parseInt(m_jbtnconfig.getProperty("cat-height", "245"))));
        m_cat.addActionListener(new CatalogListener());
        ((JCatalogSubgroups)m_cat).setGuideMode(true);
        return m_cat.getComponent();
    }

    public JComponent getComponent() {
        return this;
    }

    public void activate() throws BasicException {

        paymentdialogreceipt = JPaymentSelectReceipt.getDialog(this);
        paymentdialogreceipt.init(m_App);
        paymentdialogrefund = JPaymentSelectRefund.getDialog(this); 
        paymentdialogrefund.init(m_App);
        
        // impuestos incluidos seleccionado ?
        m_jaddtax.setSelected("true".equals(m_jbtnconfig.getProperty("taxesincluded")));

        // Inicializamos el combo de los impuestos.
        java.util.List<TaxInfo> taxlist = this.dlSales.getTaxList();
        taxcollection = new ListKeyed<TaxInfo>(taxlist);
        java.util.List<TaxCategoryInfo> taxcategorieslist = this.dlSales.getTaxCategoriesList();
        taxcategoriescollection = new ListKeyed<TaxCategoryInfo>(taxcategorieslist);
        
        taxcategoriesmodel = new ComboBoxValModel(taxcategorieslist);
        m_jTax.setModel(taxcategoriesmodel);

        String taxesid = m_jbtnconfig.getProperty("taxcategoryid");
        if (taxesid == null) {
            if (m_jTax.getItemCount() > 0) {
                m_jTax.setSelectedIndex(0);
            }
        } else {
            taxcategoriesmodel.setSelectedKey(taxesid);
        }              
                
        taxeslogic = new TaxesLogic(taxlist);
        
        // Show taxes options
        if (m_App.getAppUserView().getUser().hasPermission("sales.ChangeTaxOptions")) {
            m_jTax.setVisible(true);
            m_jaddtax.setVisible(true);
        } else {
            m_jTax.setVisible(false);
            m_jaddtax.setVisible(false);
        }

        // Initialize tariff area combobox
        m_TariffList = this.dlSales.getTariffAreaList();
        TariffInfo defaultArea = new TariffInfo(-1,
                AppLocal.getIntString("Label.DefaultTariffArea"));
        m_TariffList.add(0, defaultArea);
        m_TariffModel = new ComboBoxValModel(m_TariffList);
        m_jTariff.setModel(m_TariffModel);
        if (m_TariffList.size() > 1) {
            this.updateTariffCombo();
            m_jTariff.setVisible(true);
        } else {
            m_jTariff.setVisible(false);
        }

        // Authorization for buttons
        btnSplit.setEnabled(m_App.getAppUserView().getUser().hasPermission("sales.Total"));
        m_jDelete.setEnabled(m_App.getAppUserView().getUser().hasPermission("sales.EditLines"));
        m_jNumberKeys.setMinusEnabled(m_App.getAppUserView().getUser().hasPermission("sales.EditLines"));
        m_jNumberKeys.setEqualsEnabled(m_App.getAppUserView().getUser().hasPermission("sales.Total"));
        m_jbtnconfig.setPermissions(m_App.getAppUserView().getUser());  
               
        m_ticketsbag.activate();

        // Start clock
        this.updateClock();
        Calendar c = Calendar.getInstance();
        c.add(Calendar.MINUTE, 1);
        c.set(Calendar.SECOND, 0);
        this.clockTimer = new Timer();
        this.clockTimer.schedule(new TimerTask(){
                public void run() {
                    updateClock();
                }
            }, c.getTime(), 60000);
    }
    
    public boolean deactivate() {
        this.clockTimer.cancel();
        this.clockTimer = null;
        return m_ticketsbag.deactivate();
    }
    
    protected abstract JTicketsBag getJTicketsBag();
    protected abstract Component getSouthComponent();
    protected abstract void resetSouthComponent();

    protected void updateClock() {
        Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minutes = c.get(Calendar.MINUTE);
        String strHour = String.valueOf(hour);
        if (strHour.length() == 1) {
            strHour = "0" + strHour;
        }
        String strMinute = String.valueOf(minutes);
        if (strMinute.length() == 1) {
            strMinute = "0" + strMinute;
        }
        this.clock.setText(AppLocal.getIntString("Clock.Format",
                        strHour, strMinute));
    }
     
    public void setActiveTicket(TicketInfo oTicket, Object oTicketExt) {
       
        m_oTicket = oTicket;
        m_oTicketExt = oTicketExt;
        
        if (m_oTicket != null) {            
            // Asign preeliminary properties to the receipt
            m_oTicket.setUser(m_App.getAppUserView().getUser().getUserInfo());
            m_oTicket.setActiveCash(m_App.getActiveCashIndex());
            m_oTicket.setDate(new Date()); // Set the edition date.
        }
        
        executeEvent(m_oTicket, m_oTicketExt, "ticket.show");
        
        refreshTicket();
        this.updateTariffCombo();
    }
    
    public TicketInfo getActiveTicket() {
        return m_oTicket;
    }
    
    public void setCustomersCount(int count) {
        this.m_oTicket.setCustomersCount(count);
        this.refreshTicketLabel();
    }

    private void refreshTicketLabel() {
        // Better view of customer's name by changing the color of text and remove hour and ticket's id 
        String name = null;
        if (m_oTicket.getCustomer() != null) {
            CustomerInfoExt customerName = m_oTicket.getCustomer();
            name = customerName.getName();
            Color green = new Color(33,67,92);
            m_jTicketId.setBackground(green);
            m_jTicketId.setForeground(java.awt.Color.WHITE);
        } else {
            name = m_oTicket.getName(m_oTicketExt);
            m_jTicketId.setForeground(java.awt.Color.DARK_GRAY);
            m_jTicketId.setBackground(java.awt.Color.WHITE);
        }
        if (m_oTicket.hasCustomersCount()) {
            name += " (" + m_oTicket.getCustomersCount() + ")";
        }
        m_jTicketId.setText(name);
    }

    private void refreshTicket() {
        
        CardLayout cl = (CardLayout)(getLayout());
        
        if (m_oTicket == null) {        
            m_jTicketId.setText(null);            
            m_ticketlines.clearTicketLines();
           
            this.subtotalLabel.setText(null);
            m_jTotalEuros.setText(null); 
            this.discountLabel.setText(null);
        
            eraseAutomator();
            
            // Muestro el panel de nulos.
            cl.show(this, "null");  
            resetSouthComponent();

        } else {
            if (m_oTicket.getTicketType() == TicketInfo.RECEIPT_REFUND) {
                //Make disable Search and Edit Buttons
                m_jEditLine.setVisible(false);
                m_jList.setVisible(false);
            }
            
            // Refresh ticket taxes
            for (TicketLineInfo line : m_oTicket.getLines()) {
                line.setTaxInfo(taxeslogic.getTaxInfo(line.getProductTaxCategoryID(), m_oTicket.getDate(), m_oTicket.getCustomer()));
            }  
        
            // The ticket name
            this.refreshTicketLabel();

            // Limpiamos todas las filas y anadimos las del ticket actual
            m_ticketlines.clearTicketLines();

            for (int i = 0; i < m_oTicket.getLinesCount(); i++) {
                m_ticketlines.addTicketLine(m_oTicket.getLine(i));
            }
            printPartialTotals();
            eraseAutomator();
            
            // Muestro el panel de tickets.
            cl.show(this, "ticket");
            resetSouthComponent();
            
            // activo el tecleador...
            m_jKeyFactory.setText(null);       
            java.awt.EventQueue.invokeLater(new Runnable() {
                public void run() {
                    m_jKeyFactory.requestFocus();
                }
            });

            // Show customers count if not set in restaurant mode
            if (this.m_ticketsbag instanceof JTicketsBagRestaurantMap) {
                if (this.m_oTicket.getCustomersCount() == null) {
                    if (!AppConfig.loadedInstance.getProperty("ui.autodisplaycustcount").equals("0")) {
                    ((JTicketsBagRestaurant)this.m_ticketsbag.getBagComponent()).custCountBtnActionPerformed(null);
                    }
                }
            }
        }
    }

    public void updateTariffCombo() {
        updateTariffCombo(m_oTicket);
    }
    /** Update tariff area combo box to select the area of the ticket. */
    public void updateTariffCombo(TicketInfo ticket) {
        m_jTariff.setSelectedIndex(0);
        if (ticket != null) {
            // Select the current tariff area
            for (int i= 0; i < m_jTariff.getItemCount(); i++) {
                try {
                    TariffInfo tariff = (TariffInfo) m_jTariff.getItemAt(i);
                    if (tariff != null) {
                       if (new Integer(tariff.getID()).equals(ticket.getTariffArea())) {
                            m_jTariff.setSelectedIndex(i);
                        }
                    }
                } catch (ClassCastException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void switchTariffArea(TariffInfo area) {
        if (m_oTicket != null) {
            if (area.getID() == -1) {
                m_oTicket.setTariffArea(null);
            } else {
                m_oTicket.setTariffArea(new Integer(area.getID()));
            }
        }
        this.updateTariffCombo();
    }

    private void printPartialTotals(){
        if (m_oTicket == null) {
            this.subtotalLabel.setText(null);
            m_jTotalEuros.setText(null);
            this.discountLabel.setText(null);
        } else {
            this.subtotalLabel.setText(AppLocal.getIntString("label.subtotalLine", m_oTicket.printSubTotal(), m_oTicket.printTax()));
            m_jTotalEuros.setText(m_oTicket.printTotal());
            if (m_oTicket.getDiscountRate() > 0.0) {
                this.discountLabel.setText(AppLocal.getIntString("label.totalDiscount", m_oTicket.printDiscountRate(), m_oTicket.printFullTotal()));
            } else {
                this.discountLabel.setText(null);
            }
        }
    }
    
    private void paintTicketLine(int index, TicketLineInfo oLine){
        
        if (executeEventAndRefresh("ticket.setline", new ScriptArg("index", index), new ScriptArg("line", oLine)) == null) {

            m_oTicket.setLine(index, oLine);
            m_ticketlines.setTicketLine(index, oLine);
            m_ticketlines.setSelectedIndex(index);

            visorTicketLine(oLine); // Y al visor tambien...
            printPartialTotals();   
            eraseAutomator();

            // event receipt
            executeEventAndRefresh("ticket.change");
        }
   }

    private void addTicketLine(ProductInfoExt oProduct, double dMul, double dPrice) {   
        
        TaxInfo tax = taxeslogic.getTaxInfo(oProduct.getTaxCategoryID(),  m_oTicket.getDate(), m_oTicket.getCustomer());
        TicketLineInfo line = new TicketLineInfo(oProduct, dMul, dPrice, tax, (java.util.Properties) (oProduct.getProperties().clone()));
        line.setSubproduct(m_bIsSubproduct);
        addTicketLine(line);
        if (oProduct.isDiscountEnabled()) {
            double rate = oProduct.getDiscountRate();
            if (rate > 0.005) {
                line.setDiscountRate(rate);
            }
        }
    }
    
    protected void addTicketLine(TicketLineInfo oLine) {   
        // Update price from tariff area if needed
        if (m_oTicket.getTariffArea() != null) {
            // TODO: don't update price of composition product (which price is 0)
            // Get tariff area price
            try {
                Double price = this.dlSales.getTariffAreaPrice(m_oTicket.getTariffArea(), oLine.getProductID());
                if (price != null) {
                    oLine.setPrice(price);
                }
            } catch (BasicException e) {
                e.printStackTrace();
            }
        }
        if (executeEventAndRefresh("ticket.addline", new ScriptArg("line", oLine)) == null) {
        
            if (oLine.isProductCom()) {
                // Comentario entonces donde se pueda
                int i = m_ticketlines.getSelectedIndex();

                // me salto el primer producto normal...
                if (i >= 0 && !m_oTicket.getLine(i).isProductCom()) {
                    i++;
                }

                // me salto todos los productos auxiliares...
                while (i >= 0 && i < m_oTicket.getLinesCount() && m_oTicket.getLine(i).isProductCom()) {
                    i++;
                }

                if (i >= 0) {
                    m_oTicket.insertLine(i, oLine);
                    m_ticketlines.insertTicketLine(i, oLine); // Pintamos la linea en la vista...                 
                } else {
                    Toolkit.getDefaultToolkit().beep();                                   
                }
            } else {    
                // Producto normal, entonces al finalnewline.getMultiply() 
                m_oTicket.addLine(oLine);            
                m_ticketlines.addTicketLine(oLine); // Pintamos la linea en la vista... 
            }

            visorTicketLine(oLine);
            printPartialTotals();   
            eraseAutomator();

            // event receipt
            executeEventAndRefresh("ticket.change");
        }
    }

    private void removeTicketLine(int i){
        
        if (executeEventAndRefresh("ticket.removeline", new ScriptArg("index", i)) == null) {
        
            if (m_oTicket.getLine(i).isProductCom()) {
                // Es un producto auxiliar, lo borro y santas pascuas.
                m_oTicket.removeLine(i);
                m_ticketlines.removeTicketLine(i);   
            } else {
                // Es un producto normal, lo borro.
                m_oTicket.removeLine(i);
                m_ticketlines.removeTicketLine(i); 
                // Y todos lo auxiliaries que hubiera debajo.
                while(i < m_oTicket.getLinesCount() && m_oTicket.getLine(i).isProductCom()) {
                    m_oTicket.removeLine(i);
                    m_ticketlines.removeTicketLine(i); 
                }
            }

            visorTicketLine(null); // borro el visor 
            printPartialTotals(); // pinto los totales parciales...                           
            eraseAutomator();// Pongo a cero

            // event receipt
            executeEventAndRefresh("ticket.change");
        }
    }
    
    private ProductInfoExt getInputProduct() {
        ProductInfoExt oProduct = new ProductInfoExt(); // Es un ticket
        oProduct.setReference(null);
        oProduct.setCode(null);
        oProduct.setName("");
        oProduct.setTaxCategoryID(((TaxCategoryInfo) taxcategoriesmodel.getSelectedItem()).getID());
        
        oProduct.setPriceSell(includeTaxes(oProduct.getTaxCategoryID(), getInputValue()));
        
        return oProduct;
    }
    
    private double includeTaxes(String tcid, double dValue) {
        if (m_jaddtax.isSelected()) {
            TaxInfo tax = taxeslogic.getTaxInfo(tcid,  m_oTicket.getDate(), m_oTicket.getCustomer());
            double dTaxRate = tax == null ? 0.0 : tax.getRate();           
            return dValue / (1.0 + dTaxRate);      
        } else {
            return dValue;
        }
    }
    
    private double getInputValue() {
        try {
            return Double.parseDouble(m_jPrice.getText());
        } catch (NumberFormatException e){
            return 0.0;
        }
    }

    private double getPorValue() {
        try {
            return Double.parseDouble(m_jPor.getText().substring(1));                
        } catch (NumberFormatException e){
            return 1.0;
        } catch (StringIndexOutOfBoundsException e){
            return 1.0;
        }
    }

    private void incProductByCode(String sCode) {
    // precondicion: sCode != null
        
        try {
            ProductInfoExt oProduct = dlSales.getProductInfoByCode(sCode);
            if (oProduct == null) {
                Toolkit.getDefaultToolkit().beep();
                new MessageInf(MessageInf.SGN_WARNING, AppLocal.getIntString("message.noproduct")).show(this);           
                eraseAutomator();
            } else {
                // Se anade directamente una unidad con el precio y todo
                incProduct(oProduct);
            }
        } catch (BasicException eData) {
            eraseAutomator();
            new MessageInf(eData).show(this);
        }
    }
    
    private void incProductByCodePrice(String sCode, double dPriceSell) {
    // precondicion: sCode != null
        
        try {
            ProductInfoExt oProduct = dlSales.getProductInfoByCode(sCode);
            if (oProduct == null) {                  
                Toolkit.getDefaultToolkit().beep();                   
                new MessageInf(MessageInf.SGN_WARNING, AppLocal.getIntString("message.noproduct")).show(this);           
                eraseAutomator();
            } else {
                // Se anade directamente una unidad con el precio y todo
                if (m_jaddtax.isSelected()) {
                    // debemos quitarle los impuestos ya que el precio es con iva incluido...
                    TaxInfo tax = taxeslogic.getTaxInfo(oProduct.getTaxCategoryID(),  m_oTicket.getDate(), m_oTicket.getCustomer());
                    addTicketLine(oProduct, 1.0, dPriceSell / (1.0 + tax.getRate()));
                } else {
                    addTicketLine(oProduct, 1.0, dPriceSell);
                }                
            }
        } catch (BasicException eData) {
            eraseAutomator();
            new MessageInf(eData).show(this);               
        }
    }
    
    private void incProduct(ProductInfoExt prod) {
        
        if (prod.isScale() && m_App.getDeviceScale().existsScale()) {
            try {
                Double value = m_App.getDeviceScale().readWeight();
                if (value != null) {
                    incProduct(value.doubleValue(), prod);
                }
            } catch (ScaleException e) {
                Toolkit.getDefaultToolkit().beep();                
                new MessageInf(MessageInf.SGN_WARNING, AppLocal.getIntString("message.noweight"), e).show(this);           
                eraseAutomator();
            }
        } else {
            // No es un producto que se pese o no hay balanza
            incProduct(1.0, prod);
        }
    }
    
    private void incProduct(double dPor, ProductInfoExt prod) {
        // precondicion: prod != null
        addTicketLine(prod, dPor, prod.getPriceSell());
        /** Pops automatically the product's attributes screen, if the
         * product has one, when a product is added to the list */
        if(prod.getAttributeSetID() != null) {
            jEditAttributesActionPerformed(AUTO_PERFORMED);
        }
    }

    /** Switch catalog according to composition state */
    public void changeCatalog() {
        catcontainer.removeAll();
        setSubgroupMode(m_iProduct == PRODUCT_SUBGROUP);
        if (m_iProduct == PRODUCT_SUBGROUP) {
            // Set composition catalog
            m_catalog = getSouthAuxComponent();
        } else {
            // Set default catalog
            m_catalog = getSouthComponent();
        }
        catcontainer.add(m_catalog, BorderLayout.CENTER);
        catcontainer.updateUI();
    }

    /** Activate or deactivate input component for subgroups (or not) */
    private void setSubgroupMode(boolean value) {
        this.lineBtnsContainer.setEnabled(!value);
        enableComponents(this.lineBtnsContainer, !value);
        enableComponents(m_jPanEntries, !value);
    }

    private void enableComponents(Container cont, boolean value) {
        for (Component c: cont.getComponents()) {
            try {
                c.setEnabled(value);
                enableComponents((Container) c, value);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    } 

    protected void buttonTransition(ProductInfoExt prod) {
        // precondicion: prod != null
        m_bIsSubproduct = false;

        // Check if picking composition content
        if (m_iProduct == PRODUCT_SUBGROUP && prod != null) {
            // Set component product price to 0
            prod.setCom(true);
            prod.setPriceSell(0.0);
            m_bIsSubproduct = true;
            // Set quantity to the same amount as composition product
            if (m_dMultiply != 1.0) {
                m_jPrice.setText(String.valueOf(m_dMultiply));
            }
        } else if (m_iProduct == PRODUCT_COMPOSITION) {
            // Picked a composition product, start composing
            // Set multiply for components
            m_dMultiply = (getInputValue()==0)? 1.0: getInputValue();
            m_iProduct = PRODUCT_SUBGROUP;
        }

        if (m_PriceActualState == N_NOTHING
                && m_QuantityActualState == N_NOTHING) {
            // No input, just add product
            incProduct(prod);
        } else if (m_PriceActualState != N_NOTHING
                && m_PriceActualState != N_ZERO
                && m_PriceActualState != N_DECIMALZERO) {
            // Price input not empty
            incProduct(getInputValue(), prod);
        } else {
            Toolkit.getDefaultToolkit().beep();
        }
    }

    /** Returns the automate to its default state*/
    public void eraseAutomator() {
        m_InputState = I_PRICE;
        m_PriceActualState = N_NOTHING;
        m_PricePreviousState = N_NOTHING;
        m_QuantityActualState = N_NOTHING;
        m_QuantityPreviousState = N_NOTHING;
        m_sBarcode = new StringBuffer();

        m_jPrice.setText("");
        m_jPor.setText("");
    }

    /** Chooses what to do when a specific key is typed
     *
     * @param entered contains the key typed
     */
    public void automator (char entered) {

        // barcode when enter is typed
        if (entered == keyEnter) {
            if (m_sBarcode.length() > 0) {
                String sCode = m_sBarcode.toString();
                if (sCode.startsWith("c")) {
                    // barcode of a customers card
                    try {
                        CustomerInfoExt newcustomer = this.dlCustomers.getCustomerByCard(sCode);
                        if (newcustomer == null) {
                            Toolkit.getDefaultToolkit().beep();
                            new MessageInf(MessageInf.SGN_WARNING, AppLocal.getIntString("message.nocustomer")).show(this);
                        } else {
                            m_oTicket.setCustomer(newcustomer);
                            m_jTicketId.setText(m_oTicket.getName(m_oTicketExt));
                        }
                    } catch (BasicException e) {
                        Toolkit.getDefaultToolkit().beep();
                        new MessageInf(MessageInf.SGN_WARNING, AppLocal.getIntString("message.nocustomer"), e).show(this);
                    }
                    eraseAutomator();
                } else if (sCode.length() == 13 && sCode.startsWith("250")) {
                    // barcode of the other machine
                    ProductInfoExt oProduct = new ProductInfoExt(); // Es un ticket
                    oProduct.setReference(null); // para que no se grabe
                    oProduct.setCode(sCode);
                    oProduct.setName("Ticket " + sCode.substring(3, 7));
                    oProduct.setPriceSell(Double.parseDouble(sCode.substring(7, 12)) / 100);
                    oProduct.setTaxCategoryID(((TaxCategoryInfo) taxcategoriesmodel.getSelectedItem()).getID());
                    // Se anade directamente una unidad con el precio y todo
                    addTicketLine(oProduct, 1.0, includeTaxes(oProduct.getTaxCategoryID(), oProduct.getPriceSell()));
                } else if (sCode.length() == 13 && sCode.startsWith("210")) {
                    // barcode of a weigth product
                    incProductByCodePrice(sCode.substring(0, 7), Double.parseDouble(sCode.substring(7, 12)) / 100);
                } else {
                    incProductByCode(sCode);
                }
            } else {
                Toolkit.getDefaultToolkit().beep();
            }
        }
        // Other character
        else {
            m_sBarcode.append(entered);

            // goes to the fonction processPrice if the key typed matches the key condition and
            // if the input is made in quantity's label
            if ((entered == '0' || entered == '1' || entered == '2' || entered == '3' || entered == '4'
                || entered == '5' || entered == '6' || entered == '7' || entered == '8' || entered == '9'
                || entered == '.' || entered == keyBack || entered == keyDel || entered == '/')
                && (m_InputState == I_NOTHING || m_InputState == I_PRICE)) {
                m_InputState = I_PRICE;
                processPrice(entered);

                // if '*' is typed and nothing in price's label, sets the price's label text to 0,
                // sets 'x' to quantity's label and goes to the quantity's label
            } else if (entered == '*' && (m_InputState == I_NOTHING || m_InputState == I_PRICE)
                    && m_PriceActualState == N_NOTHING) {
                m_PriceActualState = N_ZERO;
                m_InputState = I_QUANTITY;
                m_QuantityActualState = N_NOTHING;
                m_jPrice.setText("0");
                m_jPor.setText("x");

                // if '*' is typed and something in price's label, sets 'x' to quantity's label and goes to the quantity's label
            } else if (entered == '*' && m_InputState == I_PRICE && m_PriceActualState != N_NOTHING) {
                m_InputState = I_QUANTITY;
                m_QuantityActualState = N_NOTHING;
                m_jPor.setText("x");

                // goes to the fonction processQuantity if the key typed matches the key condition and
                // if the input is made in quantity's label
            } else if ((entered == '0' || entered == '1' || entered == '2' || entered == '3' || entered == '4'
                    || entered == '5' || entered == '6' || entered == '7' || entered == '8' || entered == '9'
                    || entered == '.' || entered == keyBack || entered == keyDel || entered == '/')
                    && m_InputState == I_QUANTITY) {
                processQuantity(entered);

                // + without input: increment selected line quantity
            } else if (entered == '+'
                    && m_PriceActualState == N_NOTHING
                    && m_QuantityActualState == N_NOTHING) {
                int i = m_ticketlines.getSelectedIndex();
                if (i < 0) {
                    Toolkit.getDefaultToolkit().beep();
                } else {
                    TicketLineInfo newline = new TicketLineInfo(m_oTicket.getLine(i));
                    //If it's a refund + button means one unit less
                    if (m_oTicket.getTicketType() == TicketInfo.RECEIPT_REFUND){
                        newline.setMultiply(newline.getMultiply() - 1.0);
                        paintTicketLine(i, newline);
                    }
                    else {
                        // add one unit to the selected line
                        newline.setMultiply(newline.getMultiply() + 1.0);
                        paintTicketLine(i, newline);
                    }
                }

                // - without input: decrement selected line quantity
                // Remove line if quantity is set to 0
            } else if (entered == '-'
                        && m_PriceActualState == N_NOTHING
                        && m_QuantityActualState == N_NOTHING
                        && m_App.getAppUserView().getUser().hasPermission("sales.EditLines")) {

                    int i = m_ticketlines.getSelectedIndex();
                    if (i < 0){
                        Toolkit.getDefaultToolkit().beep();
                    } else {
                        TicketLineInfo newline = new TicketLineInfo(m_oTicket.getLine(i));
                        //If it's a refund - button means one unit more
                        if (m_oTicket.getTicketType() == TicketInfo.RECEIPT_REFUND){
                            newline.setMultiply(newline.getMultiply() + 1.0);
                            if (newline.getMultiply() >= 0) {
                                removeTicketLine(i);
                            } else {
                                paintTicketLine(i, newline);
                            }
                        } else {
                            // substract one unit to the selected line
                            newline.setMultiply(newline.getMultiply() - 1.0);
                            if (newline.getMultiply() <= 0.0) {
                                removeTicketLine(i); // elimino la linea
                            } else {
                                paintTicketLine(i, newline);
                            }
                        }
                    }

                // + with multiply input (without price): replace quantity
            } else if (entered == '+'
                    && (m_PriceActualState == N_NOTHING || m_PriceActualState == N_ZERO)
                    && m_QuantityActualState != N_NOTHING && m_QuantityActualState != N_ZERO && m_QuantityActualState != N_DECIMALZERO
                    && m_InputState == I_QUANTITY) {
                int i = m_ticketlines.getSelectedIndex();
                if (i < 0){
                    Toolkit.getDefaultToolkit().beep();
                } else {
                    double dPor = getPorValue();
                    TicketLineInfo newline = new TicketLineInfo(m_oTicket.getLine(i));
                    if (m_oTicket.getTicketType() == TicketInfo.RECEIPT_REFUND) {
                        newline.setMultiply(-dPor);
                        newline.setPrice(Math.abs(newline.getPrice()));
                        paintTicketLine(i, newline);
                    } else {
                        newline.setMultiply(dPor);
                        newline.setPrice(Math.abs(newline.getPrice()));
                        paintTicketLine(i, newline);
                    }
                }
                // - with multiply input (without price): set negative quantity
            } else if (entered == '-'
                    && (m_PriceActualState == N_NOTHING || m_PriceActualState == N_ZERO)
                    && m_QuantityActualState != N_NOTHING && m_QuantityActualState != N_ZERO && m_QuantityActualState != N_DECIMALZERO
                    && m_InputState == I_QUANTITY
                    && m_App.getAppUserView().getUser().hasPermission("sales.EditLines")) {
                int i = m_ticketlines.getSelectedIndex();
                if (i < 0){
                    Toolkit.getDefaultToolkit().beep();
                } else {
                    double dPor = getPorValue();
                    TicketLineInfo newline = new TicketLineInfo(m_oTicket.getLine(i));
                    if (m_oTicket.getTicketType() == TicketInfo.RECEIPT_NORMAL) {
                        newline.setMultiply(-dPor);
                        paintTicketLine(i, newline);
                    }
                }

                // + with price input (without multiply): create an empty line
                // with entered price
            } else if (entered == '+'
                    && m_PriceActualState != N_NOTHING && m_PriceActualState != N_ZERO && m_PriceActualState != N_DECIMALZERO
                    && m_QuantityActualState == N_NOTHING
                    && m_App.getAppUserView().getUser().hasPermission("sales.EditLines")) {
                ProductInfoExt product = getInputProduct();
                addTicketLine(product, 1.0, product.getPriceSell());

                // - with price input (without multiply): create an empty line
                // with negative entered price
            } else if (entered == '-'
                    && m_PriceActualState != N_NOTHING && m_PriceActualState != N_ZERO && m_PriceActualState != N_DECIMALZERO
                    && m_QuantityActualState == N_NOTHING
                    && m_App.getAppUserView().getUser().hasPermission("sales.EditLines")) {
                ProductInfoExt product = getInputProduct();
                addTicketLine(product, 1.0, -product.getPriceSell());

                // + with price and multiply: create an empty line with entered price
                // and quantity set to entered multiply
            } else if (entered == '+'
                    && m_PriceActualState != N_NOTHING && m_PriceActualState != N_ZERO && m_PriceActualState != N_DECIMALZERO
                    && m_QuantityActualState != N_NOTHING && m_QuantityActualState != N_ZERO && m_PriceActualState != N_DECIMALZERO
                    && m_App.getAppUserView().getUser().hasPermission("sales.EditLines")) {
                ProductInfoExt product = getInputProduct();
                addTicketLine(product, getPorValue(), product.getPriceSell());

                // - with price and multiply: create an empty line with entered
                // negative price and quantity set to entered multiply
            } else if (entered == '-'
                    && m_PriceActualState != N_NOTHING && m_PriceActualState != N_ZERO && m_PriceActualState != N_DECIMALZERO
                    && m_QuantityActualState != N_NOTHING && m_QuantityActualState != N_ZERO && m_PriceActualState != N_DECIMALZERO
                    && m_App.getAppUserView().getUser().hasPermission("sales.EditLines")) {
                ProductInfoExt product = getInputProduct();
                addTicketLine(product, getPorValue(), -product.getPriceSell());

                // Space bar or = : go to payments
            } else if (entered == ' ' || entered == '=') {
                if (m_oTicket.getLinesCount() > 0) {
                    if (closeTicket(m_oTicket, m_oTicketExt)) {
                        // Ends edition of current receipt
                        m_ticketsbag.deleteTicket();
                    } else {
                        // repaint current ticket
                        refreshTicket();
                    }
                } else {
                    Toolkit.getDefaultToolkit().beep();
                }

                // Scale button pressed and a number typed as a price
            } else if (entered == keySection
                    && m_PriceActualState != N_NOTHING && m_PriceActualState != N_ZERO && m_PriceActualState != N_DECIMALZERO
                    && m_QuantityActualState == N_NOTHING) {
                if (m_App.getDeviceScale().existsScale() && m_App.getAppUserView().getUser().hasPermission("sales.EditLines")) {
                    try {
                        Double value = m_App.getDeviceScale().readWeight();
                        if (value != null) {
                            ProductInfoExt product = getInputProduct();
                            addTicketLine(product, value.doubleValue(), product.getPriceSell());
                        }
                    } catch (ScaleException e) {
                        Toolkit.getDefaultToolkit().beep();
                        new MessageInf(MessageInf.SGN_WARNING, AppLocal.getIntString("message.noweight"), e).show(this);
                        eraseAutomator();
                    }
                } else {
                    // No existe la balanza;
                    Toolkit.getDefaultToolkit().beep();
                }

                // Scale button pressed and no number typed.
            } else if (entered == keySection
                    && m_PriceActualState == N_NOTHING
                    && m_QuantityActualState == N_NOTHING) {
                int i = m_ticketlines.getSelectedIndex();
                if (i < 0){
                    Toolkit.getDefaultToolkit().beep();
                } else if (m_App.getDeviceScale().existsScale()) {
                    try {
                        Double value = m_App.getDeviceScale().readWeight();
                        if (value != null) {
                            TicketLineInfo newline = new TicketLineInfo(m_oTicket.getLine(i));
                            newline.setMultiply(value.doubleValue());
                            newline.setPrice(Math.abs(newline.getPrice()));
                            paintTicketLine(i, newline);
                        }
                    } catch (ScaleException e) {
                        // Error de pesada.
                        Toolkit.getDefaultToolkit().beep();
                        new MessageInf(MessageInf.SGN_WARNING, AppLocal.getIntString("message.noweight"), e).show(this);
                        eraseAutomator();
                    }
                } else {
                    // No existe la balanza;
                    Toolkit.getDefaultToolkit().beep();
                }
            }
        }
    }

    /** Manages the automate's state for the price's label.
     * @author Loïc Dumont
     * @since since 20/02/2013
     * @param entered contains the key typed
     */
    public void processPrice(char entered){
        // 0 stays 0
        if (entered == '0' && (m_PriceActualState == N_NOTHING || m_PriceActualState == N_ZERO)) {
            m_PriceActualState = N_ZERO;
            m_jPrice.setText("0");
        } else if ((entered == '1' || entered == '2' || entered == '3' || entered == '4' || entered == '5'
                || entered == '6' || entered == '7' || entered == '8' || entered == '9')
                && (m_PriceActualState == N_NOTHING || m_PriceActualState == N_ZERO)) {
            m_jPrice.setText(Character.toString(entered));
            m_PriceActualState = N_NUMBER;
        } else if ((entered == '0' || entered == '1' || entered == '2' || entered == '3' || entered == '4'
                || entered == '5' || entered == '6' || entered == '7' || entered == '8' || entered == '9')
                && (m_PriceActualState == N_NUMBER || m_PriceActualState == N_DECIMAL)) {
            m_jPrice.setText(m_jPrice.getText() + entered);
        } else if ((entered == '0' || entered == '1' || entered == '2' || entered == '3' || entered == '4'
                || entered == '5' || entered == '6' || entered == '7' || entered == '8' || entered == '9')
                && (m_PriceActualState == N_DECIMALZERO)) {
            m_jPrice.setText(m_jPrice.getText() + entered);
            m_PriceActualState = N_DECIMAL;
            m_PricePreviousState = N_DECIMALZERO;
        } else if ((entered == '0' || entered == '1' || entered == '2' || entered == '3' || entered == '4'
                || entered == '5' || entered == '6' || entered == '7' || entered == '8' || entered == '9')
                && (m_PriceActualState == N_DECIMALNUMBER)) {
            m_jPrice.setText(m_jPrice.getText() + entered);
            m_PriceActualState = N_DECIMAL;
            m_PricePreviousState = N_DECIMALNUMBER;
        } else if (entered == '.' && m_PriceActualState == N_NOTHING) {
            m_jPrice.setText("0.");
            m_PriceActualState = N_DECIMALZERO;
            m_PricePreviousState = N_ZERO;
        } else if (entered == '.' && (m_PriceActualState == N_ZERO || m_PriceActualState == N_NUMBER)) {
            m_jPrice.setText(m_jPrice.getText() + ".");
            if (m_PriceActualState == N_ZERO) {
                m_PriceActualState = N_DECIMALZERO;
                m_PricePreviousState = N_ZERO;
            } else if (m_PriceActualState == N_NUMBER) {
                m_PriceActualState = N_DECIMALNUMBER;
                m_PricePreviousState = N_NUMBER;
            }

            // erase all numbers
        } else if (entered == keyDel || entered == '/') {
            eraseAutomator(); // sets the automate to its default state

            // erases numbers one by one
        } else if (entered == keyBack) {
            String price = m_jPrice.getText();
            int back = price.length() - 1;
            if (back - 1 == price.indexOf('.') && m_PriceActualState == N_DECIMAL) {
                m_PriceActualState = m_PricePreviousState;
            } else if (m_PriceActualState == N_DECIMALZERO) {
                m_PriceActualState = N_ZERO;
            } else if (m_PriceActualState == N_DECIMALNUMBER) {
                m_PriceActualState = N_NUMBER;
            } else if (m_PriceActualState == N_ZERO) {
                m_PriceActualState = N_NOTHING;
                eraseAutomator(); // sets the automate to its default state
            } else if (back == 0 && m_PriceActualState == N_NUMBER) {
                m_PriceActualState = N_NOTHING;
                eraseAutomator(); // sets the automate to its default state
            }
            if (back >0) {
                price = price.substring(0, back);
                m_jPrice.setText(price);
            } else if (back <0) {
                eraseAutomator(); // sets the automate to its default state
            }
        }
    }

    /** Manages the automate's state for the quantity's label.
     * @author Loïc Dumont
     * @since since 20/02/2013
     * @param entered contains the key typed
     */
    public void processQuantity(char entered) {
        // 0 stays 0
        if (entered == '0' && (m_QuantityActualState == N_NOTHING || m_QuantityActualState == N_ZERO)) {
            m_jPor.setText("x0");
            m_QuantityActualState = N_ZERO;
        } else if ((entered == '1' || entered == '2' || entered == '3' || entered == '4' || entered == '5'
                || entered == '6' || entered == '7' || entered == '8' || entered == '9')
                && (m_QuantityActualState == N_NOTHING || m_QuantityActualState == N_ZERO)) {
            m_jPor.setText("x" + Character.toString(entered));
            m_QuantityActualState = N_NUMBER;
        } else if ((entered == '0' || entered == '1' || entered == '2' || entered == '3' || entered == '4'
                || entered == '5' || entered == '6' || entered == '7' || entered == '8' || entered == '9')
                && (m_QuantityActualState == N_NUMBER || m_QuantityActualState == N_DECIMAL)) {
            m_jPor.setText(m_jPor.getText() + entered);
        } else if ((entered == '0' || entered == '1' || entered == '2' || entered == '3' || entered == '4'
                || entered == '5' || entered == '6' || entered == '7' || entered == '8' || entered == '9')
                && (m_QuantityActualState == N_DECIMALZERO)) {
            m_jPor.setText(m_jPor.getText() + entered);
            m_QuantityActualState = N_DECIMAL;
            m_QuantityPreviousState = N_DECIMALZERO;
        } else if ((entered == '0' || entered == '1' || entered == '2' || entered == '3' || entered == '4'
                || entered == '5' || entered == '6' || entered == '7' || entered == '8' || entered == '9')
                && (m_QuantityActualState == N_DECIMALNUMBER)) {
            m_jPor.setText(m_jPor.getText() + entered);
            m_QuantityActualState = N_DECIMAL;
            m_QuantityPreviousState = N_DECIMALNUMBER;
        } else if (entered == '.' && m_QuantityActualState == N_NOTHING) {
            m_jPor.setText("x0.");
            m_QuantityActualState = N_DECIMALZERO;
            m_QuantityPreviousState = N_ZERO;
        } else if (entered =='.' && (m_QuantityActualState == N_ZERO || m_QuantityActualState == N_NUMBER)) {
            m_jPor.setText(m_jPor.getText() + ".");
            if (m_QuantityActualState == N_ZERO) {
                m_QuantityActualState = N_DECIMALZERO;
                m_QuantityPreviousState = N_ZERO;
            } else if (m_QuantityActualState == N_NUMBER) {
                m_QuantityActualState = N_DECIMALNUMBER;
                m_QuantityPreviousState = N_NUMBER;
            }

            // erases all numbers
        } else if (entered == keyDel || entered == '/') {
            m_QuantityActualState = N_NOTHING;
            m_InputState = I_PRICE;
            m_jPor.setText("");

            // erases numbers one by one
        } else if (entered == keyBack) {
            String quantity = m_jPor.getText();
            int back = quantity.length() - 1;
            if (back - 1 == quantity.indexOf('.') && m_QuantityActualState == N_DECIMAL) {
                m_QuantityActualState = m_QuantityPreviousState;
            } else if (m_QuantityActualState == N_DECIMALZERO) {
                m_QuantityActualState = N_ZERO;
            } else if (m_QuantityActualState == N_DECIMALNUMBER) {
                m_QuantityActualState = N_NUMBER;
            } else if (back == 1 && m_QuantityActualState == N_ZERO) {
                m_QuantityActualState = N_NOTHING;
            } else if (back == 1 && m_QuantityActualState == N_NUMBER) {
                m_QuantityActualState = N_NOTHING;
            } else if (back == 0 && m_QuantityActualState == N_NOTHING) {
                m_InputState = I_PRICE;
            }
            if (back >=0) {
                quantity = quantity.substring(0, back);
                m_jPor.setText(quantity);
            }
        }
    }

    private boolean closeTicket(TicketInfo ticket, Object ticketext) {
    
        boolean resultok = false;
        
        if (m_App.getAppUserView().getUser().hasPermission("sales.Total")) {  
            
            try {
                // reset the payment info
                taxeslogic.calculateTaxes(ticket);
                if (ticket.getTotal()>=0.0){
                    ticket.resetPayments(); //Only reset if is sale
                }
                
                if (executeEvent(ticket, ticketext, "ticket.total") == null) {

                    // Muestro el total
                    printTicket("Printer.TicketTotal", ticket, ticketext);
                    
                    
                    // Select the Payments information
                    JPaymentSelect paymentdialog = ticket.getTicketType() == TicketInfo.RECEIPT_NORMAL
                            ? paymentdialogreceipt
                            : paymentdialogrefund;
                    String printSelectedConfigBtn = m_jbtnconfig.getProperty("printselected");
                    boolean printSelected;
                    if (printSelectedConfigBtn != null) {
                        printSelected = printSelectedConfigBtn.equals("true");
                    } else {
                        printSelected = AppConfig.loadedInstance.getProperty("ui.printticketbydefault").equals("1");
                    }
                    paymentdialog.setPrintSelected(printSelected);

                    paymentdialog.setTransactionID(ticket.getTransactionID());

                    if (paymentdialog.showDialog(ticket.getTotal(), ticket.getCustomer())) {

                        // assign the payments selected and calculate taxes.         
                        ticket.setPayments(paymentdialog.getSelectedPayments());

                        // Asigno los valores definitivos del ticket...
                        ticket.setUser(m_App.getAppUserView().getUser().getUserInfo()); // El usuario que lo cobra
                        ticket.setActiveCash(m_App.getActiveCashIndex());
                        ticket.setDate(new Date()); // Le pongo la fecha de cobro

                        if (executeEvent(ticket, ticketext, "ticket.save") == null) {
                            // Save the receipt and assign a receipt number
                            try {
                                dlSales.saveTicket(ticket,
                                        m_App.getInventoryLocation(),
                                        m_App.getActiveCashSession().getId());
                                // Refresh customer if any
                                if (ticket.getCustomer() != null) {
                                    dlCustomers.updateCustomer(ticket.getCustomer().getId(), null);
                                }
                            } catch (BasicException eData) {
                                MessageInf msg = new MessageInf(MessageInf.SGN_NOTICE, AppLocal.getIntString("message.nosaveticket"), eData);
                                msg.show(this);
                            }

                            executeEvent(ticket, ticketext, "ticket.close", new ScriptArg("print", paymentdialog.isPrintSelected()));

                            // Print receipt.
                            printTicket(paymentdialog.isPrintSelected()
                                    ? "Printer.Ticket"
                                    : "Printer.Ticket2", ticket, ticketext);
                            resultok = true;
                        }
                    }
                }
            } catch (TaxesException e) {
                MessageInf msg = new MessageInf(MessageInf.SGN_WARNING, AppLocal.getIntString("message.cannotcalculatetaxes"));
                msg.show(this);
                resultok = false;
            }
            
            // reset the payment info
            m_oTicket.resetTaxes();
            m_oTicket.resetPayments();
        }
        
        // cancelled the ticket.total script
        // or canceled the payment dialog
        // or canceled the ticket.close script
        return resultok;        
    }
       
    private void printTicket(String sresourcename, TicketInfo ticket, Object ticketext) {

        String sresource = dlSystem.getResourceAsXML(sresourcename);
        try {
            taxeslogic.calculateTaxes(ticket);
        } catch (TaxesException e) {
            e.printStackTrace();
        }
        if (sresource == null) {
            MessageInf msg = new MessageInf(MessageInf.SGN_WARNING, AppLocal.getIntString("message.cannotprintticket"));
            msg.show(JPanelTicket.this);
        } else {
            try {
                ScriptEngine script = ScriptFactory.getScriptEngine(ScriptFactory.VELOCITY);
                script.put("taxes", taxcollection);
                script.put("taxeslogic", taxeslogic);
                script.put("ticket", ticket);
                script.put("place", ticketext);
                m_TTP.printTicket(script.eval(sresource).toString());
            } catch (ScriptException e) {
                MessageInf msg = new MessageInf(MessageInf.SGN_WARNING, AppLocal.getIntString("message.cannotprintticket"), e);
                msg.show(JPanelTicket.this);
            } catch (TicketPrinterException e) {
                MessageInf msg = new MessageInf(MessageInf.SGN_WARNING, AppLocal.getIntString("message.cannotprintticket"), e);
                msg.show(JPanelTicket.this);
            }
        }
    }
    
    private void printReport(String resourcefile, TicketInfo ticket, Object ticketext) {
        
        try {     
         
            JasperReport jr;
           
            InputStream in = getClass().getResourceAsStream(resourcefile + ".ser");
            if (in == null) {      
                // read and compile the report
                JasperDesign jd = JRXmlLoader.load(getClass().getResourceAsStream(resourcefile + ".jrxml"));            
                jr = JasperCompileManager.compileReport(jd);    
            } else {
                // read the compiled reporte
                ObjectInputStream oin = new ObjectInputStream(in);
                jr = (JasperReport) oin.readObject();
                oin.close();
            }
           
            // Construyo el mapa de los parametros.
            Map reportparams = new HashMap();
            // reportparams.put("ARG", params);
            try {
                reportparams.put("REPORT_RESOURCE_BUNDLE", ResourceBundle.getBundle(resourcefile + ".properties"));
            } catch (MissingResourceException e) {
            }
            reportparams.put("TAXESLOGIC", taxeslogic); 
            
            Map reportfields = new HashMap();
            reportfields.put("TICKET", ticket);
            reportfields.put("PLACE", ticketext);

            JasperPrint jp = JasperFillManager.fillReport(jr, reportparams, new JRMapArrayDataSource(new Object[] { reportfields } ));
            
            PrintService service = ReportUtils.getPrintService(m_App.getProperties().getProperty("machine.printername"));
            
            JRPrinterAWT300.printPages(jp, 0, jp.getPages().size() - 1, service);
            
        } catch (Exception e) {
            MessageInf msg = new MessageInf(MessageInf.SGN_WARNING, AppLocal.getIntString("message.cannotloadreport"), e);
            msg.show(this);
        }               
    }

    private void visorTicketLine(TicketLineInfo oLine){
        if (oLine == null) { 
             m_App.getDeviceTicket().getDeviceDisplay().clearVisor();
        } else {                 
            try {
                ScriptEngine script = ScriptFactory.getScriptEngine(ScriptFactory.VELOCITY);
                script.put("ticketline", oLine);
                m_TTP.printTicket(script.eval(dlSystem.getResourceAsXML("Printer.TicketLine")).toString());
            } catch (ScriptException e) {
                MessageInf msg = new MessageInf(MessageInf.SGN_WARNING, AppLocal.getIntString("message.cannotprintline"), e);
                msg.show(JPanelTicket.this);
            } catch (TicketPrinterException e) {
                MessageInf msg = new MessageInf(MessageInf.SGN_WARNING, AppLocal.getIntString("message.cannotprintline"), e);
                msg.show(JPanelTicket.this);
            }
        } 
    }    
    
    
    private Object evalScript(ScriptObject scr, String resource, ScriptArg... args) {
        
        // resource here is guaratied to be not null
         try {
            scr.setSelectedIndex(m_ticketlines.getSelectedIndex());
            return scr.evalScript(dlSystem.getResourceAsXML(resource), args);                
        } catch (ScriptException e) {
            MessageInf msg = new MessageInf(MessageInf.SGN_WARNING, AppLocal.getIntString("message.cannotexecute"), e);
            msg.show(this);
            return msg;
        } 
    }
        
    public void evalScriptAndRefresh(String resource, ScriptArg... args) {

        if (resource == null) {
            MessageInf msg = new MessageInf(MessageInf.SGN_WARNING, AppLocal.getIntString("message.cannotexecute"));
            msg.show(this);            
        } else {
            ScriptObject scr = new ScriptObject(m_oTicket, m_oTicketExt);
            scr.setSelectedIndex(m_ticketlines.getSelectedIndex());
            evalScript(scr, resource, args);   
            refreshTicket();
            setSelectedIndex(scr.getSelectedIndex());
        }
    }  
    
    public void printTicket(String resource) {
        printTicket(resource, m_oTicket, m_oTicketExt);
    }
    
    private Object executeEventAndRefresh(String eventkey, ScriptArg ... args) {
        
        String resource = m_jbtnconfig.getEvent(eventkey);
        if (resource == null) {
            return null;
        } else {
            ScriptObject scr = new ScriptObject(m_oTicket, m_oTicketExt);
            scr.setSelectedIndex(m_ticketlines.getSelectedIndex());
            Object result = evalScript(scr, resource, args);   
            refreshTicket();
            setSelectedIndex(scr.getSelectedIndex());
            return result;
        }
    }
   
    private Object executeEvent(TicketInfo ticket, Object ticketext, String eventkey, ScriptArg ... args) {
        
        String resource = m_jbtnconfig.getEvent(eventkey);
        if (resource == null) {
            return null;
        } else {
            ScriptObject scr = new ScriptObject(ticket, ticketext);
            return evalScript(scr, resource, args);
        }
    }
    
    public String getResourceAsXML(String sresourcename) {
        return dlSystem.getResourceAsXML(sresourcename);
    }

    public BufferedImage getResourceAsImage(String sresourcename) {
        return dlSystem.getResourceAsImage(sresourcename);
    }
    
    private void setSelectedIndex(int i) {
        
        if (i >= 0 && i < m_oTicket.getLinesCount()) {
            m_ticketlines.setSelectedIndex(i);
        } else if (m_oTicket.getLinesCount() > 0) {
            m_ticketlines.setSelectedIndex(m_oTicket.getLinesCount() - 1);
        }    
    }
     
    public static class ScriptArg {
        private String key;
        private Object value;
        
        public ScriptArg(String key, Object value) {
            this.key = key;
            this.value = value;
        }
        public String getKey() {
            return key;
        }
        public Object getValue() {
            return value;
        }
    }
    
    public class ScriptObject {
        
        private TicketInfo ticket;
        private Object ticketext;
        
        private int selectedindex;
        
        private ScriptObject(TicketInfo ticket, Object ticketext) {
            this.ticket = ticket;
            this.ticketext = ticketext;
        }
        
        public double getInputValue() {
            if (m_PriceActualState != N_NOTHING && m_PriceActualState != N_ZERO
                && m_QuantityActualState == N_NOTHING) {
                return JPanelTicket.this.getInputValue();
            } else {
                return 0.0;
            }
        }
        
        public int getSelectedIndex() {
            return selectedindex;
        }
        
        public void setSelectedIndex(int i) {
            selectedindex = i;
        }  
        
        public void printReport(String resourcefile) {
            JPanelTicket.this.printReport(resourcefile, ticket, ticketext);
        }
        
        public void printTicket(String sresourcename) {
            JPanelTicket.this.printTicket(sresourcename, ticket, ticketext);   
        }              
        
        public Object evalScript(String code, ScriptArg... args) throws ScriptException {
            
            ScriptEngine script = ScriptFactory.getScriptEngine(ScriptFactory.BEANSHELL);
            script.put("ticket", ticket);
            script.put("place", ticketext);
            script.put("taxes", taxcollection);
            script.put("taxeslogic", taxeslogic);             
            script.put("user", m_App.getAppUserView().getUser());
            script.put("sales", this);

            // more arguments
            for(ScriptArg arg : args) {
                script.put(arg.getKey(), arg.getValue());
            }             

            return script.eval(code);
        }            
    }

    protected class CatalogListener implements ActionListener {
        private void reloadCatalog () {
            changeCatalog();
            try {
                m_cat.loadCatalog();
            } catch (BasicException e) {
                e.printStackTrace();
            }
        }
        
        public void actionPerformed(ActionEvent e) {
            if ( (e.getSource()).getClass().equals(ProductInfoExt.class) ) {
                // Clicked on a product
                ProductInfoExt prod = ((ProductInfoExt) e.getSource());
                // Special command to end composition
                if (e.getActionCommand().equals("-1")) {
                    m_iProduct = PRODUCT_SINGLE;
                    reloadCatalog();
                } else {
                    if (prod.getCategoryID().equals("0")) {
                        // Clicked on a composition
                        m_iProduct = PRODUCT_COMPOSITION;
                        buttonTransition(prod);
                        reloadCatalog();
                        m_cat.showCatalogPanel(prod.getID());
                    } else {
                        // Clicked on a regular product
                        buttonTransition(prod);
                    }
                }
            } else {
                // Si se ha seleccionado cualquier otra cosa...
                // Si es una orden de cancelar la venta de una composición
                if ( e.getActionCommand().equals("cancelSubgroupSale")){
                    int i=m_oTicket.getLinesCount();
                    TicketLineInfo line = m_oTicket.getLine(--i);
                    //Quito todas las líneas que son subproductos
                    // (puesto que está recién añadido, pertenecen
                    // al menú que estamos cancelando)
                    while ((i>0) && (line.isSubproduct())) {
                        m_oTicket.removeLine(i);
                        m_ticketlines.removeTicketLine(i);
                        line= m_oTicket.getLine(--i);
                    }
                    // Quito la línea siguiente, perteneciente al menú en sí
                    if(i >= 0){
                        m_oTicket.removeLine(i);
                        m_ticketlines.removeTicketLine(i);
                    }
                    // Actualizo el interfaz
                    m_iProduct = PRODUCT_SINGLE;
                    reloadCatalog();
                }
            }
        }
        
    }
    
    protected class CatalogSelectionListener implements ListSelectionListener {
        public void valueChanged(ListSelectionEvent e) {      
            
            if (!e.getValueIsAdjusting()) {
                int i = m_ticketlines.getSelectedIndex();
                
                // Buscamos el primer producto no Auxiliar.
                while (i >= 0 && m_oTicket.getLine(i).isProductCom()) {
                    i--;
                }
                        
                // Mostramos el panel de catalogo adecuado...
                if (i >= 0) {
                    m_cat.showCatalogPanel(m_oTicket.getLine(i).getProductID());
                } else {
                    m_cat.showCatalogPanel(null);
                }
            }
        }  
    }

    public void customerLoaded(CustomerInfoExt customer) {
        if (m_oTicket != null && m_oTicket.getCustomer() != null
                && customer != null
                && m_oTicket.getCustomer().getId().equals(customer.getId())) {
            // Loading went well and the customer is still the one on the ticket
            logger.log(Level.INFO, "Customer refreshed from server.");
            m_oTicket.setCustomer(customer);
        } else {
            logger.log(Level.INFO,
                    "Customer refresh failed or customer changed.");
        }
    }

    private void initComponents() {
        AppConfig cfg = AppConfig.loadedInstance;
        int btnspacing = WidgetsBuilder.pixelSize(Float.parseFloat(cfg.getProperty("ui.touchbtnspacing")));
        
        java.awt.GridBagConstraints gridBagConstraints;

        JPanel m_jPanContainer = new JPanel(); // The main container
        m_jButtonsExt = new JPanel();
        m_jPanelBag = new JPanel();
        lineBtnsContainer = new JPanel();
        m_jUp = WidgetsBuilder.createButton(ImageLoader.readImageIcon("button_up.png"), AppLocal.getIntString("Button.m_jUpSales.toolTip"));
        m_jDown = WidgetsBuilder.createButton(ImageLoader.readImageIcon("button_down.png"), AppLocal.getIntString("Button.m_jDownSales.toolTip"));
        m_jDelete = WidgetsBuilder.createButton(ImageLoader.readImageIcon("tkt_line_delete.png"), AppLocal.getIntString("Button.m_jDelete.toolTip"));
        m_jList = WidgetsBuilder.createButton(ImageLoader.readImageIcon("tkt_search.png"), AppLocal.getIntString("Button.m_jList.toolTip"));
        m_jEditLine = WidgetsBuilder.createButton(ImageLoader.readImageIcon("tkt_line_edit.png"), AppLocal.getIntString("Button.m_jEditLine.toolTip"));
        m_jbtnLineDiscount = WidgetsBuilder.createButton(ImageLoader.readImageIcon("tkt_line_discount.png"), AppLocal.getIntString("Button.m_jbtnLineDiscount.toolTip"));
        jEditAttributes = WidgetsBuilder.createButton(ImageLoader.readImageIcon("tkt_line_attr.png"), AppLocal.getIntString("Button.jEditAttributes.toolTip"));
        m_jPanTotals = new JPanel();
        m_jTotalEuros = WidgetsBuilder.createImportantLabel();
        this.discountLabel = WidgetsBuilder.createLabel();
        this.subtotalLabel = WidgetsBuilder.createSmallLabel(AppLocal.getIntString("label.subtotalLine"));
        m_jPanEntries = new JPanel();
        m_jNumberKeys = new JNumberKeys();
        jPanel9 = new JPanel();
        m_jPrice = WidgetsBuilder.createLabel();
        m_jPor = WidgetsBuilder.createLabel();
        m_jEnter = WidgetsBuilder.createButton(ImageLoader.readImageIcon("barcode.png"),AppLocal.getIntString("Button.m_jEnter.toolTip"));
        m_jTax = new JComboBox();
        m_jaddtax = new JToggleButton();
        m_jKeyFactory = new JTextField();
        catcontainer = new JPanel();
        m_jInputContainer = new JPanel();
        m_jTariff = WidgetsBuilder.createComboBox();
        JLabel tariffLbl = WidgetsBuilder.createLabel(AppLocal.getIntString("Label.TariffArea"));

        this.setBackground(new Color(255, 204, 153));
        this.setLayout(new CardLayout());


        m_jPanContainer.setLayout(new GridBagLayout());
        GridBagConstraints cstr = null;

        // Main container is broken into 4 vertical parts
        // Brand header
        // Ticket header
        // Input
        // Footer

        // Brand header
        ///////////////
        JPanel brandHeader = new JPanel();
        brandHeader.setLayout(new GridBagLayout());
        ImageIcon brand = ImageLoader.readImageIcon("logo_flat.png");
        JLabel brandLabel = new JLabel(brand);
        this.clock = WidgetsBuilder.createLabel("00:00");
        this.messageBox = WidgetsBuilder.createTextField();
        cstr = new GridBagConstraints();
        cstr.gridy = 0;
        brandHeader.add(brandLabel, cstr);
        cstr = new GridBagConstraints();
        cstr.gridy = 0;
        cstr.fill = GridBagConstraints.BOTH;
        cstr.weightx = 1.0;
        cstr.insets = new Insets(5, 20, 5, 20);
        brandHeader.add(this.messageBox, cstr);
        cstr = new GridBagConstraints();
        cstr.gridy = 0;
        brandHeader.add(this.clock, cstr);
        cstr = new GridBagConstraints();
        cstr.gridy = 0;
        cstr.insets = new Insets(5, 5, 5, 5);
        cstr.fill = GridBagConstraints.HORIZONTAL;
        m_jPanContainer.add(brandHeader, cstr);

        // Ticket info/buttons
        //////////////////////
        JPanel ticketHeader = new JPanel();
        ticketHeader.setLayout(new GridBagLayout());
        // Ticket id
        m_jTicketId = WidgetsBuilder.createLabel();
        m_jTicketId.setBackground(java.awt.Color.white);
        m_jTicketId.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        m_jTicketId.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createLineBorder(javax.swing.UIManager.getDefaults().getColor("Button.darkShadow")), javax.swing.BorderFactory.createEmptyBorder(1, 4, 1, 4)));
        m_jTicketId.setOpaque(true);
        m_jTicketId.setPreferredSize(new java.awt.Dimension(160, 25));
        m_jTicketId.setRequestFocusEnabled(false);
        cstr = new GridBagConstraints();
        cstr.gridy = 0;
        cstr.weightx = 1.0;
        cstr.fill = GridBagConstraints.NONE;
        ticketHeader.add(m_jTicketId, cstr);
        // Customer button
        btnCustomer = WidgetsBuilder.createButton(ImageLoader.readImageIcon("tkt_assign_customer.png"), AppLocal.getIntString("Button.btnCustomer.toolTip"));
        btnCustomer.setFocusPainted(false);
        btnCustomer.setFocusable(false);
        btnCustomer.setRequestFocusEnabled(false);
        btnCustomer.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCustomerActionPerformed(evt);
            }
        });
        cstr = new GridBagConstraints();
        cstr.gridy = 0;
        cstr.insets = new Insets(0, btnspacing, 0, btnspacing);
        ticketHeader.add(btnCustomer, cstr);
        // Split button
        btnSplit = WidgetsBuilder.createButton(ImageLoader.readImageIcon("tkt_split.png"),AppLocal.getIntString("Button.btnSplit.toolTip"));
        btnSplit.setFocusPainted(false);
        btnSplit.setFocusable(false);
        btnSplit.setRequestFocusEnabled(false);
        btnSplit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSplitActionPerformed(evt);
            }
        });
        cstr = new GridBagConstraints();
        cstr.gridy = 0;
        ticketHeader.add(btnSplit, cstr);
        // Ticket bag extra buttons
        cstr = new GridBagConstraints();
        cstr.gridy = 0;
        ticketHeader.add(m_jPanelBag, cstr);
        // Script extra buttons
        cstr = new GridBagConstraints();
        cstr.gridy = 0;
        ticketHeader.add(m_jButtonsExt, cstr);
        // Add container
        cstr = new GridBagConstraints();
        cstr.gridx = 0;
        cstr.fill = GridBagConstraints.HORIZONTAL;
        m_jPanContainer.add(ticketHeader, cstr);        
        
        // Main zone
        ////////////
        JPanel mainZone = new JPanel();
        mainZone.setLayout(new GridBagLayout());
        // Catalog
        catcontainer.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        catcontainer.setLayout(new java.awt.BorderLayout());
        cstr = new GridBagConstraints();
        cstr.gridx = 0;
        cstr.gridy = 0;
        cstr.gridheight = 3;
        cstr.weightx = 1.0;
        cstr.weighty = 1.0;
        cstr.fill = GridBagConstraints.BOTH;
        mainZone.add(catcontainer, cstr);
        // Ticket zone
        JPanel ticketZone = new JPanel();
        ticketZone.setLayout(new GridBagLayout());
        // Tariff area
        m_jTariff.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_jTariffActionPerformed(evt);
            }
        });
        cstr = new GridBagConstraints();
        cstr.gridx = 0;
        cstr.gridy = 0;
        cstr.gridwidth = 2;
        cstr.fill = GridBagConstraints.HORIZONTAL;
        cstr.insets = new Insets(btnspacing, btnspacing,
                btnspacing, btnspacing);
        ticketZone.add(m_jTariff, cstr);
        // Ticket lines
        m_ticketlines = new JTicketLines();
        m_ticketlines.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        cstr = new GridBagConstraints();
        cstr.gridx = 0;
        cstr.gridy = 1;
        cstr.fill = GridBagConstraints.BOTH;
        cstr.weightx = 1.0;
        cstr.weighty = 1.0;
        ticketZone.add(m_ticketlines, cstr);

        JPanel lineEditBtns = new JPanel();
        lineEditBtns.setLayout(new GridBagLayout());
        // Up/down buttons
        if (cfg == null
                || cfg.getProperty("ui.showupdownbuttons").equals("1")) {
            m_jUp.setFocusPainted(false);
            m_jUp.setFocusable(false);
            m_jUp.setRequestFocusEnabled(false);
            m_jUp.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    m_jUpActionPerformed(evt);
                }
            });
            cstr = new GridBagConstraints();
            cstr.gridx = 0;
            cstr.insets = new Insets(0, 0, btnspacing, btnspacing);
            lineEditBtns.add(m_jUp, cstr);
            m_jDown.setFocusPainted(false);
            m_jDown.setFocusable(false);
            m_jDown.setRequestFocusEnabled(false);
            m_jDown.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    m_jDownActionPerformed(evt);
                }
            });
            cstr = new GridBagConstraints();
            cstr.gridx = 0;
            cstr.insets = new Insets(0, 0, btnspacing, btnspacing);
            lineEditBtns.add(m_jDown, cstr);
        }
        // Delete line
        m_jDelete.setFocusPainted(false);
        m_jDelete.setFocusable(false);
        m_jDelete.setRequestFocusEnabled(false);
        m_jDelete.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_jDeleteActionPerformed(evt);
            }
        });
        cstr = new GridBagConstraints();
        cstr.gridx = 0;
        cstr.insets = new Insets(0, 0, btnspacing, btnspacing);
        lineEditBtns.add(m_jDelete, cstr);
        // Find product
        m_jList.setFocusPainted(false);
        m_jList.setFocusable(false);
        m_jList.setRequestFocusEnabled(false);
        m_jList.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_jListActionPerformed(evt);
            }
        });
        cstr = new GridBagConstraints();
        cstr.gridx = 0;
        cstr.insets = new Insets(0, 0, btnspacing, btnspacing);
        lineEditBtns.add(m_jList, cstr);
        // Edit line
        m_jEditLine.setFocusPainted(false);
        m_jEditLine.setFocusable(false);
        m_jEditLine.setRequestFocusEnabled(false);
        m_jEditLine.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_jEditLineActionPerformed(evt);
            }
        });
        cstr = new GridBagConstraints();
        cstr.gridx = 0;
        cstr.insets = new Insets(0, 0, btnspacing, btnspacing);
        lineEditBtns.add(m_jEditLine, cstr);
        // Attributes
        jEditAttributes.setFocusPainted(false);
        jEditAttributes.setFocusable(false);
        jEditAttributes.setRequestFocusEnabled(false);
        jEditAttributes.setEnabled(false); // TODO: set attributes
        jEditAttributes.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jEditAttributesActionPerformed(BUTTON_PERFORMED);
            }
        });
        cstr = new GridBagConstraints();
        cstr.gridx = 0;
        cstr.insets = new Insets(0, 0, btnspacing, btnspacing);
        // disabled until come back
        //lineEditBtns.add(jEditAttributes, cstr);
        // Line discount button
        m_jbtnLineDiscount.setFocusPainted(false);
        m_jbtnLineDiscount.setFocusable(false);
        m_jbtnLineDiscount.setRequestFocusEnabled(false);
        m_jbtnLineDiscount.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_jbtnLineDiscountActionPerformed(evt);
            }
        });
        cstr = new GridBagConstraints();
        cstr.gridx = 0;
        cstr.insets = new Insets(0, 0, btnspacing, btnspacing);
        lineEditBtns.add(m_jbtnLineDiscount, cstr);
        // Add line edit container
        cstr = new GridBagConstraints();
        cstr.gridx = 1;
        cstr.gridy = 1;
        cstr.weighty = 1.0;
        cstr.fill = GridBagConstraints.VERTICAL;
        ticketZone.add(lineEditBtns, cstr);
        // Total zone
        JPanel totalZone = new JPanel();
        totalZone.setLayout(new GridBagLayout());
        // Discount
        this.discountLabel.setRequestFocusEnabled(false);
        cstr = new GridBagConstraints();
        cstr.gridx = 0;
        cstr.gridy = 0;
        cstr.anchor = GridBagConstraints.CENTER;
        cstr.fill = GridBagConstraints.HORIZONTAL;
        totalZone.add(this.discountLabel, cstr);
        // Total
        m_jTotalEuros.setRequestFocusEnabled(false);
        cstr = new GridBagConstraints();
        cstr.gridx = 1;
        cstr.gridy = 0;
        cstr.anchor = GridBagConstraints.FIRST_LINE_END;
        cstr.weightx = 1.0;
        //cstr.fill = GridBagConstraints.HORIZONTAL;
        totalZone.add(m_jTotalEuros, cstr);
        // Subtotal and total (label and amount)
        this.subtotalLabel.setRequestFocusEnabled(false);
        cstr = new GridBagConstraints();
        cstr.gridx = 0;
        cstr.gridy = 1;
        cstr.gridwidth = 2;
        cstr.weightx = 1.0;
        cstr.anchor = GridBagConstraints.FIRST_LINE_END;
        cstr.insets = new java.awt.Insets(5, 5, 5, 5);
        totalZone.add(this.subtotalLabel, cstr);
        // Add total zone
        cstr = new GridBagConstraints();
        cstr.gridx = 0;
        cstr.gridy = 2;
        cstr.gridwidth = 2;
        cstr.fill = GridBagConstraints.HORIZONTAL;
        ticketZone.add(totalZone, cstr);
        // Add ticket zone
        cstr = new GridBagConstraints();
        cstr.gridy = 0;
        cstr.gridx = 1;
        cstr.fill = GridBagConstraints.BOTH;
        cstr.weighty = 1.0;
        mainZone.add(ticketZone, cstr);
        // Barcode and manual input zone
        JPanel barcodeZone = new JPanel();
        barcodeZone.setLayout(new GridBagLayout());
        m_jPrice.setBackground(java.awt.Color.white);
        m_jPrice.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        m_jPrice.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createLineBorder(javax.swing.UIManager.getDefaults().getColor("Button.darkShadow")), javax.swing.BorderFactory.createEmptyBorder(1, 4, 1, 4)));
        m_jPrice.setOpaque(true);
        m_jPrice.setPreferredSize(new java.awt.Dimension(100, 22));
        m_jPrice.setRequestFocusEnabled(false);
        cstr = new java.awt.GridBagConstraints();
        cstr.gridx = 0;
        cstr.gridy = 0;
        cstr.gridwidth = 2;
        cstr.insets = new Insets(btnspacing, btnspacing, btnspacing, btnspacing);
        cstr.fill = java.awt.GridBagConstraints.BOTH;
        cstr.weightx = 1.0;
        cstr.weighty = 1.0;
        barcodeZone.add(m_jPrice, cstr);
        m_jPor.setBackground(java.awt.Color.white);
        m_jPor.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        m_jPor.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createLineBorder(javax.swing.UIManager.getDefaults().getColor("Button.darkShadow")), javax.swing.BorderFactory.createEmptyBorder(1, 4, 1, 4)));
        m_jPor.setOpaque(true);
        m_jPor.setPreferredSize(new java.awt.Dimension(22, 22));
        m_jPor.setRequestFocusEnabled(false);
        cstr = new java.awt.GridBagConstraints();
        cstr.gridx = 2;
        cstr.gridy = 0;
        cstr.fill = java.awt.GridBagConstraints.BOTH;
        cstr.weightx = 1.0;
        cstr.weighty = 1.0;
        cstr.insets = new java.awt.Insets(btnspacing, 0, btnspacing, 0);
        barcodeZone.add(m_jPor, cstr);
        m_jEnter.setFocusPainted(false);
        m_jEnter.setFocusable(false);
        m_jEnter.setRequestFocusEnabled(false);
        m_jEnter.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_jEnterActionPerformed(evt);
            }
        });
        cstr = new java.awt.GridBagConstraints();
        cstr.gridx = 3;
        cstr.gridy = 0;
        cstr.insets = new java.awt.Insets(btnspacing, btnspacing,
                btnspacing, btnspacing);
        barcodeZone.add(m_jEnter, cstr);
        cstr = new GridBagConstraints();
        cstr.gridx = 1;
        cstr.gridy = 1;
        cstr.fill = GridBagConstraints.HORIZONTAL;
        mainZone.add(barcodeZone, cstr);
        // Numpad zone
        JPanel numpadZone = new JPanel();
        numpadZone.setLayout(new GridBagLayout());
        m_jNumberKeys.addJNumberEventListener(new JNumberEventListener() {
            public void keyPerformed(JNumberEvent evt) {
                m_jNumberKeysKeyPerformed(evt);
            }
        });
        cstr = new GridBagConstraints();
        cstr.gridx = 1;
        cstr.gridy = 2;
        mainZone.add(m_jNumberKeys, cstr);
        // Add main zone container
        cstr = new GridBagConstraints();
        cstr.gridx = 0;
        cstr.weightx = 1.0;
        cstr.weighty = 1.0;
        cstr.fill = GridBagConstraints.BOTH;
        m_jPanContainer.add(mainZone, cstr);

        // Footer line
        //////////////

        m_jPanTotals.setLayout(new java.awt.GridBagLayout());

        m_jPanEntries.setLayout(new javax.swing.BoxLayout(m_jPanEntries, javax.swing.BoxLayout.Y_AXIS));

        m_jTax.setFocusable(false);
        m_jTax.setRequestFocusEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        jPanel9.add(m_jTax, gridBagConstraints);

        m_jaddtax.setText("+");
        m_jaddtax.setFocusPainted(false);
        m_jaddtax.setFocusable(false);
        m_jaddtax.setRequestFocusEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
        jPanel9.add(m_jaddtax, gridBagConstraints);

        jPanel9.setMaximumSize(new java.awt.Dimension(m_jNumberKeys.getMaximumSize().width, jPanel9.getPreferredSize().height));
        m_jPanEntries.add(jPanel9);

        m_jKeyFactory.setBackground(javax.swing.UIManager.getDefaults().getColor("Panel.background"));
        m_jKeyFactory.setForeground(javax.swing.UIManager.getDefaults().getColor("Panel.background"));
        m_jKeyFactory.setBorder(null);
        m_jKeyFactory.setCaretColor(javax.swing.UIManager.getDefaults().getColor("Panel.background"));
        m_jKeyFactory.setPreferredSize(new java.awt.Dimension(1, 1));
        m_jKeyFactory.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                m_jKeyFactoryKeyTyped(evt);
            }
            public void keyPressed(java.awt.event.KeyEvent evt) {
                m_jKeyFactoryKeyPressed(evt);
            }
        });
        m_jPanEntries.add(m_jKeyFactory);

        cstr = new GridBagConstraints();
        cstr.gridx = 2;
        cstr.gridy = 0;
        cstr.gridheight = 2;
        m_jInputContainer.add(m_jPanEntries, cstr);
        

        cstr = new GridBagConstraints();
        cstr.gridx = 0;
        cstr.gridy = 1;
        cstr.weightx = 1.0;
        cstr.fill = GridBagConstraints.HORIZONTAL;
        //m_jPanContainer.add(m_jInputContainer, cstr);

        this.add(m_jPanContainer, "ticket");
    }

    private void m_jbtnScaleActionPerformed(java.awt.event.ActionEvent evt) {

        automator('\u00a7');
        
    }

    private void m_jEditLineActionPerformed(java.awt.event.ActionEvent evt) {

        int i = m_ticketlines.getSelectedIndex();
        if (i < 0){
            Toolkit.getDefaultToolkit().beep(); // no line selected
        } else {
            try {
                TicketLineInfo newline = JProductLineEdit.showMessage(this, m_App, m_oTicket.getLine(i));
                if (newline != null) {
                    // line has been modified
                    paintTicketLine(i, newline);
                }
            } catch (BasicException e) {
                new MessageInf(e).show(this);
            }
        }
    }

    private void m_jbtnLineDiscountActionPerformed(java.awt.event.ActionEvent evt) {
        double discountRate = this.getInputValue() / 100.0;

        int index = m_ticketlines.getSelectedIndex();
        if (index >= 0) {
            TicketLineInfo line = m_oTicket.getLine(index);
            if (discountRate > 0.005) {
                line.setDiscountRate(discountRate);
                this.refreshTicket();
            } else {
                 // No rate
                 MessageInf msg = new MessageInf(MessageInf.SGN_WARNING,
                         AppLocal.getIntString("message.selectratefordiscount"));
                 msg.show(this);
                 java.awt.Toolkit.getDefaultToolkit().beep();
            }
        } else {
            // No item or discount selected
            MessageInf msg = new MessageInf(MessageInf.SGN_WARNING,
                    AppLocal.getIntString("message.selectlinefordiscount"));
            msg.show(this);
            java.awt.Toolkit.getDefaultToolkit().beep();
        }
    }

    private void m_jTariffActionPerformed(java.awt.event.ActionEvent evt) {
        try{
            TariffInfo tariff = (TariffInfo) m_jTariff.getSelectedItem();
            if(tariff!=null) {
                this.switchTariffArea(tariff);
            }
        } catch(java.lang.ClassCastException e){
            
        }
    }

    private void m_jEnterActionPerformed(java.awt.event.ActionEvent evt) {

        automator('\n');

    }

    private void m_jNumberKeysKeyPerformed(JNumberEvent evt) {

        automator(evt.getKey());

    }

    private void m_jKeyFactoryKeyTyped(java.awt.event.KeyEvent evt) {
        m_jKeyFactory.setText(null);
        automator(evt.getKeyChar());
    }

    private void m_jKeyFactoryKeyPressed(java.awt.event.KeyEvent evt) {
        // Adding shortcuts for product's selection in panel sales screen
        if (evt.getKeyCode() == evt.VK_UP || evt.getKeyCode() == evt.VK_PAGE_UP) {
                m_ticketlines.selectionUp();
            } else if (evt.getKeyCode() == evt.VK_DOWN || evt.getKeyCode() == evt.VK_PAGE_DOWN) {
                m_ticketlines.selectionDown();
            }
        }

    private void m_jDeleteActionPerformed(java.awt.event.ActionEvent evt) {

        int i = m_ticketlines.getSelectedIndex();
        if (i < 0){
            Toolkit.getDefaultToolkit().beep(); // No hay ninguna seleccionada
        } else {               
            removeTicketLine(i); // elimino la linea           
        }   

    }

    private void m_jUpActionPerformed(java.awt.event.ActionEvent evt) {

        m_ticketlines.selectionUp();

    }

    private void m_jDownActionPerformed(java.awt.event.ActionEvent evt) {

        m_ticketlines.selectionDown();

    }

    private void m_jListActionPerformed(java.awt.event.ActionEvent evt) {

        ProductInfoExt prod = JProductFinder.showMessage(JPanelTicket.this, dlSales);    
        if (prod != null) {
            buttonTransition(prod);
        }
        
    }

    private void btnCustomerActionPerformed(java.awt.event.ActionEvent evt) {

        JCustomerFinder finder = JCustomerFinder.getCustomerFinder(this, dlCustomers);
        finder.search(m_oTicket.getCustomer());
        finder.setVisible(true);

        CustomerInfoExt customer = finder.getSelectedCustomer();
        this.m_oTicket.setCustomer(finder.getSelectedCustomer());
        if (customer != null && customer.getDiscountProfileId() != null) {
            // Set discount profile and rate to ticket
            try {
                DiscountProfile profile = dlCustomers.getDiscountProfile(customer.getDiscountProfileId());
                this.m_oTicket.setDiscountRate(profile.getRate());
                this.m_oTicket.setDiscountProfileId(profile.getId());
            } catch (BasicException e) {
                e.printStackTrace();
            }
        } else {
            // Reset discount profile and rate
            this.m_oTicket.setDiscountProfileId(null);
            this.m_oTicket.setDiscountRate(0.0);
        }
        if (customer != null) {
            // Refresh customer from server
            dlCustomers.updateCustomer(customer.getId(), this);
        }
        refreshTicket();
    }

    private void btnSplitActionPerformed(java.awt.event.ActionEvent evt) {

        if (m_oTicket.getLinesCount() > 0) {
            ReceiptSplit splitdialog = ReceiptSplit.getDialog(this, dlSystem.getResourceAsXML("Ticket.Line"), dlSales, dlCustomers, taxeslogic);
            
            TicketInfo ticket1 = m_oTicket.copyTicket();
            TicketInfo ticket2 = new TicketInfo();
            ticket2.setCustomer(m_oTicket.getCustomer());
            
            if (splitdialog.showDialog(ticket1, ticket2, m_oTicketExt)) {
                if (closeTicket(ticket2, m_oTicketExt)) { // already checked  that number of lines > 0                            
                    setActiveTicket(ticket1, m_oTicketExt);// set result ticket
                }
            }
        }
    }

    private void jEditAttributesActionPerformed(int cameFrom) {

        int i = m_ticketlines.getSelectedIndex();
        if (i < 0) {
            Toolkit.getDefaultToolkit().beep(); // no line selected
        } else {
            try {
                TicketLineInfo line = m_oTicket.getLine(i);
                JProductAttEdit attedit = JProductAttEdit.getAttributesEditor(this, m_App.getSession());
                attedit.editAttributes(line.getProductAttSetId(), line.getProductAttSetInstId());
                attedit.setVisible(true);
                if (attedit.isOK()) {
                    // The user pressed OK
                    line.setProductAttSetInstId(attedit.getAttributeSetInst());
                    line.setProductAttSetInstDesc(attedit.getAttributeSetInstDescription());
                    paintTicketLine(i, line);
                }
            }
            catch (BasicException ex) {
                if (cameFrom == BUTTON_PERFORMED) {
                    MessageInf msg = new MessageInf(MessageInf.SGN_WARNING, AppLocal.getIntString("message.cannotfindattributes"), ex);
                    msg.show(this);
                }
            }
        }
    }

    private javax.swing.JButton btnCustomer;
    private javax.swing.JButton btnSplit;
    private javax.swing.JPanel catcontainer;
    private javax.swing.JButton jEditAttributes;
    private javax.swing.JPanel lineBtnsContainer;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JPanel m_jButtons;
    private javax.swing.JPanel m_jButtonsExt;
    private javax.swing.JButton m_jDelete;
    private javax.swing.JButton m_jDown;
    private javax.swing.JButton m_jEditLine;
    private javax.swing.JButton m_jEnter;
    private javax.swing.JTextField m_jKeyFactory;
    private javax.swing.JButton m_jList;
    private JNumberKeys m_jNumberKeys;
    private javax.swing.JPanel m_jPanEntries;
    private javax.swing.JPanel m_jPanTotals;
    private javax.swing.JPanel m_jPanelBag;
    private javax.swing.JLabel m_jPor;
    private javax.swing.JLabel m_jPrice;
    private javax.swing.JComboBox m_jTax;
    private javax.swing.JLabel m_jTicketId;
    private javax.swing.JLabel m_jTotalEuros;
    private javax.swing.JLabel subtotalLabel;
    private javax.swing.JLabel discountLabel;
    private javax.swing.JButton m_jUp;
    private javax.swing.JToggleButton m_jaddtax;
    private javax.swing.JButton m_jbtnLineDiscount;
    private javax.swing.JPanel m_jInputContainer;
    private javax.swing.JComboBox m_jTariff;
    private javax.swing.JLabel clock;
    private javax.swing.JTextField messageBox;
}
