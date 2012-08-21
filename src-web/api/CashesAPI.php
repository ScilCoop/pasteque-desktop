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

require_once(dirname(dirname(__FILE__)) . "/services/CashesService.php");

$action = $_GET['action'];
$ret = null;

switch ($action) {
case 'get':
    if (!isset($_GET['host'])) {
        $ret = false;
        break;
    }
    $ret = CashesService::getHost($_GET['host']);
    if ($ret == null || $ret->isClosed()) {
        // Create a new one
        if (CashesService::add($_GET['host'])) {
            $ret = CashesService::getHost($_GET['host']);
        }
    }
    break;
case 'update':
    $json = json_decode($_POST['cash']);
    $open = null;
    if (property_exists($json, 'openDate')) {
        $open = $json->openDate;
    }
    $close = null;
    if (property_exists($json, 'closeDate')) {
        $close = $json->closeDate;
    }
    $host = $json->host;
    $cash = Cash::__build($json->id, $host, -1, $open, $close);
    $ret = array();
    $ret['result'] = CashesService::update($cash);
    $lastCash = CashesService::getHost($host);
    if ($lastCash != null && $lastCash->isClosed()) {
        if (CashesService::add($host)) {
            $newCash = CashesService::getHost($host);
        } else {
            $newCash = null;
        }
        $ret['cash'] = $newCash;
    } else {
        $ret['cash'] = $lastCash;
    }
    break;
}

echo(json_encode($ret));

?>
