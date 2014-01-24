//    Openbravo POS is a point of sales application designed for touch screens.
//    Copyright (C) 2007-2009 Openbravo, S.L.
//    http://www.openbravo.com/product/pos
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

package fr.pasteque.pos.panels;

import java.util.*;
import javax.swing.table.AbstractTableModel;
import fr.pasteque.basic.BasicException;
import fr.pasteque.data.loader.*;
import fr.pasteque.format.Formats;
import fr.pasteque.pos.admin.CurrencyInfo;
import fr.pasteque.pos.forms.DataLogicSales;
import fr.pasteque.pos.forms.AppLocal;
import fr.pasteque.pos.forms.AppView;
import fr.pasteque.pos.ticket.CashSession;
import fr.pasteque.pos.ticket.CategoryInfo;
import fr.pasteque.pos.ticket.TaxInfo;
import fr.pasteque.pos.ticket.ZTicket;
import fr.pasteque.pos.util.StringUtils;

/**
 *
 * @author adrianromero
 */
public class PaymentsModel {

    private CashSession cashSession;
    private Integer m_iPayments;
    private Double m_dPaymentsTotal;
    private java.util.List<PaymentsLine> m_lpayments;
    private List<CategoryLine> catSales;
    private static List<CurrencyInfo> currencies;

    private final static String[] PAYMENTHEADERS = {"Label.Payment", "label.totalcash"};
    
    private Integer m_iSales;
    private Double m_dSalesBase;
    private Double m_dSalesTaxes;
    private java.util.List<SalesLine> m_lsales;
    private Integer custCount;
    private Double expectedCash;
    
    private final static String[] SALEHEADERS = {"label.taxcash", "label.subtotalcash", "label.totalcash"};
    private final static String[] CATEGORYHEADERS = {"label.catname", "label.totalcash"};

    private PaymentsModel() {
    }    
        
    public static PaymentsModel loadInstance(AppView app) throws BasicException {
        DataLogicSales dlSales = (DataLogicSales) app.getBean("fr.pasteque.pos.forms.DataLogicSales");
        
        PaymentsModel p = new PaymentsModel();
        currencies = dlSales.getCurrenciesList();
        
        // Cash session info
        CashSession cash = app.getActiveCashSession();
        p.cashSession = cash;
        // Load z ticket
        ZTicket z = dlSales.getZTicket(cash.getId());
        
        // Get number of payments and total amount
        p.m_iPayments = z.getPaymentCount();
        p.m_dPaymentsTotal = 0.0;
        p.m_lpayments = new ArrayList<PaymentsLine>();
        for (ZTicket.Payment payment : z.getPayments()) {
            p.m_dPaymentsTotal += payment.getAmount();
            CurrencyInfo curr = dlSales.getCurrency(payment.getCurrencyId());
            PaymentsLine l = new PaymentsLine(payment.getType(),
                    curr, payment.getCurrencyAmount());
            p.m_lpayments.add(l);
        }
        
        // Sales
        p.m_iSales = z.getTicketCount();
        p.m_dSalesBase = z.getConsolidatedSales();
        p.custCount = z.getCustomersCount();

        // Sales by categories
        p.catSales = new ArrayList<CategoryLine>();
        for (ZTicket.Category cat : z.getCategories()) {
            String catId = cat.getId();
            CategoryInfo catInfo = dlSales.getCategory(catId);
            String name = catInfo.getName();
            CategoryLine l = new CategoryLine(name, cat.getAmount());
            p.catSales.add(l);
        }
        
        // Taxes amount
        p.m_lsales = new ArrayList<SalesLine>();
        p.m_dSalesTaxes = 0.0;
        List<TaxInfo> taxes = dlSales.getTaxList();
        for (ZTicket.Tax tax : z.getTaxes()) {
            p.m_dSalesTaxes += tax.getAmount();
            String taxId = tax.getId();
            String name = null;
            double rate = 0.0;
            for (TaxInfo t : taxes) {
                if (t.getId().equals(taxId)) {
                    name = t.getName();
                    rate = t.getRate();
                    break;
                }
            }
            SalesLine l = new SalesLine(name, rate, tax.getBase(),
                    tax.getAmount());
            p.m_lsales.add(l);
        }

        // Count expected cash
        if (p.hasFunds()) {
            double expectedTotal = 0.0;
            // Get initial fund
            if (p.cashSession.getOpenCash() != null) {
                expectedTotal = p.cashSession.getOpenCash();
            }
            // Add cash payments
            for (PaymentsModel.PaymentsLine line : p.getPaymentLines()) {
                if (line.getType().equals("cash")
                        && line.getCurrency().isMain()) {
                    expectedTotal += line.getValue();
                }
            }
            p.expectedCash = expectedTotal;
        }

        return p;
    }

