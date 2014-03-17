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

import fr.pasteque.format.DateUtils;
import fr.pasteque.pos.forms.AppConfig;
import fr.pasteque.pos.forms.AppUser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ByteArrayOutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Local database cache. Not intended for multithreading or multiuser. */
public class LocalDB {

    private static Logger logger = Logger.getLogger("fr.pasteque.pos.caching.LocalDB");
    private static final int VERSION = 1;

    private static Connection conn = null;

    private static String path() {
        return AppConfig.loadedInstance.getDataDir() + "/db_cache";
    }

    private LocalDB() {}

    private static Connection getConnection() throws SQLException {
        if (conn == null) {
            try {
                Class.forName("org.h2.Driver");
                String url = "jdbc:h2:" + path();
                conn = DriverManager.getConnection(url, "pasteque", "");
            } catch (ClassNotFoundException e) {
                // Should never happen
                e.printStackTrace();
            }
        }
        return conn;
    }

    public static void init() throws SQLException {
        Connection c = getConnection();
        Statement stmt = c.createStatement();
        try {
            stmt.executeQuery("SELECT version FROM meta");
            // When it will be usefull upgrade code goes here
        } catch (SQLException e) {
            // Create the database
            stmt.execute("CREATE TABLE meta (version INTEGER)");
            stmt.execute("INSERT INTO meta (version) VALUES (" + VERSION + ")");
            stmt.execute("CREATE TABLE products ("
                    + "id VARCHAR(255), ref VARCHAR(255), label VARCHAR(255), "
                    + "barcode VARCHAR(255), categoryId VARCHAR(255), "
                    + "dispOrder INTEGER(255), data BINARY(5000000), "
                    + "PRIMARY KEY (id))");
            stmt.execute("CREATE TABLE categories ("
                    + "id VARCHAR(255), parentId VARCHAR(255), "
                    + "dispOrder INTEGER(255), data BINARY(5000000), "
                    + "PRIMARY KEY (id))");
            stmt.execute("CREATE TABLE taxCats ("
                    + "id VARCHAR(255), data BINARY(500000), "
                    + "PRIMARY KEY (id))");
            stmt.execute("CREATE TABLE taxes ("
                    + "id VARCHAR(255), taxCatId VARCHAR(255), "
                    + "data BINARY(500000), "
                    + "PRIMARY KEY (id))");
            stmt.execute("CREATE TABLE currencies ("
                    + "id INTEGER(255), main BOOLEAN, data BINARY(500000), "
                    + "PRIMARY KEY (id))");
            stmt.execute("CREATE TABLE customers ("
                    + "id VARCHAR(255), number INTEGER(255), key VARCHAR(255), "
                    + "name VARCHAR(255), card VARCHAR(255), "
                    + "data BINARY(500000), "
                    + "PRIMARY KEY (id))");
            stmt.execute("CREATE TABLE customerRanking ("
                    + "id VARCHAR(255), rank INTEGER(255), "
                    + "PRIMARY KEY (id))");
            stmt.execute("CREATE TABLE tariffAreas ("
                    + "id INTEGER(255), data BINARY(500000), "
                    + "PRIMARY KEY (id))");
            stmt.execute("CREATE TABLE tariffAreaPrices ("
                    + "areaId INTEGER(255), prdId VARCHAR(255), price DOUBLE, "
                    + "PRIMARY KEY (areaId, prdId))");
            stmt.execute("CREATE TABLE subgroups ("
                    + "id INTEGER(255), compositionId VARCHAR(255), "
                    + "dispOrder INTEGER(255), data BINARY (500000), "
                    + "PRIMARY KEY (id))");
            stmt.execute("CREATE TABLE subgroupProds ("
                    + "groupId INTEGER(255), prdId VARCHAR(255), "
                    + "dispOrder INTEGER(255), "
                    + "PRIMARY KEY (groupId, prdId))");
            logger.log(Level.INFO, "Initialized database version " + VERSION);
        }
    }

    public static void close() {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                // Can't we get anything from it...
                e.printStackTrace();
            }
            conn = null;
        }
    }

    public static PreparedStatement prepare(String sql) throws SQLException {
        Connection c = getConnection();
        return c.prepareStatement(sql);
    }

    public static void execute(String sql) throws SQLException {
        Connection c = getConnection();
        Statement stmt = c.createStatement();
        stmt.execute(sql);
    }
}
