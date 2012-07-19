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

require_once(dirname(dirname(__FILE__)) . "/models/Tax.php");
require_once(dirname(dirname(__FILE__)) . "/models/TaxCat.php");
require_once(dirname(dirname(__FILE__)) . "/dao/DAOFactory.php");

class TaxesService {

    static function getAll() {
        $taxcatDAO = DAOFactory::getTaxCatDAO();
        $taxDAO = DAOFactory::getTaxDAO();
        $taxcats = $taxcatDAO->getTaxCats();
        foreach ($taxcats as $taxcat) {
            $tax = $taxDAO->getTaxesFromCat($taxcat->id);
            $taxcat->addTax($tax);
        }
        return $taxcats;
    }

    static function updateCat($cat) {
        if ($cat->getId() == null) {
            return false;
        }
        $taxcatDAO = DAOFactory::getTaxCatDAO();
        return $taxcatDAO->updateCat($cat);
    }

    static function createCat($cat) {
        $taxcatDAO = DAOFactory::getTaxCatDAO();
        return $taxcatDAO->createCat($cat);
    }
}

?>
