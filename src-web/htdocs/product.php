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
use Symfony\Component\Validator\Constraints as Assert;
$request = $app['request'];
$id=$request->get('id');



$message="";

$form = $app['form.factory']
->createbuilder('form')
->add('image', 'file', array('label' => 'Charger une image','constraints' => array(new Assert\Image())))
->getForm();

if ('POST' == $app['request']->getMethod()) {
	$form->bindRequest($app['request']);
	
	if ($form->isValid()) {
	     $files = $request->files->get($form->getName());
                    /* Make sure that Upload Directory is properly configured and writable */
                    $path = __DIR__.'/upload/';
                    $filename = $files['image']->getClientOriginalName();
					$files['image']->move($path,$filename);
					require_once(dirname(dirname(__FILE__)) . "/utils/image.class.php");
					$image = new Zubrag_image();
					$output_filename = $path.basename($filename).'-resized.jpg';
					$image->GenerateThumbFile($path.$filename, $output_filename);
					ProductsService::setImage($id,$output_filename);
					unlink($path.$filename);
					$message='Image chargé';
						

            }

	
}

$product = ProductsService::get($id);
$product->price_total = $product->getTotalPrice();
$product->margin = $product->getMargin();


$args = array('product' => $product,
              'form'=>$form->createView(),
              'message'=>$message
             );


return $app['twig']->render('product.twig', $args);

?>
