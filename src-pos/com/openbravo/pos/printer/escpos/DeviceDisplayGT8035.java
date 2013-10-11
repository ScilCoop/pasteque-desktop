//    Pasteque 30/07/2013;
//	Openbravo POS is a point of sales application designed for touch screens.
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

import com.openbravo.pos.printer.DeviceTicket;

public class DeviceDisplayGT8035 extends DeviceDisplaySerial {

    private UnicodeTranslator trans;

    /** Creates a new instance of DeviceDisplayESCPOS */
    public DeviceDisplayGT8035(PrinterWritter display, UnicodeTranslator trans) {
        this.trans = trans;
        init(display);
    }

    @Override
    public void initVisor() {
        display.init(ESCPOS.INIT);
        display.write(ESCPOS.SELECT_DISPLAY); // Al visor
        display.write(trans.getCodeTable());
        display.write(ESCPOS.VISOR_HIDE_CURSOR);
        display.write(ESCPOS.VISOR_CLEAR);
        display.write(ESCPOS.VISOR_HOME);
        display.flush();
    }

    public void repaintLines() {
        display.write(ESCPOS.SELECT_DISPLAY);
        display.write(ESCPOS.VISOR_CLEAR);
        try {
            Thread.sleep(70); // hardware display persistence
        }
        catch (InterruptedException e) {
        }
        display.write(ESCPOS.VISOR_HOME);
        display.write(trans.transString(DeviceTicket.alignLeft(m_displaylines.getLine1(), 20)));
        display.write(trans.transString(DeviceTicket.alignLeft(m_displaylines.getLine2(), 20)));
        try {
            Thread.sleep(300); // Make sur persistence is gone
        }
        catch (InterruptedException e) {
        }
        display.flush();
    }
}
