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
require_once(dirname(dirname(__FILE__)) . "/PDOBuilder.php");

class AttributesService {

    static function getAll() {
        $pdo = PDOBuilder::getPDO();
        $attrs = array();
        $sql = "SELECT * FROM ATTRIBUTE";
        foreach ($pdo->query($sql) as $db_attr) {
            $attr = Attribute::__build($db_attr['ID'], $db_attr['NAME']);
            $valstmt = $pdo->prepare("SELECT * FROM ATTRIBUTEVALUE WHERE "
                                     . "ATTRIBUTE_ID = :id");
            $valstmt->execute(array(':id' => $db_attr['ID']));
            while ($db_val = $valstmt->fetch()) {
                $val = AttributeValue::__build($db_val['ID'], $db_val['VALUE']);
                $attr->addValue($val);
            }
            $attrs[] = $attr;
        }
        return $attrs;
    }

    static function createAttribute($attr) {
        $pdo = PDOBuilder::getPDO();
        $id = md5(time() . rand());
        $stmt = $pdo->prepare("INSERT INTO ATTRIBUTE (ID, NAME) VALUES "
                              . "(:id, :name)");
        if ($stmt->execute(array(':id' => $id, ':name' => $attr->label))) {
            $attr->id = $id;
            return true;
        } else {
            return false;
        }
    }

    static function deleteAttribute($id) {
        $pdo = PDOBuilder::getPDO();
        $stmt = $pdo->prepare("DELETE FROM ATTRIBUTE WHERE ID = :id");
        return $stmt->execute(array(':id' => $id));
    }

    static function updateAttribute($attr) {
        if ($attr->id == null) {
            return false;
        }
        $pdo = PDOBuilder::getPDO();
        $stmt = $pdo->prepare("UPDATE ATTRIBUTE SET NAME = :name WHERE ID = :id");
        return $stmt->execute(array(':id' => $attr->id, ':name' => $attr->label));
    }

    static function createValue($value, $attr_id) {
        $id = md5(time() . rand());
        $pdo = PDOBuilder::getPDO();
        $stmt = $pdo->prepare("INSERT INTO ATTRIBUTEVALUE "
                              . "(ID, VALUE, ATTRIBUTE_ID) VALUES "
                              . "(:id, :value, :attr_id)");
        return $stmt->execute(array(':id' => $id, ':value' => $value->label,
                                    ':attr_id' => $attr_id));
    }

    static function deleteValue($id) {
        $pdo = PDOBuilder::getPDO();
        $stmt = $pdo->prepare("DELETE FROM ATTRIBUTEVALUE WHERE ID = :id");
        return $stmt->execute(array(':id' => $id));
    }

    static function updateValue($val) {
        if ($val->id == null) {
            return false;
        }
        $pdo = PDOBuilder::getPDO();
        $stmt = $pdo->prepare("UPDATE ATTRIBUTEVALUE SET VALUE = :value "
                              . "WHERE ID = :id");
        return $stmt->execute(array(':id' => $val->id, ':value' => $val->label));
    }
}

?>
