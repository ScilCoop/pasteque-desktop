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

package com.openbravo.pos.sales;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Date;

import com.openbravo.format.Formats;
import com.openbravo.data.gui.ComboBoxValModel;
import com.openbravo.data.gui.MessageInf;
import com.openbravo.pos.printer.*;

import com.openbravo.pos.forms.JPanelView;
import com.openbravo.pos.forms.AppView;
import com.openbravo.pos.forms.AppLocal;
import com.openbravo.pos.panels.JProductFinder;
import com.openbravo.pos.scale.ScaleException;
import com.openbravo.pos.payment.JPaymentSelect;
import com.openbravo.basic.BasicException;
import com.openbravo.data.gui.ListKeyed;
import com.openbravo.data.loader.SentenceList;
import com.openbravo.pos.customers.CustomerInfoExt;
import com.openbravo.pos.customers.DataLogicCustomers;
import com.openbravo.pos.customers.JCustomerFinder;
import com.openbravo.pos.scripting.ScriptEngine;
import com.openbravo.pos.scripting.ScriptException;
import com.openbravo.pos.scripting.ScriptFactory;
import com.openbravo.pos.forms.DataLogicSystem;
import com.openbravo.pos.forms.DataLogicSales;
import com.openbravo.pos.forms.BeanFactoryApp;
import com.openbravo.pos.forms.BeanFactoryException;
import com.openbravo.pos.forms.AppConfig;
import com.openbravo.pos.inventory.TaxCategoryInfo;
import com.openbravo.pos.payment.JPaymentSelectReceipt;
import com.openbravo.pos.payment.JPaymentSelectRefund;
import com.openbravo.pos.ticket.ProductInfoExt;
import com.openbravo.pos.ticket.TaxInfo;
import com.openbravo.pos.ticket.TicketInfo;
import com.openbravo.pos.ticket.TicketLineInfo;
import com.openbravo.pos.util.JRPrinterAWT300;
import com.openbravo.pos.util.ReportUtils;
import com.openbravo.pos.widgets.JEditorCurrencyPositive;
import com.openbravo.pos.widgets.JEditorKeys;
import com.openbravo.pos.widgets.JNumberEvent;
import com.openbravo.pos.widgets.JNumberEventListener;
import com.openbravo.pos.widgets.JNumberKeys;
import com.openbravo.pos.widgets.WidgetsBuilder;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import javax.print.PrintService;
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
public abstract class JPanelTicket extends JPanel implements JPanelView, BeanFactoryApp, TicketsEditor {

    protected JTicketLines m_ticketlines;

    // private Template m_tempLine;
    private TicketParser m_TTP;

    protected TicketInfo m_oTicket; 
    protected Object m_oTicketExt; 

    private StringBuffer m_sBarcode;

    private JTicketsBag m_ticketsbag;

    private SentenceList senttax;
    private ListKeyed taxcollection;
    // private ComboBoxValModel m_TaxModel;

    private SentenceList senttaxcategories;
    private ListKeyed taxcategoriescollection;
    private ComboBoxValModel taxcategoriesmodel;
    
    private TaxesLogic taxeslogic;

//    private ScriptObject scriptobjinst;
    protected JPanelButtons m_jbtnconfig;

    protected AppView m_App;
    protected DataLogicSystem dlSystem;
    protected DataLogicSales dlSales;
    protected DataLogicCustomers dlCustomers;

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

    private static final char keyBack = '\u0008';
    private static final char keyDel = '\u007f';
    private static final char keySection = '\u00a7';
    private static final char keyEnter = '\n';


    /** Creates new form JTicketView */
    public JPanelTicket() {
        
        initComponents ();
    }

