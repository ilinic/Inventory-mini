<?php
/*
Author: Artem Mouraviev ilinic8@mail.ru
*/
 
require('db.php');

if(!checkUserId($con, $_COOKIE['userid']))
{
    header('Location: login');
    exit;
}

?>
<!DOCTYPE html>
<html>
<head>
<meta charset="utf-8">
<title>Склад мини - Продукты для вывода на печать</title>
<link rel="stylesheet" href="css/style.css" />
</head>
<body>

<div class="printed">
    
<div>
    <a href="/"><img src="logo.png" class="logo" alt="Inventory Mini" style="vertical-align: middle;"></a>
</div>    
<p style="color:#ff0000;"><?php echo parse_users($con); ?></p>
<p style="color:#ff0000;"><?php echo parse_products($con); ?></p>
<h2>Продукты для вывода на печать</h2>
<table width="100%" border="1" style="border-collapse:collapse;">
<thead>
<tr><th class="tno"><strong>Инв #</strong></th><th class="tname"><strong>Данные продукта</strong></th></tr>
</thead>
<tbody>
<?php
$count=0;

$sel_query=mysqli_real_escape_string($con,"Select * from products ORDER BY prodname");
$result = mysqli_query($con,$sel_query);
while($row = mysqli_fetch_assoc($result)) { ?>
<tr>
    <td align="center"><img src="http://qrcoder.ru/code/?<?php echo $row["id"]; ?>&4&0" width="170" height="170" border="0"><?php echo $row["id"]; ?></br></br></td>
    <td align="center" style="padding: 10px 10px 10px 10px; font-size: 40px; font-weight: bold;"><?php echo $row["prodname"]; ?></td>
</tr>
<?php $count++; } ?>
</tbody>
</table>
<br><strong><?php echo "Всего: ".$count;  ?></strong>
<div class="top"><a href="./inventory-mini.apk">Download Android App&nbsp;&nbsp;&nbsp;<img src="favicon.ico" width = "20px" height="20px" alt="Mini Inventory" style="vertical-align: middle;"></a></div>
</div>
</body>
</html>
