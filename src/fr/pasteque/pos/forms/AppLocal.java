//    POS-Tech
//    Based upon Openbravo POS
//
//    Copyright (C) 2007-2009 Openbravo, S.L.
//                       2012 Scil (http://scil.coop)
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

import fr.pasteque.beans.LocaleResources;

/**
 *
 * @author adrianromero
 */
public class AppLocal {
    
    public static final String APP_NAME = "Pasteque";
    public static final String APP_ID = "pasteque";
    public static final String APP_VERSION = "2.0 alpha 8";
    public static final String DB_VERSION = "5";
  
    // private static List<ResourceBundle> m_messages;
    private static LocaleResources resources;

    static {
        AppLocal.resources = new LocaleResources();
        AppLocal.resources.addBundleName("pos_messages");
    }

    /** Creates a new instance of AppLocal */
    private AppLocal() {
    }

    public static String getIntString(String sKey) {
        return AppLocal.resources.getString(sKey);
    }

    public static String getIntString(String sKey, Object ... sValues) {
        return AppLocal.resources.getString(sKey, sValues);
    }
}
