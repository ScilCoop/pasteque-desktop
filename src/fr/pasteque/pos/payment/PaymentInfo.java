//    Openbravo POS is a point of sales application designed for touch screens.
//    Copyright (C) 2008-2009 Openbravo, S.L.
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

package fr.pasteque.pos.payment;

import fr.pasteque.basic.BasicException;
import fr.pasteque.format.Formats;
import fr.pasteque.pos.admin.CurrencyInfo;
import fr.pasteque.pos.forms.DataLogicSales;
import org.json.JSONObject;

/** Generic PaymentInfo as stored in database.
 * Use subclasses when creating a new PaymentInfo.
 */
public abstract class PaymentInfo {

    protected CurrencyInfo currency;
    
    public abstract String getName();
    /** Get total of paiement in payment currency. */
    public abstract double getTotal();
    public CurrencyInfo getCurrency() {
        return this.currency;
    }
    public abstract PaymentInfo copyPayment();
    public abstract String getTransactionID();

    public JSONObject toJSON() {
        JSONObject o = new JSONObject();
        o.put("type", this.getName());
        o.put("amount", this.currency.convertToMain(this.getTotal()));
        o.put("currencyId", this.currency.getID());
        o.put("currencyAmount", this.getTotal());
        return o;
    }

    public static PaymentInfo readJSON(JSONObject o) throws BasicException {
        String type = o.getString("type");
        double amount = o.getDouble("amount");
        int currencyId = o.getInt("currencyId");
        double currencyAmount = o.getDouble("currencyAmount");
        DataLogicSales dlSales = new DataLogicSales();
        return new PaymentInfoGeneric(type, currencyAmount,
                dlSales.getCurrency(currencyId));
    }

    public String printTotal() {
        Formats.setAltCurrency(this.currency);
        return Formats.CURRENCY.formatValue(new Double(getTotal()));
    }

}
