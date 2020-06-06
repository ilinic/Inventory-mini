<?php
if(isset($_COOKIE['userid'])){
    // delete cookie
    setcookie('userid', null, time() - 1);
    // if you use sessions delete session variables as well
}
header('Location: login');
?>