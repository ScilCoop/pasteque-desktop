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

require_once(dirname(dirname(__FILE__)) . "/models/Cash.php");
require_once(dirname(dirname(__FILE__)) . "/PDOBuilder.php");
require_once(dirname(dirname(__FILE__)) . "/utils/date_utils.php");

class CashesService {

    private static function buildDBCash($db_cash) {
        $cash = Cash::__build($db_cash['MONEY'], $db_cash['HOST'],
                              $db_cash['HOSTSEQUENCE'],
                              stdtimefstr($db_cash['DATESTART']),
                              stdtimefstr($db_cash['DATEEND']));
        return $cash;
    }


    static function getAll() {
        $cashes = array();
        $pdo = PDOBuilder::getPDO();
        $sql = "SELECT * FROM CLOSEDCASH";
        foreach ($pdo->query($sql) as $db_cash) {
            $cash = CashesService::buildDBCash($db_cash);
            $cashes[] = $cash;
        }
        return $cashes;
    }

    static function get($id) {
        $pdo = PDOBuilder::getPDO();
        $stmt = $pdo->prepare("SELECT * FROM CLOSEDCASH WHERE MONEY = :id");
        if ($stmt->execute(array(':id' => $id))) {
            if ($row = $stmt->fetch()) {
                return CashesService::buildDBCash($row);
            }
        }
        return null;
    }

    static function getHost($host) {
        $pdo = PDOBuilder::getPDO();
        $stmt = $pdo->prepare("SELECT * FROM CLOSEDCASH WHERE HOST = :host "
                              . "ORDER BY HOSTSEQUENCE DESC");
        if ($stmt->execute(array(':host' => $host))) {
            if ($row = $stmt->fetch()) {
                return CashesService::buildDBCash($row);
            }
        }
        return null;
    }
}

?>
