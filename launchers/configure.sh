#!/bin/sh

#    POS-Tech
#    Based upon Openbravo POS
#
#    Copyright (C) 2008-2009 Openbravo, S.L.
#                       2012 Scil (http://scil.coop)
#
#    This file is part of POS-Tech.
#
#    POS-Tech is free software: you can redistribute it and/or modify
#    it under the terms of the GNU General Public License as published by
#    the Free Software Foundation, either version 3 of the License, or
#    (at your option) any later version.
#
#    POS-Tech is distributed in the hope that it will be useful,
#    but WITHOUT ANY WARRANTY; without even the implied warranty of
#    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
#    GNU General Public License for more details.
#
#    You should have received a copy of the GNU General Public License
#    along with POS-Tech.  If not, see <http://www.gnu.org/licenses/>.

DIRNAME=`dirname $0`

CP=$DIRNAME/pasteque.jar

CP=$CP:$DIRNAME/locales/
CP=$CP:$DIRNAME/lib/substance.jar


java -cp $CP -Djava.util.logging.config.file=$DIRNAME/logging.properties -Dswing.defaultlaf=javax.swing.plaf.metal.MetalLookAndFeel com.openbravo.pos.config.JFrmConfig
