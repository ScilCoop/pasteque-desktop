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

require_once(dirname(dirname(__FILE__)) . "/services/CategoriesService.php");
require_once(dirname(dirname(__FILE__)) . "/services/TaxesService.php");
require_once(dirname(dirname(__FILE__)) . "/services/AttributesService.php");
require_once(dirname(dirname(__FILE__)) . "/models/Product.php");
require_once(dirname(dirname(__FILE__)) . "/PDOBuilder.php");

class ProductsService {

    private static function buildDBLightPrd($db_prd, $pdo) {
        $stmt = $pdo->prepare("SELECT * FROM PRODUCTS_CAT WHERE PRODUCT = :id");
        $stmt->execute(array(':id' => $db_prd['ID']));
        $visible = ($stmt->fetch() !== false);
        return ProductLight::__build($db_prd['ID'], $db_prd['REFERENCE'],
                                     $db_prd['NAME'], $db_prd['PRICESELL'],
                                     $visible,
                                     ord($db_prd['ISSCALE']) == 1,
                                     $db_prd['CODE'], $db_prd['PRICEBUY']);
    }

    private static function buildDBPrd($db_prd, $pdo) {
        $cat = CategoriesService::get($db_prd['CATEGORY']);
        $tax_cat = TaxesService::get($db_prd['TAXCAT']);
        $attr = AttributesService::get($db_prd['ATTRIBUTES']);
        $stmt = $pdo->prepare("SELECT * FROM PRODUCTS_CAT WHERE PRODUCT = :id");
        $stmt->execute(array(':id' => $db_prd['ID']));
        $visible = ($stmt->fetch() !== false);
        return Product::__build($db_prd['ID'], $db_prd['REFERENCE'],
                                $db_prd['NAME'], $db_prd['PRICESELL'],
                                $cat, $tax_cat, $visible,
                                ord($db_prd['ISSCALE']) == 1,
                                $db_prd['PRICEBUY'], $attr, $db_prd['CODE']);
    }

    static function getAll($full = false) {
        $prds = array();
        $pdo = PDOBuilder::getPDO();
        $stmt = $pdo->prepare("SELECT * FROM PRODUCTS");
        $stmt->execute();
        while ($db_prd = $stmt->fetch()) {
            if ($full) {
                $prd = ProductsService::buildDBPrd($db_prd, $pdo);
            } else {
                $prd = ProductsService::buildDBLightPrd($db_prd, $pdo);
            }
            $prds[] = $prd;
        }
        return $prds;
    }

    
    static function search($where = '', $groupby = '',$orderby='',$limit='',$having='',$full = false) {
        $prds = array();
        $pdo = PDOBuilder::getPDO();
		$supplement_req="";
		if(!empty($where)) $supplement_req.=" WHERE ".$where;
		if(!empty($groupby)) $supplement_req.=" GROUP BY ".$groupby;
		if(!empty($orderby)) $supplement_req.=" ORDER BY ".$orderby;
		if(!empty($limit)) $supplement_req.=" LIMIT ".$limit;
		if(!empty($having)) $supplement_req.=" HAVING ".$having;
		$stmt = $pdo->prepare("SELECT * FROM PRODUCTS".$supplement_req);
        $stmt->execute();
        while ($db_prd = $stmt->fetch()) {
            if ($full) {
                $prd = ProductsService::buildDBPrd($db_prd, $pdo);
            } else {
                $prd = ProductsService::buildDBLightPrd($db_prd, $pdo);
            }
            $prds[] = $prd;
        }
        return $prds;
    }

    
    static function getCount($where = '') {
        $prds = array();
        $pdo = PDOBuilder::getPDO();
		$supplement_req="";
		if(!empty($where)) $supplement_req.=" WHERE ".$where;
		$stmt = $pdo->prepare("SELECT COUNT(*) FROM PRODUCTS".$supplement_req);
        $stmt->execute();
        if ($db_prd = $stmt->fetch()) {
			return $db_prd[0];
        }
		return 0;
    }

