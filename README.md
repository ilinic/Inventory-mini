Inventory mini
_________________

Tracks list of products and changes history - online.
Android app to scan QR code (product#) and record changes in product quantity (with username and timestamp). Scan code or enter product# manually.
Show inventory and history in Android app and online.

ONLINE PART:
1. List products quantity with field sort
2. List history of changes (user, date, delta, etc.)
3. Click on product# to generate QR code for printing
4. To update product list - just edit products.csv file on hosting and refresh products page - red "products updated" string will appear
5. To update user list - just edit users.csv file on hosting and refresh products page - red "users updated" string will appear on the page

IMPORTANT:
User IDs or product IDs (product#) are ALPHANUMERIC + "_" + "-" for greater flexibility.
User IDs and product IDs should be UNIQUE STRINGS, otherwise errors will occur. 
On user or product uploads leading and trailing spaces of IDs are stripped.
When you perform product list update - quantities of preserved products will not change.

ANDROID PART:
1. Login using server address and userId (string alpha numeric + "_" + "-").
2. Scan QR code, see product name and quantity, set quantity delta, press send button
3. Manually enter product code in case of QR code scan error
4. Manually enter delta, or hold + - buttons for faster value change
5. To change working (correct) server adsress (website) or userId - remove and reinstall application
6. On inventory page - you can sort on product ID, name or quantity. And refresh list from server.
7. On history page - just view last inventory changes
8. Number of history items and product and history list appearances in Android app are partially customizable in db.php file (templates are provided)

INSTALLATION
1. get shared hosting or dedicated server with PHP and MySQL
2. create database using database.sql script
3. put all files to html root folder of your hosting
4. check .htaccess file for correct rewrite rules for php files and blocked access to .csv files for some minimal security
5. put correct database connection parameters into db.php file
6. verify correct protocol for the site (http or https)
7. edit and verify correct data in products.csv and users.csv files
8. verify correct access rights for php and csv files
9. download and install android application APK from link on products page.
10. type FULL (with http...) website address and userId (from users.csv file) to application on login screen.

![alt text](https://github.com/ilinic/Inventory-mini/blob/master/screenshots/login.png?raw=true)
![alt text](https://github.com/ilinic/Inventory-mini/blob/master/screenshots/scan.png?raw=true)
![alt text](https://github.com/ilinic/Inventory-mini/blob/master/screenshots/inv.png?raw=true)
![alt text](https://github.com/ilinic/Inventory-mini/blob/master/screenshots/hist.png?raw=true)