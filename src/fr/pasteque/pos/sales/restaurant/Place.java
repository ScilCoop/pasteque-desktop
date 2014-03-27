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

package fr.pasteque.pos.sales.restaurant;

import java.awt.Dimension;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.SwingConstants;
import fr.pasteque.data.gui.NullIcon;
import fr.pasteque.data.loader.ImageLoader;
import fr.pasteque.basic.BasicException;
import org.json.JSONObject;

public class Place implements java.io.Serializable {
    
    private static final long serialVersionUID = 8652254694281L;
    private static final Icon ICO_OCU = ImageLoader.readImageIcon("tkt_place_filled.png");
    private static final Icon ICO_FRE = new NullIcon(22, 22);
    
    private String m_sId;
    private String m_sName;
    private int m_ix;
    private int m_iy;
    private String m_sfloor;
    
    private boolean m_bPeople;
    private JButton m_btn;
        
    public Place(JSONObject o) {
        this.m_sId = o.getString("id");
        this.m_sName = o.getString("label");
        this.m_ix = o.getInt("x");
        this.m_iy = o.getInt("y");
        this.m_sfloor = o.getString("floorId");
        
        m_bPeople = false;
        m_btn = new JButton();

        m_btn.setFocusPainted(false);
        m_btn.setFocusable(false);
        m_btn.setRequestFocusEnabled(false);
        m_btn.setHorizontalTextPosition(SwingConstants.CENTER);
        m_btn.setVerticalTextPosition(SwingConstants.BOTTOM);
        m_btn.setIcon(ICO_FRE);
        m_btn.setText(m_sName);
    }

    public String getId() { return m_sId; }
    
    public String getName() { return m_sName; }

    public int getX() { return m_ix; }

    public int getY() { return m_iy; }

    public String getFloor() { return m_sfloor; }
   
    public JButton getButton() { return m_btn; }

    public boolean hasPeople() {
        return m_bPeople;
    }   
    public void setPeople(boolean bValue, int custCount) {
        m_bPeople = bValue;
        m_btn.setIcon(bValue ? ICO_OCU : ICO_FRE);
        if (custCount > 0) {
            m_btn.setText(m_sName + " (" + custCount + ")");
            m_btn.setPreferredSize(null);
            m_btn.revalidate();
        } else {
            m_btn.setText(m_sName);
            m_btn.setPreferredSize(null);
            m_btn.revalidate();
        }
    }
    public void setButtonBounds() {
        Dimension d = m_btn.getPreferredSize();
        d.setSize(d.getWidth() + 30, d.getHeight());
        m_btn.setBounds(m_ix - d.width / 2, m_iy - d.height / 2,
                d.width, d.height);
    }
}
