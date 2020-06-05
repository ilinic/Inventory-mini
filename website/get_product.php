<?php
/*
Author: Artem Mouraviev ilinic8@mail.ru
*/
require('db.php');
header('Content-Type: application/json');
?>
<?php
echo get_product($con, $_GET["uid"],  $_GET["prodid"]);
?>