    static function get($id) {
        $pdo = PDOBuilder::getPDO();
        $stmt = $pdo->prepare("SELECT * FROM PRODUCTS WHERE ID = :id");
        if ($stmt->execute(array(':id' => $id))) {
            if ($row = $stmt->fetch()) {
                $prd = ProductsService::buildDBPrd($row, $pdo);
                return $prd;
            }
        }
        return null;
    }

    static function getImage($id) {
        $pdo = PDOBuilder::getPDO();
        $stmt = $pdo->prepare("SELECT image FROM PRODUCTS WHERE ID = :id");
        if ($stmt->execute(array(':id' => $id))) {
            if ( $row = $stmt->fetch()) {
				//var_dump($row);
                //$data = mysql_fetch_assoc($res);
                return $row['image'];
            }
        }
       return null;
    }

	static function setImage($id,$fichier) {
        $pdo = PDOBuilder::getPDO();
        $image = file_get_contents($fichier);
        $stmt = $pdo->prepare("UPDATE PRODUCTS SET image = :image  WHERE ID = :id");
        $stmt->bindParam(':id', $id, PDO::PARAM_INT);
        $stmt->bindParam(':image', $image, PDO::PARAM_LOB);
 //       var_dump($stmt->query(array(':id' => $id,':image'=>$image)));
       $stmt->execute(array(':id' => $id,':image'=>$image));
    }


    static function update($prd) {
        $pdo = PDOBuilder::getPDO();
        $attr_id = null;
        if ($prd->attributes_set != null) {
            $attr_id = $prd->attributes_set->id;
        }
        $code = "";
        if ($prd->barcode != null) {
            $code = $prd->barcode;
        }
        $stmt = $pdo->prepare("UPDATE PRODUCTS SET REFERENCE = :ref, "
                              . "CODE = :code, NAME = :name, PRICEBUY = :buy, "
                              . "PRICESELL = :sell, CATEGORY = :cat, "
                              . "TAXCAT = :tax, ATTRIBUTESET_ID = :attr, "
                              . "ISCOM = :com, ISSCALE = :scale "
                              . "WHERE ID = :id");
        return $stmt->execute(array(':ref' => $prd->reference,
                                    ':code' => $code,
                                    ':name' => $prd->label,
                                    ':buy' => $prd->price_buy,
                                    ':sell' => $prd->price_sell,
                                    ':cat' => $prd->category->id,
                                    ':tax' => $prd->tax_cat->id,
                                    ':attr' => $attr_id,
                                    ':com' => $prd->visible,
                                    ':scale' => $prd->scaled,
                                    ':id' => $prd->id));
    }
    
    static function create($prd) {
        $pdo = PDOBuilder::getPDO();
        $id = md5(time() . rand());
        $attr_id = null;
        if ($prd->attributes_set != null) {
            $attr_id = $prd->attributes_set->id;
        }
        $code = "";
        if ($prd->barcode != null) {
            $code = $prd->barcode;
        }
        $stmt = $pdo->prepare("INSERT INTO PRODUCTS (ID, REFERENCE, CODE, NAME, "
                              . "PRICEBUY, PRICESELL, CATEGORY, TAXCAT, "
                              . "ATTRIBUTESET_ID, ISCOM, ISSCALE) VALUES "
                              . "(:id, :ref, :code, :name, :buy, :sell, :cat, "
                              . ":tax, :attr, :com, :scale)");
        return $stmt->execute(array(':ref' => $prd->reference,
                                    ':code' => $code,
                                    ':name' => $prd->label,
                                    ':buy' => $prd->price_buy,
                                    ':sell' => $prd->price_sell,
                                    ':cat' => $prd->category->id,
                                    ':tax' => $prd->tax_cat->id,
                                    ':attr' => $attr_id,
                                    ':com' => $prd->visible,
                                    ':scale' => $prd->scaled,
                                    ':id' => $id));
    }
    
    static function delete($id) {
        $pdo = PDOBuilder::getPDO();
        $stmt = $pdo->prepare("DELETE FROM PRODUCTS WHERE ID = :id");
        return $stmt->execute(array(':id' => $id));
    }
}

?>
