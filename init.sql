CREATE DATABASE IF NOT EXISTS carservicedb;

ALTER USER 'root'@'%' IDENTIFIED WITH mysql_native_password BY 'driss123';

GRANT ALL PRIVILEGES ON carservicedb.* TO 'root'@'%' WITH GRANT OPTION;

FLUSH PRIVILEGES;
