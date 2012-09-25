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
$request = $app['request'];
$product = ProductsService::get($request->get('id'));

$args = array('id' => $product->id,
              'reference' => $product->reference,
              'barcode' => $product->barcode,
              'label' => $product->label,
              'price_buy' => $product->price_buy,
              'price_sell' => $product->price_sell,
              'price_total' => $product->getTotalPrice(),
              'margin' => $product->getMargin(),
              'scaled' => $product->scaled,
              'visible' => $product->visible,
              'tax_label' => $product->tax_cat->label
             );

return $app['twig']->render('product.twig', $args);

?>
