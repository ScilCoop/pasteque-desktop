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

/** Ticket with id only for external references */
class TicketLight {
    public $cashId;
    public $label;
    public $cashierId;
    /** Payment date, as timestamp */
    public $date;
    public $linesLight;
    public $payments;

    function __construct($label, $cashierId, $date, $lines, $payments, $cashId) {
        $this->label = $label;
        $this->cashierId = $cashierId;
        $this->date = $date;
        $this->linesLight = $lines;
        $this->payments = $payments;
        $this->cashId = $cashId;
    }

    function getTaxAmounts() {
        $amounts = array();
        foreach ($ticket->lines as $line) {
            if (isset($amounts[$line->tax])) {
                $amounts[$line->tax] += $line->getSubtotal();
            } else {
                $amounts[$line->tax] = $line->getSubtotal();
            }
        }
        $ta = array();
        foreach ($amounts as $tax => $base) {
            $ta[] = new TaxAmount($tax, $base);
        }
        return $ta;
    }
}

class Ticket {

    public $id;
    public $cash;
    public $label;
    public $cashier;
    /** Payment date, as timestamp */
    public $date;
    public $lines;
    public $payments;

    static function __build($id, $label, $cashier, $date, $lines, $payments,
                            $cash) {
        $ticket = new Ticket();
        $ticket->id = $id;
        return $ticket;
    }

    function __construct($label, $cashier, $date, $lines, $payments, $cash) {
        $this->label = $label;
        $this->cashier = $cashier;
        $this->date = $date;
        $this->lines = $lines;
        $this->payments = $payments;
        $this->cash = $cash;
    }

    function getTaxAmounts() {
        $amounts = array();
        $taxesMap = array(); // taxes by id
        $amountsMap = array(); // amounts by tax id
        foreach ($this->lines as $line) {
            if (isset($taxesMap[$line->tax->id])) {
                $amountsMap[$line->tax->id] += $line->getSubtotal();
            } else {
                $amountsMap[$line->tax->id] = $line->getSubtotal();
                $taxesMap[$line->tax->id] = $line->tax;
            }
        }
        $ta = array();
        foreach ($taxesMap as $taxId => $tax) {
            $ta[] = new TaxAmount($taxesMap[$taxId], $amountsMap[$taxId]);
        }
        return $ta;
    }
}

?>
