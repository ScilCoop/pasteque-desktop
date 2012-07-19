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

require_once(dirname(dirname(__FILE__)) . "/lib/bidorm.php");
require_once(dirname(dirname(__FILE__)) . "/lib/MysqlConnector.php");
require_once(dirname(dirname(__FILE__)) . "/config.php");

require_once(dirname(__FILE__) . "/TaxCatDAO.php");
require_once(dirname(__FILE__) . "/TaxDAO.php");
require_once(dirname(__FILE__) . "/AttributeDAO.php");

class DAOFactory {

    private static $bidorm = null;

    private static function bidorm() {
        if (DAOFactory::$bidorm === null) {
            switch (Config::$db_type) {
            case "mysql":
            default:
                $connector = new MysqlConnector(Config::$host, Config::$port,
                                                Config::$user,
                                                Config::$password,
                                                Config::$database);
                break;
            }
            DAOFactory::$bidorm = new Bidorm($connector);
        }
        return DAOFactory::$bidorm;
    }

    static function getTaxCatDAO() {
        return new TaxCatDAO(DAOFactory::bidorm());
    }

    static function getTaxDAO() {
        return new TaxDAO(DAOFactory::bidorm());
    }

    static function getAttributeDAO() {
        return new AttributeDAO(DAOFactory::bidorm());
    }
}

?>
