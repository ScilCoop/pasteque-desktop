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
import fr.pasteque.pos.forms.AppUser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ByteArrayOutputStream;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ResourcesCache {

    private static Logger logger = Logger.getLogger("fr.pasteque.pos.caching.ResourcesCache");

    private static String path(String name) {
        name = name.replace("/", "_");
        name = name.replace("\\", "_");
        return AppConfig.loadedInstance.getDataDir() + "/res-" + name + ".cache";
    }

    private ResourcesCache() {}

    /** Get cached data if any, null otherwise */
    public static byte[] load(String name) throws IOException {
        File cache = new File(path(name));
        logger.log(Level.INFO, "Reading resource from "
                + cache.getAbsolutePath());
        if (cache.exists()) {
            byte[] buffer = new byte[2048];
            FileInputStream fis = new FileInputStream(cache);
            ByteArrayOutputStream os = new ByteArrayOutputStream(buffer.length);
            int read = fis.read(buffer);
            while (read != -1) {
                os.write(buffer, 0, read);
                read = fis.read(buffer);
            }
            byte[] data = os.toByteArray();
            os.close();
            logger.log(Level.INFO, "Read " + data.length + " bytes");
            return data;
        } else {
            return null;
        }
    }

    public static void save(String name, byte[] data) throws IOException {
        File cache = new File(path(name));
        logger.log(Level.INFO, "Saving " + data.length + " bytes in "
                + cache.getAbsoluteFile());
        if (!cache.exists()) {
            cache.createNewFile();
            logger.log(Level.INFO, "Created cache file "
                    + cache.getAbsoluteFile());
        }
        FileOutputStream fos = new FileOutputStream(cache);
        fos.write(data);
        fos.flush();
        fos.close();
    }

    public Date getDate(String name) {
        File cache = new File(path(name));
        if (!cache.exists()) {
            return null;
        } else {
            return DateUtils.readMilliTimestamp(cache.lastModified());
        }
    }
}
