CREATE TABLE IF NOT EXISTS plugin_information(
    id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    name varchar(255) UNIQUE NOT NULL,
    enabled INTEGER(1) DEFAULT 1 NOT NULL,
    disabled_at TIMESTAMP,
    enabled_at TIMESTAMP,
    settings TEXT,
    creation_time TIMESTAMP,
    update_time TIMESTAMP,
    version INTEGER
)
