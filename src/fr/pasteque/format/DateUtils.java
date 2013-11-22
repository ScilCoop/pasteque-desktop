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

package fr.pasteque.format;

import java.util.Calendar;
import java.util.Date;

public abstract class DateUtils {

    /** Convert a YYYY-MM-DD string to a Date */
    public static Date readDate(String date) {
        if (!date.matches("\\A\\d{4}-\\d{2}-\\d{2}\\z")) {
            throw new IllegalArgumentException("Date must be YYYY-MM-DD, found " + date);
        }
        Calendar c = Calendar.getInstance();
        String[] parts = date.split("-");
        int year = Integer.parseInt(parts[0]);
        int month = Integer.parseInt(parts[1]);
        int day = Integer.parseInt(parts[2]);
        c.set(year, month - 1, day);
        return c.getTime();
    }

    /** Convert a timestamp in seconds to a Date */
    public static Date readSecTimestamp(long timestamp) {
        return new Date(timestamp * 1000);
    }

    public static long toSecTimestamp(Date date) {
        return date.getTime() / 1000;
    }
}