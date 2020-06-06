<?php
/*
Author: Artem M br_in_arms@mail.ru
*/
 
require('db.php');

$msg = "Введите данные пользователя";

$userid = $_POST["userid"];

if (isset($userid)){

    if(checkUserId($con, $userid))
    {
        setcookie("userid", $userid, time() + (86400 * 30), "/");
        header('Location: /');
        exit;
    }
    else
        $msg = "Неправильный User ID";
}

?>

<html>
   <head>
    <meta charset="utf-8">
    <title>Склад мини - Логин</title>
    <link rel="stylesheet" href="css/style.css" />
   </head>
	
   <body align = "center">

         <form class = "form-signin" role = "form" 
            action = "/login" method = "post">
            <br><p><?php echo $msg; ?></p>
            <input type = "password" class = "form-control" 
               name = "userid" placeholder = "User ID" 
               required autofocus/></br>
            
            <button class = "btn" type = "submit" 
               name = "login">Вход</button>
         </form>

      
   </body>
</html>