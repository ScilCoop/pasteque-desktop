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

import fr.pasteque.data.loader.ServerLoader;
import fr.pasteque.pos.sales.SharedTicketInfo;
import fr.pasteque.pos.ticket.TicketInfo;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Message queue for disconnected usage */
public class CallQueue {

    private static Logger logger = Logger.getLogger("fr.pasteque.pos.caching.CallQueue");

    private static final int OP_EDIT = 1;
    private static final int OP_DELETE = 2;

    private static PreparedStatement sharedTicket;
    private static PreparedStatement delSTOperations;
    private static PreparedStatement addTicket;
    private static PreparedStatement getSTOps;
    private static PreparedStatement getTktOps;
    private static PreparedStatement delTktOp;
    private static PreparedStatement countSTOps;
    private static PreparedStatement countTktOps;

    private static String cashId;
    private static boolean offline;
    private static Timer recoverTimer;

    public static void setup(String cashId) {
        CallQueue.cashId = cashId;
    }

    private static void init() throws SQLException {
        sharedTicket = LocalDB.prepare("INSERT INTO sharedTicketQueue "
                + "(id, operation, data) VALUES (?, ?, ?)");
        delSTOperations = LocalDB.prepare("DELETE FROM sharedTicketQueue "
                + "WHERE id = ?");
        addTicket = LocalDB.prepare("INSERT INTO ticketQueue (ticketId , data) "
                + "VALUES (?, ?)");
        getSTOps = LocalDB.prepare("SELECT * FROM sharedTicketQueue");
        getTktOps = LocalDB.prepare("SELECT * FROM ticketQueue");
        delTktOp = LocalDB.prepare("DELETE FROM ticketQueue "
                + "WHERE ticketId = ?");
        countSTOps = LocalDB.prepare("SELECT count(id) AS num "
                + "FROM shareTicketQueue");
        countTktOps = LocalDB.prepare("SELECT count(ticketId) as num "
                + "FROM ticketQueue");
    }

    private static void turnOffline() {
        if (!offline) {
            logger.log(Level.INFO, "Turning offline");
            offline = true;
            // Start the timer to recover
            recoverTimer = new Timer();
            recoverTimer.schedule(new TimerTask(){
                    public void run() {
                        checkRecovery();
                    }
                }, 6000, 60000);
        }
    }
    private static void checkRecovery() {
        ServerLoader loader = new ServerLoader();
        ServerLoader.Response r;
        try {
            r = loader.read("VersionAPI", "");
            if (r.getStatus().equals(ServerLoader.Response.STATUS_OK)) {
                // Server is there!
                logger.log(Level.INFO, "Recovery started");
                if (recover()) {
                    recoverTimer.cancel();
                    recoverTimer = null;
                }
            }
        } catch (Exception e) {
            // Server unavailable
        }
    }

