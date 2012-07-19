<?php
/*
	Copyright 2011 Cédric Houbart (email : cedric@scil.coop)
	
	This program is free software: you can redistribute it and/or modify
	it under the terms of the GNU General Public License as published by
	the Free Software Foundation, either version 3 of the License, or
	(at your option) any later version.

	This program is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
	GNU General Public License for more details.

	You should have received a copy of the GNU General Public License
	along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

class BidORM {
    
    private $connector;
    
    public function __construct($connector) {
        $this->connector = $connector;
    }

    private function escape($param) {
        return mysql_real_escape_string($param,
                                        $this->connector->getConnection());
    }

    public function getStructure($table) {
        $this->connector->open();
        $table = $this->escape($table);
        $request = sprintf("SHOW COLUMNS FROM %s", $table);
        $rs = mysql_query($request, $this->connector->getConnection());
        if ($rs === FALSE) {
            $error = mysql_error();
            $this->connector->close();
            throw new Exception("Unable to query " . $request . "(" . $error
                                . ")");
        }
        $fields = array();
        while($rows = mysql_fetch_assoc($rs)) {
            array_push($fields, $rows['Field']);
        }
        $this->connector->close();
        return $fields;
    }
    
    /** Get a single object from a single table matching a particular value
     * for a given field. Typically used to get an object from an id.
     * @param string table The table name.
     * @param string idField The field name on which value requirement is done.
     * @param mixed id The requested value for the given field field.
     * @return array Associative array representing object or NULL if not found.
     * @throws excetion If connection cannot be opened or if request is not
     * valid.
     */
    public function getOne($table, $idField, $id) {
        $this->connector->open();
        $table = $this->escape($table);
        $idField = $this->escape($idField);
        $id = $this->escape($id);
        $request = sprintf("SELECT * FROM %s WHERE %s = \"%s\"",
                           $table,
                           $idField,
                           $id);
        $rs = mysql_query($request, $this->connector->getConnection());
        if ($rs === FALSE) {
            $error = mysql_error();
            $this->connector->close();
            throw new Exception("Unable to query " . $request . "(" . $error
                                . ")");
        }
        $row = mysql_fetch_assoc($rs);
        if ($row === FALSE) {
            $return = NULL;
        } else {
            $return = $row;
        }
        $this->connector->close();
        return $return;
    }

    /** Get an object from multiple tables.
     * @param array tables The list of tables to use
     * @param string idField The name of the field to filter
     * @param mixed id The value of the filtered field
     * @param array linkFields list of linked fields, first with second, third
     * with fourth…
     */
    public function getComplexOne($tables, $idField, $id, $linkFields) {
        $this->connector->open();
        $request = "SELECT * FROM ";
        for($i = 0; $i < count($tables); $i++) {
            if ($i == 0) {
                $request .= $this->escape($tables[$i]);
            } else {
                $request .= ", " . $this->escape($tables[$i]);
            }
        }
        $idField = $this->escape($idField);
        $id = $this->escape($id);
        $request .= sprintf(" WHERE %s = \"%s\"", $idField, $id);
        for($i = 0; $i < count($linkFields); $i += 2) {
            $linkFieldA = $this->escape($linkFields[$i]);
            $linkFieldB = $this->escape($linkFields[$i + 1]);
            $request .= sprintf(" AND %s = %s", $linkFieldA, $linkFieldB);
        }
        $rs = mysql_query($request, $this->connector->getConnection());
        if ($rs === FALSE) {
            $error = mysql_error();
            $this->connector->close();
            throw new Exception("Unable to query " . $request . "(" . $error
                                . ")");
        }
        $row = mysql_fetch_assoc($rs);
        if ($row === FALSE) {
            $return = NULL;
        } else {
            $return = $row;
        }
        $this->connector->close();
        return $return;
    }

    /** Get all the content of a table */
    public function getAll($table) {
        $this->connector->open();
        $table = $this->escape($table);
        $request = sprintf("SELECT * FROM %s", $table);
        $rs = mysql_query($request, $this->connector->getConnection());
        if ($rs === FALSE) {
            $error = mysql_error();
            $this->connector->close();
            throw new Exception("Unable to query " . $request . "(" . $error
                                . ")");
        }
        $return = array();
        while ($row = mysql_fetch_assoc($rs)) {
            array_push($return, $row);
        }
        $this->connector->close();
        return $return;
    }

    /** Get multiple object from a table matching the given criteria.
     * @param string table The table name
     * @param array criteriaFileds The name of the fields to check.
     * @param array criteriaValues The values of each criteria.
     */
    public function getCriteria($table, $criteriaFields, $criteriaValues) {
        $this->connector->open();
        // Build the request
        $request = sprintf("SELECT * FROM %s WHERE ", $table);
        $limit = min(count($criteriaFields), count($criteriaValues));
        for($i = 0; $i < $limit; $i++) {
            // Escape string value with ""
            $field = $this->escape($criteriaFields[$i]);
            $value = $this->escape($criteriaValues[$i]);
            if ($i == 0) {
                $request .= sprintf("%s = \"%s\"", $field, $value);
            } else {
                $request .= sprintf("AND %s = \"%s\"", $field, $value);
            }
        }
        // Run it
        $rs = mysql_query($request, $this->connector->getConnection());
        if ($rs === NULL) {
            $error = mysql_error();
            $this->connector->close();
            throw new Exception("Unable to query " . $request . "(" . $error
                                . ")");
        }
        $return = array();
        while ($row = mysql_fetch_assoc($rs)) {
            array_push($return, $row);
        }
        $this->connector->close();
        return $return;
    }
    
    /** Get the list of registered values for a given field of a given table */
    function getValues($table, $field) {
        $this->connector->open();
        $table = $this->escape($table);
        $field = $this->escape($field);
        $request = sprintf("SELECT %s FROM %s GROUP BY %s", $field, $table,
                           $field);
        // Run it
        $rs = mysql_query($request, $this->connector->getConnection());
        if ($rs === NULL) {
            $error = mysql_error();
            $this->connector->close();
            throw new Exception("Unable to query " . $request . "(" . $error
                                . ")");
        }
        $return = array();
        while ($row = mysql_fetch_array($rs)) {
            array_push($return, $row[0]);
        }
        $this->connector->close();
        return $return;
    }


    function addOne($object, $table) {
        $this->connector->open();
        $table = $this->escape($table);
        $fields = "";
        $values = "";
        $first = TRUE;
        foreach($object as $key => $value) {
            $value = $this->escape($value);
            $key = $this->escape($key);
            if ($first) {
                $first = FALSE;
                $fields .= sprintf("%s",$key);
                $values .= sprintf("\"%s\"", $value);
            } else {
                $fields .= sprintf(", %s",$key);
                $values .= sprintf(", \"%s\"", $value);
            }
        }
        $request = sprintf("INSERT INTO %s (%s) VALUES (%s)",
                           $table, $fields, $values);
        if (!mysql_query($request, $this->connector->getConnection())) {
            $this->connector->close();
            $error = mysql_error();
            $this->connector->close();
            throw new Exception("Unable to query " . $request . "(" . $error
                                . ")");
        } else {
            $this->connector->close();
        }
    }

    function delete($table, $idField, $id) {
        $this->connector->open();
        $idField = $this->escape($idField);
        $id = $this->escape($id);
        $request = sprintf("DELETE FROM %s WHERE %s = \"%s\"",
                           $table,
                           $idField,
                           $id);
        if (mysql_query($request, $this->connector->getConnection())) {

        }
        $this->connector->close();
    }

    function updateOne($object, $table, $idField) {
        $this->connector->open();
        $table = $this->escape($table);
        $fields = "";
        $first = TRUE;
        foreach($object as $key => $value) {
            $value = $this->escape($value);
            $key = $this->escape($key);
            if ($first) {
                $first = FALSE;
                $fields .= sprintf("%s = \"%s\"", $key, $value);
            } else {
                $fields .= sprintf(", %s = \"%s\"", $key, $value);
            }
        }
        $id = $object[$idField];
        $id = $this->escape($id);
        $request = sprintf("UPDATE %s SET %s WHERE %s = \"%s\"",
                           $table, $fields, $idField, $object[$idField]);
        if (!mysql_query($request, $this->connector->getConnection())) {
            $this->connector->close();
            $error = mysql_error();
            $this->connector->close();
            throw new Exception("Unable to query " . $request . "(" . $error
                                . ")");
        } else {
            $this->connector->close();
        }
    }
}

?>
