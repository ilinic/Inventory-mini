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
<title>Склад мини - История</title>
<link rel="stylesheet" href="css/style.css" />
</head>
<body>
<div class="history">
<div class="top"><p><a href=".">В список продуктов</a>&nbsp;&nbsp;</p></div>
<p style="color:#ff0000;"><?php echo parse_users($con); ?></p>
<p style="color:#ff0000;"><?php echo parse_products($con); ?></p>
<h2>История операций</h2>
<table width="100%" border="1" style="border-collapse:collapse;">
<thead>
<tr><th class="tdate"><strong>Дата</strong></th>
    <th class="tuser"><strong>Пользователь</strong></th>
    <th class="tproduct"><strong>Название продукта</strong></th>
    <th class="tdelta"><strong>Дельта</strong></th>
    <th class="tbcount"><strong>До</strong></th>
    <th class="tacount"><strong>После</strong></th>
    <th class="tprodid"><strong>Инв #</strong></th>
</tr>
</thead>
<tbody>
<?php
$sel_query=mysqli_real_escape_string($con,"Select * from history ORDER BY actiondate DESC LIMIT 1000");
$result = mysqli_query($con,$sel_query);
while($row = mysqli_fetch_assoc($result)) { ?>
<tr>
    <td align="center"><?php echo date("H:i:s d M y",strtotime($row["actiondate"])); ?></td>
    <td align="center"><?php echo $row["user"]; ?></td>
    <td align="center"><?php echo $row["product"]; ?></td>
    <td align="center"><?php echo $row["delta"]; ?></td>
    <td align="center"><?php echo $row["count_before"]; ?></td>
    <td align="center"><?php echo $row["count_after"]; ?></td>
    <td align="center"><a href="http://qrcoder.ru/code/?<?php echo $row["prodid"]; ?>&16&0" target="_blank"><?php echo $row["prodid"]; ?></a></td>
</tr>
<?php } ?>
</tbody>
</table>
</div>
</body>
</html>