--    Openbravo POS is a point of sales application designed for touch screens.
--    Copyright (C) 2008-2010 Openbravo, S.L.
--    http://sourceforge.net/projects/openbravopos
--
--    This file is part of Openbravo POS.
--
--    Openbravo POS is free software: you can redistribute it and/or modify
--    it under the terms of the GNU General Public License as published by
--    the Free Software Foundation, either version 3 of the License, or
--    (at your option) any later version.
--
--    Openbravo POS is distributed in the hope that it will be useful,
--    but WITHOUT ANY WARRANTY; without even the implied warranty of
--    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
--    GNU General Public License for more details.
--
--    You should have received a copy of the GNU General Public License
--    along with Openbravo POS.  If not, see <http://www.gnu.org/licenses/>.

-- Database initial data script for DERBY
-- v2

INSERT INTO ROLES(ID, NAME, PERMISSIONS) VALUES('0', 'Rôle administrateur', $FILE{/com/openbravo/pos/templates/Role.Administrator.xml} );
INSERT INTO ROLES(ID, NAME, PERMISSIONS) VALUES('1', 'Rôle responsable', $FILE{/com/openbravo/pos/templates/Role.Manager.xml} );
INSERT INTO ROLES(ID, NAME, PERMISSIONS) VALUES('2', 'Rôle employé', $FILE{/com/openbravo/pos/templates/Role.Employee.xml} );
INSERT INTO ROLES(ID, NAME, PERMISSIONS) VALUES('3', 'Rôle invité', $FILE{/com/openbravo/pos/templates/Role.Guest.xml} );

INSERT INTO PEOPLE(ID, NAME, APPPASSWORD, ROLE, VISIBLE, IMAGE) VALUES ('0', 'Administrateur', NULL, '0', 1, NULL);
INSERT INTO PEOPLE(ID, NAME, APPPASSWORD, ROLE, VISIBLE, IMAGE) VALUES ('1', 'Responsable', NULL, '1', 1, NULL);
INSERT INTO PEOPLE(ID, NAME, APPPASSWORD, ROLE, VISIBLE, IMAGE) VALUES ('2', 'Employé', NULL, '2', 1, NULL);
INSERT INTO PEOPLE(ID, NAME, APPPASSWORD, ROLE, VISIBLE, IMAGE) VALUES ('3', 'Invité', NULL, '3', 1, NULL);

