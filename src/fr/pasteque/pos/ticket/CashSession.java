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

package fr.pasteque.pos.ticket;

import fr.pasteque.format.DateUtils;

import java.util.Date;
import org.json.JSONObject;

public class CashSession {

    private String id;
    private String host;
    private int sequence;
    private Date openDate;
    private Date closeDate;

    public CashSession(String id, String host, int sequence, Date openDate,
            Date closeDate) {
        this.id = id;
        this.host = host;
        this.sequence = sequence;
        this.openDate = openDate;
        this.closeDate = closeDate;
    }

    public CashSession(JSONObject o) {
        this.id = o.getString("id");
        this.host = o.getString("host");
        this.sequence = o.getInt("sequence");
        if (!o.isNull("openDate")) {
            this.openDate = DateUtils.readSecTimestamp(o.getLong("openDate"));
        }
        if (!o.isNull("closeDate")) {
            this.closeDate = DateUtils.readSecTimestamp(o.getLong("closeDate"));
        }

    }

    public String getId() {
        return this.id;
    }

    public String getHost() {
        return this.host;
    }

    public int getSequence() {
        return this.sequence;
    }

    public Date getOpenDate() {
        return this.openDate;
    }

    public Date getCloseDate() {
        return this.closeDate;
    }

    /** Cash is opened when usable (opened and not closed) */
    public boolean isOpened() {
        return this.openDate != null && this.closeDate == null;
    }

    public boolean wasOpened() {
        return this.openDate != null;
    }

    public boolean isClosed() {
        return this.closeDate == null;
    }
}