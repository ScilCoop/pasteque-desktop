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

package fr.pasteque.pos.customers;

import fr.pasteque.basic.BasicException;
import fr.pasteque.data.loader.DataParams;
import fr.pasteque.data.loader.DataRead;
import fr.pasteque.data.loader.Datas;
import fr.pasteque.data.loader.PreparedSentence;
import fr.pasteque.data.loader.QBFBuilder;
import fr.pasteque.data.loader.SentenceExec;
import fr.pasteque.data.loader.SentenceExecTransaction;
import fr.pasteque.data.loader.SentenceList;
import fr.pasteque.data.loader.SerializerRead;
import fr.pasteque.data.loader.SerializerReadBasic;
import fr.pasteque.data.loader.SerializerWriteBasic;
import fr.pasteque.data.loader.SerializerWriteBasicExt;
import fr.pasteque.data.loader.SerializerWriteParams;
import fr.pasteque.data.loader.ServerLoader;
import fr.pasteque.data.loader.Session;
import fr.pasteque.data.loader.StaticSentence;
import fr.pasteque.data.loader.TableDefinition;
import fr.pasteque.format.Formats;
import fr.pasteque.pos.forms.AppLocal;
import fr.pasteque.pos.forms.BeanFactoryDataSingle;

import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author adrianromero
 */
public class DataLogicCustomers extends BeanFactoryDataSingle {
    
    // TODO: use local database for caching
    private static List<CustomerInfoExt> cache;
    private static List<DiscountProfile> discProfileCache;
    
    public void init(Session s){
        DataLogicCustomers.cache = null; // Reset cache
        DataLogicCustomers.discProfileCache = null;
    }

    private static void loadCustomers() throws BasicException {
         try {
             ServerLoader loader = new ServerLoader();
             ServerLoader.Response r = loader.read("CustomersAPI", "getAll");
             if (r.getStatus().equals(ServerLoader.Response.STATUS_OK)) {
                 DataLogicCustomers.cache = new ArrayList<CustomerInfoExt>();
                 JSONArray a = r.getArrayContent();
                 for (int i = 0; i < a.length(); i++) {
                     JSONObject o = a.getJSONObject(i);
                     CustomerInfoExt customer = new CustomerInfoExt(o);
                     DataLogicCustomers.cache.add(customer);
                 }
             }
         } catch (Exception e) {
             throw new BasicException(e);
         }
    }

    /** Get all customers */
    public List<CustomerInfoExt> getCustomerList() throws BasicException {
        if (DataLogicCustomers.cache == null) {
            DataLogicCustomers.loadCustomers();
        }
        return DataLogicCustomers.cache;
    }

    /** Search customers, use null as argument to disable filter */
    public List<CustomerInfoExt> searchCustomers(String number,
            String searchkey, String name) throws BasicException {
        if (DataLogicCustomers.cache == null) {
            DataLogicCustomers.loadCustomers();
        }
        List<CustomerInfoExt> results = new ArrayList<CustomerInfoExt>();
        for (CustomerInfoExt c : DataLogicCustomers.cache) {
            boolean matches = true;
            if (number != null) {
                String custNum = c.getTaxid();
                if (custNum != null) {
                    custNum = custNum.toLowerCase();
                    if (!custNum.contains(number.toLowerCase())) {
                        matches = false;
                    }
                } else {
                    matches = false;
                }
            }
            if (matches && searchkey != null) {
                String custSK = c.getSearchkey();
                if (custSK != null) {
                    custSK = custSK.toLowerCase();
                    if (!custSK.contains(searchkey.toLowerCase())) {
                        matches = false;
                    }
                } else {
                    matches = false;
                }
            }
            if (matches && name != null) {
                String custName = c.getName();
                if (custName != null) {
                    custName = custName.toLowerCase();
                    if (!custName.contains(name.toLowerCase())) {
                        matches = false;
                    }
                } else {
                    matches = false;
                }
            }
            if (matches) {
                results.add(c);
            }
        }
        return results;
    }


    /** Gets the TOP 10 customer's list by number of tickets
     * with their id
     */
    public SentenceList getTop10CustomerList() {
        /*        return new StaticSentence(s
            , new QBFBuilder("SELECT CUSTOMERS.ID, CUSTOMERS.TAXID, CUSTOMERS.SEARCHKEY, CUSTOMERS.NAME, " + 
            " Count( TICKETS.CUSTOMER ) AS Top10 FROM CUSTOMERS " +
            " LEFT JOIN TICKETS ON TICKETS.CUSTOMER = CUSTOMERS.ID " +
            " WHERE VISIBLE = " + s.DB.TRUE() + " AND ?(QBF_FILTER) " +
            " GROUP BY CUSTOMERS.ID ORDER BY Top10 DESC, NAME ASC LIMIT 10 ", new String[] {"TAXID", "SEARCHKEY", "NAME"})
            , new SerializerWriteBasic(new Datas[] {Datas.OBJECT, Datas.STRING, Datas.OBJECT, Datas.STRING, Datas.OBJECT, Datas.STRING})
            , new SerializerRead() {
                    public Object readValues(DataRead dr) throws BasicException {
                        CustomerInfo c = new CustomerInfo(dr.getString(1));
                        c.setTaxid(dr.getString(2));
                        c.setSearchkey(dr.getString(3));
                        c.setName(dr.getString(4));
                        return c;
                    }
                    });*/
        // TODO: reenable top 10 customers list
        return null;
    }
       
