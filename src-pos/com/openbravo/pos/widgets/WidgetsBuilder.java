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

package com.openbravo.pos.widgets;

import com.openbravo.pos.forms.AppConfig;

import java.awt.image.*;
import java.awt.*;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JTextArea;
import javax.swing.JButton;
import javax.swing.JLabel;

public class WidgetsBuilder {
    
    public static final int SIZE_SMALL = 0;
    public static final int SIZE_MEDIUM = 1;
    public static final int SIZE_BIG = 2;
    
    private WidgetsBuilder() {}
    
    public static JButton createButton(ImageIcon icon) {
    	return WidgetsBuilder.createButton(icon, SIZE_MEDIUM);
    }
    
    public static JButton createButton(ImageIcon icon, int size) {
    	JButton btn = new JButton();
    	btn.setIcon(icon);
    	WidgetsBuilder.adaptSize(btn, size);
    	return btn;
    }
    
    public static JButton createButton(ImageIcon icon, String text, int size) {
    	JButton btn = new JButton();
    	btn.setText(text);
    	btn.setIcon(icon);
    	WidgetsBuilder.adaptSize(btn, size);
    	return btn;
    }
    
    public static void adaptSize(Component widget, int size) {
        AppConfig cfg = AppConfig.loadedInstance;
    	if (cfg != null) {
    	    if (cfg.getProperty("machine.screentype").equals("touchscreen")) {
    	        int minWidth, minHeight = 0;
    	        switch (size) {
    	        case SIZE_SMALL:
    	            minWidth = pixelSize(Float.parseFloat(cfg.getProperty("ui.touchsmallbtnminwidth")));
                    minHeight = pixelSize(Float.parseFloat(cfg.getProperty("ui.touchsmallbtnminheight")));
    	            break;
    	        case SIZE_BIG:
    	            minWidth = pixelSize(Float.parseFloat(cfg.getProperty("ui.touchbigbtnminwidth")));
                    minHeight = pixelSize(Float.parseFloat(cfg.getProperty("ui.touchbigbtnminheight")));
    	            break;
    	        case SIZE_MEDIUM:
    	        default:
    	            minWidth = pixelSize(Float.parseFloat(cfg.getProperty("ui.touchbtnminwidth")));
                    minHeight = pixelSize(Float.parseFloat(cfg.getProperty("ui.touchbtnminheight")));
    	            break;
    	        }
                
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
    
    public static int pixelSize(float inchSize) {
        AppConfig cfg = AppConfig.loadedInstance;
        int density = Integer.parseInt(cfg.getProperty("machine.screendensity"));
        return (int)(inchSize * density);
    }
    
    public static int dipToPx(int px) {
        AppConfig cfg = AppConfig.loadedInstance;
        int density = Integer.parseInt(cfg.getProperty("machine.screendensity"));        
        return (int)((float)px * (float)density / 72.0);
    }
    
    public static Font getFont(int size) {
        AppConfig cfg = AppConfig.loadedInstance;
        int dipSize;
        int style;
        switch (size) {
        case SIZE_SMALL:
            dipSize = Integer.parseInt(cfg.getProperty("ui.fontsizesmall"));
            style = Font.PLAIN;
            break;
        case SIZE_BIG:
            dipSize = Integer.parseInt(cfg.getProperty("ui.fontsizebig"));
            style = Font.BOLD;
            break;
        case SIZE_MEDIUM:
        default:
            dipSize = Integer.parseInt(cfg.getProperty("ui.fontsize"));
            style = Font.PLAIN;
            break;
        }
        int fontSize = dipToPx(dipSize);
        return new Font("Dialog", style, fontSize);
    }
    
    /** Setup an existing label with config properties */
    public static void setupLabel(JLabel lbl, int size) {
        lbl.setFont(getFont(size));
    }
    
    public static JLabel createLabel(String text) {
        JLabel lbl = new JLabel();
        setupLabel(lbl, SIZE_MEDIUM);
        if (text != null) {
            lbl.setText(text);
        }
        return lbl;
    }
    
    public static JLabel createLabel() {
        return createLabel(null);
    }
    
    public static JLabel createImportantLabel() {
        JLabel lbl = new JLabel();
        setupLabel(lbl, SIZE_BIG);
        
        return lbl;
    }
    
    public static JLabel createSmallLabel() {
        JLabel lbl = new JLabel();
        setupLabel(lbl, SIZE_SMALL);
        return lbl;
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
