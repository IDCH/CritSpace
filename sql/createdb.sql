-- File: createdb.sql
-- Author: Neal Audenaert (neal@idch.org)
--
-- This file provides a basic installation script to create a new database 
-- and database user that has restricted access to work with that database

-- Create the database to be used by this installation
CREATE DATABASE critspace DEFAULT CHARACTER SET = 'UTF8';

-- Create the administrative user for this project, remove all
-- permissions and then add back the permissions we want.
-- Note that this password is going to be used in plain text server-side 
-- configuration scripts. 
CREATE USER 'critspace_admin'@'localhost' IDENTIFIED BY 'p455w0Rd'; 
REVOKE ALL PRIVILEGES, GRANT OPTION FROM 'critspace_admin'@'localhost';
GRANT ALL PRIVILEGES
  ON critspace.* 
  TO 'critspace_admin'@'localhost'; 

-- Revoke alter privledges. Dsome privledges that are especially dangerous
-- TODO need to update this to be more precise.
REVOKE ALTER
  ON critspace.* 
  FROM 'critspace_admin'@'localhost';

-- Optionally, limit usage to prevent this user 
-- Use this with caution, only if needed. Other usage restrictions include
-- MAX_CONNECTIONS_PER_HOUR, MAX_UPDATES_PER_HOUR and MAX_USER_CONNECTIONS
-- GRANT USAGE ON *.* TO 'critspace_admin'@'localhost' WITH MAX_QUERIES_PER_HOUR 10000;

-- Optionally, you may wish to repeat this process for a 'webuser' with 
-- restricted permissions (SELECT, UPDATE, INSERT, DELETE?, EXECUTE?)