INSERT INTO RESOURCES(ID, NAME, RESTYPE, CONTENT) VALUES('0', 'Printer.Start', 0, $FILE{/com/openbravo/pos/templates/Printer.Start.xml});
INSERT INTO RESOURCES(ID, NAME, RESTYPE, CONTENT) VALUES('1', 'Printer.Ticket', 0, $FILE{/com/openbravo/pos/templates/Printer.Ticket.xml});
INSERT INTO RESOURCES(ID, NAME, RESTYPE, CONTENT) VALUES('2', 'Printer.Ticket2', 0, $FILE{/com/openbravo/pos/templates/Printer.Ticket2.xml});
INSERT INTO RESOURCES(ID, NAME, RESTYPE, CONTENT) VALUES('3', 'Printer.TicketPreview', 0, $FILE{/com/openbravo/pos/templates/Printer.TicketPreview.xml});
INSERT INTO RESOURCES(ID, NAME, RESTYPE, CONTENT) VALUES('4', 'Printer.TicketTotal', 0, $FILE{/com/openbravo/pos/templates/Printer.TicketTotal.xml});
INSERT INTO RESOURCES(ID, NAME, RESTYPE, CONTENT) VALUES('5', 'Printer.OpenDrawer', 0, $FILE{/com/openbravo/pos/templates/Printer.OpenDrawer.xml});
INSERT INTO RESOURCES(ID, NAME, RESTYPE, CONTENT) VALUES('6', 'Printer.Ticket.Logo', 1, $FILE{/com/openbravo/pos/templates/Printer.Ticket.Logo.png});
INSERT INTO RESOURCES(ID, NAME, RESTYPE, CONTENT) VALUES('7', 'Printer.TicketLine', 0, $FILE{/com/openbravo/pos/templates/Printer.TicketLine.xml});
INSERT INTO RESOURCES(ID, NAME, RESTYPE, CONTENT) VALUES('8', 'Printer.CloseCash', 0, $FILE{/com/openbravo/pos/templates/Printer.CloseCash.xml});
INSERT INTO RESOURCES(ID, NAME, RESTYPE, CONTENT) VALUES('9', 'Window.Logo', 1, $FILE{/com/openbravo/pos/templates/Window.Logo.png});
INSERT INTO RESOURCES(ID, NAME, RESTYPE, CONTENT) VALUES('10', 'Window.Title', 0, $FILE{/com/openbravo/pos/templates/Window.Title.txt});
INSERT INTO RESOURCES(ID, NAME, RESTYPE, CONTENT) VALUES('11', 'Ticket.Buttons', 0, $FILE{/com/openbravo/pos/templates/Ticket.Buttons.xml});
INSERT INTO RESOURCES(ID, NAME, RESTYPE, CONTENT) VALUES('12', 'Ticket.Line', 0, $FILE{/com/openbravo/pos/templates/Ticket.Line.xml});
INSERT INTO RESOURCES(ID, NAME, RESTYPE, CONTENT) VALUES('13', 'Printer.Inventory', 0, $FILE{/com/openbravo/pos/templates/Printer.Inventory.xml});
INSERT INTO RESOURCES(ID, NAME, RESTYPE, CONTENT) VALUES('14', 'Menu.Root', 0, $FILE{/com/openbravo/pos/templates/Menu.Root.txt});
INSERT INTO RESOURCES(ID, NAME, RESTYPE, CONTENT) VALUES('15', 'Printer.CustomerPaid', 0, $FILE{/com/openbravo/pos/templates/Printer.CustomerPaid.xml});
INSERT INTO RESOURCES(ID, NAME, RESTYPE, CONTENT) VALUES('16', 'Printer.CustomerPaid2', 0, $FILE{/com/openbravo/pos/templates/Printer.CustomerPaid2.xml});
INSERT INTO RESOURCES(ID, NAME, RESTYPE, CONTENT) VALUES('17', 'payment.cash', 0, $FILE{/com/openbravo/pos/templates/payment.cash.txt});
INSERT INTO RESOURCES(ID, NAME, RESTYPE, CONTENT) VALUES('18', 'banknote.50euro', 1, $FILE{/com/openbravo/pos/templates/banknote.50euro.png});
INSERT INTO RESOURCES(ID, NAME, RESTYPE, CONTENT) VALUES('19', 'banknote.20euro', 1, $FILE{/com/openbravo/pos/templates/banknote.20euro.png});
INSERT INTO RESOURCES(ID, NAME, RESTYPE, CONTENT) VALUES('20', 'banknote.10euro', 1, $FILE{/com/openbravo/pos/templates/banknote.10euro.png});
INSERT INTO RESOURCES(ID, NAME, RESTYPE, CONTENT) VALUES('21', 'banknote.5euro', 1, $FILE{/com/openbravo/pos/templates/banknote.5euro.png});
INSERT INTO RESOURCES(ID, NAME, RESTYPE, CONTENT) VALUES('22', 'coin.2euro', 1, $FILE{/com/openbravo/pos/templates/coin.2euro.png});
INSERT INTO RESOURCES(ID, NAME, RESTYPE, CONTENT) VALUES('23', 'coin.1euro', 1, $FILE{/com/openbravo/pos/templates/coin.1euro.png});
INSERT INTO RESOURCES(ID, NAME, RESTYPE, CONTENT) VALUES('24', 'coin.50cent', 1, $FILE{/com/openbravo/pos/templates/coin.50cent.png});
INSERT INTO RESOURCES(ID, NAME, RESTYPE, CONTENT) VALUES('25', 'coin.20cent', 1, $FILE{/com/openbravo/pos/templates/coin.20cent.png});
INSERT INTO RESOURCES(ID, NAME, RESTYPE, CONTENT) VALUES('26', 'coin.10cent', 1, $FILE{/com/openbravo/pos/templates/coin.10cent.png});
INSERT INTO RESOURCES(ID, NAME, RESTYPE, CONTENT) VALUES('27', 'coin.5cent', 1, $FILE{/com/openbravo/pos/templates/coin.5cent.png});
INSERT INTO RESOURCES(ID, NAME, RESTYPE, CONTENT) VALUES('28', 'coin.2cent', 1, $FILE{/com/openbravo/pos/templates/coin.2cent.png});
INSERT INTO RESOURCES(ID, NAME, RESTYPE, CONTENT) VALUES('29', 'coin.1cent', 1, $FILE{/com/openbravo/pos/templates/coin.1cent.png});
INSERT INTO RESOURCES(ID, NAME, RESTYPE, CONTENT) VALUES('30', 'Printer.PartialCash', 0, $FILE{/com/openbravo/pos/templates/Printer.PartialCash.xml});

