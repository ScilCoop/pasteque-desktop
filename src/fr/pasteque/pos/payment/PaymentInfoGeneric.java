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

import fr.pasteque.pos.admin.CurrencyInfo;

/** Generic payment model */
public class PaymentInfoGeneric extends PaymentInfo {
    
    private double m_dTotal;
    private String code;
   
    /** Creates a new instance of PaymentInfoFree */
    public PaymentInfoGeneric(String code, double dTotal,
            CurrencyInfo currency) {
        this.code = code;
        m_dTotal = dTotal;
        this.currency = currency;
    }
    
    public PaymentInfo copyPayment(){
        return new PaymentInfoGeneric(this.code, m_dTotal, this.currency);
    }    
    public String getName() {
        return this.code;
    }   
    public double getTotal() {
        return m_dTotal;
    }
    public String getTransactionID(){
        return "no ID";
    }
}
