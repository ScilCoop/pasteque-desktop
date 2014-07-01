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
package fr.pasteque.pos.ticket;

import fr.pasteque.basic.BasicException;
import fr.pasteque.pos.admin.CurrencyInfo;
import fr.pasteque.pos.forms.DataLogicSales;
import fr.pasteque.pos.payment.PaymentInfoGeneric;

import java.io.Serializable;
import java.util.Date;
import org.json.JSONObject;

public class CashMove implements Serializable {

    public static final String CASH_MOVE_OUT = "cashout";
    public static final String CASH_MOVE_IN = "cashin";

    private String cashId;
    private Date date;
    private PaymentInfoGeneric payment;
    private String note;

    public CashMove(String cashId, String reason, double amount, String note) {
        this.cashId = cashId;
        this.date = new Date();
        this.note = note;
        DataLogicSales dlSales = new DataLogicSales();
        CurrencyInfo currency = null;
        try {
            currency = dlSales.getMainCurrency();
        } catch (BasicException e) {
            e.printStackTrace();
        }
        this.payment = new PaymentInfoGeneric(reason, amount, currency);
    }

    public String getCashId() {
        return this.cashId;
    }

    public Date getDate() {
        return this.date;
    }

    public String getNote() {
        return this.note;
    }

    public PaymentInfoGeneric getPayment() {
        return this.payment;
    }
}
