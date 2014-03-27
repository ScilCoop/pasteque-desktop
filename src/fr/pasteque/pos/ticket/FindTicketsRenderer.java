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

package fr.pasteque.pos.ticket;

import fr.pasteque.data.loader.ImageLoader;
import fr.pasteque.format.Formats;

import java.awt.Component;
import java.util.Date;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JList;

/**
 *
 * @author Mikel Irurita
 */
public class FindTicketsRenderer extends DefaultListCellRenderer {
    
    private Icon icoTicketNormal;
    private Icon icoTicketRefund;
    private Icon icoTicketDebt;

    /** Creates a new instance of ProductRenderer */
    public FindTicketsRenderer() {
        this.icoTicketNormal = ImageLoader.readImageIcon("tkt_type_pay.png");
        this.icoTicketRefund = ImageLoader.readImageIcon("tkt_type_refund.png");
        this.icoTicketDebt = ImageLoader.readImageIcon("tkt_type_debtrecov.png");
    }

    @Override
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        super.getListCellRendererComponent(list, null, index, isSelected, cellHasFocus);
        TicketInfo tkt = (TicketInfo) value;
        int ticketType = tkt.getTicketType();
        String sCustomer;
        if (tkt.getCustomer() == null) {
            sCustomer = "";
        } else {
            sCustomer = tkt.getCustomer().getName();
        }
        int ticketId = tkt.getTicketId();
        Date date = tkt.getDate();
        double total = tkt.getTotalPaid();
        String name = tkt.getUser().getName();
        String sHtml = "<tr><td width=\"30\">"+ "["+ ticketId +"]" +"</td>" +
                "<td width=\"100\">"+ Formats.TIMESTAMP.formatValue(date) +"</td>" +
                "<td align=\"center\" width=\"100\">"+ sCustomer +"</td>" +
                "<td align=\"right\" width=\"100\">"+ Formats.CURRENCY.formatValue(total) +"</td>"+
                "<td width=\"100\">"+ Formats.STRING.formatValue(name) +"</td></tr>";

        setText("<html><table>" + sHtml +"</table></html>");
        switch (ticketType) {
        case TicketInfo.RECEIPT_NORMAL:
            setIcon(icoTicketNormal);
            break;
        case TicketInfo.RECEIPT_REFUND:
            setIcon(icoTicketRefund);
            break;
        case TicketInfo.RECEIPT_PAYMENT:
            setIcon(icoTicketDebt);
            break;
        }
        
        return this;
    }   
}
