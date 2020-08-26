<?php
/*
Author: Artem Mouraviev ilinic8@mail.ru
*/

// Limits on number of history records online and in Android app
const __MAX_WEBSITE_HISTORY__ = 1000;   
const __MAX_ANROID_APP_HISTORY__ = 200; 

// format strings to control inventory and history list appearance in Android app
const __INVENTORY_TO_ANDROID_APP_FORMAT__ = "№%-6s ∑ %-3s %s"; //  s s s = prodId count prodName
const __HISTORY_TO_ANDROID_APP_FORMAT_STR_TOP__ = "%s 𝚫 %s ∑%s→%s №%s"; //date, delta, count_before, count_after, productId
const __HISTORY_TO_ANDROID_APP_FORMAT_STR_BOTTOM__ = "%s → %s"; // userName, productName

//Database connection mysqli_connect("127.0.0.1", "my_user", "my_password", "my_db");
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
            if(preg_match('^[a-zA-Z\s\d-_@]+$', $data[0]))
                continue;
                
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
            if(preg_match('^[a-zA-Z\s\d-_]+$', $data[0]) || $data[0]=="")
                continue;
                
            if(!array_key_exists(2, $data))
                $data[2] = "1";
            
            $sel_query = "INSERT INTO products (id, prodname, count, update_helper) VALUES('" . trim($data[0]) . "', '" . trim($data[1]) . "','" . $data[2] . "',1) ON DUPLICATE KEY UPDATE prodname='" . trim($data[1]) . "', update_helper=1";
            if (!mysqli_query($con, $sel_query)) return "Ошибка чтения списка продуктов 3 " . mysqli_error($con);
        }

        $sel_query = "DELETE FROM products WHERE update_helper= 0";
        if (!mysqli_query($con, $sel_query)) return "Ошибка чтения списка продуктов 4 " . mysqli_error($con);

        fclose($h);

        return "Список продуктов обновлен";
    }

    else return "Ошибка чтения списка продуктов 0";
}

function get_product($con, $userid, $prodid)
{

    $sel_query = "SELECT id FROM users WHERE id='$userid'";

    $result = mysqli_query($con, $sel_query);
    if (mysqli_num_rows($result) != 1) {
        
        $row["err"] = 1;
        $row["userid"] = $userid;
        $row["prodid"] = $prodid;

        return json_encode($row);
    }
    
    $sel_query = "SELECT prodname, count FROM products WHERE id='$prodid'";
    $result = mysqli_query($con, $sel_query);
    if (mysqli_num_rows($result) != 1) {
        
        $row["err"] = 2;
        $row["userid"] = $userid;
        $row["prodid"] = $prodid;

        return json_encode($row);
    }
    
    $row = mysqli_fetch_assoc($result);

    $row["err"] = 0;
    $row["userid"] = $userid;
    $row["prodid"] = $prodid;
    
    return json_encode($row);
}

function check_login($con, $userid)
{
    $sel_query = "SELECT username FROM users WHERE id='$userid'";

    $result = mysqli_query($con, $sel_query);
    if (mysqli_num_rows($result) == 1) 
        return "{\"err\": 0}";
    else 
        return "{\"err\": 1}";
}


