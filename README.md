Inventory mini
_________________

Tracks list of products and changes history - online.
Android app to scan QR code (product#) and record changes in product quantity (with username and timestamp). Scan code or enter product# manually.
Show inventory and history in Android app and online.

ONLINE PART:
1. List products quantity with field sort
2. List history of changes (user, date, delta, etc.)
3. Click on product# to generate QR code for printing
4. To update product list - just edit products.csv file on hosting and refresh products page - red message will appear
5. To update user list - just edit users.csv file on hosting and refresh products page - red message will appear

IMPORTANT:
User ids or product ids (product#) are ALPHANUMERIC + "_" + "-" for greater flexibility.
User and product IDs should be UNIQUE STRINGS, otherwise errors will occur. Leading and trailing spaces are stripped.
When you perform product list update - quantities of preserved products will not change.

ANDROID PART:
1. Login using server address and userId (string alpha numeric + "_" + "-").
2. Scan QR code, see product name and quantity, set quantity delta
3. Manually enter product code in case of QR code scan error
4. Manually enter delta, or hold + - buttons for faster value change
5. To change working (correct) site or userId - remove and reinstall application

INSTALLATION
1. get shared hosting or dedicated server with PHP and MySQL
2. create database using database.sql script
3. put all files to html root folder of your hosting
4. check .htaccess file for correct rewrite rules for php files and blocked access to .csv files for some minimal security
5. put correct database connection parameters into db.php file
6. verify correct protocol for the site (http or https)
7. edit and verify correct data in products.csv and users.csv files
8. verify correct access rights for php and csv files
9. download and install android application.
10. provide full website address and userId (from users.csv file) in application to login.


Author:
Artem Mouraviev
ilinic8@mail.ru