DROP TABLE IF EXISTS ConfigurationOptions;
DROP TABLE IF EXISTS PropertyConfigurations;
DROP TABLE IF EXISTS VisualProperties;
DROP TABLE IF EXISTS PropertyGroups;
DROP TABLE IF EXISTS PropertyTypes;

CREATE TABLE IF NOT EXISTS PropertyTypes (
    type_id     VARCHAR(32) PRIMARY KEY, 
    css         VARCHAR(32) UNIQUE,
    name        VARCHAR(64) NOT NULL,
    description TEXT,
    format      ENUM('txt', 'num', 'tog') NOT NULL, 
    config      TEXT
) ENGINE=InnoDB CHARSET utf8;

CREATE TABLE IF NOT EXISTS PropertyGroups (
    group_id     SERIAL PRIMARY KEY,
    parent_group BIGINT UNSIGNED,
    group_type   VARCHAR(255) DEFAULT 'undefined',
    group_name   VARCHAR(32),
    
    FOREIGN KEY (parent_group)
      REFERENCES PropertyGroups (group_id)
      ON DELETE CASCADE
) ENGINE=InnoDB CHARSET utf8;

CREATE TABLE IF NOT EXISTS VisualProperties (
    vprop_id    SERIAL PRIMARY KEY,
    group_id    BIGINT UNSIGNED,
    prop_name   VARCHAR(32),
    type_id     VARCHAR(32) NOT NULL, 
    prop_value  VARCHAR(128),
    enabled     BOOLEAN,
    
    -- denormalized for effeciency - this is redundant of PropertyTypes.format
    format      ENUM('txt', 'num', 'tog') NOT NULL,
    
    FOREIGN KEY (type_id)
      REFERENCES PropertyTypes (type_id)
      ON DELETE RESTRICT,
    FOREIGN KEY (group_id)
      REFERENCES PropertyGroups (group_id)
      ON DELETE CASCADE
) ENGINE=InnoDB CHARSET utf8;

CREATE TABLE IF NOT EXISTS PropertyConfigurations (
    vprop_id     BIGINT UNSIGNED PRIMARY KEY,
    prop_value   VARCHAR(128),
    enabled      BOOLEAN,
    units        VARCHAR(16),
    minimum      FLOAT,
    maximum      FLOAT,
    on_value     VARCHAR(128),
    off_value    VARCHAR(128),
    options_only BOOLEAN DEFAULT FALSE,
    regex        VARCHAR(255),
    regext       ENUM('i', 'g'),
    
    FOREIGN KEY (vprop_id)
      REFERENCES VisualProperties (vprop_id)
      ON DELETE RESTRICT
) ENGINE=InnoDB CHARSET utf8;

CREATE TABLE IF NOT EXISTS ConfigurationOptions (
    vprop_id     BIGINT UNSIGNED,
    option_value VARCHAR(128),
    
    FOREIGN KEY (vprop_id)
      REFERENCES PropertyConfigurations (vprop_id)
      ON DELETE CASCADE
) ENGINE=InnoDB CHARSET utf8;










