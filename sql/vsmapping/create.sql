DROP TABLE IF EXISTS NT_VerseMapping;

-- Defines a mapping from a facsimile image and a NT verse reference
CREATE TABLE IF NOT EXISTS NT_VerseMapping (
    mapping_id  SERIAL  PRIMARY KEY,
    f_id   BIGINT UNSIGNED NOT NULL,
    img_id VARCHAR(255) NOT NULL,
    x      FLOAT NOT NULL,
    y      FLOAT NOT NULL,
    chpt   SMALLINT NOT NULL,
    vs     SMALLINT NOT NULL,
    book   ENUM('matt',  'mark', 'luke', 'john', 'acts',
                'rom', '1cor', '2cor', 'gal', 'eph', 'phil', 'col',
                '1thess', '2thess', '1tim', '2tim', 'titus', 'phlm',
                'heb', 'jas', '1pet', '2pet', 
                '1john', '2john', '3john', 'jude', 'rev')  NOT NULL
    
) ENGINE=InnoDB CHARSET utf8;