    public int getPayments() {
        return m_iPayments.intValue();
    }
    public boolean hasCustomersCount() {
        return custCount != null;
    }
    public int getCustomersCount() {
        return custCount;
    }
    public double getTotal() {
        return m_dPaymentsTotal.doubleValue();
    }
    public String getHost() {
        return this.cashSession.getHost();
    }
    public int getSequence() {
        return this.cashSession.getSequence();
    }
    public Date getDateStart() {
        return this.cashSession.getOpenDate();
    }
    public void setDateEnd(Date dValue) {
        this.cashSession.close(dValue);
    }
    public Date getDateEnd() {
        return this.cashSession.getCloseDate();
    }
    public Double getOpenCash() {
        return this.cashSession.getOpenCash();
    }
    public Double getCloseCash() {
        return this.cashSession.getCloseCash();
    }
    /** Check if cash was counted at open and/or close */
    public boolean hasFunds() {
        return this.cashSession.getOpenCash() != null
                || this.cashSession.getCloseCash() != null;
    }
    public Double getExpectedCash() {
        return this.expectedCash;
    }

    public String printHost() {
        return StringUtils.encodeXML(this.cashSession.getHost());
    }
    public String printSequence() {
        return Formats.INT.formatValue(this.cashSession.getSequence());
    }
    public String printDateStart() {
        return Formats.TIMESTAMP.formatValue(this.cashSession.getOpenDate());
    }
    public String printDateEnd() {
        return Formats.TIMESTAMP.formatValue(this.cashSession.getCloseDate());
    }  
    public String printOpenCash() {
        if (this.cashSession.getOpenCash() != null) {
            return Formats.CURRENCY.formatValue(this.cashSession.getOpenCash());
        } else {
            return "";
        }
    }
    public String printCloseCash() {
        if (this.cashSession.getCloseCash() != null) {
            return Formats.CURRENCY.formatValue(this.cashSession.getCloseCash());
        } else {
            return "";
        }
    }
    public String printExpectedCash() {
        if (this.expectedCash != null) {
            return Formats.CURRENCY.formatValue(this.expectedCash);
        } else {
            return "";
        }
    }

    public String printPayments() {
        return Formats.INT.formatValue(m_iPayments);
    }

    public String printPaymentsTotal() {
        return Formats.CURRENCY.formatValue(m_dPaymentsTotal);
    }     
    
    public List<PaymentsLine> getPaymentLines() {
        return m_lpayments;
    }
    
    public int getSales() {
        return m_iSales == null ? 0 : m_iSales.intValue();
    }
    /** Prints the number of tickets */
    public String printSales() {
        return Formats.INT.formatValue(m_iSales);
    }
    public String printCustomersCount() {
        return Formats.INT.formatValue(custCount);
    }
    /** Prints the subtotal */
    public String printSalesBase() {
        return Formats.CURRENCY.formatValue(m_dSalesBase);
    }
    /** Print taxes total */
    public String printSalesTaxes() {
        return Formats.CURRENCY.formatValue(m_dSalesTaxes);
    }
    /** Print total */
    public String printSalesTotal() {            
        return Formats.CURRENCY.formatValue((m_dSalesBase == null || m_dSalesTaxes == null)
                ? null
                : m_dSalesBase + m_dSalesTaxes);
    }
    /** Get average sales per customer */
    public String printSalesPerCustomer() {
        if (custCount != 0) {
            return Formats.CURRENCY.formatValue((m_dSalesBase + m_dSalesTaxes) / custCount);
        } else {
            return "";
        }
    }
    public List<SalesLine> getSaleLines() {
        return m_lsales;
    }
    public List<CategoryLine> getCategoryLines() {
        return this.catSales;
    }

