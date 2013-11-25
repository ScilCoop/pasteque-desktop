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
//
//    cashin/cashout notes by Henri Azar

package fr.pasteque.pos.forms;

import fr.pasteque.pos.ticket.CategoryInfo;
import fr.pasteque.pos.ticket.ProductInfoExt;
import fr.pasteque.pos.ticket.TaxInfo;
import fr.pasteque.pos.ticket.TicketInfo;
import fr.pasteque.pos.ticket.TicketLineInfo;
import java.util.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import fr.pasteque.data.loader.*;
import fr.pasteque.format.Formats;
import fr.pasteque.basic.BasicException;
import fr.pasteque.data.model.Field;
import fr.pasteque.data.model.Row;
import fr.pasteque.pos.admin.CurrencyInfo;
import fr.pasteque.pos.customers.CustomerInfoExt;
import fr.pasteque.pos.inventory.AttributeSetInfo;
import fr.pasteque.pos.inventory.TaxCustCategoryInfo;
import fr.pasteque.pos.inventory.LocationInfo;
import fr.pasteque.pos.inventory.MovementReason;
import fr.pasteque.pos.inventory.TaxCategoryInfo;
import fr.pasteque.pos.mant.FloorsInfo;
import fr.pasteque.pos.payment.PaymentInfo;
import fr.pasteque.pos.payment.PaymentInfoTicket;
import fr.pasteque.pos.ticket.FindTicketsInfo;
import fr.pasteque.pos.ticket.SubgroupInfo;
import fr.pasteque.pos.ticket.TariffInfo;
import fr.pasteque.pos.ticket.TicketTaxInfo;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author adrianromero
 */
public class DataLogicSales extends BeanFactoryDataSingle {

    protected Session s;

    protected Datas[] auxiliarDatas;
    protected Datas[] stockdiaryDatas;
    // protected Datas[] productcatDatas;
    protected Datas[] paymenttabledatas;
    protected Datas[] stockdatas;
    protected Datas[] compositionDatas;
    protected Datas[] compositionPrdDatas;
    protected Datas[] subgroupDatas;
    protected Datas[] subgroup_prodDatas;
    protected Datas[] tariffareaDatas;
    protected Datas[] tariffprodDatas;
    protected Datas[] currencyData;

    protected Row productsRow;

    /** Creates a new instance of SentenceContainerGeneric */
    public DataLogicSales() {
        stockdiaryDatas = new Datas[] {Datas.STRING, Datas.TIMESTAMP, Datas.INT, Datas.STRING, Datas.STRING, Datas.STRING, Datas.DOUBLE, Datas.DOUBLE};
        paymenttabledatas = new Datas[] {Datas.STRING, Datas.STRING, Datas.TIMESTAMP, Datas.STRING, Datas.STRING, Datas.DOUBLE, Datas.STRING, Datas.INT};
        stockdatas = new Datas[] {Datas.STRING, Datas.STRING, Datas.STRING, Datas.DOUBLE, Datas.DOUBLE, Datas.DOUBLE};
        auxiliarDatas = new Datas[] {Datas.STRING, Datas.STRING, Datas.STRING, Datas.STRING, Datas.STRING, Datas.STRING};
        tariffareaDatas = new Datas[] {Datas.INT, Datas.STRING, Datas.INT};
        tariffprodDatas = new Datas[] {Datas.INT, Datas.STRING, Datas.DOUBLE};
        compositionDatas = new Datas[] {Datas.INT, Datas.STRING, Datas.STRING, Datas.STRING, Datas.BOOLEAN, Datas.BOOLEAN, Datas.DOUBLE, Datas.DOUBLE, Datas.STRING, Datas.STRING, Datas.IMAGE, Datas.BOOLEAN, Datas.INT, Datas.BYTES};
        subgroupDatas = new Datas[] {Datas.INT, Datas.STRING, Datas.STRING, Datas.IMAGE, Datas.INT};
        subgroup_prodDatas = new Datas[] {Datas.INT, Datas.STRING};
        currencyData = new Datas[] {Datas.INT, Datas.STRING, Datas.STRING, Datas.STRING, Datas.STRING, Datas.STRING, Datas.DOUBLE, Datas.BOOLEAN};

        productsRow = new Row(
                new Field("ID", Datas.STRING, Formats.STRING),
                new Field(AppLocal.getIntString("label.prodref"), Datas.STRING, Formats.STRING, true, true, true),
                new Field(AppLocal.getIntString("label.prodbarcode"), Datas.STRING, Formats.STRING, false, true, true),
                new Field(AppLocal.getIntString("label.prodname"), Datas.STRING, Formats.STRING, true, true, true),
                new Field("ISCOM", Datas.BOOLEAN, Formats.BOOLEAN),
                new Field("ISSCALE", Datas.BOOLEAN, Formats.BOOLEAN),
                new Field(AppLocal.getIntString("label.prodpricebuy"), Datas.DOUBLE, Formats.CURRENCY, false, true, true),
                new Field(AppLocal.getIntString("label.prodpricesell"), Datas.DOUBLE, Formats.CURRENCY, false, true, true),
                new Field(AppLocal.getIntString("label.prodcategory"), Datas.STRING, Formats.STRING, false, false, true),
                new Field(AppLocal.getIntString("label.taxcategory"), Datas.STRING, Formats.STRING, false, false, true),
                new Field(AppLocal.getIntString("label.attributeset"), Datas.STRING, Formats.STRING, false, false, true),
                new Field("IMAGE", Datas.IMAGE, Formats.NULL),
                new Field("STOCKCOST", Datas.DOUBLE, Formats.CURRENCY),
                new Field("STOCKVOLUME", Datas.DOUBLE, Formats.DOUBLE),
                new Field("ISCATALOG", Datas.BOOLEAN, Formats.BOOLEAN),
                new Field("CATORDER", Datas.INT, Formats.INT),
                new Field("PROPERTIES", Datas.BYTES, Formats.NULL),
                new Field("DISCOUNTENABLED", Datas.BOOLEAN, Formats.BOOLEAN),
                new Field("DISCOUNTRATE", Datas.DOUBLE, Formats.DOUBLE));
    }

    public void init(Session s){
        this.s = s;
    }

    public final Row getProductsRow() {
        return productsRow;
    }

