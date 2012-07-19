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

class AttributeDAO {

    private $bidorm;

    function __construct($bidorm) {
        $this->bidorm = $bidorm;
    }

    function getAttributes() {
        $db_attrs = $this->bidorm->getAll("ATTRIBUTE");
        $attrs = array();
        foreach ($db_attrs as $db_attr) {
            $db_vals = $this->bidorm->getCriteria("ATTRIBUTEVALUE",
                                                  array("ATTRIBUTE_ID"),
                                                  array($db_attr['ID']));
            $attr = Attribute::__build($db_attr['ID'], $db_attr['NAME']);
            foreach ($db_vals as $db_val) {
                $val = AttributeValue::__build($db_val['ID'], $db_val['VALUE']);
                $attr->add_value($val);
            }
            $attrs[] = $attr;
        }
        return $attrs;
    }
}

?>
