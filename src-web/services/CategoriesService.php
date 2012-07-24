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

require_once(dirname(dirname(__FILE__)) . "/models/Category.php");
require_once(dirname(dirname(__FILE__)) . "/PDOBuilder.php");

class CategoriesService {

    static function getAll() {
        $cats = array();
        $pdo = PDOBuilder::getPDO();
        $sql = "SELECT * FROM CATEGORIES";
        foreach ($pdo->query($sql) as $db_cat) {
            $cat = Category::__build($db_cat['ID'], $db_cat['PARENTID'],
                                     $db_cat['NAME']);
            $cats[] = $cat;
        }
        return $cats;
    }

    static function updateCat($cat) {
        if ($cat->id == null) {
            return false;
        }
        $pdo = PDOBuilder::getPDO();
        $stmt = $pdo->prepare('UPDATE CATEGORIES SET NAME = :name, '
                              . 'PARENTID = :pid WHERE ID = :id');
        return $stmt->execute(array(':name' => $cat->label,
                                    ':pid' => $cat->parent_id,
                                    ':id' => $cat->id));
    }

    static function createCat($cat) {
        $pdo = PDOBuilder::getPDO();
        $id = md5(time() . rand());
        $stmt = $pdo->prepare('INSERT INTO CATEGORIES (ID, NAME, PARENTID) VALUES '
                              . '(:id, :name, :pid)');
        return $stmt->execute(array(':id' => $id,
                                    ':name' => $cat->label,
                                    ':pid' => $cat->parent_id));
    }

    static function deleteCat($id) {
        $pdo = PDOBuilder::getPDO();
        $stmt = $pdo->prepare('DELETE FROM CATEGORIES WHERE ID = :id');
        return $stmt->execute(array(':id' => $id));
    }

}

?>