    public void init(AppView app) throws BeanFactoryException {
        m_App = app;
        dlSystem = (DataLogicSystem) m_App.getBean("com.openbravo.pos.forms.DataLogicSystem");
        dlSales = (DataLogicSales) m_App.getBean("com.openbravo.pos.forms.DataLogicSales");
        dlCustomers = (DataLogicCustomers) m_App.getBean("com.openbravo.pos.customers.DataLogicCustomers");
                    

        if (!m_App.getDeviceScale().existsScale()) {
            lineBtnsContainer.remove(m_jbtnScale);
        }
        
        m_ticketsbag = getJTicketsBag();
        m_jPanelBag.add(m_ticketsbag.getBagComponent(), BorderLayout.LINE_START);
        add(m_ticketsbag.getNullComponent(), "null");

        m_ticketlines = new JTicketLines(dlSystem.getResourceAsXML("Ticket.Line"));
        m_ticketlines.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        GridBagConstraints cstr = new GridBagConstraints();
        cstr.gridx = 0;
        cstr.gridy = 0;
        cstr.fill = GridBagConstraints.BOTH;
        cstr.weightx = 1.0;
        cstr.weighty = 1.0;
        m_jInputContainer.add(m_ticketlines, cstr);

        
        m_TTP = new TicketParser(m_App.getDeviceTicket(), dlSystem);
               
        // Los botones configurables...
        m_jbtnconfig = new JPanelButtons("Ticket.Buttons", this);
        m_jButtonsExt.add(m_jbtnconfig);           
       
        // El panel de los productos o de las lineas...        
        catcontainer.add(getSouthComponent(), BorderLayout.CENTER);
        
        // El modelo de impuestos
        senttax = dlSales.getTaxList();
        senttaxcategories = dlSales.getTaxCategoriesList();
        
        taxcategoriesmodel = new ComboBoxValModel();

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
        java.util.List<TaxInfo> taxlist = senttax.list();
        taxcollection = new ListKeyed<TaxInfo>(taxlist);
        java.util.List<TaxCategoryInfo> taxcategorieslist = senttaxcategories.list();
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
        
        // Authorization for buttons
        btnSplit.setEnabled(m_App.getAppUserView().getUser().hasPermission("sales.Total"));
        m_jDelete.setEnabled(m_App.getAppUserView().getUser().hasPermission("sales.EditLines"));
        m_jNumberKeys.setMinusEnabled(m_App.getAppUserView().getUser().hasPermission("sales.EditLines"));
        m_jNumberKeys.setEqualsEnabled(m_App.getAppUserView().getUser().hasPermission("sales.Total"));
        m_jbtnconfig.setPermissions(m_App.getAppUserView().getUser());  
               
        m_ticketsbag.activate();        
    }
    
    public boolean deactivate() {

        return m_ticketsbag.deactivate();
    }
    
    protected abstract JTicketsBag getJTicketsBag();
    protected abstract Component getSouthComponent();
    protected abstract void resetSouthComponent();
     
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
            Color green = new Color(64,150,23);
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
           
