<?php
/*
Author: Artem Mouraviev ilinic8@mail.ru
*/
 
require('db.php');


function sortLink($field){

    if($_GET["ord"] == "")
        $_GET["ord"] = "asc";
        
    if($_GET["sort"] == "")
        $_GET["sort"] = "prodname";        
        
    if($_GET["ord"] == "asc")
        $nextOrd = "desc";
    else
        $nextOrd = "asc";
        
    echo ".?sort=", $field, "&ord=", $nextOrd;
}


?>
<!DOCTYPE html>
<html>
<head>
<meta charset="utf-8">
<title>Склад мини - Продукты</title>
<link rel="stylesheet" href="css/style.css" />
</head>
<body>

<div class="products">
<div class="top"><p><a href="history">В историю</a>&nbsp;&nbsp;</p></div>    
<p style="color:#ff0000;"><?php echo parse_users($con); ?></p>
<p style="color:#ff0000;"><?php echo parse_products($con); ?></p>
<h2>Продукты на складе</h2>
<table width="100%" border="1" style="border-collapse:collapse;">
<thead>
<tr><th class="tno"><strong><a href="<?php sortLink("id"); ?>">Инв #</a></strong></th><th class="tname"><strong><a href="<?php sortLink("prodname"); ?>">Название продукта</a></strong></th><th class="tcount"><strong><a href="<?php sortLink("count"); ?>">Наличие</a></strong></th></tr>
</thead>
<tbody>
<?php
$count=0;

$sel_query=mysqli_real_escape_string($con,"Select * from products ORDER BY ". $_GET["sort"]. " " . $_GET["ord"]);
$result = mysqli_query($con,$sel_query);
while($row = mysqli_fetch_assoc($result)) { ?>
<tr>
    <td align="center"><a href="http://qrcoder.ru/code/?<?php echo $row["id"]; ?>&16&0" target="_blank"><?php echo $row["id"]; ?></a></td>
    <td align="center"><?php echo $row["prodname"]; ?></td>
    <td align="center"><?php echo $row["count"]; ?></td>
</tr>
<?php $count++; } ?>
</tbody>
</table>
<br><p><strong><?php echo "Всего: ".$count;  ?></strong></p>
<div class="top"><p><a href="./mini-inventory.apk" class="top"><img src="downloadbtn.png" title="Download Mini Inventory Android" alt="Mini Inventory"></a></p></div>
</div>
</body>
</html>
