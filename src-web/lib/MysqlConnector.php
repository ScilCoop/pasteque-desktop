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

//require_once(dirname(__FILE__)."/../../env.php");
//require_once($_TOOLS_DIR."/TypeChecker.php");

/** Connection handler. The connector manages opening and closing database
 * connection and is passed to any class that requires to connect to a
 * database.
 *
 * This connector handles connection to a single database.
 */
class MysqlConnector {

	private $sHost;
	private $iPort;
	private $sUserName;
	private $sPassword;
	private $sDbName;
	private $mysql;
	/** Counts references to connection uses. */
	private $iTokens;

	/** Create a connector to a database.
	 * @param String sHost URL of database (without port).
	 * @param String sPort Host port. Can be null, in that case default port
	 * 3306 is assigned.
	 * @param String sUserName Database user.
	 * @param String sPassword User's password (nullable).
	 * @param String sDbName Database name.
	 */
	public function __construct($sHost, $iPort, $sUserName, $sPassword,
			$sDbName) {
        /*		TypeChecker::checkNotNull($sHost, '$sHost');
		TypeChecker::checkNotNull($sUserName, '$sUserName');
		TypeChecker::checkNotNull($sDbName, '$sDbName');
		TypeChecker::checkString($sHost, true, '$sHost');
		if ($iPort !== NULL) {
			TypeChecker::checkInt($iPort, '$iPort');
		}
		TypeChecker::checkString($sUserName, true, '$sUserName');
		if ($sPassword !== NULL) {
			TypeChecker::checkString($sPassword, false, '$sPassword');
		}
		TypeChecker::checkString($sDbName, true, '$sDbName');*/
		$this->sHost = $sHost;
		if ($iPort == NULL) {
			$this->iPort = 3306;
		} else {
			$this->iPort = $iPort;
		}
		$this->sUserName = $sUserName;
		$this->sPassword = $sPassword;
		$this->sDbName = $sDbName;
	}

	/** Open the connection to the database and increment connections count.
	 * Success call of open() (i.e. no exception) must be folllowed
	 * by a call to close().
	 * @return Boolean TRUE on success, FALSE if the connection is reused
	 * (but a call to close is required anyway).
	 * @throw Exception If connection cannot be opened.
	 */
	public function open() {
		if ($this->mysql != NULL) {
			$this->iTokens++;
			return FALSE;
		}
		$this->mysql = mysql_connect($this->sHost.":".$this->iPort,
			$this->sUserName, $this->sPassword);
		if ($this->mysql === FALSE) {
			$this->mysql = NULL;
			$error = mysql_error();
			throw new Exception("Unable to open database (".$error.")");
		} else {
			if (mysql_select_db($this->sDbName, $this->mysql) === FALSE) {
				$error = mysql_error();
				mysql_close($this->mysql);
				$this->mysql = NULL;
				throw new Exception("Unable to open database (".$error.")");
			}
			$this->iTokens++;
			return TRUE;
		}
	}

	/** Close the connection to the database. This does nothing if connection
	 * is not opened.
	 * Each success call to open() must be followed by a call to close to
	 * decrement connection tokens. Connection is effectively closed when
	 * all tokens are given back.
	 * @return Boolean TRUE on success or if connection is not opened,
	 * FALSE on failure
	 * @throws Exception If the connection is not opened
	 */
	public function close() {
		if ($this->mysql == NULL) {
			$this->iTokens = 0;
			return TRUE;
		}
		$this->iTokens--;
		if ($this->iTokens <= 0) {
			$bSuccess = mysql_close($this->mysql);
			if ($bSuccess) {
				$this->mysql = NULL;
			}
			return $bSuccess;
		}
	}

	/** Get connection resource to database
	 * @return Resource The mysql resource to call mysql functions on or
	 * NULL if not opened
	 */
	public function getConnection() {
		return $this->mysql;
	}
}

?>
