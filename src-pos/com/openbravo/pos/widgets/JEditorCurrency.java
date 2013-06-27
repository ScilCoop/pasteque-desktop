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

package com.openbravo.pos.widgets;

import com.openbravo.format.Formats;
import com.openbravo.pos.admin.CurrencyInfo;

public class JEditorCurrency extends JEditorNumber {
    
	private static final long serialVersionUID = 5096754100573262803L;
	
    private CurrencyInfo altCurrency;

	/** Creates a new instance of JEditorCurrency */
    public JEditorCurrency() {
    }

    public void setAltCurrency(CurrencyInfo currency) {
        this.altCurrency = currency;
    }

    protected Formats getFormat() {
        return Formats.CURRENCY;
    }
    protected String getTextFormat() {
        Formats.setAltCurrency(this.altCurrency);
        return this.getFormat().formatValue(getDoubleValue());
    }
    protected int getMode() {
        return EditorKeys.MODE_DOUBLE;
    }  
}
