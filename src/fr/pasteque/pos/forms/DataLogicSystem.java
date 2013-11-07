//    POS-Tech
//    Based upon Openbravo POS
//
//    Copyright (C) 2007-2009 Openbravo, S.L.
//                       2012 SARL SCOP Scil (http://scil.coop)
//
//    This file is part of POS-Tech.
//
//    POS-Tech is free software: you can redistribute it and/or modify
//    it under the terms of the GNU General Public License as published by
//    the Free Software Foundation, either version 3 of the License, or
//    (at your option) any later version.
//
//    POS-Tech is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//    GNU General Public License for more details.
//
//    You should have received a copy of the GNU General Public License
//    along with POS-Tech.  If not, see <http://www.gnu.org/licenses/>.

package fr.pasteque.pos.forms;

import java.awt.image.BufferedImage;
import java.io.*;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import javax.imageio.ImageIO;
import fr.pasteque.basic.BasicException;
import fr.pasteque.data.loader.*;
import fr.pasteque.format.Formats;
import fr.pasteque.pos.util.ThumbNailBuilder;
import java.util.HashMap;
import java.util.Map;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author adrianromero
 */
public class DataLogicSystem extends BeanFactoryDataSingle {
    
    private static final String RES_DIR = "res/";
    
    protected String m_sInitScript;
    
    protected SentenceList m_peoplevisible;
    protected SentenceFind m_peoplebycard;  
    protected SerializerRead peopleread;
    
    private SentenceFind m_rolepermissions; 
    private SentenceExec m_changepassword;    
    private SentenceFind m_locationfind;
    
    private SentenceFind m_resourcebytes;
    private SentenceExec m_resourcebytesinsert;
    private SentenceExec m_resourcebytesupdate;

    protected SentenceFind m_sequencecash;
    protected SentenceFind m_activecash;
    protected SentenceExec m_insertcash;
    
    private Map<String, byte[]> resourcescache;
    
    /** Creates a new instance of DataLogicSystem */
    public DataLogicSystem() {            
    }
    
    public void init(Session s){

        m_sInitScript = "/fr/pasteque/pos/scripts/" + s.DB.getName();

        m_peoplebycard = new PreparedSentence(s
            , "SELECT ID, NAME, APPPASSWORD, CARD, ROLE, IMAGE FROM PEOPLE WHERE CARD = ? AND VISIBLE = " + s.DB.TRUE()
            , SerializerWriteString.INSTANCE
            , peopleread);
         
        m_resourcebytes = new PreparedSentence(s
            , "SELECT CONTENT FROM RESOURCES WHERE NAME = ?"
            , SerializerWriteString.INSTANCE
            , SerializerReadBytes.INSTANCE);
        
        Datas[] resourcedata = new Datas[] {Datas.STRING, Datas.STRING, Datas.INT, Datas.BYTES};
        m_resourcebytesinsert = new PreparedSentence(s
                , "INSERT INTO RESOURCES(ID, NAME, RESTYPE, CONTENT) VALUES (?, ?, ?, ?)"
                , new SerializerWriteBasic(resourcedata));
        m_resourcebytesupdate = new PreparedSentence(s
                , "UPDATE RESOURCES SET NAME = ?, RESTYPE = ?, CONTENT = ? WHERE NAME = ?"
                , new SerializerWriteBasicExt(resourcedata, new int[] {1, 2, 3, 1}));
        
        m_rolepermissions = new PreparedSentence(s
                , "SELECT PERMISSIONS FROM ROLES WHERE ID = ?"
            , SerializerWriteString.INSTANCE
            , SerializerReadBytes.INSTANCE);     
        
        m_changepassword = new StaticSentence(s
                , "UPDATE PEOPLE SET APPPASSWORD = ? WHERE ID = ?"
                ,new SerializerWriteBasic(new Datas[] {Datas.STRING, Datas.STRING}));

        m_sequencecash = new StaticSentence(s,
                "SELECT MAX(HOSTSEQUENCE) FROM CLOSEDCASH WHERE HOST = ?",
                SerializerWriteString.INSTANCE,
                SerializerReadInteger.INSTANCE);
        m_activecash = new StaticSentence(s
            , "SELECT HOST, HOSTSEQUENCE, DATESTART, DATEEND FROM CLOSEDCASH WHERE MONEY = ?"
            , SerializerWriteString.INSTANCE
            , new SerializerReadBasic(new Datas[] {Datas.STRING, Datas.INT, Datas.TIMESTAMP, Datas.TIMESTAMP}));            
        m_insertcash = new StaticSentence(s
                , "INSERT INTO CLOSEDCASH(MONEY, HOST, HOSTSEQUENCE, DATESTART, DATEEND) " +
                  "VALUES (?, ?, ?, ?, ?)"
                , new SerializerWriteBasic(new Datas[] {Datas.STRING, Datas.STRING, Datas.INT, Datas.TIMESTAMP, Datas.TIMESTAMP}));
            
        m_locationfind = new StaticSentence(s
                , "SELECT NAME FROM LOCATIONS WHERE ID = ?"
                , SerializerWriteString.INSTANCE
                , SerializerReadString.INSTANCE);   
        
        resetResourcesCache();        
    }


