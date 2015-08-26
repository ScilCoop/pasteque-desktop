//    POS-Tech
//
//    Copyright (C) 2012 Scil (http://scil.coop)
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


package fr.pasteque.pos.widgets;

import fr.pasteque.data.loader.ImageLoader;
import fr.pasteque.pos.forms.AppConfig;

import java.awt.image.*;
import java.awt.*;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JTextArea;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JPasswordField;

public class WidgetsBuilder {

    public static final int SIZE_SMALL = 0;
    public static final int SIZE_MEDIUM = 1;
    public static final int SIZE_BIG = 2;
    public static final int SIZE_HUDGE = 3;

    private WidgetsBuilder() {}

    public static JButton createButton(ImageIcon icon) {
        return WidgetsBuilder.createButton(icon, SIZE_MEDIUM);
    }

    public static JButton createButton(String text) {
        return WidgetsBuilder.createButton(null, text, SIZE_MEDIUM);
    }

    public static JButton createButton(ImageIcon icon, int size) {
        JButton btn = new JButton();
        btn.setIcon(icon);
        WidgetsBuilder.adaptSize(btn, size);
        return btn;
    }

    public static JButton createButton(ImageIcon icon, String text, int size) {
        JButton btn = new JButton();
        btn.setText("  " + text + "  ");
        btn.setIcon(icon);
        WidgetsBuilder.adaptSize(btn, size);
        return btn;
    }

    /**Creates a button with icon and tooltiptext
     * @param icon  Button's icon
     * @param tooltiptext  Button's tooltiptext
     * @return  this modified button
     */
     public static JButton createButton(ImageIcon icon, String toolTipText) {
        JButton btn = new JButton();
        btn.setIcon(icon);
        btn.setToolTipText(toolTipText);
        WidgetsBuilder.adaptSize(btn, SIZE_MEDIUM);
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
                case SIZE_HUDGE:
                    minWidth = pixelSize(Float.parseFloat(cfg.getProperty("ui.touchhudgebtnminwidth")));
                    minHeight = pixelSize(Float.parseFloat(cfg.getProperty("ui.touchhudgebtnminheight")));
                    break;
                case SIZE_MEDIUM:
                default:
                    minWidth = pixelSize(Float.parseFloat(cfg.getProperty("ui.touchbtnminwidth")));
                    minHeight = pixelSize(Float.parseFloat(cfg.getProperty("ui.touchbtnminheight")));
                    break;
                }

                int width = (int) widget.getPreferredSize().getWidth();
                int height = (int) widget.getPreferredSize().getHeight();
                if (width < minWidth) {
                    width = minWidth;
                }
                if (height < minHeight) {
                    height = minHeight;
                }
                widget.setPreferredSize(new Dimension(width, height));
                widget.setMinimumSize(new Dimension(width, height));
                widget.setMaximumSize(new Dimension(width, height));

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
        return createImportantLabel(null);
    }

    public static JLabel createImportantLabel(String text) {
        JLabel lbl = new JLabel(text);
        setupLabel(lbl, SIZE_BIG);

        return lbl;
    }

    public static JLabel createSmallLabel() {
        return createSmallLabel(null);
    }

    public static JLabel createSmallLabel(String text) {
        JLabel lbl = new JLabel(text);
        setupLabel(lbl, SIZE_SMALL);
        return lbl;
    }

    /** Make a label looks like an input field. */
    public static void inputStyle(JLabel label) {
        label.setBackground(java.awt.Color.white);
        label.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        label.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createLineBorder(javax.swing.UIManager.getDefaults().getColor("Button.darkShadow")), javax.swing.BorderFactory.createEmptyBorder(1, 4, 1, 4)));
        label.setOpaque(true);
        label.setRequestFocusEnabled(false);
    }

    public static JTextField createTextField() {
        JTextField field = new JTextField();
        field.setFont(getFont(SIZE_MEDIUM));
        return field;
    }
    public static JPasswordField createPasswordField() {
        JPasswordField field = new JPasswordField();
        field.setFont(getFont(SIZE_MEDIUM));
        return field;
    }
    public static JComboBox createComboBox() {
        JComboBox box = new JComboBox();
        box.setFont(getFont(SIZE_MEDIUM));
        return box;
    }
    public static JCheckBox createCheckBox() {
        JCheckBox box = new JCheckBox();
        AppConfig cfg = AppConfig.loadedInstance;
        if (cfg != null) {
            if (cfg.getProperty("machine.screentype").equals("touchscreen")) {
                adaptSize(box, SIZE_MEDIUM);
            }
        }
        return box;
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

    public static ImageIcon createIcon(String name) {
        ImageIcon icon = ImageLoader.readImageIcon(name);
        return createIcon(icon);
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