    /** Get total count of pending operations */
    public static synchronized int getOperationsCount() {
        try {
            if (countSTOps == null) {
                init();
            }
            int count = 0;
            ResultSet strs = countSTOps.executeQuery();
            if (strs.next()) {
                count += strs.getInt("num");
            }
            strs.close();
            ResultSet tktrs = countTktOps.executeQuery();
            if (tktrs.next()) {
                count += tktrs.getInt("num");
            }
            tktrs.close();
            return count;
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

    /** Recover from offline mode. Try to send all pending operations, turns
     * off offline mode once everything is sent.
     */
    public static synchronized boolean recover() {
        if (getSTOps == null) {
            try {
                init();
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        }
        ServerLoader loader = new ServerLoader();
        // Send shared tickets operations
        try {
            ResultSet strs = getSTOps.executeQuery();
            while (strs.next()) {
                int op = strs.getInt("operation");
                String id = strs.getString("id");
                if (op == OP_EDIT) {
                    // Edit shared ticket
                    try {
                        byte[] data = strs.getBytes("data");
                        ByteArrayInputStream bis = new ByteArrayInputStream(data);
                        ObjectInputStream os = new ObjectInputStream(bis);
                        SharedTicketInfo tkt = (SharedTicketInfo) os.readObject();
                        os.close();
                        ServerLoader.Response r;
                        r = loader.write("TicketsAPI", "share", "ticket",
                                tkt.toJSON().toString());
                        if (!r.getStatus().equals(ServerLoader.Response.STATUS_OK)) {
                            logger.log(Level.WARNING, "Recovery failed, "
                                    + "server error: "
                                    + r.getResponse().toString());
                            try {
                                strs.close();
                            } catch (SQLException e) {
                                e.printStackTrace();
                            }
                            return false;
                        }
                        // Shared ticket edited, remove from queue
                        deletePreviousOperations(id);
                        logger.log(Level.INFO, "Shared ticket " + id
                                + " edited");
                    } catch (Exception e) {
                        logger.log(Level.INFO, "Recovery failed: "
                                + e.getMessage());
                        try {
                            strs.close();
                        } catch (SQLException e2) {
                            e2.printStackTrace();
                        }
                        return false;
                    }
                } else {
                    // Delete shared ticket
                    try {
                        ServerLoader.Response r;
                        r = loader.write("TicketsAPI", "delShared", "id", id);
                        if (!r.getStatus().equals(ServerLoader.Response.STATUS_OK)) {
                            logger.log(Level.WARNING, "Shared ticket "
                                    + "recovery failed, server error: "
                                    + r.getResponse().toString());
                            strs.close();
                            return false;
                        }
                        // Shared ticket deleted, remove from queue
                        deletePreviousOperations(id);
                        logger.log(Level.INFO, "Shared ticket " + id
                                + " deleted");
                    } catch (Exception e) {
                        logger.log(Level.INFO, "Recovery failed: "
                                + e.getMessage());
                        strs.close();
                        return false;
                    }
                }
            }
            // Everything went well, send ticket operations
            ResultSet tktrs = getTktOps.executeQuery();
            while (tktrs.next()) {
                try {
                    byte[] data = tktrs.getBytes("data");
                    ByteArrayInputStream bis = new ByteArrayInputStream(data);
                    ObjectInputStream os = new ObjectInputStream(bis);
                    TicketInfo tkt = (TicketInfo) os.readObject();
                    os.close();
                    ServerLoader.Response r;
                    r = loader.write("TicketsAPI", "save",
                            "ticket", tkt.toJSON().toString(),
                            "cashId", cashId);
                    if (!r.getStatus().equals(ServerLoader.Response.STATUS_OK)) {
                        logger.log(Level.WARNING, "Ticket recovery failed, "
                                + "server error: "
                                + r.getResponse().toString());
                        try {
                            tktrs.close();
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                        return false;
                    }
                    // Save ticket went well, delete from queue
                    delTktOp.clearParameters();
                    delTktOp.setInt(1, tkt.getTicketId());
                    delTktOp.execute();
                    logger.log(Level.INFO, "Ticket " + tkt.getTicketId()
                            + " saved");
                } catch (Exception e) {
                    logger.log(Level.INFO, "Recovery failed: "
                            + e.getMessage());
                    e.printStackTrace();
                    try {
                        tktrs.close();
                    } catch (SQLException e2) {
                        e2.printStackTrace();
                    }
                    return false;
                }
            }
        } catch (SQLException fuck) {
            fuck.printStackTrace();
            return false;
        }
        logger.log(Level.INFO, "Recovered from offline mode");
        offline = false;
        return true;
    }

    public static boolean isOffline() {
        return offline;
    }

    /** Delete pending operation on a shared ticket. */
    private static synchronized void deletePreviousOperations(String id) throws SQLException {
        if (delSTOperations == null) {
            init();
        }
        delSTOperations.clearParameters();
        delSTOperations.setString(1, id);
        delSTOperations.execute();
    }

    /** Queue a call to save a shared ticket. If a previous call for the ticket
     * is already queued it is discarded. Turns on offline mode.
     */
    public static synchronized void queueSharedTicketSave(String id,
            SharedTicketInfo ticket) {
        turnOffline();
        try {
            deletePreviousOperations(id);        
            // Replace with the new ticket
            sharedTicket.clearParameters();
            sharedTicket.setString(1, id);
            sharedTicket.setInt(2, OP_EDIT);
            ByteArrayOutputStream bos = new ByteArrayOutputStream(5120);
            ObjectOutputStream os = new ObjectOutputStream(bos);
            os.writeObject(ticket);
            byte[] data = bos.toByteArray();
            os.close();
            sharedTicket.setBytes(3, data);
            sharedTicket.execute();
            logger.log(Level.INFO, "Queued shared ticket " + id);
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    /** Queue a call to delete a shared ticket. All previous calls for the
     * ticket are discarded. Turns on offline mode.
     */
    public static synchronized void queueDeleteSharedTicket(String id) {
        turnOffline();
        try {
            deletePreviousOperations(id);
            sharedTicket.clearParameters();
            sharedTicket.setString(1, id);
            sharedTicket.setInt(2, OP_DELETE);
            sharedTicket.setBytes(3, null);
            sharedTicket.execute();
            logger.log(Level.INFO, "Queued shared ticket " + id + " delete");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /** Queue a call to save a ticket. Turns on offline mode. */
    public static synchronized void queueTicketSave(TicketInfo ticket) {
        turnOffline();
        try {
            if (addTicket == null) {
                init();
            }
            addTicket.clearParameters();
            addTicket.setInt(1, ticket.getTicketId());
            ByteArrayOutputStream bos = new ByteArrayOutputStream(5120);
            ObjectOutputStream os = new ObjectOutputStream(bos);
            os.writeObject(ticket);
            byte[] data = bos.toByteArray();
            os.close();
            addTicket.setBytes(2, data);
            addTicket.execute();
            logger.log(Level.INFO, "Queued ticket " + ticket.getTicketId());
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}