function action($con, $userid, $prodid, $delta)
{
    
    $sel_query = "SELECT username FROM users WHERE id='$userid'";

    $result = mysqli_query($con, $sel_query);
    if (mysqli_num_rows($result) != 1) {
        
        $row["err"] = 1;
        $row["userid"] = $userid;
        $row["prodid"] = $prodid;

        return json_encode($row);
    }
    
    $row = mysqli_fetch_assoc($result);
    $username = $row["username"];
    
    $sel_query = "SELECT prodname, count FROM products WHERE id='$prodid'";
    $result = mysqli_query($con, $sel_query);
    if (mysqli_num_rows($result) != 1) {
        
        $row["err"] = 2;
        unset($row["username"]);
        $row["userid"] = $userid;
        $row["prodid"] = $prodid;

        return json_encode($row);
    }
    
    $row = mysqli_fetch_assoc($result);

    $countbefore = $row["count"];
    $prodname = $row["prodname"];

    $sel_query = "UPDATE products SET count=GREATEST(0,count+'$delta') WHERE id='$prodid'";
    if (!mysqli_query($con, $sel_query)) {
        $row["err"] = 2;
        unset($row["username"]);
        $row["userid"] = $userid;
        $row["prodid"] = $prodid;

        return json_encode($row);
    }

    $sel_query = "SELECT count, prodname, id FROM products WHERE id='$prodid'";
    $result = mysqli_query($con, $sel_query);
    if (mysqli_num_rows($result) != 1) {
        $row["err"] = 2;
        unset($row["username"]);
        $row["userid"] = $userid;
        $row["prodid"] = $prodid;

        return json_encode($row);
    }
    
    $row = mysqli_fetch_assoc($result);

    $countafter = $row["count"];

    $sel_query = "INSERT INTO `history`(`product`, `user`, `delta`, `count_before`, `count_after`, `prodid`) VALUES ('$prodname', '$username', '$delta', '$countbefore', '$countafter', '$prodid')";
    if (!mysqli_query($con, $sel_query)) {
        $row["err"] = 2;
        unset($row["username"]);
        $row["userid"] = $userid;
        $row["prodid"] = $prodid;

        return json_encode($row);
    }

    $row["err"] = 0;
    $row["userid"] = $userid;
    $row["prodid"] = $prodid;

    return json_encode($row);
}


function get_inventory($con, $userid)
{

    $sel_query = "SELECT id FROM users WHERE id='$userid'";

    $result = mysqli_query($con, $sel_query);
    if (mysqli_num_rows($result) != 1) {
        return "[]";
    }
    
    $sel_query = "SELECT id, count AS cnt, prodname AS name FROM products";
    $result = mysqli_query($con, $sel_query);
    
    //String.format("№ %1$-8s ∑ %1$-4s  %s", el.id, el.cnt, el.name)
    
    while($row = mysqli_fetch_assoc($result))
    {   
        $row["fullStr"] = sprintf(__INVENTORY_TO_ANDROID_APP_FORMAT__, $row["id"], $row["cnt"], $row["name"]);
        $res[] = $row;
    }

    return json_encode($res);
}

function get_history($con, $userid)
{

    $sel_query = "SELECT id FROM users WHERE id='$userid'";

    $result = mysqli_query($con, $sel_query);
    if (mysqli_num_rows($result) != 1) {
        return "[]";
    }
    
    //        String dt, unme, pnme, pid;
    //        int d, bf, af;
    
    $sel_query = "Select actiondate, user AS unme, product AS pnme, prodid AS pid, delta AS d, count_before AS bf, count_after AS af FROM history ORDER BY actiondate DESC LIMIT " . __MAX_ANROID_APP_HISTORY__;
    $result = mysqli_query($con, $sel_query);
    while($row = mysqli_fetch_assoc($result))
    {
        $topStr = sprintf(__HISTORY_TO_ANDROID_APP_FORMAT_STR_TOP__,  date("H:i:s dMy",strtotime($row["actiondate"])), $row["d"], $row["bf"], $row["af"], $row["pid"]);
        $botStr = sprintf(__HISTORY_TO_ANDROID_APP_FORMAT_STR_BOTTOM__, $row["unme"], $row["pnme"]);
        
        $rec["topStr"] = $topStr;
        $rec["botStr"] = $botStr;


        $res[] = $rec;
    }

    return json_encode($res);
}

function checkUserId($con, $userid){
    
    if(!isset($userid))
        return false;
    
    $sel_query = "SELECT id FROM users WHERE id='$userid'";

    $result = mysqli_query($con, $sel_query);
    if (mysqli_num_rows($result) == 1)
        return true;
    else
        return false;
}

?>
