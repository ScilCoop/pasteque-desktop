//    Openbravo POS is a point of sales application designed for touch screens.
//    Copyright (C) 2007-2009, 2012 Openbravo, S.L.
//                                  Cédric Houbart (cedric@scil.coop)
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

package com.openbravo.pos.util;

import com.openbravo.pos.forms.AppConfig;

import java.awt.image.*;
import java.awt.*;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JTextArea;
import javax.swing.JButton;

public class WidgetsBuilder {
    
    private WidgetsBuilder() {}
    
    public static JButton createButton(ImageIcon icon) {
    	JButton btn = new JButton();
    	btn.setIcon(icon);
    	WidgetsBuilder.adaptSize(btn);
    	return btn;
    }
    
    public static void adaptSize(Component widget) {
        AppConfig cfg = AppConfig.loadedInstance;
    	if (cfg != null) {
    	    if (cfg.getProperty("machine.screentype").equals("touchscreen")) {
                int minWidth = pixelSize(Float.parseFloat(cfg.getProperty("ui.touchbtnminwidth")));
                int minHeight = pixelSize(Float.parseFloat(cfg.getProperty("ui.touchbtnminheight")));
                int width = (int) widget.getPreferredSize().getWidth();
                int height = (int) widget.getPreferredSize().getHeight();
                widget.setMinimumSize(new Dimension(minWidth, minHeight));
                // Some layout ignore minimum size, adjust preferred size in case
                if (width < minWidth) {
                    widget.setPreferredSize(new Dimension(minWidth, height));
                    width = minWidth;
                }
                if (height < minHeight) {
                    widget.setPreferredSize(new Dimension(width, minHeight));
                }
    	    }
    	}
    }
    
    private static int pixelSize(float inchSize) {
        AppConfig cfg = AppConfig.loadedInstance;
        int density = Integer.parseInt(cfg.getProperty("machine.screendensity"));
        return (int)(inchSize * density);
    }
    
    public static ImageIcon createIcon(ImageIcon icon) {
        AppConfig cfg = AppConfig.loadedInstance;
        if (cfg != null) {
            if (cfg.getProperty("machine.screentype").equals("touchscreen")) {
    	        int minWidth = pixelSize(Float.parseFloat(cfg.getProperty("ui.touchbtnminwidth")));
                int minHeight = pixelSize(Float.parseFloat(cfg.getProperty("ui.touchbtnminheight")));
                return new TouchIcon(icon, minWidth, minHeight);
            }
        }
        return icon;
    }
    
    public static class TouchIcon extends ImageIcon {
    	private int fullHeight;
    	private int fullWidth;
    	private ImageIcon icon;
    	
    	public TouchIcon(ImageIcon baseIcon, int fullWidth, int fullHeight) {
    		this.icon = baseIcon;
    		this.fullWidth = fullWidth;
    		this.fullHeight = fullHeight;
    	}
    	
    	@Override
    	public int getIconHeight() {
    	    return Math.max(this.fullHeight, this.icon.getIconHeight());
    	}
    	
    	@Override
    	public int getIconWidth() {
    	    return Math.max(this.fullWidth, this.icon.getIconWidth());
    	}
    	
    	@Override
    	public void paintIcon(Component c, Graphics g, int x, int y) {
            int marginTop = 0;
            int marginLeft = 0;
            if (this.fullHeight > 0 
                && this.icon.getIconHeight() < this.fullHeight) {
                marginTop = (this.fullHeight - this.icon.getIconHeight())/2;
            }
            if (this.fullWidth > 0 
                && this.icon.getIconWidth() < this.fullWidth) {
                marginLeft = (this.fullWidth - this.icon.getIconWidth())/2;
            }
            g.translate(marginLeft, marginTop);
            this.icon.paintIcon(c, g, x, y);
            g.translate(-marginLeft, -marginTop);
        }
    }
}
