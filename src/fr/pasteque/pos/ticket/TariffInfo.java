//    Openbravo POS is a point of sales application designed for touch screens.
//    Copyright (C) 2008 Open Sistemas de Información Internet, S.L.
//    http://www.opensistemas.com
//    http://sourceforge.net/projects/openbravopos
//
//    This program is free software; you can redistribute it and/or modify
//    it under the terms of the GNU General Public License as published by
//    the Free Software Foundation; either version 2 of the License, or
//    (at your option) any later version.
//
//    This program is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//    GNU General Public License for more details.
//
//    You should have received a copy of the GNU General Public License
//    along with this program; if not, write to the Free Software
//    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

package fr.pasteque.pos.ticket;

import java.io.Serializable;
import org.json.JSONObject;

/**
 *
 * @author  Luis Ig. Bacas Riveiro	lbacas@opensistemas.com
 * @author  Pablo J. Urbano Santos	purbano@opensistemas.com
 */
public class TariffInfo implements Serializable {

    private int id;
    private String m_sName;
    private int m_iOrder;

    public TariffInfo(int id, String name) {
        this.id = id;
        m_sName = name;
        m_iOrder = 0;
    }

    public TariffInfo(JSONObject o) {
        this.id = o.getInt("id");
        this.m_sName = o.getString("label");
        this.m_iOrder = o.getInt("dispOrder");
    }

    public int getID() {
        return this.id;
    }

    public String getName() {
        return m_sName;
    }
    public void setName(String sName) {
        m_sName = sName;
    }


    public int getOrder() {
        return m_iOrder;
    }
    public void setOrder (int iOrder) {
        m_iOrder = iOrder;
    }

    public String toString(){
        return m_sName;
    }
}