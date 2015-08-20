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

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.DatatypeConverter;
import fr.pasteque.basic.BasicException;
import fr.pasteque.data.loader.ServerLoader;
import fr.pasteque.pos.caching.CallQueue;
import fr.pasteque.pos.caching.TicketsCache;
import fr.pasteque.pos.ticket.TicketInfo;
import org.json.JSONObject;
import org.json.JSONArray;

/**
 *
 * @author adrianromero
 */
public class DataLogicReceipts {

    private static Logger logger = Logger.getLogger("fr.pasteque.pos.sales.DataLogicReceipts");

    /** Creates a new instance of DataLogicReceipts */
    public DataLogicReceipts() {
    }
    
    public void init(){
    }
     
    public final TicketInfo getSharedTicket(String id) throws BasicException {
        if (CallQueue.isOffline()) {
            // Read from cache until recovery
            SharedTicketInfo stkt = TicketsCache.getTicket(id);
            if (stkt != null) {
                return stkt.getTicket();
            } else {
                return null;
            }
        }
        try {
            ServerLoader loader = new ServerLoader();
            ServerLoader.Response r = loader.read("TicketsAPI", "getShared",
                    "id", id);
            if (r.getStatus().equals(ServerLoader.Response.STATUS_OK)) {
                JSONObject o = r.getObjContent();
                if (o == null) {
                    return null;
                }
                // Ticket read from server, cache it
                SharedTicketInfo stkt = new SharedTicketInfo(o);
                try {
                    TicketsCache.saveTicket(stkt);
                } catch (BasicException e) {
                    e.printStackTrace();
                }
                return stkt.getTicket();
            } else {
                return null;
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "Failed to load shared ticket " + id
                    + " from server: " + e.getMessage());
            // Unable to read it from server, try reading from cache
            SharedTicketInfo stkt = TicketsCache.getTicket(id);
            if (stkt != null) {
                return stkt.getTicket();
            } else {
                return null;
            }
        }
    } 
    
    public final List<SharedTicketInfo> getSharedTicketList() throws BasicException {
        List<SharedTicketInfo> tkts = new ArrayList<SharedTicketInfo>();
        if (CallQueue.isOffline()) {
            // Read from cache until recovery
            tkts = TicketsCache.getAllTickets();
            return tkts;
        }
        try {
            ServerLoader loader = new ServerLoader();
            ServerLoader.Response r = loader.read("TicketsAPI", "getAllShared");
            if (r.getStatus().equals(ServerLoader.Response.STATUS_OK)) {
                JSONArray a = r.getArrayContent();
                for (int i = 0; i < a.length(); i++) {
                    JSONObject o = a.getJSONObject(i);
                    SharedTicketInfo stkt = new SharedTicketInfo(o);
                    tkts.add(stkt);
                }
                // All tickets read from server, cache them
                try {
                    TicketsCache.refreshTickets(tkts);
                } catch (BasicException e) {
                    e.printStackTrace();
                }
                return tkts;
            } else {
                return null;
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "Failed to load shared tickets "
                    + "from server: " + e.getMessage());
            // Failed to load from server, try loading from cache
            tkts = TicketsCache.getAllTickets();
            return tkts;
        }
    }
    
    public final void updateSharedTicket(final String id, final TicketInfo ticket) throws BasicException {
        SharedTicketInfo stkt = new SharedTicketInfo(id, ticket);
        try {
            TicketsCache.saveTicket(stkt);
        } catch (BasicException e) {
            e.printStackTrace();
        }
        if (CallQueue.isOffline()) {
            // Enqueue until recovery
            CallQueue.queueSharedTicketSave(id, stkt);
            return;
        }
        try {
            ServerLoader loader = new ServerLoader();
            ServerLoader.Response r;
            JSONObject tkt = ticket.toSharedJSON();
            tkt.put("id", id);

            r = loader.write("TicketsAPI", "share",
                    "ticket", tkt.toString());
            if (!r.getStatus().equals(ServerLoader.Response.STATUS_OK)) {
                throw new BasicException("Bad server response");
            }
        } catch (Exception e) {
            // Update failed, add to queue
            logger.log(Level.WARNING, "Unable to save shared ticket " + id
                    + ": " + e.getMessage());
            CallQueue.queueSharedTicketSave(id, stkt);
            throw new BasicException(e);
        }
    }
    
    public final void insertSharedTicket(final String id, final TicketInfo ticket) throws BasicException {
        try {
            SharedTicketInfo stkt = new SharedTicketInfo(id, ticket);
            TicketsCache.saveTicket(stkt);
        } catch (BasicException e) {
            e.printStackTrace();
        }
        this.updateSharedTicket(id, ticket);
    }
    
    public final void deleteSharedTicket(final String id) throws BasicException {
        try {
            TicketsCache.deleteTicket(id);
        } catch (BasicException e) {
            e.printStackTrace();
        }
        if (CallQueue.isOffline()) {
            // Enqueue until recovery
            CallQueue.queueDeleteSharedTicket(id);
            return;
        }
        try {
            ServerLoader loader = new ServerLoader();
            ServerLoader.Response r;
            r = loader.write("TicketsAPI", "delShared", "id", id);
            if (!r.getStatus().equals(ServerLoader.Response.STATUS_OK)) {
                throw new BasicException("Bad server response");
            }
        } catch (Exception e) {
            // Delete failed, queue call
            logger.log(Level.WARNING, "Unable to delete shared ticket: "
                    + e.getMessage());
            CallQueue.queueDeleteSharedTicket(id);
            throw new BasicException(e);
        }
    }    
}
