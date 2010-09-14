CREATE TABLE IF NOT EXISTS Users (
    user_id   SERIAL PRIMARY KEY,
    username  VARCHAR(32)     NOT NULL, 
    password  CHAR(40)        NOT NULL, 
    resp      INTEGER,
    status    ENUM('A', 'R') NOT NULL DEFAULT 'A'       -- active or reset required

) ENGINE=InnoDB CHARSET utf8;

CREATE TABLE IF NOT EXISTS Groups (
    group_id    SERIAL PRIMARY KEY,
    groupname   VARCHAR(32) NOT NULL UNIQUE,
    description TEXT
) ENGINE=InnoDB CHARSET utf8;

CREATE TABLE IF NOT EXISTS Roles (
    role        CHAR(64) PRIMARY KEY,
    description TEXT
    
) ENGINE=InnoDB CHARSET utf8;


CREATE TABLE IF NOT EXISTS UserRoles (
    user_id   BIGINT  UNSIGNED NOT NULL,
    role      CHAR(64) NOT NULL
) ENGINE=InnoDB CHARSET utf8;