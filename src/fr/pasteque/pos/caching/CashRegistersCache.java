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
import fr.pasteque.pos.ticket.CashRegisterInfo;

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

public class CashRegistersCache {

    private static Logger logger = Logger.getLogger("fr.pasteque.pos.caching.CashRegistersCache");

    private static PreparedStatement cashRegister;

    private CashRegistersCache() {}

    private static void init() throws SQLException {
        cashRegister = LocalDB.prepare("SELECT data FROM cashRegisters "
                + "WHERE id = ?");
    }

    private static List<CashRegisterInfo> readCashRegisterResult(ResultSet rs)
        throws BasicException {
        try {
            List<CashRegisterInfo> crs = new ArrayList<CashRegisterInfo>();
            while (rs.next()) {
                byte[] data = rs.getBytes("data");
                ByteArrayInputStream bis = new ByteArrayInputStream(data);
                ObjectInputStream os = new ObjectInputStream(bis);
                CashRegisterInfo cr = (CashRegisterInfo) os.readObject();
                crs.add(cr);
            }
            return crs;
        } catch (SQLException e) {
            throw new BasicException(e);
        } catch (IOException e) {
            throw new BasicException(e);
        } catch (ClassNotFoundException e) {
            // Should never happen
            throw new BasicException(e);
        }
    }

    /** Clear and replace cash registers. */
    public static void refreshCashRegisters(List<CashRegisterInfo> crs)
        throws BasicException {
        try {
            LocalDB.execute("TRUNCATE TABLE cashRegisters");
            PreparedStatement stmt = LocalDB.prepare("INSERT INTO "
                    + "cashRegisters (id, data) VALUES (?, ?)");
            for (CashRegisterInfo cr : crs) {
                stmt.setInt(1, cr.getId());
                ByteArrayOutputStream bos = new ByteArrayOutputStream(5120);
                ObjectOutputStream os = new ObjectOutputStream(bos);
                os.writeObject(cr);
                stmt.setBytes(2, bos.toByteArray());
                os.close();
                stmt.execute();
            }
        } catch (SQLException e) {
            throw new BasicException(e);
        } catch (IOException e) {
            throw new BasicException(e);
        }
    }

    public static CashRegisterInfo getCashRegister(int id)
        throws BasicException {
        try {
            if (cashRegister == null) {
                init();
            }
            cashRegister.clearParameters();
            cashRegister.setInt(1, id);
            ResultSet rs = cashRegister.executeQuery();
            List<CashRegisterInfo> crs = readCashRegisterResult(rs);
            if (crs.size() > 0) {
                return crs.get(0);
            } else {
                return null;
            }
        } catch (SQLException e) {
            throw new BasicException(e);
        }
    }
}
