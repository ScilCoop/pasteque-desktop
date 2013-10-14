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

package com.openbravo.pos.util;

import java.awt.image.*;
import java.awt.*;
import javax.imageio.ImageIO;
import javax.swing.JTextArea;

import com.openbravo.pos.widgets.WidgetsBuilder;

public class ThumbNailBuilder {
    
    private Image m_imgdefault;
    private int m_width;
    private int m_height;
    
    /** Creates a new instance of ThumbNailBuilder */    
    public ThumbNailBuilder(int width, int height) {
        init(width, height, null);
    }
    
    public ThumbNailBuilder(int width, int height, Image imgdef) {
        init(width, height, imgdef);
      
    }
    
    public ThumbNailBuilder(int width, int height, String img) {
        
        Image defimg;
        try {
            init(width, height, ImageIO.read(getClass().getClassLoader().getResourceAsStream(img)));               
        } catch (Exception fnfe) {
            init(width, height, null);
        }                 
    }    
    
    private void init(int width, int height, Image imgdef) {
        m_width = width;
        m_height = height;
        if (imgdef == null) {
            m_imgdefault = null;
        } else {
            m_imgdefault = createThumbNail(imgdef);
        } 
    }
    
    public Image getThumbNail(Image img) {
   
        if (img == null) {
            return m_imgdefault;
        } else {
            return createThumbNail(img);
        }     
    }      
    
    public Image getThumbNailText(Image img, String text) {
                
        img = getThumbNail(img);
        
        BufferedImage imgtext = new BufferedImage(img.getWidth(null), img.getHeight(null),  BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = imgtext.createGraphics();
                
        // The text        
        JTextArea label = new JTextArea();
        label.setOpaque(false);
        label.setLineWrap(true);
        label.setWrapStyleWord(true);
	
        // Make font bold if text is not too long
        int wordsCount = text.split(" ").length;
        int charCount = text.length();
        label.setFont(WidgetsBuilder.getFont(WidgetsBuilder.SIZE_SMALL));
        label.setText(text);
        label.setSize(imgtext.getWidth(), imgtext.getHeight()); // Force for preferredSize
        Dimension d = label.getPreferredSize();
        label.setBounds(0, 0, imgtext.getWidth(), d.height);  
        
        // The background
        Color c1 = new Color(0xff, 0xff, 0xff, 0x40);
        Color c2 = new Color(0xff, 0xff, 0xff, 0xd0);

//        Point2D center = new Point2D.Float(imgtext.getWidth() / 2, label.getHeight());
//        float radius = imgtext.getWidth() / 3;
//        float[] dist = {0.1f, 1.0f};
//        Color[] colors = {c2, c1};        
//        Paint gpaint = new RadialGradientPaint(center, radius, dist, colors);
        Paint gpaint = new GradientPaint(new Point(0,0), c1, new Point(label.getWidth() / 2, 0), c2, true);
        
        g2d.drawImage(img, 0, 0, null);
        g2d.translate(0, imgtext.getHeight() - label.getHeight());
        g2d.setPaint(gpaint);            
        g2d.fillRect(0 , 0, imgtext.getWidth(), label.getHeight());    
        label.paint(g2d);
            
        g2d.dispose();
        
        return imgtext;    
    }
    
    private Image createThumbNail(Image img) {
        int targetw;
        int targeth;

        double scalex = (double) m_width / (double) img.getWidth(null);
        double scaley = (double) m_height / (double) img.getHeight(null);
        if (scalex < scaley) {
            targetw = m_width;
            targeth = (int) (img.getHeight(null) * scalex);
        } else {
            targetw = (int) (img.getWidth(null) * scaley);
            targeth = (int) m_height;
        }

        BufferedImage midimg = null;
        Graphics2D g2d = null;

        int imgw = img.getWidth(null);
        int imgh = img.getHeight(null);

        midimg = new BufferedImage(m_width, m_height, BufferedImage.TYPE_INT_ARGB);
        int x = (m_width > targetw) ? (m_width - targetw) / 2 : 0;
        int y = (m_height > targeth) ? (m_height - targeth) / 2 : 0;
        g2d = midimg.createGraphics();
        g2d.drawImage(img, x, y, x + targetw, y + targeth,
                               0, 0, imgw, imgh, null);
        g2d.dispose();
        return midimg;
    }    
}
