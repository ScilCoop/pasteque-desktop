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

package fr.pasteque.data.user;

import java.util.*;
import fr.pasteque.data.loader.TableDefinition;
import fr.pasteque.basic.BasicException;
import fr.pasteque.data.loader.SentenceList;

public class ListProviderCreator implements ListProvider {

    private SentenceList sent;
    private EditorCreator prov;
    private Object params;

    /** Creates a new instance of ListProviderEditor */
    public ListProviderCreator(SentenceList sent, EditorCreator prov) {
        this.sent = sent;
        this.prov = prov;
        params = null;
    }

    public ListProviderCreator(SentenceList sent) {
        this(sent, null);
    }

    public ListProviderCreator(TableDefinition table) {
        this(table.getListSentence(), null);
    }

    public List loadData() throws BasicException {
        params = (prov == null) ? null : prov.createValue();
        return refreshData();
    }

    public List refreshData() throws BasicException {
        return sent.list(params);
    }
}
