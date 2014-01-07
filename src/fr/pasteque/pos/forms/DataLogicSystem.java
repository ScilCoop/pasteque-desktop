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
import fr.pasteque.format.DateUtils;
import fr.pasteque.format.Formats;
import fr.pasteque.pos.ticket.CashRegisterInfo;
import fr.pasteque.pos.ticket.CashSession;
import fr.pasteque.pos.util.ThumbNailBuilder;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.xml.bind.DatatypeConverter;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author adrianromero
 */
public class DataLogicSystem extends BeanFactoryDataSingle {
    
    protected SerializerRead peopleread;
    
    private SentenceExec m_changepassword;    
    private SentenceFind m_locationfind;
    
    private SentenceExec m_resourcebytesinsert;
    private SentenceExec m_resourcebytesupdate;

    protected SentenceExec m_insertcash;
    
    private Map<String, byte[]> resourcescache;
    
    /** Creates a new instance of DataLogicSystem */
    public DataLogicSystem() {            
    }
    
    public void init(Session s){
        m_changepassword = new StaticSentence(s
                , "UPDATE PEOPLE SET APPPASSWORD = ? WHERE ID = ?"
                ,new SerializerWriteBasic(new Datas[] {Datas.STRING, Datas.STRING}));

        resetResourcesCache();        
    }

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
        List<AppUser> allUsers = this.listPeople();
        for (AppUser user : allUsers) {
            if (user.getCard() != null && user.getCard().equals(card)) {
                return user;
            }
        }
        return null;
    }   
    
    public final String findRolePermissions(String sRole) throws BasicException {
        try {
            ServerLoader loader = new ServerLoader();
            ServerLoader.Response r = loader.read("RolesAPI", "get",
                    "id", sRole);
            if (r.getStatus().equals(ServerLoader.Response.STATUS_OK)) {
                JSONObject o = r.getObjContent();
                return o.getString("permissions");
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new BasicException(e);
        }
    }
    
    public final void execChangePassword(Object[] userdata) throws BasicException {
        m_changepassword.exec(userdata);
    }
    
    public final void resetResourcesCache() {
        resourcescache = new HashMap<String, byte[]>();      
    }
    
    private final byte[] getResource(String name) {
        ServerLoader loader = new ServerLoader();
        byte[] resource;
        resource = resourcescache.get(name);
        if (resource == null) {
            // Check resource from server
            try {
                ServerLoader.Response r = loader.read("ResourcesAPI", "get",
                        "label", name);
                if (r.getStatus().equals(ServerLoader.Response.STATUS_OK)) {
                    JSONObject o = r.getObjContent();
                    String strRes = o.getString("content");
                    if (o.getInt("type") == 0) {
                        resource = strRes.getBytes();
                    } else {
                        resource = DatatypeConverter.parseBase64Binary(strRes);
                    }
                    resourcescache.put(name, resource);
                } else {
                    return null;
                }
            } catch (Exception e) {
                e.printStackTrace();
                resource = null;
            }
        }
        return resource;
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
            e.printStackTrace();
            return null;
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
            e.printStackTrace();
        }
        return p;
    }

    public final CashRegisterInfo getCashRegister(String host)
        throws BasicException {
        try {
            ServerLoader loader = new ServerLoader();
            ServerLoader.Response r = loader.read("CashRegistersAPI", "get",
                    "label", host);
            if (r.getStatus().equals(ServerLoader.Response.STATUS_OK)) {
                JSONObject o = r.getObjContent();
                if (o == null) {
                    return null;
                }
                return new CashRegisterInfo(o);
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new BasicException(e);
        }
    }

    public CashSession getCashSession(String host) throws BasicException {
        try {
            ServerLoader loader = new ServerLoader();
            ServerLoader.Response r = loader.read("CashesAPI", "get",
                    "host", host);
            if (r.getStatus().equals(ServerLoader.Response.STATUS_OK)) {
                JSONObject o = r.getObjContent();
                if (o == null) {
                    return null;
                } else {
                    return new CashSession(o);
                }
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new BasicException(e);
        }
    }

    /** Send a cash session to the server and return the updated session
     * (id may have been set)
     */
    public final CashSession saveCashSession(CashSession cashSess)
        throws BasicException {
        try {
            ServerLoader loader = new ServerLoader();
            ServerLoader.Response r = loader.write("CashesAPI", "update",
                    "cash", cashSess.toJSON().toString());
            if (r.getStatus().equals(ServerLoader.Response.STATUS_OK)) {
                JSONObject o = r.getObjContent();
                if (o == null) {
                    return null;
                } else {
                    return new CashSession(o);
                }
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new BasicException(e);
        }
    } 
    
    public final String findLocationName(String iLocation) throws BasicException {
        try {
            ServerLoader loader = new ServerLoader();
            ServerLoader.Response r = loader.read("RolesAPI", "get",
                    "id", iLocation);
            if (r.getStatus().equals(ServerLoader.Response.STATUS_OK)) {
                JSONObject o = r.getObjContent();
                return o.getString("label");
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new BasicException(e);
        }
    }    
}
