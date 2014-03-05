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
import fr.pasteque.pos.admin.CurrencyInfo;
import fr.pasteque.pos.forms.AppConfig;

import java.io.File;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CurrenciesCache {

    private static Logger logger = Logger.getLogger("fr.pasteque.pos.caching.CurrenciesCache");

    private static PreparedStatement currencies;
    private static PreparedStatement currency;
    private static PreparedStatement mainCurrency;

    private CurrenciesCache() {}

    private static void init() throws SQLException {
        currencies = LocalDB.prepare("SELECT data FROM currencies");
        currency = LocalDB.prepare("SELECT data FROM currencies "
                + "WHERE id = ?");
        mainCurrency = LocalDB.prepare("SELECT data FROM currencies "
                + "WHERE main = true");
    }

    private static List<CurrencyInfo> readCurrencyResult(ResultSet rs)
        throws BasicException {
        try {
            List<CurrencyInfo> currs = new ArrayList<CurrencyInfo>();
            while (rs.next()) {
                byte[] data = rs.getBytes("data");
                ByteArrayInputStream bis = new ByteArrayInputStream(data);
                ObjectInputStream os = new ObjectInputStream(bis);
                CurrencyInfo curr = (CurrencyInfo) os.readObject();
                currs.add(curr);
            }
            return currs;
        } catch (SQLException e) {
            throw new BasicException(e);
        } catch (IOException e) {
            throw new BasicException(e);
        } catch (ClassNotFoundException e) {
            // Should never happen
            throw new BasicException(e);
        }
    }

    /** Clear and replace tax categories. */
    public static void refreshCurrencies(List<CurrencyInfo> currencies)
        throws BasicException {
        try {
            LocalDB.execute("TRUNCATE TABLE currencies");
            PreparedStatement stmt = LocalDB.prepare("INSERT INTO currencies "
                    + "(id, main, data) VALUES (?, ?, ?)");
            for (CurrencyInfo curr : currencies) {
                stmt.setInt(1, curr.getID());
                stmt.setBoolean(2, curr.isMain());
                ByteArrayOutputStream bos = new ByteArrayOutputStream(5120);
                ObjectOutputStream os = new ObjectOutputStream(bos);
                os.writeObject(curr);
                stmt.setBytes(3, bos.toByteArray());
                os.close();
                stmt.execute();
            }
        } catch (SQLException e) {
            throw new BasicException(e);
        } catch (IOException e) {
            throw new BasicException(e);
        }
    }

    public static CurrencyInfo getCurrency(int currId) throws BasicException {
        try {
            if (currency == null) {
                init();
            }
            currency.clearParameters();
            currency.setInt(1, currId);
            ResultSet rs = currency.executeQuery();
            List<CurrencyInfo> currs = readCurrencyResult(rs);
            if (currs.size() > 0) {
                return currs.get(0);
            } else {
                return null;
            }
        } catch (SQLException e) {
            throw new BasicException(e);
        }
    }

    public static List<CurrencyInfo> getCurrencies() throws BasicException {
        try {
            if (currencies == null) {
                init();
            }
            ResultSet rs = currencies.executeQuery();
            List<CurrencyInfo> currs = readCurrencyResult(rs);
            return currs;
        } catch (SQLException e) {
            throw new BasicException(e);
        }
    }

    public static CurrencyInfo getMainCurrency()
        throws BasicException {
        try {
            if (mainCurrency == null) {
                init();
            }
            ResultSet rs = mainCurrency.executeQuery();
            List<CurrencyInfo> currs = readCurrencyResult(rs);
            if (currs.size() > 0) {
                return currs.get(0);
            } else {
                return null;
            }
        } catch (SQLException e) {
            throw new BasicException(e);
        }
    }
}
