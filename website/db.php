<?php
/*
Author: Artem Mouraviev ilinic8@mail.ru
*/

$con = mysqli_connect("localhost", "samogo6c_sklad", "sklad123_", "samogo6c_sklad");
// Check connection
if (mysqli_connect_errno())
{
    echo "Failed to connect to MySQL: " . mysqli_connect_error();
}

function parse_users($con)
{

    $fchanged = filemtime("users.csv");

    $sel_query = "Select value from config WHERE param='user_list_changed'";
    $result = mysqli_query($con, $sel_query);
    $row = mysqli_fetch_assoc($result);

    if ($row["value"] != $fchanged)
    {
        $sel_query = "Update config  SET value = '" . $fchanged . "' WHERE param='user_list_changed'";
        if (!mysqli_query($con, $sel_query)) return ("Ошибка чтения списка пользователей 1 " . mysqli_error($con));
    }
    else return "";

    if (($h = fopen("users.csv", "r")) !== false)
    {
        $sel_query = "UPDATE `users` SET `update_helper`= 0";

        if (!mysqli_query($con, $sel_query)) return ("Ошибка чтения списка пользователей 2 " . mysqli_error($con));

        // Convert each line into the local $data variable
        while (($data = fgetcsv($h, 1000, ";")) !== false)
        {
            $sel_query = "INSERT INTO users (id, username, update_helper) VALUES('" . trim($data[0]) . "', '" . trim($data[1]) . "', 1) ON DUPLICATE KEY UPDATE username='" . trim($data[1]) . "', update_helper=1";
            if (!mysqli_query($con, $sel_query)) return "Ошибка чтения списка пользователей 3 " . mysqli_error($con);
        }

        $sel_query = "DELETE FROM users WHERE update_helper= 0";

        if (!mysqli_query($con, $sel_query)) return "Ошибка чтения списка пользователей 4 " . mysqli_error($con);

        fclose($h);

        return "Список пользователей обновлен";
    }

    else return "Ошибка чтения списка пользователей 0";
}


function parse_products($con)
{

    $fchanged = filemtime("products.csv");

    $sel_query = "Select value from config WHERE param='product_list_changed'";
    $result = mysqli_query($con, $sel_query);
    $row = mysqli_fetch_assoc($result);

    if ($row["value"] != $fchanged)
    {
        $sel_query = "Update config  SET value = '" . $fchanged . "' WHERE param='product_list_changed'";
        if (!mysqli_query($con, $sel_query)) return ("Ошибка чтения списка продуктов 1 " . mysqli_error($con));
    }
    else return "";

    if (($h = fopen("products.csv", "r")) !== false)
    {
        $sel_query = "UPDATE `products` SET `update_helper`= 0";
        if (!mysqli_query($con, $sel_query)) return ("Ошибка чтения списка продуктов 2 " . mysqli_error($con));

        // Convert each line into the local $data variable
        while (($data = fgetcsv($h, 1000, ";")) !== false)
        {
            $sel_query = "INSERT INTO products (id, prodname, update_helper) VALUES('" . trim($data[0]) . "', '" . trim($data[1]) . "', 1) ON DUPLICATE KEY UPDATE prodname='" . trim($data[1]) . "', update_helper=1";
            if (!mysqli_query($con, $sel_query)) return "Ошибка чтения списка продуктов 3 " . mysqli_error($con);
        }

        $sel_query = "DELETE FROM products WHERE update_helper= 0";
        if (!mysqli_query($con, $sel_query)) return "Ошибка чтения списка продуктов 4 " . mysqli_error($con);

        fclose($h);

        return "Список продуктов обновлен";
    }

    else return "Ошибка чтения списка продуктов 0";
}


function getcount($con, $userid, $prodid)
{

    $sel_query = "SELECT username FROM users WHERE id='$userid'";

    $result = mysqli_query($con, $sel_query);
    if (mysqli_num_rows($result) == 0) return "";
    $row = mysqli_fetch_assoc($result);

    $username = $row["username"];
    
    $sel_query = "SELECT prodname, count FROM products WHERE id=$prodid";
    $result = mysqli_query($con, $sel_query);
    if (mysqli_num_rows($result) != 1) return "";
    $row = mysqli_fetch_assoc($result);

    $row["username"] = $username;
    $row["prodid"] = $prodid;
    
    //echo json_encode($row);

    return $row;
}

function action($con, $userid, $prodid, $delta)
{

    $return = "{\"err\": 1, \"data\":\"%s\"}";

    $res = getcount($con, $userid, $prodid);
    
    if($res=="") return sprintf($return,"Ошибка авторизации");
    
    $countbefore = $res["count"];
    $prodname = $res["prodname"];
    $prodid = $res["prodid"];
    $username = $res["username"];

    if ($countbefore == "") return sprintf($return,"Ошибка БД");

    $sel_query = "UPDATE products SET count=GREATEST(0,count+'$delta') WHERE id=$prodid";
    if (!mysqli_query($con, $sel_query)) return sprintf($return,"Ошибка БД1");

    $sel_query = "SELECT count FROM products WHERE id=$prodid";
    $result = mysqli_query($con, $sel_query);
    if (mysqli_num_rows($result) != 1) return sprintf($return,"Ошибка БД2");
    $row = mysqli_fetch_assoc($result);

    $countafter = $row["count"];

    $sel_query = "INSERT INTO `history`(`product`, `user`, `delta`, `count_before`, `count_after`, `prodid`) VALUES ('$prodname', '$username', '$delta', '$countbefore', '$countafter', '$prodid')";
    if (!mysqli_query($con, $sel_query)) return sprintf($return,"Ошибка БД3");

    return sprintf("{\"err\": 0, \"data\":\"%s\"}", $countafter);
}

?>
