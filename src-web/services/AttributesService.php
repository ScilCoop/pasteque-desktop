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

require_once(dirname(dirname(__FILE__)) . "/models/Attribute.php");
require_once(dirname(dirname(__FILE__)) . "/dao/DAOFactory.php");

class AttributesService {

    static function getAll() {
        $attrsDAO = DAOFactory::getAttributeDAO();
        $attrs = $attrsDAO->getAttributes();
        return $attrs;
    }

    static function createAttribute($attr) {
        $attrsDAO = DAOFactory::getAttributeDAO();
        return $attrsDAO->createAttribute($attr);
    }

    static function deleteAttribute($id) {
        $attrsDAO = DAOFactory::getAttributeDAO();
        return $attrsDAO->deleteAttribute($id);
    }

    static function updateAttribute($attr) {
        if ($attr->id == null) {
            return false;
        }
        $attrsDAO = DAOFactory::getAttributeDAO();
        return $attrsDAO->updateAttribute($attr);
    }

    static function createValue($value, $attr_id) {
        $attrsDAO = DAOFactory::getAttributeDAO();
        return $attrsDAO->createValue($value, $attr_id);
    }

    static function deleteValue($id) {
        $attrsDAO = DAOFactory::getAttributeDAO();
        return $attrsDAO->deleteValue($id);
    }

    static function updateValue($val) {
        if ($val->id == null) {
            return false;
        }
        $attrsDAO = DAOFactory::getAttributeDAO();
        return $attrsDAO->updateValue($val);
    }
}

?>
