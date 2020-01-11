CREATE TABLE IF NOT EXISTS smart_playlist(
    id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    name VARCHAR(255) UNIQUE NOT NULL,
    query TEXT NOT NULL,
    order_by VARCHAR(255),
    order_direction VARCHAR(255),
    creation_time TIMESTAMP,
    update_time TIMESTAMP,
    version INTEGER
);
