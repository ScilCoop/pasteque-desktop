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
import fr.pasteque.format.DateUtils;
import fr.pasteque.pos.customers.CustomerInfoExt;
import fr.pasteque.pos.forms.AppConfig;
import fr.pasteque.pos.ticket.ProductInfoExt;
import fr.pasteque.pos.ticket.CategoryInfo;

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

public class CatalogCache {

    private static Logger logger = Logger.getLogger("fr.pasteque.pos.caching.CatalogCache");

    private static PreparedStatement category;
    private static PreparedStatement categories;
    private static PreparedStatement subCategories;
    private static PreparedStatement product;
    private static PreparedStatement productsByCat;
    private static PreparedStatement productsSearch;
    private static PreparedStatement productByBarcode;

    private CatalogCache() {}

    private static void init() throws SQLException {
        category = LocalDB.prepare("SELECT data FROM categories "
                + "WHERE id = ?");
        categories = LocalDB.prepare("SELECT data FROM categories");
        subCategories = LocalDB.prepare("SELECT data FROM categories "
                + "WHERE parentId = ? ORDER BY dispOrder");
        product = LocalDB.prepare("SELECT data FROM products "
                + "WHERE id = ?");
        productsByCat = LocalDB.prepare("SELECT data FROM products "
                + "WHERE categoryId = ? ORDER BY dispOrder");
        productsSearch = LocalDB.prepare("SELECT data FROM products "
                + "WHERE LOWER(ref) LIKE ? AND LOWER(label) LIKE ?");
        productByBarcode = LocalDB.prepare("SELECT data FROM products "
                + "WHERE barcode = ?");
    }

    private static List<ProductInfoExt> readProductResult(ResultSet rs)
        throws BasicException {
        try {
            List<ProductInfoExt> prds = new ArrayList<ProductInfoExt>();
            while (rs.next()) {
                byte[] data = rs.getBytes("data");
                ByteArrayInputStream bis = new ByteArrayInputStream(data);
                ObjectInputStream os = new ObjectInputStream(bis);
                ProductInfoExt prd = (ProductInfoExt) os.readObject();
                prds.add(prd);
            }
            return prds;
        } catch (SQLException e) {
            throw new BasicException(e);
        } catch (IOException e) {
            throw new BasicException(e);
        } catch (ClassNotFoundException e) {
            // Should never happen
            throw new BasicException(e);
        }
    }
    private static List<CategoryInfo> readCategoryResult(ResultSet rs)
        throws BasicException {
        try {
            List<CategoryInfo> categories = new ArrayList<CategoryInfo>();
            while (rs.next()) {
                byte[] data = rs.getBytes("data");
                ByteArrayInputStream bis = new ByteArrayInputStream(data);
                ObjectInputStream os = new ObjectInputStream(bis);
                CategoryInfo cat = (CategoryInfo) os.readObject();
                categories.add(cat);
            }
            return categories;
        } catch (SQLException e) {
            throw new BasicException(e);
        } catch (IOException e) {
            throw new BasicException(e);
        } catch (ClassNotFoundException e) {
            // Should never happen
            throw new BasicException(e);
        }
    }

    /** Clear and replace categories. Categories must be ordered
     * in display order*/
    public static void refreshCategories(List<CategoryInfo> categories)
        throws BasicException {
        try {
            LocalDB.execute("TRUNCATE TABLE categories");
            PreparedStatement stmt = LocalDB.prepare("INSERT INTO categories "
                    + "(id, parentId, dispOrder, data) VALUES (?, ?, ?, ?)");
            int order = 0;
            for (CategoryInfo cat : categories) {
                stmt.setString(1, cat.getID());
                stmt.setString(2, cat.getParentId());
                stmt.setInt(3, order);
                ByteArrayOutputStream bos = new ByteArrayOutputStream(5120);
                ObjectOutputStream os = new ObjectOutputStream(bos);
                os.writeObject(cat);
                stmt.setBytes(4, bos.toByteArray());
                os.close();
                stmt.execute();
                order++;
            }
        } catch (SQLException e) {
            throw new BasicException(e);
        } catch (IOException e) {
            throw new BasicException(e);
        }
    }

