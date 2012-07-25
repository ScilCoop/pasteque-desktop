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

require_once(dirname(dirname(__FILE__)) . "/services/ProductsService.php");

$action = $_GET['action'];
$ret = null;

switch ($action) {
case 'get':
    if (!isset($_GET['id'])) {
       $ret = false;
       break;
    }
    $ret = ProductsService::get($_GET['id']);
    break;
case 'getAll':
    $ret = ProductsService::getAll();
    break;
case 'getAllFull':
    $ret = ProductsService::getAll(true);
    break;
case 'create':
    if (!isset($_GET['ref']) || !isset($_GET['label'])
        || !isset($_GET['sell']) || !isset($_GET['cat']) || !isset($_GET['tax'])
        || !isset($_GET['visible']) || !isset($_GET['scaled'])) {
        $ret = false;
        break;
    }
    $code = "";
    if (isset($_GET['code'])) {
        $code = $_GET['code'];
    }
    $buy = null;
    if (isset($_GET['buy'])) {
        $buy = $_GET['buy'];
    }
    $attr = null;
    if (isset($_GET['attr'])) {
        $attr = Attribute::__build($_GET['attr'], "dummy");
    }
    $cat = Category::__build($_GET['cat'], null, "dummy");
    $tax = TaxCat::__build($_GET['tax'], "dummy");
    $prd = new Product($_GET['ref'], $_GET['label'], $_GET['sell'], $cat, $tax,
                       $_GET['visible'], $_GET['scaled'], $buy, $attr, $code);
    $ret = ProductsService::create($prd);
    break;
case 'delete':
    if (!isset($_GET['id'])) {
        $ret = false;
        break;
    }
    $ret = ProductsService::delete($_GET['id']);
    break;
case 'update':
    if (!isset($_GET['id']) || !isset($_GET['ref']) || !isset($_GET['label'])
        || !isset($_GET['sell']) || !isset($_GET['cat']) || !isset($_GET['tax'])
        || !isset($_GET['visible']) || !isset($_GET['scaled'])) {
        $ret = false;
        break;
    }
    $code = "";
    if (isset($_GET['code'])) {
        $code = $_GET['code'];
    }
    $buy = null;
    if (isset($_GET['buy'])) {
        $buy = $_GET['buy'];
    }
    $attr = null;
    if (isset($_GET['attr'])) {
        $attr = Attribute::__build($_GET['attr'], "dummy");
    }
    $cat = Category::__build($_GET['cat'], null, "dummy");
    $tax = TaxCat::__build($_GET['tax'], "dummy");
    $prd = Product::__build($_GET['id'], $_GET['ref'], $_GET['label'],
                            $_GET['sell'], $cat, $tax,
                            $_GET['visible'], $_GET['scaled'],
                            $buy, $attr, $code);
    $ret = ProductsService::update($prd);
    break;
}

echo(json_encode($ret));

?>
