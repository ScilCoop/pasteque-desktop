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

import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

public class ZTicket {

    private String cashSessionId;
    private int ticketCount;
    private Integer custCount;
    private int paymentCount;
    private Double cs;
    private List<ZTicket.Payment> payments;
    private List<ZTicket.Tax> taxes;
    private List<ZTicket.Category> catSales;

    public ZTicket(String cashSessionId) {
        this.cashSessionId = cashSessionId;
        this.payments = new ArrayList<ZTicket.Payment>();
        this.taxes = new ArrayList<ZTicket.Tax>();
        this.catSales = new ArrayList<ZTicket.Category>();
    }

    public ZTicket(JSONObject o) {
        this.cashSessionId = o.getString("cashId");
        this.ticketCount = o.getInt("ticketCount");
        if (!o.isNull("custCount")) {
            this.custCount = o.getInt("custCount");
        }
        this.cs = o.getDouble("cs");
        this.paymentCount = o.getInt("paymentCount");
        this.payments = new ArrayList<ZTicket.Payment>();
        this.taxes = new ArrayList<ZTicket.Tax>();
        this.catSales = new ArrayList<ZTicket.Category>();
        JSONArray aPayments = o.getJSONArray("payments");
        for (int i = 0; i < aPayments.length(); i++) {
            JSONObject oPayment = aPayments.getJSONObject(i);
            ZTicket.Payment p = new ZTicket.Payment(oPayment.getString("type"),
                    oPayment.getDouble("amount"), oPayment.getInt("currencyId"),
                    oPayment.getDouble("currencyAmount"));
            this.payments.add(p);
        }
        JSONArray aTaxes = o.getJSONArray("taxes");
        for (int i = 0; i < aTaxes.length(); i++) {
            JSONObject oTax = aTaxes.getJSONObject(i);
            ZTicket.Tax t = new ZTicket.Tax(oTax.getString("id"),
                    oTax.getDouble("base"), oTax.getDouble("amount"));
            this.taxes.add(t);
        }
        JSONArray aCat = o.getJSONArray("catSales");
        for (int i = 0; i < aCat.length(); i++) {
            JSONObject oCat = aCat.getJSONObject(i);
            ZTicket.Category c = new ZTicket.Category(oCat.getString("id"),
                    oCat.getDouble("amount"));
            this.catSales.add(c);
        }
    }

    public String getCashSessionId() {
        return this.cashSessionId;
    }

    public Double getConsolidatedSales() {
        return this.cs;
    }

    public Integer getCustomersCount() {
        return this.custCount;
    }

    public int getTicketCount() {
        return this.ticketCount;
    }

    public int getPaymentCount() {
        return this.paymentCount;
    }

    public List<ZTicket.Payment> getPayments() {
        return this.payments;
    }

    public List<ZTicket.Tax> getTaxes() {
        return this.taxes;
    }

    public List<ZTicket.Category> getCategories() {
        return this.catSales;
    }

    public class Payment {
        private String type;
        private double amount;
        private int currencyId;
        private double currencyAmount;

        public Payment(String type, double amount, int currencyId,
                double currencyAmount) {
            this.type = type;
            this.amount = amount;
            this.currencyId = currencyId;
            this.currencyAmount = currencyAmount;
        }
        public String getType() {
            return this.type;
        }
        public double getAmount() {
            return this.amount;
        }
        public int getCurrencyId() {
            return this.currencyId;
        }
        public double getCurrencyAmount() {
            return this.currencyAmount;
        }
    }

    public class Tax {
        private String taxId;
        private double base;
        private double amount;

        public Tax(String id, double base, double amount) {
            this.taxId = id;
            this.base = base;
            this.amount = amount;
        }
        public String getId() {
            return this.taxId;
        }
        public double getBase() {
            return this.base;
        }
        public double getAmount() {
            return this.amount;
        }
    }

    public class Category {
        private String catId;
        private double amount;

        public Category(String id, double amount) {
            this.catId = id;
            this.amount = amount;
        }
        public String getId() {
            return this.catId;
        }
        public double getAmount() {
            return this.amount;
        }
    }
}