    public AbstractTableModel getPaymentsModel() {
        return new AbstractTableModel() {
            public String getColumnName(int column) {
                return AppLocal.getIntString(PAYMENTHEADERS[column]);
            }
            public int getRowCount() {
                return m_lpayments.size();
            }
            public int getColumnCount() {
                return PAYMENTHEADERS.length;
            }
            public Object getValueAt(int row, int column) {
                PaymentsLine l = m_lpayments.get(row);
                switch (column) {
                case 0: return new Object[] {l.getType(), l.getCurrency().getName(), l.getCurrency().isMain()};
                case 1: return l;
                default: return null;
                }
            }  
        };
    }
    
    public static class SalesLine {
        
        private String m_SalesTaxName;
        private Double taxRate;
        private Double taxBase;
        private Double m_SalesTaxes;

        public SalesLine(String taxName, double rate, double base,
                double amount) {
            this.m_SalesTaxName = taxName;
            this.taxRate = rate;
            this.taxBase = taxBase;
            this.m_SalesTaxes = amount;
        }
        public String printTaxName() {
            return m_SalesTaxName;
        }
        public String printTaxRate() {
        	return Formats.PERCENT.formatValue(this.taxRate);
        }
        public String printTaxes() {
            return Formats.CURRENCY.formatValue(m_SalesTaxes);
        }
        public String printTaxBase() {
            return Formats.CURRENCY.formatValue(this.taxBase);
        }
        public String getTaxName() {
            return m_SalesTaxName;
        }
        public Double getTaxRate() {
            return this.taxRate;
        }
        public Double getTaxes() {
            return m_SalesTaxes;
        }
        public Double getTaxBase() {
            return this.taxBase;
        }
    }

    public AbstractTableModel getSalesModel() {
        return new AbstractTableModel() {
            public String getColumnName(int column) {
                return AppLocal.getIntString(SALEHEADERS[column]);
            }
            public int getRowCount() {
                return m_lsales.size();
            }
            public int getColumnCount() {
                return SALEHEADERS.length;
            }
            public Object getValueAt(int row, int column) {
                SalesLine l = m_lsales.get(row);
                switch (column) {
                case 0: return l.getTaxName();
                case 1: return l.getTaxBase();
                case 2: return l.getTaxes();
                default: return null;
                }
            }  
        };
    }
    
    public static class PaymentsLine {
        
        private String m_PaymentType;
        private CurrencyInfo currency;
        private Double m_PaymentValue;
 
        public PaymentsLine(String type, CurrencyInfo currency, double value) {
            this.m_PaymentType = type;
            this.currency = currency;
            this.m_PaymentValue = value;
        }
        
        public String printType() {
            return AppLocal.getIntString("transpayment." + m_PaymentType);
        }
        public String getType() {
            return m_PaymentType;
        }
        public String printValue() {
            Formats.setAltCurrency(this.currency);
            return Formats.CURRENCY.formatValue(m_PaymentValue);
        }
        public Double getValue() {
            return m_PaymentValue;
        }
        public CurrencyInfo getCurrency() {
            return this.currency;
        }
    }

    public AbstractTableModel getCategoriesModel() {
        return new AbstractTableModel() {
            public String getColumnName(int column) {
                return AppLocal.getIntString(CATEGORYHEADERS[column]);
            }
            public int getRowCount() {
                return catSales.size();
            }
            public int getColumnCount() {
                return CATEGORYHEADERS.length;
            }
            public Object getValueAt(int row, int column) {
                CategoryLine l = catSales.get(row);
                switch (column) {
                case 0: return l.getCategory();
                case 1: return l.getValue();
                default: return null;
                }
            }  
        };
    }

    public static class CategoryLine {
        private String category;
        private Double amount;

        public CategoryLine(String category, double amount) {
            this.category = category;
            this.amount = amount;
        }
        public String getCategory() {
            return this.category;
        }
        public String printCategory() {
            return this.category;
        }
        public String printValue() {
            return Formats.CURRENCY.formatValue(this.amount);
        }
        public Double getValue() {
            return this.amount;
        }
    }
}
