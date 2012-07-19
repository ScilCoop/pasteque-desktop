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

require_once(dirname(dirname(__FILE__)) . "/services/TaxesService.php");

$action = $_GET['action'];
$ret = null;

switch ($action) {
case 'getAll':
    $ret = TaxesService::getAll();
    break;
case 'updateCat':
    if (!isset($_GET['id']) || !isset($_GET['label'])) {
        $ret = false;
        break;
    }
    $cat = TaxCat::__build($_GET['id'], $_GET['label']);
    $ret = TaxesService::updateCat($cat);
    break;
case 'createCat':
    if (!isset($_GET['label'])) {
        $ret = false;
        break;
    }
    $cat = new TaxCat($_GET['label']);
    $ret = TaxesService::createCat($cat);
    break;
case 'deleteCat':
    if (!isset($_GET['id'])) {
        $ret = false;
        break;
    }
    $id = $_GET['id'];
    $ret = TaxesService::deleteCat($id);
}

echo(json_encode($ret));

?>
