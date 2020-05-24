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
echo action($con, $_GET["uid"], $_GET["prodid"], $_GET["delta"]);
?>
</body>
</html>