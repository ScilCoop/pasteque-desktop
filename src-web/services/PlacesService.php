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

require_once(dirname(dirname(__FILE__)) . "/models/Floor.php");
require_once(dirname(dirname(__FILE__)) . "/models/Place.php");
require_once(dirname(dirname(__FILE__)) . "/PDOBuilder.php");

class PlacesService {

    private static function buildDBPlace($db_place) {
        $place = Place::__build($db_place['ID'], $db_place['NAME'],
                                $db_place['X'], $db_place['Y']);
        return $place;
    }

    private static function buildDBFloor($db_floor, $pdo) {
        $floor = Floor::__build($db_floor['ID'], $db_floor['NAME']);
        $sqlplaces = 'SELECT * FROM PLACES WHERE FLOOR = "'
                     . $db_floor['ID'] . '"';
        foreach ($pdo->query($sqlplaces) as $db_place) {
            $place = PlacesService::buildDBPlace($db_place);
            $floor->addPlace($place);
        }
        return $floor;
    }

    static function getAll() {
        $floors = array();
        $pdo = PDOBuilder::getPDO();
        $sql = "SELECT * FROM FLOORS";
        foreach ($pdo->query($sql) as $db_floor) {
            $floor = PlacesService::buildDBFloor($db_floor, $pdo);
            $floors[] = $floor;
        }
        return $floors;
    }

    static function get($id) {
        $pdo = PDOBuilder::getPDO();
        $stmt = $pdo->prepare("SELECT * FROM FLOORS WHERE ID = :id");
        if ($stmt->execute(array(':id' => $id))) {
            if ($row = $stmt->fetch()) {
                return PlacesService::buildDBFloor($row, $pdo);
            }
        }
        return null;
    }

}

?>
