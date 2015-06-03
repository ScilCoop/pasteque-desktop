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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Date;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RolesCache {

    private static final Logger logger = Logger.getLogger("fr.pasteque.pos.caching.RolesCache");

    private static String path() {
        return AppConfig.loadedInstance.getDataDir() + "/roles.cache";
    }

    private RolesCache() {}

    /** Get cached data if any, null otherwise */
    public static Map<String, String> load() throws IOException {
        File cache = new File(path());
        logger.log(Level.INFO, "Reading roles from " + cache.getAbsolutePath());
        if (cache.exists()) {
            FileInputStream fis = new FileInputStream(cache);
            ObjectInputStream os = new ObjectInputStream(fis);
            Map<String, String> data = null;
            try {
                data = (Map<String, String>) os.readObject();
            } catch (ClassNotFoundException e) {
                // Should never happen
                os.close();
                throw new IOException(e.getMessage());
            }
            os.close();
            logger.log(Level.INFO, "Read " + data.size() + " roles");
            return data;
        } else {
            return null;
        }
    }

    public static void save(Map<String, String> data) throws IOException {
        File cache = new File(path());
        logger.log(Level.INFO, "Saving " + data.size() + " roles in "
                + cache.getAbsoluteFile());
        if (!cache.exists()) {
            cache.createNewFile();
            logger.log(Level.INFO, "Created cache file "
                    + cache.getAbsoluteFile());
        }
        FileOutputStream fos = new FileOutputStream(cache);
        ObjectOutputStream os = new ObjectOutputStream(fos);
        os.writeObject(data);
        os.flush();
        os.close();
    }

    public Date getDate() {
        File cache = new File(path());
        if (!cache.exists()) {
            return null;
        } else {
            return DateUtils.readMilliTimestamp(cache.lastModified());
        }
    }
}
