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

package fr.pasteque.pos.sales;

import fr.pasteque.basic.BasicException;
import fr.pasteque.pos.ticket.TicketInfo;

import java.io.IOException;
import java.io.Serializable;
import javax.xml.bind.DatatypeConverter;
import org.json.JSONObject;


/**
 *
 * @author adrianromero
 */
public class SharedTicketInfo implements Serializable {
    
    private static final long serialVersionUID = 7640633837719L;
    private String id;
    private String name;
    private TicketInfo ticket;
    
    /** Creates a new instance of SharedTicketInfo */
    public SharedTicketInfo(String id, TicketInfo ticket) {
        this.id = id;
        this.name = ticket.getName();
        this.ticket = ticket;
    }

    public SharedTicketInfo(JSONObject o) throws IOException {
        this.id = o.getString("id");
        this.name = o.getString("label");
        String strdata = o.getString("data");
        byte[] data = DatatypeConverter.parseBase64Binary(strdata);
        TicketInfo tkt = new TicketInfo(data);
        this.ticket = tkt;
    }
    
    public String getId() {
        return id;
    }
    
    public String getName() {
        return name;
    }

    public TicketInfo getTicket() {
        return this.ticket;
    }
}
