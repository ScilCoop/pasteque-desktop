//    Nicolas juillet 2013
//	  Openbravo POS is a point of sales application designed for touch screens.
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

package com.openbravo.pos.printer.escpos;

public class UnicodeTranslatorGlancetron extends UnicodeTranslator {

    /** Creates a UnicodeTranslatorStar instance of UnicodeTranslatorInt */
    public UnicodeTranslatorGlancetron() {
    }

    public byte[] getCodeTable() {
        return new byte[] {0x1B, 0x1D, 0x74, 0x01}; // Select code page 437
       // return ESCPOS.CODE_TABLE_00;    
    }

    public byte transChar(char sChar) {
        if ((sChar >= 0x0000) && (sChar < 0x0080)) {
            return (byte) sChar;
        } else {
            switch (sChar) { 
                // unicode return, as comment equivalent in GT8035 manual; 
                // list is organize based on comment hex code //0x80... 
                // simply write equivalent unicode - display code        
            case '\u20AC': return (byte) -0xF9;// euro sign
            case '\u00C7': return -0x80; // C cedilla
            case '\u00E9': return -0x7E; // e dieresis
            case '\u00E2': return -0x7D; // é
            case '\u00E4': return -0x7C; // a dieresis
            case '\u00E0': return -0x7B; // diese
            case '\u00E5': return -0x7A; // a circle
            case '\u00E7': return -0x79; // c cedilla
            case '\u00EA': return -0x78; // ê
            case '\u00EB': return 0x77; // ë
            case '\u00E8': return -0x76; // è
            case '\u00EF': return -0x75; // ï
            case '\u00EE': return -0x74; // î
            case '\u00EC': return -0x73; // ì
            case '\u00F4': return -0x6D; // ô
            case '\u00F2': return -0x6B; // ò
            case '\u00FB': return -0x6A; // û
            case '\u00FC': return -0x69; // ù
            case '\u00E3': return -0x66; // ü u dieresesis
            case '\u00F1': return -0x65; // ñ n tilde
            case '\u00D1': return -0x64; // Ñ N tilde
            case '\u00E1': return -0x60; // a acute
            case '\u00ED': return -0x5F; // i acute
            case '\u00F3': return -0x5E; // o acute
            case '\u00FA': return -0x5D; // u acute
            case '\u00B0': return -0x4E; // °
            case '\u00B4': return -0x4C; // ' 
            case '\u00A0': return -0x4C; // No-Break space; no equivalence in Glancetron table Apostrophe used
            default: return (byte) -0xC1;
            }
            
        }
    }

}
