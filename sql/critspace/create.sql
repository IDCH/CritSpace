DROP TABLE IF EXISTS CRIT_PanelProps;
DROP TABLE IF EXISTS CRIT_Panels;
DROP TABLE IF EXISTS CRIT_Workspaces;

-- Defines the primary workspace objects
CREATE TABLE IF NOT EXISTS CRIT_Workspaces (
    ws_id       SERIAL  PRIMARY KEY,
    owner_id    BIGINT UNSIGNED DEFAULT 0,
    name        VARCHAR(255) NOT NULL,         // Max size for UNIQUE INDEX
    visibility  ENUM('private', 'public') DEFAULT 'private',
    
    UNIQUE (owner_id, name)
) ENGINE=InnoDB CHARSET utf8;

-- Record for the panels displayed in a workspace
CREATE TABLE IF NOT EXISTS CRIT_Panels (
    panel_id    SERIAL PRIMARY KEY,
    ws_id       BIGINT UNSIGNED NOT NULL,
    panel_type  VARCHAR(64),
    vprops_grp  BIGINT UNSIGNED,
    
    FOREIGN KEY (ws_id)
      REFERENCES CRIT_Workspaces (ws_id)
      ON DELETE CASCADE,
      
    FOREIGN KEY (vprops_grp)
      REFERENCES PropertyGroups (group_id)
      ON DELETE CASCADE
) ENGINE=InnoDB CHARSET utf8;

-- Stores custom key/value property information for use by third-party
-- panel implementations to record their state  
CREATE TABLE IF NOT EXISTS CRIT_PanelProps (
    panel_id    BIGINT UNSIGNED NOT NULL,
    prop_name   VARCHAR(64),
    prop_value  TEXT,
    
    UNIQUE (panel_id, prop_name),
    FOREIGN KEY (panel_id)
      REFERENCES CRIT_Panels (panel_id)
      ON DELETE CASCADE
) ENGINE=InnoDB CHARSET utf8;