    /** Clear and replace products. */
    public static void refreshProducts(List<ProductInfoExt> products)
        throws BasicException {
        try {
            LocalDB.execute("TRUNCATE TABLE products");
            PreparedStatement stmt = LocalDB.prepare("INSERT INTO products "
                    + "(id, ref, label, barcode, categoryId, dispOrder, data) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?)");
            for (ProductInfoExt prd : products) {
                stmt.setString(1, prd.getID());
                stmt.setString(2, prd.getReference());
                stmt.setString(3, prd.getName());
                stmt.setString(4, prd.getCode());
                stmt.setString(5, prd.getCategoryID());
                stmt.setInt(6, prd.getDispOrder());
                ByteArrayOutputStream bos = new ByteArrayOutputStream(5120);
                ObjectOutputStream os = new ObjectOutputStream(bos);
                os.writeObject(prd);
                stmt.setBytes(7, bos.toByteArray());
                os.close();
                stmt.execute();
            }
        } catch (SQLException e) {
            throw new BasicException(e);
        } catch (IOException e) {
            throw new BasicException(e);
        }
    }

    public static CategoryInfo getCategory(String catId) throws BasicException {
        try {
            if (category == null) {
                init();
            }
            category.clearParameters();
            category.setString(1, catId);
            ResultSet rs = category.executeQuery();
            List<CategoryInfo> cats = readCategoryResult(rs);
            if (cats.size() > 0) {
                return cats.get(0);
            } else {
                return null;
            }
        } catch (SQLException e) {
            throw new BasicException(e);
        }
    }

    public static List<CategoryInfo> getCategories() throws BasicException {
        try {
            if (categories == null) {
                init();
            }
            ResultSet rs = categories.executeQuery();
            return readCategoryResult(rs);
        } catch (SQLException e) {
            throw new BasicException(e);
        }
    }


    public static List<CategoryInfo> getRootCategories() throws BasicException {
        try {
            PreparedStatement stmt = LocalDB.prepare("SELECT * FROM categories "
                    + "WHERE parentId IS NULL");
            ResultSet rs = stmt.executeQuery();
            return readCategoryResult(rs);
        } catch (SQLException e) {
            throw new BasicException(e);
        }
    }

    public static List<CategoryInfo> getSubcategories(String catId)
        throws BasicException {
        try {
            if (subCategories == null) {
                init();
            }
            subCategories.clearParameters();
            subCategories.setString(1, catId);
            ResultSet rs = subCategories.executeQuery();
            return readCategoryResult(rs);
        } catch (SQLException e) {
            throw new BasicException(e);
        }
    }

    public static ProductInfoExt getProduct(String prdId)
        throws BasicException {
        try {
            if (product == null) {
                init();
            }
            product.clearParameters();
            product.setString(1, prdId);
            ResultSet rs = product.executeQuery();
            List<ProductInfoExt> prds = readProductResult(rs);
            if (prds.size() > 0) {
                return prds.get(0);
            } else {
                return null;
            }
        } catch (SQLException e) {
            throw new BasicException(e);
        }
    }

    public static List<ProductInfoExt> getProductsByCategory(String catId)
        throws BasicException {
        try {
            if (productsByCat == null) {
                init();
            }
            productsByCat.clearParameters();
            productsByCat.setString(1, catId);
            ResultSet rs = productsByCat.executeQuery();
            return readProductResult(rs);
        } catch (SQLException e) {
            throw new BasicException(e);
        }
    }

    public static ProductInfoExt getProductByCode(String code)
        throws BasicException {
        try {
            if (productByBarcode == null) {
                init();
            }
            productByBarcode.clearParameters();
            productByBarcode.setString(1, code);
            ResultSet rs = productByBarcode.executeQuery();
            List<ProductInfoExt> prds = readProductResult(rs);
            if (prds.size() > 0) {
                return prds.get(0);
            } else {
                return null;
            }
        } catch (SQLException e) {
            throw new BasicException(e);
        }
    }

    public static List<ProductInfoExt> searchProducts(String label, String ref)
        throws BasicException {
        try {
            if (productsSearch == null) {
                init();
            }
            productsSearch.clearParameters();
            if (ref == null) {
                productsSearch.setString(1, "%%");
            } else {
                productsSearch.setString(1, "%" + ref.toLowerCase() + "%");
            }
            if (label == null) {
                productsSearch.setString(2, "%%");
            } else {
                productsSearch.setString(2, "%" + label.toLowerCase() + "%");
            }
            ResultSet rs = productsSearch.executeQuery();
            return readProductResult(rs);
        } catch (SQLException e) {
            throw new BasicException(e);
        }
    }

}
