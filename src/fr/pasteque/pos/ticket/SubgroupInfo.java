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

import java.awt.image.*;
import fr.pasteque.basic.BasicException;
import fr.pasteque.data.loader.ImageUtils;

import java.io.Serializable;
import org.json.JSONObject;

/**
 *
 * @author  Luis Ig. Bacas Riveiro	lbacas@opensistemas.com
 * @author  Pablo J. Urbano Santos	purbano@opensistemas.com
 */
public class SubgroupInfo implements Serializable {

    private Integer m_sID;
    private String m_sName;
    private BufferedImage m_Image;
    private int dispOrder;
    
    /** Constructor por defecto
     */
    public SubgroupInfo() {
        m_sID = null;
        m_sName = null;
        m_Image = null;
        dispOrder = 0;
    }

    public SubgroupInfo(JSONObject o) {
        this.m_sID = o.getInt("id");
        this.m_sName = o.getString("label");
        this.dispOrder = o.getInt("dispOrder");
    }

    public void setID(Integer sID) {
        m_sID = sID;
    }
    
    public Integer getID() {
        return m_sID;
    }

    public String getName() {
        return m_sName;
    }
    
    public void setName(String sName) {
        m_sName = sName;
    }
    
    public BufferedImage getImage() {
        return m_Image;
    }
    
    public void setImage(BufferedImage img) {
        m_Image = img;
    }

    public int getDispOrder() {
        return this.dispOrder;
    }

    public void setDispOrder(int order) {
        this.dispOrder = order;
    }

    /**
     * Devuelve una cadena con el nombre del objeto
     * @return nombre
     */
    @Override
    public String toString(){
        return m_sName;
    }
}
