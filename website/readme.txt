Inventory mini

Tracks list of products and change history - online.
Android app to scan QR code (product#) and record product quantity change with user name and timestamp
Or enter product# manually. 
Show inventory and history in android app.


ONLINE PART:
implemented in php/MySQL

1. List products quanity with field sort
2. List history of changes (user, date, delta, etc.)
3. Click on product# to generate QR code for printing
4. To update product list - just edit products.csv file on hosting and refresh products page - red message will appear
5. To update user list - just edit users.csv file on hosting and refresh products page - red message will appear

IMPORTANT:
User ids or product ids (product#) are ALPHANUMERIC + "_" + "-" for greater flexibility.
User and product ids should be UNIQUE STRINGS, otherwise errors will occur. Leading and trailing spaces are stripped.
On product list update quantities of preserved products are not changed.

ANDROID PART:
1. Login using server address and userId (string alpha numeric + "_" + "-").
2. Scan QR code, see product name and quantiy, set quantity delta
3. Manually enter product code in case of QR code scan error
4. Manually enter delta, or hold + - buttons for faster value change
5. To change working (correct) site or userId - reinstall application

INSTALLATION
1. create database using database.sql script
2. check .htaccess file for correct rewrite rules for php files and blocked access to .csv files or some security
3. put correct database connection parameters into db.php file
4. verify correct protocol for the site (http or https)
5. edit and verify correct data in products.csv and users.csv files
6. verify correct access rights for php and csv files
4. download and install android application.
5. provide website and userId in application.
