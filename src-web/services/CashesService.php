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

    private static function getLastSequence($host, $pdo) {
        $stmt = $pdo->prepare("SELECT max(HOSTSEQUENCE) FROM CLOSEDCASH WHERE "
                              . "HOST = :host");
        $stmt->execute(array(':host' => $host));
        if ($data = $stmt->fetch()) {
            return $data[0];
        } else {
            return 0;
        }
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

    static function update($cash) {
        $pdo = PDOBuilder::getPDO();
        $pdo->setAttribute(PDO::ATTR_EMULATE_PREPARES, false);
        $startParam = ($cash->isOpened()) ? ':start' : 'NULL';
        $endParam = ($cash->isClosed()) ? ':end' : 'NULL';
        $stmt = $pdo->prepare("UPDATE CLOSEDCASH SET DATESTART = $startParam, "
                              . "DATEEND = $endParam WHERE MONEY = :id");
        $stmt->bindParam(':id', $cash->id);
        if ($cash->isOpened()) {
            $open = stdstrftime($cash->openDate);
            $stmt->bindParam(':start', $open, PDO::PARAM_INT);
        }
        if ($cash->isClosed()) {
            $close = stdstrftime($cash->closeDate);
            $stmt->bindParam(':end', $close, PDO::PARAM_INT);
        }
        return $stmt->execute();
    }

    static function add($host) {
        $pdo = PDOBuilder::getPDO();
        $id = md5(time() . rand());
        $stmt = $pdo->prepare("INSERT INTO CLOSEDCASH (MONEY, HOST, "
                              . "HOSTSEQUENCE) VALUES (:id, :host, :sequence)");
        $sequence = CashesService::getLastSequence($host, $pdo) + 1;
        return $stmt->execute(array(':id' => $id, ':host' => $host,
                                    ':sequence' => $sequence));
    }
}

?>