    public String getInitScript() {
        return m_sInitScript;
    }
    
//    public abstract BaseSentence getShutdown();
    
    public final String findDbVersion() throws BasicException {
        try {
        ServerLoader loader = new ServerLoader();
        ServerLoader.Response r = loader.read("VersionAPI", "");
        if (r.getStatus().equals(ServerLoader.Response.STATUS_OK)) {
            JSONObject v = r.getObjContent();
            return v.getString("level");
        } else {
            return null;
        }
        } catch (Exception e) {
            e.printStackTrace();
            throw new BasicException(e);
        }
    }
    public final void execDummy() throws BasicException {
        try {
            ServerLoader loader = new ServerLoader();
            ServerLoader.Response r = loader.read("" ,"");
            if (r.getStatus().equals(ServerLoader.Response.STATUS_REJECTED)) {
                return;
            } else {
                throw new BasicException("Bad server response");
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new BasicException(e);
        }
    }
    public final List<AppUser> listPeople() throws BasicException {
        try {
            ServerLoader loader = new ServerLoader();
            ServerLoader.Response r = loader.read("UsersAPI", "getAll");
            final ThumbNailBuilder tnb = new ThumbNailBuilder(32, 32, "default_user.png");
            if (r.getStatus().equals(ServerLoader.Response.STATUS_OK)) {
                JSONArray a = r.getArrayContent();
                List<AppUser> users = new LinkedList<AppUser>();
                for (int i = 0; i < a.length(); i++) {
                    JSONObject jsU = a.getJSONObject(i);
                    String password = null;
                    String card = null;
                    if (!jsU.isNull("password")) {
                        password = jsU.getString("password");
                    }
                    if (!jsU.isNull("card")) {
                        card = jsU.getString("card");
                    }
                    ImageIcon icon;
                    if (jsU.getBoolean("hasImage")) {
                        byte[] data = loader.readBinary("user",
                                jsU.getString("id"));
                        icon = new ImageIcon(tnb.getThumbNail(ImageUtils.readImage(data)));
                    } else {
                        icon = new ImageIcon(tnb.getThumbNail(null));
                    }
                    AppUser u = new AppUser(jsU.getString("id"),
                            jsU.getString("name"), password, card,
                            jsU.getString("roleId"), icon,
                            jsU.getBoolean("visible"));
                    users.add(u);
                }
                return users;
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new BasicException(e);
        }
    }
    public final List listPeopleVisible() throws BasicException {
        List<AppUser> allUsers = this.listPeople();
        List<AppUser> visUsers = new LinkedList<AppUser>();
        for (AppUser user : allUsers) {
            if (user.isVisible()) {
                visUsers.add(user);
            }
        }
        return visUsers;
    }      
    public final AppUser findPeopleByCard(String card) throws BasicException {
        return (AppUser) m_peoplebycard.find(card);
    }   
    
    public final String findRolePermissions(String sRole) {
        
        try {
            return Formats.BYTEA.formatValue(m_rolepermissions.find(sRole));        
        } catch (BasicException e) {
            return null;                    
        }             
    }
    
    public final void execChangePassword(Object[] userdata) throws BasicException {
        m_changepassword.exec(userdata);
    }
    
    public final void resetResourcesCache() {
        resourcescache = new HashMap<String, byte[]>();      
    }
    
    private final byte[] getResource(String name) {

        byte[] resource;
        
        resource = resourcescache.get(name);
        
        if (resource == null) {
            // Check resource on file system
            String basePath = System.getProperty("dirname.path");
            File resFile = new File(basePath + RES_DIR + name);
            if (resFile.exists() && resFile.isFile() && resFile.canRead()) {
                ByteArrayOutputStream bos = new ByteArrayOutputStream(2048);
                byte[] buffer = new byte[2048];
                BufferedInputStream bis = null;
                try {
                   bis = new BufferedInputStream(new FileInputStream(resFile));
                } catch (FileNotFoundException fnfe) {
                    // Unreachable
                    fnfe.printStackTrace();
                }
                int read = 0;
                try {
                    read = bis.read(buffer, 0, buffer.length);
                    while (read != -1) {
                        bos.write(buffer, 0, read);
                        read = bis.read(buffer, 0, buffer.length);
                    }
                } catch (IOException ioe) {
                    // TODO: log error;
                    ioe.printStackTrace();
                }
                resource = bos.toByteArray();
                try {
                    bis.close();
                } catch (IOException ioe) {
                    // TODO: log error
                    ioe.printStackTrace();
                }
                try {
                    bos.close();
                } catch (IOException ioe) {
                    // TODO: log error
                    ioe.printStackTrace();
                }
            } else {
                // Check resource in database
                try {
                    resource = (byte[]) m_resourcebytes.find(name);
                    resourcescache.put(name, resource);
                } catch (BasicException e) {
                    resource = null;
                }
            }
        }
        return resource;
    }
    
    public final void setResource(String name, int type, byte[] data) {
        
        Object[] value = new Object[] {UUID.randomUUID().toString(), name, new Integer(type), data};
        try {
            if (m_resourcebytesupdate.exec(value) == 0) {
                m_resourcebytesinsert.exec(value);
            }
            resourcescache.put(name, data);
        } catch (BasicException e) {
        }
    }
    
    public final void setResourceAsBinary(String sName, byte[] data) {
        setResource(sName, 2, data);
    }
    
    public final byte[] getResourceAsBinary(String sName) {
        return getResource(sName);
    }
    
    public final String getResourceAsText(String sName) {
        return Formats.BYTEA.formatValue(getResource(sName));
    }
    
    public final String getResourceAsXML(String sName) {
        return Formats.BYTEA.formatValue(getResource(sName));
    }    
    
    public final BufferedImage getResourceAsImage(String sName) {
        try {
            byte[] img = getResource(sName); // , ".png"
            return img == null ? null : ImageIO.read(new ByteArrayInputStream(img));
        } catch (IOException e) {
            return null;
        }
    }
    
    public final void setResourceAsProperties(String sName, Properties p) {
        if (p == null) {
            setResource(sName, 0, null); // texto
        } else {
            try {
                ByteArrayOutputStream o = new ByteArrayOutputStream();
                p.storeToXML(o, AppLocal.APP_NAME, "UTF8");
                setResource(sName, 0, o.toByteArray()); // El texto de las propiedades   
            } catch (IOException e) { // no deberia pasar nunca
            }            
        }
    }
    
    public final Properties getResourceAsProperties(String sName) {
        
        Properties p = new Properties();
        try {
            byte[] img = getResourceAsBinary(sName);
            if (img != null) {
                p.loadFromXML(new ByteArrayInputStream(img));
            }
        } catch (IOException e) {
        }
        return p;
    }

    public final int getSequenceCash(String host) throws BasicException {
        Integer i = (Integer) m_sequencecash.find(host);
        return (i == null) ? 1 : i.intValue();
    }

    public final Object[] findActiveCash(String sActiveCashIndex) throws BasicException {
        return (Object[]) m_activecash.find(sActiveCashIndex);
    }
    
    public final void execInsertCash(Object[] cash) throws BasicException {
        m_insertcash.exec(cash);
    } 
    
    public final String findLocationName(String iLocation) throws BasicException {
        return (String) m_locationfind.find(iLocation);
    }    
}
