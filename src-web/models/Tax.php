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

class Tax {

    public $id;
    public $tax_cat_id;
    public $label;
    public $start_date;
    public $rate;

    static function __build($id, $tax_cat_id, $label, $start_date, $rate) {
        $tax = new Tax($tax_cat_id, $label, $start_date, $rate);
        $tax->id = $id;
        return $tax;
    }

    function __construct($tax_cat_id, $label, $start_date, $rate) {
        $this->tax_cat_id = $tax_cat_id;
        $this->label = $label;
        if (!preg_match('%^\\d*$%', $start_date) && !is_int($start_date)) {
            $start_date = strtotime($start_date);
        }
        $this->start_date = $start_date;
        $this->rate = $rate;
    }
    
    /** Check if this tax is valid at a given date.
     * @param $date (optional) The date as timestamp (default now)
     */
    function isValid($date = null) {
        if ($date === null) {
            $date = time();
        }
        return $this->start_date < $date;
    }
}