    public int updateCustomerExt(final CustomerInfoExt customer) throws BasicException {
        /*        return new PreparedSentence(s
                , "UPDATE CUSTOMERS SET NOTES = ? WHERE ID = ?"
                , SerializerWriteParams.INSTANCE      
                ).exec(new DataParams() { public void writeValues() throws BasicException {
                        setString(1, customer.getNotes());
                        setString(2, customer.getId());
                        }});*/
        // TODO: reenable customer update
        return 0;
    }

    public final SentenceList getReservationsList() {
        /*        return new PreparedSentence(s
            , "SELECT R.ID, R.CREATED, R.DATENEW, C.CUSTOMER, CUSTOMERS.TAXID, CUSTOMERS.SEARCHKEY, COALESCE(CUSTOMERS.NAME, R.TITLE),  R.CHAIRS, R.ISDONE, R.DESCRIPTION " +
              "FROM RESERVATIONS R LEFT OUTER JOIN RESERVATION_CUSTOMERS C ON R.ID = C.ID LEFT OUTER JOIN CUSTOMERS ON C.CUSTOMER = CUSTOMERS.ID " +
              "WHERE R.DATENEW >= ? AND R.DATENEW < ?"
            , new SerializerWriteBasic(new Datas[] {Datas.TIMESTAMP, Datas.TIMESTAMP})
            , new SerializerReadBasic(customerdatas));*/
        // TODO: enable reservation list
        return null;
    }
    
    public final SentenceExec getReservationsUpdate() {
        /*return new SentenceExecTransaction(s) {
            public int execInTransaction(Object params) throws BasicException {  
    
                new PreparedSentence(s
                    , "DELETE FROM RESERVATION_CUSTOMERS WHERE ID = ?"
                    , new SerializerWriteBasicExt(customerdatas, new int[]{0})).exec(params);
                if (((Object[]) params)[3] != null) {
                    new PreparedSentence(s
                        , "INSERT INTO RESERVATION_CUSTOMERS (ID, CUSTOMER) VALUES (?, ?)"
                        , new SerializerWriteBasicExt(customerdatas, new int[]{0, 3})).exec(params);                
                }
                return new PreparedSentence(s
                    , "UPDATE RESERVATIONS SET ID = ?, CREATED = ?, DATENEW = ?, TITLE = ?, CHAIRS = ?, ISDONE = ?, DESCRIPTION = ? WHERE ID = ?"
                    , new SerializerWriteBasicExt(customerdatas, new int[]{0, 1, 2, 6, 7, 8, 9, 0})).exec(params);
            }
            };*/
        // TODO: enable reservation update
        return null;
    }
    
    public final SentenceExec getReservationsDelete() {
        /*        return new SentenceExecTransaction(s) {
            public int execInTransaction(Object params) throws BasicException {  
    
                new PreparedSentence(s
                    , "DELETE FROM RESERVATION_CUSTOMERS WHERE ID = ?"
                    , new SerializerWriteBasicExt(customerdatas, new int[]{0})).exec(params);
                return new PreparedSentence(s
                    , "DELETE FROM RESERVATIONS WHERE ID = ?"
                    , new SerializerWriteBasicExt(customerdatas, new int[]{0})).exec(params);
            }
            };*/
        // TODO: enable reservation delete
        return null;
    }
    
    public final SentenceExec getReservationsInsert() {
        /*        return new SentenceExecTransaction(s) {
            public int execInTransaction(Object params) throws BasicException {  
    
                int i = new PreparedSentence(s
                    , "INSERT INTO RESERVATIONS (ID, CREATED, DATENEW, TITLE, CHAIRS, ISDONE, DESCRIPTION) VALUES (?, ?, ?, ?, ?, ?, ?)"
                    , new SerializerWriteBasicExt(customerdatas, new int[]{0, 1, 2, 6, 7, 8, 9})).exec(params);

                if (((Object[]) params)[3] != null) {
                    new PreparedSentence(s
                        , "INSERT INTO RESERVATION_CUSTOMERS (ID, CUSTOMER) VALUES (?, ?)"
                        , new SerializerWriteBasicExt(customerdatas, new int[]{0, 3})).exec(params);                
                }
                return i;
            }
            };*/
        // TODO: enable reservation create
        return null;
    }

    private static void loadDiscountProfiles() throws BasicException {
         try {
             ServerLoader loader = new ServerLoader();
             ServerLoader.Response r = loader.read("DiscountProfilesAPI",
                     "getAll");
             if (r.getStatus().equals(ServerLoader.Response.STATUS_OK)) {
                 DataLogicCustomers.discProfileCache = new ArrayList<DiscountProfile>();
                 JSONArray a = r.getArrayContent();
                 for (int i = 0; i < a.length(); i++) {
                     JSONObject o = a.getJSONObject(i);
                     DiscountProfile prof = new DiscountProfile(o);
                     DataLogicCustomers.discProfileCache.add(prof);
                 }
             }
         } catch (Exception e) {
             throw new BasicException(e);
         }
    }

    public DiscountProfile getDiscountProfile(int id) throws BasicException {
        if (DataLogicCustomers.discProfileCache == null) {
            DataLogicCustomers.loadDiscountProfiles();
        }
        for (DiscountProfile p : DataLogicCustomers.discProfileCache) {
            if (id == p.getId()) {
                return p;
            }
        }
        return null;
    }

}
