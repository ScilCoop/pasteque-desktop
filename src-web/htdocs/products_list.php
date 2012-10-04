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
require_once(dirname(dirname(__FILE__)) . "/utils/Pagination.php");
require_once(dirname(dirname(__FILE__)) . "/utils/searchbar.php");

$where=array();
$request = $app['request'];
//Critere de recherche
$searchbar = new searchbar();
if(!empty($searchbar->where))
	$where[]=$searchbar->where;


//Filter
$filter_cat=$request->get('filter_category');
if(!empty($filter_cat))
	$where[]='category ='.$filter_cat;


$where=implode(" AND ",$where);

//Pagination
$nb_products=ProductsService::getCount($where);
$pagination = new Pagination ( $nb_products, NB_ROW_PRODUCT );
 
$products = ProductsService::search($where,'','',$pagination->minid.','.NB_ROW_PRODUCT);

return $app['twig']->render('products_list.twig', array('products_list' => $products,'pagination'=>$pagination,'searchbar'=>$searchbar));



?>