INSERT INTO CATEGORIES(ID, NAME) VALUES ('000', 'Categorie standard');
INSERT INTO CATEGORIES(ID, NAME) VALUES ('0', 'Formules');
INSERT INTO CATEGORIES(ID, NAME) VALUES ('-1', 'Pré-paiement');

INSERT INTO TAXCATEGORIES(ID, NAME) VALUES ('000', 'Sans TVA');
INSERT INTO TAXCATEGORIES(ID, NAME) VALUES ('001', 'TVA 2,1%');
INSERT INTO TAXCATEGORIES(ID, NAME) VALUES ('002', 'TVA 5,5%');
INSERT INTO TAXCATEGORIES(ID, NAME) VALUES ('003', 'TVA 7%');
INSERT INTO TAXCATEGORIES(ID, NAME) VALUES ('004', 'TVA 19,6%');

INSERT INTO TAXES(ID, NAME, CATEGORY, CUSTCATEGORY, PARENTID, RATE, RATECASCADE, RATEORDER) VALUES ('000', 'Sans TVA', '000', NULL, NULL, 0, 0, NULL);
INSERT INTO TAXES(ID, NAME, CATEGORY, CUSTCATEGORY, PARENTID, RATE, RATECASCADE, RATEORDER) VALUES ('001', 'TVA 2,5%', '001', NULL, NULL, 0.021, 0, NULL);
INSERT INTO TAXES(ID, NAME, CATEGORY, CUSTCATEGORY, PARENTID, RATE, RATECASCADE, RATEORDER) VALUES ('002', 'TVA 5,5%', '002', NULL, NULL, 0.055, 0, NULL);
INSERT INTO TAXES(ID, NAME, CATEGORY, CUSTCATEGORY, PARENTID, RATE, RATECASCADE, RATEORDER) VALUES ('003', 'TVA 7%', '003', NULL, NULL, 0.07, 0, NULL);
INSERT INTO TAXES(ID, NAME, CATEGORY, CUSTCATEGORY, PARENTID, RATE, RATECASCADE, RATEORDER) VALUES ('004', 'TVA 19,6%', '004', NULL, NULL, 0.196, 0, NULL);

INSERT INTO CURRENCIES (NAME, SYMBOL, DECIMALSEP, THOUSANDSSEP, RATE, FORMAT, MAIN) VALUES
('Euro', '€', ',', ' ', 1, '#,##0.00 $', 1);

INSERT INTO LOCATIONS(ID, NAME,ADDRESS) VALUES('0', 'Principal', NULL);

INSERT INTO FLOORS(ID, NAME, IMAGE) VALUES ('0', 'Salle', $FILE{/com/openbravo/pos/templates/restaurantsample.png});

INSERT INTO PLACES(ID, NAME, X, Y, FLOOR) VALUES ('1', 'Table 1', 133, 151, '0');
INSERT INTO PLACES(ID, NAME, X, Y, FLOOR) VALUES ('2', 'Table 2', 532, 151, '0');
INSERT INTO PLACES(ID, NAME, X, Y, FLOOR) VALUES ('3', 'Table 3', 133, 264, '0');
INSERT INTO PLACES(ID, NAME, X, Y, FLOOR) VALUES ('4', 'Table 4', 266, 264, '0');
INSERT INTO PLACES(ID, NAME, X, Y, FLOOR) VALUES ('5', 'Table 5', 399, 264, '0');
INSERT INTO PLACES(ID, NAME, X, Y, FLOOR) VALUES ('6', 'Table 6', 532, 264, '0');
INSERT INTO PLACES(ID, NAME, X, Y, FLOOR) VALUES ('7', 'Table 7', 133, 377, '0');
INSERT INTO PLACES(ID, NAME, X, Y, FLOOR) VALUES ('8', 'Table 8', 266, 377, '0');
INSERT INTO PLACES(ID, NAME, X, Y, FLOOR) VALUES ('9', 'Table 9', 399, 377, '0');
INSERT INTO PLACES(ID, NAME, X, Y, FLOOR) VALUES ('10', 'Table 10', 532, 377, '0');

