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
import fr.pasteque.pos.ticket.TariffInfo;

import java.io.File;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class TariffAreasCache {

    private static Logger logger = Logger.getLogger("fr.pasteque.pos.caching.TariffAreasCache");

    private static PreparedStatement areas;
    private static PreparedStatement price;

    private TariffAreasCache() {}

    private static void init() throws SQLException {
        areas = LocalDB.prepare("SELECT data FROM tariffAreas");
        price = LocalDB.prepare("SELECT price FROM tariffAreaPrices "
                + "WHERE areaId = ? AND prdId = ?");
    }

    private static List<TariffInfo> readAreaResult(ResultSet rs)
        throws BasicException {
        try {
            List<TariffInfo> areas = new ArrayList<TariffInfo>();
            while (rs.next()) {
                byte[] data = rs.getBytes("data");
                ByteArrayInputStream bis = new ByteArrayInputStream(data);
                ObjectInputStream os = new ObjectInputStream(bis);
                TariffInfo area = (TariffInfo) os.readObject();
                areas.add(area);
            }
            return areas;
        } catch (SQLException e) {
            throw new BasicException(e);
        } catch (IOException e) {
            throw new BasicException(e);
        } catch (ClassNotFoundException e) {
            // Should never happen
            throw new BasicException(e);
        }
    }
    private static Double readPriceResult(ResultSet rs)
        throws BasicException {
        try {
            if (rs.next()) {
                return rs.getDouble("price");
            }
            return null;
        } catch (SQLException e) {
            throw new BasicException(e);
        }
    }

    /** Clear and replace tariff areas. */
    public static void refreshTariffAreas(List<TariffInfo> areas)
        throws BasicException {
        try {
            LocalDB.execute("TRUNCATE TABLE tariffAreas");
            PreparedStatement stmt = LocalDB.prepare("INSERT INTO tariffAreas "
                    + "(id, data) VALUES (?, ?)");
            for (TariffInfo area : areas) {
                stmt.setInt(1, area.getID());
                ByteArrayOutputStream bos = new ByteArrayOutputStream(5120);
                ObjectOutputStream os = new ObjectOutputStream(bos);
                os.writeObject(area);
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

    /** Clear and replace prices. */
    public static void refreshPrices(Map<Integer, Map<String, Double>> prices)
        throws BasicException {
        try {
            LocalDB.execute("TRUNCATE TABLE tariffAreaPrices");
            PreparedStatement stmt = LocalDB.prepare("INSERT INTO tariffAreaPrices "
                    + "(areaId, prdId, price) "
                    + "VALUES (?, ?, ?)");
            for (int areaId : prices.keySet()) {
                stmt.setInt(1, areaId);
                Map<String, Double> areaPrices = prices.get(areaId);
                for (String prdId : areaPrices.keySet()) {
                    double prdPrice = areaPrices.get(prdId);
                    stmt.setString(2, prdId);
                    stmt.setDouble(3, prdPrice);
                    stmt.execute();
                }
            }
        } catch (SQLException e) {
            throw new BasicException(e);
        }
    }

    public static List<TariffInfo> getAreas() throws BasicException {
        try {
            if (areas == null) {
                init();
            }
            ResultSet rs = areas.executeQuery();
            List<TariffInfo> list = readAreaResult(rs);
            return list;
        } catch (SQLException e) {
            throw new BasicException(e);
        }
    }

    public static Double getPrice(int areaId, String prdId)
        throws BasicException {
        try {
            if (price == null) {
                init();
            }
            price.clearParameters();
            price.setInt(1, areaId);
            price.setString(2, prdId);
            ResultSet rs = price.executeQuery();
            return readPriceResult(rs);
        } catch (SQLException e) {
            throw new BasicException(e);
        }
    }
}
