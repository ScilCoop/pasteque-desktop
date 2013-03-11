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

require_once(dirname(dirname(__FILE__)) . "/services/TicketsService.php");
require_once(dirname(dirname(__FILE__)) . "/models/Ticket.php");

$action = $_GET['action'];
$ret = null;

switch ($action) {
case 'save':
    // Receive ticket data as json
    $json = json_decode($_POST['tickets']);
    $jsonCash = json_decode($_POST['cash']);
    $cashId = $jsonCash->id;
    $ret = true;
    foreach ($json as $jsonTkt) {
        $label = $jsonTkt->ticket->label;
        $cashierId = $jsonTkt->cashier->id;
        $date = $jsonTkt->date;
        $lines = array();
        foreach ($jsonTkt->ticket->lines as $jsline) {
            // Get line info
            $line = count($lines) + 1;
            $productId = $jsline->product->id;
            $quantity = $jsline->quantity;
            $price = $jsline->product->price;
            $taxId = $jsline->product->taxId;
            $newLine = new TicketLineLight($line, $productId, $quantity, 
                                           $price, $taxId);
            $lines[] = $newLine;
        }
        $payments = array();
        foreach ($jsonTkt->payments as $jspay) {
            $type = $jspay->mode->code;
            $amount = $jspay->amount;
            $payment = new Payment($type, $amount);
            $payments[] = $payment;
        }
        $tktLght = new TicketLight($label, $cashierId, $date, $lines,
                                   $payments, $cashId);
        $ticket = TicketsService::buildLight($tktLght);
        $ret = TicketsService::save($ticket) && $ret;
    }
    break;
}

echo(json_encode($ret));

?>
