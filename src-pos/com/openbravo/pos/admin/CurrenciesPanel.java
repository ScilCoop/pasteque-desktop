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

package com.openbravo.pos.admin;

import javax.swing.ListCellRenderer;
import com.openbravo.data.gui.ListCellRendererBasic;
import com.openbravo.data.loader.ComparatorCreator;
import com.openbravo.pos.forms.AppLocal;
import com.openbravo.pos.panels.*;
import com.openbravo.data.loader.TableDefinition;
import com.openbravo.data.loader.Vectorer;
import com.openbravo.data.user.EditorRecord;
import com.openbravo.data.user.SaveProvider;
import com.openbravo.data.user.ListProvider;
import com.openbravo.data.user.ListProviderCreator;
import com.openbravo.pos.forms.DataLogicSales;

public class CurrenciesPanel extends JPanelTable {
    
    private TableDefinition tcurrencies;
    private CurrenciesEditor jeditor;
    private DataLogicSales dlSales;
    
    /** Creates a new instance of JPanelCategories */
    public CurrenciesPanel() {        
    }   
    
    protected void init() {   
        dlSales = (DataLogicSales) app.getBean("com.openbravo.pos.forms.DataLogicSales");           
        tcurrencies = dlSales.getTableCurrencies();
        jeditor = new CurrenciesEditor(app, dirty);    
    }
    
    public ListProvider getListProvider() {
        return new ListProviderCreator(tcurrencies);
    }
    
    public SaveProvider getSaveProvider() {
        return new SaveProvider(
            dlSales.getCurrencyUpdate(),
            dlSales.getCurrencyInsert(),
            dlSales.getCurrencyDelete());
    }
    
    public Vectorer getVectorer() {
        return tcurrencies.getVectorerBasic(new int[]{1});
    }
    
    public ComparatorCreator getComparatorCreator() {
        return tcurrencies.getComparatorCreator(new int[]{1});
    }
    
    public ListCellRenderer getListCellRenderer() {
        return new ListCellRendererBasic(tcurrencies.getRenderStringBasic(new int[]{1}));
    }
    
    public EditorRecord getEditor() {
        return jeditor;
    }
    
    public String getTitle() {
        return AppLocal.getIntString("Menu.Currencies");
    }
    
    public boolean requiresOpenedCash() {
        return false;
    }
}
