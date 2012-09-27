<?php
//    POS-Tech Plonk
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
//    along with POS-Tech.  If not, see <http://www.gnu.org/licenses/>.<!DOCTYPE html>
?>
<html>
<body>
<form action="" method="get" id="cardform" style="position:absolute;top:-500px">
<input id="card" type="text" name="custCard" value="" />
</form>
<script type="text/javascript">
function giveFocus() {
    if (document.getElementById("card").value.length >= 13) {
        document.getElementById("cardform").submit();
        document.getElementById("card").value = "";
    }
    document.getElementById("card").focus();
    setTimeout(giveFocus, 1000);
}
giveFocus();
</script>

<?php

if (!isset($_GET['custCard'])) {
    die();
}

require_once(dirname(dirname(__FILE__)) . "/PDOBuilder.php");
require_once(dirname(dirname(__FILE__)) . "/utils/date_utils.php");
require_once(dirname(__FILE__) . "/plonk_conf.php");

// Pure laziness defines
define("INVALID_MSG", "<p style=\"color:#f00\">Replonk</p><embed src=\"KDE-Sys-App-Negative.ogg\" autostart=\"true\" width=\"0\" height=\"0\" enablejavascript=\"true\">");
define("VALID_MSG", "<p style=\"color:#0a0\">Plonk</p><embed src=\"KDE-Sys-App-Positive.ogg\" autostart=\"true\" width=\"0\" height=\"0\" enablejavascript=\"true\">");

// Init PDO
$pdo = PDOBuilder::getPDO();

// Get customer from card number
$custCard = $_GET['custCard'];
$custStmt = $pdo->prepare("SELECT ID FROM CUSTOMERS WHERE CARD = :card");
$custStmt->execute(array(':card' => $custCard));
if ($db_cust = $custStmt->fetch()) {
    $custId = $db_cust['ID'];
} else {
    die(INVALID_MSG);
}

// Get last purchase date
$stmt = $pdo->prepare("SELECT DATENEW FROM RECEIPTS, TICKETS, TICKETLINES "
                      . "WHERE RECEIPTS.ID = TICKETLINES.TICKET "
                      . "AND RECEIPTS.ID = TICKETS.ID "
                      . "AND PRODUCT = :prdId AND CUSTOMER = :cust");
$stmt->execute(array(':prdId' => $conf['product'],
                     ':cust' => $custId));
$last_date = null;
while ($db_date = $stmt->fetch()) {
    $time = stdtimefstr($db_date['DATENEW']);
    if ($last_date == null || $last_date < $time) {
        $last_date = $time;
    }
}

// Show message
$now = time();
if ($last_date == null || ($now - $last_date) > $conf['validity']) {
    echo INVALID_MSG;
} else {
    echo VALID_MSG;
}
?>

</body>
</html>

