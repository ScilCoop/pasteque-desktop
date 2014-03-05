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

package fr.pasteque.pos.admin;

import fr.pasteque.basic.BasicException;
import fr.pasteque.data.loader.DataRead;
import fr.pasteque.data.loader.IKeyed;
import fr.pasteque.data.loader.SerializerRead;

import java.io.Serializable;
import org.json.JSONObject;

public class CurrencyInfo implements IKeyed, Serializable {
    
    private static final long serialVersionUID = 4680364653429L;
    private Integer id;
    private String name;
    private String symbol;
    private String decimal;
    private String thousands;
    private String format;
    private double rate;
    private boolean main;
    private boolean active;

    /** Creates a new instance of FloorsInfo */
    public CurrencyInfo() {
    }
    public CurrencyInfo(JSONObject o) {
        this.id = o.getInt("id");
        this.name = o.getString("label");
        this.symbol = o.getString("symbol");
        this.decimal = o.getString("decimalSeparator");
        this.thousands = o.getString("thousandsSeparator");
        this.format = o.getString("format");
        this.rate = o.getDouble("rate");
        this.main = o.getBoolean("isMain");
        this.active = o.getBoolean("isActive");
    }

    public Object getKey() {
        return this.id;
    }

    public static SerializerRead getSerializerRead() {
        return new SerializerRead() {
            public Object readValues(DataRead dr) throws BasicException {
                CurrencyInfo curr = new CurrencyInfo();
                curr.id = dr.getInt(1);
                curr.name = dr.getString(2);
                curr.symbol = dr.getString(3);
                curr.decimal = dr.getString(4);
                curr.thousands = dr.getString(5);
                curr.format = dr.getString(6);
                curr.rate = dr.getDouble(7);
                curr.main = dr.getBoolean(8);
                return curr;
            }
        };
    } 

    public double convertToMain(double amount) {
        if (this.main) {
            return amount;
        } else {
            return amount * this.rate;
        }
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
