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

package fr.pasteque.pos.ticket;

import java.io.*;
import fr.pasteque.pos.util.StringUtils;
import fr.pasteque.format.Formats;
import fr.pasteque.basic.BasicException;
import fr.pasteque.pos.forms.AppLocal;
import fr.pasteque.pos.forms.DataLogicSales;
import java.util.Properties;
import org.json.JSONObject;

/**
 *
 * @author adrianromero
 */
public class TicketLineInfo implements Serializable {

    private static final long serialVersionUID = 6608012948284450199L;
    private String m_sTicket;
    private int m_iLine;
    private double multiply;
    private double price;
    private double discountRate;
    private TaxInfo tax;
    private Properties attributes;
    private String productid;
    private String attsetinstid;
    private boolean subproduct;

    public TicketLineInfo(String productname, String producttaxcategory, double dMultiply, double dPrice, TaxInfo tax) {

        Properties props = new Properties();
        props.setProperty("product.name", productname);
        props.setProperty("product.taxcategoryid", producttaxcategory);
        init(null, null, dMultiply, dPrice, tax, props);
    }

    private TicketLineInfo() {
        init(null, null, 0.0, 0.0, null, new Properties());
    }

    public TicketLineInfo(ProductInfoExt product, double dMultiply, double dPrice, TaxInfo tax, Properties attributes) {

        String pid;

        if (product == null) {
            pid = null;
        } else {
            pid = product.getID();
            attributes.setProperty("product.name", product.getName());
            attributes.setProperty("product.com", product.isCom() ? "true" : "false");
            attributes.setProperty("product.scale", product.isScale() ? "true" : "false");
            if (product.getAttributeSetID() != null) {
                attributes.setProperty("product.attsetid", product.getAttributeSetID());
            }
            attributes.setProperty("product.taxcategoryid", product.getTaxCategoryID());
            if (product.getCategoryID() != null) {
                attributes.setProperty("product.categoryid", product.getCategoryID());
            }
        }
        init(pid, null, dMultiply, dPrice, tax, attributes);
    }

    public TicketLineInfo(TicketLineInfo line) {
        init(line.productid, line.attsetinstid, line.multiply, line.price, line.tax, (Properties) line.attributes.clone());
        this.subproduct = line.isSubproduct();
        this.discountRate = line.discountRate;
    }

    private void init(String productid, String attsetinstid, double dMultiply, double dPrice, TaxInfo tax, Properties attributes) {

        this.productid = productid;
        this.attsetinstid = attsetinstid;
        multiply = dMultiply;
        price = dPrice;
        this.tax = tax;
        this.attributes = attributes;

        m_sTicket = null;
        m_iLine = -1;

        this.subproduct = false;
    }

    void setTicket(String ticket, int line) {
        m_sTicket = ticket;
        m_iLine = line;
    }

    public JSONObject toJSON() {
        JSONObject o = new JSONObject();
        o.put("dispOrder", this.m_iLine);
        o.put("productId", this.productid);
        o.put("attributes", JSONObject.NULL); // TODO: add attributes
        o.put("quantity", this.multiply);
        o.put("price", this.price);
        o.put("taxId", this.tax.getId());
        o.put("discountRate", this.discountRate);
        return o;
    }

    public TicketLineInfo(JSONObject o) throws BasicException {
        this.m_iLine = o.getInt("dispOrder");
        this.productid = o.getString("productId");
        DataLogicSales dlSales = new DataLogicSales();
        ProductInfoExt product = dlSales.getProductInfo(this.productid);
        this.attributes = new Properties();
        if (product != null) {
            attributes.setProperty("product.name", product.getName());
            attributes.setProperty("product.com",
                    product.isCom() ? "true" : "false");
            attributes.setProperty("product.scale",
                    product.isScale() ? "true" : "false");
            // TODO: attributes
            attributes.setProperty("product.taxcategoryid",
                    product.getTaxCategoryID());
            if (product.getCategoryID() != null) {
                attributes.setProperty("product.categoryid",
                        product.getCategoryID());
            }
        }
        this.multiply = o.getDouble("quantity");
        this.price = o.getDouble("price");
        this.tax = dlSales.getTax(o.getString("taxId"));
        this.discountRate = o.getDouble("discountRate");
    }

    public TicketLineInfo copyTicketLine() {
        TicketLineInfo l = new TicketLineInfo();
        // l.m_sTicket = null;
        // l.m_iLine = -1;
        l.productid = productid;
        l.attsetinstid = attsetinstid;
        l.multiply = multiply;
        l.price = price;
        l.tax = tax;
        l.discountRate = this.discountRate;
        l.attributes = (Properties) attributes.clone();
        l.subproduct = this.subproduct;
        return l;
    }

    public int getTicketLine() {
        return m_iLine;
    }

    public String getProductID() {
        return productid;
    }
    
