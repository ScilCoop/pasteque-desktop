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
import fr.pasteque.data.loader.DataRead;
import fr.pasteque.data.loader.SerializableRead;
import fr.pasteque.data.loader.DataWrite;
import fr.pasteque.data.loader.SerializableWrite;
import fr.pasteque.basic.BasicException;
import fr.pasteque.data.loader.IKeyed;
import fr.pasteque.data.loader.ImageUtils;

/**
 *
 * @author  Luis Ig. Bacas Riveiro	lbacas@opensistemas.com
 * @author  Pablo J. Urbano Santos	purbano@opensistemas.com
 */
public class SubgroupInfo implements SerializableRead, SerializableWrite, IKeyed {

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
    
    /**
     * Devuelve el identificador del objeto
     * @return  id
     */
    public Object getKey() {
        return m_sID;
    }
    
    /**
     * Lee los parámetros del objeto del DataRead
     * @param dr DataRead del que lee los datos
     * @throws fr.pasteque.basic.BasicException
     * @see DataRead
     */
    public void readValues(DataRead dr) throws BasicException {
        m_sID = dr.getInt(1);
        m_sName = dr.getString(2);
        m_Image = ImageUtils.readImage(dr.getBytes(3));
        Integer order = dr.getInt(4);
        if (order == null) {
            this.dispOrder = 0;
        } else {
            this.dispOrder = order.intValue();
        }
    }
    
    /**
     * Crea un DataWrite a partir de los parámetros del objeto
     * @param dp DataWrite en el que escribe los datos
     * @throws fr.pasteque.basic.BasicException
     * @see DataWrite
     */
    public void writeValues(DataWrite dp) throws BasicException {
        dp.setInt(1, m_sID);
        dp.setString(2, m_sName);
        dp.setBytes(3, ImageUtils.writeImage(m_Image));
        dp.setInt(4, this.dispOrder);
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
