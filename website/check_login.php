<?php
/*
Author: Artem M br_in_arms@mail.ru
*/
require('db.php');
header('Content-Type: application/json');
?>
<?php
echo check_login($con, $_GET["uid"]);
?>