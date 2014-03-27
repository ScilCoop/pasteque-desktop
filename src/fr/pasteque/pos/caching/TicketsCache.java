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

package fr.pasteque.pos.caching;

import fr.pasteque.basic.BasicException;
import fr.pasteque.pos.forms.AppConfig;
import fr.pasteque.pos.sales.SharedTicketInfo;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class TicketsCache {

    private static Logger logger = Logger.getLogger("fr.pasteque.pos.caching.TicketsCache");

    private static PreparedStatement insert;
    private static PreparedStatement get;
    private static PreparedStatement update;
    private static PreparedStatement delete;
    private static PreparedStatement getAll;

    private TicketsCache() {}

    private static void init() throws SQLException {
        insert = LocalDB.prepare("INSERT INTO sharedTickets (id, data) "
                + "VALUES (?, ?)");
        get = LocalDB.prepare("SELECT data FROM sharedTickets WHERE id = ?");
        update = LocalDB.prepare("UPDATE sharedTickets SET data = ? "
                + "WHERE id = ?");
        delete = LocalDB.prepare("DELETE FROM sharedTickets WHERE id = ?");
        getAll = LocalDB.prepare("SELECT data FROM sharedTickets");
    }

    private static List<SharedTicketInfo> readSharedTicketResult(ResultSet rs)
        throws BasicException {
        try {
            List<SharedTicketInfo> tkts = new ArrayList<SharedTicketInfo>();
            while (rs.next()) {
                byte[] data = rs.getBytes("data");
                ByteArrayInputStream bis = new ByteArrayInputStream(data);
                ObjectInputStream os = new ObjectInputStream(bis);
                SharedTicketInfo tkt = (SharedTicketInfo) os.readObject();
                tkts.add(tkt);
            }
            return tkts;
        } catch (SQLException e) {
            throw new BasicException(e);
        } catch (IOException e) {
            throw new BasicException(e);
        } catch (ClassNotFoundException e) {
            // Should never happen
            throw new BasicException(e);
        }
    }

    public static void refreshTickets(List<SharedTicketInfo> tickets)
        throws BasicException {
        try {
            if (insert == null) {
                init();
            }
            logger.log(Level.INFO, "Cleared shared tickets cache");
            LocalDB.execute("TRUNCATE TABLE sharedTickets");
            for (SharedTicketInfo ticket : tickets) {
                ByteArrayOutputStream bos = new ByteArrayOutputStream(5120);
                ObjectOutputStream os = new ObjectOutputStream(bos);
                os.writeObject(ticket);
                byte[] data = bos.toByteArray();
                os.close();
                // Insert
                insert.clearParameters();
                insert.setString(1, ticket.getId());
                insert.setBytes(2, data);
                insert.execute();
                logger.log(Level.INFO, "Cached shared ticket "
                        + ticket.getId());
            }
        } catch (SQLException e) {
            throw new BasicException(e);
        } catch (IOException e) {
            throw new BasicException(e);
        }        
    }

    public static List<SharedTicketInfo> getAllTickets() throws BasicException {
        try {
            if (getAll == null) {
                init();
            }
            ResultSet rs = getAll.executeQuery();
            return readSharedTicketResult(rs);
        } catch (SQLException e) {
            throw new BasicException(e);
        }
    }

    public static SharedTicketInfo getTicket(String id) throws BasicException {
        try {
            if (get == null) {
                init();
            }
            get.clearParameters();
            get.setString(1, id);
            ResultSet rs = get.executeQuery();
            List<SharedTicketInfo> tkts = readSharedTicketResult(rs);
            if (tkts.size() > 0) {
                return tkts.get(0);
            } else {
                return null;
            }
        } catch (SQLException e) {
            throw new BasicException(e);
        }
    }

    public static void saveTicket(SharedTicketInfo ticket)
        throws BasicException {
        try {
            if (insert == null) {
                init();
            }
            get.clearParameters();
            get.setString(1, ticket.getId());
            ResultSet rs = get.executeQuery();
            ByteArrayOutputStream bos = new ByteArrayOutputStream(5120);
            ObjectOutputStream os = new ObjectOutputStream(bos);
            os.writeObject(ticket);
            byte[] data = bos.toByteArray();
            os.close();
            if (rs.next()) {
                // Update
                update.clearParameters();
                update.setString(2, ticket.getId());
                update.setBytes(1, data);
                update.execute();
                logger.log(Level.INFO, "Updated cached shared ticket "
                        + ticket.getId());
            } else {
                // Insert
                insert.clearParameters();
                insert.setString(1, ticket.getId());
                insert.setBytes(2, data);
                insert.execute();
                logger.log(Level.INFO, "Cached shared ticket "
                        + ticket.getId());
            }
        } catch (SQLException e) {
            throw new BasicException(e);
        } catch (IOException e) {
            throw new BasicException(e);
        }
    }

    public static void deleteTicket(String id) throws BasicException {
        try {
            if (delete == null) {
                init();
            }
            delete.clearParameters();
            delete.setString(1, id);
            delete.execute();
            logger.log(Level.INFO, "Deleted cached shared ticket " + id);
        } catch (SQLException e) {
            throw new BasicException(e);
        }
    }
}
