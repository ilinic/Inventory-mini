<?php
/*
Author: Artem Mouraviev ilinic8@mail.ru
*/
require('db.php');
?>
<!DOCTYPE html>
<html>
<head>
<meta charset="utf-8">
</head>
<body>
<?php

$row =  getcount($con, $_GET["uid"], $_GET["prodid"]);

if($row == "") return;

unset($row["username"]);
echo json_encode($row);
?>
</body>
</html>