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

require_once(dirname(dirname(__FILE__)) . "/models/User.php");
require_once(dirname(dirname(__FILE__)) . "/PDOBuilder.php");

class UsersService {

    private static function buildDBUser($db_user, $pdo) {
        $stmt = $pdo->prepare("SELECT PERMISSIONS FROM ROLES WHERE ID = :id");
        if ($stmt->execute(array(':id' => $db_user['ID']))) {
            if ($row = $stmt->fetch()) {
                $permissions = $row['PERMISSIONS'];
            }
    	}
        $user = User::__build($db_user['ID'], $db_user['NAME'],
                              $db_user['APPPASSWORD'], $permissions);
        return $user;
    }


    static function getAll() {
        $users = array();
        $pdo = PDOBuilder::getPDO();
        $sql = "SELECT * FROM PEOPLE";
        foreach ($pdo->query($sql) as $db_user) {
            $user = UsersService::buildDBUser($db_user, $pdo);
            $users[] = $user;
        }
        return $users;
    }

    static function get($id) {
        $pdo = PDOBuilder::getPDO();
        $stmt = $pdo->prepare("SELECT * FROM USERS WHERE ID = :id");
        if ($stmt->execute(array(':id' => $id))) {
            if ($row = $stmt->fetch()) {
                return UsersService::buildDBUser($row, $pdo);
            }
        }
        return null;
    }

}

?>
