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

import fr.pasteque.format.Formats;
import fr.pasteque.pos.admin.CurrencyInfo;

import java.io.Serializable;

public class PaymentInfoCash extends PaymentInfo implements Serializable {
    
    private double m_dPaid;
    private double m_dTotal;
    
    /** Creates a new instance of PaymentInfoCash */
    public PaymentInfoCash(double dTotal, double dPaid, CurrencyInfo currency) {
        m_dTotal = dTotal;
        m_dPaid = dPaid;
        this.currency = currency;
    }
    
    public PaymentInfo copyPayment(){
        return new PaymentInfoCash(m_dTotal, m_dPaid, this.currency);
    }
    
    public String getName() {
        return "cash";
    }   
    public double getTotal() {
        return m_dTotal;
    }   
    public double getPaid() {
        return m_dPaid;
    }
    public String getTransactionID(){
        return "no ID";
    }
    
    public String printPaid() {
        Formats.setAltCurrency(this.currency);
        return Formats.CURRENCY.formatValue(new Double(m_dPaid));
    }   
    public String printChange() {
        double change = m_dPaid - m_dTotal;
        if (this.currency != null && !this.currency.isMain()) {
            change /= this.currency.getRate();
        }
        return Formats.CURRENCY.formatValue(new Double(change));
    }    
}
