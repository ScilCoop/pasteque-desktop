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

package com.openbravo.pos.config;

import java.util.Locale;
import com.openbravo.pos.forms.AppConfig;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author  adrianromero
 */
public class JPanelConfigLocale extends PanelConfig {
    
    private final static String DEFAULT_VALUE = "(Default)";

    /** Creates new form JPanelConfigLocale */
    public JPanelConfigLocale() {
        
        initComponents();
        
        List<Locale> availablelocales = new ArrayList<Locale>();
        availablelocales.addAll(Arrays.asList(Locale.getAvailableLocales())); // Available java locales
        addLocale(availablelocales, new Locale("eu", "ES", "")); // Basque
        addLocale(availablelocales, new Locale("gl", "ES", "")); // Gallegan
        
        Collections.sort(availablelocales, new LocaleComparator());
        
        jcboLocale.addItem(new LocaleInfo (null));
        for (Locale l : availablelocales) {
            jcboLocale.addItem(new LocaleInfo(l));
        }
        
        jcboInteger.addItem(DEFAULT_VALUE);
        jcboInteger.addItem("#0");
        jcboInteger.addItem("#,##0");
        
        jcboDouble.addItem(DEFAULT_VALUE);
        jcboDouble.addItem("#0.0");
        jcboDouble.addItem("#,##0.#");
        
        jcboCurrency.addItem(DEFAULT_VALUE);
        jcboCurrency.addItem("\u00A4 #0.00");
        jcboCurrency.addItem("'$' #,##0.00");
        
        jcboPercent.addItem(DEFAULT_VALUE);
        jcboPercent.addItem("#,##0.##%");
        
        jcboDate.addItem(DEFAULT_VALUE);
//        jcboDate.addItem(DEFAULT_VALUE);
        
        jcboTime.addItem(DEFAULT_VALUE);
        
        jcboDatetime.addItem(DEFAULT_VALUE);
               
    }

    private void addLocale(List<Locale> ll, Locale l) {
        if (!ll.contains(l)) {
            ll.add(l);
        }
    }
    
    public void loadProperties(AppConfig config) {
        
        String slang = config.getProperty("user.language");
        String scountry = config.getProperty("user.country");
        String svariant = config.getProperty("user.variant");
        
        if (slang != null && !slang.equals("") && scountry != null && svariant != null) {                    
            Locale currentlocale = new Locale(slang, scountry, svariant);
            for (int i = 0 ; i < jcboLocale.getItemCount(); i++)  {
                LocaleInfo l = (LocaleInfo) jcboLocale.getItemAt(i);
                if (currentlocale.equals(l.getLocale())) {
                    jcboLocale.setSelectedIndex(i);
                    break;
                }
            }        
        } else {
            jcboLocale.setSelectedIndex(0);
        }
        
        jcboInteger.setSelectedItem(writeWithDefault(config.getProperty("format.integer")));
        jcboDouble.setSelectedItem(writeWithDefault(config.getProperty("format.double")));
        jcboCurrency.setSelectedItem(writeWithDefault(config.getProperty("format.currency")));
        jcboPercent.setSelectedItem(writeWithDefault(config.getProperty("format.percent")));
        jcboDate.setSelectedItem(writeWithDefault(config.getProperty("format.date")));
        jcboTime.setSelectedItem(writeWithDefault(config.getProperty("format.time")));
        jcboDatetime.setSelectedItem(writeWithDefault(config.getProperty("format.datetime")));
               
        dirty.setDirty(false);
    }
    
    public void saveProperties(AppConfig config) {
        
        Locale l = ((LocaleInfo) jcboLocale.getSelectedItem()).getLocale();
        if (l == null) {
            config.setProperty("user.language", "");
            config.setProperty("user.country", "");
            config.setProperty("user.variant", "");
        } else {
            config.setProperty("user.language", l.getLanguage());
            config.setProperty("user.country", l.getCountry());
            config.setProperty("user.variant", l.getVariant());
        }
         
        config.setProperty("format.integer", readWithDefault(jcboInteger.getSelectedItem()));
        config.setProperty("format.double", readWithDefault(jcboDouble.getSelectedItem()));
        config.setProperty("format.currency", readWithDefault(jcboCurrency.getSelectedItem()));
        config.setProperty("format.percent", readWithDefault(jcboPercent.getSelectedItem()));
        config.setProperty("format.date", readWithDefault(jcboDate.getSelectedItem()));
        config.setProperty("format.time", readWithDefault(jcboTime.getSelectedItem()));
        config.setProperty("format.datetime", readWithDefault(jcboDatetime.getSelectedItem()));
        
        dirty.setDirty(false);
    }
    
    private String readWithDefault(Object value) {
        if (DEFAULT_VALUE.equals(value)) {
            return "";
        } else {
            return value.toString();
        }
    }
    
    private Object writeWithDefault(String value) {
        if (value == null || value.equals("") || value.equals(DEFAULT_VALUE)) {
            return DEFAULT_VALUE;
        } else {
            return value.toString();
        }
    }
    
    private static class LocaleInfo {
        private Locale locale;
        
        public LocaleInfo(Locale locale) {
            this.locale = locale;
        }
        public Locale getLocale() {
            return locale;
        }
        @Override
        public String toString() {
            return locale == null 
                    ? "(System default)" 
                    : locale.getDisplayName();
        }
    }
    
    private void initComponents() {
        this.addOptionsContainer("label.locale");
        this.jcboLocale = this.addComboBoxParam("label.locale");
        this.jcboInteger = this.addComboBoxParam("label.integer");
        this.jcboInteger.setEditable(true);
        this.jcboDouble = this.addComboBoxParam("label.double");
        this.jcboDouble.setEditable(true);
        this.jcboCurrency = this.addComboBoxParam("label.currency");
        this.jcboCurrency.setEditable(true);
        this.jcboPercent = this.addComboBoxParam("label.percent");
        this.jcboPercent.setEditable(true);
        this.jcboDate = this.addComboBoxParam("label.date");
        this.jcboDate.setEditable(true);
        this.jcboTime = this.addComboBoxParam("label.time");
        this.jcboTime.setEditable(true);
        this.jcboDatetime = this.addComboBoxParam("label.datetime");
        this.jcboDatetime.setEditable(true);
    }
    
    private javax.swing.JComboBox jcboCurrency;
    private javax.swing.JComboBox jcboDate;
    private javax.swing.JComboBox jcboDatetime;
    private javax.swing.JComboBox jcboDouble;
    private javax.swing.JComboBox jcboInteger;
    private javax.swing.JComboBox jcboLocale;
    private javax.swing.JComboBox jcboPercent;
    private javax.swing.JComboBox jcboTime;
    
}
