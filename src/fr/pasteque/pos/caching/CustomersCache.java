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
import fr.pasteque.pos.customers.CustomerInfoExt;
import fr.pasteque.pos.forms.AppConfig;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CustomersCache {

    private static Logger logger = Logger.getLogger("fr.pasteque.pos.caching.CustomersCache");

    private static PreparedStatement customers;
    private static PreparedStatement customer;
    private static PreparedStatement customerCard;
    private static PreparedStatement ranking;
    private static PreparedStatement search;

    private CustomersCache() {}

    private static void init() throws SQLException {
        customers = LocalDB.prepare("SELECT data FROM customers");
        customer = LocalDB.prepare("SELECT data FROM customers "
                + "WHERE id = ?");
        customerCard = LocalDB.prepare("SELECT data FROM customers "
                + "WHERE card = ?");
        ranking = LocalDB.prepare("SELECT data FROM customers, customerRanking "
                + "WHERE customers.id = customerRanking.id "
                + "ORDER BY rank ASC");
        search = LocalDB.prepare("SELECT data FROM customers "
                + "WHERE LOWER(number) LIKE LOWER(?) "
                + "AND LOWER(key) LIKE LOWER(?) AND LOWER(name) LIKE LOWER(?)");
    }

    private static List<CustomerInfoExt> readCustomerResult(ResultSet rs)
        throws BasicException {
        try {
            List<CustomerInfoExt> custs = new ArrayList<CustomerInfoExt>();
            while (rs.next()) {
                byte[] data = rs.getBytes("data");
                ByteArrayInputStream bis = new ByteArrayInputStream(data);
                ObjectInputStream os = new ObjectInputStream(bis);
                CustomerInfoExt cust = (CustomerInfoExt) os.readObject();
                custs.add(cust);
            }
            return custs;
        } catch (SQLException e) {
            throw new BasicException(e);
        } catch (IOException e) {
            throw new BasicException(e);
        } catch (ClassNotFoundException e) {
            // Should never happen
            throw new BasicException(e);
        }
    }

    /** Clear and replace customers. */
    public static void refreshCustomers(List<CustomerInfoExt> customers)
        throws BasicException {
        try {
            LocalDB.execute("TRUNCATE TABLE customers");
            PreparedStatement stmt = LocalDB.prepare("INSERT INTO customers "
                    + "(id, number, key, name, card, data) VALUES "
                    + "(?, ?, ?, ?, ?, ?)");
            for (CustomerInfoExt cust : customers) {
                stmt.setString(1, cust.getId());
                stmt.setString(2, cust.getTaxid());
                stmt.setString(3, cust.getSearchkey());
                stmt.setString(4, cust.getName());
                stmt.setString(5, cust.getCard());
                ByteArrayOutputStream bos = new ByteArrayOutputStream(5120);
                ObjectOutputStream os = new ObjectOutputStream(bos);
                os.writeObject(cust);
                stmt.setBytes(6, bos.toByteArray());
                os.close();
                stmt.execute();
            }
        } catch (SQLException e) {
            throw new BasicException(e);
        } catch (IOException e) {
            throw new BasicException(e);
        }
    }
    /** Clear and replace customer ranking. */
    public static void refreshRanking(List<String> ids)
        throws BasicException {
        try {
            LocalDB.execute("TRUNCATE TABLE customerRanking");
            PreparedStatement stmt = LocalDB.prepare("INSERT "
                    + "INTO customerRanking "
                    + "(id, rank) VALUES (?, ?)");
            for (int i = 0; i < ids.size(); i++) {
                stmt.setString(1, ids.get(i));
                stmt.setInt(2, i);
                stmt.execute();
            }
        } catch (SQLException e) {
            throw new BasicException(e);
        }
    }

    /** Get cached data if any, null otherwise */
    public static List<CustomerInfoExt> getCustomers() throws BasicException {
        try {
            if (customers == null) {
                init();
            }
            ResultSet rs = customers.executeQuery();
            return readCustomerResult(rs);
        } catch (SQLException e) {
            throw new BasicException(e);
        }
    }

    public static CustomerInfoExt getCustomer(String id) throws BasicException {
        try {
            if (customer == null) {
                init();
            }
            customer.clearParameters();
            customer.setString(1, id);
            ResultSet rs = customer.executeQuery();
            List<CustomerInfoExt> custs = readCustomerResult(rs);
            if (custs.size() > 0) {
                return custs.get(0);
            } else {
                return null;
            }
        } catch (SQLException e) {
            throw new BasicException(e);
        }
    }

    public static CustomerInfoExt getCustomerByCard(String card)
        throws BasicException {
        try {
            if (customerCard == null) {
                init();
            }
            customerCard.clearParameters();
            customerCard.setString(1, card);
            ResultSet rs = customerCard.executeQuery();
            List<CustomerInfoExt> custs = readCustomerResult(rs);
            if (custs.size() > 0) {
                return custs.get(0);
            } else {
                return null;
            }
        } catch (SQLException e) {
            throw new BasicException(e);
        }
    }

    /** Get cached data if any, null otherwise */
    public static List<CustomerInfoExt> getTopCustomers()
        throws BasicException {
        try {
            if (ranking == null) {
                init();
            }
            ResultSet rs = ranking.executeQuery();
            return readCustomerResult(rs);
        } catch (SQLException e) {
            throw new BasicException(e);
        }
    }

    public static List<CustomerInfoExt> searchCustomers(String number,
            String searchkey, String name) throws BasicException {
        if (number == null) {
            number = "%%";
        } else {
            number = "%" + number + "%";
        }
        if (searchkey == null) {
            searchkey = "%%";
        } else {
            searchkey = "%" + searchkey + "%";
        }
        if (name == null) {
            name = "%%";
        } else {
            name = "%" + name + "%";
        }
        try {
            if (search == null) {
                init();
            }
            search.clearParameters();
            search.setString(1, number);
            search.setString(2, searchkey);
            search.setString(3, name);
            ResultSet rs = search.executeQuery();
            return readCustomerResult(rs);
        } catch (SQLException e) {
            throw new BasicException(e);
        }
    }
}
