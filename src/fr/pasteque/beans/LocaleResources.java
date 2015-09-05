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

package fr.pasteque.beans;

import java.text.MessageFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;
import gnu.gettext.GettextResource;
import java.util.Locale;

/**
 *
 * @author adrian
 */
public class LocaleResources {
  
    private List<ResourceBundle> m_resources;
//    private ClassLoader m_localeloader;
    
    /** Creates a new instance of LocaleResources */
    public LocaleResources() {
        m_resources = new LinkedList<ResourceBundle>();
        
//        File fuserdir = new File(System.getProperty("user.dir"));
//        File fresources = new File(fuserdir, "locales");
//        try {
//            m_localeloader = URLClassLoader.newInstance(
//                    new URL[] { fresources.toURI().toURL() },
//                    Thread.currentThread().getContextClassLoader());
//        } catch (MalformedURLException e) {
//            m_localeloader = Thread.currentThread().getContextClassLoader();
//        }        
    }
    
//    public ResourceBundle getBundle(String bundlename) {
//        return ResourceBundle.getBundle(bundlename, Locale.getDefault(), m_localeloader);
//    }
    
    public void addBundleName(String bundlename) {
//        m_resources.add(getBundle(bundlename));
        m_resources.add(ResourceBundle.getBundle(bundlename, Locale.FRANCE));
    }    
    
    public String getString(String sKey) {
        
        if (sKey == null) {
            return null;
        } else  {            
            for (ResourceBundle r : m_resources) {
                String msg = GettextResource.gettext(r, sKey);
                if (!msg.equals(sKey)) {
                    return msg; // else check for next bundle
                }
            }
            
            // MissingResourceException in all ResourceBundle
            return sKey;
        }
    }

    public String getString(String sKey, Object ... sValues) {
        
        if (sKey == null) {
            return null;
        } else  {
            String msg = sKey;
            for (ResourceBundle r : m_resources) {
                msg = GettextResource.gettext(r, sKey);
                if (!msg.equals(sKey)) {
                    return MessageFormat.format(msg, sValues);
                }
            }
            // MissingResourceException in all ResourceBundle
            try {
                return MessageFormat.format(msg, sValues);
            } catch (IllegalArgumentException e) {
                return msg;
            }
        }
    }

}
