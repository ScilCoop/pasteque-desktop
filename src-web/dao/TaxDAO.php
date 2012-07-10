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

class TaxDAO {

    private $bidorm;

    function __construct($bidorm) {
        $this->bidorm = $bidorm;
    }

    function getTaxesFromCat($taxcat_id) {
        $db_taxes = $this->bidorm->getCriteria("TAXES", array("CATEGORY"),
                                               array($taxcat_id));
        $taxes = array();
        foreach ($db_taxes as $db_tax) {
        
            $taxes[] = Tax::__build($db_tax['ID'], $db_tax['CATEGORY'],
                                    $db_tax['NAME'], $db_tax['VALIDFROM'],
                                    $db_tax['RATE']);
        }
        return $taxes;
    }
}

?>