    /** Get a product by ID */
    public final ProductInfoExt getProductInfo(String id) throws BasicException {
        try {
            ServerLoader loader = new ServerLoader();
            ServerLoader.Response r = loader.read("ProductsAPI", "get",
                    "id", id);
            if (r.getStatus().equals(ServerLoader.Response.STATUS_OK)) {
                JSONObject o = r.getObjContent();
                ProductInfoExt prd = new ProductInfoExt(o);
                return prd;
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new BasicException(e);
        }
    }

    /** Get a product by code */
    public final ProductInfoExt getProductInfoByCode(String sCode) throws BasicException {
        try {
            ServerLoader loader = new ServerLoader();
            ServerLoader.Response r = loader.read("ProductsAPI", "get",
                    "code", sCode);
            if (r.getStatus().equals(ServerLoader.Response.STATUS_OK)) {
                JSONObject o = r.getObjContent();
                ProductInfoExt prd = new ProductInfoExt(o);
                return prd;
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new BasicException(e);
        }
    }

    /** Get a product by reference */
    public final ProductInfoExt getProductInfoByReference(String sReference) throws BasicException {
        try {
            ServerLoader loader = new ServerLoader();
            ServerLoader.Response r = loader.read("ProductsAPI", "get",
                    "reference", sReference);
            if (r.getStatus().equals(ServerLoader.Response.STATUS_OK)) {
                JSONObject o = r.getObjContent();
                ProductInfoExt prd = new ProductInfoExt(o);
                return prd;
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new BasicException(e);
        }
    }


    /** Get root categories */
    public final List<CategoryInfo> getRootCategories() throws BasicException {
        try {
            ServerLoader loader = new ServerLoader();
            ServerLoader.Response r = loader.read("CategoriesAPI", "getAll");
            if (r.getStatus().equals(ServerLoader.Response.STATUS_OK)) {
                JSONArray a = r.getArrayContent();
                List<CategoryInfo> rootCats = new ArrayList<CategoryInfo>();
                for (int i = 0; i < a.length(); i++) {
                    JSONObject o = a.getJSONObject(i);
                    if (o.isNull("parentId")) {
                        CategoryInfo cat = new CategoryInfo(o);
                        rootCats.add(cat);
                    }
                }
                return rootCats;
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new BasicException(e);
        }
    }

    /** Get subcategories from parent ID */
    public final List<CategoryInfo> getSubcategories(String category) throws BasicException  {
        try {
            ServerLoader loader = new ServerLoader();
            ServerLoader.Response r = loader.read("CategoriesAPI",
                    "getChildren", "parentId", category);
            if (r.getStatus().equals(ServerLoader.Response.STATUS_OK)) {
                JSONArray a = r.getArrayContent();
                List<CategoryInfo> rootCats = new ArrayList<CategoryInfo>();
                for (int i = 0; i < a.length(); i++) {
                    JSONObject o = a.getJSONObject(i);
                    if (!o.isNull("parentId")) {
                        CategoryInfo cat = new CategoryInfo(o);
                        rootCats.add(cat);
                    }
                }
                return rootCats;
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new BasicException(e);
        }
    }

    //Subgrupos de una composición
    public final List<SubgroupInfo> getSubgroups(String composition) throws BasicException  {
        return new PreparedSentence(s
            , "SELECT ID, NAME, IMAGE, DISPORDER FROM SUBGROUPS WHERE COMPOSITION = ? ORDER BY DISPORDER, NAME"
            , SerializerWriteString.INSTANCE
            , new SerializerReadClass(SubgroupInfo.class)).list(composition);
    }

    //Productos de un grupo de tarifas
    public final List<ProductInfoExt> getTariffProds(String area) throws BasicException  {
        return new PreparedSentence(s
            , "SELECT P.ID, P.REFERENCE, P.CODE, P.NAME, P.ISCOM, P.ISSCALE, "
            + "P.PRICEBUY, TAP.PRICESELL, P.TAXCAT, P.CATEGORY, "
            + "P.ATTRIBUTESET_ID, P.IMAGE, P.ATTRIBUTES, P.DISCOUNTENABLED, "
            + "P.DISCOUNTRATE " +
              "FROM PRODUCTS P " +
              "		LEFT OUTER JOIN TARIFFAREAS_PROD TAP ON P.ID = TAP.PRODUCTID " +
              "		LEFT OUTER JOIN TARIFFAREAS TA ON TA.ID = TAP.TARIFFID " +
              "WHERE TA.ID = ? ORDER BY P.NAME"
            , SerializerWriteString.INSTANCE
            , ProductInfoExt.getSerializerRead()).list(area);
    }

    //Producto de un subgrupo de una composicion
    public final List<ProductInfoExt> getSubgroupCatalog(Integer subgroup) throws BasicException  {
            return new PreparedSentence(s
            , "SELECT P.ID, P.REFERENCE, P.CODE, P.NAME, P.ISCOM, P.ISSCALE, "
            + "P.PRICEBUY, P.PRICESELL, P.TAXCAT, P.CATEGORY, "
            + "P.ATTRIBUTESET_ID, P.IMAGE, P.ATTRIBUTES, P.DISCOUNTENABLED, "
            + "P.DISCOUNTRATE "
            + "FROM SUBGROUPS S INNER JOIN SUBGROUPS_PROD SP ON S.ID = SP.SUBGROUP "
            + "LEFT OUTER JOIN PRODUCTS P ON SP.PRODUCT = P.ID "
            + "WHERE S.ID = ? "
            + "ORDER BY P.NAME"
            , SerializerWriteString.INSTANCE
            , ProductInfoExt.getSerializerRead()).list(subgroup.toString());
    }

    public final List<CategoryInfo> getCategoryComposition() throws BasicException {
        try {
            ServerLoader loader = new ServerLoader();
            ServerLoader.Response r = loader.read("CategoriesAPI", "get",
                    "id", "0");
            if (r.getStatus().equals(ServerLoader.Response.STATUS_OK)) {
                JSONObject o = r.getObjContent();
                List<CategoryInfo> list = new ArrayList<CategoryInfo>();
                list.add(new CategoryInfo(o));
                return list;
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new BasicException(e);
        }
    }

    /** Get products from a category ID */
    public List<ProductInfoExt> getProductCatalog(String category, int pos) throws BasicException  {
        try {
            ServerLoader loader = new ServerLoader();
            ServerLoader.Response r = loader.read("ProductsAPI",
                    "getCategory", "id", category);
            if (r.getStatus().equals(ServerLoader.Response.STATUS_OK)) {
                JSONArray a = r.getArrayContent();
                List<ProductInfoExt> prds = new ArrayList<ProductInfoExt>();
                for (int i = 0; i < a.length(); i++) {
                    JSONObject o = a.getJSONObject(i);
                    prds.add(new ProductInfoExt(o));
                }
                return prds;
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new BasicException(e);
        }
    }

    /** Get products associated to a first one by ID */
    public List<ProductInfoExt> getProductComments(String id) throws BasicException {
        return new ArrayList<ProductInfoExt>();
        // TODO: enable product comments
        /*return new PreparedSentence(s,
            "SELECT P.ID, P.REFERENCE, P.CODE, P.NAME, P.ISCOM, P.ISSCALE, "
            + "P.PRICEBUY, P.PRICESELL, P.TAXCAT, P.CATEGORY, "
            + "P.ATTRIBUTESET_ID, P.IMAGE, P.ATTRIBUTES, P.DISCOUNTENABLED, "
            + "P.DISCOUNTRATE "
            + "FROM PRODUCTS P, PRODUCTS_CAT O, PRODUCTS_COM M "
            + "WHERE P.ID = O.PRODUCT AND P.ID = M.PRODUCT2 "
            + "AND M.PRODUCT = ? AND P.ISCOM = " + s.DB.TRUE() + " "
            + "ORDER BY O.CATORDER, P.NAME",
            SerializerWriteString.INSTANCE,
            ProductInfoExt.getSerializerRead()).list(id);*/
    }
  
    /** Get search all products query */
    public final SentenceList getProductList() {
        return new StaticSentence(s,
            new QBFBuilder(
                "SELECT ID, REFERENCE, CODE, NAME, ISCOM, ISSCALE, PRICEBUY, "
                + "PRICESELL, TAXCAT, CATEGORY, ATTRIBUTESET_ID, IMAGE, "
                + "ATTRIBUTES, DISCOUNTENABLED, DISCOUNTRATE "
                + "FROM PRODUCTS "
                + "WHERE ?(QBF_FILTER) AND CATEGORY != '0' "
                + "ORDER BY REFERENCE",
                new String[] {"NAME", "PRICEBUY", "PRICESELL", "CATEGORY",
                    "CODE"}),
            new SerializerWriteBasic(new Datas[] {Datas.OBJECT, Datas.STRING,
                Datas.OBJECT, Datas.DOUBLE, Datas.OBJECT, Datas.DOUBLE,
                Datas.OBJECT, Datas.STRING, Datas.OBJECT, Datas.STRING}),
            ProductInfoExt.getSerializerRead());
    }
    
    /** Get search products query of regular products */
    public SentenceList getProductListNormal() {
        return new StaticSentence(s,
            new QBFBuilder(
                "SELECT ID, REFERENCE, CODE, NAME, ISCOM, ISSCALE, PRICEBUY, "
                + "PRICESELL, TAXCAT, CATEGORY, ATTRIBUTESET_ID, IMAGE, "
                + "ATTRIBUTES, DISCOUNTENABLED, DISCOUNTRATE "
                + "FROM PRODUCTS "
                + "WHERE ISCOM = " + s.DB.FALSE() + " AND ?(QBF_FILTER) "
                + "AND CATEGORY != '0' "
                + "ORDER BY REFERENCE",
                new String[] {"NAME", "PRICEBUY", "PRICESELL", "CATEGORY",
                    "CODE"}),
            new SerializerWriteBasic(new Datas[] {Datas.OBJECT, Datas.STRING,
                Datas.OBJECT, Datas.DOUBLE, Datas.OBJECT, Datas.DOUBLE,
                Datas.OBJECT, Datas.STRING, Datas.OBJECT, Datas.STRING}),
            ProductInfoExt.getSerializerRead());
    }
    
    /** Get search products query of auxilliar products */
    public SentenceList getProductListAuxiliar() {
         return new StaticSentence(s,
            new QBFBuilder(
                "SELECT ID, REFERENCE, CODE, NAME, ISCOM, ISSCALE, PRICEBUY, "
                + "PRICESELL, TAXCAT, CATEGORY, ATTRIBUTESET_ID, IMAGE, "
                + "ATTRIBUTES, DISCOUNTENABLED, DISCOUNTRATE "
                + "FROM PRODUCTS WHERE ISCOM = " + s.DB.TRUE()
                + " AND ?(QBF_FILTER) AND CATEGORY != '0' "
                + "ORDER BY REFERENCE",
                new String[] {"NAME", "PRICEBUY", "PRICESELL", "CATEGORY",
                    "CODE"}),
            new SerializerWriteBasic(new Datas[] {Datas.OBJECT, Datas.STRING,
                Datas.OBJECT, Datas.DOUBLE, Datas.OBJECT, Datas.DOUBLE,
                Datas.OBJECT, Datas.STRING, Datas.OBJECT, Datas.STRING}),
            ProductInfoExt.getSerializerRead());
    }

    public final SentenceList getCompositionList() {
        return new StaticSentence(s
            , new QBFBuilder("SELECT P.ID, P.REFERENCE, P.CODE, P.NAME, "
            + "P.ISCOM, P.ISSCALE, P.PRICEBUY, P.PRICESELL, P.TAXCAT, "
            + "P.CATEGORY, P.ATTRIBUTESET_ID, P.IMAGE, "
            + "P.ATTRIBUTES, P.DISCOUNTENABLED, P.DISCOUNTRATE "
            + "FROM PRODUCTS P, CATEGORIES C "
            + "WHERE P.CATEGORY = C.ID AND C.ID LIKE '0' "
            + "AND ?(QBF_FILTER) ORDER BY P.NAME"
            , new String[] {"P.NAME", "P.PRICESELL", "P.CODE"})
            , new SerializerWriteBasic(new Datas[] {Datas.OBJECT, Datas.STRING, Datas.OBJECT, Datas.DOUBLE, Datas.OBJECT, Datas.STRING})
            , new SerializerReadClass(ProductInfoExt.class));
    }

    //Tickets and Receipt list
    public SentenceList getTicketsList() {
         return new StaticSentence(s,
            new QBFBuilder(
                "SELECT T.TICKETID, T.TICKETTYPE, R.DATENEW, P.NAME, C.NAME, "
                + "SUM(PM.TOTAL) "
                + "FROM RECEIPTS R JOIN TICKETS T ON R.ID = T.ID "
                + "LEFT OUTER JOIN PAYMENTS PM ON R.ID = PM.RECEIPT "
                + "LEFT OUTER JOIN CUSTOMERS C ON C.ID = T.CUSTOMER "
                + "LEFT OUTER JOIN PEOPLE P ON T.PERSON = P.ID "
                + "WHERE ?(QBF_FILTER) "
                + "GROUP BY T.ID, T.TICKETID, T.TICKETTYPE, R.DATENEW, "
                + "P.NAME, C.NAME ORDER BY R.DATENEW DESC, T.TICKETID",
                new String[] {"T.TICKETID", "T.TICKETTYPE", "PM.TOTAL",
                    "R.DATENEW", "R.DATENEW", "P.NAME", "C.NAME"}),
            new SerializerWriteBasic(new Datas[] {Datas.OBJECT, Datas.INT,
                Datas.OBJECT, Datas.INT, Datas.OBJECT, Datas.DOUBLE,
                Datas.OBJECT, Datas.TIMESTAMP, Datas.OBJECT, Datas.TIMESTAMP,
                Datas.OBJECT, Datas.STRING, Datas.OBJECT, Datas.STRING}),
            new SerializerReadClass(FindTicketsInfo.class));
    }

    public SentenceList getLast10Tickets() {
         return new StaticSentence(s,
            new QBFBuilder(
                "SELECT T.TICKETID, T.TICKETTYPE, R.DATENEW, P.NAME, C.NAME, "
                + "SUM(PM.TOTAL) "
                + "FROM RECEIPTS R JOIN TICKETS T ON R.ID = T.ID "
                + "LEFT OUTER JOIN PAYMENTS PM ON R.ID = PM.RECEIPT "
                + "LEFT OUTER JOIN CUSTOMERS C ON C.ID = T.CUSTOMER "
                + "LEFT OUTER JOIN PEOPLE P ON T.PERSON = P.ID "
                + "WHERE ?(QBF_FILTER) "
                + "GROUP BY T.ID, T.TICKETID, T.TICKETTYPE, R.DATENEW, "
                + "P.NAME, C.NAME ORDER BY R.DATENEW DESC, T.TICKETID "
                + "LIMIT 10",
                new String[] {"T.TICKETID", "T.TICKETTYPE", "PM.TOTAL",
                    "R.DATENEW", "R.DATENEW", "P.NAME", "C.NAME"}),
            new SerializerWriteBasic(new Datas[] {Datas.OBJECT, Datas.INT,
                Datas.OBJECT, Datas.INT, Datas.OBJECT, Datas.DOUBLE,
                Datas.OBJECT, Datas.TIMESTAMP, Datas.OBJECT, Datas.TIMESTAMP,
                Datas.OBJECT, Datas.STRING, Datas.OBJECT, Datas.STRING}),
            new SerializerReadClass(FindTicketsInfo.class));
    }
    
    //User list
    public final SentenceList getUserList() {
        return new StaticSentence(s
            , "SELECT ID, NAME FROM PEOPLE ORDER BY NAME"
            , null
            , new SerializerRead() { public Object readValues(DataRead dr) throws BasicException {
                return new TaxCategoryInfo(
                        dr.getString(1), 
                        dr.getString(2));
            }});
    }
   
    // Listados para combo
    public final List<TaxInfo> getTaxList() throws BasicException {
        try {
            ServerLoader loader = new ServerLoader();
            ServerLoader.Response r = loader.read("TaxesAPI", "getAll");
            if (r.getStatus().equals(ServerLoader.Response.STATUS_OK)) {
                JSONArray a = r.getArrayContent();
                List<TaxInfo> taxes = new ArrayList<TaxInfo>();
                for (int i = 0; i < a.length(); i++) {
                    JSONObject o = a.getJSONObject(i);
                    JSONArray a2 = o.getJSONArray("taxes");
                    for (int j = 0; j < a2.length(); j++) {
                        JSONObject o2 = a2.getJSONObject(j);
                        TaxInfo tax = new TaxInfo(o2);
                        taxes.add(tax);
                    }
                }
                return taxes;
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new BasicException(e);
        }
    }

    public final SentenceList getCategoriesList() {
        return new StaticSentence(s
            , "SELECT ID, NAME, IMAGE FROM CATEGORIES "
            + "WHERE ID != '0' ORDER BY NAME"
            , null
            , CategoryInfo.getSerializerRead());
    }
    public final SentenceList getAllCategoriesList() {
        return new StaticSentence(s
            , "SELECT ID, NAME, IMAGE FROM CATEGORIES "
            + "ORDER BY NAME"
            , null
            , CategoryInfo.getSerializerRead());
    }
    public final SentenceList getTaxCustCategoriesList() {
        return new StaticSentence(s
            , "SELECT ID, NAME FROM TAXCUSTCATEGORIES ORDER BY NAME"
            , null
            , new SerializerRead() { public Object readValues(DataRead dr) throws BasicException {
                return new TaxCustCategoryInfo(dr.getString(1), dr.getString(2));
            }});
    }

    public final List<TaxCategoryInfo> getTaxCategoriesList()
        throws BasicException {
        try {
            ServerLoader loader = new ServerLoader();
            ServerLoader.Response r = loader.read("TaxesAPI", "getAll");
            if (r.getStatus().equals(ServerLoader.Response.STATUS_OK)) {
                JSONArray a = r.getArrayContent();
                List<TaxCategoryInfo> taxCats = new ArrayList<TaxCategoryInfo>();
                for (int i = 0; i < a.length(); i++) {
                    JSONObject o = a.getJSONObject(i);
                    TaxCategoryInfo taxCat = new TaxCategoryInfo(o);
                    taxCats.add(taxCat);
                }
                return taxCats;
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new BasicException(e);
        }
    }

    public final SentenceList getAttributeSetList() {
        return new StaticSentence(s
            , "SELECT ID, NAME FROM ATTRIBUTESET ORDER BY NAME"
            , null
            , new SerializerRead() { public Object readValues(DataRead dr) throws BasicException {
                return new AttributeSetInfo(dr.getString(1), dr.getString(2));
            }});
    }
    public final SentenceList getLocationsList() {
        return new StaticSentence(s
            , "SELECT ID, NAME, ADDRESS FROM LOCATIONS ORDER BY NAME"
            , null
            , new SerializerReadClass(LocationInfo.class));
    }
    public final SentenceList getFloorsList() {
        return new StaticSentence(s
            , "SELECT ID, NAME FROM FLOORS ORDER BY NAME"
            , null
            , new SerializerReadClass(FloorsInfo.class));
    }

    public final List<CurrencyInfo> getCurrenciesList() throws BasicException {
        try {
            ServerLoader loader = new ServerLoader();
            ServerLoader.Response r = loader.read("CurrenciesAPI", "getAll");
            if (r.getStatus().equals(ServerLoader.Response.STATUS_OK)) {
                JSONArray a = r.getArrayContent();
                List<CurrencyInfo> currencies = new ArrayList<CurrencyInfo>();
                for (int i = 0; i < a.length(); i++) {
                    JSONObject o = a.getJSONObject(i);
                    CurrencyInfo currency = new CurrencyInfo(o);
                    currencies.add(currency);
                }
                return currencies;
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new BasicException(e);
        }
    }

    public CurrencyInfo getCurrency(int currencyId) throws BasicException {
        try {
            ServerLoader loader = new ServerLoader();
            ServerLoader.Response r = loader.read("CurrenciesAPI", "get",
                    "id", String.valueOf(currencyId));
            if (r.getStatus().equals(ServerLoader.Response.STATUS_OK)) {
                JSONObject o = r.getObjContent();
                CurrencyInfo currency = new CurrencyInfo(o);
                return currency;
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new BasicException(e);
        }
    }

    public CurrencyInfo getMainCurrency() throws BasicException {
        try {
            ServerLoader loader = new ServerLoader();
            ServerLoader.Response r = loader.read("CurrenciesAPI", "getMain");
            if (r.getStatus().equals(ServerLoader.Response.STATUS_OK)) {
                JSONObject o = r.getObjContent();
                CurrencyInfo currency = new CurrencyInfo(o);
                return currency;
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new BasicException(e);
        }
    }

    public CustomerInfoExt findCustomerExt(String card) throws BasicException {
        return (CustomerInfoExt) new PreparedSentence(s,
                "SELECT ID, TAXID, SEARCHKEY, NAME, CARD, TAXCATEGORY, "
                + "NOTES, MAXDEBT, VISIBLE, CURDATE, CURDEBT, PREPAID, "
                + "FIRSTNAME, LASTNAME, EMAIL, PHONE, PHONE2, FAX, "
                + "ADDRESS, ADDRESS2, POSTAL, CITY, REGION, COUNTRY "
                + "FROM CUSTOMERS "
                + "WHERE CARD = ? AND VISIBLE = " + s.DB.TRUE(),
                SerializerWriteString.INSTANCE,
                new CustomerExtRead()).find(card);
    }

    public CustomerInfoExt loadCustomerExt(String id) throws BasicException {
        return (CustomerInfoExt) new PreparedSentence(s,
                "SELECT ID, TAXID, SEARCHKEY, NAME, CARD, TAXCATEGORY, NOTES, "
                + "MAXDEBT, VISIBLE, CURDATE, CURDEBT, PREPAID, "
                + "FIRSTNAME, LASTNAME, EMAIL, PHONE, PHONE2, FAX, "
                + "ADDRESS, ADDRESS2, POSTAL, CITY, REGION, COUNTRY "
                + "FROM CUSTOMERS "
                + "WHERE ID = ?",
                SerializerWriteString.INSTANCE,
                new CustomerExtRead()).find(id);
    }

    public final boolean isCashActive(String id) throws BasicException {

        return new PreparedSentence(s,
                "SELECT MONEY FROM CLOSEDCASH WHERE DATEEND IS NULL AND MONEY = ?",
                SerializerWriteString.INSTANCE,
                SerializerReadString.INSTANCE).find(id)
            != null;
    }

    private boolean isRefill(String productId) throws BasicException {
        return new PreparedSentence(s,
                "SELECT ID FROM PRODUCTS WHERE ID= ? AND CATEGORY = ?",
                new SerializerWriteBasic(Datas.STRING, Datas.STRING),
                SerializerReadString.INSTANCE).find(productId, "-1")
                != null;
    }
    private void addPrepaid(final String customerId, final double amount)
        throws BasicException {
        PreparedSentence ps = new PreparedSentence(s,
                "UPDATE CUSTOMERS SET PREPAID = (PREPAID + ?) "
                + "WHERE ID = ?",
                SerializerWriteParams.INSTANCE);
        ps.exec(new DataParams() {
                public void writeValues() throws BasicException {
                    setDouble(1, amount);
                    setString(2, customerId);
                }
            });
    }

    public final TicketInfo loadTicket(final int tickettype, final int ticketid) throws BasicException {
        TicketInfo ticket = (TicketInfo) new PreparedSentence(s,
                "SELECT T.ID, T.TICKETTYPE, T.TICKETID, R.DATENEW, R.MONEY, "
                + "R.ATTRIBUTES, P.ID, P.NAME, T.CUSTOMER, T.CUSTCOUNT "
                + "FROM RECEIPTS R JOIN TICKETS T ON R.ID = T.ID "
                + "LEFT OUTER JOIN PEOPLE P ON T.PERSON = P.ID "
                + "WHERE T.TICKETTYPE = ? AND T.TICKETID = ?",
                SerializerWriteParams.INSTANCE,
                new SerializerReadClass(TicketInfo.class))
                .find(new DataParams() { public void writeValues() throws BasicException {
                    setInt(1, tickettype);
                    setInt(2, ticketid);
                }});
        if (ticket != null) {

            String customerid = ticket.getCustomerId();
            ticket.setCustomer(customerid == null
                    ? null
                    : loadCustomerExt(customerid));

            ticket.setLines(new PreparedSentence(s
                , "SELECT L.TICKET, L.LINE, L.PRODUCT, L.ATTRIBUTESETINSTANCE_ID, L.UNITS, L.PRICE, T.ID, T.NAME, T.CATEGORY, T.VALIDFROM, T.CUSTCATEGORY, T.PARENTID, T.RATE, T.RATECASCADE, T.RATEORDER, L.ATTRIBUTES " +
                  "FROM TICKETLINES L, TAXES T WHERE L.TAXID = T.ID AND L.TICKET = ? ORDER BY L.LINE"
                , SerializerWriteString.INSTANCE
                , new SerializerReadClass(TicketLineInfo.class)).list(ticket.getId()));
            ticket.setPayments(new PreparedSentence(s
                , "SELECT PAYMENT, CURRENCY, TOTALCURRENCY, TRANSID FROM PAYMENTS WHERE RECEIPT = ?"
                , SerializerWriteString.INSTANCE
                , new SerializerRead() {
                        public Object readValues(DataRead dr) throws BasicException {
                            String name = dr.getString(1);
                            double amount = dr.getDouble(3).doubleValue();
                            String transactionID = dr.getString(4);
                            CurrencyInfo currency = getCurrency(dr.getInt(2).intValue());
                            return new PaymentInfoTicket(amount, currency, name, transactionID);
                        }
                 }).list(ticket.getId()));
        }
        return ticket;
    }

    public final void saveTicket(final TicketInfo ticket, final String location) throws BasicException {

        Transaction t = new Transaction(s) {
            public Object transact() throws BasicException {

                // Set Receipt Id
                if (ticket.getTicketId() == 0) {
                    switch (ticket.getTicketType()) {
                        case TicketInfo.RECEIPT_NORMAL:
                            ticket.setTicketId(getNextTicketIndex().intValue());
                            break;
                        case TicketInfo.RECEIPT_REFUND:
                            ticket.setTicketId(getNextTicketRefundIndex().intValue());
                            break;
                        case TicketInfo.RECEIPT_PAYMENT:
                            ticket.setTicketId(getNextTicketPaymentIndex().intValue());
                            break;
                        default:
                            throw new BasicException();
                    }
                }

                // new receipt
                new PreparedSentence(s
                    , "INSERT INTO RECEIPTS (ID, MONEY, DATENEW, ATTRIBUTES) VALUES (?, ?, ?, ?)"
                    , SerializerWriteParams.INSTANCE
                    ).exec(new DataParams() { public void writeValues() throws BasicException {
                        setString(1, ticket.getId());
                        setString(2, ticket.getActiveCash());
                        setTimestamp(3, ticket.getDate());
                        try {
                            ByteArrayOutputStream o = new ByteArrayOutputStream();
                            ticket.getProperties().storeToXML(o, AppLocal.APP_NAME, "UTF-8");
                            setBytes(4, o.toByteArray());
                        } catch (IOException e) {
                            setBytes(4, null);
                        }
                    }});

                // new ticket
                new PreparedSentence(s,
                    "INSERT INTO TICKETS (ID, TICKETTYPE, TICKETID, PERSON, "
                    + "CUSTOMER, CUSTCOUNT) VALUES (?, ?, ?, ?, ?, ?)",
                    SerializerWriteParams.INSTANCE
                    ).exec(new DataParams() { public void writeValues() throws BasicException {
                        setString(1, ticket.getId());
                        setInt(2, ticket.getTicketType());
                        setInt(3, ticket.getTicketId());
                        setString(4, ticket.getUser().getId());
                        setString(5, ticket.getCustomerId());
                        setInt(6, ticket.getCustomersCount());
                    }});

                SentenceExec ticketlineinsert = new PreparedSentence(s
                    , "INSERT INTO TICKETLINES (TICKET, LINE, PRODUCT, ATTRIBUTESETINSTANCE_ID, UNITS, PRICE, TAXID, ATTRIBUTES) VALUES (?, ?, ?, ?, ?, ?, ?, ?)"
                    , SerializerWriteBuilder.INSTANCE);

                for (TicketLineInfo l : ticket.getLines()) {
                    ticketlineinsert.exec(l);
                    if (l.getProductID() != null)  {
                        // update the stock
                        getStockDiaryInsert().exec(new Object[] {
                            UUID.randomUUID().toString(),
                            ticket.getDate(),
                            l.getMultiply() < 0.0
                                ? MovementReason.IN_REFUND.getKey()
                                : MovementReason.OUT_SALE.getKey(),
                            location,
                            l.getProductID(),
                            l.getProductAttSetInstId(),
                            new Double(-l.getMultiply()),
                            new Double(l.getPrice())
                        });
                        // Update prepaid
                        if (isRefill(l.getProductID())) {
                            addPrepaid(ticket.getCustomerId(),
                                    l.getPrice());
                        }
                    }
                }

                SentenceExec paymentinsert = new PreparedSentence(s
                    , "INSERT INTO PAYMENTS (ID, RECEIPT, PAYMENT, TOTAL, CURRENCY, TOTALCURRENCY, TRANSID, RETURNMSG) VALUES (?, ?, ?, ?, ?, ?, ?, ?)"
                    , SerializerWriteParams.INSTANCE);
                for (final PaymentInfo p : ticket.getPayments()) {
                    final CurrencyInfo currency;
                    if (p.getCurrency() != null) {
                        currency = p.getCurrency();
                    } else {
                        currency = getMainCurrency();
                    }
                    final double total;
                    if (!currency.isMain()) {
                        total = p.getTotal() / currency.getRate();
                    } else {
                        total = p.getTotal();
                    }
                    paymentinsert.exec(new DataParams() { public void writeValues() throws BasicException {
                        setString(1, UUID.randomUUID().toString());
                        setString(2, ticket.getId());
                        setString(3, p.getName());
                        setDouble(4, total);
                        setInt(5, currency.getID());
                        setDouble(6, p.getTotal());
                        setString(7, ticket.getTransactionID());
                        setBytes(8, (byte[]) Formats.BYTEA.parseValue(ticket.getReturnMessage()));
                    }});

                    if ("debt".equals(p.getName()) || "debtpaid".equals(p.getName())) {

                        // udate customer fields...
                        ticket.getCustomer().updateCurDebt(p.getTotal(), ticket.getDate());

                        // save customer fields...
                        getDebtUpdate().exec(new DataParams() { public void writeValues() throws BasicException {
                            setDouble(1, ticket.getCustomer().getCurdebt());
                            setTimestamp(2, ticket.getCustomer().getCurdate());
                            setString(3, ticket.getCustomer().getId());
                        }});
                    } else if ("prepaid".equals(p.getName())) {
                        // Decrease prepaid amount
                        addPrepaid(ticket.getCustomer().getId(), -p.getTotal());
                    }
                }

                SentenceExec taxlinesinsert = new PreparedSentence(s
                        , "INSERT INTO TAXLINES (ID, RECEIPT, TAXID, BASE, AMOUNT)  VALUES (?, ?, ?, ?, ?)"
                        , SerializerWriteParams.INSTANCE);
                if (ticket.getTaxes() != null) {
                    for (final TicketTaxInfo tickettax: ticket.getTaxes()) {
                        taxlinesinsert.exec(new DataParams() { public void writeValues() throws BasicException {
                            setString(1, UUID.randomUUID().toString());
                            setString(2, ticket.getId());
                            setString(3, tickettax.getTaxInfo().getId());
                            setDouble(4, tickettax.getSubTotal());
                            setDouble(5, tickettax.getTax());
                        }});
                    }
                }

                return null;
            }
        };
        t.execute();
    }

    public final void deleteTicket(final TicketInfo ticket, final String location) throws BasicException {

        Transaction t = new Transaction(s) {
            public Object transact() throws BasicException {

                // update the inventory
                Date d = new Date();
                for (int i = 0; i < ticket.getLinesCount(); i++) {
                    if (ticket.getLine(i).getProductID() != null)  {
                        // Hay que actualizar el stock si el hay producto
                        getStockDiaryInsert().exec( new Object[] {
                            UUID.randomUUID().toString(),
                            d,
                            ticket.getLine(i).getMultiply() >= 0.0
                                ? MovementReason.IN_REFUND.getKey()
                                : MovementReason.OUT_SALE.getKey(),
                            location,
                            ticket.getLine(i).getProductID(),
                            ticket.getLine(i).getProductAttSetInstId(),
                            new Double(ticket.getLine(i).getMultiply()),
                            new Double(ticket.getLine(i).getPrice())
                        });
                    }
                }

                // update customer debts
                for (PaymentInfo p : ticket.getPayments()) {
                    if ("debt".equals(p.getName()) || "debtpaid".equals(p.getName())) {

                        // udate customer fields...
                        ticket.getCustomer().updateCurDebt(-p.getTotal(), ticket.getDate());

                         // save customer fields...
                        getDebtUpdate().exec(new DataParams() { public void writeValues() throws BasicException {
                            setDouble(1, ticket.getCustomer().getCurdebt());
                            setTimestamp(2, ticket.getCustomer().getCurdate());
                            setString(3, ticket.getCustomer().getId());
                        }});
                    }
                }

                // and delete the receipt
                new StaticSentence(s
                    , "DELETE FROM TAXLINES WHERE RECEIPT = ?"
                    , SerializerWriteString.INSTANCE).exec(ticket.getId());
                new StaticSentence(s
                    , "DELETE FROM PAYMENTS WHERE RECEIPT = ?"
                    , SerializerWriteString.INSTANCE).exec(ticket.getId());
                new StaticSentence(s
                    , "DELETE FROM TICKETLINES WHERE TICKET = ?"
                    , SerializerWriteString.INSTANCE).exec(ticket.getId());
                new StaticSentence(s
                    , "DELETE FROM TICKETS WHERE ID = ?"
                    , SerializerWriteString.INSTANCE).exec(ticket.getId());
                new StaticSentence(s
                    , "DELETE FROM RECEIPTS WHERE ID = ?"
                    , SerializerWriteString.INSTANCE).exec(ticket.getId());
                return null;
            }
        };
        t.execute();
    }

    public final Integer getNextTicketIndex() throws BasicException {
        return (Integer) s.DB.getSequenceSentence(s, "TICKETSNUM").find();
    }

    public final Integer getNextTicketRefundIndex() throws BasicException {
        return (Integer) s.DB.getSequenceSentence(s, "TICKETSNUM_REFUND").find();
    }

    public final Integer getNextTicketPaymentIndex() throws BasicException {
        return (Integer) s.DB.getSequenceSentence(s, "TICKETSNUM_PAYMENT").find();
    }

    public final SentenceList getCompositionQBF() {
        return new StaticSentence(s
            , new QBFBuilder("SELECT P.ID, P.REFERENCE, P.CODE, P.NAME, "
            + "P.ISCOM, P.ISSCALE, P.PRICEBUY, P.PRICESELL, P.CATEGORY, P.TAXCAT, "
            + "P.IMAGE, "
            + "CASE WHEN C.PRODUCT IS NULL THEN " + s.DB.FALSE()
            + " ELSE " + s.DB.TRUE() + " END, "
            + "C.CATORDER, P.ATTRIBUTES "
            + "FROM PRODUCTS P LEFT OUTER JOIN PRODUCTS_CAT C ON P.ID = C.PRODUCT "
            + "WHERE P.CATEGORY LIKE '0' AND ?(QBF_FILTER) "
            + "ORDER BY P.NAME",
            new String[] {"P.NAME", "P.PRICESELL", "P.CODE"})
            , new SerializerWriteBasic(new Datas[] {Datas.OBJECT, Datas.STRING, Datas.OBJECT, Datas.DOUBLE, Datas.OBJECT, Datas.STRING})
            , new SerializerReadBasic(compositionDatas));
    }

    /** Get query of ordered products of a category */
    public final SentenceList getProductCatQBF() {
        return new StaticSentence(s,
            new QBFBuilder(
                "SELECT P.ID, P.REFERENCE, P.CODE, P.NAME, P.ISCOM, P.ISSCALE, "
                + "P.PRICEBUY, P.PRICESELL, P.CATEGORY, P.TAXCAT, "
                + "P.ATTRIBUTESET_ID, P.IMAGE, P.STOCKCOST, P.STOCKVOLUME, "
                + "CASE WHEN C.PRODUCT IS NULL THEN " + s.DB.FALSE()
                + " ELSE " + s.DB.TRUE() + " END, "
                + "C.CATORDER, P.ATTRIBUTES, P.DISCOUNTENABLED, P.DISCOUNTRATE "
                + "FROM PRODUCTS P LEFT OUTER JOIN PRODUCTS_CAT C "
                + "ON P.ID = C.PRODUCT "
                + "WHERE P.CATEGORY != '0' AND ?(QBF_FILTER) "
                + "ORDER BY P.REFERENCE",
                new String[] {"P.NAME", "P.PRICEBUY", "P.PRICESELL",
                    "P.CATEGORY", "P.CODE"}),
            new SerializerWriteBasic(new Datas[] {Datas.OBJECT, Datas.STRING,
                Datas.OBJECT, Datas.DOUBLE, Datas.OBJECT, Datas.DOUBLE,
                Datas.OBJECT, Datas.STRING, Datas.OBJECT, Datas.STRING}),
            productsRow.getSerializerRead());
    }

    public final List<TariffInfo> getTariffAreaList() throws BasicException {
        try {
            ServerLoader loader = new ServerLoader();
            ServerLoader.Response r = loader.read("TariffAreasAPI", "getAll");
            if (r.getStatus().equals(ServerLoader.Response.STATUS_OK)) {
                JSONArray a = r.getArrayContent();
                List<TariffInfo> areas = new ArrayList<TariffInfo>();
                for (int i = 0; i < a.length(); i++) {
                    JSONObject o = a.getJSONObject(i);
                    TariffInfo area = new TariffInfo(o);
                    areas.add(area);
                }
                return areas;
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new BasicException(e);
        }
    }

    public final SentenceExec getDebtUpdate() {

        return new PreparedSentence(s
                , "UPDATE CUSTOMERS SET CURDEBT = ?, CURDATE = ? WHERE ID = ?"
                , SerializerWriteParams.INSTANCE);
    }

    public final SentenceExec getStockDiaryInsert() {
        return new SentenceExecTransaction(s) {
            public int execInTransaction(Object params) throws BasicException {
                int updateresult = 0;
                if (((Object[]) params)[2].equals(new Integer(0))) {
                    // Reset stock before insert
                    updateresult = ((Object[]) params)[5] == null // si ATTRIBUTESETINSTANCE_ID is null
                    ? new PreparedSentence(s,
                        "UPDATE STOCKCURRENT SET UNITS = (0) "
                        + "WHERE LOCATION = ? AND PRODUCT = ? "
                        + "AND ATTRIBUTESETINSTANCE_ID IS NULL",
                        new SerializerWriteBasicExt(stockdiaryDatas,
                            new int[] {3, 4})).exec(params)
                    : new PreparedSentence(s,
                        "UPDATE STOCKCURRENT SET UNITS = (0) "
                        + "WHERE LOCATION = ? AND PRODUCT = ? "
                        + "AND ATTRIBUTESETINSTANCE_ID = ?",
                        new SerializerWriteBasicExt(stockdiaryDatas,
                            new int[] {3, 4, 5})).exec(params);
                }
                updateresult = ((Object[]) params)[5] == null // si ATTRIBUTESETINSTANCE_ID is null
                    ? new PreparedSentence(s,
                        "UPDATE STOCKCURRENT SET UNITS = (UNITS + ?) "
                        + "WHERE LOCATION = ? AND PRODUCT = ? "
                        + "AND ATTRIBUTESETINSTANCE_ID IS NULL",
                        new SerializerWriteBasicExt(stockdiaryDatas,
                            new int[] {6, 3, 4})).exec(params)
                    : new PreparedSentence(s,
                        "UPDATE STOCKCURRENT SET UNITS = (UNITS + ?) "
                        + "WHERE LOCATION = ? AND PRODUCT = ? "
                        + "AND ATTRIBUTESETINSTANCE_ID = ?",
                        new SerializerWriteBasicExt(stockdiaryDatas,
                            new int[] {6, 3, 4, 5})).exec(params);
                if (updateresult == 0) {
                    new PreparedSentence(s
                        , "INSERT INTO STOCKCURRENT (LOCATION, PRODUCT, "
                        + "ATTRIBUTESETINSTANCE_ID, UNITS) "
                        + "VALUES (?, ?, ?, ?)",
                        new SerializerWriteBasicExt(stockdiaryDatas,
                            new int[] {3, 4, 5, 6})).exec(params);
                }
                return new PreparedSentence(s,
                    "INSERT INTO STOCKDIARY (ID, DATENEW, REASON, LOCATION, "
                    + "PRODUCT, ATTRIBUTESETINSTANCE_ID, UNITS, PRICE) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                    new SerializerWriteBasicExt(stockdiaryDatas,
                        new int[] {0, 1, 2, 3, 4, 5, 6, 7})).exec(params);
            }
        };
    }

    public final SentenceExec getStockDiaryDelete() {
        return new SentenceExecTransaction(s) {
            public int execInTransaction(Object params) throws BasicException {
                int updateresult = ((Object[]) params)[5] == null // if ATTRIBUTESETINSTANCE_ID is null
                        ? new PreparedSentence(s,
                            "UPDATE STOCKCURRENT SET UNITS = (UNITS - ?) "
                            + "WHERE LOCATION = ? AND PRODUCT = ? "
                            + "AND ATTRIBUTESETINSTANCE_ID IS NULL",
                            new SerializerWriteBasicExt(stockdiaryDatas,
                                new int[] {6, 3, 4})).exec(params)
                        : new PreparedSentence(s,
                            "UPDATE STOCKCURRENT SET UNITS = (UNITS - ?) "
                            + "WHERE LOCATION = ? AND PRODUCT = ? "
                            + "AND ATTRIBUTESETINSTANCE_ID = ?",
                            new SerializerWriteBasicExt(stockdiaryDatas,
                                new int[] {6, 3, 4, 5})).exec(params);

                if (updateresult == 0) {
                    new PreparedSentence(s,
                        "INSERT INTO STOCKCURRENT (LOCATION, PRODUCT, "
                        + "ATTRIBUTESETINSTANCE_ID, UNITS) "
                        + "VALUES (?, ?, ?, -(?))",
                        new SerializerWriteBasicExt(stockdiaryDatas,
                            new int[] {3, 4, 5, 6})).exec(params);
                }
                return new PreparedSentence(s,
                    "DELETE FROM STOCKDIARY WHERE ID = ?",
                    new SerializerWriteBasicExt(stockdiaryDatas,
                        new int[] {0})).exec(params);
            }
        };
    }

    public final SentenceExec getPaymentMovementInsert() {
        return new SentenceExecTransaction(s) {
            public int execInTransaction(Object params) throws BasicException {
                new PreparedSentence(s,
                    "INSERT INTO RECEIPTS (ID, MONEY, DATENEW) "
                    + "VALUES (?, ?, ?)",
                    new SerializerWriteBasicExt(paymenttabledatas,
                        new int[] {0, 1, 2})).exec(params);
                return new PreparedSentence(s,
                    "INSERT INTO PAYMENTS (ID, RECEIPT, PAYMENT, TOTAL, NOTES, CURRENCY, TOTALCURRENCY) "
                    + "VALUES (?, ?, ?, ?, ?, ?)",
                    new SerializerWriteBasicExt(paymenttabledatas,
                            new int[] {3, 0, 4, 5, 6, 7, 5})).exec(params);
            }
        };
    }

    public final SentenceExec getPaymentMovementDelete() {
        return new SentenceExecTransaction(s) {
            public int execInTransaction(Object params) throws BasicException {
                new PreparedSentence(s
                    , "DELETE FROM PAYMENTS WHERE ID = ?"
                    , new SerializerWriteBasicExt(paymenttabledatas, new int[] {3})).exec(params);
                return new PreparedSentence(s
                    , "DELETE FROM RECEIPTS WHERE ID = ?"
                    , new SerializerWriteBasicExt(paymenttabledatas, new int[] {0})).exec(params);
            }
        };
    }

    public final double findProductStock(String warehouse, String id, String attsetinstid) throws BasicException {

        PreparedSentence p = attsetinstid == null
                ? new PreparedSentence(s,
                    "SELECT UNITS FROM STOCKCURRENT "
                    + "WHERE LOCATION = ? AND PRODUCT = ? "
                    + "AND ATTRIBUTESETINSTANCE_ID IS NULL",
                    new SerializerWriteBasic(Datas.STRING, Datas.STRING),
                    SerializerReadDouble.INSTANCE)
                : new PreparedSentence(s,
                    "SELECT UNITS FROM STOCKCURRENT "
                    + "WHERE LOCATION = ? AND PRODUCT = ? "
                    + "AND ATTRIBUTESETINSTANCE_ID = ?",
                    new SerializerWriteBasic(Datas.STRING, Datas.STRING,
                        Datas.STRING),
                    SerializerReadDouble.INSTANCE);

        Double d = (Double) p.find(warehouse, id, attsetinstid);
        return d == null ? 0.0 : d.doubleValue();
    }

    protected static class CustomerExtRead implements SerializerRead {
        public Object readValues(DataRead dr) throws BasicException {
            CustomerInfoExt c = new CustomerInfoExt(dr.getString(1));
            c.setTaxid(dr.getString(2));
            c.setSearchkey(dr.getString(3));
            c.setName(dr.getString(4));
            c.setCard(dr.getString(5));
            c.setTaxCustomerID(dr.getString(6));
            c.setNotes(dr.getString(7));
            c.setMaxdebt(dr.getDouble(8));
            c.setVisible(dr.getBoolean(9).booleanValue());
            c.setCurdate(dr.getTimestamp(10));
            c.setCurdebt(dr.getDouble(11));
            c.setPrepaid(dr.getDouble(12));
            c.setFirstname(dr.getString(13));
            c.setLastname(dr.getString(14));
            c.setEmail(dr.getString(15));
            c.setPhone(dr.getString(16));
            c.setPhone2(dr.getString(17));
            c.setFax(dr.getString(18));
            c.setAddress(dr.getString(19));
            c.setAddress2(dr.getString(20));
            c.setPostal(dr.getString(21));
            c.setCity(dr.getString(22));
            c.setRegion(dr.getString(23));
            c.setCountry(dr.getString(24));

            return c;
        }
    }
}
