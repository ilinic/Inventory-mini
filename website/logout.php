<?php
/*
Author: Javed Ur Rehman
Website: https://www.allphptricks.com/
*/

session_start();
if(session_destroy()) // Destroying All Sessions
{
header("Location:  http://sklad.samogoncity.ru/login.php"); // Redirecting To Home Page
}
?>