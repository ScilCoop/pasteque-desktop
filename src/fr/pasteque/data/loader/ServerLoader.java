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

package fr.pasteque.data.loader;

import fr.pasteque.pos.forms.AppConfig;
import fr.pasteque.pos.util.AltEncrypter;
import fr.pasteque.pos.util.URLBinGetter;
import fr.pasteque.pos.util.URLTextGetter;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ServerLoader {

    private static Logger logger = Logger.getLogger("fr.pasteque.data.loader.ServerLoader");

    /** Lock to take and release for synchronous/asynchronous call. */
    private Object lock;
    private String url;
    private String binUrl;
    private String user;
    private String password;

    private void preformatUrl() {
        if (!this.url.startsWith("http")) {
            this.url = "http://" + this.url;
        }
        if (!this.url.endsWith("/")) {
            this.url += "/";
        }
        this.url += "api.php";
    }

    private void preformatBinUrl() {
        if (!this.url.startsWith("http")) {
            this.url = "http://" + this.url;
        }
        if (!this.url.endsWith("/")) {
            this.url += "/";
        }
        this.url += "dbImg.php";
    }

    /** Create from AppConfig */
    public ServerLoader() {
        String url = AppConfig.loadedInstance.getProperty("server.backoffice");
        String binUrl = AppConfig.loadedInstance.getProperty("server.backoffice");
        String user = AppConfig.loadedInstance.getProperty("db.user");
        String password = AppConfig.loadedInstance.getProperty("db.password");
        if (password != null && password.startsWith("crypt:")) {
            // the password is encrypted
            AltEncrypter cypher = new AltEncrypter("cypherkey" + user);
            password = cypher.decrypt(password.substring(6));
        }
        this.url = url;
        this.preformatUrl();
        this.user = user;
        this.password = password;
    }

    public ServerLoader(String url, String user, String password) {
        this.url = url;
        this.preformatUrl();
        this.user = user;
        this.password = password;
    }

    private Map<String, String> params(String api, String action,
            String... params) {
        Map<String, String> ret = new HashMap<String, String>();
        ret.put("user", this.user);
        ret.put("password", this.password);
        ret.put("action", action);
        ret.put("p", api);
        for (int i = 0; i < params.length; i+= 2) {
            String key = params[i];
            String value = params[i + 1];
            ret.put(key, value);
        }
        return ret;
    }

    private Map<String, String> binParams(String model, String id) {
        Map<String, String> ret = new HashMap<String, String>();
        ret.put("w", model);
        ret.put("id", id);
        return ret;
    }

    private Response parseResponse(String resp) {
        JSONObject o = new JSONObject(resp);
        Response response = new Response(o);
        try {
            return new Response(new JSONObject(resp));
        } catch (JSONException e) {
            return null;
        }
    }

    public Response read(String api, String action, String... params)
        throws SocketTimeoutException, URLTextGetter.ServerException,
               IOException {
        logger.log(Level.INFO, "Reading " + api + " action " + action + " "
                + Arrays.deepToString(params));
        String resp = URLTextGetter.getText(this.url,
                this.params(api, action, params));
        logger.log(Level.INFO, "Server response: " + resp);
        try {
            return new Response(new JSONObject(resp));
        } catch (JSONException e) {
            throw new URLTextGetter.ServerException(e.getMessage());
        }
    }

    public Response write(String api, String action, String... params)
        throws SocketTimeoutException, URLTextGetter.ServerException,
               IOException {
        logger.log(Level.INFO, "Writing " + api + " action " + action + " "
                + Arrays.deepToString(params));
        String resp = URLTextGetter.getText(this.url, null,
                this.params(api, action, params));
        logger.log(Level.INFO, "Server response: " + resp);
        try {
            return new Response(new JSONObject(resp));
        } catch (JSONException e) {
            throw new URLTextGetter.ServerException(e.getMessage());
        }
    }

    public byte[] readBinary(String model, String id)
        throws SocketTimeoutException, URLBinGetter.ServerException,
               IOException {
        return URLBinGetter.getBinary(this.binUrl, this.binParams(model, id));
    }

    public class Response {
        public static final String STATUS_OK = "ok";
        public static final String STATUS_REJECTED = "rej";
        public static final String STATUS_ERROR = "err";

        private String status;
        private JSONObject response;
        
        public Response(JSONObject content) {
            this.status = content.getString("status");
            this.response = content;
        }

        public String getStatus() {
            return this.status;
        }

        public JSONObject getResponse() {
            return this.response;
        }

        public JSONObject getObjContent() {
            try {
                return this.response.getJSONObject("content");
            } catch (JSONException e) {
                return null;
            }
        }
        public JSONArray getArrayContent() {
            try {
                return this.response.getJSONArray("content");
            } catch (JSONException e) {
                return null;
            }
        }
    }
}