            m_jSubtotalEuros.setText(null);
            m_jTaxesEuros.setText(null);
            m_jTotalEuros.setText(null); 
        
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
        }
    }
       
    private void printPartialTotals(){
        if (m_oTicket == null) {
            m_jSubtotalEuros.setText(null);
            m_jTaxesEuros.setText(null);
            m_jTotalEuros.setText(null);
        } else {
            m_jSubtotalEuros.setText(m_oTicket.printSubTotal());
            m_jTaxesEuros.setText(m_oTicket.printTax());
            m_jTotalEuros.setText(m_oTicket.printTotal());
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
        addTicketLine(line);
        if (oProduct.isDiscountEnabled()) {
            double rate = oProduct.getDiscountRate();
            if (rate > 0.005) {
                // Add discount line
                this.addDiscountLine(line, oProduct.getDiscountRate());
            }
        }
    }
    
    protected void addTicketLine(TicketLineInfo oLine) {   
        
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

    /** Add a discount line related to an other one */
    private void addDiscountLine(TicketLineInfo line, double discountRate) {
        int index = this.m_ticketlines.getSelectedIndex();
        String sdiscount = Formats.PERCENT.formatValue(discountRate);
        TicketLineInfo newLine = new TicketLineInfo(
                AppLocal.getIntString("label.discount") + " " + sdiscount,
                line.getProductTaxCategoryID(),
                line.getMultiply(),
                -line.getPrice () * discountRate,
                line.getTaxInfo());
        newLine.setDiscount(true);
        this.m_oTicket.insertLine(index + 1, newLine);                    
        this.refreshTicket();
        this.setSelectedIndex(index + 1);
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
            jEditAttributesActionPerformed();
        }
    }

    protected void buttonTransition(ProductInfoExt prod) {
    // precondicion: prod != null

         if (m_PriceActualState == N_NOTHING && m_QuantityActualState == N_NOTHING) {
            incProduct(prod);
        } else if (m_PriceActualState != N_NOTHING && m_PriceActualState != N_ZERO && m_PriceActualState != N_DECIMALZERO
                && m_QuantityActualState != N_NOTHING && m_QuantityActualState != N_ZERO && m_QuantityActualState != N_DECIMALZERO) {
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
                        CustomerInfoExt newcustomer = dlSales.findCustomerExt(sCode);
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
            }

            // + without input: increment selected line quantity
            else if (entered == '+'
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
            }
                // - without input: decrement selected line quantity
                // Remove line if quantity is set to 0
                else if (entered == '-'
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
                }
                // + with multiply input (without price): replace quantity
                else if (entered == '+'
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
                }
                // - with multiply input (without price): set negative quantity
                else if (entered == '-'
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
                }
                // + with price input (without multiply): create an empty line
                // with entered price
                else if (entered == '+'
                        && m_PriceActualState != N_NOTHING && m_PriceActualState != N_ZERO && m_PriceActualState != N_DECIMALZERO
                        && m_QuantityActualState == N_NOTHING
                        && m_App.getAppUserView().getUser().hasPermission("sales.EditLines")) {
                    ProductInfoExt product = getInputProduct();
                    addTicketLine(product, 1.0, product.getPriceSell());
                }
                // - with price input (without multiply): create an empty line
                // with negative entered price
                else if (entered == '-'
                        && m_PriceActualState != N_NOTHING && m_PriceActualState != N_ZERO && m_PriceActualState != N_DECIMALZERO
                        && m_QuantityActualState == N_NOTHING
                        && m_App.getAppUserView().getUser().hasPermission("sales.EditLines")) {
                    ProductInfoExt product = getInputProduct();
                    addTicketLine(product, 1.0, -product.getPriceSell());
                }
                // + with price and multiply: create an empty line with entered price
                // and quantity set to entered multiply
                else if (entered == '+'
                        && m_PriceActualState != N_NOTHING && m_PriceActualState != N_ZERO && m_PriceActualState != N_DECIMALZERO
                        && m_QuantityActualState != N_NOTHING && m_QuantityActualState != N_ZERO && m_PriceActualState != N_DECIMALZERO
                        && m_App.getAppUserView().getUser().hasPermission("sales.EditLines")) {
                    ProductInfoExt product = getInputProduct();
                    addTicketLine(product, getPorValue(), product.getPriceSell());
                }
                // - with price and multiply: create an empty line with entered
                // negative price and quantity set to entered multiply
                else if (entered == '-'
                        && m_PriceActualState != N_NOTHING && m_PriceActualState != N_ZERO && m_PriceActualState != N_DECIMALZERO
                        && m_QuantityActualState != N_NOTHING && m_QuantityActualState != N_ZERO && m_PriceActualState != N_DECIMALZERO
                        && m_App.getAppUserView().getUser().hasPermission("sales.EditLines")) {
                    ProductInfoExt product = getInputProduct();
                    addTicketLine(product, getPorValue(), -product.getPriceSell());
                }
                // Space bar or = : go to payments
                else if (entered == ' ' || entered == '=') {
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
                }
                else if (entered == keySection
                    && m_PriceActualState != N_NOTHING && m_PriceActualState != N_ZERO && m_PriceActualState != N_DECIMALZERO
                    && m_QuantityActualState == N_NOTHING) {
                // Scale button pressed and a number typed as a price
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
            }
            else if (entered == keySection
                    && m_PriceActualState == N_NOTHING
                    && m_QuantityActualState == N_NOTHING) {
                // Scale button pressed and no number typed.
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

            //
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
            m_PriceActualState = N_NOTHING;
            m_InputState = I_NOTHING;
            m_jPrice.setText("");

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
            } else if (back == 0 && m_PriceActualState == N_NUMBER) {
                m_PriceActualState = N_NOTHING;
            }
            if (back >=0) {
                price = price.substring(0, back);
                m_jPrice.setText(price);
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
                    paymentdialog.setPrintSelected("true".equals(m_jbtnconfig.getProperty("printselected", "true")));

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
                                dlSales.saveTicket(ticket, m_App.getInventoryLocation());                       
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

    private void initComponents() {
        AppConfig cfg = AppConfig.loadedInstance;
        int btnspacing = WidgetsBuilder.pixelSize(Float.parseFloat(cfg.getProperty("ui.touchbtnspacing")));
        
        java.awt.GridBagConstraints gridBagConstraints;

        JPanel m_jPanContainer = new JPanel(); // The main container
        m_jButtonsExt = new JPanel();
        m_jbtnScale = WidgetsBuilder.createButton(new ImageIcon(getClass().getResource("/com/openbravo/images/scale.png")), AppLocal.getIntString("Button.m_jbtnScale.toolTip"));
        m_jPanelBag = new JPanel();
        lineBtnsContainer = new JPanel();
        m_jUp = WidgetsBuilder.createButton(new ImageIcon(getClass().getResource("/com/openbravo/images/1uparrow22.png")), AppLocal.getIntString("Button.m_jUpSales.toolTip"));
        m_jDown = WidgetsBuilder.createButton(new ImageIcon(getClass().getResource("/com/openbravo/images/1downarrow22.png")), AppLocal.getIntString("Button.m_jDownSales.toolTip"));
        m_jDelete = WidgetsBuilder.createButton(new ImageIcon(getClass().getResource("/com/openbravo/images/locationbar_erase.png")), AppLocal.getIntString("Button.m_jDelete.toolTip"));
        m_jList = WidgetsBuilder.createButton(new ImageIcon(getClass().getResource("/com/openbravo/images/search22.png")), AppLocal.getIntString("Button.m_jList.toolTip"));
        m_jEditLine = WidgetsBuilder.createButton(new ImageIcon(getClass().getResource("/com/openbravo/images/color_line.png")), AppLocal.getIntString("Button.m_jEditLine.toolTip"));
        m_jbtnLineDiscount = WidgetsBuilder.createButton(new ImageIcon(getClass().getResource("/com/openbravo/images/discount.png")), AppLocal.getIntString("Button.m_jbtnLineDiscount.toolTip"));
        jEditAttributes = WidgetsBuilder.createButton(new ImageIcon(getClass().getResource("/com/openbravo/images/colorize.png")), AppLocal.getIntString("Button.jEditAttributes.toolTip"));
        m_jPanelCentral = new JPanel();
        m_jPanTotals = new JPanel();
        m_jTotalEuros = WidgetsBuilder.createImportantLabel();
        m_jLblTotalEuros1 = WidgetsBuilder.createImportantLabel(AppLocal.getIntString("label.totalcash"));
        m_jSubtotalEuros = WidgetsBuilder.createLabel();
        m_jTaxesEuros = WidgetsBuilder.createLabel();
        m_jLblTotalEuros2 = WidgetsBuilder.createLabel();
        m_jLblTotalEuros3 = WidgetsBuilder.createLabel();
        m_jPanEntries = new JPanel();
        m_jNumberKeys = new JNumberKeys();
        jPanel9 = new JPanel();
        m_jPrice = WidgetsBuilder.createLabel();
        m_jPor = WidgetsBuilder.createLabel();
        m_jEnter = WidgetsBuilder.createButton(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/barcode.png")),AppLocal.getIntString("Button.m_jEnter.toolTip"));
        m_jTax = new JComboBox();
        m_jaddtax = new JToggleButton();
        m_jKeyFactory = new JTextField();
        catcontainer = new JPanel();
        m_jInputContainer = new JPanel();

        this.setBackground(new Color(255, 204, 153));
        this.setLayout(new CardLayout());

        m_jPanContainer.setLayout(new GridBagLayout());
        GridBagConstraints cstr = null;

        // Tickets buttons part
        JPanel m_jOptions = new JPanel();
        m_jOptions.setLayout(new GridBagLayout());
        JPanel m_jButtons = new JPanel();
        
        m_jTicketId = WidgetsBuilder.createLabel();
        m_jTicketId.setBackground(java.awt.Color.white);
        m_jTicketId.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        m_jTicketId.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createLineBorder(javax.swing.UIManager.getDefaults().getColor("Button.darkShadow")), javax.swing.BorderFactory.createEmptyBorder(1, 4, 1, 4)));
        m_jTicketId.setOpaque(true);
        m_jTicketId.setPreferredSize(new java.awt.Dimension(160, 25));
        m_jTicketId.setRequestFocusEnabled(false);
        m_jButtons.add(m_jTicketId);

        // Customers list button
        btnCustomer = WidgetsBuilder.createButton(new ImageIcon(getClass().getResource("/com/openbravo/images/kuser.png")), AppLocal.getIntString("Button.btnCustomer.toolTip"));
        btnCustomer.setFocusPainted(false);
        btnCustomer.setFocusable(false);
        btnCustomer.setRequestFocusEnabled(false);
        btnCustomer.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCustomerActionPerformed(evt);
            }
        });
        m_jButtons.add(btnCustomer);

        // Split ticket button
        btnSplit = WidgetsBuilder.createButton(new ImageIcon(getClass().getResource("/com/openbravo/images/editcut.png")),AppLocal.getIntString("Button.btnSplit.toolTip"));
        btnSplit.setFocusPainted(false);
        btnSplit.setFocusable(false);
        btnSplit.setRequestFocusEnabled(false);
        btnSplit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSplitActionPerformed(evt);
            }
        });
        m_jButtons.add(btnSplit);

        cstr = new GridBagConstraints();
        cstr.gridx = 0;
        cstr.gridy = 0;
        m_jOptions.add(m_jButtons, cstr);

        //m_jPanelBag.setLayout(new java.awt.BorderLayout());
        cstr = new GridBagConstraints();
        cstr.gridx = 1;
        cstr.gridy = 0;
        m_jOptions.add(m_jPanelBag, cstr);
        
        // Extra buttons
        //m_jButtonsExt.setLayout(new javax.swing.BoxLayout(m_jButtonsExt, BoxLayout.LINE_AXIS));
        cstr = new GridBagConstraints();
        cstr.gridx = 2;
        cstr.gridy = 0;
        cstr.weightx = 1.0;
        cstr.anchor = GridBagConstraints.LINE_END;
        m_jOptions.add(m_jButtonsExt, cstr);

        
        // Pack buttons line
        m_jOptions.setMaximumSize(new java.awt.Dimension(m_jOptions.getMaximumSize().width, m_jOptions.getPreferredSize().height));

        cstr = new GridBagConstraints();
        cstr.gridx = 0;
        cstr.gridy = 0;
        cstr.weightx = 1.0;
        cstr.fill = GridBagConstraints.HORIZONTAL;
        m_jPanContainer.add(m_jOptions, cstr);
        
        
        // Second panel line: ticket and input keyboard
        m_jInputContainer.setLayout(new GridBagLayout());

        lineBtnsContainer.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 5, 0, 5));
        lineBtnsContainer.setLayout(new GridBagLayout());
        int x = 0;
        int y = 0;
        int buttons = 5;

        // Up/down buttons
        if (cfg == null || cfg.getProperty("ui.showupdownbuttons").equals("1")) {
            m_jUp.setFocusPainted(false);
            m_jUp.setFocusable(false);
            m_jUp.setRequestFocusEnabled(false);
            m_jUp.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    m_jUpActionPerformed(evt);
                }
            });
            cstr = new GridBagConstraints();
            cstr.gridx = x;
            cstr.gridy = y++;
            cstr.insets = new Insets(0, 0, btnspacing, btnspacing);
            lineBtnsContainer.add(m_jUp, cstr);

            m_jDown.setFocusPainted(false);
            m_jDown.setFocusable(false);
            m_jDown.setRequestFocusEnabled(false);
            m_jDown.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    m_jDownActionPerformed(evt);
                }
            });
            cstr = new GridBagConstraints();
            cstr.gridx = x;
            cstr.gridy = y++;
            cstr.insets = new Insets(0, 0, btnspacing, btnspacing);
            lineBtnsContainer.add(m_jDown, cstr);
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
        cstr.gridx = x;
        cstr.gridy = y++;
        cstr.insets = new Insets(0, 0, btnspacing, btnspacing);
        lineBtnsContainer.add(m_jDelete, cstr);

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
        cstr.gridx = x;
        cstr.gridy = y++;
        cstr.insets = new Insets(0, 0, btnspacing, btnspacing);
        lineBtnsContainer.add(m_jList, cstr);

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
        cstr.gridx = x;
        cstr.gridy = y++;
        cstr.insets = new Insets(0, 0, btnspacing, btnspacing);
        lineBtnsContainer.add(m_jEditLine, cstr);

        // Attributes
        jEditAttributes.setFocusPainted(false);
        jEditAttributes.setFocusable(false);
        jEditAttributes.setRequestFocusEnabled(false);
        jEditAttributes.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jEditAttributesActionPerformed();
            }
        });
        cstr = new GridBagConstraints();
        cstr.gridx = x;
        cstr.gridy = y++;
        cstr.insets = new Insets(0, 0, btnspacing, btnspacing);
        lineBtnsContainer.add(jEditAttributes, cstr);
        
        // Scales button (hidden if disabled, see init)
        m_jbtnScale.setFocusPainted(false);
        m_jbtnScale.setFocusable(false);
        m_jbtnScale.setRequestFocusEnabled(false);
        m_jbtnScale.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_jbtnScaleActionPerformed(evt);
            }
        });
        cstr = new GridBagConstraints();
        cstr.gridx = x;
        cstr.gridy = y++;
        cstr.insets = new Insets(0, 0, btnspacing, btnspacing);
        lineBtnsContainer.add(m_jbtnScale, cstr);
        
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
        cstr.gridx = x;
        cstr.gridy = y++;
        cstr.insets = new Insets(0, 0, btnspacing, btnspacing);
        lineBtnsContainer.add(m_jbtnLineDiscount, cstr);

        cstr = new GridBagConstraints();
        cstr.gridx = 1;
        cstr.gridy = 0;
        cstr.gridheight = 2;
        cstr.anchor = GridBagConstraints.NORTH;
        m_jInputContainer.add(lineBtnsContainer, cstr);

        m_jPanelCentral.setLayout(new java.awt.BorderLayout());

        m_jPanTotals.setLayout(new java.awt.GridBagLayout());

        m_jTotalEuros.setBackground(java.awt.Color.white);
        m_jTotalEuros.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        m_jTotalEuros.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createLineBorder(javax.swing.UIManager.getDefaults().getColor("Button.darkShadow")), javax.swing.BorderFactory.createEmptyBorder(1, 4, 1, 4)));
        m_jTotalEuros.setOpaque(true);
        m_jTotalEuros.setRequestFocusEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
        m_jPanTotals.add(m_jTotalEuros, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
        m_jPanTotals.add(m_jLblTotalEuros1, gridBagConstraints);

        m_jSubtotalEuros.setBackground(java.awt.Color.white);
        m_jSubtotalEuros.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        m_jSubtotalEuros.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createLineBorder(javax.swing.UIManager.getDefaults().getColor("Button.darkShadow")), javax.swing.BorderFactory.createEmptyBorder(1, 4, 1, 4)));
        m_jSubtotalEuros.setOpaque(true);
        m_jSubtotalEuros.setRequestFocusEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
        m_jPanTotals.add(m_jSubtotalEuros, gridBagConstraints);

        m_jTaxesEuros.setBackground(java.awt.Color.white);
        m_jTaxesEuros.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        m_jTaxesEuros.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createLineBorder(javax.swing.UIManager.getDefaults().getColor("Button.darkShadow")), javax.swing.BorderFactory.createEmptyBorder(1, 4, 1, 4)));
        m_jTaxesEuros.setOpaque(true);
        m_jTaxesEuros.setRequestFocusEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 5);
        m_jPanTotals.add(m_jTaxesEuros, gridBagConstraints);

        m_jLblTotalEuros2.setText(AppLocal.getIntString("label.taxcash")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        m_jPanTotals.add(m_jLblTotalEuros2, gridBagConstraints);

        m_jLblTotalEuros3.setText(AppLocal.getIntString("label.subtotalcash")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 0);
        m_jPanTotals.add(m_jLblTotalEuros3, gridBagConstraints);

        cstr = new GridBagConstraints();
        cstr.gridx = 0;
        cstr.gridy = 1;
        cstr.anchor = GridBagConstraints.EAST;
        m_jInputContainer.add(m_jPanTotals, cstr);

        cstr = new GridBagConstraints();
        cstr.gridx = 0;
        cstr.gridy = 0;
        cstr.weighty = 1.0;
        cstr.weightx = 1.0;
        cstr.fill = GridBagConstraints.HORIZONTAL;
        m_jInputContainer.add(m_jPanelCentral, cstr);

        m_jPanEntries.setLayout(new javax.swing.BoxLayout(m_jPanEntries, javax.swing.BoxLayout.Y_AXIS));

        m_jNumberKeys.addJNumberEventListener(new JNumberEventListener() {
            public void keyPerformed(JNumberEvent evt) {
                m_jNumberKeysKeyPerformed(evt);
            }
        });
        m_jPanEntries.add(m_jNumberKeys);

        // jPanel9: barcode entry line under keyboard
        jPanel9.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        jPanel9.setLayout(new java.awt.GridBagLayout());

        m_jPrice.setBackground(java.awt.Color.white);
        m_jPrice.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        m_jPrice.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createLineBorder(javax.swing.UIManager.getDefaults().getColor("Button.darkShadow")), javax.swing.BorderFactory.createEmptyBorder(1, 4, 1, 4)));
        m_jPrice.setOpaque(true);
        m_jPrice.setPreferredSize(new java.awt.Dimension(100, 22));
        m_jPrice.setRequestFocusEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        jPanel9.add(m_jPrice, gridBagConstraints);

        m_jPor.setBackground(java.awt.Color.white);
        m_jPor.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        m_jPor.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createLineBorder(javax.swing.UIManager.getDefaults().getColor("Button.darkShadow")), javax.swing.BorderFactory.createEmptyBorder(1, 4, 1, 4)));
        m_jPor.setOpaque(true);
        m_jPor.setPreferredSize(new java.awt.Dimension(22, 22));
        m_jPor.setRequestFocusEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        jPanel9.add(m_jPor, gridBagConstraints);

        m_jEnter.setFocusPainted(false);
        m_jEnter.setFocusable(false);
        m_jEnter.setRequestFocusEnabled(false);
        m_jEnter.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_jEnterActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        jPanel9.add(m_jEnter, gridBagConstraints);

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
        m_jPanContainer.add(m_jInputContainer, cstr);
        // Last line: catalog selector
        catcontainer.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        catcontainer.setLayout(new java.awt.BorderLayout());
        
        cstr = new GridBagConstraints();
        cstr.gridx = 0;
        cstr.gridy = 2;
        cstr.weightx = 1.0;
        cstr.weighty = 1.0;
        cstr.fill = GridBagConstraints.BOTH;
        m_jPanContainer.add(catcontainer, cstr);
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
        if (index >= 0 && m_oTicket.getLine(index).getPrice() > 0.0) {
            TicketLineInfo line = m_oTicket.getLine(index);
            
            double discount = - line.getPrice() * discountRate;
            // Round discount to 0.01
            long cents = Math.round(discount * 100.0f);
            discount = ((double)cents) / 100.0;
            if (discountRate > 0.005) {

                this.addDiscountLine(line, discountRate);
                
            } else {
                 // No rate
                 MessageInf msg = new MessageInf(MessageInf.SGN_WARNING, AppLocal.getIntString("message.selectratefordiscount"));
                 msg.show(this);
                 java.awt.Toolkit.getDefaultToolkit().beep();
            }
        } else {
            // No item or discount selected
            MessageInf msg = new MessageInf(MessageInf.SGN_WARNING, AppLocal.getIntString("message.selectlinefordiscount"));
            msg.show(this);
            java.awt.Toolkit.getDefaultToolkit().beep();
        }
    }

    private void m_jEnterActionPerformed(java.awt.event.ActionEvent evt) {

        automator('\n');

    }

    private void m_jNumberKeysKeyPerformed(JNumberEvent evt) {

        automator(evt.getKey());

    }

    private void m_jKeyFactoryKeyPressed(java.awt.event.KeyEvent evt) {
        // Adding shortcuts for product's selection in panel sales screen
        if (evt.getKeyCode() == evt.VK_UP || evt.getKeyCode() == evt.VK_PAGE_UP) {
                m_ticketlines.selectionUp();
            } else if (evt.getKeyCode() == evt.VK_DOWN || evt.getKeyCode() == evt.VK_PAGE_DOWN) {
                m_ticketlines.selectionDown();
            } else {
                m_jKeyFactory.setText(null);
                //stateTransition(evt.getKeyChar());
                automator(evt.getKeyChar());
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
        
        try {
            m_oTicket.setCustomer(finder.getSelectedCustomer() == null
                    ? null
                    : dlSales.loadCustomerExt(finder.getSelectedCustomer().getId()));
        } catch (BasicException e) {
            MessageInf msg = new MessageInf(MessageInf.SGN_WARNING, AppLocal.getIntString("message.cannotfindcustomer"), e);
            msg.show(this);            
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

    private void jEditAttributesActionPerformed() {

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
            } catch (BasicException ex) {
                MessageInf msg = new MessageInf(MessageInf.SGN_WARNING, AppLocal.getIntString("message.cannotfindattributes"), ex);
                msg.show(this);
            }
        }
    }

    private javax.swing.JButton btnCustomer;
    private javax.swing.JButton btnSplit;
    private javax.swing.JPanel catcontainer;
    private javax.swing.JButton jEditAttributes;
    private javax.swing.JPanel lineBtnsContainer;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JPanel m_jButtonsExt;
    private javax.swing.JButton m_jDelete;
    private javax.swing.JButton m_jDown;
    private javax.swing.JButton m_jEditLine;
    private javax.swing.JButton m_jEnter;
    private javax.swing.JTextField m_jKeyFactory;
    private javax.swing.JLabel m_jLblTotalEuros1;
    private javax.swing.JLabel m_jLblTotalEuros2;
    private javax.swing.JLabel m_jLblTotalEuros3;
    private javax.swing.JButton m_jList;
    private JNumberKeys m_jNumberKeys;
    private javax.swing.JPanel m_jPanEntries;
    private javax.swing.JPanel m_jPanTotals;
    private javax.swing.JPanel m_jPanelBag;
    private javax.swing.JPanel m_jPanelCentral;
    private javax.swing.JLabel m_jPor;
    private javax.swing.JLabel m_jPrice;
    private javax.swing.JLabel m_jSubtotalEuros;
    private javax.swing.JComboBox m_jTax;
    private javax.swing.JLabel m_jTaxesEuros;
    private javax.swing.JLabel m_jTicketId;
    private javax.swing.JLabel m_jTotalEuros;
    private javax.swing.JButton m_jUp;
    private javax.swing.JToggleButton m_jaddtax;
    private javax.swing.JButton m_jbtnScale;
    private javax.swing.JButton m_jbtnLineDiscount;
    private javax.swing.JPanel m_jInputContainer;

}
