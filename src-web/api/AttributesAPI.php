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

require_once(dirname(dirname(__FILE__)) . "/services/AttributesService.php");

$action = $_GET['action'];
$ret = null;

switch ($action) {
case 'getAll':
    $ret = AttributesService::getAll();
    break;
case 'createVal':
    if (!isset($_GET['label']) || !isset($_GET['attr_id'])) {
        $ret = false;
        break;
    }
    $val = new AttributeValue($_GET['label']);
    $ret = AttributesService::createValue($val, $_GET['attr_id']);
    break;
case 'deleteVal':
    if (!isset($_GET['id'])) {
        $ret = false;
        break;
    }
    $ret = AttributesService::deleteValue($_GET['id']);
    break;
case 'updateVal':
    if (!isset($_GET['id']) || !isset($_GET['label'])) {
        $ret = false;
        break;
    }
    $val = AttributeValue::__build($_GET['id'], $_GET['label']);
    $ret = AttributesService::updateValue($val);
    break;
case 'createAttr':
    if (!isset($_GET['label'])) {
        $ret = false;
    }
    $attr = new Attribute($_GET['label']);
    $ret = AttributesService::createAttribute($attr);
    break;
case 'deleteAttr':
    if (!isset($_GET['id'])) {
        $ret = false;
        break;
    }
    $ret = AttributesService::deleteAttribute($_GET['id']);
    break;
case 'updateAttr':
    if (!isset($_GET['id']) || !isset($_GET['label'])) {
        $ret = false;
        break;
    }
    $attr = Attribute::__build($_GET['id'], $_GET['label']);
    $ret = AttributesService::updateAttribute($attr);
    break;
}

echo(json_encode($ret));

?>
