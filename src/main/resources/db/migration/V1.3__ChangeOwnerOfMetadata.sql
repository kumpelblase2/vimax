PRAGMA foreign_keys=OFF;

CREATE TEMPORARY TABLE METADATA_BAK(
    id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    name VARCHAR(255),
    ordering VARCHAR(255),
    read_only INTEGER NOT NULL,
    system_specified INTEGER,
    display_order INTEGER,
    type INTEGER,
    options TEXT,
    creation_time TIMESTAMP,
    update_time TIMESTAMP,
    version INTEGER
);

INSERT INTO METADATA_BAK SELECT * FROM metadata;
DROP TABLE metadata;
CREATE TABLE metadata(
    id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    name VARCHAR(255),
    ordering VARCHAR(255),
    read_only INTEGER NOT NULL,
    display_order INTEGER,
    type INTEGER,
    options TEXT,
    creation_time TIMESTAMP,
    update_time TIMESTAMP,
    version INTEGER,
    owner_id INTEGER CONSTRAINT metadata_owner_fk REFERENCES plugin_information ON DELETE CASCADE
);

INSERT INTO metadata SELECT
 id, name, ordering, read_only, display_order, type, options, creation_time, update_time, version, null as owner_id
 FROM METADATA_BAK;
DROP TABLE METADATA_BAK;

PRAGMA foreign_keys=ON;