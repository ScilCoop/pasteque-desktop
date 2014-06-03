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
import fr.pasteque.pos.ticket.TicketLineInfo;

import java.io.IOException;
import java.io.Serializable;
import javax.xml.bind.DatatypeConverter;
import org.json.JSONArray;
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

    public SharedTicketInfo(JSONObject o) throws BasicException {
        this.id = o.getString("id");
        this.name = o.getString("label");
        TicketInfo tkt = TicketInfo.sharedTicketInfo(o);
        this.ticket = tkt;
    }

    public JSONObject toJSON() {
        JSONObject o = new JSONObject();
        o.put("id", id);
        o.put("label", ticket.getName());
        if (this.ticket.getCustomerId() != null) {
            o.put("customerId", this.ticket.getCustomerId());
        } else {
            o.put("customerId", JSONObject.NULL);
        }
        if (this.ticket.getTariffArea() != null) {
            o.put("tariffAreaId", this.ticket.getTariffArea());
        } else {
            o.put("tariffAreaId", JSONObject.NULL);
        }
        if (this.ticket.getDiscountProfileId() != null) {
            o.put("discountProfileId", this.ticket.getDiscountProfileId());
        } else {
            o.put("discountProfileId", JSONObject.NULL);
        }
        o.put("discountRate", this.ticket.getDiscountRate());

        JSONArray lines = new JSONArray();
        int i = 0;
        for (TicketLineInfo l : this.ticket.getLines()) {
            JSONObject line = l.toJSON();
            lines.put(line);
        }
        o.put("lines", lines);
        return o;
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
