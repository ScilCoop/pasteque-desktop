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

import com.openbravo.basic.BasicException;
import com.openbravo.data.loader.DataRead;
import com.openbravo.data.loader.IKeyed;
import com.openbravo.data.loader.SerializableRead;

public class CurrencyInfo implements SerializableRead, IKeyed {
    
    private static final long serialVersionUID = 4680364653429L;
    private Integer id;
    private String name;
    private String symbol;
    private String decimal;
    private String thousands;
    private String format;
    private double rate;
    private boolean main;

    /** Creates a new instance of FloorsInfo */
    public CurrencyInfo() {
    }
    public CurrencyInfo(Object[] data) {
        this.id = (Integer) data[0];
        this.name = (String) data[1];
        this.symbol = (String) data[2];
        this.decimal = (String) data[3];
        this.thousands = (String) data[4];
        this.format = (String) data[5];
        this.rate = (Double) data[6];
        this.main = (Boolean) data[7];
    }

    public Object getKey() {
        return this.id;
    }

    public void readValues(DataRead dr) throws BasicException {
        this.id = dr.getInt(0);
        this.name = dr.getString(1);
        this.symbol = dr.getString(2);
        this.decimal = dr.getString(3);
        this.thousands = dr.getString(4);
        this.format = dr.getString(5);
        this.rate = dr.getDouble(6);
        this.main = dr.getBoolean(7);
    } 

    public void setID(Integer id) {
        this.id = id;
    }

    public Integer getID() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSymbol() {
        return this.symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getDecimal() {
        return this.decimal;
    }
    public void setDecimal(String decimal) {
        this.decimal = decimal;
    }

    public String getThousands() {
        return this.thousands;
    }
    public void setThousands(String thousands) {
        this.thousands = thousands;
    }

    public String getFormat() {
        return this.format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public double getRate() {
        return this.rate;
    }

    public void setRate(double rate) {
        this.rate = rate;
    }

    public boolean isMain() {
        return this.main;
    }

    public void setMain(boolean main) {
        this.main = main;
    }

    public String toString(){
        return this.name;
    }       
}
