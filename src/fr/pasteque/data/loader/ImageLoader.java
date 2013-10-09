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

package fr.pasteque.data.loader;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.File;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.imageio.ImageIO;

public class ImageLoader {

    private static Logger logger = Logger.getLogger("fr.pasteque.data.loader.ImageLoader");

    public static ImageIcon readImageIcon(String name) {
        BufferedImage image = readImage(name);
        ImageIcon icon = new ImageIcon(image);
        return icon;
    }
    
    public static ImageIcon readImageIcon(String name, String locale) {
        BufferedImage image = readImage(name, locale);
        ImageIcon icon = new ImageIcon(image);
        return icon;
    }

    public static BufferedImage readImage(String name) {
        return readImage(name, null);
    }

    public static BufferedImage readImage(String name, String locale) {
        String fullLocName = null;
        String locName = null;
        if (locale != null) {
            String[] loc = locale.split("_");
            String lang = loc[0].replace(".", "").replace("/", "");
            String country = null;
            if (loc.length > 1) {
                country = loc[1].replace(".", "").replace("/", "");
                fullLocName = lang + "/" + country + "/" + name;
            }
            locName = lang + "/" + name;
        }
        try {
            String base = System.getProperty("dirname.path");
            if (base == null) {
                base = ".";
            }
            if (locale != null) {
                // Try first with localized versions and fallback to generic
                File f = new File(base + "/res/images/" + fullLocName);
                if (f.exists()) {
                    BufferedImage image = ImageIO.read(f);
                    return image;
                } else {
                    f = new File(base + "/res/images/" + locName);
                    if (f.exists()) {
                        BufferedImage image = ImageIO.read(f);
                        return image;
                    }
                }
                // No localized version, fallback
            }
            File f = new File(base + "/res/images/" + name);
            if (f.exists()) {
                BufferedImage image = ImageIO.read(f);
                return image;
            }
        } catch (IOException ioe) {
            logger.throwing("Unable to read image " + name, "readImage", ioe);
            ioe.printStackTrace();
        }
        try {
            return ImageIO.read(ImageLoader.class.getResource("/fr.pasteque.images/broken.png"));
        } catch (IOException ioe) {
            // Should never happen
            ioe.printStackTrace();
            return null;
        }
    }

}
