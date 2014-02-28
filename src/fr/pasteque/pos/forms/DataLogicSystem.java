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

import fr.pasteque.basic.BasicException;
import fr.pasteque.data.loader.*;
import fr.pasteque.format.DateUtils;
import fr.pasteque.format.Formats;
import fr.pasteque.pos.caching.ResourcesCache;
import fr.pasteque.pos.caching.RolesCache;
import fr.pasteque.pos.caching.UsersCache;
import fr.pasteque.pos.ticket.CashRegisterInfo;
import fr.pasteque.pos.ticket.CashSession;
import fr.pasteque.pos.util.ThumbNailBuilder;

import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.xml.bind.DatatypeConverter;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author adrianromero
 */
public class DataLogicSystem {

    private static Logger logger = Logger.getLogger("fr.pasteque.pos.forms.DataLogicSystem");

    /** Creates a new instance of DataLogicSystem */
    public DataLogicSystem() {
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

    /** Get users from server */
    private List<AppUser> loadUsers() throws BasicException {
        try {
            ServerLoader loader = new ServerLoader();
            ServerLoader.Response r = loader.read("UsersAPI", "getAll");
            final ThumbNailBuilder tnb = new ThumbNailBuilder(32, 32,
                    "default_user.png");
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
            throw new BasicException(e);
        }
    }
    /** Preload and update cache if possible. Return true if succes. False
     * otherwise and cache is not modified.
     */
    public boolean preloadUsers() {
        try {
            logger.log(Level.INFO, "Preloading users");
            List<AppUser> data = this.loadUsers();
            if (data == null) {
                return false;
            }
            try {
                UsersCache.save(data);
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
            return true;
        } catch (BasicException e) {
            e.printStackTrace();
            return false;
        }
    }
    /** Get all users */
    public final List<AppUser> listPeople() throws BasicException {
        List<AppUser> data = null;
        try {
            data = UsersCache.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (data == null) {
            data = this.loadUsers();
            if (data != null) {
                try {
                    UsersCache.save(data);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return data;
    }
    /** Get visible users */
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
    /** Get user by card. Return null if nothing is found. */
    public final AppUser findPeopleByCard(String card) throws BasicException {
        List<AppUser> allUsers = this.listPeople();
        for (AppUser user : allUsers) {
            if (user.getCard() != null && user.getCard().equals(card)) {
                return user;
            }
        }
        return null;
    }

    private final Map<String, String> loadRoles() throws BasicException {
        try {
            ServerLoader loader = new ServerLoader();
            ServerLoader.Response r = loader.read("RolesAPI", "getAll");
            if (r.getStatus().equals(ServerLoader.Response.STATUS_OK)) {
                JSONArray a = r.getArrayContent();
                Map<String, String> roles = new HashMap<String, String>();
                for (int i = 0; i < a.length(); i++) {
                    JSONObject o = a.getJSONObject(i);
                    roles.put(o.getString("id"), o.getString("permissions"));
                }
                return roles;
            } else {
                return null;
            }
        } catch (Exception e) {
            throw new BasicException(e);
        }
    }
    public boolean preloadRoles() {
        try {
            logger.log(Level.INFO, "Preloading roles");
            Map<String, String> data = this.loadRoles();
            if (data == null) {
                return false;
            }
            try {
                RolesCache.save(data);
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
            return true;
        } catch (BasicException e) {
            e.printStackTrace();
            return false;
        }
    }
    public final String findRolePermissions(String sRole)
        throws BasicException {
        Map<String, String> data = null;
        try {
            data = RolesCache.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (data == null) {
            data = this.loadRoles();
            if (data != null) {
                try {
                    RolesCache.save(data);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        if (data != null) {
            return data.get(sRole);
        } else {
            return null;
        }
    }

    public final boolean changePassword(String userId, String oldPwd,
            String newPwd) throws BasicException {
        try {
            ServerLoader loader = new ServerLoader();
            ServerLoader.Response r = loader.write("UsersAPI", "updPwd",
                    "id", userId, "oldPwd", oldPwd, "newPwd", newPwd);
            if (r.getStatus().equals(ServerLoader.Response.STATUS_OK)) {
                boolean ok = r.getResponse().getBoolean("content");
                return ok;
            } else {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /** Load resource from server */
    private final byte[] loadResource(String name) throws BasicException {
        ServerLoader loader = new ServerLoader();
        byte[] resource;
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
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            resource = null;
        }
        return resource;
    }
    /** Preload and update cache if possible. Return true if succes. False
     * otherwise and cache is not modified.
     */
    public boolean preloadResource(String name) {
        try {
            logger.log(Level.INFO, "Preloading resource " + name);
            byte[] data = this.loadResource(name);
            if (data == null) {
                return false;
            }
            try {
                ResourcesCache.save(name, data);
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
            return true;
        } catch (BasicException e) {
            e.printStackTrace();
            return false;
        }
    }
    private byte[] getResource(String name) {
        byte[] data = null;
        try {
            data = ResourcesCache.load(name);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (data == null) {
            try {
                data = this.loadResource(name);
                if (data != null) {
                    try {
                        ResourcesCache.save(name, data);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } catch (BasicException e) {
                e.printStackTrace();
            }
        }
        return data;
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

    public final String findLocationName(String locationId)
        throws BasicException {
        try {
            ServerLoader loader = new ServerLoader();
            ServerLoader.Response r = loader.read("LocationsAPI", "get",
                    "id", locationId);
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
