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
import fr.pasteque.pos.inventory.TaxCategoryInfo;
import fr.pasteque.pos.ticket.TaxInfo;

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

public class TaxesCache {

    private static Logger logger = Logger.getLogger("fr.pasteque.pos.caching.CatalogCache");

    private static PreparedStatement taxCat;
    private static PreparedStatement taxCats;
    private static PreparedStatement tax;
    private static PreparedStatement taxes;
    private static PreparedStatement taxByCat;

    private TaxesCache() {}

    private static void init() throws SQLException {
        taxCat = LocalDB.prepare("SELECT data FROM taxCats "
                + "WHERE id = ?");
        taxCats = LocalDB.prepare("SELECT data FROM taxCats");
        tax = LocalDB.prepare("SELECT data FROM taxes WHERE id = ?");
        taxes = LocalDB.prepare("SELECT data FROM taxes");
        taxByCat = LocalDB.prepare("SELECT data FROM taxes "
                + "WHERE taxCatId = ?");
    }

    private static List<TaxCategoryInfo> readTaxCatResult(ResultSet rs)
        throws BasicException {
        try {
            List<TaxCategoryInfo> cats = new ArrayList<TaxCategoryInfo>();
            while (rs.next()) {
                byte[] data = rs.getBytes("data");
                ByteArrayInputStream bis = new ByteArrayInputStream(data);
                ObjectInputStream os = new ObjectInputStream(bis);
                TaxCategoryInfo cat = (TaxCategoryInfo) os.readObject();
                cats.add(cat);
            }
            return cats;
        } catch (SQLException e) {
            throw new BasicException(e);
        } catch (IOException e) {
            throw new BasicException(e);
        } catch (ClassNotFoundException e) {
            // Should never happen
            throw new BasicException(e);
        }
    }
    private static List<TaxInfo> readTaxResult(ResultSet rs)
        throws BasicException {
        try {
            List<TaxInfo> taxes = new ArrayList<TaxInfo>();
            while (rs.next()) {
                byte[] data = rs.getBytes("data");
                ByteArrayInputStream bis = new ByteArrayInputStream(data);
                ObjectInputStream os = new ObjectInputStream(bis);
                TaxInfo tax = (TaxInfo) os.readObject();
                taxes.add(tax);
            }
            return taxes;
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
    public static void refreshTaxCategories(List<TaxCategoryInfo> categories)
        throws BasicException {
        try {
            LocalDB.execute("TRUNCATE TABLE taxCats");
            PreparedStatement stmt = LocalDB.prepare("INSERT INTO taxCats "
                    + "(id, data) VALUES (?, ?)");
            for (TaxCategoryInfo cat : categories) {
                stmt.setString(1, cat.getID());
                ByteArrayOutputStream bos = new ByteArrayOutputStream(5120);
                ObjectOutputStream os = new ObjectOutputStream(bos);
                os.writeObject(cat);
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

    /** Clear and replace taxes. */
    public static void refreshTaxes(List<TaxInfo> taxes)
        throws BasicException {
        try {
            LocalDB.execute("TRUNCATE TABLE taxes");
            PreparedStatement stmt = LocalDB.prepare("INSERT INTO taxes "
                    + "(id, taxCatId, data) "
                    + "VALUES (?, ?, ?)");
            for (TaxInfo tax : taxes) {
                stmt.setString(1, tax.getId());
                stmt.setString(2, tax.getTaxCategoryID());
                ByteArrayOutputStream bos = new ByteArrayOutputStream(5120);
                ObjectOutputStream os = new ObjectOutputStream(bos);
                os.writeObject(tax);
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

    public static TaxInfo getTax(String taxId) throws BasicException {
        try {
            if (tax == null) {
                init();
            }
            tax.clearParameters();
            tax.setString(1, taxId);
            ResultSet rs = tax.executeQuery();
            List<TaxInfo> taxes = readTaxResult(rs);
            if (taxes.size() > 0) {
                return taxes.get(0);
            } else {
                return null;
            }
        } catch (SQLException e) {
            throw new BasicException(e);
        }
    }

    public static List<TaxInfo> getTaxes() throws BasicException {
        try {
            if (taxes == null) {
                init();
            }
            ResultSet rs = taxes.executeQuery();
            List<TaxInfo> taxes = readTaxResult(rs);
            return taxes;
        } catch (SQLException e) {
            throw new BasicException(e);
        }
    }

    public static List<TaxCategoryInfo> getTaxCats() throws BasicException {
        try {
            if (taxCats == null) {
                init();
            }
            ResultSet rs = taxCats.executeQuery();
            List<TaxCategoryInfo> taxCats = readTaxCatResult(rs);
            return taxCats;
        } catch (SQLException e) {
            throw new BasicException(e);
        }
    }
}
