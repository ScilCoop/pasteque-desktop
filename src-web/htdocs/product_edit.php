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



function createTree(&$list, $parent){
    $tree = array();
    foreach ($parent as $k=>$l){
		//var_dump($list[$l['ID']]);
        if(isset($list[$l->id])){
            $l->children = createTree($list, $list[$l->id]);
        }
        $tree[] = $l;
    } 
    return $tree;
}

function getTree($list,$profondeur=0){
    $tree = array();
    foreach ($list as $k=>$l){
		$tree[$l->id]=trim(str_pad('',$profondeur*4,'- ')." ".$l->label);
		//var_dump($list[$l['ID']]);
        if(!empty($l->children)){
            $tree = array_merge($tree,getTree($l->children,$profondeur+1));
        }
    } 
    return $tree;
}


require_once(dirname(dirname(__FILE__)) . "/services/CategoriesService.php");
require_once(dirname(dirname(__FILE__)) . "/services/TaxesService.php");
require_once(dirname(dirname(__FILE__)) . "/services/ProductsService.php");

$request = $app['request'];
$id=$request->get('id');
$product = ProductsService::get($id);
$tab_categories_sql=CategoriesService::getAll();
$new = array();

foreach($tab_categories_sql as $cat)
{
$parentid=$cat->parent_id;
if(is_null($parentid))
	$parentid=0;
$new[$parentid][] = $cat;
}
$new = createTree($new, $new[0]); // changed
$tab_categories=getTree($new);

$tab_taxe_categories_sql=TaxesService::getAll();

foreach($tab_taxe_categories_sql as $cat)
{
$tab_taxe_categories[$cat->id]=trim($cat->label);
}


$data=$product;


$taux_tva=floatval($product->tax_cat->taxes->rate);
$data->price_ttc=$product->price_sell*(1+($taux_tva));
$data->marge=round(((($product->price_sell*100)/$product->price_buy)-100),2);
$data->tax_cat=$data->tax_cat->id;
$data->category=$data->category->id;

$form = $app['form.factory']
->createbuilder('form',$data)
->add('reference', 'text', array('label' => 'réference :'))
->add('label', 'text', array('label' => 'nom:'))
->add('tax_cat', 'choice', array('label' => 'catégorie taxe:','choices' => $tab_taxe_categories))
->add('category', 'choice', array('label' => 'catégorie:','choices' => $tab_categories))
->add('price_ttc', 'money', array('label' => 'prix de vente + taxes:'))
->add('price_sell', 'money', array('label' => 'prix de vente:'))
->add('marge', 'percent', array('label' => 'marge :','type'=>'integer','precision'=>2))
->add('price_buy', 'money', array('label' => 'prix d\'achat'))
->add('barcode', 'text', array('label' => 'code barre:'))
//->add('attributeset_id', 'choice', array('label' => 'caractéristiques :','required'    => false))
//->add('stockcost', 'number', array('label' => 'coût annuel du stock:'))
//->add('stockvolume', 'number', array('label' => 'volume :'))
->add('visible', 'checkbox', array('label' => 'substitution:','required'    => false,))
//->add('vente', 'checkbox', array('label' => 'en vente :','required'    => false,))
->add('scaled', 'checkbox', array('label' => 'facturation au poids  :','required'    => false,))
//->add('attributes', 'textarea', array('label' => 'propriétés:','required'    => false))
->getForm();

    if ('POST' == $app['request']->getMethod()) {
        $form->bindRequest($app['request']);
        if ($form->isValid()) {
            $data = $form->getData();
            $data->tax_cat=TaxesService::get($data->tax_cat);
			$data->category=CategoriesService::get($data->category);
			ProductsService::update($data);

            return $app->redirect($app['url_generator']->generate(
                'product',
                array('id' => $id,'modify' => true)
            ));
        }
    }

return $app['twig']->render('product_edit.twig', array('product' => $product,'form'=>$form->createView(),'modify'=>false ));



?>