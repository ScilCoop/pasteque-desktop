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

import fr.pasteque.format.DateUtils;
import fr.pasteque.pos.ticket.CashSession;
import fr.pasteque.pos.ticket.CategoryInfo;
import fr.pasteque.pos.ticket.ProductInfoExt;
import fr.pasteque.pos.ticket.TaxInfo;
import fr.pasteque.pos.ticket.TicketInfo;
import fr.pasteque.pos.ticket.TicketLineInfo;
import fr.pasteque.pos.ticket.ZTicket;
import java.util.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import fr.pasteque.data.loader.*;
import fr.pasteque.format.Formats;
import fr.pasteque.basic.BasicException;
import fr.pasteque.data.model.Field;
import fr.pasteque.data.model.Row;
import fr.pasteque.pos.admin.CurrencyInfo;
import fr.pasteque.pos.caching.CatalogCache;
import fr.pasteque.pos.caching.CurrenciesCache;
import fr.pasteque.pos.caching.TariffAreasCache;
import fr.pasteque.pos.caching.TaxesCache;
import fr.pasteque.pos.customers.CustomerInfoExt;
import fr.pasteque.pos.customers.DataLogicCustomers;
import fr.pasteque.pos.inventory.AttributeSetInfo;
import fr.pasteque.pos.inventory.TaxCategoryInfo;
import fr.pasteque.pos.inventory.TaxCustCategoryInfo;
import fr.pasteque.pos.inventory.LocationInfo;
import fr.pasteque.pos.inventory.MovementReason;
import fr.pasteque.pos.inventory.TaxCategoryInfo;
import fr.pasteque.pos.mant.FloorsInfo;
import fr.pasteque.pos.payment.PaymentInfo;
import fr.pasteque.pos.payment.PaymentInfoTicket;
import fr.pasteque.pos.ticket.CashMove;
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

    private static Logger logger = Logger.getLogger("fr.pasteque.pos.forms.DatalogicSales");

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

    public boolean preloadCategories() {
        try {
            logger.log(Level.INFO, "Preloading categories");
            ServerLoader loader = new ServerLoader();
            ServerLoader.Response r = loader.read("CategoriesAPI", "getAll");
            if (r.getStatus().equals(ServerLoader.Response.STATUS_OK)) {
                JSONArray a = r.getArrayContent();
                List<CategoryInfo> categories = new ArrayList<CategoryInfo>();
                for (int i = 0; i < a.length(); i++) {
                    JSONObject o = a.getJSONObject(i);
                    CategoryInfo cat = new CategoryInfo(o);
                    categories.add(cat);
                }
                CatalogCache.refreshCategories(categories);
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    public boolean preloadProducts() {
        try {
            logger.log(Level.INFO, "Preloading products");
            ServerLoader loader = new ServerLoader();
            ServerLoader.Response r = loader.read("ProductsAPI", "getAll");
            if (r.getStatus().equals(ServerLoader.Response.STATUS_OK)) {
                JSONArray a = r.getArrayContent();
                List<ProductInfoExt> products = new ArrayList<ProductInfoExt>();
                for (int i = 0; i < a.length(); i++) {
                    JSONObject o = a.getJSONObject(i);
                    ProductInfoExt prd = new ProductInfoExt(o);
                    products.add(prd);
                }
                CatalogCache.refreshProducts(products);
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /** Get a product by ID */
    public final ProductInfoExt getProductInfo(String id)
        throws BasicException {
        return CatalogCache.getProduct(id);
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


    /** Get root categories. Categories must be preloaded. */
    public final List<CategoryInfo> getRootCategories() throws BasicException {
        return CatalogCache.getRootCategories();
    }

    public final CategoryInfo getCategory(String id) throws BasicException {
        return CatalogCache.getCategory(id);
    }

    /** Get subcategories from parent ID. Categories must be preloaded. */
    public final List<CategoryInfo> getSubcategories(String category) throws BasicException  {
        return CatalogCache.getSubcategories(category);
    }

    public final List<CategoryInfo> getCategories() throws BasicException {
        return CatalogCache.getCategories();
    }


    //Subgrupos de una composición
    public final List<SubgroupInfo> getSubgroups(String composition) throws BasicException  {
        return new PreparedSentence(s
            , "SELECT ID, NAME, IMAGE, DISPORDER FROM SUBGROUPS WHERE COMPOSITION = ? ORDER BY DISPORDER, NAME"
            , SerializerWriteString.INSTANCE
            , new SerializerReadClass(SubgroupInfo.class)).list(composition);
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

    /** Get products from a category ID. Products must be preloaded. */
    public List<ProductInfoExt> getProductCatalog(String category) throws BasicException  {
        return CatalogCache.getProductsByCategory(category);
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

    /** Get the list of tickets from opened cash sessions. */
    public List<TicketInfo> getSessionTickets() throws BasicException {
        try {
            ServerLoader loader = new ServerLoader();
            ServerLoader.Response r = loader.read("TicketsAPI", "getOpen");
            if (r.getStatus().equals(ServerLoader.Response.STATUS_OK)) {
                JSONArray a = r.getArrayContent();
                List<TicketInfo> list = new ArrayList<TicketInfo>();
                for (int i = 0; i < a.length(); i++) {
                    JSONObject o = a.getJSONObject(i);
                    list.add(new TicketInfo(o));
                }
                return list;
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new BasicException(e);
        }
    }

    //Tickets and Receipt list
    public List<TicketInfo> searchTickets(Integer ticketId, Integer ticketType,
            String cashId, Date start, Date stop, String customerId,
            String userId) throws BasicException {
        try {
            ServerLoader loader = new ServerLoader();
            ServerLoader.Response r = loader.read("TicketsAPI", "search",
                    "ticketId", ticketId != null ? ticketId.toString() : null,
                    "ticketType",
                    ticketType != null ? ticketType.toString() : null,
                    "cashId", cashId,
                    "dateStart",
                    start != null ? String.valueOf(DateUtils.toSecTimestamp(start)) : null,
                    "dateStop",
                    stop != null ? String.valueOf(DateUtils.toSecTimestamp(stop)) : null,
                    "customerId", customerId, "userId", userId);
            if (r.getStatus().equals(ServerLoader.Response.STATUS_OK)) {
                JSONArray a = r.getArrayContent();
                List<TicketInfo> list = new ArrayList<TicketInfo>();
                for (int i = 0; i < a.length(); i++) {
                    JSONObject o = a.getJSONObject(i);
                    list.add(new TicketInfo(o));
                }
                return list;
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new BasicException(e);
        }
    }

    public boolean preloadTaxes() {
        try {
            logger.log(Level.INFO, "Preloading taxes");
            ServerLoader loader = new ServerLoader();
            ServerLoader.Response r = loader.read("TaxesAPI", "getAll");
            if (r.getStatus().equals(ServerLoader.Response.STATUS_OK)) {
                JSONArray a = r.getArrayContent();
                List<TaxCategoryInfo> taxCats = new ArrayList<TaxCategoryInfo>();
                List<TaxInfo> taxes = new ArrayList<TaxInfo>();
                for (int i = 0; i < a.length(); i++) {
                    JSONObject o = a.getJSONObject(i);
                    TaxCategoryInfo taxCat = new TaxCategoryInfo(o);
                    taxCats.add(taxCat);
                    JSONArray a2 = o.getJSONArray("taxes");
                    for (int j = 0; j < a2.length(); j++) {
                        JSONObject o2 = a2.getJSONObject(j);
                        TaxInfo tax = new TaxInfo(o2);
                        taxes.add(tax);
                    }
                }
                TaxesCache.refreshTaxCategories(taxCats);
                TaxesCache.refreshTaxes(taxes);
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public final List<TaxInfo> getTaxList() throws BasicException {
        return TaxesCache.getTaxes();
    }
    public TaxInfo getTax(String taxId) throws BasicException {
        return TaxesCache.getTax(taxId);
    }

    public final List<TaxCategoryInfo> getTaxCategoriesList()
        throws BasicException {
        return TaxesCache.getTaxCats();
    }

    public final SentenceList getAttributeSetList() {
        return new StaticSentence(s
            , "SELECT ID, NAME FROM ATTRIBUTESET ORDER BY NAME"
            , null
            , new SerializerRead() { public Object readValues(DataRead dr) throws BasicException {
                return new AttributeSetInfo(dr.getString(1), dr.getString(2));
            }});
    }


    public boolean preloadCurrencies() {
        try {
            logger.log(Level.INFO, "Preloading currencies");
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
                CurrenciesCache.refreshCurrencies(currencies);
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public final List<CurrencyInfo> getCurrenciesList() throws BasicException {
        return CurrenciesCache.getCurrencies();
    }

    public CurrencyInfo getCurrency(int currencyId) throws BasicException {
        return CurrenciesCache.getCurrency(currencyId);
    }

    public CurrencyInfo getMainCurrency() throws BasicException {
        return CurrenciesCache.getMainCurrency();
    }

    public final boolean isCashActive(String id) throws BasicException {
        DataLogicSystem dlSystem = new DataLogicSystem();
        CashSession session = dlSystem.getCashSessionById(id);
        return session.isOpened();
    }

    public ZTicket getZTicket(String cashSessionId) throws BasicException {
        try {
            ServerLoader loader = new ServerLoader();
            ServerLoader.Response r = loader.read("CashesAPI", "zticket",
                    "id", cashSessionId);
            if (r.getStatus().equals(ServerLoader.Response.STATUS_OK)) {
                JSONObject o = r.getObjContent();
                if (o == null) {
                    return null;
                } else {
                    return new ZTicket(o);
                }
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new BasicException(e);
        }
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
            DataLogicCustomers dlCustomers = new DataLogicCustomers();
            ticket.setCustomer(customerid == null
                    ? null
                    : dlCustomers.getCustomer(customerid));

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

    /** Save or edit ticket */
    public final void saveTicket(final TicketInfo ticket,
            final String locationId,
            final String cashId) throws BasicException {
        try {
            ServerLoader loader = new ServerLoader();
            ServerLoader.Response r;
            r = loader.write("TicketsAPI", "save",
                    "ticket", ticket.toJSON().toString(), "cashId", cashId,
                    "locationId", locationId);
            if (!r.getStatus().equals(ServerLoader.Response.STATUS_OK)) {
                throw new BasicException("Bad server response");
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new BasicException(e);
        }
    }

    public final void deleteTicket(final TicketInfo ticket, final String location) throws BasicException {

        /*Transaction t = new Transaction(s) {
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
        t.execute();*/
    }

    public boolean preloadTariffAreas() {
        try {
            logger.log(Level.INFO, "Preloading tariff areas");
            ServerLoader loader = new ServerLoader();
            ServerLoader.Response r = loader.read("TariffAreasAPI", "getAll");
            if (r.getStatus().equals(ServerLoader.Response.STATUS_OK)) {
                JSONArray a = r.getArrayContent();
                Map<Integer, Map<String, Double>> prices = new HashMap<Integer, Map<String, Double>>();
                List<TariffInfo> areas = new ArrayList<TariffInfo>();
                for (int i = 0; i < a.length(); i++) {
                    JSONObject o = a.getJSONObject(i);
                    TariffInfo area = new TariffInfo(o);
                    areas.add(area);
                    Map<String, Double> areaPrices = new HashMap<String, Double>();
                    JSONArray aPrices = o.getJSONArray("prices");
                    for (int j = 0; j < aPrices.length(); j++) {
                        JSONObject oPrice = aPrices.getJSONObject(j);
                        String prdId = oPrice.getString("productId");
                        double price = oPrice.getDouble("price");
                        areaPrices.put(prdId, price);
                    }
                    prices.put(area.getID(), areaPrices);
                }
                TariffAreasCache.refreshTariffAreas(areas);
                TariffAreasCache.refreshPrices(prices);
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public Double getTariffAreaPrice(int tariffAreaId, String productId) throws BasicException {
        return TariffAreasCache.getPrice(tariffAreaId, productId);
    }

    public final List<TariffInfo> getTariffAreaList() throws BasicException {
        return TariffAreasCache.getAreas();
    }

    public boolean saveMove(CashMove move) throws BasicException {
        try {
            ServerLoader loader = new ServerLoader();
            ServerLoader.Response r = loader.write("CashMvtsAPI", "move",
                    "cashId", move.getCashId(),
                    "date", String.valueOf(DateUtils.toSecTimestamp(move.getDate())),
                    "note", move.getNote(),
                    "payment", move.getPayment().toJSON().toString());
            if (r.getStatus().equals(ServerLoader.Response.STATUS_OK)) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new BasicException(e);
        }
    }

}
