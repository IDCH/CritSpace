-- This is a SimpleSQL test script for trying to access a database.
-- It shoule execute 2 SQL statements.

# This is a comment
-- this is also a comment
CREATE TABLE IF NOT EXISTS TestTable (
    type_id     CHAR(32) PRIMARY KEY,   // this is an end of line comment. 
    data        CHAR(32) UNIQUE
) ENGINE=InnoDB CHARSET utf8;

DROP TABLE IF EXISTS TestTable;

-- /* 
--  * Multi-line comments are not yet supported.
--  */
