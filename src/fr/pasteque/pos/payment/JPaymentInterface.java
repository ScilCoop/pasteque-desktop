//    Pastèque is based uppon Openbravo POS
//    Openbravo POS is a point of sales application designed for touch screens.
//    Copyright (C) 2007-2009 Openbravo, S.L.
//                  2015 Scil
//    Philippe Pary
//
//    This file is part of Pastèque
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
import fr.pasteque.pos.customers.CustomerInfoExt;
import javax.swing.JPanel;

/**
 *
 * @author Adrian
 */
public interface JPaymentInterface {

    public void activate(CustomerInfoExt customerext, double dTotal,
            double partAmount, CurrencyInfo currency, String transactionID);
    public PaymentInfo executePayment();
    public JPanel getComponent();
    public JPanel getPanel();
}
