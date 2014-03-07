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
import fr.pasteque.pos.caching.CustomersCache;
import fr.pasteque.pos.forms.AppLocal;
import fr.pasteque.pos.forms.BeanFactoryDataSingle;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author adrianromero
 */
public class DataLogicCustomers extends BeanFactoryDataSingle {

    private static Logger logger = Logger.getLogger("fr.pasteque.pos.customers.DataLogicCustomers");
    
    // TODO: use local database for caching
    private static List<DiscountProfile> discProfileCache;
    
    public void init(Session s){
        DataLogicCustomers.discProfileCache = null;
    }

    /** Load customers list from server */
    private List<CustomerInfoExt> loadCustomers() throws BasicException {
        try {
            ServerLoader loader = new ServerLoader();
            ServerLoader.Response r = loader.read("CustomersAPI", "getAll");
            if (r.getStatus().equals(ServerLoader.Response.STATUS_OK)) {
                List<CustomerInfoExt> data = new ArrayList<CustomerInfoExt>();
                JSONArray a = r.getArrayContent();
                for (int i = 0; i < a.length(); i++) {
                    JSONObject o = a.getJSONObject(i);
                    CustomerInfoExt customer = new CustomerInfoExt(o);
                    data.add(customer);
                }
                return data;
            }
        } catch (Exception e) {
            throw new BasicException(e);
        }
        return null;
    }
    private List<String> loadTopCustomers() throws BasicException {
        try {
            ServerLoader loader = new ServerLoader();
            ServerLoader.Response r = loader.read("CustomersAPI", "getTop");
            if (r.getStatus().equals(ServerLoader.Response.STATUS_OK)) {
                List<String> data = new ArrayList<String>();
                JSONArray a = r.getArrayContent();
                for (int i = 0; i < a.length(); i++) {
                    data.add(a.getString(i));
                }
                return data;
            }
        } catch (Exception e) {
            throw new BasicException(e);
        }
        return null;
    }
    /** Preload and update cache if possible. Return true if succes. False
     * otherwise and cache is not modified.
     */
    public boolean preloadCustomers() {
        try {
            logger.log(Level.INFO, "Preloading customers");
            List<CustomerInfoExt> data = this.loadCustomers();
            List<String> topIds = this.loadTopCustomers();
            if (data == null) {
                return false;
            }
            try {
                CustomersCache.refreshCustomers(data);
                CustomersCache.refreshRanking(topIds);
            } catch (BasicException e) {
                e.printStackTrace();
                return false;
            }
            return true;
        } catch (BasicException e) {
            e.printStackTrace();
            return false;
        }
    }

    /** Get all customers */
    public List<CustomerInfoExt> getCustomerList() throws BasicException {
        return CustomersCache.getCustomers();
    }

    public CustomerInfoExt getCustomer(String id) throws BasicException {
        return CustomersCache.getCustomer(id);
    }

    /** Search customers, use null as argument to disable filter */
    public List<CustomerInfoExt> searchCustomers(String number,
            String searchkey, String name) throws BasicException {
        return CustomersCache.searchCustomers(number, searchkey, name);
    }


    /** Gets the TOP 10 customer's list by number of tickets
     * with their id
     */
    public List<CustomerInfoExt> getTop10CustomerList() throws BasicException {
        return CustomersCache.getTopCustomers();
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
