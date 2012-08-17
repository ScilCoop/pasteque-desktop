<?php
//    POS-Tech API
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

require_once(dirname(dirname(__FILE__)) . "/models/Ticket.php");
require_once(dirname(dirname(__FILE__)) . "/models/TicketLine.php");
require_once(dirname(dirname(__FILE__)) . "/models/User.php");
require_once(dirname(dirname(__FILE__)) . "/models/Payment.php");
require_once(dirname(dirname(__FILE__)) . "/models/Tax.php");
require_once(dirname(dirname(__FILE__)) . "/models/TaxAmount.php");
require_once(dirname(dirname(__FILE__)) . "/services/ProductsService.php");
require_once(dirname(dirname(__FILE__)) . "/services/UsersService.php");
require_once(dirname(dirname(__FILE__)) . "/services/CashesService.php");
require_once(dirname(dirname(__FILE__)) . "/PDOBuilder.php");

class TicketsService {

    /** Build a full ticket from a light ticket */
    static function buildLight($ticketLight) {
        $cashier = UsersService::get($ticketLight->cashierId);
        $cash = CashesService::get($ticketLight->cashId);
        $lines = array();
        foreach ($ticketLight->linesLight as $lineLight) {
            $product = ProductsService::get($lineLight->productId);
            $tax = TaxesService::getTax($lineLight->taxId);
            $line = new TicketLine($lineLight->line, $product,
                                   $lineLight->quantity, $lineLight->price,
                                   $tax);
            $lines[] = $line;
        }
        $ticket = new Ticket($ticketLight->label, $cashier, $ticketLight->date,
                             $lines, $ticketLight->payments, $cash);
        return $ticket;
    }

    static function save($ticket) {
        $pdo = PDOBuilder::getPDO();
        $pdo->beginTransaction();
        $id = md5(time() . rand());
        $stmtRcpt = $pdo->prepare("INSERT INTO RECEIPTS	(ID, MONEY, DATENEW) "
                                  . "VALUES (:id, :money, :date)");
        $strdate = strftime("%Y-%m-%d %H:%M", $ticket->date);
        $ok = $stmtRcpt->execute(array(':id' => $id,
                                       ':money' => $ticket->cash->id,
                                       ':date' => $strdate));
        if ($ok === false) {
            $pdo->rollback();
            return false;
        }
        // Get next ticket number
        $stmtNum = $pdo->prepare("SELECT ID FROM TICKETSNUM");
        $ok = $stmtNum->execute();
        if ($ok === false) {
            $pdo->rollback();
            return false;
        }
        $nextNum = $stmtNum->fetchColumn(0);
        //  Insert ticket
        $stmtTkt = $pdo->prepare("INSERT INTO TICKETS (ID, TICKETID, PERSON) "
                                 . "VALUES (:id, :tktId, :person)");
        $ok = $stmtTkt->execute(array(':id' => $id,
                                      ':tktId' => $nextNum,
                                      ':person' => $ticket->cashier->id));
        if ($ok === false) {
            $pdo->rollback();
            return false;
        }
        // Increment next ticket number
        $stmtNumInc = $pdo->prepare("UPDATE TICKETSNUM SET ID = :id");
        $ok = $stmtNumInc->execute(array(':id' => $nextNum + 1));
        if ($ok === false) {
            $pdo->rollback();
            return false;
        }
        // Insert ticket lines
        $stmtLines = $pdo->prepare("INSERT INTO TICKETLINES (TICKET, LINE, "
                                   . "PRODUCT, UNITS, "
                                   . "PRICE, TAXID, ATTRIBUTES) VALUES "
                                   . "(:id, :line, :product, :qty, :price, "
                                   . ":tax, :attrs)");
        foreach ($ticket->lines as $line) {
            $ok = $stmtLines->execute(array(':id' => $id,
                                            ':line' => $line->line,
                                            ':product' => $line->product->id,
                                            ':qty' => $line->quantity,
                                            ':price' => $line->price,
                                            ':tax' => $line->tax->id,
                                            ':attrs' => $line->attributes));
            if ($ok === false) {
                $pdo->rollback();
                return false;
            }
        }
        // Insert payments
        $stmtPay = $pdo->prepare("INSERT INTO PAYMENTS (ID, RECEIPT, PAYMENT, "
                                 . "TOTAL) VALUES (:id, :rcptId, "
                                 . ":type, :amount)");
        foreach ($ticket->payments as $payment) {
            $paymentId = md5(time() . rand());
            $ok = $stmtPay->execute(array(':id' => $paymentId,
                                          ':rcptId' => $id,
                                          ':type' => $payment->type,
                                          ':amount' => $payment->amount));
            if ($ok === false) {
                $pdo->rollback();
                return false;
            }
        }
        // Insert taxlines
        $stmtTax = $pdo->prepare("INSERT INTO TAXLINES (ID, RECEIPT, TAXID, "
                                 . "BASE, AMOUNT)  VALUES (:id, :rcptId, "
                                 . ":taxId, :base, :amount)");
        foreach ($ticket->getTaxAmounts() as $ta) {
            $taxId = md5(time() . rand());
            $ok = $stmtTax->execute(array(':id' => $taxId,
                                          ':rcptId' => $id,
                                          ':taxId' => $ta->tax->id,
                                          ':base' => $ta->base,
                                          ':amount' => $ta->getAmount()));
            if ($ok === false) {
                $pdo->rollback();
                return false;
            }
        }
        $pdo->commit();
        return true;
    }
}

?>
