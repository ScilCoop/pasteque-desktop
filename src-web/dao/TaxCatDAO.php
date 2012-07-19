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

class TaxCatDAO {

    private $bidorm;

    function __construct($bidorm) {
        $this->bidorm = $bidorm;
    }

    function getTaxCats() {
        $db_taxcats = $this->bidorm->getAll("TAXCATEGORIES");
        $taxcats = array();
        foreach ($db_taxcats as $db_taxcat) {
            $taxcats[] = TaxCat::__build($db_taxcat['ID'], $db_taxcat['NAME']);
        }
        return $taxcats;
    }

    function updateCat($cat) {
        $dbcat = array();
        $dbcat['ID'] = $cat->getId();
        $dbcat['NAME'] = $cat->label;
        return $this->bidorm->updateOne($dbcat, "TAXCATEGORIES", "ID");
    }

    function createCat($cat) {
        $dbcat = array();
        $dbcat['NAME'] = $cat->label;
        $id = md5(time() . rand());
        $dbcat['ID'] = $id;
        return $this->bidorm->addOne($dbcat, "TAXCATEGORIES");
    }

    function deleteCat($id) {
        $this->bidorm->delete("TAXES", "CATEGORY", $id);
        return $this->bidorm->delete("TAXCATEGORIES", "ID", $id);
    }
}

?>