    public String getProductName() {
        return attributes.getProperty("product.name");
    }

    public String getProductAttSetId() {
        return attributes.getProperty("product.attsetid");
    }

    public String getProductAttSetInstDesc() {
        return attributes.getProperty("product.attsetdesc", "");
    }

    public void setProductAttSetInstDesc(String value) {
        if (value == null) {
            attributes.remove(value);
        } else {
            attributes.setProperty("product.attsetdesc", value);
        }
    }

    public String getProductAttSetInstId() {
        return attsetinstid;
    }

    public void setProductAttSetInstId(String value) {
        attsetinstid = value;
    }

    public boolean isProductCom() {
        return "true".equals(attributes.getProperty("product.com"));
    }

    public boolean isProductScale() {
        return "true".equals(attributes.getProperty("product.scale"));
    }

    public String getProductTaxCategoryID() {
        return (attributes.getProperty("product.taxcategoryid"));
    }

    public String getProductCategoryID() {
        return (attributes.getProperty("product.categoryid"));
    }

    public double getMultiply() {
        return multiply;
    }

    public void setMultiply(double dValue) {
        multiply = dValue;
    }

    public double getDiscountRate() {
        return this.discountRate;
    }
    public void setDiscountRate(double rate) {
        this.discountRate = rate;
    }
    public boolean hasDiscount() {
        return this.discountRate > 0.0;
    }

    /** Get price without discount */
    public double getFullPrice() {
        return this.price;
    }
    /** Get price with discount */
    public double getPrice() {
        return this.price * (1.0 - this.discountRate);
    }

    public void setPrice(double dValue) {
        price = dValue;
    }

    public double getFullPriceTax() {
        return price * (1.0 + getTaxRate());
    }
    public double getPriceTax() {
        return price * (1.0 - this.discountRate) * (1.0 + getTaxRate());
    }

    public void setPriceTax(double dValue) {
        price = dValue / (1.0 + getTaxRate());
    }

    public TaxInfo getTaxInfo() {
        return tax;
    }

    public void setTaxInfo(TaxInfo value) {
        tax = value;
    }

    public String getProperty(String key) {
        return attributes.getProperty(key);
    }

    public String getProperty(String key, String defaultvalue) {
        return attributes.getProperty(key, defaultvalue);
    }

    public void setProperty(String key, String value) {
        attributes.setProperty(key, value);
    }

    public Properties getProperties() {
        return attributes;
    }

    public double getTaxRate() {
        return tax == null ? 0.0 : tax.getRate();
    }

    /** Get price with quantity and discount */
    public double getSubValue() {
        return price * (1.0 - this.discountRate) * multiply;
    }
    /** Get price with quantity (without discount) */
    public double getFullSubValue() {
        return this.price * this.multiply;
    }
    public double getFullTax() {
        return price * multiply * getTaxRate();
    }
    /** Get tax amount with discount */
    public double getTax() {
        return price * (1.0 - this.discountRate) * multiply * getTaxRate();
    }
    /** Get price with quantity, taxes and discount */
    public double getValue() {
        return price * (1.0 - this.discountRate) * multiply * (1.0 + getTaxRate());
    }
    /** Get price with quantity and taxes (without discount) */
    public double getFullValue() {
        return this.price * this.multiply * (1.0 + this.getTaxRate());
    }

    public boolean isSubproduct() {
        return this.subproduct;
    }
    public void setSubproduct(boolean subproduct) {
        this.subproduct = subproduct;
    }

    public String printName() {
        return StringUtils.encodeXML(attributes.getProperty("product.name"));
    }

    public String printMultiply() {
        return Formats.DOUBLE.formatValue(multiply);
    }

    public String printFullPrice() {
        return Formats.CURRENCY.formatValue(this.getFullPrice());
    }
    public String printPrice() {
        return Formats.CURRENCY.formatValue(getPrice());
    }

    public String printFullPriceTax() {
        return Formats.CURRENCY.formatValue(this.getFullPriceTax());
    }
    public String printPriceTax() {
        return Formats.CURRENCY.formatValue(getPriceTax());
    }

    public String printFullTax() {
        return Formats.CURRENCY.formatValue(this.getFullTax());
    }
    public String printTax() {
        return Formats.CURRENCY.formatValue(getTax());
    }

    public String printTaxRate() {
        return Formats.PERCENT.formatValue(getTaxRate());
    }

    public String printFullSubValue() {
        return Formats.CURRENCY.formatValue(this.getFullSubValue());
    }
    public String printSubValue() {
        return Formats.CURRENCY.formatValue(getSubValue());
    }

    public String printFullValue() {
        return Formats.CURRENCY.formatValue(this.getFullValue());
    }
    public String printValue() {
        return Formats.CURRENCY.formatValue(getValue());
    }

    public String printDiscountRate() {
        return Formats.PERCENT.formatValue(this.discountRate);
    }
}
